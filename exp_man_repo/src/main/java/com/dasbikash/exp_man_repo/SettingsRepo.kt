package com.dasbikash.exp_man_repo

import android.content.Context
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.exp_man_repo.firebase.FireStoreSettingsUtils
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.model.UnitOfMeasure

object SettingsRepo:ExpenseManagerRepo() {

    suspend fun syncSettings(context: Context){
        syncExpenseCategories(context)
        syncUoms(context)
    }

    suspend fun getAllExpenseCategories(context: Context) = getDatabase(context).expenseCategoryDao.findAll()
    suspend fun getAllUoms(context: Context) = getDatabase(context).unitOfMeasureDao.findAll()

    private suspend fun syncExpenseCategories(context: Context){
        val newCategories = mutableListOf<ExpenseCategory>()
        getAllExpenseCategories(context).let {
            if (it.isEmpty()){
                debugLog("No old category")
                FireStoreSettingsUtils.getExpenseCategories().let {
                    it.forEach { debugLog(it) }
                    newCategories.addAll(it)
                }
            }else{
                it.sortedBy { it.modified }.last().modified!!.apply {
                    debugLog("Last modified ExpenseCategory: $this")
                    FireStoreSettingsUtils.getExpenseCategories(this).let {
                        it.forEach { debugLog(it) }
                        newCategories.addAll(it)
                    }
                }
            }
        }
        getDatabase(context).expenseCategoryDao.addAll(newCategories)
    }

    private suspend fun syncUoms(context: Context){
        val newUoms = mutableListOf<UnitOfMeasure>()
        getAllUoms(context).let {
            if (it.isEmpty()){
                debugLog("No old UnitOfMeasure")
                FireStoreSettingsUtils.getUnitOfMeasures().let {
                    it.forEach { debugLog(it) }
                    newUoms.addAll(it)
                }
            }else{
                it.sortedBy { it.modified }.last().modified!!.apply {
                    debugLog("Last modified UnitOfMeasure: $this")
                    FireStoreSettingsUtils.getUnitOfMeasures(this).let {
                        it.forEach { debugLog(it) }
                        newUoms.addAll(it)
                    }
                }
            }
        }
        getDatabase(context).unitOfMeasureDao.addAll(newUoms)
    }

}