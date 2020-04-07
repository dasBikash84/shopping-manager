package com.dasbikash.book_keeper_repo.firebase

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.OnlineDocShareReq
import com.dasbikash.book_keeper_repo.model.ShoppingListApprovalStatus
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration

internal object FireStoreOnlineDocShareService {

    fun postRequest(onlineDocShareReq: OnlineDocShareReq) {
        debugLog(onlineDocShareReq)
        FireStoreRefUtils
            .getOnlineDocShareRequestCollectionRef()
            .document(onlineDocShareReq.id)
            .set(onlineDocShareReq)
            .addOnSuccessListener {
                debugLog("Post success")
            }.addOnFailureListener {
                it.printStackTrace()
                debugLog("Post failure: ${it.javaClass.simpleName}")
            }

    }

    fun setListenerForPendingOnlineDocShareRequest(
        lifecycleOwner: LifecycleOwner,
        onlineDocShareReq: OnlineDocShareReq,
        doOnDocumentChange:(OnlineDocShareReq)->Unit
    ) {
        debugLog("setListenerForPendingOnlineDocShareRequest: ${onlineDocShareReq}")
        PendingOnlineDocShareRequestListener(lifecycleOwner, onlineDocShareReq, doOnDocumentChange)
    }
}

internal class PendingOnlineDocShareRequestListener(
    lifecycleOwner: LifecycleOwner,
    onlineDocShareReq: OnlineDocShareReq,
    val doOnDocumentChange:(OnlineDocShareReq)->Unit
):DefaultLifecycleObserver{
    private lateinit var listener:ListenerRegistration
    init {
        debugLog("init")
        lifecycleOwner.lifecycle.addObserver(this)
        listener = FireStoreRefUtils
                    .getOnlineDocShareRequestCollectionRef()
                    .document(onlineDocShareReq.id)
                    .addSnapshotListener(object : EventListener<DocumentSnapshot>{
                        override fun onEvent(
                            documentSnapshot: DocumentSnapshot?,
                            exception: FirebaseFirestoreException?
                        ) {
                            debugLog("onEvent")
                            documentSnapshot?.let {
                                debugLog("documentSnapshot?.let")
                                it.toObject(OnlineDocShareReq::class.java)?.let {
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