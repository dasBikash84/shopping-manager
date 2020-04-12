package com.dasbikash.book_keeper.activities.home.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ConnectionRequestRepo
import com.dasbikash.book_keeper_repo.model.User

class ViewModelConnections(private val mApplication: Application) : AndroidViewModel(mApplication) {
    fun getPendingLiveData() = ConnectionRequestRepo.getLivaDataForPendingConnections(mApplication)
    fun getApprovedLiveData() = ConnectionRequestRepo.getLivaDataForApprovedConnections(mApplication)
}