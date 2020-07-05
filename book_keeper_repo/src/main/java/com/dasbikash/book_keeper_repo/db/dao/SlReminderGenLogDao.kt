package com.dasbikash.book_keeper_repo.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dasbikash.book_keeper_repo.model.SlReminderGenLog

@Dao
interface SlReminderGenLogDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(slReminderGenLog: SlReminderGenLog)

    @Query("SELECT * FROM SlReminderGenLog where shoppingListId=:shoppingListId")
    suspend fun findByShoppingListId(shoppingListId:String):List<SlReminderGenLog>

    @Query("DELETE FROM SlReminderGenLog where shoppingListId=:shoppingListId")
    suspend fun deleteByShoppingListId(shoppingListId:String)

    @Query("DELETE FROM SlReminderGenLog")
    suspend fun nukeTable()
}