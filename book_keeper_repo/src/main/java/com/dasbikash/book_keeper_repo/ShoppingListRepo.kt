package com.dasbikash.book_keeper_repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.book_keeper_repo.firebase.FireStoreShoppingListService
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.ShoppingListItem

object ShoppingListRepo:BookKeeperRepo() {

    private fun getShoppingListDao(context: Context) = getDatabase(context).shoppingListDao
    private fun getShoppingListItemDao(context: Context) = getDatabase(context).shoppingListItemDao

    suspend fun getAllShoppingLists(context: Context):LiveData<List<ShoppingList>>{
        return AuthRepo.getUser(context)!!.let { getDatabase(context).shoppingListDao.findForUser(it.id) }
    }

    suspend fun createList(context: Context, shoppingListTitle: CharSequence):ShoppingList?{
        AuthRepo.getUser(context)!!.let {
            if (getShoppingListDao(context).findByUserAndTitle (it.id,shoppingListTitle.toString()) == null){
                val shoppingList = ShoppingList(userId = it.id,title = shoppingListTitle.trim().toString())
                saveToFireBase(context,shoppingList)
                getShoppingListDao(context).add(shoppingList)
                return shoppingList
            }else{
                return null
            }
        }
    }

    private suspend fun saveToFireBase(context: Context,shoppingList: ShoppingList){
        val shoppingListItems = mutableListOf<ShoppingListItem>()
        shoppingList.getShoppingListItemIds()?.asSequence()?.forEach {
            getShoppingListItemDao(context).findById(it)!!.let { shoppingListItems.add(it) }
        }
        shoppingList.shoppingListItems = shoppingListItems.toList()
        FireStoreShoppingListService.saveShoppingList(shoppingList)
    }

    private suspend fun saveRemoteEntry(context: Context,shoppingList: ShoppingList){
        shoppingList.shoppingListItems?.asSequence()?.forEach {
            getShoppingListItemDao(context).add(it)
        }
        getShoppingListDao(context).add(shoppingList)
    }
}