package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep

@Keep
enum class SupportedLanguage(val language:String,val country:String,val displayName:String){
    BANGLA("bn","Bangladesh","বাংলা"),
    ENGLISH("en","US","English"),
    HINDI("hi","India","हिन्दी")
}