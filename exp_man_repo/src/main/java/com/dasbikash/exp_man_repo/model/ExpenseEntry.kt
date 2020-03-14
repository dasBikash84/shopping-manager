package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep
import java.util.*

@Keep
data class ExpenseEntry(
    var id:String,
    var userId:String?=null,
    var time: Date,
    var unitPrice:Double,
    var qty:Int,
    var unit: UnitOfMeasure,
    var description:String,
    var category: ExpenseCategory,
    var categoryProposal:String?=null,
    var productName:String?=null,
    var brand:String?=null,
    var modified:Long,
    var created:Long
)