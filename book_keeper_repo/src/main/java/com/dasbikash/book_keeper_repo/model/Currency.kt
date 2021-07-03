package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Currency(
    var code:String?=null,
    var name:String?=null,
    var symbol:String?=null
):Serializable{

    fun displayText():String = "${code ?: ""}${symbol?.let { "($it)" } ?: ""}"

    companion object{
        @Keep
        val DEFAULT_CURRENCY = Currency("USD","United States dollar","$")
    }
}