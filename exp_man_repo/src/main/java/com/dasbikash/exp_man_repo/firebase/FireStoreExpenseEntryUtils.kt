package com.dasbikash.exp_man_repo.firebase

import com.dasbikash.exp_man_repo.model.ExpenseEntry

internal object FireStoreExpenseEntryUtils {

    fun saveExpenseEntry(expenseEntry: ExpenseEntry) =
        FireStoreRefUtils.getExpenseEntryCollectionRef().document(expenseEntry.id).set(expenseEntry)

    fun deleteExpenseEntry(expenseEntry: ExpenseEntry) {
        expenseEntry.active = false
        saveExpenseEntry(expenseEntry)
    }
}
