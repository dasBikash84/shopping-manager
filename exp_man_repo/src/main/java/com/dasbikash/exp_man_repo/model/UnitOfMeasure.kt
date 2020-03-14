package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep

@Keep
data class UnitOfMeasure(
    var id:String,
    var name:String,
    var nameBangla:String,
    var modified:Long
)