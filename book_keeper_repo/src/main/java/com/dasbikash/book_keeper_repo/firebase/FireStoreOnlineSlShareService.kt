package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FireStoreOnlineSlShareService {

    private const val MODIFIED_FIELD = "modified"
    private const val PARTNER_USER_ID_FIELD = "partnerId"
    private const val REQUESTER_USER_ID_FIELD = "requesterId"

    fun postRequest(onlineSlShareReq: OnlineSlShareReq) {
        debugLog(onlineSlShareReq)
        onlineSlShareReq.refreshModified()
        FireStoreRefUtils
            .getOnlineSlShareRequestCollectionRef()
            .document(onlineSlShareReq.id)
            .set(onlineSlShareReq)
            .addOnSuccessListener {
                debugLog("Post success")
            }.addOnFailureListener {
                it.printStackTrace()
                debugLog("Post failure: ${it.javaClass.simpleName}")
            }

    }

    fun saveRequest(onlineSlShareReq: OnlineSlShareReq) {
        postRequest(onlineSlShareReq)
    }

    suspend fun getLatestRequestsToMe(lastUpdated: Timestamp?):List<OnlineSlShareReq>{

        debugLog("lastUpdated:$lastUpdated")

        var query = FireStoreRefUtils
                    .getOnlineSlShareRequestCollectionRef()
                    .whereEqualTo(PARTNER_USER_ID_FIELD,AuthRepo.getUserId())

        if (lastUpdated!=null){
            query = query.whereGreaterThan(MODIFIED_FIELD,lastUpdated)
        }

        return executeQuery(query)
    }

    suspend fun getLatestRequestsFromMe(lastUpdated: Timestamp?):List<OnlineSlShareReq>{

        debugLog("lastUpdated:$lastUpdated")
        var query = FireStoreRefUtils
                            .getOnlineSlShareRequestCollectionRef()
                            .whereEqualTo(REQUESTER_USER_ID_FIELD,AuthRepo.getUserId())

        if (lastUpdated!=null){
            query = query.whereGreaterThan(MODIFIED_FIELD,lastUpdated)
        }

        return executeQuery(query)
    }

    private suspend fun executeQuery(query: Query): List<OnlineSlShareReq>{
        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects(OnlineSlShareReq::class.java))
                }.addOnFailureListener {
                    it.printStackTrace()
                    continuation.resume(emptyList())
                }
        }
    }
}