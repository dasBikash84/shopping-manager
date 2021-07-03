package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import com.dasbikash.book_keeper_repo.AuthRepo
import com.google.firebase.Timestamp

@Keep
abstract class OnlineDocShareReq {
    abstract var id: String
    abstract var partnerId: String?
    abstract var requesterId: String?
    abstract var documentPath: String?
    abstract var approvalStatus: RequestApprovalStatus
    abstract var modified: Timestamp

    fun sharedDocumentId():String? = documentPath?.split("/")?.last()
    fun checkIfFromMe():Boolean = requesterId == AuthRepo.getUserId()
    fun checkIfToMe():Boolean = partnerId == AuthRepo.getUserId()
    fun checkIfActive():Boolean = approvalStatus == RequestApprovalStatus.PENDING
    fun checkIfApproved():Boolean = approvalStatus == RequestApprovalStatus.APPROVED
    fun refreshModified(){modified = Timestamp.now()}
}