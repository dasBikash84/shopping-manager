package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep
import com.dasbikash.exp_man_repo.utils.toSerializable
import com.dasbikash.exp_man_repo.utils.toSerializedString
import java.io.Serializable
import java.util.*
@Keep
data class ExpenseItem(
    var id:String=UUID.randomUUID().toString(),
    var name:String?=null,
    var brandName:String?=null,
    var unitPrice:Double=0.0,
    var qty:Double=1.0,
    var unitOfMeasureSerialized: String?=null,
    var unitOfMeasureId: String?=null,
    var created:Date=Date(),
    var modified:Date=Date()
):Serializable{
    fun setUnitOfMeasure(unitOfMeasure: UnitOfMeasure):ExpenseItem{
        unitOfMeasureSerialized = unitOfMeasure.toSerializedString()
        return this
    }
    fun getUnitOfMeasure():UnitOfMeasure? = unitOfMeasureSerialized?.toSerializable(UnitOfMeasure::class.java)
}