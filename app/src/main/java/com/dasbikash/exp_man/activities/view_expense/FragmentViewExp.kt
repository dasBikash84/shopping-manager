package com.dasbikash.exp_man.activities.view_expense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.edit_expense.ActivityEditExpense
import com.dasbikash.exp_man_repo.ExpenseRepo
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick
import kotlinx.android.synthetic.main.fragment_view_exp.*
import kotlinx.coroutines.launch

class FragmentViewExp : Fragment(), WaitScreenOwner {

    private lateinit var expenseEntry: ExpenseEntry

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_view_exp, container, false)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            expenseEntry = getExpenseEntry()!!
            debugLog(expenseEntry)
            attachOptionMenuTasks()
        }
    }

    private suspend fun getExpenseEntry():ExpenseEntry?{
        arguments?.getString(ARG_EXP_ENTRY_ID)?.let {
            val id = it
            return ExpenseRepo.getExpenseEntryById(context!!,id)
        }
        return null
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen

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
                        showWaitScreen()
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