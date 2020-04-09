package com.dasbikash.book_keeper_repo

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.firebase.FirebaseAuthService
import com.dasbikash.book_keeper_repo.firebase.FirebaseUserService
import com.dasbikash.book_keeper_repo.model.User
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object AuthRepo:BookKeeperRepo() {
    private val PHONE_LOG_IN_PROVIDER_ID="phone"

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

    fun isPhoneLogin():Boolean{
        FirebaseAuthService
            .getFireBaseUser()
            ?.providerData
            ?.map { it.providerId.toLowerCase() }
            ?.let {
                return it.contains(PHONE_LOG_IN_PROVIDER_ID)
            }
        return false
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

    fun getUserLiveDate(context: Context): LiveData<User> =
        getUserDao(context).getUserLiveDate(getUserId())

    private suspend fun updateUser(
        user: User,
        context: Context
    ) {
        FirebaseUserService.saveUser(user)
        getUserDao(context).add(user)
    }

    suspend fun updateUserEmail(context: Context, inputEmail: String) {
        getUser(context)!!.let {
            it.email = inputEmail
            updateUser(it, context)
        }
    }

    suspend fun updatePhone(context: Context, inputPhone: String) {
        getUser(context)!!.let {
            it.phone = inputPhone
            updateUser(it, context)
        }
    }

    suspend fun updateFirstName(context: Context, inputFirstName: String) {
        getUser(context)!!.let {
            it.firstName = inputFirstName
            updateUser(it, context)
        }
    }

    suspend fun updateLastName(context: Context, inputLastName: String) {
        getUser(context)!!.let {
            it.lastName = inputLastName
            updateUser(it, context)
        }
    }

    suspend fun refreshUserData(context: Context){
        FirebaseUserService.getUpdatedUserInfo(getUser(context)!!).let {
            if (it!=null) {
                getUserDao(context).add(it)
                debugLog("Updated: $it")
            }else{
                debugLog("No user data update.")
            }
        }
    }
}