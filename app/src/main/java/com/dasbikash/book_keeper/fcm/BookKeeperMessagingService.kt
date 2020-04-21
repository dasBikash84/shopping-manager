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
import com.dasbikash.android_basic_utils.utils.debugLog
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
    }

    companion object {
        private const val BROADCAST_NOTIFICATION_TOPIC_NAME = "bk_broadcast"

        fun init() {
            subscribeToTopic(BROADCAST_NOTIFICATION_TOPIC_NAME)
        }

        fun subscribeToTopic(topicName: String): Task<Void> {
            debugLog("subscribeToTopic: $topicName")
            return FirebaseMessaging.getInstance().subscribeToTopic(topicName)
        }

        fun unSubscribeFromTopic(topicName: String): Task<Void> {
            debugLog("unSubscribeFromTopic: $topicName")
            return FirebaseMessaging.getInstance().unsubscribeFromTopic(topicName)
        }
    }

}