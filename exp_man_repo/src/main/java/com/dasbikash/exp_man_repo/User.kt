package com.dasbikash.exp_man_repo

data class User(
    var id:String?=null,
    var email:String?=null,
    var phone:String?=null,
    var firstName:String?=null,
    var lastName:String?=null,
    var photoUrl:String?=null
){
    fun validateData():Boolean{
        return !id.isNullOrBlank() && (!phone.isNullOrBlank() || !email.isNullOrBlank())
    }
}