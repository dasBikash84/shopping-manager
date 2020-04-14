package com.dasbikash.book_keeper_repo.firebase

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import com.dasbikash.book_keeper_repo.model.RequestApprovalStatus
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FireStoreOnlineSlShareService {

    private const val MODIFIED_FIELD = "modified"
    private const val OWNER_USER_ID_FIELD = "ownerId"
    private const val PARTNER_USER_ID_FIELD = "partnerUserId"

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

    fun setListenerForPendingOnlineDocShareRequest(
        lifecycleOwner: LifecycleOwner,
        onlineSlShareReq: OnlineSlShareReq,
        doOnDocumentChange:(OnlineSlShareReq)->Unit
    ) {
        debugLog("setListenerForPendingOnlineDocShareRequest: ${onlineSlShareReq}")
        PendingOnlineDocShareRequestListener(lifecycleOwner, onlineSlShareReq, doOnDocumentChange)
    }

//    suspend fun getLatestRequests(lastUpdated: Date?=null):List<OnlineSlShareReq>{
//        val allRequests = mutableListOf<OnlineSlShareReq>()
//        getLatestRequestsToMe(lastUpdated)?.let { allRequests.addAll(it) }
//        getLatestRequestsFromMe(lastUpdated)?.let { allRequests.addAll(it) }
//        return allRequests.toList()
//    }

    suspend fun getLatestRequestsToMe(lastUpdated: Date?):List<OnlineSlShareReq>{

        debugLog("lastUpdated:$lastUpdated")

        var query = FireStoreRefUtils
                    .getOnlineSlShareRequestCollectionRef()
                    .whereEqualTo(OWNER_USER_ID_FIELD,AuthRepo.getUserId())

        if (lastUpdated!=null){
            query = query.whereGreaterThan(MODIFIED_FIELD,lastUpdated)
        }

        return executeQuery(query)
    }

    suspend fun getLatestRequestsFromMe(lastUpdated: Date?):List<OnlineSlShareReq>{

        debugLog("lastUpdated:$lastUpdated")
        var query = FireStoreRefUtils
                            .getOnlineSlShareRequestCollectionRef()
                            .whereEqualTo(PARTNER_USER_ID_FIELD,AuthRepo.getUserId())

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
                /*.addOnCompleteListener(OnCompleteListener {
                    if (it.isSuccessful) {
                        try {
                            continuation.resume(it.result!!.toObjects(OnlineSlShareReq::class.java))
                        } catch (ex: Throwable) {
                            continuation.resumeWithException(FbDocumentReadException(ex))
                        }
                    } else {
                        continuation.resumeWithException(it.exception ?: FbDocumentReadException())
                    }
                })*/
        }
    }
}

internal class PendingOnlineDocShareRequestListener(
    lifecycleOwner: LifecycleOwner,
    onlineSlShareReq: OnlineSlShareReq,
    val doOnDocumentChange:(OnlineSlShareReq)->Unit
):DefaultLifecycleObserver{
    private lateinit var listener:ListenerRegistration
    init {
        debugLog("init")
        lifecycleOwner.lifecycle.addObserver(this)
        listener = FireStoreRefUtils
                    .getOnlineSlShareRequestCollectionRef()
                    .document(onlineSlShareReq.id)
                    .addSnapshotListener(object : EventListener<DocumentSnapshot>{
                        override fun onEvent(
                            documentSnapshot: DocumentSnapshot?,
                            exception: FirebaseFirestoreException?
                        ) {
                            debugLog("onEvent")
                            documentSnapshot?.let {
                                debugLog("documentSnapshot?.let")
                                it.toObject(OnlineSlShareReq::class.java)?.let {
                                    debugLog("it.toObject(OnlineDocShareReq::class.java)?")
                                    debugLog(it)
                                    if (it.approvalStatus!=RequestApprovalStatus.PENDING) {
                                        listener.remove()
                                    }
                                    doOnDocumentChange(it)
                                }
                            }
                        }
                    })
    }

    override fun onDestroy(owner: LifecycleOwner) {
        listener.remove()
    }

}