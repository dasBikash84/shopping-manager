package com.dasbikash.book_keeper.activities.home.shopping_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList
import com.dasbikash.book_keeper.activities.sl_import.ActivityShoppingListImport
import com.dasbikash.book_keeper.activities.sl_share_requests.ActivityShoppingListShareRequests
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.ShoppingListAdapter
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import com.dasbikash.book_keeper_repo.model.RequestApprovalStatus
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.utils.getDayCount
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showLongSnack
import kotlinx.android.synthetic.main.fragment_shopping_list.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FragmentShoppingList : FragmentTemplate(),WaitScreenOwner {
    private lateinit var viewModel: ViewModelShoppingList
    override fun registerWaitScreen(): ViewGroup = wait_screen

    private var filterMode = FilterMode.ALL
    private var sortMode = SortMode.dscDeadline

    private enum class FilterMode{ALL,SELF,IMPORTED,SHARED,EXPIRED,PENDING,DEADLINE_TODAY}
    private enum class SortMode{dscDeadline,ascDeadline,dscTitle,ascTitle}

    private val shoppingLists = mutableListOf<ShoppingList>()


    private val recentOnlineDocShareRequests = mutableListOf<OnlineSlShareReq>()

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
                    shoppingLists.clear()
                    shoppingLists.addAll(it)
                    updateDisplay()
                }
            }
        })

        viewModel.getRecentModifiedShareRequests().observe(this,object : Observer<List<OnlineSlShareReq>>{
            override fun onChanged(list: List<OnlineSlShareReq>?) {
                list?.let {
                    it.asSequence().forEach { processRecentOnlineDocShareRequest(it) }
                    if (it.isNotEmpty()) {
                        viewModel.setLastSharedRequestEntryUpdateTime()
                    }
                }
            }
        })
        viewModel.setLastSharedRequestEntryUpdateTime()

        sr_page_holder.setOnRefreshListener {
            syncShoppingListData()
        }

        chip_all.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    filterMode = FilterMode.ALL
                    updateDisplay()
                }
            }
        })

        chip_self_sl.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    filterMode = FilterMode.SELF
                    updateDisplay()
                }
            }
        })

        chip_imported_sl.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    filterMode = FilterMode.IMPORTED
                    updateDisplay()
                }
            }
        })

        chip_shared_sl.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    filterMode = FilterMode.SHARED
                    updateDisplay()
                }
            }
        })

        chip_expired_sl.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    filterMode = FilterMode.EXPIRED
                    updateDisplay()
                }
            }
        })

        chip_not_bought_sl.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    filterMode = FilterMode.PENDING
                    updateDisplay()
                }
            }
        })

        chip_deadline_today_sl.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    filterMode = FilterMode.DEADLINE_TODAY
                    updateDisplay()
                }
            }
        })

        chip_dsc_deadline.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    sortMode = SortMode.dscDeadline
                    updateDisplay()
                }
            }
        })

        chip_asc_deadline.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    sortMode = SortMode.ascDeadline
                    updateDisplay()
                }
            }
        })

        chip_dsc_title.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    sortMode = SortMode.dscTitle
                    updateDisplay()
                }
            }
        })

        chip_asc_title.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    sortMode = SortMode.ascTitle
                    updateDisplay()
                }
            }
        })
    }

    private fun updateDisplay(){
        runWithContext {
            lifecycleScope.launch {
                shoppingLists.let {
                    when(filterMode){
                        FilterMode.ALL -> shoppingLists.toList()
                        FilterMode.SELF -> shoppingLists.filter { it.userId == AuthRepo.getUserId() }
                        FilterMode.IMPORTED -> shoppingLists.filter { it.userId !=AuthRepo.getUserId() }
                        FilterMode.SHARED -> shoppingLists.filter { it.userId == AuthRepo.getUserId() && !it.partnerIds.isNullOrEmpty() }
                        FilterMode.PENDING -> shoppingLists.filter {!ShoppingListRepo.checkIfAllBought(context!!,it)}
                        FilterMode.EXPIRED -> shoppingLists.filter {it.deadLine !=null && !ShoppingListRepo.checkIfAllBought (context!!,it) && it.deadLine!!.time < System.currentTimeMillis()}
                        FilterMode.DEADLINE_TODAY -> shoppingLists.filter {it.deadLine !=null /*&& !ShoppingListRepo.checkIfAllBought (context!!,it)*/ && it.deadLine!!.getDayCount() == Date().getDayCount()}
                    }
                }.let {
                    when(sortMode){
                        SortMode.dscDeadline -> it.sortedByDescending { it.deadLine }
                        SortMode.ascDeadline -> it.sortedBy { it.deadLine }
                        SortMode.dscTitle -> it.sortedByDescending { it.title }
                        SortMode.ascTitle -> it.sortedBy { it.title }
                    }
                }.let {
                    shoppingListAdapter.submitList(it)
                }
            }
        }
    }

    private fun syncShoppingListData(){
        runWithContext {
            NetworkMonitor.runWithNetwork(it){
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        ShoppingListRepo.syncShoppingListData(it)
                        ShoppingListRepo.syncSlShareRequestData(it)
                    } catch (ex: Throwable) {
                        ex.printStackTrace()
                    }
                    runOnMainThread({
                        sr_page_holder?.isRefreshing = false
                    })
                }
            }.let {
                if (!it){
                    sr_page_holder?.isRefreshing = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        syncShoppingListData()
    }

    private fun processRecentOnlineDocShareRequest(onlineSlShareReq: OnlineSlShareReq){
        debugLog("processRecentOnlineDocShareRequest: $onlineSlShareReq")
        if (onlineSlShareReq.checkIfFromMe()) {
            debugLog("checkIfShoppingListShareRequest: $onlineSlShareReq")
            debugLog(onlineSlShareReq.approvalStatus.name)
            when (onlineSlShareReq.approvalStatus) {
                RequestApprovalStatus.PENDING -> {
                    if (!recentOnlineDocShareRequests.map { it.id }
                            .contains(onlineSlShareReq.id)) {
                        recentOnlineDocShareRequests.add(onlineSlShareReq)
                        setListenerForPendingOnlineSlShareRequest(onlineSlShareReq)
                    }
                }
                RequestApprovalStatus.APPROVED -> {
                    runWithContext {
                        lifecycleScope.launch {
                            val shoppingList:ShoppingList = ShoppingListRepo.findById(it,onlineSlShareReq.sharedDocumentId()!!)!!
                            AuthRepo.findUserById(it,onlineSlShareReq.ownerId!!)?.let {
                                showLongSnack(
                                    getString(R.string.shopping_list_share_req_approved,it.displayText()),
                                    getString(R.string.show_list_action_text),
                                    {launchDetailView(shoppingList)}
                                )
                            }
                        }
                    }
                }
                RequestApprovalStatus.DENIED -> {
                    runWithContext {
                        lifecycleScope.launch {
                            AuthRepo.findUserById(it,onlineSlShareReq.ownerId!!)?.let {
                                showLongSnack(getString(R.string.shopping_list_share_req_denied,it.displayText()))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun setListenerForPendingOnlineSlShareRequest(onlineSlShareReq: OnlineSlShareReq) {
        debugLog("setListenerForPendingOnlineSlShareRequest: ${onlineSlShareReq}")
        runWithActivity {
            ShoppingListRepo.setListenerForPendingOnlineSlShareRequest(it,it as AppCompatActivity,onlineSlShareReq)
        }
    }

    private fun showListAddDialog() {
        runWithContext {
            startActivity(ActivityShoppingList.getCreateIntent(it))
        }
    }
    override fun getPageTitle(context: Context):String? = context.getString(R.string.shopping_list_title)

    override fun getOptionsMenu(context: Context): MenuView? {
        return MenuView().apply {
            add(
                MenuViewItem(
                    text = context.getString(R.string.shopping_list_import_title),
                    task = {
                        activity?.let {
                            (it as Activity).startActivity(
                                ActivityShoppingListImport::class.java
                            )
                        }
                    }
                )
            )
            /*add(
                MenuViewItem(
                    text = context.getString(R.string.add_shopping_list),
                    task = { showListAddDialog() }
                )
            )*/
            add(
                MenuViewItem(
                    text = context.getString(R.string.color_code),
                    task = {showBgColorCodeScreen()}
                )
            )
            add(
                MenuViewItem(
                    text = context.getString(R.string.pending_sl_share_requests),
                    task = {
                        runWithActivity {
                            NetworkMonitor.runWithNetwork(it) {
                                it.startActivity(
                                    ActivityShoppingListShareRequests::class.java
                                )
                            }
                        }
                    }
                )
            )
        }
    }

    private fun showBgColorCodeScreen() {
        runWithContext {
            val view = LayoutInflater.from(it).inflate(R.layout.view_sl_bg_color_code,null,false)
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                view = view,
                positiveButtonText = "",
                negetiveButtonText = ""
            ))
        }
    }
}