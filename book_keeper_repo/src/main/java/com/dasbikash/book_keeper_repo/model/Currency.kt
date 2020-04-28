package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import java.io.Serializable

@Keep
data class Currency(
    var code:String?=null,
    var name:String?=null,
    var symbol:String?=null
):Serializable{

    fun displayText():String = "$code (${symbol})"

    companion object{
        val DEFAULT_CURRENCY = Currency("USD","United States dollar","$")

        fun displayTextToSymbol(displayText:String):String{
            displayText.split("(").get(1).let {
                return it.substring(0,it.length-1)
            }
        }
    }
}