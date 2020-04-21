package com.dasbikash.book_keeper_repo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.firebase.FireStoreExpenseEntryService
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.book_keeper_repo.model.ExpenseEntryFetchParam
import com.dasbikash.book_keeper_repo.model.TimeBasedExpenseEntryGroup
import com.dasbikash.book_keeper_repo.model.TimeDuration
import com.dasbikash.book_keeper_repo.utils.getDayCount
import com.dasbikash.book_keeper_repo.utils.getMonthCount
import com.dasbikash.book_keeper_repo.utils.getWeekCount
import java.util.*

object ExpenseRepo:BookKeeperRepo() {

    private fun getExpenseEntryDao(context: Context) = getDatabase(context).expenseEntryDao

    suspend fun saveExpenseEntry(context: Context,expenseEntry: ExpenseEntry):Boolean{
        if (AuthRepo.checkLogIn()){
            val doOnError:suspend ()->Unit = getExpenseEntryById(context,expenseEntry.id).let {
                if (it==null){
                    { getExpenseEntryDao(context).delete(expenseEntry)}
                }else{
                    { getExpenseEntryDao(context).add(it.copy())}
                }
            }
            expenseEntry.userId = AuthRepo.getUserId()
            FireStoreExpenseEntryService.saveExpenseEntry(expenseEntry,doOnError)
        }
        getExpenseEntryDao(context).add(expenseEntry)
        return true
    }

    private fun getSqlForExpenseEntryFetch(expenseEntryFetchParam: ExpenseEntryFetchParam):Pair<String,List<Any>> {
        expenseEntryFetchParam.apply {
            val sqlBuilder = StringBuilder("SELECT * from ExpenseEntry where active AND ")
            val params = mutableListOf<Any>()

            if (expenseCategory != null) {
                sqlBuilder.append(" categoryId = ? AND ")
                params.add(expenseCategory!!)
            }

            sqlBuilder.append(" details like '%${searchText}%' AND ")

            if (user != null) {
                sqlBuilder.append(" userId = ? ")
                params.add(user.id)
            } else {
                sqlBuilder.append(" userId is null ")
            }

            sqlBuilder.append(" ORDER BY timeTs DESC")
            sqlBuilder.append(" limit $limit")

            return Pair(sqlBuilder.toString(), params)
        }
    }

    fun fetchAllExpenseEntriesLiveData(context: Context, expenseEntryFetchParam: ExpenseEntryFetchParam):LiveData<List<ExpenseEntry>>{

        val (sqlBuilder,params) = getSqlForExpenseEntryFetch(expenseEntryFetchParam)
        debugLog(sqlBuilder)
        return getExpenseEntryDao(context).getExpenseEntryLiveDataByRawQuery(
                                                        SimpleSQLiteQuery(sqlBuilder,params.toTypedArray()))
    }

    suspend fun delete(context: Context,expenseEntry: ExpenseEntry){
        if (expenseEntry.userId!=null && AuthRepo.checkLogIn()){
            val originalEntry = expenseEntry.copy()
            FireStoreExpenseEntryService.deleteExpenseEntry(expenseEntry,{
                debugLog("Adding: $originalEntry")
                getExpenseEntryDao(context).add(originalEntry)
            })
        }
        getExpenseEntryDao(context).delete(expenseEntry)
    }

    suspend fun getDayBasedExpenseEntryGroups(context: Context):List<TimeBasedExpenseEntryGroup>{
        return getExpenseDates(context)
                    .let {return@let it.distinctBy { it.getDayCount() }}
                    .map { getTimeBasedExpenseEntryGroup(context,it,TimeDuration.DAY) }
    }

    suspend fun getWeekBasedExpenseEntryGroups(context: Context):List<TimeBasedExpenseEntryGroup>{
        return getExpenseDates(context)
                .let {return@let it.distinctBy { it.getWeekCount() }}
                .map { getTimeBasedExpenseEntryGroup(context,it,TimeDuration.WEEK) }
    }

    suspend fun getMonthBasedExpenseEntryGroups(context: Context):List<TimeBasedExpenseEntryGroup>{
        return getExpenseDates(context)
                .let {return@let it.distinctBy { it.getMonthCount() }}
                .map { getTimeBasedExpenseEntryGroup(context,it,TimeDuration.MONTH) }
    }

    suspend fun getExpenseDates(context: Context): List<Date> {
        return if (AuthRepo.checkLogIn()) {
            getExpenseEntryDao(context).getDates(AuthRepo.getUserId()).map { it.toDate() }
        } else {
            getExpenseEntryDao(context).getDates().map { it.toDate() }
        }
    }

    private suspend fun getTimeBasedExpenseEntryGroup(context: Context,date: Date,timeDuration: TimeDuration)
            :TimeBasedExpenseEntryGroup{
        val (startTime,endTime) = TimeBasedExpenseEntryGroup.getStartEndTime(date, timeDuration)
        val expenseEntryIds:List<String> = getExpenseEntryIds(context,startTime,endTime)
        val totalExpense:Double = getTotalExpense(context,startTime,endTime)
        return TimeBasedExpenseEntryGroup(startTime, timeDuration, expenseEntryIds, totalExpense)
    }

    private suspend fun getTotalExpense(context: Context,startTime: Date, endTime: Date): Double {
        return AuthRepo.getUserId().let {
            if (it.isBlank()){
                getExpenseEntryDao(context).getTotalExpense(startTime.time,endTime.time)
            }else{
                getExpenseEntryDao(context).getTotalExpense(it.trim(),startTime.time,endTime.time)
            }
        }
    }

    private suspend fun getExpenseEntryIds(context: Context,startTime: Date, endTime: Date): List<String> {
        return AuthRepo.getUserId().let {
            if (it.isBlank()){
                getExpenseEntryDao(context).getExpenseEntryIds(startTime.time,endTime.time)
            }else{
                getExpenseEntryDao(context).getExpenseEntryIds(it.trim(),startTime.time,endTime.time)
            }
        }
    }

    fun getExpenseEntryLiveDataByIds(context: Context,expenseEntryIds:List<String>):LiveData<List<ExpenseEntry>>{
        val sqlBuilder = StringBuilder("SELECT * from ExpenseEntry where active AND id IN (")
        for (i in 0..expenseEntryIds.size-1){
            sqlBuilder.append("'${expenseEntryIds.get(i)}'")
            if (i!=expenseEntryIds.size-1){
                sqlBuilder.append(",")
            }
        }
        sqlBuilder.append(")")
        debugLog(sqlBuilder.toString())
        return getExpenseEntryDao(context).getExpenseEntryLiveDataByInRawQuery(SimpleSQLiteQuery(sqlBuilder.toString()))
    }

    suspend fun getExpenseEntryById(context: Context,id:String):ExpenseEntry?{
        return getExpenseEntryDao(context).findById(id)
    }

    internal suspend fun syncData(context: Context) {
        FireStoreExpenseEntryService
            .getLatestExpenseEntries(getMaxExpenseModifiedTime(context))
            .let { getExpenseEntryDao(context).addAll(it)}
    }

    private suspend fun getMaxExpenseModifiedTime(context: Context):Date?{
        return getExpenseEntryDao(context).getLatestModifiedTimeForUser(AuthRepo.getUserId())?.toDate()
    }

    suspend fun getGuestData(context: Context):List<ExpenseEntry> {
        return getExpenseEntryDao(context).getGuestData()
    }

    suspend fun findByPeriod(context: Context,periodStart:Date,periodEnd:Date): List<ExpenseEntry>{
        if (!AuthRepo.checkLogIn()) {return emptyList()}
        return getExpenseEntryDao(context).findByPeriod(periodStart = periodStart.time,periodEnd = periodEnd.time)
    }
}