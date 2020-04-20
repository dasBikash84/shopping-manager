package com.dasbikash.book_keeper.activities.sl_share_requests

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.viewModelScope
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.book_keeper.models.TbaSlShareReq
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModelShoppingListShareRequests(private val mApplication: Application) : AndroidViewModel(mApplication) {

    private val tbaSlShareReqLiveData = MediatorLiveData<List<TbaSlShareReq>>()

    init {
        tbaSlShareReqLiveData.addSource(
            ShoppingListRepo.getApprovalPendingEntries(mApplication),{
                viewModelScope.launch(Dispatchers.IO) {
                    val tbaSlShareReqList = mutableListOf<TbaSlShareReq>()
                    it.asSequence().forEach {
                        tbaSlShareReqList.add(getTbaSlShareReq(it))
                    }
                    runOnMainThread({tbaSlShareReqLiveData.postValue(tbaSlShareReqList)})
                }
            }
        )
    }

    fun getTbaSlShareReqLiveData():LiveData<List<TbaSlShareReq>> = tbaSlShareReqLiveData

    private suspend fun getTbaSlShareReq(onlineSlShareReq: OnlineSlShareReq):TbaSlShareReq{
        val shoppingList = ShoppingListRepo.findInLocalById(mApplication,onlineSlShareReq.sharedDocumentId()!!)!!
        val requester = AuthRepo.findUserById(mApplication,onlineSlShareReq.requesterId!!)!!
        return TbaSlShareReq(shoppingList, onlineSlShareReq, requester)
    }

}