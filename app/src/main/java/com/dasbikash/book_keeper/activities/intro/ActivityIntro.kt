package com.dasbikash.book_keeper.activities.intro

import android.content.Context
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.book_keeper.activities.login.ActivityLogin
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils

class ActivityIntro : ActivityTemplate() {
    override fun registerDefaultFragment(): FragmentTemplate {
        return when(checkLangSelectedFlag(this)){
            true -> FragmentAppFeatures()
            false -> FragmentLanguageSelection()
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkAppFeaturesShownFlag(this)){
            finish()
            startActivity(ActivityLogin::class.java)
        }
    }

    companion object{
        private const val LANG_SELECTED_SP_KEY =
            "com.dasbikash.book_keeper.activities.intro.ActivityIntro.LANG_SELECTED_SP_KEY"

        private const val APP_FEATURES_SHOWN_SP_KEY =
            "com.dasbikash.book_keeper.activities.intro.ActivityIntro.APP_FEATURES_SHOWN_SP_KEY"

        fun setLangSelectedFlag(context: Context){
            SharedPreferenceUtils.getDefaultInstance().saveDataSync(
                context, LANG_SELECTED_SP_KEY, LANG_SELECTED_SP_KEY)
        }

        fun checkLangSelectedFlag(context: Context):Boolean =
            SharedPreferenceUtils.getDefaultInstance().checkIfExists(context, LANG_SELECTED_SP_KEY)

        fun setAppFeaturesShownFlag(context: Context){
            SharedPreferenceUtils.getDefaultInstance().saveDataSync(
                context, APP_FEATURES_SHOWN_SP_KEY, APP_FEATURES_SHOWN_SP_KEY)
        }

        fun checkAppFeaturesShownFlag(context: Context):Boolean =
            SharedPreferenceUtils.getDefaultInstance().checkIfExists(context, APP_FEATURES_SHOWN_SP_KEY)

    }
}
