package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity
data class ExpenseCategory(
    @PrimaryKey
    var name:String="",
    var nameBangla:String?=null,
    var modified:Long?=null
)