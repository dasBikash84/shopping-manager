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
    abstract var approvalStatus: ShoppingListApprovalStatus
    abstract var modified: Date

    fun sharedDocumentId():String? = documentPath?.split("/")?.last()
    fun checkIfFromMe():Boolean = partnerUserId == AuthRepo.getUserId()
    fun checkIfToMe():Boolean = ownerId == AuthRepo.getUserId()
    fun checkIfActive():Boolean = approvalStatus == ShoppingListApprovalStatus.PENDING
    fun checkIfApproved():Boolean = approvalStatus == ShoppingListApprovalStatus.APPROVED
    fun refreshModified(){modified = Date()}
}