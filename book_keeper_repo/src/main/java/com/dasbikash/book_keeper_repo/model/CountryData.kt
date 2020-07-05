package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep

@Keep
data class CountryData(
    var countryList:List<Country>?=null
)