package com.dasbikash.book_keeper_repo

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.firebase.FireStoreOnlineSlShareService
import com.dasbikash.book_keeper_repo.firebase.FireStoreShoppingListService
import com.dasbikash.book_keeper_repo.model.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

object ShoppingListRepo : BookKeeperRepo() {

    private fun getShoppingListDao(context: Context) = getDatabase(context).shoppingListDao
    private fun getShoppingListItemDao(context: Context) = getDatabase(context).shoppingListItemDao
    private fun getSlReminderGenLogDao(context: Context) = getDatabase(context).slReminderGenLogDao
    private fun getOnlineDocShareReqDao(context: Context) = getDatabase(context).onlineSlShareReqDao

    fun getAllShoppingLists(context: Context): LiveData<List<ShoppingList>> {
        return getDatabase(context).shoppingListDao.findAllLiveData()
    }

    suspend fun save(context: Context, shoppingListItem: ShoppingListItem) {
        shoppingListItem.shoppingListId?.let {
            getShoppingListDao(context).findById(it)?.let {
                val itemIds = mutableListOf<String>()
                it.shoppingListItemIds?.let { itemIds.addAll(it) }
                if (!itemIds.contains(shoppingListItem.id)) {
                    itemIds.add(shoppingListItem.id)
                }
                it.shoppingListItemIds = itemIds.toList()
                shoppingListItem.updateModified()
                saveLocal(context, shoppingListItem)
                save(context, it)
            }
        }
    }

    suspend fun saveLocal(context: Context, shoppingList: ShoppingList) {
        getShoppingListDao(context).add(shoppingList)
    }

    suspend fun saveLocal(context: Context, shoppingListItem: ShoppingListItem) {
        getShoppingListItemDao(context).add(shoppingListItem)
    }

    suspend fun saveLocal(context: Context, shoppingListItems: List<ShoppingListItem>) {
        getShoppingListItemDao(context).addAll(shoppingListItems)
    }

    suspend fun save(context: Context, shoppingList: ShoppingList) {
        shoppingList.updateModified()
        saveToFireBase(context, shoppingList)
        saveLocal(context, shoppingList)
        getSlReminderGenLogDao(context).deleteByShoppingListId(shoppingList.id)
    }

    private suspend fun saveToFireBase(context: Context, shoppingList: ShoppingList) {
        shoppingList.shoppingListItems = getShoppingListItems(context, shoppingList)
        FireStoreShoppingListService.saveShoppingList(shoppingList)
    }

    suspend fun checkIfAllBought(context: Context, shoppingList: ShoppingList):Boolean{
        return  getShoppingListItems(context,shoppingList)
                    ?.filter { it.expenseEntryId!=null }
                    ?.size ?: 0 == shoppingList.shoppingListItemIds?.size ?: 0
    }

    suspend fun getShoppingListItems(context: Context, shoppingList: ShoppingList)
            : List<ShoppingListItem>? {
        return shoppingList.shoppingListItemIds?.map { getShoppingListItemDao(context).findById(it)!! }
    }

    private suspend fun saveFireBaseEntry(context: Context, shoppingList: ShoppingList) {
        val shoppingListItemIds = mutableListOf<String>()
        shoppingList.shoppingListItems?.asSequence()?.forEach {
            shoppingListItemIds.add(it.id)
        }
        shoppingList.shoppingListItemIds = shoppingListItemIds.toList()
        saveLocal(context, shoppingList)
        shoppingList.shoppingListItems?.let {
            saveLocal(context, it)
        }
    }

    fun getLiveDataById(context: Context, shoppingListId: String) =
        getDatabase(context).shoppingListDao.findByIdLiveData(shoppingListId)

    suspend fun findInLocalById(context: Context, shoppingListId: String):ShoppingList? {
        return getDatabase(context).shoppingListDao.findById(shoppingListId)
    }

    suspend fun findById(context: Context, shoppingListId: String):ShoppingList? {
        findInLocalById(context,shoppingListId)?.let {
            return it
        }
        return syncShoppingListById(shoppingListId,context)
    }

    suspend fun findShoppingListItemById(context: Context, shoppingListItemId: String) =
        getShoppingListItemDao(context).findById(shoppingListItemId)

    suspend fun delete(context: Context, shoppingListItem: ShoppingListItem) {
        val shoppingList = getShoppingListDao(context).findById(shoppingListItem.shoppingListId!!)!!
        val itemIds = mutableListOf<String>()
        shoppingList.shoppingListItemIds?.filter { it != shoppingListItem.id }
            ?.let { itemIds.addAll(it) }
        shoppingList.shoppingListItemIds = itemIds.toList()
        shoppingListItem.updateModified()
        getShoppingListItemDao(context).delete(shoppingListItem)
        save(context, shoppingList)
    }

    suspend fun delete(context: Context, shoppingList: ShoppingList) {
        shoppingList.active = false
        shoppingList.updateModified()
        saveToFireBase(context, shoppingList)
        shoppingList.shoppingListItemIds?.asSequence()?.forEach {
            getShoppingListItemDao(context).findById(it)?.let {
                getShoppingListItemDao(context).delete(it)
            }
        }
        getShoppingListDao(context).delete(shoppingList)
    }

    suspend fun syncShoppingListData(context: Context) {
        getShoppingListDao(context).findAll(AuthRepo.getUserId()).sortedBy { it.modified }.let {
            if (it.isEmpty()) {
                return@let null
            } else {
                return@let it.last().modified
            }
        }.let {
            FireStoreShoppingListService
                .getLatestShoppingLists(it)
                .asSequence()
                .forEach {
                    saveFireBaseEntry(context, it)
                }
        }
    }

    suspend fun syncSlShareRequestData(context: Context){
        getOnlineDocShareReqDao(context).findAll().let {
            debugLog("getOnlineDocShareReqDao: ${it}")
            if (it.isEmpty()){
                return@let null
            }else{
                return@let it.last().modified
            }
        }.let {
            FireStoreOnlineSlShareService
                .getLatestRequestsToMe(it)
                .asSequence()
                .forEach {
                        debugLog("getLatestRequestsToMe: ${it}")
                        getOnlineDocShareReqDao(context).add(it)
                }

            (FireStoreOnlineSlShareService
                .getLatestRequestsFromMe(it)).let {
                    it.forEach {
                        debugLog("getLatestRequestsFromMe: ${it}")
                        getOnlineDocShareReqDao(context).add(it)
                    }
                    it.filter {
                        debugLog("it.checkIfApproved(): $it")
                        it.checkIfApproved()
                    }.map { it.sharedDocumentId()!! }.let {
                        debugLog("docids: $it")
                        val sharedSlIds = mutableSetOf<String>()
                        sharedSlIds.addAll(it)
                        sharedSlIds.addAll(getSharedSlIds(context))
                        sharedSlIds.asSequence().forEach {
                            syncShoppingListById(it,context)
                        }
                    }
                }
        }
    }

    private suspend fun getSharedSlIds(context: Context): List<String> {
        return getShoppingListDao(context)
                    .findAll()
                    .filter { it.userId != AuthRepo.getUserId() &&
                                it.partnerIds?.contains(AuthRepo.getUserId()) == true }
                    .map {
                        debugLog("getSharedSlIds: $it")
                        it.id
                    }
    }

    suspend fun findAllWithReminder(context: Context): List<ShoppingList> {
        return getShoppingListDao(context).findAllWithReminder()
    }

    suspend fun calculateNextReminderTime(context: Context, shoppingList: ShoppingList): Date? {
        if (shoppingList.deadLine != null &&
            shoppingList.getCountDownTime() != null &&
            System.currentTimeMillis() < shoppingList.deadLine!!.time &&
            (getShoppingListItems(context, shoppingList)?.filter { it.expenseEntryId==null }?.count() ?: 0 > 0)) {
            val firstReminderTime = shoppingList.deadLine!!.time - shoppingList.getCountDownTime()!!
            val reminderLogs = getSlReminderGenLogDao(context).findByShoppingListId(shoppingList.id)
                .sortedBy { it.created }
            if (reminderLogs.isNotEmpty()) {
                if (shoppingList.getReminderInterval() != null) {//single reminder check
                    val nextReminderTime =
                        reminderLogs.last().created.time + shoppingList.getReminderInterval()!!
                    if (nextReminderTime < shoppingList.deadLine!!.time) {
                        return Date(nextReminderTime)
                    }
                }
            } else {
                return Date(firstReminderTime)
            }
        }
        return null
    }

    suspend fun logShoppingReminder(context: Context, shoppingList: ShoppingList) {
        getSlReminderGenLogDao(context).add(SlReminderGenLog(shoppingListId = shoppingList.id))
    }

    suspend fun saveOfflineShoppingList(context: Context, shoppingList: ShoppingList) {
        getShoppingListDao(context).findByUserAndTitle(AuthRepo.getUserId(),shoppingList.title!!.trim())?.let {
            shoppingList.title = "${shoppingList.title}_${DateUtils.getLongDateString(Date())}"
        }
        saveCopiedShoppingList(context, shoppingList)
    }

    suspend fun saveCopiedShoppingList(context: Context, shoppingListId: String,name:String):ShoppingList?{
        if (name.isBlank()){return null}
        getShoppingListDao(context)
            .findByUserAndTitle(
                AuthRepo.getUserId(),name.trim())?.let {
                return null
            }
        val shoppingList = findById(context, shoppingListId)!!
        debugLog("saveCopiedShoppingList: $shoppingList")
        shoppingList.title = name.trim()
        shoppingList.deadLine = null
        shoppingList.setCountDownTime(null)
        shoppingList.setReminderInterval(null)
        shoppingList.shoppingListItems = getShoppingListItems(context, shoppingList)
        debugLog("saveCopiedShoppingList: $shoppingList")
        return saveCopiedShoppingList(context, shoppingList)
    }

    private suspend fun saveCopiedShoppingList(context: Context, shoppingList: ShoppingList):ShoppingList{
        UUID.randomUUID().toString().apply {
            shoppingList.id = this
            shoppingList.shoppingListItems?.forEach {
                it.id = UUID.randomUUID().toString()
                it.shoppingListId = this
                it.expenseEntryId = null
            }
        }
        shoppingList.partnerIds = null
        shoppingList.userId = AuthRepo.getUserId()
        shoppingList.created=Date()
        shoppingList.modified=Date()
        shoppingList.shoppingListItemIds =
            shoppingList.shoppingListItems?.map { it.id }
        debugLog("saveCopiedShoppingList 2: $shoppingList")
        saveLocal(context, shoppingList)
        shoppingList.shoppingListItems?.forEach {
            saveLocal(context, it)
        }
        debugLog("saveCopiedShoppingList 2: $shoppingList")
        save(context, shoppingList)
        return shoppingList
    }

    suspend fun postOnlineSlShareRequest(context: Context, onlineDocShareParams: OnlineDocShareParams) {
        val onlineDocShareReq = OnlineSlShareReq.getInstance(onlineDocShareParams)
        FireStoreOnlineSlShareService.postRequest(onlineDocShareReq)
        save(context,onlineDocShareReq)
    }

    suspend fun isShareRequestValid(context: Context, shoppingListPath:String):Boolean{
        try {
            val shoppingListId = shoppingListPath.split("/").let { return@let it.last() }
            debugLog(shoppingListId)
            return findInLocalById(context, shoppingListId) == null
        }catch (ex:Throwable){
            ex.printStackTrace()
            return true
        }
    }

    internal suspend fun save(context:Context, onlineSlShareReq:OnlineSlShareReq){
        onlineSlShareReq.modified = Date()
        getOnlineDocShareReqDao(context).add(onlineSlShareReq)
    }

    fun getFbPath(shoppingList: ShoppingList):String =
        FireStoreShoppingListService.getFbPath(shoppingList.id)

    fun getRecentModifiedShareRequestEntries(context: Context,leastModifiedTime: Date) =
        getOnlineDocShareReqDao(context).getRecentModifiedEntries(leastModifiedTime)

    fun getApprovalPendingEntries(context: Context) =
        getOnlineDocShareReqDao(context).getApprovalPendingEntries()

    fun setListenerForPendingOnlineSlShareRequest(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        onlineSlShareReq: OnlineSlShareReq
    ) {
        debugLog("setListenerForPendingOnlineSlShareRequest: ${onlineSlShareReq}")
        FireStoreOnlineSlShareService
            .setListenerForPendingOnlineDocShareRequest(
                lifecycleOwner,
                onlineSlShareReq,
                {
                    processDownloadedOnlineDocShareRequest(context,it)
                }
            )
    }

    private fun processDownloadedOnlineDocShareRequest(
        context: Context, onlineSlShareReq: OnlineSlShareReq) {
        debugLog("processDownloadedOnlineDocShareRequest: ${onlineSlShareReq}")
        GlobalScope.launch {
            debugLog("processDownloadedOnlineDocShareRequest: ${onlineSlShareReq.checkIfFromMe()}")
            if (onlineSlShareReq.checkIfFromMe()) {
                debugLog("processDownloadedOnlineDocShareRequest: onlineDocShareReq.checkIfShoppingListShareRequest()")
                if (onlineSlShareReq.approvalStatus != RequestApprovalStatus.PENDING) {
                    if (onlineSlShareReq.approvalStatus == RequestApprovalStatus.APPROVED) {
                        syncShoppingListById(onlineSlShareReq.sharedDocumentId()!!, context)
                    }
                    save(context, onlineSlShareReq)
                }
            }
        }
    }

    suspend fun syncShoppingListById(
        shoppingListId:String,
        context: Context
    ):ShoppingList? {
        debugLog("fetchShoppingListById: $shoppingListId")
        return FireStoreShoppingListService
                .fetchShoppingListById(shoppingListId)?.apply {
                debugLog("fetchShoppingListById: $this")
                    saveFireBaseEntry(context, this)
                }
    }

    suspend fun approveOnlineShareRequest(
        context: Context,
        shoppingList: ShoppingList,
        onlineSlShareReq: OnlineSlShareReq
    ) {
        val partnerIds = mutableSetOf<String>()
        shoppingList.partnerIds?.let { partnerIds.addAll(it) }
        partnerIds.add(onlineSlShareReq.partnerUserId!!)
        shoppingList.partnerIds = partnerIds.toList()
        saveToFireBase(context, shoppingList)
        saveLocal(context, shoppingList)
        onlineSlShareReq.approvalStatus = RequestApprovalStatus.APPROVED
        FireStoreOnlineSlShareService.saveRequest(onlineSlShareReq)
        getOnlineDocShareReqDao(context).add(onlineSlShareReq)
    }

    suspend fun declineOnlineShareRequest(context: Context, onlineSlShareReq: OnlineSlShareReq) {
        onlineSlShareReq.approvalStatus = RequestApprovalStatus.DENIED
        FireStoreOnlineSlShareService.saveRequest(onlineSlShareReq)
        getOnlineDocShareReqDao(context).add(onlineSlShareReq)
    }
}