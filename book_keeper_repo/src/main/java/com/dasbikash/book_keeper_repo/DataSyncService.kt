package com.dasbikash.book_keeper_repo

import android.content.Context
import com.dasbikash.android_basic_utils.utils.debugLog

object DataSyncService {

    suspend fun syncExpenseData(context: Context){
        debugLog("starting syncExpenseData.")
        ExpenseRepo.syncData(context)
        debugLog("syncExpenseData done.")
    }

    suspend fun syncShoppingListData(context: Context){
        debugLog("starting syncShoppingListData.")
        ShoppingListRepo.syncShoppingListData(context)
        debugLog("syncShoppingListData done.")
    }

    suspend fun syncSlShareRequestData(context: Context){
        debugLog("starting syncSlShareRequestData.")
        ShoppingListRepo.syncSlShareRequestData(context)
        debugLog("syncSlShareRequestData done.")
    }

    suspend fun syncConnectionRequestData(context: Context){
        debugLog("starting syncConnectionRequestData.")
        ConnectionRequestRepo.syncData(context)
        debugLog("syncConnectionRequestData done.")
    }

    suspend fun syncUserData(context: Context){
        debugLog("starting syncUserData.")
        AuthRepo.syncUserData(context)
        debugLog("syncUserData done.")
    }

    suspend fun syncEventNotifications(context: Context){
        debugLog("starting syncEventNotifications.")
        EventNotificationRepo.syncData(context)
        debugLog("syncEventNotifications done.")
    }

    suspend fun syncNoteEntries(context: Context){
        debugLog("starting syncNoteEntries.")
        NoteEntryRepo.syncData(context)
        debugLog("syncNoteEntries done.")
    }

    suspend fun syncAppData(context: Context){
        debugLog("starting Data sync!!")
        syncExpenseData(context)
        syncShoppingListData(context)
        syncSlShareRequestData(context)
        syncConnectionRequestData(context)
        syncUserData(context)
        syncEventNotifications(context)
        syncNoteEntries(context)
        debugLog("Data sync done!!")
    }
}