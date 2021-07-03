package com.dasbikash.book_keeper.activities.home.shopping_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList

class ViewModelShoppingList(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val shoppingListsLiveData = MediatorLiveData<List<ShoppingList>>()
    fun getShoppingListLiveData(): LiveData<List<ShoppingList>> = shoppingListsLiveData


    init {
        ShoppingListRepo.getAllShoppingLists(mApplication).let {
                shoppingListsLiveData.addSource(it,{
                    val shoppingLists = mutableListOf<ShoppingList>()
                    it.map {
                        debugLog("All: $it")
                        it
                    }.asSequence().filter {
                        it.userId == AuthRepo.getUserId() ||
                                (it.partnerIds?.contains(AuthRepo.getUserId()) == true)
                    }.forEach {
                        debugLog("Filtered: ${it}")
                        shoppingLists.add(it)
                    }
                    shoppingListsLiveData.postValue(shoppingLists)
                })
            }
    }

}