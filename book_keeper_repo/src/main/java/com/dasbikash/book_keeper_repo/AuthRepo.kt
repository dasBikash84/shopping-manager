package com.dasbikash.book_keeper_repo

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.firebase_auth.exceptions.SignUpException
import com.dasbikash.firebase_auth.FirebaseAuthService
import com.dasbikash.book_keeper_repo.firebase.FirebaseUserService
import com.dasbikash.book_keeper_repo.model.SupportedLanguage
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.book_keeper_repo.utils.ValidationUtils
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

object AuthRepo : BookKeeperRepo() {

    private const val MOBILE_NUMBER_SP_KEY =
        "com.dasbikash.exp_man_repo.AuthRepo.MOBILE_NUMBER_SP_KEY"

    fun checkLogIn(): Boolean {
        return getUserId().isNotBlank()
    }

    fun getUserId(): String {
        return FirebaseAuthService.getUserId() ?: ""
    }

    suspend fun getUser(context: Context): User? {
        return if (checkLogIn()) {
            getUserDao(context).findById(getUserId())
        }else{
            null
        }
    }

    suspend fun isPhoneLogin(context: Context): Boolean {
        return getUser(context)?.mobileLogin == true
    }

    private suspend fun saveLogin(context: Context, user: User) {
        getUserDao(context).add(user)
    }

    suspend fun createUserWithEmailAndPassword(
        context: Context,email: String, password: String,
        firstName: String, lastName: String, mobile: String,
        language: SupportedLanguage
    ):User {
        FirebaseAuthService
            .createUserWithEmailAndPassword(
                context,email.trim().toLowerCase(Locale.ENGLISH),password
            ).let {
                createUser(getUserId(),email,firstName, lastName, mobile,language)
                    .let {
                        saveLogin(context,it)
                        return it
                    }
            }
    }

    private fun createUser(
        userId: String,email: String,
        firstName: String, lastName: String, mobile: String,
        language: SupportedLanguage
    ):User {
        return User().apply {
            id = userId
            this.email = email.trim().toLowerCase(Locale.ENGLISH)
            this.firstName = firstName.trim()
            this.lastName = lastName.trim()
            phone = mobile.trim()
            mobileLogin = false
            this.language=language
            FirebaseUserService.saveUser(this)
        }
    }

    fun resolveSignUpException(ex: Throwable): String =
        if (ex is SignUpException) {FirebaseAuthService.resolveSignUpException(ex)} else {ex.message ?: ex.cause?.message ?: ex.javaClass.simpleName}

    suspend fun logInUserWithEmailAndPassword(
        context: Context,
        email: String,
        password: String
    ): User {
        FirebaseAuthService.logInUserWithEmailAndPassword(email, password).let {
            try {
                FirebaseUserService.getUser(getUserId())!!.let {
                    saveLogin(context, it)
                    return it
                }
            } catch (ex: Throwable) {
                FirebaseAuthService.signOut()
                throw ex
            }
        }
    }

    suspend fun checkIfAlreadyVerified(context: Context,
                                       language: SupportedLanguage): User? {
        try {
            FirebaseAuthService.logInUserWithPhoneAuthCredential(context).let {
                return processPhoneLogin(context,language)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return null
    }

    suspend fun logInUserWithVerificationCode(context: Context, code: String,
                                              language: SupportedLanguage): User {
        return FirebaseAuthService.logInUserWithVerificationCode(context, code).let {
            processPhoneLogin(context,language)
        }
    }

    private suspend fun processPhoneLogin(context: Context,
                                          language: SupportedLanguage): User {
        try {
            FirebaseUserService.getUser(getUserId()).let {
                return if (it == null) {
                    FirebaseUserService.createUserForPhoneLogin(getCurrentMobileNumber(context)!!,language).let {
                        saveLogin(context, it)
                        it
                    }
                } else {
                    saveLogin(context, it)
                    it
                }.apply {
                    clearCurrentMobileNumber(context)
                }
            }
        } catch (ex: Throwable) {
            FirebaseAuthService.signOut()
            throw ex
        }
    }

    fun signOut(context: Context) {
        GlobalScope.launch {
            getDatabase(context).clearData()
            enableGuestDataImport(context)
        }
        FirebaseAuthService.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean =
        FirebaseAuthService.sendPasswordResetEmail(email)

    suspend fun sendLoginCodeToMobile(phoneNumber: String?, activity: Activity) {
        (phoneNumber ?: getCurrentMobileNumber(activity))?.let {
            debugLog("phoneNumber: $phoneNumber")
            FirebaseAuthService.sendLoginCodeToMobile(it, activity)
            saveCurrentMobileNumber(activity,it)
        }
    }

    suspend fun codeResendWaitMs(context: Context): Long =
        FirebaseAuthService.codeResendWaitMs(context)

    private suspend fun getCurrentMobileNumber(context: Context):String? {
        return SharedPreferenceUtils.getDefaultInstance()
                .getDataSuspended(context, MOBILE_NUMBER_SP_KEY, String::class.java)
    }
    private fun clearCurrentMobileNumber(context: Context) {
        return SharedPreferenceUtils.getDefaultInstance().removeKey(context, MOBILE_NUMBER_SP_KEY)
    }

    private suspend fun saveCurrentMobileNumber(context: Context,phone: String){
        return SharedPreferenceUtils.getDefaultInstance()
                .saveDataSuspended(context, phone,MOBILE_NUMBER_SP_KEY)
    }

    suspend fun findUserById(context: Context, userId: String): User? {
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
        getUser(context)?.let {
            it.email = inputEmail
            updateUser(it, context)
        }
    }

    suspend fun updateUserLanguage(context: Context, language: SupportedLanguage) {
        getUser(context)?.let {
            it.language = language
            updateUser(it, context)
        }
    }

    suspend fun updatePhone(context: Context, inputPhone: String) {
        getUser(context)?.let {
            it.phone = inputPhone
            updateUser(it, context)
        }
    }

    suspend fun updateFirstName(context: Context, inputFirstName: String) {
        getUser(context)?.let {
            it.firstName = inputFirstName
            updateUser(it, context)
        }
    }

    suspend fun updateLastName(context: Context, inputLastName: String) {
        getUser(context)?.let {
            it.lastName = inputLastName
            updateUser(it, context)
        }
    }

    suspend fun refreshUserData(context: Context) {
        getUser(context)?.let {
            FirebaseUserService.getUpdatedUserInfo(it)?.let {
                    getUserDao(context).add(it)
            }
        }
    }

    suspend fun profilePictureEditTask(context: Context, imageUrls: Pair<String, String>) {
        getUser(context)?.let {
            it.photoUrl = imageUrls.first
            it.thumbPhotoUrl = imageUrls.second
            updateUser(it, context)
        }
    }

    fun isVerified(): Boolean {
        return FirebaseAuthService.isUserVerified()
    }

    fun getEmailVerificationLinkGenDelay(context:Context):Long = FirebaseAuthService.getEmailVerificationLinkGenDelay(context)

    suspend fun sendEmailVerificationLink(context:Context){
        FirebaseAuthService.generateEmailVerificationLink(context)
    }

    suspend fun refreshLogin(): Boolean {
        return FirebaseAuthService.refreshLogin()
    }

    suspend fun searchUser(searchString: String):List<User>{
        val sanitizedString = searchString.trim().toLowerCase(Locale.ENGLISH)
        if (sanitizedString.isBlank()){return emptyList()}
        return when{
            ValidationUtils.validateEmailAddress(sanitizedString) -> {
                findUserByEmail(sanitizedString)
            }
            ValidationUtils.validateMobileNumber(sanitizedString) -> {
                findUsersByPhone(sanitizedString)
            }
            else -> emptyList()
        }.filter{ it.id != getUserId() }
    }

    suspend fun findUserByEmail(email: String): List<User> {
        try {
            return FirebaseUserService.findUsersByEmail(email)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            return emptyList()
        }
    }

    suspend fun findEmailLoginUsersByPhone(phone: String): List<User> {
        debugLog("phone: $phone")
        FirebaseUserService
            .findEmailLoginUsersByPhone(phone).let {
                debugLog(it)
                if (it.isNotEmpty()){
                    return it
                }
            }
        return emptyList()
    }

    suspend fun findUsersByPhone(phone: String): List<User> {
        debugLog("phone: $phone")
        return FirebaseUserService
            .findUsersByPhone(phone).apply {
                debugLog(this)
            }
    }

    suspend fun syncUserData(context: Context) {
        getUserDao(context)
            .findUsers()
            .map {FirebaseUserService.getUpdatedUserInfo(it)}.let {
                it.forEach {
                    it?.let { getUserDao(context).add(it) }
                }
            }
    }

    suspend fun findUsersForPhoneLogin(phone: String):List<User>{
        return FirebaseUserService.findUsersForPhoneLogin(phone)
    }

    suspend fun loginAnonymous():Boolean{
        return FirebaseUserService.loginAnonymous()
    }
}