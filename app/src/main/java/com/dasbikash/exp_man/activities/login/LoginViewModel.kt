package com.dasbikash.exp_man.activities.login

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils

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
        getStoredUserIds(mApplication).let {
            if (input.isBlank() || it.contains(input)){
                return emptyList()
            }else{
                return it.filter { it.contains(input,true) }
            }
        }
    }

    companion object{

        private const val USER_IDS_SP_KEY =
            "com.dasbikash.exp_man.activities.login.LoginViewModel.USER_IDS_SP_KEY"

        private fun getStoredUserIds(context: Context):List<String>{
            return SharedPreferenceUtils.getDefaultInstance().getSerializableCollection(context,String::class.java,USER_IDS_SP_KEY)?.toList() ?: emptyList()
        }

        fun saveUserId(context: Context,userId:String){
            val currentIds = getStoredUserIds(context).toMutableList()
            if (!currentIds.contains(userId.trim())){
                currentIds.add(userId.trim())
                SharedPreferenceUtils.getDefaultInstance().saveSerializableCollectionSync(context,currentIds,USER_IDS_SP_KEY)
            }
        }
    }
}