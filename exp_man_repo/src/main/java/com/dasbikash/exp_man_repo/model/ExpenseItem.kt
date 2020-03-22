package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep
import androidx.room.Ignore
import com.dasbikash.exp_man_repo.utils.toSerializable
import com.dasbikash.exp_man_repo.utils.toSerializedString
import com.google.firebase.firestore.Exclude
import java.io.Serializable
import java.util.*
@Keep
data class ExpenseItem(
    var id:String=UUID.randomUUID().toString(),
    var name:String?=null,
    var brandName:String?=null,
    var unitPrice:Double=0.0,
    var qty:Double=1.0,
    var modified:Date=Date()
):Serializable{
    @Exclude private var unitOfMeasureSerialized: String?=null
    @Exclude fun getUnitOfMeasureSerialized() : String?= unitOfMeasureSerialized
    fun setUnitOfMeasureSerialized(unitOfMeasureSerialized: String?){ this.unitOfMeasureSerialized = unitOfMeasureSerialized}

    fun setUnitOfMeasure(unitOfMeasure: UnitOfMeasure):ExpenseItem{
        setUnitOfMeasureSerialized(unitOfMeasure.toSerializedString())
        return this
    }
    @Ignore
    fun getUnitOfMeasure():UnitOfMeasure? = getUnitOfMeasureSerialized()?.toSerializable(UnitOfMeasure::class.java)
}