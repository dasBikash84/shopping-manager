package com.dasbikash.book_keeper.activities.shopping_list.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.pop_up_message.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.*
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.expense_entry.ActivityExpenseEntry
import com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList
import com.dasbikash.book_keeper.activities.shopping_list.ShoppingListUtils
import com.dasbikash.book_keeper.activities.shopping_list.edit.FragmentShoppingListAddEdit
import com.dasbikash.book_keeper.activities.shopping_list.edit.FragmentShoppingListAddEdit.Companion.reminderUnitPeriods
import com.dasbikash.book_keeper.activities.sl_item.ActivityShoppingListItem
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.application.BookKeeperApp
import com.dasbikash.book_keeper.rv_helpers.ShoppingListItemAdapter
import com.dasbikash.book_keeper.utils.GetCalculatorMenuItem
import com.dasbikash.book_keeper.utils.getCurrencyStringWithSymbol
import com.dasbikash.book_keeper.utils.toTranslatedString
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import kotlinx.android.synthetic.main.fragment_shopping_list_view.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class FragmentShoppingListView : FragmentTemplate(),WaitScreenOwner {

    override fun registerWaitScreen(): ViewGroup = wait_screen

    private lateinit var viewModel: ViewModelShoppingListView
    private  lateinit var shoppingListItemAdapter : ShoppingListItemAdapter

    private fun editTask(shoppingListItem: ShoppingListItem) {
        runWithActivity {
            it.startActivity(ActivityShoppingListItem.getEditIntent(it,shoppingListItem.id))
        }
    }

    private fun deleteTask(shoppingListItem: ShoppingListItem) {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.confirm_delete_prompt),
                doOnPositivePress = {
                    lifecycleScope.launch {
                        ShoppingListRepo.delete(it,shoppingListItem)
                    }
                }
            ))
        }
    }

    private fun closeTask(shoppingListItem: ShoppingListItem) {
        runWithContext {
            startActivity(
                ActivityExpenseEntry
                    .getShoppingListItemSaveIntent(
                        it,shoppingListItem.id)
            )
        }
    }

    private fun launchShoppingListItemDetailView(shoppingListItem: ShoppingListItem) {
        debugLog(shoppingListItem)
        runWithActivity {
            it.startActivity(ActivityShoppingListItem.getViewIntent(it,shoppingListItem.id))
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            context as ActivityShoppingList
        }catch (ex:Throwable){
            throw IllegalStateException("Should only be attached to shopping list activity!!")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewModelShoppingListView::class.java)

        viewModel.setShoppingListId(getShoppingListId())

        viewModel.getShoppingList().observe(this,object : Observer<ShoppingList>{
            override fun onChanged(shoppingList: ShoppingList?) {
                shoppingList?.apply {
                    debugLog(this)
                    if (!::shoppingListItemAdapter.isInitialized) {
                        shoppingListItemAdapter =
                            ShoppingListItemAdapter(
                                this,
                                { launchShoppingListItemDetailView(it) },
                                { closeTask(it) },
                                { editTask(it) },
                                { deleteTask(it) }
                            )
                        rv_shopping_list_items.adapter = shoppingListItemAdapter
                    }
                    refreshView(this)
                }
            }
        })

        btn_add_shopping_item.setOnClickListener {
            launchAddItemScreen()
        }

        sr_page_holder.setOnRefreshListener {syncShoppingListData()}
    }

    private fun syncShoppingListData(){
        runWithContext {
            NetworkMonitor.runWithNetwork(it) {
                lifecycleScope.launch(Dispatchers.IO) {
                    ShoppingListRepo.syncShoppingListById(getShoppingListId(),it)
                    runOnMainThread({sr_page_holder?.isRefreshing = false})
                }
            }.let {
                if (!it){
                    sr_page_holder?.isRefreshing = false
                }
            }
        }
    }

    private fun launchAddItemScreen() {
        runWithActivity {
            it.startActivity(ActivityShoppingListItem.getCreateIntent(it, getShoppingListId()))
        }
    }

    private fun refreshView(shoppingList: ShoppingList) {
        runWithContext {
            shoppingList.apply {
                debugLog("spppp: $this")
                (activity as ActivityShoppingList?)?.setTitle(title!!)
                lifecycleScope.launch {
                    val (minExp,maxExp) = ShoppingList.calculateExpenseRange(it,shoppingList)
                    tv_sl_price_range.text = getString(R.string.sl_price_range,minExp.getCurrencyStringWithSymbol(it),maxExp.getCurrencyStringWithSymbol(it))
                }
                if (deadLine != null) {
                    tv_sl_deadline.text = shoppingList.deadLine!!.toDate().toTranslatedString(it)
                    sl_deadline_text_holder.show()
                } else {
                    sl_deadline_text_holder.hide()
                }
                if (getCountDownTime() !=null){

                    shoppingList.getCountDownTime()!!.apply {
                        val remUnitIndex = if(this > 0L && this % reminderUnitPeriods[1] == 0L){
                            1
                        }else{
                            0
                        }
                        tv_sl_count_down.text =
                            it.getString(
                                R.string.sl_count_down_text,
                                (this/reminderUnitPeriods[remUnitIndex]).toInt(),
                                it.resources.getStringArray(R.array.reminder_time_units).toList().get(remUnitIndex)
                            )
                    }
                    ShoppingList.Companion.ReminderInterval.values().find {
                        it.intervalMs==getReminderInterval()
                    }!!.let {
                        tv_sl_reminder_interval.text = getString(R.string.sl_remind_interval_text,it.getText(BookKeeperApp.getLanguageSetting(context!!)))
                    }
                    sl_remainder_block.show()
                }else{
                    sl_remainder_block.hide()
                }
                lifecycleScope.launch {
                    (ShoppingListRepo.getShoppingListItems(it, this@apply) ?: emptyList()).let {
                        shoppingListItemAdapter.submitList(it.toMutableList().sortedByDescending { it.modified }.sortedBy { !it.expenseEntryId.isNullOrBlank() })
                        if (it.isEmpty()){
                            holder_rv_shopping_list_items.hide()
                        }else{
                            holder_rv_shopping_list_items.show()
                        }
                    }
                }
                debugLog("note: $note")
                note.let {
                    if (it.isNullOrBlank()){
                        debugLog("it.isNullOrBlank()")
                        sl_note_holder.hide()
                    }else{
                        debugLog("it.trim(): ${it.trim()}")
                        tv_sl_note.text = it.trim()
                        sl_note_holder.show()
                    }
                }
            }
        }
    }

    private fun getShoppingListId():String = arguments!!.getString(ARG_SHOPPING_LIST_ID)!!

    override suspend fun getOptionsMenu(context: Context): MenuView? {
        return MenuView().apply {
            getEditOptionsMenuItem(context)?.let { add(it) }
            getDeleteOptionsMenuItem(context)?.let { add(it)}
            ShoppingListUtils.getShareOptionsMenu(context,getShoppingListId()).let {
                this.addAll(it)
            }
            add(GetCalculatorMenuItem(context))
        }
    }

    private suspend fun getEditOptionsMenuItem(context: Context): MenuViewItem? {
        return ShoppingListRepo.findById(context,getShoppingListId())!!.let {
            if (it.userId == AuthRepo.getUserId()){
                return@let MenuViewItem(
                    text = context.getString(R.string.edit),
                    task = {
                        (activity as ActivityShoppingList).addFragmentClearingBackStack(
                            FragmentShoppingListAddEdit.getEditInstance(
                                getShoppingListId()
                            )
                        )
                    }
                )
            }else{
                return@let null
            }
        }
    }

    private suspend fun getDeleteOptionsMenuItem(context: Context): MenuViewItem? {
        return ShoppingListRepo.findById(context,getShoppingListId())!!.let {
            if (it.userId == AuthRepo.getUserId()){
                return@let MenuViewItem(
                    text = context.getString(R.string.delete),
                    task = { deleteTask() }
                )
            }else{
                return@let null
            }
        }
    }

    private fun deleteTask(){
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = getString(R.string.confirm_delete_prompt),
                doOnPositivePress = {
                    lifecycleScope.launch {
                        ShoppingListRepo.delete(it,viewModel.getShoppingList().value!!)
                        activity?.finish()
                    }
                }
            ))
        }
    }

    companion object{

        private const val ARG_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.shopping_list.view.FragmentShoppingListView.ARG_SHOPPING_LIST_ID"

        fun getInstance(shoppingListId: String): FragmentShoppingListView {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ID,shoppingListId)
            val fragment =
                FragmentShoppingListView()
            fragment.arguments = arg
            return fragment
        }
    }

}
