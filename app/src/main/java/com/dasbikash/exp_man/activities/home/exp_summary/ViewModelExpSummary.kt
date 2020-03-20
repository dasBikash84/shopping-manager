package com.dasbikash.exp_man.activities.home.exp_summary

import android.app.Application
import androidx.lifecycle.*
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.exp_man_repo.ExpenseRepo
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import com.dasbikash.exp_man_repo.model.User
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class ViewModelExpSummary(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private var expenseCategory:ExpenseCategory? = null
    private var searchText:String = ""
    private var user:User?=null
    private var limit:Int = EXPENSE_FETCH_LIMIT_INC_VALUE

    private val allExpenseEntryLiveData = MutableLiveData<List<ExpenseEntry>>()

    init {
        viewModelScope.launch {
            debugLog("ViewModelExpSummary init")
            user = AuthRepo.getUser(mApplication)
            delay(100L)
            refreshExpenseEntries()
        }
    }

    private fun resetAllExpenseFetchLimit(){
        limit = EXPENSE_FETCH_LIMIT_INC_VALUE
    }

    fun incAllExpenseFetchLimit(){
        limit += EXPENSE_FETCH_LIMIT_INC_VALUE
    }

    fun setSearchText(searchText:String) {
        this.searchText = searchText
        resetAllExpenseFetchLimit()
        refreshExpenseEntries()
    }

    fun setExpenseCategory(expenseCategory: ExpenseCategory?=null) {
        this.expenseCategory = expenseCategory
        resetAllExpenseFetchLimit()
        refreshExpenseEntries()
    }

    fun getAllExpenseEntryLiveData():LiveData<List<ExpenseEntry>> = allExpenseEntryLiveData

    private fun refreshExpenseEntries(){
        viewModelScope.launch {
            ExpenseRepo.fetchAllExpenseEntries(mApplication.applicationContext,searchText, limit, user, expenseCategory).let {
                allExpenseEntryLiveData.postValue(it)
            }
        }
    }

    companion object{
        private val EXPENSE_FETCH_LIMIT_INC_VALUE = 20
    }
}