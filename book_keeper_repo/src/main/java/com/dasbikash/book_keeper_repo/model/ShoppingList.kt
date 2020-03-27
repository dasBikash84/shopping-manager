package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.*
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.google.firebase.firestore.Exclude
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
        Index(value = ["userId","title"], unique = true),
        Index(value = ["modified"], unique = false)
    )
)
data class ShoppingList(
    @PrimaryKey
    var id:String= UUID.randomUUID().toString(),
    var userId: String?=null,
    var open:Boolean = true,
    var active:Boolean = true,
    var title:String?=null,
    var deadLine: Date?=null,
    var modified: Date = Date(),
    var created: Date = Date()
){
    private var reminderInterval:Long?=null
    private var countDownTime:Long?=null

    @Exclude
    private var shoppingListItemIds:List<String>?=null

    @Ignore
    var shoppingListItems:List<ShoppingListItem>?=null

    @Exclude
    fun getShoppingListItemIds():List<String>? = shoppingListItems?.map { it.id }
    fun setShoppingListItemIds(ids:List<String>?) {
        this.shoppingListItemIds = ids
    }

    fun getReminderMins():Long? = reminderInterval
    fun setReminderMins(mins:Long?){
        mins?.let {
            if (mins > MINIMUM_REMAINDER_INTERVAL) {
                reminderInterval = mins
            }
        }
    }
    fun getCountDownMins():Long? = countDownTime
    fun setCountDownMins(mins:Long?){
        mins?.let {
            if (mins > MINIMUM_REMAINDER_INTERVAL) {
                countDownTime = mins
            }
        }
    }

    fun updateModified(){this.modified = Date()}

    companion object{
        private val MINIMUM_REMAINDER_INTERVAL = DateUtils.MINUTE_IN_MS * 15
    }
}