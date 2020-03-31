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

package com.dasbikash.book_keeper_repo.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dasbikash.book_keeper_repo.db.dao.*
import com.dasbikash.book_keeper_repo.db.room_converters.*
import com.dasbikash.book_keeper_repo.model.*

@Database(entities = [ExpenseEntry::class,ExpenseCategory::class,UnitOfMeasure::class,User::class,
                        ShoppingList::class,ShoppingListItem::class,RemoteImageInfo::class],version = 1, exportSchema = false)
@TypeConverters(DateConverter::class, UnitOfMeasureConverter::class, ExpenseCategoryConverter::class,
                ExpenseItemListConverter::class,StringListConverter::class)
internal abstract class EMDatabase internal constructor(): RoomDatabase() {

    abstract val userDao:UserDao
    abstract val expenseEntryDao:ExpenseEntryDao
    abstract val expenseCategoryDao:ExpenseCategoryDao
    abstract val unitOfMeasureDao:UnitOfMeasureDao
    abstract val shoppingListDao:ShoppingListDao
    abstract val shoppingListItemDao:ShoppingListItemDao
    abstract val remoteImageInfoDao:RemoteImageInfoDao

    companion object {
        private val DATABASE_NAME = "ex_man_database"
        @Volatile
        private lateinit var INSTANCE: EMDatabase

        internal fun getDatabase(context: Context): EMDatabase {
            if (!Companion::INSTANCE.isInitialized) {
                synchronized(EMDatabase::class.java) {
                    if (!Companion::INSTANCE.isInitialized) {
                        INSTANCE = Room.databaseBuilder(context.applicationContext,
                                                        EMDatabase::class.java,
                                                        DATABASE_NAME).build()
                    }
                }
            }
            return INSTANCE
        }
    }

}
