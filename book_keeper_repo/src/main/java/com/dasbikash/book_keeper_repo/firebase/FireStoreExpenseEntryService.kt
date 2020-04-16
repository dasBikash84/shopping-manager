package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FireStoreExpenseEntryService {

    private const val EXPENSE_ENTRY_MODIFIED_FIELD = "modified"
    private const val EXPENSE_ENTRY_USER_ID_FIELD = "userId"
    private const val EXPENSE_ENTRY_ACTIVE_FIELD = "active"

    fun saveExpenseEntry(expenseEntry: ExpenseEntry) {
        expenseEntry.updateModified()
        FireStoreRefUtils.getExpenseEntryCollectionRef().document(expenseEntry.id).set(expenseEntry)
    }

    fun deleteExpenseEntry(expenseEntry: ExpenseEntry) {
        expenseEntry.active = false
        expenseEntry.updateModified()
        saveExpenseEntry(expenseEntry)
    }

    suspend fun getLatestExpenseEntries(lastUpdated: Date?=null):List<ExpenseEntry>{
        debugLog("lastUpdated:$lastUpdated")
        var query = FireStoreRefUtils
                                        .getExpenseEntryCollectionRef()
                                        .whereEqualTo(EXPENSE_ENTRY_USER_ID_FIELD,AuthRepo.getUserId())


        if (lastUpdated!=null){
            query = query.whereGreaterThan(EXPENSE_ENTRY_MODIFIED_FIELD,lastUpdated)
        }else{
            query = query.whereEqualTo(EXPENSE_ENTRY_ACTIVE_FIELD,true)
        }

        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects(ExpenseEntry::class.java))
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    continuation.resume(emptyList())
                }
        }
    }
}
