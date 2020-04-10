package com.dasbikash.book_keeper.activities.home.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.User

class ViewModelUser(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private val userLiveData = MediatorLiveData<User>()
    fun getUserLiveData():LiveData<User> = userLiveData

    init {
        userLiveData.addSource(AuthRepo.getUserLiveDate(mApplication),{
            userLiveData.postValue(it)
        })
    }
}