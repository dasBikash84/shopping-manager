package com.dasbikash.book_keeper.activities.home.account

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntryFetchParam
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.book_keeper_repo.model.TimeBasedExpenseEntryGroup
import com.dasbikash.book_keeper_repo.model.User
import kotlinx.coroutines.launch

class ViewModelUser(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private val userLiveData = MediatorLiveData<User>()
    fun getUserLiveData():LiveData<User> = userLiveData

    init {
        userLiveData.addSource(AuthRepo.getUserLiveDate(mApplication),{
            userLiveData.postValue(it)
        })
    }
}