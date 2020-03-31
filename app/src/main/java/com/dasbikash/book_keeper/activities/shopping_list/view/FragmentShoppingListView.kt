package com.dasbikash.book_keeper.activities.shopping_list.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.show

import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList
import com.dasbikash.book_keeper.activities.shopping_list.FragmentShoppingListDetails
import com.dasbikash.book_keeper.activities.shopping_list.edit.FragmentShoppingListAddEdit
import com.dasbikash.book_keeper.activities.sl_item.ActivityShoppingListItem
import com.dasbikash.book_keeper.rv_helpers.ShoppingListItemAdapter
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_shopping_list_view.*
import kotlinx.coroutines.launch
import java.lang.IllegalStateException


class FragmentShoppingListView : FragmentShoppingListDetails() {

    private lateinit var viewModel: ViewModelShoppingListView
    private val shoppingListItemAdapter = ShoppingListItemAdapter({launchShoppingListItemDetailView(it)},{editTask(it)},{deleteTask(it)},{closeTask(it)})

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
        TODO("Not yet implemented")
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
        rv_shopping_list_items.adapter = shoppingListItemAdapter

        viewModel.getShoppingList().observe(this,object : Observer<ShoppingList>{
            override fun onChanged(shoppingList: ShoppingList?) {
                shoppingList?.let {
                    debugLog(it)
                    refreshView(it)
                }
            }
        })

        viewModel.setShoppingListId(getShoppingListId())

        btn_add_shopping_item.setOnClickListener {
            runWithActivity {
                it.startActivity(ActivityShoppingListItem.getCreateIntent(it,getShoppingListId()))
            }
        }
    }

    private fun refreshView(shoppingList: ShoppingList) {
        runWithContext {
            shoppingList.apply {
                (activity as ActivityShoppingList?)?.setPageTitle(title!!)
                if (deadLine != null) {
                    tv_sl_deadline.text = DateUtils.getTimeString(
                        shoppingList.deadLine!!,
                        it.getString(R.string.exp_entry_time_format)
                    ).let {
                            return@let when (checkIfEnglishLanguageSelected()) {
                                true -> it
                                false -> TranslatorUtils.englishToBanglaDateString(it)
                            }
                        }
                    sl_deadline_text_holder.show()
                } else {
                    sl_deadline_text_holder.hide()
                }
                if (getCountDownTime() !=null){
                    tv_sl_count_down.text = it.getString(R.string.sl_count_down,getCountDownTime()!!/DateUtils.MINUTE_IN_MS)
                    ShoppingList.Companion.ReminderInterval.values().find {
                        it.intervalMs==getReminderInterval()
                    }!!.let {
                        tv_sl_reminder_interval.text = getString(R.string.sl_remind_interval,if (checkIfEnglishLanguageSelected()) {it.text} else {it.textBangla})
                    }
                    sl_remainder_block.show()
                }else{
                    sl_remainder_block.hide()
                }
                lifecycleScope.launch {
                    (ShoppingListRepo.getShoppingListItems(it, this@apply) ?: emptyList()).let {
                        shoppingListItemAdapter.submitList(it)
                    }
                }
            }
        }
    }

    private fun getShoppingListId():String = arguments!!.getString(ARG_SHOPPING_LIST_ID)!!

    override fun getOptionsMenu(context: Context): MenuView? {
        return MenuView().apply {
            add(
                MenuViewItem(
                    text = context.getString(R.string.edit),
                    task = {
                        (activity as ActivityShoppingList).addFragmentClearingBackStack(
                            FragmentShoppingListAddEdit.getEditInstance(
                                getShoppingListId()
                            )
                        )
                    }
                )
            )
            add(
                MenuViewItem(
                    text = "Share",
                    task = { showShortSnack("Sharing not implemented yet!") }
                )
            )
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
