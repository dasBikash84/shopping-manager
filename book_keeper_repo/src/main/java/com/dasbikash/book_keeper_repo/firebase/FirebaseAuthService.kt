package com.dasbikash.book_keeper_repo.firebase

import android.app.Activity
import android.content.Context
import android.os.Parcelable
import com.dasbikash.book_keeper_repo.exceptions.LoginCodeGenerationException
import com.dasbikash.book_keeper_repo.exceptions.SignInException
import com.dasbikash.book_keeper_repo.exceptions.SignUpException
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import java.io.Serializable
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class FirebaseAuthService {

    companion object {

        private const val emailVerificationLinkGenLogSPKey = "com.dasbikash.book_keeper_repo.firebase.FirebaseAuthService.emailVerificationLinkGenLogSPKey"

        private val emailValidator =
            Regex("(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])")

        private const val MIN_EMAIL_LINK_GEN_DELAY_MS = 2*60*1000L

        private data class EmailVerificationLinkGenLog(
            var email:String?=null,
            var time:Long?=null
        ):Serializable

        private fun logEmailVerificationLinkGen(context: Context,email: String){
            val currentLog = mutableListOf<EmailVerificationLinkGenLog>()
            SharedPreferenceUtils
                .getDefaultInstance()
                .getSerializableCollection(
                    context,
                    EmailVerificationLinkGenLog::class.java,
                    emailVerificationLinkGenLogSPKey
                )?.let { currentLog.addAll(it) }
            currentLog.removeAll { it.email == email }
            currentLog.add(
                EmailVerificationLinkGenLog(
                    email=email,time = System.currentTimeMillis()
                )
            )
            SharedPreferenceUtils
                .getDefaultInstance()
                .saveSerializableCollectionSync(context,currentLog, emailVerificationLinkGenLogSPKey)
        }

        private fun getEmailVerificationLinkGenDelay(context: Context,email: String):Long{
            SharedPreferenceUtils
                .getDefaultInstance()
                .getSerializableCollection(
                    context,
                    EmailVerificationLinkGenLog::class.java,
                    emailVerificationLinkGenLogSPKey
                )?.let {
                    it.find { it.email==email }?.let {
                        (it.time!! + MIN_EMAIL_LINK_GEN_DELAY_MS-System.currentTimeMillis()).let {
                            return if(it>0L) {it} else {0L}
                        }
                    }
                }
            return 0
        }

        private const val DUPLICATE_EMAIL_MESSAGE = "Email id already registered!"
        private const val WEAK_PASSWORD_MESSAGE = "Password too weak! Use atleast 6 characters."
        private const val SIGN_UP_FAILURE_MESSAGE = "Sign up failure!"

        private const val VERIFICATION_ID_SP_KEY =
            "com.dasbikash.exp_man_repo.firebase.FirebaseAuthService.VERIFICATION_ID_SP_KEY"
        private const val AUTH_CREDENTIALS_SP_KEY =
            "com.dasbikash.exp_man_repo.firebase.FirebaseAuthService.AUTH_CREDENTIALS_SP_KEY"
        private const val CODE_SEND_TIME_SP_KEY =
            "com.dasbikash.exp_man_repo.firebase.FirebaseAuthService.CODE_SEND_TIME_SP_KEY"
        private const val MOBILE_NUMBER_SP_KEY =
            "com.dasbikash.exp_man_repo.firebase.FirebaseAuthService.MOBILE_NUMBER_SP_KEY"

        private fun validateEmailAddress(emailAddress: CharSequence) =
            emailAddress.trim().toString().toLowerCase(Locale.getDefault()).matches(
                emailValidator
            )

        suspend fun createUserWithEmailAndPassword(
            context: Context,email: String, password: String
        ): String {

            if (!validateEmailAddress(email)) {
                throw SignUpException("Invalid email address format!!")
            }

            return createUser(email, password).let {
                try {
                    generateEmailVerificationLink(context)
                }catch (ex:Throwable){
                    ex.printStackTrace()
                    try {
                        generateEmailVerificationLink(context)
                    }catch (ex:Throwable){
                        ex.printStackTrace()
                    }
                }
                it.uid
            }
        }

        fun resolveSignUpException(ex: SignUpException): String {
            ex.printStackTrace()
            return when (ex.cause) {
                is FirebaseAuthUserCollisionException -> DUPLICATE_EMAIL_MESSAGE
                is FirebaseAuthWeakPasswordException -> ex.message?.split(":")?.let { if (it.size==2) {it.get(1).trim()} else {null} } ?: WEAK_PASSWORD_MESSAGE
                else -> ex.message ?: SIGN_UP_FAILURE_MESSAGE
            }
        }

        private suspend fun createUser(email: String, password: String): FirebaseUser {
            return suspendCoroutine {
                val continuation = it
                FirebaseAuth
                    .getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        if (it.user!=null) {
                            continuation.resume(it.user!!)
                        }else{
                            continuation.resumeWithException(SignUpException())
                        }
                    }.addOnFailureListener {
                        it.printStackTrace()
                        continuation.resumeWithException(SignUpException(it))
                    }
            }
        }

        fun getEmailVerificationLinkGenDelay(context: Context):Long{
            return getEmailVerificationLinkGenDelay(context,getUserForEmailLogin().email!!)
        }

        suspend fun generateEmailVerificationLink(context: Context): Boolean {
            val firebaseUser = getUserForEmailLogin()
            getEmailVerificationLinkGenDelay(context,firebaseUser.email!!).let {
                if (it>0){
                    throw IllegalStateException("Please retry in ${it/1000} secs.")
                }
            }
            return suspendCoroutine {
                        val continuation = it
                        firebaseUser
                            .sendEmailVerification()
                            .addOnSuccessListener {
                                logEmailVerificationLinkGen(context,firebaseUser.email!!)
                                continuation.resume(true)
                            }
                            .addOnFailureListener {
                                it.printStackTrace()
                                continuation.resumeWithException(it)
                            }
                    }
        }

        private fun getUserForEmailLogin(): FirebaseUser {
            if (getFireBaseUser() == null ||
                getFireBaseUser()?.email == null
            ) {
                throw IllegalArgumentException()
            }
            return getFireBaseUser()!!
        }

        private suspend fun deleteUser(firebaseUser: FirebaseUser) {
            return suspendCoroutine {
                val continuation = it
                firebaseUser.delete().addOnCompleteListener {
                    if (it.isSuccessful) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(it.exception ?: RuntimeException())
                    }
                }
            }
        }

        suspend fun logInUserWithEmailAndPassword(email: String, password: String): FirebaseUser {

            return suspendCoroutine {
                val continuation = it
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email.trim(), password)
                    .addOnCompleteListener {
                        if (it.isSuccessful && it.result != null && it.result!!.user != null) {
                            continuation.resume(it.result!!.user!!)
                        } else {
                            continuation.resumeWithException(
                                SignInException(
                                    it.exception
                                )
                            )
                        }
                    }
            }
        }

        fun getFireBaseUser(): FirebaseUser? {
            return FirebaseAuth.getInstance().currentUser
        }

        fun isUserVerified(): Boolean {
            return getFireBaseUser()?.isEmailVerified ?: false
        }

        fun signOut() {
            FirebaseAuth.getInstance().signOut()
        }

        suspend fun sendPasswordResetEmail(email: String): Boolean {
            return suspendCoroutine {
                val continuation = it
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener {
                        continuation.resume(it.isSuccessful)
                    }
            }
        }

        suspend fun codeResendWaitMs(context: Context): Long {
            SharedPreferenceUtils.getDefaultInstance().apply {
                getDataSuspended(context, CODE_SEND_TIME_SP_KEY, Date::class.java).let {
                    if (it == null ||
                        (System.currentTimeMillis() - it.time) > CODE_RESEND_INTERVAL_SEC * 1000
                    ) {
                        return 0
                    } else {
                        return CODE_RESEND_INTERVAL_SEC * 1000 - (System.currentTimeMillis() - it.time)
                    }
                }
            }
        }

        suspend fun getCurrentMobileNumber(context: Context) =
            SharedPreferenceUtils.getDefaultInstance()
                .getDataSuspended(context, MOBILE_NUMBER_SP_KEY, String::class.java)

        private const val CODE_RESEND_INTERVAL_SEC = 60L

        suspend fun sendLoginCodeToMobile(phoneNumber: String, activity: Activity) {
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                CODE_RESEND_INTERVAL_SEC,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                activity,               // Activity (for callback binding)
                getCallBacks()
            )     // OnVerificationStateChangedCallbacks
            val data = channel.receive()
            if (data is Throwable) {
                throw LoginCodeGenerationException(
                    data
                )
            }
            val spu = SharedPreferenceUtils.getDefaultInstance()
            clearPhoneAuthData(activity)
            if (data is String) {
                spu.saveDataSuspended(activity, data, VERIFICATION_ID_SP_KEY)
                spu.saveDataSuspended(activity, Date(), CODE_SEND_TIME_SP_KEY)
                spu.saveDataSuspended(activity, phoneNumber, MOBILE_NUMBER_SP_KEY)
                return
            } else if (data is Parcelable) {
                spu.saveDataSuspended(activity, Date(), CODE_SEND_TIME_SP_KEY)
                spu.saveParcelable(activity, data, AUTH_CREDENTIALS_SP_KEY)
                spu.saveDataSuspended(activity, phoneNumber, MOBILE_NUMBER_SP_KEY)
                return
            }

            throw LoginCodeGenerationException()
        }

        private fun clearPhoneAuthData(
            context: Context
        ) {
            val spu = SharedPreferenceUtils.getDefaultInstance()
            spu.removeKey(context, VERIFICATION_ID_SP_KEY)
            spu.removeKey(context, AUTH_CREDENTIALS_SP_KEY)
            spu.removeKey(context, CODE_SEND_TIME_SP_KEY)
            spu.removeKey(context, MOBILE_NUMBER_SP_KEY)
        }

        val channel = Channel<Any>()

        private fun getCallBacks() =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    println("onVerificationCompleted")
                    GlobalScope.launch { channel.send(credential) }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    println("onVerificationFailed")
                    println(e.javaClass.canonicalName)
                    e.printStackTrace()
                    GlobalScope.launch { channel.send(e) }
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    println("verificationId: $verificationId")
                    GlobalScope.launch { channel.send(verificationId) }
                }
            }

        suspend fun logInUserWithVerificationCode(context: Context, code: String): FirebaseUser {
            return logInUserWithPhoneAuthCredential(getPhoneAuthCredential(context, code)!!)
        }

        suspend fun logInUserWithPhoneAuthCredential(context: Context): FirebaseUser? {
            getPhoneAuthCredential(context)?.let {
                return logInUserWithPhoneAuthCredential(it)
            }
            return null
        }

        private suspend fun logInUserWithPhoneAuthCredential(credential: PhoneAuthCredential): FirebaseUser {
            return suspendCoroutine {
                val continuation = it
                FirebaseAuth.getInstance().signInWithCredential(credential)
                    .addOnCompleteListener {
                        if (it.isSuccessful && it.result != null && it.result!!.user != null) {
                            continuation.resume(it.result!!.user!!)
                        } else {
                            continuation.resumeWithException(
                                SignInException(
                                    it.exception
                                )
                            )
                        }
                    }
            }
        }

        private suspend fun getPhoneAuthCredential(
            context: Context,
            code: String
        ): PhoneAuthCredential? {
            getPhoneAuthCredential(context)?.let {
                return it
            }
            getPhoneAuthCredentialByCode(context, code)?.let {
                return it
            }
            return null
        }

        private suspend fun getPhoneAuthCredential(context: Context): PhoneAuthCredential? {
            SharedPreferenceUtils
                .getDefaultInstance()
                .getParcelableDataSuspended(
                    context, AUTH_CREDENTIALS_SP_KEY, PhoneAuthCredential.CREATOR
                )?.let {
                    return it
                }
            return null
        }

        private suspend fun getPhoneAuthCredentialByCode(
            context: Context,
            code: String
        ): PhoneAuthCredential? {
            SharedPreferenceUtils
                .getDefaultInstance()
                .getDataSuspended(context, VERIFICATION_ID_SP_KEY, String::class.java)?.let {
                    return PhoneAuthProvider.getCredential(it, code)
                }
            return null
        }
    }
}