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

    suspend fun save(context: Context,shoppingList: ShoppingList){
        shoppingList.updateModified()
        saveToFireBase(context, shoppingList)
        getShoppingListDao(context).add(shoppingList)
    }

    private suspend fun saveToFireBase(context: Context,shoppingList: ShoppingList){
        shoppingList.shoppingListItems = getShoppingListItems(context, shoppingList)
        FireStoreShoppingListService.saveShoppingList(shoppingList)
    }

    suspend fun getShoppingListItems(context: Context,shoppingList: ShoppingList)
            :List<ShoppingListItem>?{
        return shoppingList.shoppingListItemIds?.map { getShoppingListItemDao(context).findById(it)!! }
    }

    private suspend fun saveFireBaseEntry(context: Context,shoppingList: ShoppingList){
        val shoppingListItemIds = mutableListOf<String>()
        shoppingList.shoppingListItems?.asSequence()?.forEach {
            getShoppingListItemDao(context).add(it)
            shoppingListItemIds.add(it.id)
        }
        shoppingList.shoppingListItemIds = shoppingListItemIds.toList()
        getShoppingListDao(context).add(shoppingList)
    }



    fun getLiveDataById(context: Context,shoppingListId:String) =
        getDatabase(context).shoppingListDao.findByIdLiveData(shoppingListId)

    suspend fun findById(context: Context,shoppingListId:String) =
        getDatabase(context).shoppingListDao.findById(shoppingListId)
}