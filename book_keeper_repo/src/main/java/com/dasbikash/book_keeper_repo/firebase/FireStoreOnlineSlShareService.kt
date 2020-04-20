package com.dasbikash.book_keeper_repo.firebase

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import com.dasbikash.book_keeper_repo.model.RequestApprovalStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
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

    fun setListenerForPendingOnlineDocShareRequest(
        lifecycleOwner: LifecycleOwner,
        onlineSlShareReq: OnlineSlShareReq,
        doOnDocumentChange:(OnlineSlShareReq)->Unit
    ) {
        debugLog("setListenerForPendingOnlineDocShareRequest: ${onlineSlShareReq}")
        PendingOnlineDocShareRequestListener(lifecycleOwner, onlineSlShareReq, doOnDocumentChange)
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