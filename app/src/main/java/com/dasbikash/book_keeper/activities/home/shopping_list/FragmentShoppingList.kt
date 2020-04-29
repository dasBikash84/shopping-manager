package com.dasbikash.book_keeper.activities.home.shopping_list

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
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
import com.dasbikash.book_keeper_repo.DataSyncService
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.utils.getDayCount
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
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

        runWithContext {
            lifecycleScope.launchWhenCreated {
                if (isShoppingListRequestModeInstance()) {
                    checkPendingSlRequests()
                } else if (isNewShoppingListInstance()) {
                    getNewShoppingListId()?.let {
                        ShoppingListRepo.findById(context!!,it)?.let {
                            launchDetailView(it)
                        }
                    }
                }
            }
        }
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
                        FilterMode.EXPIRED -> shoppingLists.filter {it.deadLine !=null && !ShoppingListRepo.checkIfAllBought (context!!,it) && it.deadLine!!.toDate().time < System.currentTimeMillis()}
                        FilterMode.DEADLINE_TODAY -> shoppingLists.filter {it.deadLine !=null /*&& !ShoppingListRepo.checkIfAllBought (context!!,it)*/ && it.deadLine!!.toDate().getDayCount() == Date().getDayCount()}
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
                        DataSyncService.syncShoppingListData(it)
                        DataSyncService.syncSlShareRequestData(it)
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

    private fun showListAddDialog() {
        runWithContext {
            startActivity(ActivityShoppingList.getCreateIntent(it))
        }
    }
    override fun getPageTitle(context: Context):String? = context.getString(R.string.shopping_list_title)

    override suspend fun getOptionsMenu(context: Context): MenuView? {
        return MenuView().apply {
            add(
                MenuViewItem(
                    text = context.getString(R.string.pending_sl_share_requests),
                    task = {checkPendingSlRequests()}
                )
            )
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
        }
    }

    private fun checkPendingSlRequests() {
        runWithActivity {
            NetworkMonitor.runWithNetwork(it) {
                it.startActivity(
                    ActivityShoppingListShareRequests::class.java
                )
            }
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

    private fun isShoppingListRequestModeInstance() = arguments?.containsKey(ARG_SHOPPING_LIST_REQUEST_MODE) == true
    private fun isNewShoppingListInstance() = arguments?.containsKey(ARG_NEW_SHOPPING_LIST_MODE) == true
    private fun getNewShoppingListId() = arguments?.getString(ARG_NEW_SHOPPING_LIST_ID)

    companion object{

        private const val ARG_SHOPPING_LIST_REQUEST_MODE =
            "com.dasbikash.book_keeper.activities.home.account.FragmentShoppingList.ARG_SHOPPING_LIST_REQUEST_MODE"

        private const val ARG_NEW_SHOPPING_LIST_MODE =
            "com.dasbikash.book_keeper.activities.home.account.FragmentShoppingList.ARG_NEW_SHOPPING_LIST_MODE"

        private const val ARG_NEW_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.home.account.FragmentShoppingList.ARG_NEW_SHOPPING_LIST_ID"

        fun getShoppingListRequestModeInstance():FragmentShoppingList{
            val arg = Bundle()
            arg.putSerializable(ARG_SHOPPING_LIST_REQUEST_MODE,ARG_SHOPPING_LIST_REQUEST_MODE)
            val fragment = FragmentShoppingList()
            fragment.arguments = arg
            return fragment
        }

        fun getNewShoppingListInstance(shoppingListId:String?=null):FragmentShoppingList{
            val arg = Bundle()
            arg.putSerializable(ARG_NEW_SHOPPING_LIST_MODE,ARG_NEW_SHOPPING_LIST_MODE)
            arg.putSerializable(ARG_NEW_SHOPPING_LIST_ID,shoppingListId)
            val fragment = FragmentShoppingList()
            fragment.arguments = arg
            return fragment
        }
    }
}