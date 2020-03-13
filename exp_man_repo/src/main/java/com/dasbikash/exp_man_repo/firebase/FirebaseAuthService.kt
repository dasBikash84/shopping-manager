package com.dasbikash.exp_man_repo.firebase

import android.os.SystemClock
import com.dasbikash.android_basic_utils.utils.runSuspended
import com.dasbikash.exp_man_repo.User
import com.dasbikash.exp_man_repo.firebase.exceptions.SignUpException
import com.google.firebase.auth.*
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object FirebaseAuthService {

    private const val DUPLICATE_EMAIL_MESSAGE = "Email id already registered!"
    private const val WEAK_PASSWORD_MESSAGE = "Password too weak!"
    private const val SIGN_UP_FAILURE_MESSAGE = "Sign up failure!"

    suspend fun createUserWithEmailAndPassword(email:String,password:String,
                                               firstName:String,lastName:String,mobile:String){
        createUser(email, password).let {
            try {
                sendEmailVerification(it)

                val user = User(id = it.uid,email = email.trim())

                firstName.trim().let {
                    if (it.isNotEmpty()){
                        user.firstName = it
                    }
                }

                lastName.trim().let {
                    if (it.isNotEmpty()){
                        user.lastName = it
                    }
                }

                mobile.trim().apply {
                    if (this.isNotEmpty()){
                        user.phone = this
                    }else{
                        user.phone = it.phoneNumber
                    }
                }

                FirebaseUserService.saveUser(user)!!
            }catch (ex:Throwable){
                do {
                    try {
                        deleteUser(it)
                    }catch (e:Throwable){
                        e.printStackTrace()
                        runSuspended { SystemClock.sleep(100) }
                        continue
                    }
                }while (false)
                throw SignUpException(ex)
            }
        }
    }

    fun resolveSignUpException(ex:Throwable):String{
        return when(ex){
            is FirebaseAuthUserCollisionException -> DUPLICATE_EMAIL_MESSAGE
            is FirebaseAuthWeakPasswordException -> ex.message ?: WEAK_PASSWORD_MESSAGE
            else -> SIGN_UP_FAILURE_MESSAGE
        }
    }

    private suspend fun createUser(email:String,password:String):FirebaseUser {
        return suspendCoroutine {
            val continuation = it
            FirebaseAuth
                .getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        if (it.result != null && it.result!!.user != null) {
                            continuation.resume(it.result!!.user!!)
                        } else {
                            continuation.resumeWithException(SignUpException())
                        }
                    } else {
                        continuation.resumeWithException(it.exception ?: SignUpException())
                    }
                }
        }
    }

    private suspend fun sendEmailVerification(firebaseUser: FirebaseUser):FirebaseUser{
        return suspendCoroutine {
            val continuation = it
            firebaseUser.sendEmailVerification().addOnCompleteListener {
                if (it.isSuccessful) {
                    continuation.resume(firebaseUser)
                }else{
                    continuation.resumeWithException(it.exception ?: RuntimeException())
                }
            }
        }
    }

    private suspend fun deleteUser(firebaseUser: FirebaseUser){
        return suspendCoroutine {
            val continuation = it
            firebaseUser.delete().addOnCompleteListener {
                if (it.isSuccessful) {
                    continuation.resume(Unit)
                }else{
                    continuation.resumeWithException(it.exception ?: RuntimeException())
                }
            }
        }
    }

    /*fun logInUserWithEmailAndPassword(email: String, password: String): FirebaseUser? {

        val lock = Object()
        var authException: com.dasbikash.e_bazar_exceptions.AuthException?=null
        var firebaseUser:FirebaseUser?=null

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    firebaseUser = it.result?.user
                    synchronized(lock) { lock.notify() }
                }else{
                    if (it.exception!=null){
                        authException =
                            com.dasbikash.e_bazar_exceptions.AuthException(it.exception!!)
                    }else{
                        authException = com.dasbikash.e_bazar_exceptions.AuthException()
                    }
                    synchronized(lock) { lock.notify() }
                }
            }

        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        authException?.let { throw it }

        return firebaseUser
    }

    fun sendPasswordResetEmail(email: String){

        val lock = Object()
        var authException: com.dasbikash.e_bazar_exceptions.AuthException?=null

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (!it.isSuccessful){
                    if (it.exception!! is FirebaseAuthInvalidUserException){
                        authException =
                            com.dasbikash.e_bazar_exceptions.InvalidEmailAddressException()
                    }else {
                        authException = com.dasbikash.e_bazar_exceptions.AuthException()
                    }
                }
                synchronized(lock) { lock.notify() }
            }

        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        authException?.let { throw it }
    }

    fun getFireBaseUser(): FirebaseUser? {
        return FirebaseAuth.getInstance().currentUser
    }

    fun resendVerificationEmail():Boolean{
        val lock = Object()
        var result:Boolean = false

        getFireBaseUser()
            ?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    result = true
                } else {
                    result=false
                }
                synchronized(lock) { lock.notify() }
            }
        }

        try {
            synchronized(lock) { lock.wait(WAITING_MS_FOR_NET_RESPONSE) }
        }catch (ex:InterruptedException){}

        return result
    }

    fun getUserEmail(): String? {
        return getFireBaseUser()
            ?.email
    }

    fun reloadUser(doOnSuccess: () -> Unit) {
        getFireBaseUser()?.reload()?.addOnCompleteListener {
            if (it.isSuccessful) {
                doOnSuccess()
            }
        }
    }

    fun isUserVerified(): Boolean {
        return getFireBaseUser()?.isEmailVerified ?: false
    }

    fun getIdToken(status: Boolean, doOnSuccess: () -> Unit) {
        getFireBaseUser()?.getIdToken(status)?.addOnCompleteListener {
            if (it.isSuccessful) {
                doOnSuccess()
            }
        }
    }*/

}