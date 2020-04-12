package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dasbikash.book_keeper_repo.AuthRepo
import java.util.*

@Keep
@Entity
data class ConnectionRequest(
    @PrimaryKey
    var id: String=UUID.randomUUID().toString(),
    var requesterUserId: String?=null,
    var partnerUserId: String?=null,
    var approvalStatus: RequestApprovalStatus=RequestApprovalStatus.PENDING,
    var active: Boolean=true,
    var modified: Date = Date()
){
    fun checkIfFromMe():Boolean =  requesterUserId == AuthRepo.getUserId()
    fun checkIfToMe():Boolean = partnerUserId == AuthRepo.getUserId()
    fun checkIfPending():Boolean = approvalStatus == RequestApprovalStatus.PENDING
    fun checkIfApproved():Boolean = approvalStatus == RequestApprovalStatus.APPROVED
    fun refreshModified(){modified = Date()}
}