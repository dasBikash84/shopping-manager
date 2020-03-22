package com.dasbikash.exp_man_repo

import android.app.Activity
import android.content.Context
import com.dasbikash.exp_man_repo.firebase.FirebaseAuthService
import com.dasbikash.exp_man_repo.firebase.FirebaseUserService
import com.dasbikash.exp_man_repo.model.User

object AuthRepo:BookKeeperRepo() {

    suspend fun checkLogIn(context: Context):Boolean{
        return getUser(context)!=null
    }

    suspend fun getUser(context: Context): User?{
        getDatabase(context).userDao.findUsers().let {
            if (it.isNotEmpty()){
                return it.get(0)
            }
        }
        return null
    }

    private suspend fun saveUser(context: Context, user: User){
        getDatabase(context).let {
            it.userDao.nukeTable()
            it.userDao.add(user)
        }
    }

    private suspend fun  clearUser(context: Context){
        getDatabase(context).let {
            it.userDao.nukeTable()
        }
    }

    suspend fun createUserWithEmailAndPassword(email:String,password:String,
                                               firstName:String,lastName:String,mobile:String) =
        FirebaseAuthService.createUserWithEmailAndPassword(email, password, firstName, lastName, mobile)

    fun resolveSignUpException(ex:Throwable):String = FirebaseAuthService.resolveSignUpException(ex)

    suspend fun logInUserWithEmailAndPassword(context: Context,email: String, password: String): User {
        FirebaseAuthService.logInUserWithEmailAndPassword(email, password).let {
            try {
                FirebaseUserService.getUser(it)!!.let {
                    saveUser(context,it)
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
                            saveUser(context, it)
                            return it
                        }
                    }else {
                        saveUser(context, it)
                        return it
                    }
                }
            }catch (ex:Throwable){
                FirebaseAuthService.signOut()
                throw ex
            }
        }
    }

    suspend fun signOut(context: Context){
        FirebaseAuthService.signOut()
        clearUser(context)
    }

    suspend fun sendPasswordResetEmail(email: String):Boolean =
        FirebaseAuthService.sendPasswordResetEmail(email)

    suspend fun sendLoginCodeToMobile(phoneNumber:String,activity: Activity) =
        FirebaseAuthService.sendLoginCodeToMobile(phoneNumber, activity)

    suspend fun codeResendWaitMs(context: Context):Long = FirebaseAuthService.codeResendWaitMs(context)
    suspend fun getCurrentMobileNumber(context: Context) = FirebaseAuthService.getCurrentMobileNumber(context)
}