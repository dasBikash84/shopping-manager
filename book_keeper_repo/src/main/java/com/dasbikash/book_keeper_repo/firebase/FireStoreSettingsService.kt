package com.dasbikash.book_keeper_repo.firebase

import androidx.annotation.Keep
import com.dasbikash.book_keeper_repo.exceptions.FbDocumentReadException
import com.dasbikash.book_keeper_repo.model.ExpenseCategory
import com.google.android.gms.tasks.OnCompleteListener
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object FireStoreSettingsService {

    private const val EXPENSE_CATEGORY_MODIFIED_FIELD = "updateTime"

    suspend fun getExpenseCategories(lastUpdated:Date?=null):List<ExpenseCategory>{

        val query = when {
            lastUpdated==null -> FireStoreRefUtils.getExpCategoriesCollectionRef()
            else -> FireStoreRefUtils.getExpCategoriesCollectionRef().whereGreaterThan(EXPENSE_CATEGORY_MODIFIED_FIELD,lastUpdated)
        }

        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnCompleteListener(OnCompleteListener {
                    if(it.isSuccessful){
                        try {
                            if (it.result != null){
                                it.result?.toObjects(ExpenseCategories::class.java)?.let {
                                    continuation.resume(
                                        if (it.isNotEmpty()){
                                            it.first()!!.categories!!
                                        }else{
                                            emptyList()
                                        }
                                    )
                                }
                            }else{
                                continuation.resume(emptyList())
                            }
                        }catch (ex:Throwable){
                            ex.printStackTrace()
                            continuation.resumeWithException(FbDocumentReadException(ex))
                        }
                    }else{
                        continuation.resumeWithException(it.exception ?: FbDocumentReadException())
                    }
                })
        }
    }
}

@Keep
data class ExpenseCategories(
    var categories: List<ExpenseCategory>?=null,
    var updateTime: Date?=null
)