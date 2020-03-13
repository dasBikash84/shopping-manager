package com.dasbikash.exp_man_repo

import android.content.Context
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils

class LoginRepo {
    companion object{
        private const val USER_ID_SP_KEY = "com.dasbikash.exp_man_repo.USER_ID_SP_KEY"
        private const val PASSWORD_SP_KEY = "com.dasbikash.exp_man_repo.PASSWORD_SP_KEY"

        fun checkLogIn(context: Context):Boolean{
            SharedPreferenceUtils.getDefaultInstance().let {
                return it.checkIfExists(context, USER_ID_SP_KEY) &&
                        it.checkIfExists(context,PASSWORD_SP_KEY)
            }
        }

        private fun saveLogin(context: Context,userId:String,password:String){
            SharedPreferenceUtils.getDefaultInstance().saveData(context,userId, USER_ID_SP_KEY)
            SharedPreferenceUtils.getDefaultInstance().saveData(context,password, PASSWORD_SP_KEY)
        }
    }
}