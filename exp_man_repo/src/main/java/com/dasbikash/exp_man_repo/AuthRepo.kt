package com.dasbikash.exp_man_repo

import android.content.Context
import com.dasbikash.exp_man_repo.firebase.FirebaseAuthService
import com.dasbikash.exp_man_repo.firebase.FirebaseUserService
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils

object AuthRepo {
    private const val USER_SP_KEY = "com.dasbikash.exp_man_repo.USER_SP_KEY"

    fun checkLogIn(context: Context):Boolean{
        SharedPreferenceUtils.getDefaultInstance().let {
            return it.checkIfExists(context, USER_SP_KEY)
        }
    }

    suspend fun getUser(context: Context):User?{
        return SharedPreferenceUtils.getDefaultInstance().getDataSuspended(context,USER_SP_KEY,User::class.java)
    }

    private suspend fun saveUser(context: Context, user: User){
        SharedPreferenceUtils.getDefaultInstance().saveDataSuspended(context,user,USER_SP_KEY)
    }

    private fun clearUser(context: Context){
        SharedPreferenceUtils.getDefaultInstance().removeKey(context,USER_SP_KEY)
    }

    suspend fun createUserWithEmailAndPassword(email:String,password:String,
                                               firstName:String,lastName:String,mobile:String) =
        FirebaseAuthService.createUserWithEmailAndPassword(email, password, firstName, lastName, mobile)

    fun resolveSignUpException(ex:Throwable):String = FirebaseAuthService.resolveSignUpException(ex)

    suspend fun logInUserWithEmailAndPassword(context: Context,email: String, password: String):User{
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

    fun signOut(context: Context){
        FirebaseAuthService.signOut()
        clearUser(context)
    }

    suspend fun sendPasswordResetEmail(email: String):Boolean =
        FirebaseAuthService.sendPasswordResetEmail(email)
}