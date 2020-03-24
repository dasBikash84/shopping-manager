package com.dasbikash.book_keeper_repo

import android.content.Context
import com.dasbikash.book_keeper_repo.model.ShoppingList

object ShoppingListRepo:BookKeeperRepo() {

    suspend fun getAllShoppingLists(context: Context):List<ShoppingList>{
        return AuthRepo.getUser(context)!!.let { getDatabase(context).shoppingListDao.findForUser(it.id) }
    }
}