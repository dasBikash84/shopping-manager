package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.exceptions.FbDocumentReadException
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.User
import com.google.android.gms.tasks.OnCompleteListener
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object FireStoreShoppingListService {

    private const val SHOPPING_LIST_MODIFIED_FIELD = "modified"
    private const val SHOPPING_LIST_USER_ID_FIELD = "userId"
    private const val SHOPPING_LIST_ACTIVE_FIELD = "active"

    fun saveShoppingList(shoppingList: ShoppingList) =
        FireStoreRefUtils.getShoppingListCollectionRef().document(shoppingList.id).set(shoppingList)

    fun deleteShoppingList(shoppingList: ShoppingList) {
        shoppingList.active = false
        saveShoppingList(shoppingList)
    }

    suspend fun getLatestShoppingLists(user: User, lastUpdated: Date?=null):List<ShoppingList>?{
        debugLog("user:$user")
        debugLog("lastUpdated:$lastUpdated")
        var query = FireStoreRefUtils
                                .getShoppingListCollectionRef()
                                .whereEqualTo(SHOPPING_LIST_ACTIVE_FIELD,true)
                                .whereEqualTo(SHOPPING_LIST_USER_ID_FIELD,user.id)

        lastUpdated?.let {
            query = query.whereGreaterThan(SHOPPING_LIST_MODIFIED_FIELD,lastUpdated)
        }

        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnCompleteListener(OnCompleteListener {
                    if(it.isSuccessful){
                        try {
                            continuation.resume(it.result!!.toObjects(ShoppingList::class.java))
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