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
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import com.dasbikash.book_keeper_repo.model.RequestApprovalStatus
import java.util.*

@Dao
internal interface OnlineSlShareReqDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(onlineSlShareReq: OnlineSlShareReq)

    @Query("SELECT * FROM OnlineSlShareReq WHERE partnerId=:ownerId OR requesterId=:partnerUserId ORDER BY modified ASC")
    suspend fun findAll(ownerId:String = AuthRepo.getUserId(), partnerUserId:String=AuthRepo.getUserId()):List<OnlineSlShareReq>

    @Query("SELECT * FROM OnlineSlShareReq WHERE documentPath=:documentPath AND requesterId=:partnerUserId")
    suspend fun findByDocumentPathAndPartnerId(documentPath:String, partnerUserId:String=AuthRepo.getUserId()):OnlineSlShareReq?

    @Query("SELECT * FROM OnlineSlShareReq WHERE modified >= :leastModifiedTime AND requesterId=:partnerUserId")
    fun getRecentModifiedEntries(leastModifiedTime: Date=Date(),
                                 partnerUserId: String=AuthRepo.getUserId()):LiveData<List<OnlineSlShareReq>>

    @Query("SELECT * FROM OnlineSlShareReq WHERE partnerId=:ownerId AND approvalStatus=:approvalStatus ORDER BY modified DESC")
    fun getApprovalPendingEntries(ownerId:String = AuthRepo.getUserId(),
                                  approvalStatus:RequestApprovalStatus = RequestApprovalStatus.PENDING):LiveData<List<OnlineSlShareReq>>

    @Query("DELETE FROM OnlineSlShareReq")
    suspend fun nukeTable()
}
