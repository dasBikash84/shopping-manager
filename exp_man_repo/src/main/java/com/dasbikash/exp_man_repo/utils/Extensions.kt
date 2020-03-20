package com.dasbikash.exp_man_repo.utils

import android.os.Parcel
import android.os.Parcelable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*

internal fun ByteArray.toCharArray():CharArray{
    val charArray = CharArray(this.size)
    for (i in 0..size-1){
        charArray.set(i,get(i).toChar())
    }
    return charArray
}

internal fun CharArray.byteArray():ByteArray{
    val bytes = ByteArray(this.size)
    for (i in 0..size-1){
        bytes.set(i,get(i).toByte())
    }
    return bytes
}

private fun ByteArray.toSerializedString():String = String(toCharArray())
private fun String.deserialize():ByteArray = toCharArray().byteArray()

@Suppress("UNCHECKED_CAST")
internal fun <T:java.io.Serializable> String.toSerializable(type:Class<T>):T?{
    return this.deserialize().toSerializable(type)
}

private fun java.io.Serializable.toByteArray():ByteArray{
    val buffer = ByteArrayOutputStream()
    val oos = ObjectOutputStream(buffer)
    oos.writeObject(this)
    oos.close()
    return buffer.toByteArray()
}

internal fun java.io.Serializable.toSerializedString():String{
    return this.toByteArray().toSerializedString()
}

@Suppress("UNCHECKED_CAST")
private fun <T:java.io.Serializable> ByteArray.toSerializable(type:Class<T>):T?{
    try {
        return ObjectInputStream(ByteArrayInputStream(this)).readObject() as T
    }catch (ex:Throwable){
        ex.printStackTrace()
        return null
    }
}

private fun Parcelable.toByteArray(): ByteArray {
    val parcel = Parcel.obtain()
    this.writeToParcel(parcel, 0)
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}

internal fun Parcelable.toSerializedString(): String {
    return this.toByteArray().toSerializedString()
}

internal fun <T : Parcelable> String.toParcelable(creator: Parcelable.Creator<T>):T
        = byteArrayToParcelable(toCharArray().byteArray(),creator)


internal fun <T : Parcelable> byteArrayToParcelable(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
    val parcel = byteArrayToParcel(bytes)
    val data = creator.createFromParcel(parcel)
    parcel.recycle()
    return data
}

internal fun byteArrayToParcel(bytes: ByteArray): Parcel {
    val parcel = Parcel.obtain()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)
    return parcel
}

private val STRING_TAG_SEPARATOR = "!@#$%!@#$%!@#$%"
internal fun String.addTag(tag:String?=null):String{
    val stringTag = when{
        tag !=null -> tag
        else -> UUID.randomUUID().toString()
    }
    return "$stringTag$STRING_TAG_SEPARATOR$this"
}

internal fun String.removeTag():Pair<String,String>?{
    try {
        this.split(STRING_TAG_SEPARATOR).let {
            return Pair(it[0],it[1])
        }
    }catch (ex:Throwable){
        ex.printStackTrace()
        return null
    }
}