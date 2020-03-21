package com.dasbikash.exp_man.activities.home.exp_summary

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.exp_man_repo.ExpenseEntryFetchParam
import com.dasbikash.exp_man_repo.ExpenseRepo
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import kotlinx.coroutines.launch
import java.util.*

class ViewModelExpSummary(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private lateinit var expenseEntryFetchParam: ExpenseEntryFetchParam

    private var expenseEntryListLiveData:LiveData<List<ExpenseEntry>>?=null
    private val expenseEntryListMediatorLiveData = MediatorLiveData<List<ExpenseEntry>>()

    init {
        viewModelScope.launch {
            AuthRepo.getUser(mApplication).let {
                debugLog(it ?: "No user")
                expenseEntryFetchParam = ExpenseEntryFetchParam(user = it)
                refreshExpenseEntries()
            }
        }
    }
    private fun resetExpenseFetchLimit(){
        expenseEntryFetchParam.limit = ExpenseEntryFetchParam.EXPENSE_FETCH_LIMIT_INC_VALUE
    }

    fun incrementExpenseFetchLimit(){
        expenseEntryFetchParam.limit += ExpenseEntryFetchParam.EXPENSE_FETCH_LIMIT_INC_VALUE
        refreshExpenseEntries()
    }

    fun setSearchText(searchText:String) {
        expenseEntryFetchParam.searchText = searchText
        resetExpenseFetchLimit()
        debugLog(expenseEntryFetchParam)
        refreshExpenseEntries()
    }

    fun setExpenseCategory(expenseCategory: ExpenseCategory?=null) {
        expenseEntryFetchParam.expenseCategory = expenseCategory
        resetExpenseFetchLimit()
        debugLog(expenseEntryFetchParam)
        refreshExpenseEntries()
    }

    fun getAllExpenseEntryLiveData():LiveData<List<ExpenseEntry>> = expenseEntryListMediatorLiveData

    private fun refreshExpenseEntries(){
        ExpenseRepo
            .fetchAllExpenseEntriesLiveData(mApplication.applicationContext,expenseEntryFetchParam)
            .let {
                expenseEntryListLiveData?.apply { expenseEntryListMediatorLiveData.removeSource(this) }
                expenseEntryListLiveData = it
                expenseEntryListMediatorLiveData.addSource(expenseEntryListLiveData!!,{
                    expenseEntryListMediatorLiveData.postValue(it)
                })
        }
    }
}