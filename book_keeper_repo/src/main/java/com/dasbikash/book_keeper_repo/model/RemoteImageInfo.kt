package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Keep
@Entity(
    indices = arrayOf(
        Index(value = ["remotePath"], unique = false),
        Index(value = ["uploadRunning"], unique = false),
        Index(value = ["modified"], unique = false)
    )
)
data class RemoteImageInfo(
    @PrimaryKey
    var localName:String = "${UUID.randomUUID().toString().let { it.substring(it.length-12,it.length) }}_${System.nanoTime()}.png",
    var remotePath:String?=null,
    var uploadRunning:Boolean = true,
    var modified:Long = System.currentTimeMillis(),
    var isProductImage:Boolean = true
){
    fun prepForUpload(){
        modified = System.currentTimeMillis()
        uploadRunning = true
    }
}