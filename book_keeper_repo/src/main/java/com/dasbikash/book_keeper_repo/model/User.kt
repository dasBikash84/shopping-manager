package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Keep
@Entity
data class User(
    @PrimaryKey
    var id:String="",
    var email:String?=null,
    var phone:String?=null,
    var firstName:String?=null,
    var lastName:String?=null,
    var photoUrl:String?=null
):Serializable{
    fun validateData():Boolean{
        return !id.isBlank() && (!phone.isNullOrBlank() || (!email.isNullOrBlank() && !firstName.isNullOrBlank()))
    }

    fun displayText(): CharSequence {
        return when{
            firstName!=null ->{ "$firstName ${lastName ?: ""}"}
            !email.isNullOrBlank() -> email!!
            !phone.isNullOrBlank() -> phone!!
            else -> ""
        }
    }

    fun detailsText(): CharSequence? {
        return displayText()
    }
}