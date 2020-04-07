package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep

@Keep
data class ExpenseEntryFetchParam(
    var searchText:String="",
    var limit:Int=EXPENSE_FETCH_LIMIT_INC_VALUE,
    var expenseCategory:Int?=null,
    val user: User?=null
){
    companion object{
        val EXPENSE_FETCH_LIMIT_INC_VALUE = 100
    }
}