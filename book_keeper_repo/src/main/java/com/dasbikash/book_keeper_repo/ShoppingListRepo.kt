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

    suspend fun save(context: Context,shoppingListItem: ShoppingListItem){
        val shoppingList = getShoppingListDao(context).findById(shoppingListItem.shoppingListId!!)!!
        val itemIds = mutableListOf<String>()
        shoppingList.shoppingListItemIds?.let { itemIds.addAll(it) }
        if (!itemIds.contains(shoppingListItem.id)){
            itemIds.add(shoppingListItem.id)
        }
        shoppingList.shoppingListItemIds = itemIds.toList()
        shoppingListItem.updateModified()
        getShoppingListItemDao(context).add(shoppingListItem)
        save(context, shoppingList)
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

    suspend fun findShoppingListItemById(context: Context,shoppingListItemId:String) =
        getShoppingListItemDao(context).findById(shoppingListItemId)

    suspend fun delete(context: Context,shoppingListItem: ShoppingListItem) {
        val shoppingList = getShoppingListDao(context).findById(shoppingListItem.shoppingListId!!)!!
        val itemIds = mutableListOf<String>()
        shoppingList.shoppingListItemIds?.filter { it!=shoppingListItem.id }?.let { itemIds.addAll(it) }
        shoppingList.shoppingListItemIds = itemIds.toList()
        shoppingListItem.updateModified()
        getShoppingListItemDao(context).delete(shoppingListItem)
        save(context, shoppingList)
    }

    suspend fun delete(context: Context,shoppingList: ShoppingList) {
        FireStoreShoppingListService.deleteShoppingList(shoppingList)
        shoppingList.shoppingListItemIds?.asSequence()?.forEach {
            getShoppingListItemDao(context).findById(it)?.let {
                getShoppingListItemDao(context).delete(it)
            }
        }
        getShoppingListDao(context).delete(shoppingList)
    }
}