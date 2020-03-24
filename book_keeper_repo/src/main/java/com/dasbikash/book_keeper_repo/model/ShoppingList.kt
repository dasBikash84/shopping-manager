package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
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
data class ShoppingList(
    @PrimaryKey
    var id:String= "",
    var userId: String?=null,
    var open:Boolean = true,
    var title:String?=null,
    var deadLine: Date?=null,
    var itemIds:List<String>?=null,
    var modified: Date = Date(),
    var created: Date = Date()
){
    private var reminderMins:Long?=null
    private var countDownMins:Long?=null

    fun getReminderMins():Long? = reminderMins
    fun setReminderMins(mins:Long?){
        mins?.let {
            if (mins > MINIMUM_REMAINDER_INTERVAL) {
                reminderMins = mins
            }
        }
    }
    fun getCountDownMins():Long? = countDownMins
    fun setCountDownMins(mins:Long?){
        mins?.let {
            if (mins > MINIMUM_REMAINDER_INTERVAL) {
                countDownMins = mins
            }
        }
    }



    companion object{
        private val MINIMUM_REMAINDER_INTERVAL = 15L
    }
}