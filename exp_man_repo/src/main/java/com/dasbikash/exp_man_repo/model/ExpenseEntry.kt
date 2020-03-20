package com.dasbikash.exp_man_repo.model

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
            entity = ExpenseCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"]
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ],
    indices = arrayOf(
        Index(value = ["userId"], unique = false),
        Index(value = ["categoryId"], unique = false)
    )
)
data class ExpenseEntry(
    @PrimaryKey
    var id:String="",
    var time: Date?=null,
    var categoryId: String?=null,
    var userId: String?=null,
    var unitPrice:Double,
    var qty:Int=1,
    var description:String?=null,
    var categoryProposal:String?=null,
    var productName:String?=null,
    var unitOfMeasure: UnitOfMeasure?=null,
    var expenseCategory: ExpenseCategory?=null,
    var modified:Date=Date(),
    var created:Date=Date()
){
    fun updateModified(){
        modified=Date()
    }
}