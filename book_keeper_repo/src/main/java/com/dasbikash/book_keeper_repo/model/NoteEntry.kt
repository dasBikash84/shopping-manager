package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
import java.util.*

@Keep
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ],
    indices = arrayOf(
        Index(value = ["userId"], unique = false),
        Index(value = ["modified"], unique = false)
    )
)
data class NoteEntry(
    @PrimaryKey
    var id:String= UUID.randomUUID().toString(),
    var userId: String?=null,
    var active:Boolean = true,
    var title:String?=null,
    var note:String?=null,
    var modified: Timestamp = Timestamp.now()
){
    fun updateModified() {
        this.modified = Timestamp.now()
    }
}