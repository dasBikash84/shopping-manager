package com.dasbikash.book_keeper.activities.view_expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.edit_expense.ActivityEditExpense
import com.dasbikash.book_keeper.rv_helpers.ExpenseItemAdapter
import com.dasbikash.book_keeper.utils.DateTranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper.utils.optimizedString
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick
import kotlinx.android.synthetic.main.fragment_view_exp.*
import kotlinx.coroutines.launch

class FragmentViewExp : Fragment() {

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
            expenseEntry = getExpenseEntry()!!
            debugLog(expenseEntry)
            attachOptionMenuTasks()
            refreshView()
        }
    }

    private fun refreshView() {
        checkIfEnglishLanguageSelected().apply {
            tv_category_title.text = if (this) {expenseEntry.expenseCategory!!.name} else {expenseEntry.expenseCategory!!.nameBangla}
            tv_entry_time.text = DateUtils.getTimeString(expenseEntry.time!!, getString(R.string.exp_entry_time_format))
                                        .let {
                                            return@let when (this) {
                                                true -> it
                                                false -> DateTranslatorUtils.englishToBanglaDateString(it)
                                            }
                                        }
            tv_exp_details.text = expenseEntry.details
            tv_vat_ait.text = expenseEntry.taxVat.toString()
            tv_total_expense.text = expenseEntry.totalExpense?.optimizedString(2)

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

    private suspend fun getExpenseEntry():ExpenseEntry?{
        arguments?.getString(ARG_EXP_ENTRY_ID)?.let {
            val id = it
            return ExpenseRepo.getExpenseEntryById(context!!,id)
        }
        return null
    }

    private fun attachOptionMenuTasks(){

        val menuViewItems = listOf<MenuViewItem>(
            MenuViewItem(
                text = getString(R.string.edit),
                task = { editTask() }
            ),
            MenuViewItem(
                text = getString(R.string.delete),
                task = { deleteTask() }
            )
        )
        val menuView = MenuView()
        menuView.addAll(menuViewItems)
        btn_options.attachMenuViewForClick(menuView)
    }

    private fun editTask(){
        runWithActivity {
            it.startActivity(ActivityEditExpense.getIntent(it, expenseEntry))
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
        private const val MISCELLANEOUS_TEXT = "Miscellaneous"

        private const val ARG_EXP_ENTRY_ID =
            "com.dasbikash.exp_man.activities.home.add_exp.FragmentAddExp.ARG_EXP_ENTRY_ID"

        fun getEditInstance(expenseEntryId: String):FragmentViewExp{
            val fragment = FragmentViewExp()
            val bundle = Bundle()
            bundle.putString(ARG_EXP_ENTRY_ID,expenseEntryId)
            fragment.arguments = bundle
            return fragment
        }
    }
}