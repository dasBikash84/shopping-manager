package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FireStoreExpenseEntryService {

    private const val EXPENSE_ENTRY_MODIFIED_FIELD = "modified"
    private const val EXPENSE_ENTRY_USER_ID_FIELD = "userId"
    private const val EXPENSE_ENTRY_ACTIVE_FIELD = "active"

    fun saveExpenseEntry(expenseEntry: ExpenseEntry,doOnError:suspend ()->Unit) {
        expenseEntry.updateModified()
        debugLog(expenseEntry)
        FireStoreRefUtils
            .getExpenseEntryCollectionRef()
            .document(expenseEntry.id)
            .set(expenseEntry)
            .addOnSuccessListener { debugLog("ExpenseEntry saved") }
            .addOnFailureListener {
                debugLog("ExpenseEntry save failure")
                GlobalScope.launch { doOnError()}
                it.printStackTrace()
            }
    }

    fun deleteExpenseEntry(expenseEntry: ExpenseEntry,doOnError:suspend ()->Unit) {
        expenseEntry.active = false
        saveExpenseEntry(expenseEntry,doOnError)
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

        debugLog(query::class.java.canonicalName ?: query::class.java.simpleName)

        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnSuccessListener {
                    debugLog("addOnSuccessListener")
                    continuation.resume(it.toObjects(ExpenseEntry::class.java))
                    debugLog("addOnSuccessListener after resume")
                }
                .addOnFailureListener {
                    debugLog("addOnFailureListener")
                    it.printStackTrace()
                    continuation.resume(emptyList())
                    debugLog("addOnFailureListener after resume")
                }
        }
    }
}
