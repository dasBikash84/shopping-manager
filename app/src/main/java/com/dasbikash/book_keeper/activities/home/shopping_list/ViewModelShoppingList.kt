package com.dasbikash.book_keeper.activities.home.shopping_list

import android.app.Application
import androidx.lifecycle.*
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import com.dasbikash.book_keeper_repo.model.ShoppingList
import java.util.*

class ViewModelShoppingList(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val shoppingListsLiveData = MediatorLiveData<List<ShoppingList>>()
    fun getShoppingListLiveData(): LiveData<List<ShoppingList>> = shoppingListsLiveData

    private var requestEntryLiveData:LiveData<List<OnlineSlShareReq>>? = null
    private val recentModifiedShareRequestEntryLiveData = MediatorLiveData<List<OnlineSlShareReq>>()


    init {
        ShoppingListRepo.getAllShoppingLists(mApplication).let {
                shoppingListsLiveData.addSource(it,{
                    val shoppingLists = mutableListOf<ShoppingList>()
                    it.asSequence().filter {
                        it.userId == AuthRepo.getUserId() ||
                                (it.partnerIds?.contains(AuthRepo.getUserId()) == true)
                    }.forEach { shoppingLists.add(it) }
                    shoppingListsLiveData.postValue(shoppingLists)
                })
            }
    }

    fun getRecentModifiedShareRequests():LiveData<List<OnlineSlShareReq>> = recentModifiedShareRequestEntryLiveData

    fun setLastSharedRequestEntryUpdateTime(time:Date=Date()){
        debugLog("setLastSharedRequestEntryUpdateTime: ${time}")
        requestEntryLiveData?.let {
            debugLog("recentModifiedShareRequestEntryLiveData.removeSource(it)")
            recentModifiedShareRequestEntryLiveData.removeSource(it)
        }
        requestEntryLiveData = ShoppingListRepo.getRecentModifiedShareRequestEntries(mApplication,time)
        debugLog("requestEntryLiveData: ${requestEntryLiveData==null} ${requestEntryLiveData?.javaClass?.simpleName}")
        recentModifiedShareRequestEntryLiveData.addSource(requestEntryLiveData!!,{
            debugLog("${it}")
            recentModifiedShareRequestEntryLiveData.postValue(it)
        })
    }

}