package com.dasbikash.book_keeper.activities.shopping_list.edit

import android.app.Application
import androidx.lifecycle.*
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelShoppingListEdit(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private val shoppingList = MutableLiveData<ShoppingList>()

    fun getShoppingList():LiveData<ShoppingList> = shoppingList
    fun setShoppingList(shoppingList: ShoppingList) = this.shoppingList.postValue(shoppingList)
    fun setShoppingListId(shoppingListId:String){
        viewModelScope.launch(Dispatchers.IO) {
            ShoppingListRepo.findById(mApplication,shoppingListId)?.let {
                shoppingList.postValue(it)
            }
        }
    }
}