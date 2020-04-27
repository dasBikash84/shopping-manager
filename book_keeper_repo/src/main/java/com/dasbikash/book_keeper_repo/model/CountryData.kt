package com.dasbikash.book_keeper_repo.model

import android.content.Context
import androidx.annotation.Keep
import com.dasbikash.book_keeper_repo.CountryRepo

@Keep
data class CountryData(
    var countryList:List<Country>?=null
)

@Keep
data class Country(
    var name:String="",
    var countryCode:String="",
    var callingCode:String?=null,
    var phoneNumberLength:Int?=null,
    var timezones:List<String>?=null,
    var currencies:List<Currency>?=null,
    var mobileNumberPrefix:List<String>?=null,
    var enabled:Boolean = false
) {
    fun displayText():String {
        return "${callingCode} (${name})"
    }

    fun checkNumber(numberString: String):Boolean{
        if(numberString.length != phoneNumberLength!!){
            return false
        }
        if ((mobileNumberPrefix?.count { numberString.startsWith(it) } ?: 0) > 0){
            return true
        }
        return false
    }

    fun fullNumber(numberString: String) = "${callingCode!!}${numberString.trim()}"

    companion object{
        fun getCallingCodeFromDisplayText(displayText:String):String{
            return displayText.split("(").get(0).trim()
        }

        suspend fun getCountryFromDisplayText(context: Context,displayText:String):Country{
            val callingCode = getCallingCodeFromDisplayText(displayText)
            return CountryRepo.getCountryData(context).find { it.callingCode == callingCode}!!
        }
    }
}

@Keep
data class Currency(
    var code:String?=null,
    var name:String?=null,
    var symbol:String?=null
)