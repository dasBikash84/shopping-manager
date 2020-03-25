package com.dasbikash.book_keeper.activities.home.shopping_list

import android.app.Application
import androidx.lifecycle.*
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import kotlinx.coroutines.launch

class ViewModelShoppingList(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val shoppingListsLiveData = MediatorLiveData<List<ShoppingList>>()
    fun getShoppingListLiveData(): LiveData<List<ShoppingList>> = shoppingListsLiveData

    init {
        viewModelScope.launch {
            ShoppingListRepo.getAllShoppingLists(mApplication).let {
                shoppingListsLiveData.addSource(it,{
                    shoppingListsLiveData.postValue(it)
                })
            }
        }
    }
}