package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.google.firebase.Timestamp
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FireStoreShoppingListService {

    private const val SHOPPING_LIST_MODIFIED_FIELD = "modified"
    private const val SHOPPING_LIST_USER_ID_FIELD = "userId"
    private const val SHOPPING_LIST_ACTIVE_FIELD = "active"

    fun saveShoppingList(shoppingList: ShoppingList) {
        shoppingList.updateModified()
        debugLog("saveShoppingList: $shoppingList")
        FireStoreRefUtils.getShoppingListCollectionRef().document(shoppingList.id).set(shoppingList)
    }

    suspend fun getLatestShoppingLists(lastUpdated: Timestamp?=null):List<ShoppingList>{
        debugLog("lastUpdated:$lastUpdated")
        var query = FireStoreRefUtils
                                .getShoppingListCollectionRef()
                                .whereEqualTo(SHOPPING_LIST_USER_ID_FIELD,AuthRepo.getUserId())

        if (lastUpdated!=null){
            query = query.whereGreaterThan(SHOPPING_LIST_MODIFIED_FIELD,lastUpdated)
        }else{
            query = query.whereEqualTo(SHOPPING_LIST_ACTIVE_FIELD,true)
        }

        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects(ShoppingList::class.java))
                }.addOnFailureListener {
                    it.printStackTrace()
                    continuation.resume(emptyList())
                }
        }
    }

    suspend fun fetchShoppingListById(shoppingListId:String):ShoppingList? {
        return suspendCoroutine {
            val continuation = it
            FireStoreRefUtils
                .getShoppingListCollectionRef()
                .document(shoppingListId)
                .get()
                .addOnSuccessListener {
                    it.toObject(ShoppingList::class.java).let {
                        continuation.resume(it)
                    }
                }
                .addOnFailureListener {
                    continuation.resume(null)
                    it.printStackTrace()
                }
        }
    }

    fun getFbPath(shoppingListId: String): String =
        FireStoreRefUtils.getShoppingListCollectionRef().document(shoppingListId).path
}