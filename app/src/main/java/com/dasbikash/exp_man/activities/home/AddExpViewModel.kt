package com.dasbikash.exp_man.activities.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dasbikash.exp_man_repo.model.ExpenseCategory

class AddExpViewModel(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val totalExpense:MutableLiveData<Double> = MutableLiveData()
    private val quantity:MutableLiveData<Int> = MutableLiveData()
    private val expenseCategory:MutableLiveData<ExpenseCategory> = MutableLiveData()

    fun setTotalExpense(totalExpense:String){
        try {
            this.totalExpense.postValue(totalExpense.toDouble())
        }catch (ex:Throwable){
            ex.printStackTrace()
            this.totalExpense.postValue(0.0)
        }
    }

    fun getTotalExpense():LiveData<Double> = totalExpense

    fun setQuantity(quantity:String){
        try {
            this.quantity.postValue(quantity.toInt())
        }catch (ex:Throwable){
            ex.printStackTrace()
            this.quantity.postValue(1)
        }
    }

    fun getQuantity():LiveData<Int> = quantity

    fun setExpenseCategory(expenseCategory: ExpenseCategory){
        this.expenseCategory.postValue(expenseCategory)
    }

    fun getExpenseCategory():LiveData<ExpenseCategory> = expenseCategory
}