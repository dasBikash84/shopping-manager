package com.dasbikash.exp_man_repo.firebase

import androidx.annotation.Keep
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.exp_man_repo.exceptions.FbDocumentReadException
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.model.UnitOfMeasure
import com.google.android.gms.tasks.OnCompleteListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object FireStoreSettingsUtils {

    private const val EXPENSE_CATEGORY_MODIFIED_FIELD = "modified"
    private const val UOM_MODIFIED_FIELD = "modified"

    suspend fun getExpenseCategories(lastUpdated:Long?=null):List<ExpenseCategory>{

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
                            continuation.resume(it.result!!.toObjects(ExpenseCategory::class.java))
                        }catch (ex:Throwable){
                            continuation.resumeWithException(FbDocumentReadException(ex))
                        }
                    }else{
                        continuation.resumeWithException(it.exception ?: FbDocumentReadException())
                    }
                })
        }
    }

    suspend fun getUnitOfMeasures(lastUpdated:Long?=null):List<UnitOfMeasure>{

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
                            continuation.resume(it.result!!.toObjects(UnitOfMeasure::class.java))
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
