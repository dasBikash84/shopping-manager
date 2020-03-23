package com.dasbikash.book_keeper_repo

import android.content.Context
import androidx.annotation.Keep
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.firebase.FireStoreExpenseEntryUtils
import com.dasbikash.book_keeper_repo.model.*
import com.dasbikash.book_keeper_repo.utils.getDayCount
import com.dasbikash.book_keeper_repo.utils.getMonthCount
import com.dasbikash.book_keeper_repo.utils.getWeekCount
import java.util.*

object ExpenseRepo:BookKeeperRepo() {

    suspend fun saveExpenseEntry(context: Context,expenseEntry: ExpenseEntry):Boolean{
        AuthRepo.getUser(context)?.let {
            expenseEntry.userId = it.id
            FireStoreExpenseEntryUtils.saveExpenseEntry(expenseEntry)
        }
        getDatabase(context).expenseEntryDao.add(expenseEntry)
        return true
    }

    private fun getSqlForExpenseEntryFetch(expenseEntryFetchParam: ExpenseEntryFetchParam):Pair<String,List<Any>> {
        expenseEntryFetchParam.apply {
            val sqlBuilder = StringBuilder("SELECT * from ExpenseEntry where ")
            val params = mutableListOf<Any>()

            if (expenseCategory != null) {
                sqlBuilder.append(" categoryId = ? AND ")
                params.add(expenseCategory!!.id)
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
        return getDatabase(context).expenseEntryDao.getExpenseEntryLiveDataByRawQuery(
                                                        SimpleSQLiteQuery(sqlBuilder,params.toTypedArray()))
    }

    suspend fun delete(context: Context,expenseEntry: ExpenseEntry){
        if (AuthRepo.checkLogIn()){
            FireStoreExpenseEntryUtils.deleteExpenseEntry(expenseEntry)
        }
        getDatabase(context).expenseEntryDao.delete(expenseEntry)
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

    private suspend fun getExpenseDates(context: Context): List<Date> {
        return if (AuthRepo.checkLogIn()) {
            getDatabase(context).expenseEntryDao.getDates(AuthRepo.getUser(context)!!.id)
        } else {
            getDatabase(context).expenseEntryDao.getDates()
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
        return AuthRepo.getUser(context).let {
            if (it==null){
                getDatabase(context).expenseEntryDao.getTotalExpense(startTime.time,endTime.time)
            }else{
                getDatabase(context).expenseEntryDao.getTotalExpense(it.id,startTime.time,endTime.time)
            }
        }
    }

    private suspend fun getExpenseEntryIds(context: Context,startTime: Date, endTime: Date): List<String> {
        return AuthRepo.getUser(context).let {
            if (it==null){
                getDatabase(context).expenseEntryDao.getExpenseEntryIds(startTime.time,endTime.time)
            }else{
                getDatabase(context).expenseEntryDao.getExpenseEntryIds(it.id,startTime.time,endTime.time)
            }
        }
    }

    fun getExpenseEntryLiveDataByIds(context: Context,expenseEntryIds:List<String>):LiveData<List<ExpenseEntry>>{
        val sqlBuilder = StringBuilder("SELECT * from ExpenseEntry where id IN (")
        for (i in 0..expenseEntryIds.size-1){
            sqlBuilder.append("'${expenseEntryIds.get(i)}'")
            if (i!=expenseEntryIds.size-1){
                sqlBuilder.append(",")
            }
        }
        sqlBuilder.append(")")
        debugLog(sqlBuilder.toString())
        return getDatabase(context).expenseEntryDao.getExpenseEntryLiveDataByInRawQuery(SimpleSQLiteQuery(sqlBuilder.toString()))
    }

    suspend fun getExpenseEntryById(context: Context,id:String):ExpenseEntry?{
        return getDatabase(context).expenseEntryDao.findById(id)
    }

    suspend fun syncData(context: Context) {
        AuthRepo.getUser(context)?.let {
            FireStoreExpenseEntryUtils.getLatestExpenseEntries(
                it, getMaxExpenseModifiedTime(context,it))?.let {
                it.asSequence().forEach { debugLog(it)}
                getDatabase(context).expenseEntryDao.addAll(it)
            }
        }
    }

    private suspend fun getMaxExpenseModifiedTime(context: Context,user: User):Date?{
        return getDatabase(context).expenseEntryDao.getLatestModifiedTimeForUser(user.id)
    }
}

@Keep
data class ExpenseEntryFetchParam(
    var searchText:String="",
    var limit:Int=EXPENSE_FETCH_LIMIT_INC_VALUE,
    var expenseCategory:ExpenseCategory?=null,
    val user:User?=null
){
    companion object{
        val EXPENSE_FETCH_LIMIT_INC_VALUE = 100
    }
}