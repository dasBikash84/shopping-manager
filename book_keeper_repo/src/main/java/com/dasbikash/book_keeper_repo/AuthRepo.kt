package com.dasbikash.book_keeper_repo

import android.app.Activity
import android.content.Context
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.firebase.FirebaseAuthService
import com.dasbikash.book_keeper_repo.firebase.FirebaseUserService
import com.dasbikash.book_keeper_repo.model.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object AuthRepo:BookKeeperRepo() {

    fun checkLogIn():Boolean{
        return FirebaseAuthService.getFireBaseUser() != null
    }

    fun getUserId(): String{
        return FirebaseAuthService.getFireBaseUser()!!.uid
    }

    suspend fun getUser(context: Context): User?{
        FirebaseAuthService.getFireBaseUser()?.let {
                return getUserDao(context).findById(it.uid)
            }
        return null
    }

    private suspend fun saveLogin(context: Context, user: User){
        getUserDao(context).add(user)
    }

    suspend fun createUserWithEmailAndPassword(email:String,password:String,
                                               firstName:String,lastName:String,mobile:String) =
        FirebaseAuthService.createUserWithEmailAndPassword(email, password, firstName, lastName, mobile)

    fun resolveSignUpException(ex:Throwable):String = FirebaseAuthService.resolveSignUpException(ex)

    suspend fun logInUserWithEmailAndPassword(context: Context,email: String, password: String): User {
        FirebaseAuthService.logInUserWithEmailAndPassword(email, password).let {
            try {
                FirebaseUserService.getUser(it)!!.let {
                    saveLogin(context,it)
                    return it
                }
            }catch (ex:Throwable){
                FirebaseAuthService.signOut()
                throw ex
            }
        }
    }

    suspend fun logInUserWithVerificationCode(context: Context,code:String): User {
        FirebaseAuthService.logInUserWithVerificationCode(context, code).let {
            try {
                val firebaseUser = it
                FirebaseUserService.getUser(it).let {
                    if (it==null){
                        FirebaseUserService.createUserForPhoneLogin(firebaseUser).let {
                            saveLogin(context, it)
                            return it
                        }
                    }else {
                        saveLogin(context, it)
                        return it
                    }
                }
            }catch (ex:Throwable){
                FirebaseAuthService.signOut()
                throw ex
            }
        }
    }

    fun signOut(context: Context){
        GlobalScope.launch {
            getDatabase(context).clearAllTables()
        }
        FirebaseAuthService.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String):Boolean =
        FirebaseAuthService.sendPasswordResetEmail(email)

    suspend fun sendLoginCodeToMobile(phoneNumber:String,activity: Activity) =
        FirebaseAuthService.sendLoginCodeToMobile(phoneNumber, activity)

    suspend fun codeResendWaitMs(context: Context):Long = FirebaseAuthService.codeResendWaitMs(context)
    suspend fun getCurrentMobileNumber(context: Context) = FirebaseAuthService.getCurrentMobileNumber(context)

    suspend fun findUserById(context: Context,userId:String):User?{
        debugLog("findUserById: ${userId}")
        getUserDao(context).findById(userId)?.let {
            return it
        }
        FirebaseUserService.findUserById(userId)?.let {
            getUserDao(context).add(it)
            return it
        }
        return null
    }

    private fun getUserDao(context: Context) = getDatabase(context).userDao
}