package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Keep
@Entity
data class UnitOfMeasure(
    @PrimaryKey
    var id:String = "",
    var name:String?=null,
    var nameBangla:String?=null,
    var modified:Date?=null
):Serializable