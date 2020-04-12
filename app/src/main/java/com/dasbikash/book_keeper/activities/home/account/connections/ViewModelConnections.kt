package com.dasbikash.book_keeper.activities.home.account.connections

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.dasbikash.book_keeper_repo.ConnectionRequestRepo

class ViewModelConnections(private val mApplication: Application) : AndroidViewModel(mApplication) {
    fun getRequestedPendingLiveData() = ConnectionRequestRepo.getLivaDataForRequestedPending(mApplication)
    fun getApprovedLiveData() = ConnectionRequestRepo.getLivaDataForApprovedConnections(mApplication)
    fun getReceivedPendingLiveData() = ConnectionRequestRepo.getLiveDataForReceivedPendingRequests(mApplication)
}