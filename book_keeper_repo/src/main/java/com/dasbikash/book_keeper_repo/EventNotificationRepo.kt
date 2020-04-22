package com.dasbikash.book_keeper_repo

import android.content.Context
import com.dasbikash.book_keeper_repo.firebase.FireStoreEventNotificationService
import com.dasbikash.book_keeper_repo.model.EventNotification

object EventNotificationRepo:BookKeeperRepo() {
    private fun getDao(context: Context) = getDatabase(context).eventNotificationDao

    internal suspend fun syncData(context: Context){
        val latestUpdateTime = getDao(context).findAll().let {
            if (it.isNotEmpty()){
                it.sortedByDescending { it.created }.map { it.created }.first()
            }else{
                null
            }
        }
        FireStoreEventNotificationService
            .getLatestEventNotifications(latestUpdateTime)
            .asSequence()
            .forEach {
                getDao(context).add(it)
            }
    }

    suspend fun delete(context: Context,
               eventNotification: EventNotification){
        FireStoreEventNotificationService
            .deleteEventNotification(
                eventNotification,
                { getDao(context).add(eventNotification)}
            )
        getDao(context).delete(eventNotification)
    }
}