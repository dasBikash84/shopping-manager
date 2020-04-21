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

package com.dasbikash.book_keeper.fcm

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_toast_utils.ToastUtils
import com.dasbikash.book_keeper.activities.home.ActivityHome
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

open class BookKeeperMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        debugLog("Refreshed token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        debugLog("From: ${remoteMessage.from} data: ${remoteMessage.data} message: ${remoteMessage.toString()}")
        ToastUtils.showLongToast(applicationContext,"FCM notification: ${remoteMessage.from}")
    }

    companion object {
        private const val BROADCAST_NOTIFICATION_TOPIC_NAME = "bk_broadcast"

        private const val BROADCAST_SUB_SP_KEY =
            "com.dasbikash.book_keeper.fcm.BookKeeperMessagingService.BROADCAST_SUB_SP_KEY"

        private const val USER_SUB_SP_KEY =
            "com.dasbikash.book_keeper.fcm.BookKeeperMessagingService.USER_SUB_SP_KEY"

        const val KEY_FCM_SUBJECT = "bk_subject"
        const val KEY_FCM_KEY = "bk_key"

        fun init(context: Context) {
            val appContext = context.applicationContext
            if (!SharedPreferenceUtils.getDefaultInstance()
                    .checkIfExists(appContext, BROADCAST_SUB_SP_KEY)) {
                subscribeToTopic(BROADCAST_NOTIFICATION_TOPIC_NAME)
                    .addOnSuccessListener {
                        debugLog("BROADCAST_NOTIFICATION sub success")
                        SharedPreferenceUtils.getDefaultInstance()
                            .saveDataSync(appContext, BROADCAST_SUB_SP_KEY, BROADCAST_SUB_SP_KEY)
                    }
                    .addOnFailureListener {
                        debugLog("BROADCAST_NOTIFICATION sub failure")
                        it.printStackTrace()
                    }
            }

            if (AuthRepo.checkLogIn()){
                subscribeToUserNotification(appContext)
            }
        }

        private fun subscribeToUserNotification(appContext: Context) {
            debugLog("subscribeToUserNotification: ${AuthRepo.getUserId()}")

            if (!SharedPreferenceUtils.getDefaultInstance()
                    .checkIfExists(appContext, USER_SUB_SP_KEY)) {

                    subscribeToTopic(AuthRepo.getUserId())
                        .addOnSuccessListener {
                            debugLog("user NOTIFICATION sub success")
                            SharedPreferenceUtils.getDefaultInstance()
                                .saveDataSync(appContext, USER_SUB_SP_KEY, USER_SUB_SP_KEY)
                        }
                        .addOnFailureListener {
                            debugLog("user NOTIFICATION sub failure")
                            it.printStackTrace()
                        }
                }
        }

        private fun unSubscribeFromUserNotification(appContext: Context) {
            debugLog("unSubscribeFromUserNotification: ${AuthRepo.getUserId()}")
            unSubscribeFromTopic(AuthRepo.getUserId())
                .addOnSuccessListener {
                    debugLog("user NOTIFICATION unsub success")
                    SharedPreferenceUtils.getDefaultInstance()
                        .removeKey(appContext, USER_SUB_SP_KEY)
                }
                .addOnFailureListener {
                    debugLog("user NOTIFICATION unsub failure")
                    it.printStackTrace()
                }
        }

        fun subscribeOnLogin(context: Context){
            debugLog("subscribeOnLogin")
            subscribeToUserNotification(context.applicationContext)
        }

        fun unSubscribeOnSignOut(context: Context){
            debugLog("unSubscribeOnSignOut")
            unSubscribeFromUserNotification(context.applicationContext)
        }

        private fun subscribeToTopic(topicName: String): Task<Void> {
            debugLog("subscribeToTopic: $topicName")
            return FirebaseMessaging.getInstance().subscribeToTopic(topicName)
        }

        private fun unSubscribeFromTopic(topicName: String): Task<Void> {
            debugLog("unSubscribeFromTopic: $topicName")
            return FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
        }

        fun resolveIntent(context: Context,fcmSubject: String, fcmKey: String?):Intent? {
            return when{

                fcmSubject== FcmSubjects.CONNECTION.subject ->{
                    ActivityHome.getConnectionIntent(context.applicationContext)
                }

                fcmSubject== FcmSubjects.NEW_SHOPPING_LIST.subject ->{
                    ActivityHome.getShoppingListIntent(context.applicationContext,fcmKey)
                }

                fcmSubject== FcmSubjects.NEW_SHOPPING_LIST_SHARE_REQ.subject ->{
                    ActivityHome.getShoppingListRequestIntent(context.applicationContext)
                }

                else ->{
                    null
                }
            }
        }
        @Keep
        private enum class FcmSubjects(val subject:String){
            CONNECTION("bk_connection"),
            NEW_SHOPPING_LIST("bk_shopping_list"),
            NEW_SHOPPING_LIST_SHARE_REQ("bk_shopping_list_share_req"),
        }
    }

}