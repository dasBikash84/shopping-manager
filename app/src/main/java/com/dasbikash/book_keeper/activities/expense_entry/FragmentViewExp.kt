package com.dasbikash.book_keeper.activities.expense_entry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.ExpenseItemAdapter
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper.utils.getCurrencyString
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import kotlinx.android.synthetic.main.fragment_view_exp.*
import kotlinx.coroutines.launch

class FragmentViewExp : FragmentTemplate() {

    private lateinit var expenseEntry: ExpenseEntry
    private val expenseItemAdapter = ExpenseItemAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_exp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_expense_items.adapter = expenseItemAdapter
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            expenseEntry = getExpenseEntry()
            debugLog(expenseEntry)
            refreshView()
        }
    }

    private fun refreshView() {
        checkIfEnglishLanguageSelected().apply {
            tv_category_title.text = resources.getStringArray(R.array.expense_categories).get(expenseEntry.categoryId)
            tv_entry_time.text = DateUtils.getTimeString(expenseEntry.time!!.toDate(), getString(R.string.exp_entry_time_format))
                                        .let {
                                            return@let when (this) {
                                                true -> it
                                                false -> TranslatorUtils.englishToBanglaDateString(it)
                                            }
                                        }
            tv_exp_details.text = expenseEntry.details
            tv_vat_ait.text = expenseEntry.taxVat.getCurrencyString()
            tv_total_expense.text = expenseEntry.totalExpense?.getCurrencyString()

            (expenseEntry.expenseItems ?: emptyList()).let {
                if (it.isEmpty()){
                    expense_item_list_holder.hide()
                }else{
                    expenseItemAdapter.submitList(it)
                    expense_item_list_holder.show()
                }
            }
        }
    }

    private suspend fun getExpenseEntry():ExpenseEntry{
        return arguments!!.getString(ARG_EXP_ENTRY_ID)!!.let {
            val id = it
            return@let ExpenseRepo.getExpenseEntryById(context!!,id)!!
        }
    }

    override fun getOptionsMenu(context: Context):MenuView?{

        val menuViewItems = listOf<MenuViewItem>(
            MenuViewItem(
                text = context.getString(R.string.edit),
                task = { editTask() }
            ),
            MenuViewItem(
                text = context.getString(R.string.delete),
                task = { deleteTask() }
            )
        )
        val menuView = MenuView()
        menuView.addAll(menuViewItems)
        return menuView
    }

    override fun getPageTitle(context: Context):String?{
        return context.getString(R.string.view_expense_title)
    }

    private fun editTask(){
        runWithActivity {
            it.startActivity(ActivityExpenseEntry.getEditIntent(it, expenseEntry))
        }
    }

    private fun deleteTask(){
        runWithActivity {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = getString(R.string.confirm_delete_prompt),
                doOnPositivePress = {
                    lifecycleScope.launch {
                        ExpenseRepo.delete(it, expenseEntry)
                        it.finish()
                    }
                }
            ))
        }
    }

    companion object {

        private const val ARG_EXP_ENTRY_ID =
            "com.dasbikash.exp_man.activities.home.add_exp.FragmentViewExp.ARG_EXP_ENTRY_ID"

        fun getInstance(expenseEntryId: String):FragmentViewExp{
            val fragment = FragmentViewExp()
            val bundle = Bundle()
            bundle.putString(ARG_EXP_ENTRY_ID,expenseEntryId)
            fragment.arguments = bundle
            return fragment
        }
    }
}