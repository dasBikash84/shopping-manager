package com.dasbikash.book_keeper.activities.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

class LoginViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val userIdInput = MutableLiveData<String>()
    private val userIdSuggestions = MediatorLiveData<List<String>>()

    fun postUserIdInput(userIdInput:String){
        this.userIdInput.postValue(userIdInput.trim())
    }

    fun getUserIdSuggestions():LiveData<List<String>> = userIdSuggestions

    init {
        userIdSuggestions.addSource(userIdInput,{
            userIdSuggestions.postValue(getSuggestions(it))
        })
    }

    private fun getSuggestions(input:String):List<String>{
        ActivityLogin.getStoredUserIds(mApplication).let {
            if (input.isBlank() || it.contains(input)){
                return emptyList()
            }else{
                return it.filter { it.contains(input,true) }
            }
        }
    }
}