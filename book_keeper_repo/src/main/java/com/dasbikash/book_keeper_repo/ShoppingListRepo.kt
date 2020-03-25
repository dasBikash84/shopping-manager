package com.dasbikash.book_keeper_repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.book_keeper_repo.firebase.FireStoreShoppingListService
import com.dasbikash.book_keeper_repo.model.ShoppingList

object ShoppingListRepo:BookKeeperRepo() {

    private fun getDao(context: Context) = getDatabase(context).shoppingListDao

    suspend fun getAllShoppingLists(context: Context):LiveData<List<ShoppingList>>{
        return AuthRepo.getUser(context)!!.let { getDatabase(context).shoppingListDao.findForUser(it.id) }
    }

    suspend fun createList(context: Context, shoppingListTitle: CharSequence):ShoppingList?{
        AuthRepo.getUser(context)!!.let {
            if (getDao(context).findByUserAndTitle(it.id,shoppingListTitle.toString()) == null){
                val shoppingList = ShoppingList(userId = it.id,title = shoppingListTitle.trim().toString())
                FireStoreShoppingListService.saveShoppingList(shoppingList)
                getDao(context).add(shoppingList)
                return shoppingList
            }else{
                return null
            }
        }
    }
}