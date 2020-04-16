package com.dasbikash.book_keeper_repo.model

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.google.firebase.firestore.Exclude
import java.util.*

@Keep
@Entity(
    indices = arrayOf(
        Index(value = ["userId"], unique = false),
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
    var note:String?=null,
    var deadLine: Date?=null,
    var partnerIds:List<String>?=null,
    var modified: Date = Date(),
    var created: Date = Date()
){
    private var reminderInterval:Long?=null
    private var countDownTime:Long?=null

    @Exclude
    var shoppingListItemIds:List<String>?=null

    @Ignore
    var shoppingListItems:List<ShoppingListItem>?=null

    fun getReminderInterval():Long? = reminderInterval
    fun setReminderInterval(ms:Long?){
        reminderInterval = ms
    }

    fun getCountDownTime():Long? = countDownTime
    fun setCountDownTime(ms:Long?){
        countDownTime = ms
    }

    fun updateModified(){this.modified = Date()}

    fun validateCountDownTime():Boolean{
        return if (countDownTime!=null){
            countDownTime!! > MINIMUM_COUNT_DOWN_DELAY
        }else{
            true
        }
    }
    override fun toString(): String {
        return "ShoppingList(id='$id', userId=$userId, partnerIds = $partnerIds, open=$open, active=$active, title=$title, deadLine=$deadLine, " +
                "modified=$modified, created=$created, reminderInterval=$reminderInterval, countDownTime=$countDownTime, " +
                "shoppingListItemIds=$shoppingListItemIds, shoppingListItems=$shoppingListItems), note=$note"
    }


    companion object{
        private val MINIMUM_COUNT_DOWN_DELAY = DateUtils.MINUTE_IN_MS * 15
        private val MINIMUM_DEADLINE_PERIOD = DateUtils.MINUTE_IN_MS * 15
        @Keep
        enum class ReminderInterval(val text:String,val textBangla:String,val intervalMs:Long?){
            ONCE("Once","শুধুমাত্র একবার",null),
            MIN_15("15 minutes","15 মিনিট",15*DateUtils.MINUTE_IN_MS),
            MIN_30("30 minutes","30 মিনিট",30*DateUtils.MINUTE_IN_MS),
            HOUR_1("1 hour","1 ঘণ্টা",1*DateUtils.HOUR_IN_MS),
            HOUR_6("6 hours","6 ঘণ্টা",6*DateUtils.HOUR_IN_MS),
            HOUR_24("24 hours","24 ঘণ্টা",24*DateUtils.HOUR_IN_MS)
        }

        fun validateDeadLine(deadLine: Date):Boolean =
            (deadLine.time - System.currentTimeMillis()) > MINIMUM_DEADLINE_PERIOD

        suspend fun calculateExpenseRange(context: Context,shoppingList: ShoppingList):Pair<Double,Double>{
            var minExp = 0.0
            var maxExp = 0.0
            shoppingList.shoppingListItemIds?.let {
                it.map {
                    ShoppingListRepo.findShoppingListItemById(context, it)?.calculatePriceRange()
                }.asSequence().forEach {
                    it?.first?.let {
                        minExp += it
                    }
                    it?.second?.let {
                        maxExp += it
                    }
                }
            }
            return Pair(minExp,maxExp)
        }
    }
}