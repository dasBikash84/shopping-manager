package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import com.dasbikash.book_keeper_repo.AuthRepo
import java.util.*

@Keep
abstract class OnlineDocShareReq {
    abstract var id: String
    abstract var ownerId: String?
    abstract var partnerUserId: String?
    abstract var documentPath: String?
    abstract var approvalStatus: RequestApprovalStatus
    abstract var modified: Date

    fun sharedDocumentId():String? = documentPath?.split("/")?.last()
    fun checkIfFromMe():Boolean = partnerUserId == AuthRepo.getUserId()
    fun checkIfToMe():Boolean = ownerId == AuthRepo.getUserId()
    fun checkIfActive():Boolean = approvalStatus == RequestApprovalStatus.PENDING
    fun checkIfApproved():Boolean = approvalStatus == RequestApprovalStatus.APPROVED
    fun refreshModified(){modified = Date()}
}