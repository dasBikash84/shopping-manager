package com.dasbikash.book_keeper_repo.firebase

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import com.dasbikash.book_keeper_repo.model.ShoppingListApprovalStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration

internal object FireStoreOnlineSlShareService {

    fun postRequest(onlineSlShareReq: OnlineSlShareReq) {
        debugLog(onlineSlShareReq)
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

    fun setListenerForPendingOnlineDocShareRequest(
        lifecycleOwner: LifecycleOwner,
        onlineSlShareReq: OnlineSlShareReq,
        doOnDocumentChange:(OnlineSlShareReq)->Unit
    ) {
        debugLog("setListenerForPendingOnlineDocShareRequest: ${onlineSlShareReq}")
        PendingOnlineDocShareRequestListener(lifecycleOwner, onlineSlShareReq, doOnDocumentChange)
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
                                    if (it.approvalStatus!=ShoppingListApprovalStatus.PENDING) {
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