package com.dasbikash.book_keeper.activities.shopping_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import com.dasbikash.book_keeper_repo.model.ShoppingList

class ViewModelShoppingListEdit(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private val shoppingList = MediatorLiveData<ShoppingList>()
//    private val shoppingListId =
}