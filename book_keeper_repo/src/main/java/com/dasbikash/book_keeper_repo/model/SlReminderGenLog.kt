package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.util.*

@Entity
@Keep
data class SlReminderGenLog(
    @PrimaryKey
    var id:String=UUID.randomUUID().toString(),
    var shoppingListId:String?=null,
    var created:Timestamp = Timestamp.now()
)