package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep

@Keep
data class CountryData(
    var countryList:List<Country>?=null
)

@Keep
data class Country(
    var name:String="",
    var countryCode:String="",
    var callingCode:String?=null,
    var timezones:List<String>?=null,
    var currencies:List<Currency>?=null,
    var mobileNumberPrefix:List<String>?=null,
    var enabled:Boolean = false
)

@Keep
data class Currency(
    var code:String?=null,
    var name:String?=null,
    var symbol:String?=null
)