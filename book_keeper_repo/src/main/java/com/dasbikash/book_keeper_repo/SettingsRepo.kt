package com.dasbikash.book_keeper_repo

import android.content.Context
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.firebase.FireStoreSettingsService
import com.dasbikash.book_keeper_repo.model.ExpenseCategory
import com.dasbikash.book_keeper_repo.model.UnitOfMeasure
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils

object SettingsRepo:BookKeeperRepo() {

    private const val EXP_CAT_SYNC_TIME_SP_KEY = "com.dasbikash.exp_man_repo.SettingsRepo.EXP_CAT_SYNC_TIME_SP_KEY"
    private const val UOM_SYNC_TIME_SP_KEY = "com.dasbikash.exp_man_repo.SettingsRepo.UOM_SYNC_TIME_SP_KEY"
    private const val SETTINGS_SYNC_INTERVAL = DateUtils.HOUR_IN_MS * 24;

    suspend fun syncSettings(context: Context){
        syncExpenseCategories(context)
        syncUoms(context)
    }

    suspend fun getAllExpenseCategories(context: Context) = getDatabase(context).expenseCategoryDao.findAll()
    suspend fun getAllUoms(context: Context) = getDatabase(context).unitOfMeasureDao.findAll()

    private suspend fun syncExpenseCategories(context: Context){
        if (!shouldSyncExpenseCategories(context)){
            debugLog("Don't need to sync Expense Categories")
            return
        }
        debugLog("Going to sync Expense Categories")

        val newCategories = mutableListOf<ExpenseCategory>()
        getAllExpenseCategories(context).let {
            if (it.isEmpty()){
                debugLog("No old category")
                FireStoreSettingsService.getExpenseCategories().let {
                    it.forEach { debugLog(it) }
                    newCategories.addAll(it)
                }
            }else{
                it.sortedBy { it.modified }.last().modified!!.apply {
                    debugLog("Last modified ExpenseCategory: $this")
                    FireStoreSettingsService.getExpenseCategories(this).let {
                        it.forEach { debugLog(it) }
                        newCategories.addAll(it)
                    }
                }
            }
        }
        getDatabase(context).expenseCategoryDao.addAll(newCategories)
        updateExpenseCategorySyncTime(context)
        debugLog("Expense Categories synced")
    }

    private suspend fun syncUoms(context: Context){
        if (!shouldSyncUoms(context)){
            debugLog("Don't need to sync uoms")
            return
        }

        debugLog("Going to sync uoms")

        val newUoms = mutableListOf<UnitOfMeasure>()
        getAllUoms(context).let {
            if (it.isEmpty()){
                debugLog("No old UnitOfMeasure")
                FireStoreSettingsService.getUnitOfMeasures().let {
                    it.forEach { debugLog(it) }
                    newUoms.addAll(it)
                }
            }else{
                it.sortedBy { it.modified }.last().modified!!.apply {
                    debugLog("Last modified UnitOfMeasure: $this")
                    FireStoreSettingsService.getUnitOfMeasures(this).let {
                        it.forEach { debugLog(it) }
                        newUoms.addAll(it)
                    }
                }
            }
        }
        getDatabase(context).unitOfMeasureDao.addAll(newUoms)
        updateUomSyncTime(context)
        debugLog("Uoms synced")
    }

    private suspend fun shouldSyncExpenseCategories(context: Context):Boolean{
        SharedPreferenceUtils
            .getDefaultInstance()
            .getDataSuspended(context,EXP_CAT_SYNC_TIME_SP_KEY,Long::class.java)
            .let {
                if (it==null){
                    return true
                }else{
                    return (System.currentTimeMillis() - it) > SETTINGS_SYNC_INTERVAL
                }
            }
    }
    private suspend fun shouldSyncUoms(context: Context):Boolean{
        SharedPreferenceUtils
            .getDefaultInstance()
            .getDataSuspended(context, UOM_SYNC_TIME_SP_KEY,Long::class.java)
            .let {
                if (it==null){
                    return true
                }else{
                    return (System.currentTimeMillis() - it) > SETTINGS_SYNC_INTERVAL
                }
            }
    }

    private suspend fun updateExpenseCategorySyncTime(context: Context){
        SharedPreferenceUtils
            .getDefaultInstance()
            .saveDataSuspended(context,System.currentTimeMillis(),EXP_CAT_SYNC_TIME_SP_KEY)
    }
    private suspend fun updateUomSyncTime(context: Context){
        SharedPreferenceUtils
            .getDefaultInstance()
            .saveDataSuspended(context,System.currentTimeMillis(),UOM_SYNC_TIME_SP_KEY)
    }

}