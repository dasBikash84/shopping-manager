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
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ],
    indices = arrayOf(
        Index(value = ["userId"], unique = false),
        Index(value = ["categoryId"], unique = false),
        Index(value = ["details"], unique = false),
        Index(value = ["created"], unique = false)
    )
)
data class ExpenseEntry(
    @PrimaryKey
    var id:String=UUID.randomUUID().toString(),
    var time: Date?=null,
    var userId: String?=null,
    var categoryId: String?=null,
    var expenseCategory: ExpenseCategory?=null,
    var categoryProposal:String?=null,
    var details:String?=null,
    var expenseItems:List<ExpenseItem>?=null,
    var totalExpense:Double?=null,
    var taxVat:Double = 0.0,
    var modified:Date=Date(),
    var created:Date=Date()
){

    fun updateModified(){
        modified=Date()
    }
}