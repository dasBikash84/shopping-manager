package com.dasbikash.book_keeper.activities.view_expense

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.super_activity.SingleFragmentSuperActivity

class ActivityViewExpense : SingleFragmentSuperActivity(){

    override fun getDefaultFragment(): Fragment = FragmentViewExp.getEditInstance(getExpenseId())
    override fun getLayoutID(): Int = R.layout.activity_view_expense
    override fun getLoneFrameId(): Int = R.id.exp_view_frame

    private fun getExpenseId() = intent.getStringExtra(EXTRA_EXPENSE_ID)!!

    companion object{

        private const val EXTRA_EXPENSE_ID = "com.dasbikash.exp_man.activities.view_expense.ActivityViewExpense.EXTRA_EXPENSE_ID"

        fun getIntent(context: Context,expenseEntry: ExpenseEntry):Intent{
            val intent = Intent(context.applicationContext,ActivityViewExpense::class.java)
            intent.putExtra(EXTRA_EXPENSE_ID,expenseEntry.id)
            return intent
        }
    }
}
