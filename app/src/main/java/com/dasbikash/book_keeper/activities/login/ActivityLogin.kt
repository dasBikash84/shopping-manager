package com.dasbikash.book_keeper.activities.login

import android.app.Activity
import android.content.Context
import com.dasbikash.book_keeper.activities.home.ActivityHome
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.application.BookKeeperApp
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils

class ActivityLogin : ActivityTemplate() {
    override fun registerDefaultFragment(): FragmentTemplate = FragmentLogin()

    companion object{
        private const val USER_IDS_SP_KEY =
            "com.dasbikash.exp_man.activities.login.ActivityLogin.USER_IDS_SP_KEY"

        fun getStoredUserIds(context: Context):List<String>{
            return SharedPreferenceUtils.getDefaultInstance().getSerializableCollection(context,String::class.java,USER_IDS_SP_KEY)?.toList() ?: emptyList()
        }

        private fun saveUserId(context: Context, userId:String){
            val currentIds = getStoredUserIds(context).toMutableList()
            if (!currentIds.contains(userId.trim())){
                currentIds.add(userId.trim())
                SharedPreferenceUtils.getDefaultInstance().saveSerializableCollectionSync(context,currentIds,USER_IDS_SP_KEY)
            }
        }

        fun processLogin(activity: Activity,user: User){
            saveUserId(activity,if (user.mobileLogin) {user.phone!!} else {user.email!!})
            BookKeeperApp.changeLanguageSettings(activity,ActivityHome.getProfileIntent(activity),user.language)
        }
    }
}