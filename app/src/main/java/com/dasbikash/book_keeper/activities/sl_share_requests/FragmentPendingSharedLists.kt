package com.dasbikash.book_keeper.activities.sl_share_requests

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.pop_up_message.DialogUtils
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.models.TbaSlShareReq
import com.dasbikash.book_keeper.rv_helpers.TbaSlShareReqListAdapter
import com.dasbikash.book_keeper_repo.DataSyncService
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.pop_up_message.showLongSnack
import com.dasbikash.pop_up_message.showShortSnack
import kotlinx.android.synthetic.main.fragment_pending_shared_lists.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentPendingSharedLists : FragmentTemplate() {

    private lateinit var mViewModel:ViewModelShoppingListShareRequests
    private val tbaSlShareReqListAdapter = TbaSlShareReqListAdapter({approveRequest(it)},{declineRequest(it)})

    private fun approveRequest(tbaSlShareReq: TbaSlShareReq) {
        runWithContext {
            NetworkMonitor.runWithNetwork(it){
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    message = "${it.getString(R.string.approve)}?",
                    positiveButtonText = it.getString(R.string.yes),
                    negetiveButtonText = it.getString(R.string.no),
                    doOnPositivePress = {
                        lifecycleScope.launch {
                            try {
                                ShoppingListRepo.approveOnlineShareRequest(it,tbaSlShareReq.shoppingList,tbaSlShareReq.onlineSlShareReq)
                                showShortSnack(R.string.sl_share_request_approved_message)
                            }catch (ex:Throwable){
                                ex.printStackTrace()
                                showShortSnack(R.string.unknown_error_message)
                            }
                        }
                    }
                ))
            }
        }
    }

    private fun declineRequest(tbaSlShareReq: TbaSlShareReq) {
        runWithContext {
            NetworkMonitor.runWithNetwork(it){
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    message = "${it.getString(R.string.decline)}?",
                    positiveButtonText = it.getString(R.string.yes),
                    negetiveButtonText = it.getString(R.string.no),
                    doOnPositivePress = {
                        lifecycleScope.launch {
                            try {
                                ShoppingListRepo.declineOnlineShareRequest(
                                    it,
                                    tbaSlShareReq.onlineSlShareReq
                                )
                                showShortSnack(R.string.sl_share_request_declined_message)
                            } catch (ex: Throwable) {
                                ex.printStackTrace()
                                showShortSnack(R.string.unknown_error_message)
                            }
                        }
                    }
                ))
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pending_shared_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(ViewModelShoppingListShareRequests::class.java)
        rv_pending_shared_lists.adapter = tbaSlShareReqListAdapter
        mViewModel.getTbaSlShareReqLiveData().observe(this,object : Observer<List<TbaSlShareReq>>{
            override fun onChanged(list: List<TbaSlShareReq>?) {
                (list ?: emptyList()).let {
                    tbaSlShareReqListAdapter.submitList(it)
                    if (it.isEmpty()){
                        showLongSnack(R.string.no_pending_list_message)
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        syncSlShareRequestData()
    }

    private fun syncSlShareRequestData(){
        runWithContext {
            NetworkMonitor.runWithNetwork(it){
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        DataSyncService.syncSlShareRequestData(it)
                    } catch (ex: Throwable) {
                        ex.printStackTrace()
                    }
                }
            }
        }
    }
}
