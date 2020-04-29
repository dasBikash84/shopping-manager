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
import com.dasbikash.book_keeper_repo.model.NoteEntry

@Dao
internal interface NoteEntryDao {

    @Query("SELECT * FROM NoteEntry WHERE userId=:userId AND active")
    fun findAllLiveData(userId: String = AuthRepo.getUserId()): LiveData<List<NoteEntry>>

    @Query("SELECT * FROM NoteEntry WHERE userId=:userId AND active")
    suspend fun findAll(userId: String = AuthRepo.getUserId()): List<NoteEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(noteEntry: NoteEntry)

    @Query("DELETE FROM NoteEntry")
    suspend fun nukeTable()

    @Query("SELECT * FROM NoteEntry WHERE id=:id AND active")
    suspend fun findById(id: String):NoteEntry?

    @Delete
    suspend fun delete(noteEntry: NoteEntry)
}
