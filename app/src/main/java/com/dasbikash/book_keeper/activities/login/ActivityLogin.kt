package com.dasbikash.book_keeper.activities.login

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.dasbikash.book_keeper.activities.launcher.ActivityLauncher
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.application.BookKeeperApp
import com.dasbikash.book_keeper.fcm.BookKeeperMessagingService
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils

class ActivityLogin : ActivityTemplate() {
    override fun registerDefaultFragment(): FragmentTemplate = if (isEmailLoginIntent()) {FragmentLogin.getEmailLoginInstance()} else {FragmentLogin()}

    private fun isEmailLoginIntent() = intent.hasExtra(EXTRA_EMAIL_LOGIN)

    companion object{
        private const val USER_IDS_SP_KEY =
            "com.dasbikash.exp_man.activities.login.ActivityLogin.USER_IDS_SP_KEY"

        private const val EXTRA_EMAIL_LOGIN =
            "com.dasbikash.exp_man.activities.login.ActivityLogin.EXTRA_EMAIL_LOGIN"

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
            if (!user.mobileLogin){
                saveUserId(activity,user.email!!)
            }
            BookKeeperMessagingService.subscribeOnLogin(activity)
            BookKeeperApp.changeLanguageSettings(activity,user.language,ActivityLauncher.getLoggedInIntent(activity))
        }

        fun getEmailLoginIntent(context: Context):Intent{
            val intent = Intent(context.applicationContext,ActivityLogin::class.java)
            intent.putExtra(EXTRA_EMAIL_LOGIN,EXTRA_EMAIL_LOGIN)
            return intent
        }
    }
}