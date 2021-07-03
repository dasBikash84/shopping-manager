package com.dasbikash.book_keeper.activities.sl_item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dasbikash.book_keeper_repo.model.ShoppingListItem

class ViewModelShoppingListItem(private val mApplication: Application) : AndroidViewModel(mApplication) {
    private val shoppingListItem = MutableLiveData<ShoppingListItem>()

    fun getShoppingListItem():LiveData<ShoppingListItem> = shoppingListItem
    fun setShoppingListItem(shoppingListItem: ShoppingListItem) = this.shoppingListItem.postValue(shoppingListItem)

    fun addProductImage(imageLoc:String){
        shoppingListItem.value?.let {
            val imageLocs = mutableListOf<String>()
            it.images?.let { imageLocs.addAll(it) }
            if (!imageLocs.contains(imageLoc)) {
                imageLocs.add(imageLoc)
            }
            it.images = imageLocs.toList()
            shoppingListItem.postValue(it)
        }
    }

    fun removeProductImage(imageLoc:String){
        shoppingListItem.value?.let {
            val imageLocs = mutableListOf<String>()
            it.images?.let { imageLocs.addAll(it) }
            imageLocs.remove(imageLoc)
            it.images = imageLocs.toList()
            shoppingListItem.postValue(it)
        }
    }

    fun addBrandSuggestion(name: String) {
        shoppingListItem.value?.let {
            val brandNames = mutableListOf<String>()
            it.brandNameSuggestions?.let { brandNames.addAll(it) }
            if (!brandNames.contains(name)) {
                brandNames.add(name)
            }
            it.brandNameSuggestions = brandNames.toList()
            shoppingListItem.postValue(it)
        }
    }

    fun removeBrandNameSuggestion(name: String){
        shoppingListItem.value?.let {
            val brandNames = mutableListOf<String>()
            it.brandNameSuggestions?.let { brandNames.addAll(it) }
            brandNames.remove(name)
            it.brandNameSuggestions = brandNames.toList()
            shoppingListItem.postValue(it)
        }
    }
}