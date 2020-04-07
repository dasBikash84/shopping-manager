package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.exceptions.FbDocumentReadException
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.book_keeper_repo.model.User
import com.google.android.gms.tasks.OnCompleteListener
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object FireStoreExpenseEntryService {

    private const val EXPENSE_ENTRY_MODIFIED_FIELD = "modified"
    private const val EXPENSE_ENTRY_USER_ID_FIELD = "userId"
    private const val EXPENSE_ENTRY_ACTIVE_FIELD = "active"

    fun saveExpenseEntry(expenseEntry: ExpenseEntry) =
        FireStoreRefUtils.getExpenseEntryCollectionRef().document(expenseEntry.id).set(expenseEntry)

    fun deleteExpenseEntry(expenseEntry: ExpenseEntry) {
        expenseEntry.active = false
        saveExpenseEntry(expenseEntry)
    }

    suspend fun getLatestExpenseEntries(user: User,lastUpdated: Date?=null):List<ExpenseEntry>?{
        debugLog("user:$user")
        debugLog("lastUpdated:$lastUpdated")
        var query = FireStoreRefUtils
                                        .getExpenseEntryCollectionRef()
                                        .whereEqualTo(EXPENSE_ENTRY_USER_ID_FIELD,user.id)

        lastUpdated?.let {
            query = query.whereGreaterThan(EXPENSE_ENTRY_MODIFIED_FIELD,lastUpdated)
        }

        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnCompleteListener(OnCompleteListener {
                    if(it.isSuccessful){
                        try {
                            continuation.resume(it.result!!.toObjects(ExpenseEntry::class.java))
                        }catch (ex:Throwable){
                            continuation.resumeWithException(FbDocumentReadException(ex))
                        }
                    }else{
                        continuation.resumeWithException(it.exception ?: FbDocumentReadException())
                    }
                })
        }
    }
}
