package com.dasbikash.exp_man_repo.firebase

import com.dasbikash.exp_man_repo.model.ExpenseEntry

internal object FireStoreExpenseEntryUtils {
    suspend fun saveExpenseEntry(expenseEntry: ExpenseEntry) =
        FireStoreRefUtils.getExpenseEntryCollectionRef().document(expenseEntry.id).set(expenseEntry)
}
