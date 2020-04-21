package com.dasbikash.book_keeper_repo

import android.content.Context

object DataSyncService {

    suspend fun syncExpenseData(context: Context){
        ExpenseRepo.syncData(context)
    }

    suspend fun syncShoppingListData(context: Context){
        ShoppingListRepo.syncShoppingListData(context)
    }

    suspend fun syncSlShareRequestData(context: Context){
        ShoppingListRepo.syncSlShareRequestData(context)
    }

    suspend fun syncConnectionRequestData(context: Context){
        ConnectionRequestRepo.syncData(context)
    }

    suspend fun syncUserData(context: Context){
        AuthRepo.syncUserData(context)
    }

    suspend fun syncAppData(context: Context){
        syncExpenseData(context)
        syncShoppingListData(context)
        syncSlShareRequestData(context)
        syncConnectionRequestData(context)
        syncUserData(context)
    }
}