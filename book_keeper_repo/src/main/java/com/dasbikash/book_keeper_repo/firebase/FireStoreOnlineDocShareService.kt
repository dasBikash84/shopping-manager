package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.model.OnlineDocShareReq

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
}