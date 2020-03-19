package com.dasbikash.exp_man_repo

import android.content.Context
import com.dasbikash.exp_man_repo.firebase.FireStoreExpenseEntryUtils
import com.dasbikash.exp_man_repo.model.ExpenseEntry

object ExpenseRepo:ExpenseManagerRepo() {

    suspend fun saveExpenseEntry(context: Context,expenseEntry: ExpenseEntry):Boolean{
        AuthRepo.getUser(context)?.let {
            expenseEntry.userId = it.id
            FireStoreExpenseEntryUtils.saveExpenseEntry(expenseEntry)
        }
        getDatabase(context).expenseEntryDao.add(expenseEntry)
        return true
    }

    suspend fun getAllExpenseEntries(context: Context):List<ExpenseEntry>{
        val categories = getDatabase(context).expenseCategoryDao.findAll()
        val uoms = getDatabase(context).unitOfMeasureDao.findAll()
        return getDatabase(context).expenseEntryDao.findAll().map {
            val expenseEntry = it
            expenseEntry.expenseCategory = categories.find { it.id==expenseEntry.categoryId }
            expenseEntry.unitOfMeasure = uoms.find { it.id==expenseEntry.unitId }
            expenseEntry
        }
    }
}