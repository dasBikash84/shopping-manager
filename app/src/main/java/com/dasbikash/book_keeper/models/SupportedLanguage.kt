package com.dasbikash.book_keeper.models

import androidx.annotation.Keep

@Keep
enum class SupportedLanguage(val language:String,val country:String){
    BANGLA("bn",""),
    ENGLISH("en","US")
}