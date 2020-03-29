package com.dasbikash.book_keeper.activities.shopping_list.view

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList

class ViewModelShoppingListView(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val shoppingList = MediatorLiveData<ShoppingList>()

    fun setShoppingListId(shoppingListId:String){
        shoppingList.addSource(ShoppingListRepo.getLiveDataById(mApplication,shoppingListId),
            Observer {
                shoppingList.postValue(it)
            })
    }

    fun getShoppingList():LiveData<ShoppingList> = shoppingList
}