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
    var requesterUserId: String?,
    var partnerUserId: String?,
    var approvalStatus: RequestApprovalStatus,
    var active: Boolean=true,
    var modified: Date = Date()
){
    fun checkIfFromMe():Boolean =  requesterUserId == AuthRepo.getUserId()
    fun checkIfToMe():Boolean = partnerUserId == AuthRepo.getUserId()
    fun checkIfActive():Boolean = approvalStatus == RequestApprovalStatus.PENDING
    fun checkIfApproved():Boolean = approvalStatus == RequestApprovalStatus.APPROVED
    fun refreshModified(){modified = Date()}
}