package com.dasbikash.book_keeper_repo.model

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dasbikash.book_keeper_repo.R
import java.io.Serializable
import java.lang.StringBuilder
import java.util.*

@Keep
@Entity
data class User(
    @PrimaryKey
    var id:String="",
    var email:String?=null,
    var phone:String?=null,
    var firstName:String?=null,
    var lastName:String?=null,
    var photoUrl:String?=null,
    var thumbPhotoUrl:String?=null,
    var modified:Date = Date()
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

    fun updateModified(){
        modified = Date()
    }
}