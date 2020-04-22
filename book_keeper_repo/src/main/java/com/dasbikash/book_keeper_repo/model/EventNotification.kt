package com.dasbikash.book_keeper_repo.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity
data class EventNotification(
    @PrimaryKey
    var id:String = "",
    var userId:String? = null,
    var title:String?=null,
    var description:String?=null,
    var subject:String?=null,
    var key:String?=null,
    var created:Timestamp?=null
)