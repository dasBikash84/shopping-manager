package com.dasbikash.book_keeper.application

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.book_keeper.activities.launcher.ActivityLauncher
import com.dasbikash.book_keeper_repo.model.SupportedLanguage
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import com.yariksoffice.lingver.Lingver
import com.yariksoffice.lingver.store.PreferenceLocaleStore
import java.util.*


class BookKeeperApp:Application() {

    override fun onCreate() {
        super.onCreate()
        val store = PreferenceLocaleStore(this, Locale(SupportedLanguage.ENGLISH.language))
        // you can use this instance for DI or get it via Lingver.getInstance() later on
        val lingver = Lingver.init(this, store)
        getLanguageSetting(this).let {
            debugLog("Read: ${it.language}")
            lingver.setLocale(this, it.language,it.country)
        }
    }

    companion object {

        private val defaultLanguage = SupportedLanguage.ENGLISH

        private const val LANGUAGE_SP_KEY =
            "com.dasbikash.book_keeper.application.BookKeeperApp.LANGUAGE_SP_KEY"

        private const val COUNTRY_SP_KEY =
            "com.dasbikash.book_keeper.application.BookKeeperApp.COUNTRY_SP_KEY"

        private fun saveLanguageSetting(context: Context,supportedLanguage: SupportedLanguage){
            debugLog("Saving: ${supportedLanguage.language}")
            SharedPreferenceUtils
                .getDefaultInstance()
                .saveDataSync(context,supportedLanguage.language,LANGUAGE_SP_KEY)
            SharedPreferenceUtils
                .getDefaultInstance()
                .saveDataSync(context,supportedLanguage.country,COUNTRY_SP_KEY)
        }

        fun getLanguageSetting(context: Context) : SupportedLanguage {
            val preferenceUtils = SharedPreferenceUtils.getDefaultInstance()
            if (!preferenceUtils.checkIfExists(context, LANGUAGE_SP_KEY)){
                debugLog("No language Found")
                saveLanguageSetting(context, defaultLanguage)
            }
            val language = preferenceUtils.getData(context,LANGUAGE_SP_KEY, String::class.java)!!
            val country = preferenceUtils.getData(context,COUNTRY_SP_KEY, String::class.java)!!
            debugLog("Found: ${language}")
            return SupportedLanguage
                            .values()
                            .find { it.language == language && it.country==country }!!
        }

        fun changeLanguageSettings(activity: Activity,supportedLanguage: SupportedLanguage){
            saveLanguage(activity, supportedLanguage)
            activity.finish()
            activity.startActivity(ActivityLauncher::class.java)
        }

        fun changeLanguageSettings(activity: Activity,intent: Intent,supportedLanguage: SupportedLanguage){
            saveLanguage(activity, supportedLanguage)
            activity.finish()
            activity.startActivity(intent)
        }

        private fun saveLanguage(
            context: Context,
            supportedLanguage: SupportedLanguage
        ) {
            debugLog("going to change into: ${supportedLanguage.language}")
            saveLanguageSetting(context, supportedLanguage)
            Lingver.getInstance()
                .setLocale(context, supportedLanguage.language, supportedLanguage.country)
        }
    }
}