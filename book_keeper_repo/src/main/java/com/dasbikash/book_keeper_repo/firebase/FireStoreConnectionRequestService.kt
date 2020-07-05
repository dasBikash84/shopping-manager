package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.ConnectionRequest
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FireStoreConnectionRequestService {

    private const val MODIFIED_FIELD = "modified"
    private const val REQUESTER_USER_ID_FIELD = "requesterUserId"
    private const val PARTNER_USER_ID_FIELD = "partnerUserId"

    fun postRequest(connectionRequest: ConnectionRequest,
                    doOnFailure:(suspend ()->Unit)?=null) {
        debugLog(connectionRequest)
        connectionRequest.refreshModified()
        FireStoreRefUtils
            .getConnectionRequestCollectionRef()
            .document(connectionRequest.id)
            .set(connectionRequest)
            .addOnSuccessListener {
                debugLog("Post success")
            }.addOnFailureListener {
                it.printStackTrace()
                debugLog("Post failure: ${it.javaClass.simpleName}")
                doOnFailure?.let {
                    GlobalScope.launch { it.invoke() }
                }
            }

    }

    suspend fun getLatestRequests(lastUpdated: Timestamp?=null):List<ConnectionRequest>{
        val allRequests = mutableListOf<ConnectionRequest>()
        getLatestRequestsToMe(lastUpdated).let { allRequests.addAll(it) }
        getLatestRequestsFromMe(lastUpdated).let { allRequests.addAll(it) }
        return allRequests.toList()
    }

    suspend fun getLatestRequestsToMe(lastUpdated: Timestamp?):List<ConnectionRequest>{

        debugLog("lastUpdated:$lastUpdated")

        var query = FireStoreRefUtils
                                .getConnectionRequestCollectionRef()
                                .whereEqualTo(PARTNER_USER_ID_FIELD, AuthRepo.getUserId())

        if (lastUpdated!=null){
            query = query.whereGreaterThan(MODIFIED_FIELD,lastUpdated)
        }

        return executeQuery(query)
    }

    suspend fun getLatestRequestsFromMe(lastUpdated: Timestamp?):List<ConnectionRequest>{

        debugLog("lastUpdated:$lastUpdated")

        var query = FireStoreRefUtils
                            .getConnectionRequestCollectionRef()
                            .whereEqualTo(REQUESTER_USER_ID_FIELD,AuthRepo.getUserId())

        if (lastUpdated!=null){
            query = query.whereGreaterThan(MODIFIED_FIELD,lastUpdated)
        }

        return executeQuery(query)
    }

    private suspend fun executeQuery(query: Query): List<ConnectionRequest> {
        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects(ConnectionRequest::class.java))
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    continuation.resume(emptyList())
                }
        }
    }
}