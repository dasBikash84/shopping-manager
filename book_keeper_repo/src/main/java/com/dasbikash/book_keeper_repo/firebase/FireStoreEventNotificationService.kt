package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.EventNotification
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FireStoreEventNotificationService {

    private const val MODIFIED_FIELD = "modified"
    private const val USER_ID_FIELD = "userId"

    fun deleteEventNotification(eventNotification: EventNotification,
                                    doOnFailure:(suspend ()->Unit)?=null) {
        debugLog("delete: $eventNotification")
        FireStoreRefUtils
            .getEventNotificationCollectionRef()
            .document(eventNotification.id)
            .delete()
            .addOnSuccessListener {
                debugLog("Delete success")
            }.addOnFailureListener {
                it.printStackTrace()
                debugLog("Delete failure: ${it.javaClass.simpleName}")
                doOnFailure?.let {
                    GlobalScope.launch { it.invoke() }
                }
            }

    }

    suspend fun getLatestEventNotifications(lastUpdated: Timestamp?):List<EventNotification>{

        debugLog("lastUpdated:$lastUpdated")

        var query = FireStoreRefUtils
                                .getEventNotificationCollectionRef()
                                .whereEqualTo(USER_ID_FIELD, AuthRepo.getUserId())

        if (lastUpdated!=null){
            query = query.whereGreaterThan(MODIFIED_FIELD,lastUpdated)
        }

        return executeQuery(query)
    }

    private suspend fun executeQuery(query: Query): List<EventNotification> {
        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects(EventNotification::class.java))
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    continuation.resume(emptyList())
                }
        }
    }
}