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
            parentColumns = ["name"],
            childColumns = ["categoryName"]
        ),
        ForeignKey(
            entity = UnitOfMeasure::class,
            parentColumns = ["name"],
            childColumns = ["unitName"]
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"]
        )
    ],
    indices = arrayOf(
        Index(value = ["userId"], unique = false),
        Index(value = ["unitName"], unique = false),
        Index(value = ["categoryName"], unique = false)
    )
)
data class ExpenseEntry(
    @PrimaryKey
    var id:String="",
    var userId:String?=null,
    var time: Date?=null,
    var unitPrice:Double,
    var qty:Int=1,
    var unitName: String?=null,
    var description:String?=null,
    var categoryName: String?=null,
    var categoryProposal:String?=null,
    var productName:String?=null,
//    var brand:String?=null,
    var modified:Long?=null,
    var created:Long?=null
)