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
        if (checkIfOnList(context,user)){
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

    suspend fun checkIfOnList(context: Context, user: User): Boolean {
        return getDao(context).findPendingRequest(user.id) > 0
    }

    suspend fun approveRequest(context: Context,user: User){
        getDao(context)
            .findAll(requesterUserId = user.id,partnerUserId = user.id)
            .filter { it.active && it.checkIfPending() }
            .let {
                if (it.isNotEmpty()){
                    approveRequest(context,it.get(0))
                }
            }
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

    suspend fun declineRequest(context: Context,user: User){
        getDao(context)
            .findAll(requesterUserId = user.id,partnerUserId = user.id)
            .filter { it.active && it.checkIfPending() }
            .let {
                if (it.isNotEmpty()){
                    declineRequest(context,it.get(0))
                }
            }
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

    suspend fun deleteApprovedConnection(context: Context, user: User){
        getDao(context)
            .findAll(requesterUserId = user.id,partnerUserId = user.id)
            .filter { it.active && it.checkIfApproved() }
            .let {
                if (it.isNotEmpty()){
                    deleteConnection(context,it.get(0))
                }
            }
    }

    suspend fun deletePendingConnection(context: Context, user: User){
        getDao(context)
            .findAll(requesterUserId = user.id,partnerUserId = user.id)
            .filter { it.active && it.checkIfPending() }
            .let {
                if (it.isNotEmpty()){
                    deleteConnection(context,it.get(0))
                }
            }
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

    fun getLivaDataForApprovedConnections(context: Context) = getDao(context).getLiveDataForApprovedRequests()

    fun getLivaDataForRequestedPending(context: Context) = getDao(context).getLiveDataForRequestedPending()
    fun getLiveDataForReceivedPendingRequests(context: Context) = getDao(context).getLiveDataForReceivedPendingRequests()
    suspend fun getAllActiveConnections(context: Context):List<User> {
        return getDao(context)
                .findByApprovalStatus(currentUserId = AuthRepo.getUserId(),status = RequestApprovalStatus.APPROVED)
                .map {
                    AuthRepo
                        .findUserById(
                            context,
                            if (it.requesterUserId==AuthRepo.getUserId()) {it.partnerUserId} else {it.requesterUserId} ?: ""
                        )
                }.filter { it!=null }.map { it!! }
    }
}