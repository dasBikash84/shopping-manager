package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp
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
        Index(value = ["categoryId"], unique = false),
        Index(value = ["details"], unique = false),
        Index(value = ["timeTs"], unique = false),
        Index(value = ["modified"], unique = false)
    )
)
data class ExpenseEntry(
    @PrimaryKey
    var id:String=UUID.randomUUID().toString(),
    var time: Timestamp?=null,
    var userId: String?=null,
    var categoryId: Int=0,
    var categoryProposal:String?=null,
    var details:String?=null,
    var expenseItems:List<ExpenseItem>?=null,
    var totalExpense:Double?=null,
    var taxVat:Double = 0.0,
    var active:Boolean = true,
    var modified:Timestamp=Timestamp.now(),
    var created:Timestamp= Timestamp.now()
){
    @Exclude
    private var timeTs: Long?=null

    @Exclude
    fun getTimeTs():Long? = time?.toDate()?.time
    @Exclude
    fun setTimeTs(timeTs:Long?){
        this.timeTs = timeTs
    }

    fun updateModified(){
        modified=Timestamp.now()
    }
}