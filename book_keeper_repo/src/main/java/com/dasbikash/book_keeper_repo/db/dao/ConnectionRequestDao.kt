/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.book_keeper_repo.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.ConnectionRequest
import com.dasbikash.book_keeper_repo.model.RequestApprovalStatus

@Dao
internal interface ConnectionRequestDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(connectionRequest: ConnectionRequest)

    @Query("SELECT * FROM ConnectionRequest WHERE (requesterUserId=:requesterUserId OR partnerUserId=:partnerUserId) AND active ORDER BY modified DESC")
    suspend fun findAll(requesterUserId:String = AuthRepo.getUserId(), partnerUserId:String=AuthRepo.getUserId()):List<ConnectionRequest>

    @Query("SELECT * FROM ConnectionRequest WHERE (requesterUserId=:currentUserId OR partnerUserId=:currentUserId) AND approvalStatus=:status AND active ORDER BY modified DESC")
    suspend fun findByApprovalStatus(currentUserId:String = AuthRepo.getUserId(),status:RequestApprovalStatus = RequestApprovalStatus.APPROVED):List<ConnectionRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(list: List<ConnectionRequest>)

    @Query("SELECT count(*) FROM ConnectionRequest WHERE (requesterUserId=:partnerUserId OR partnerUserId=:partnerUserId) AND approvalStatus !=:statusToNeg  AND active")
    suspend fun findActiveRequests(partnerUserId:String, statusToNeg: RequestApprovalStatus=RequestApprovalStatus.DENIED):Int

    @Delete
    suspend fun delete(connectionRequest: ConnectionRequest)

    @Query("DELETE FROM ConnectionRequest")
    suspend fun nukeTable()

    @Query("SELECT * FROM ConnectionRequest WHERE id=:id  AND active")
    suspend fun findById(id: String): ConnectionRequest?

    @Query("SELECT * FROM ConnectionRequest WHERE (requesterUserId=:userId OR partnerUserId=:userId) AND approvalStatus=:status AND active ORDER BY modified DESC")
    fun getLiveDataForApprovedRequests(status:RequestApprovalStatus=RequestApprovalStatus.APPROVED, userId:String = AuthRepo.getUserId()):LiveData<List<ConnectionRequest>>

    @Query("SELECT * FROM ConnectionRequest WHERE requesterUserId=:userId AND approvalStatus=:status AND active ORDER BY modified DESC")
    fun getLiveDataForRequestedPending(status:RequestApprovalStatus=RequestApprovalStatus.PENDING, userId:String = AuthRepo.getUserId()):LiveData<List<ConnectionRequest>>

    @Query("SELECT * FROM ConnectionRequest WHERE partnerUserId=:userId AND approvalStatus=:status AND active ORDER BY modified DESC")
    fun getLiveDataForReceivedPendingRequests(status:RequestApprovalStatus=RequestApprovalStatus.PENDING, userId:String = AuthRepo.getUserId()):LiveData<List<ConnectionRequest>>
}
