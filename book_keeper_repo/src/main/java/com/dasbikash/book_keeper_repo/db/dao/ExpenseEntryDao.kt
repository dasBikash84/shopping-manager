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
import androidx.sqlite.db.SupportSQLiteQuery
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.google.firebase.Timestamp

@Dao
internal interface ExpenseEntryDao {

    @Query("SELECT * FROM ExpenseEntry where id=:id")
    suspend fun findById(id:String): ExpenseEntry?

    @Query("SELECT time FROM ExpenseEntry where userId=:userId AND active ORDER BY timeTs DESC")
    suspend fun getDates(userId:String): List<Timestamp>

    @Query("SELECT time FROM ExpenseEntry where userId is NULL AND active ORDER BY timeTs DESC")
    suspend fun getDates(): List<Timestamp>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(expenseEntry: ExpenseEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(expenseEntries: List<ExpenseEntry>)

    @Query("DELETE FROM ExpenseEntry where userId IS NOT NULL")
    suspend fun nukeTable()

    @Delete
    suspend fun delete(expenseEntry: ExpenseEntry)

    @RawQuery(observedEntities = [ExpenseEntry::class])
    fun getExpenseEntryLiveDataByRawQuery(simpleSQLiteQuery: SupportSQLiteQuery):LiveData<List<ExpenseEntry>>

    @Query("SELECT id FROM ExpenseEntry where userId=:userId AND timeTs>=:startTime AND timeTs<=:endTime")
    suspend fun getExpenseEntryIds(userId:String,startTime:Long,endTime:Long):List<String>

    @Query("SELECT id FROM ExpenseEntry where userId IS NULL AND timeTs>=:startTime AND timeTs<=:endTime")
    suspend fun getExpenseEntryIds(startTime:Long,endTime:Long):List<String>

    @Query("SELECT sum(totalExpense) FROM ExpenseEntry where userId=:userId AND timeTs>=:startTime AND timeTs<=:endTime")
    suspend fun getTotalExpense(userId:String,startTime:Long,endTime:Long):Double

    @Query("SELECT sum(totalExpense) FROM ExpenseEntry where userId IS NULL AND timeTs>=:startTime AND timeTs<=:endTime")
    suspend fun getTotalExpense(startTime:Long,endTime:Long):Double

    @RawQuery(observedEntities = [ExpenseEntry::class])
    fun getExpenseEntryLiveDataByInRawQuery(simpleSQLiteQuery: SupportSQLiteQuery):LiveData<List<ExpenseEntry>>

    @Query("SELECT max(modified) FROM ExpenseEntry where userId=:userId")
    suspend fun getLatestModifiedTimeForUser(userId:String): Timestamp?

    @Query("SELECT * FROM ExpenseEntry where userId is NULL AND active")
    suspend fun getGuestData(): List<ExpenseEntry>

    @Query("SELECT * FROM ExpenseEntry where userId=:userId AND timeTs>=:periodStart AND timeTs <= :periodEnd AND active ORDER BY timeTs DESC")
    suspend fun findByPeriod(userId:String=AuthRepo.getUserId(),periodStart:Long,periodEnd:Long): List<ExpenseEntry>
}
