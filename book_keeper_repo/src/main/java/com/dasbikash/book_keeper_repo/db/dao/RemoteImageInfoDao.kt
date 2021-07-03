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

import androidx.room.*
import com.dasbikash.book_keeper_repo.model.RemoteImageInfo

@Dao
internal interface RemoteImageInfoDao {

    @Query("SELECT count(*) FROM RemoteImageInfo where remotePath is NULL")
    suspend fun getPendingUploadCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(remoteImageInfo: RemoteImageInfo)

    @Delete
    suspend fun delete(remoteImageInfo: RemoteImageInfo)

    @Query("SELECT * FROM RemoteImageInfo WHERE remotePath is NULL AND (~uploadRunning OR modified< :minModified)")
    suspend fun getAllPendingUploadInfo(minModified:Long):List<RemoteImageInfo>

    @Query("DELETE FROM RemoteImageInfo")
    suspend fun nukeTable()
}
