package com.dasbikash.book_keeper.activities.edit_expense

import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.add_exp.FragmentAddExp
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.super_activity.SingleFragmentSuperActivity
import kotlinx.android.synthetic.main.view_wait_screen.*

class ActivityEditExpense : SingleFragmentSuperActivity(),WaitScreenOwner {

    override fun getDefaultFragment(): Fragment = FragmentAddExp.getEditInstance(intent.getStringExtra(EXTRA_EXPENSE_ID)!!)

    override fun getLayoutID(): Int = R.layout.activity_edit_expense

    override fun getLoneFrameId(): Int = R.id.exp_edit_frame

    override fun registerWaitScreen(): ViewGroup = wait_screen

    override fun onBackPressed() {
        DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
            message = getString(R.string.discard_and_exit_prompt),
            doOnPositivePress = {
                super.onBackPressed()
            }
        ))
    }

    companion object{
        private const val EXTRA_EXPENSE_ID = "com.dasbikash.exp_man.activities.edit_expense.ActivityEditExpense.EXTRA_EXPENSE_ID"

        fun getIntent(context: Context,expenseEntry: ExpenseEntry):Intent{
            val intent = Intent(context.applicationContext,ActivityEditExpense::class.java)
            intent.putExtra(EXTRA_EXPENSE_ID,expenseEntry.id)
            return intent
        }
    }
}
