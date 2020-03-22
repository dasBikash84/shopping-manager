package com.dasbikash.exp_man_repo.model

import androidx.annotation.Keep
import androidx.room.Ignore
import com.dasbikash.android_basic_utils.utils.debugLog
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
    var uom:String?=null,
    var uomBangla:String?=null,
    var modified:Date=Date()
):Serializable