package com.dasbikash.book_keeper.activities.home.shopping_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList
import com.dasbikash.book_keeper.activities.sl_import.ActivityShoppingListImport
import com.dasbikash.book_keeper.activities.sl_share_requests.ActivityShoppingListShareRequests
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.ShoppingListAdapter
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.OnlineDocShareReq
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.ShoppingListApprovalStatus
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showLongSnack
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_shopping_list.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class FragmentShoppingList : FragmentTemplate(),WaitScreenOwner {
    private lateinit var viewModel: ViewModelShoppingList
    override fun registerWaitScreen(): ViewGroup = wait_screen

    private val recentOnlineDocShareRequests = mutableListOf<OnlineDocShareReq>()

    private val shoppingListAdapter = ShoppingListAdapter({launchDetailView(it)})

    private fun launchDetailView(shoppingList: ShoppingList){
        runWithContext {
            startActivity(ActivityShoppingList.getViewIntent(it,shoppingList.id))
        }
    }

    private fun launchEditView(shoppingList: ShoppingList){
        runWithContext {
            startActivity(ActivityShoppingList.getEditIntent(it,shoppingList.id))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewModelShoppingList::class.java)
        rv_shopping_list.adapter = shoppingListAdapter

        btn_add_shopping_list.setOnClickListener { showListAddDialog() }

        viewModel.getShoppingListLiveData().observe(this,object : Observer<List<ShoppingList>>{
            override fun onChanged(list: List<ShoppingList>?) {
                (list ?: emptyList()).let {
                    shoppingListAdapter.submitList(it.sortedByDescending { it.deadLine })
                }
            }
        })

        viewModel.getRecentModifiedShareRequests().observe(this,object : Observer<List<OnlineDocShareReq>>{
            override fun onChanged(list: List<OnlineDocShareReq>?) {
                list?.let {
                    it.asSequence().forEach { processRecentOnlineDocShareRequest(it) }
                    if (it.isNotEmpty()) {
                        viewModel.setLastSharedRequestEntryUpdateTime()
                    }
                }
            }
        })
        viewModel.setLastSharedRequestEntryUpdateTime()
    }

    private fun processRecentOnlineDocShareRequest(onlineDocShareReq: OnlineDocShareReq){
        debugLog("processRecentOnlineDocShareRequest: $onlineDocShareReq")
        if (onlineDocShareReq.checkIfShoppingListShareRequest()) {
            debugLog("checkIfShoppingListShareRequest: $onlineDocShareReq")
            debugLog(onlineDocShareReq.approvalStatus.name)
            when (onlineDocShareReq.approvalStatus) {
                ShoppingListApprovalStatus.PENDING -> {
                    if (!recentOnlineDocShareRequests.map { it.id }
                            .contains(onlineDocShareReq.id)) {
                        recentOnlineDocShareRequests.add(onlineDocShareReq)
                        setListenerForPendingOnlineSlShareRequest(onlineDocShareReq)
                    }
                }
                ShoppingListApprovalStatus.APPROVED -> {
                    runWithContext {
                        lifecycleScope.launch {
                            val shoppingList:ShoppingList = ShoppingListRepo.findById(it,onlineDocShareReq.sharedDocumentId()!!)!!
                            AuthRepo.findUserById(it,onlineDocShareReq.ownerId!!)?.let {
                                showLongSnack(
                                    getString(R.string.shopping_list_share_req_approved,it.displayText()),
                                    getString(R.string.show_list_action_text),
                                    {launchDetailView(shoppingList)}
                                )
                            }
                        }
                    }
                }
                ShoppingListApprovalStatus.DENIED -> {
                    runWithContext {
                        lifecycleScope.launch {
                            AuthRepo.findUserById(it,onlineDocShareReq.ownerId!!)?.let {
                                showLongSnack(getString(R.string.shopping_list_share_req_denied,it.displayText()))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setListenerForPendingOnlineSlShareRequest(onlineDocShareReq: OnlineDocShareReq) {
        debugLog("setListenerForPendingOnlineSlShareRequest: ${onlineDocShareReq}")
        runWithActivity {
            ShoppingListRepo.setListenerForPendingOnlineSlShareRequest(it,it as AppCompatActivity,onlineDocShareReq)
        }
    }

    private fun showListAddDialog() {
        runWithContext {
            startActivity(ActivityShoppingList.getCreateIntent(it))
        }
    }
    override fun getPageTitle(context: Context):String? = context.getString(R.string.shopping_list_title)

    override fun getOptionsMenu(context: Context): MenuView? {
        val menuView = MenuView()
        menuView.add(
            MenuViewItem(
                text = context.getString(R.string.shopping_list_import_title),
                task = {activity?.let { (it as Activity).startActivity(ActivityShoppingListImport::class.java) }}
            )
        )
        menuView.add(
            MenuViewItem(
                text = context.getString(R.string.add_shopping_list),
                task = {showListAddDialog()}
            )
        )
        menuView.add(
            MenuViewItem(
                text = context.getString(R.string.pending_sl_share_requests),
                task = { runWithActivity { it.startActivity(ActivityShoppingListShareRequests::class.java) }}
            )
        )
        return menuView
    }
}