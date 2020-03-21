package com.dasbikash.exp_man_repo

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.exp_man_repo.firebase.FireStoreExpenseEntryUtils
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import com.dasbikash.exp_man_repo.model.User
import java.lang.StringBuilder

object ExpenseRepo:ExpenseManagerRepo() {

    suspend fun saveExpenseEntry(context: Context,expenseEntry: ExpenseEntry):Boolean{
        AuthRepo.getUser(context)?.let {
            expenseEntry.userId = it.id
            FireStoreExpenseEntryUtils.saveExpenseEntry(expenseEntry)
        }
        getDatabase(context).expenseEntryDao.add(expenseEntry)
        return true
    }

    private fun getSqlForExpenseEntryFetch(searchText:String,limit:Int,
                                       user: User?=null,expenseCategory: ExpenseCategory?=null):Pair<String,List<Any>> {
        val sqlBuilder = StringBuilder("SELECT * from ExpenseEntry where ")
        val params = mutableListOf<Any>()

        if (expenseCategory!=null){
            sqlBuilder.append(" categoryId = ? AND ")
            params.add(expenseCategory.id)
        }

        sqlBuilder.append(" details like '%${searchText}%' AND ")

        if (user!=null){
            sqlBuilder.append(" userId = ? ")
            params.add(user.id)
        }else{
            sqlBuilder.append(" userId is null ")
        }

        sqlBuilder.append(" ORDER BY created DESC")
        sqlBuilder.append(" limit $limit")

        return Pair(sqlBuilder.toString(),params)
    }

    fun fetchAllExpenseEntriesLiveData(context: Context, searchText:String,limit:Int,
                                       user: User?=null,expenseCategory: ExpenseCategory?=null):LiveData<List<ExpenseEntry>>{

        val (sqlBuilder,params) = getSqlForExpenseEntryFetch(searchText, limit, user, expenseCategory)
        debugLog(sqlBuilder)
        return getDatabase(context).expenseEntryDao.getExpenseEntryLiveDataByRawQuery(
                                                        SimpleSQLiteQuery(sqlBuilder,params.toTypedArray()))
    }

    suspend fun delete(context: Context,expenseEntry: ExpenseEntry){
        if (AuthRepo.checkLogIn(context)){
            FireStoreExpenseEntryUtils.deleteExpenseEntry(expenseEntry)
        }
        getDatabase(context).expenseEntryDao.delete(expenseEntry)
    }
}