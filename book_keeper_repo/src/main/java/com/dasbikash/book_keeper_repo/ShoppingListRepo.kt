package com.dasbikash.book_keeper_repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.firebase.FireStoreShoppingListService
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import com.dasbikash.book_keeper_repo.model.SlReminderGenLog
import com.dasbikash.book_keeper_repo.model.User
import java.util.*

object ShoppingListRepo:BookKeeperRepo() {

    private fun getShoppingListDao(context: Context) = getDatabase(context).shoppingListDao
    private fun getShoppingListItemDao(context: Context) = getDatabase(context).shoppingListItemDao
    private fun getSlReminderGenLogDao(context: Context) = getDatabase(context).slReminderGenLogDao

    suspend fun getAllShoppingLists(context: Context):LiveData<List<ShoppingList>>{
        return AuthRepo.getUser(context)!!.let { getDatabase(context).shoppingListDao.findAllLiveData(it.id) }
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
        getSlReminderGenLogDao(context).deleteByShoppingListId(shoppingList.id)
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
            shoppingListItemIds.add(it.id)
        }
        shoppingList.shoppingListItemIds = shoppingListItemIds.toList()
        getShoppingListDao(context).add(shoppingList)
        shoppingList.shoppingListItems?.asSequence()?.forEach {
            getShoppingListItemDao(context).add(it)
        }
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
        shoppingList.active =false
        shoppingList.updateModified()
        saveToFireBase(context, shoppingList)
        shoppingList.shoppingListItemIds?.asSequence()?.forEach {
            getShoppingListItemDao(context).findById(it)?.let {
                getShoppingListItemDao(context).delete(it)
            }
        }
        getShoppingListDao(context).delete(shoppingList)
    }

    suspend fun syncData(context: Context) {
        AuthRepo.getUser(context)?.apply {
            getShoppingListDao(context).findAll(id).sortedBy { it.modified }.let {
                if (it.isEmpty()){
                    return@let null
                }else{
                    return@let it.last().modified
                }
            }.let {
                FireStoreShoppingListService
                    .getLatestShoppingLists(this,it)
                    ?.asSequence()
                    ?.forEach {
                        saveFireBaseEntry(context,it)
                    }
            }
        }
    }

    suspend fun findAllWithReminder(context: Context, user: User): List<ShoppingList> {
        return getShoppingListDao(context).findAllWithReminder(user.id)
    }

    suspend fun calculateNextReminderTime(context: Context,shoppingList: ShoppingList): Date?{
        if (shoppingList.deadLine !=null && shoppingList.getCountDownTime() !=null){
            val firstReminderTime = shoppingList.deadLine!!.time - shoppingList.getCountDownTime()!!
            val reminderLogs = getSlReminderGenLogDao(context).findByShoppingListId(shoppingList.id).sortedBy { it.created }
            if (reminderLogs.isNotEmpty()){
                if (shoppingList.getReminderInterval() != null){//single reminder check
                    val nextReminderTime = reminderLogs.last().created.time + shoppingList.getReminderInterval()!!
                    if (nextReminderTime < shoppingList.deadLine!!.time){
                        return Date(nextReminderTime)
                    }
                }
            }else{
                return Date(firstReminderTime)
            }
        }
        return null
    }

    suspend fun logShoppingReminder(context: Context, shoppingList: ShoppingList) {
        getSlReminderGenLogDao(context).add(SlReminderGenLog(shoppingListId = shoppingList.id))
    }
}