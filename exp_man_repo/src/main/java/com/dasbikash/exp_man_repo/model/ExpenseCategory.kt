package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity
data class ExpenseCategory(
    @PrimaryKey
    var id:String="",
    var name:String?=null,
    var nameBangla:String?=null,
    var modified:Long?=null
)