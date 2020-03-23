package com.dasbikash.book_keeper_repo.firebase

import androidx.annotation.Keep
import com.dasbikash.book_keeper_repo.exceptions.FbDocumentReadException
import com.dasbikash.book_keeper_repo.model.ExpenseCategory
import com.dasbikash.book_keeper_repo.model.UnitOfMeasure
import com.google.android.gms.tasks.OnCompleteListener
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object FireStoreSettingsUtils {

    private const val EXPENSE_CATEGORY_MODIFIED_FIELD = "updateTime"
    private const val UOM_MODIFIED_FIELD = "updateTime"

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
                            continuation.resume(it.result!!.toObjects(ExpenseCategories::class.java).first().categories!!)
                        }catch (ex:Throwable){
                            continuation.resumeWithException(FbDocumentReadException(ex))
                        }
                    }else{
                        continuation.resumeWithException(it.exception ?: FbDocumentReadException())
                    }
                })
        }
    }

    suspend fun getUnitOfMeasures(lastUpdated:Date?=null):List<UnitOfMeasure>{

        val query = when {
            lastUpdated==null -> FireStoreRefUtils.getUomCollectionRef()
            else -> FireStoreRefUtils.getUomCollectionRef().whereGreaterThan(UOM_MODIFIED_FIELD,lastUpdated)
        }

        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnCompleteListener(OnCompleteListener {
                    if(it.isSuccessful){
                        try {
                            continuation.resume(it.result!!.toObjects(UnitOfMeasures::class.java).first().uoms!!)
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

@Keep
data class ExpenseCategories(
    var categories: List<ExpenseCategory>?=null,
    var updateTime: Date?=null
)

@Keep
data class UnitOfMeasures(
    var uoms: List<UnitOfMeasure>?=null,
    var updateTime:Date?=null
)