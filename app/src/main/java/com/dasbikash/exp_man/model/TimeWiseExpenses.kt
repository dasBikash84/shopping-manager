package com.dasbikash.exp_man.model

import com.dasbikash.exp_man_repo.model.ExpenseEntry

data class TimeWiseExpenses(
    val periodText:String,
    val expenses:MutableList<ExpenseEntry> = mutableListOf()
)