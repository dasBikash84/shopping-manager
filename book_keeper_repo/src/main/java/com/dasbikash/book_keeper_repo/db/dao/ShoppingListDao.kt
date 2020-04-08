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
import com.dasbikash.book_keeper_repo.model.ShoppingList

@Dao
internal interface ShoppingListDao {

    @Query("SELECT * FROM  ShoppingList where id=:id AND active")
    fun findByIdLiveData(id: String): LiveData<ShoppingList>

    @Query("SELECT * FROM ShoppingList where id=:id AND active")
    suspend fun findById(id: String): ShoppingList?

    @Query("SELECT * FROM ShoppingList where active ORDER BY modified DESC")
    fun findAllLiveData(): LiveData<List<ShoppingList>>

    @Query("SELECT * FROM ShoppingList where userId=:userId AND active ORDER BY modified DESC")
    fun findAll(userId: String): List<ShoppingList>

    @Query("SELECT * FROM ShoppingList ORDER BY modified DESC")
    suspend fun findAll(): List<ShoppingList>

    @Query("SELECT * FROM ShoppingList where userId=:userId AND title=:title AND active")
    suspend fun findByUserAndTitle(userId: String,title:String): ShoppingList?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(shoppingList: ShoppingList)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAll(shoppingLists: List<ShoppingList>)

    @Delete
    suspend fun delete(shoppingList: ShoppingList)

    @Query("SELECT * FROM ShoppingList where userId=:userId AND deadLine is NOT NULL and countDownTime is NOT NULL AND active")
    suspend fun findAllWithReminder(userId: String): List<ShoppingList>
}
