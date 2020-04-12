package com.dasbikash.book_keeper_repo

import android.app.Activity
import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.firebase.FirebaseAuthService
import com.dasbikash.book_keeper_repo.firebase.FirebaseUserService
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AuthRepo : BookKeeperRepo() {

    private val PHONE_LOG_IN_PROVIDER_ID = "phone"
    private val PHONE_NUM_PATTERN_FIRST_PLUS = "[+]\\d+"
    private val PHONE_NUM_PATTERN_LEADING_ZEROS = "(00)\\d+"

    fun checkLogIn(): Boolean {
        return FirebaseAuthService.getFireBaseUser() != null
    }

    fun getUserId(): String {
        return FirebaseAuthService.getFireBaseUser()!!.uid
    }

    suspend fun getUser(context: Context): User? {
        FirebaseAuthService.getFireBaseUser()?.let {
            return getUserDao(context).findById(it.uid)
        }
        return null
    }

    fun isPhoneLogin(): Boolean {
        FirebaseAuthService
            .getFireBaseUser()
            ?.providerData
            ?.map { it.providerId.toLowerCase() }
            ?.let {
                return it.contains(PHONE_LOG_IN_PROVIDER_ID)
            }
        return false
    }

    private suspend fun saveLogin(context: Context, user: User) {
        getUserDao(context).add(user)
    }

    suspend fun createUserWithEmailAndPassword(
        email: String, password: String,
        firstName: String, lastName: String, mobile: String
    ) =
        FirebaseAuthService.createUserWithEmailAndPassword(
            email.toLowerCase(),
            password,
            firstName,
            lastName,
            mobile
        )

    fun resolveSignUpException(ex: Throwable): String =
        FirebaseAuthService.resolveSignUpException(ex)

    suspend fun logInUserWithEmailAndPassword(
        context: Context,
        email: String,
        password: String
    ): User {
        FirebaseAuthService.logInUserWithEmailAndPassword(email, password).let {
            try {
                FirebaseUserService.getUser(it)!!.let {
                    saveLogin(context, it)
                    return it
                }
            } catch (ex: Throwable) {
                FirebaseAuthService.signOut()
                throw ex
            }
        }
    }

    suspend fun checkIfAlreadyVerified(context: Context): User? {
        try {
            FirebaseAuthService.logInUserWithPhoneAuthCredential(context)?.let {
                return processPhoneLogin(context, it)
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
        return null
    }

    suspend fun logInUserWithVerificationCode(context: Context, code: String): User {
        return processPhoneLogin(
            context,
            FirebaseAuthService.logInUserWithVerificationCode(context, code)
        )
    }

    private suspend fun processPhoneLogin(context: Context, firebaseUser: FirebaseUser): User {
        try {
            FirebaseUserService.getUser(firebaseUser).let {
                if (it == null) {
                    FirebaseUserService.createUserForPhoneLogin(firebaseUser).let {
                        saveLogin(context, it)
                        return it
                    }
                } else {
                    saveLogin(context, it)
                    return it
                }
            }
        } catch (ex: Throwable) {
            FirebaseAuthService.signOut()
            throw ex
        }
    }

    fun signOut(context: Context) {
        GlobalScope.launch {
            getDatabase(context).clearAllTables()
        }
        FirebaseAuthService.signOut()
    }

    suspend fun sendPasswordResetEmail(email: String): Boolean =
        FirebaseAuthService.sendPasswordResetEmail(email)

    suspend fun sendLoginCodeToMobile(phoneNumber: String, activity: Activity) =
        FirebaseAuthService.sendLoginCodeToMobile(phoneNumber, activity)

    suspend fun codeResendWaitMs(context: Context): Long =
        FirebaseAuthService.codeResendWaitMs(context)

    suspend fun getCurrentMobileNumber(context: Context) =
        FirebaseAuthService.getCurrentMobileNumber(context)

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

    suspend fun refreshUserData(context: Context) {
        FirebaseUserService.getUpdatedUserInfo(getUser(context)!!).let {
            if (it != null) {
                getUserDao(context).add(it)
                debugLog("Updated: $it")
            } else {
                debugLog("No user data update.")
            }
        }
    }

    suspend fun profilePictureEditTask(context: Context, imageUrls: Pair<String, String>) {
        getUser(context)!!.let {
            it.photoUrl = imageUrls.first
            it.thumbPhotoUrl = imageUrls.second
            updateUser(it, context)
        }
    }

    fun isVerified(): Boolean {
        return (FirebaseAuthService.getFireBaseUser() == null) || isPhoneLogin() || FirebaseAuthService.isUserVerified()
    }

    suspend fun sendEmailVerificationLink(context: Context): Long? {
        FirebaseAuthService.getFireBaseUser()?.let {
            getEmailVerificationRequestLog(context)?.apply {
                if (email == it.email) {
                    (System.currentTimeMillis() - sentTime!!.time).let {
                        if (it < VERIFICATION_EMAIL_MINIMUM_INTERVAL) {
                            return VERIFICATION_EMAIL_MINIMUM_INTERVAL - it
                        }
                    }
                }
            }
            try {
                FirebaseAuthService.sendEmailVerificationLink(it)
                saveEmailVerificationRequestLog(context, it.email!!)
                return null
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
        return VERIFICATION_EMAIL_MINIMUM_INTERVAL
    }

    suspend fun refreshLogin(): Boolean {
        return suspendCoroutine<Boolean> {
            val continuation = it
            FirebaseAuthService
                .getFireBaseUser()
                ?.reload()
                ?.addOnSuccessListener {
                    continuation.resume(true)
                }
                ?.addOnFailureListener {
                    it.printStackTrace()
                    continuation.resume(false)
                }
        }
    }

    private const val VERIFICATION_EMAIL_MINIMUM_INTERVAL = DateUtils.MINUTE_IN_MS * 3

    private const val EMAIL_VERIFICATION_REQUEST_LOG_SP_KEY =
        "com.dasbikash.book_keeper_repo.EMAIL_VERIFICATION_REQUEST_LOG_SP_KEY"

    private suspend fun saveEmailVerificationRequestLog(context: Context, email: String) {
        SharedPreferenceUtils
            .getDefaultInstance()
            .saveDataSuspended(
                context,
                EmailVerificationRequestLog(email, Date()),
                EMAIL_VERIFICATION_REQUEST_LOG_SP_KEY
            )
    }

    private suspend fun getEmailVerificationRequestLog(context: Context): EmailVerificationRequestLog? {
        return SharedPreferenceUtils
            .getDefaultInstance()
            .getDataSuspended(
                context,
                EMAIL_VERIFICATION_REQUEST_LOG_SP_KEY,
                EmailVerificationRequestLog::class.java
            )
    }

    suspend fun findUserByEmail(email: String): List<User> {
        try {
            return FirebaseUserService.findUsersByEmail(email)
        } catch (ex: Throwable) {
            ex.printStackTrace()
            return emptyList()
        }
    }

    suspend fun findUserByPhone(phone: String): Flow<User> {
        return flow<User> {
            FirebaseUserService.findUsersByPhone(phone).asSequence().forEach {
                emit(it)
                delay(10)
            }
            getSecondPhoneSearchString(phone)?.let {
                FirebaseUserService.findUsersByPhone(it).asSequence().forEach {
                    emit(it)
                    delay(10)
                }
            }
        }
    }

    private fun getSecondPhoneSearchString(phone: String):String?{
        return if (phone.matches(Regex(PHONE_NUM_PATTERN_FIRST_PLUS))) {
            phone.substring(3)
        } else if (phone.matches(Regex(PHONE_NUM_PATTERN_LEADING_ZEROS))) {
            phone.substring(4)
        } else {
            null
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

}

@Keep
data class EmailVerificationRequestLog(
    var email: String? = null,
    var sentTime: Date? = null
) : Serializable