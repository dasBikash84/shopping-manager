package com.dasbikash.book_keeper.activities.home.add_exp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dasbikash.book_keeper_repo.model.ExpenseItem

class ViewModelAddExp(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val expenseCategory:MutableLiveData<Int> = MutableLiveData()
    private val expenseItems:MutableLiveData<List<ExpenseItem>> = MutableLiveData()
    private val vatTax:MutableLiveData<Double> = MutableLiveData()

    init {
        vatTax.postValue(0.0)
        expenseItems.postValue(emptyList())
    }

    fun setExpenseCategory(expenseCategory: Int){
        this.expenseCategory.postValue(expenseCategory)
    }

    fun getExpenseCategory():LiveData<Int> = expenseCategory

    fun addExpenseItem(expenseItem: ExpenseItem){
        expenseItems.value.let {
            if (it==null){
                expenseItems.postValue(listOf(expenseItem))
            }else{
                val newItems = mutableListOf<ExpenseItem>()
                it.filter { it.id!=expenseItem.id }.forEach {
                    newItems.add(it)
                }
                newItems.add(expenseItem)
                expenseItems.postValue(newItems.toList())
            }
        }
    }
    fun addExpenseItems(expenseItems: List<ExpenseItem>){
        this.expenseItems.value.let {
            if (it==null){
                this.expenseItems.postValue(expenseItems)
            }else{
                val newItems = mutableListOf<ExpenseItem>()
                newItems.addAll(it)
                expenseItems.asSequence().forEach {
                    if (!newItems.map { it.id }.contains(it.id)){
                        newItems.add(it)
                    }
                }
                this.expenseItems.postValue(newItems.toList())
            }
        }
    }

    fun removeExpenseItem(expenseItem: ExpenseItem){
        expenseItems.value?.filter { it.id!=expenseItem.id }?.let {
            expenseItems.postValue(it)
        }
    }

    fun getExpenseItems():LiveData<List<ExpenseItem>> = expenseItems
    fun getVatTax():LiveData<Double> = vatTax
    fun setVatTax(vatTax:Double){
        if (vatTax>=0) {
            this.vatTax.postValue(vatTax)
        }
    }
}