package com.dasbikash.book_keeper.activities.home.exp_summary

import android.app.Application
import androidx.lifecycle.*
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ExpenseEntryFetchParam
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.model.ExpenseCategory
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.book_keeper_repo.model.TimeBasedExpenseEntryGroup
import kotlinx.coroutines.launch

class ViewModelExpBrowser(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private lateinit var expenseEntryFetchParam: ExpenseEntryFetchParam
    private lateinit var timeBasedExpenseEntryGroup: TimeBasedExpenseEntryGroup

    private var expenseEntryListLiveData:LiveData<List<ExpenseEntry>>?=null
    private val expenseEntryListMediatorLiveData = MediatorLiveData<List<ExpenseEntry>>()

    private var groupExpenseEntryListLiveData:LiveData<List<ExpenseEntry>>?=null
    private val groupExpenseEntryListMediatorLiveData = MediatorLiveData<Pair<TimeBasedExpenseEntryGroup,List<ExpenseEntry>>>()

    fun getTimeBasedExpenseEntryGroupLiveData():LiveData<Pair<TimeBasedExpenseEntryGroup,List<ExpenseEntry>>> = groupExpenseEntryListMediatorLiveData

    fun setTimeBasedExpenseEntryGroup(timeBasedExpenseEntryGroup: TimeBasedExpenseEntryGroup){
        debugLog("${timeBasedExpenseEntryGroup.startTime}")
        this.timeBasedExpenseEntryGroup = timeBasedExpenseEntryGroup
        groupExpenseEntryListLiveData?.let { groupExpenseEntryListMediatorLiveData.removeSource(it) }
        groupExpenseEntryListLiveData = ExpenseRepo.getExpenseEntryLiveDataByIds(mApplication.applicationContext,timeBasedExpenseEntryGroup.expenseEntryIds)
        groupExpenseEntryListMediatorLiveData.addSource(groupExpenseEntryListLiveData!!,{
            debugLog("addSource: ${it.map { it.id }}")
            groupExpenseEntryListMediatorLiveData.postValue(Pair(timeBasedExpenseEntryGroup,it))
        })
    }

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