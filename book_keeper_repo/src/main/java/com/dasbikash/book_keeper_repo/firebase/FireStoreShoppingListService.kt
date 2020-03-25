package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.book_keeper_repo.model.ShoppingList

internal object FireStoreShoppingListService {
    fun saveShoppingList(shoppingList: ShoppingList) =
        FireStoreRefUtils.getShoppingListCollectionRef().document(shoppingList.id).set(shoppingList)
}