package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep
import androidx.room.*
import com.google.firebase.firestore.Exclude
import java.util.*

@Keep
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ExpenseCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"]
        ),
        ForeignKey(
            entity = UnitOfMeasure::class,
            parentColumns = ["id"],
            childColumns = ["unitId"]
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ],
    indices = arrayOf(
        Index(value = ["userId"], unique = false),
        Index(value = ["unitId"], unique = false),
        Index(value = ["categoryId"], unique = false)
    )
)
data class ExpenseEntry(
    @PrimaryKey
    var id:String="",
    var userId:String?=null,
    var time: Date?=null,
    var unitPrice:Double,
    var qty:Int=1,
    var unitId: String?=null,
    var description:String?=null,
    var categoryId: String?=null,
    var categoryProposal:String?=null,
    var productName:String?=null,
//    var brand:String?=null,
    var modified:Long?=null,
    var created:Long?=null
){
    @Ignore
    @Exclude
    var unitOfMeasure: UnitOfMeasure?=null
    @Ignore
    @Exclude
    var expenseCategory: ExpenseCategory?=null
}