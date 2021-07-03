/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dasbikash.book_keeper.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import com.dasbikash.book_keeper.R

object OptionsIntentBuilderUtility {
    private fun getAppLink(context: Context): String {
        return "<a href=\"https://play.google.com/store/apps/details?id=" +
                context.packageName +
                "\">" + context.resources.getString(R.string.app_name) +
                "</a>"
    }

    fun getShareAppIntent(activity: Activity): Intent {
        return ShareCompat.IntentBuilder.from(activity)
            .setType("text/plain")
            .setSubject(activity.resources.getString(R.string.email_share_app_subject))
            .setChooserTitle(activity.resources.getString(R.string.share_app_chooser_text))
            .setText(getAppLink(activity))
            .createChooserIntent()
    }


    fun launchEmailDialog(activity: Activity,emailAddress:String) {
        activity.startActivity(
            ShareCompat.IntentBuilder.from(activity)
                .setType("text/plain")
                .setSubject(activity.resources.getString(R.string.email_sub))
                .addEmailTo(emailAddress)
                .setChooserTitle("Email to: $emailAddress")
                .createChooserIntent()
        )
    }

    fun dialPartner(activity: Activity,phoneNumber:String) {
        activity.startActivity(
            Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber))
        )
    }
}