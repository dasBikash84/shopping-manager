package com.dasbikash.book_keeper_repo

import android.content.Context
import com.dasbikash.book_keeper_repo.firebase.FireStoreConnectionRequestService
import com.dasbikash.book_keeper_repo.model.ConnectionRequest
import com.dasbikash.book_keeper_repo.model.RequestApprovalStatus
import com.dasbikash.book_keeper_repo.model.User

object ConnectionRequestRepo: BookKeeperRepo()  {

    private fun getDao(context: Context) = getDatabase(context).connectionRequestDao

    suspend fun syncData(context: Context){
        val lastUpdated = getDao(context).findAll().let {
            if (it.isEmpty()){
                return@let null
            }else{
                return@let it.get(0).modified
            }
        }
        FireStoreConnectionRequestService
            .getLatestRequests(lastUpdated).let {
                getDao(context).addAll(it)
            }
    }

    suspend fun submitNewRequest(context: Context,user: User){
        if (user.id == AuthRepo.getUserId()){return}
        syncData(context)
        if (checkIfRequestPending(context,user)){
            return
        }
        val connectionRequest =
            ConnectionRequest(
                requesterUserId = AuthRepo.getUserId(),
                partnerUserId = user.id,
                approvalStatus = RequestApprovalStatus.PENDING,
                active = true)
        getDao(context).add(connectionRequest)
        FireStoreConnectionRequestService
            .postRequest(
                connectionRequest,
                { getDao(context).delete(connectionRequest)}
            )
    }

    private suspend fun checkIfRequestPending(context: Context,user: User): Boolean {
        return getDao(context).findPendingRequest(user.id) > 0
    }

    suspend fun approveRequest(context: Context,connectionRequest: ConnectionRequest){
        syncData(context)
        val subRequest = getDao(context).findById(connectionRequest.id)!!
        if (subRequest.approvalStatus != RequestApprovalStatus.PENDING) {return}
        if (!subRequest.checkIfToMe()) {return}
        connectionRequest.approvalStatus = RequestApprovalStatus.APPROVED
        getDao(context).add(connectionRequest)
        FireStoreConnectionRequestService
            .postRequest(
                connectionRequest,
                {
                    connectionRequest.approvalStatus = RequestApprovalStatus.PENDING
                    getDao(context).add(connectionRequest)
                }
            )
    }

    suspend fun declineRequest(context: Context,connectionRequest: ConnectionRequest){
        syncData(context)
        val subRequest = getDao(context).findById(connectionRequest.id)!!
        if (subRequest.approvalStatus != RequestApprovalStatus.PENDING) {return}
        if (!subRequest.checkIfToMe()) {return}
        connectionRequest.approvalStatus = RequestApprovalStatus.DENIED
        getDao(context).add(connectionRequest)
        FireStoreConnectionRequestService
            .postRequest(
                connectionRequest,
                {
                    connectionRequest.approvalStatus = RequestApprovalStatus.PENDING
                    getDao(context).add(connectionRequest)
                }
            )
    }

    suspend fun deleteConnection(context: Context,connectionRequest: ConnectionRequest){
        syncData(context)
        val subRequest = getDao(context).findById(connectionRequest.id)!!
        if (!subRequest.active) {return}
        connectionRequest.active = false
        getDao(context).add(connectionRequest)
        FireStoreConnectionRequestService
            .postRequest(
                connectionRequest,
                {
                    connectionRequest.active = true
                    getDao(context).add(connectionRequest)
                }
            )
    }

    fun getLivaDataForApprovedConnections(context: Context) =
        getDao(context).getLiveDataForStatus(RequestApprovalStatus.APPROVED)

    fun getLivaDataForPendingConnections(context: Context) =
        getDao(context).getLiveDataForStatus(RequestApprovalStatus.PENDING)
}