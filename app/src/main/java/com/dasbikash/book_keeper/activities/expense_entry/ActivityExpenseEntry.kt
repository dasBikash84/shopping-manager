package com.dasbikash.book_keeper.activities.expense_entry

import android.content.Context
import android.content.Intent
import com.dasbikash.book_keeper.activities.expense_entry.add_exp.FragmentExpAddEdit
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.model.ExpenseEntry

class ActivityExpenseEntry : ActivityTemplate(){

    private fun getExpenseId() = intent.getStringExtra(EXTRA_EXPENSE_ID)!!

    override fun registerDefaultFragment(): FragmentTemplate {
        return if(isEditIntent()){
                    FragmentExpAddEdit.getEditInstance(getExpenseId())
                }else if(isAddIntent()) {
                    FragmentExpAddEdit()
                }else if(isShoppingListItemSaveIntent()) {
                    FragmentExpAddEdit.getShoppingListItemSaveInstance(getShoppingListItemId())
                }else {
                    FragmentViewExp.getInstance(getExpenseId())
                }
    }

    private fun getShoppingListItemId(): String = intent.getStringExtra(EXTRA_SHOPPING_LIST_ITEM_ID)!!

    private fun isShoppingListItemSaveIntent(): Boolean {
        return intent.hasExtra(EXTRA_SHOPPING_LIST_ITEM_ID)
    }

    private fun isEditIntent():Boolean{
        return intent.hasExtra(EXTRA_EDIT_MODE)
    }
    private fun isAddIntent():Boolean{
        return intent.hasExtra(EXTRA_ADD_MODE)
    }

    companion object{
        private const val EXTRA_ADD_MODE = "com.dasbikash.exp_man.activities.view_expense.ActivityExpenseEntry.EXTRA_ADD_MODE"
        private const val EXTRA_EDIT_MODE = "com.dasbikash.exp_man.activities.view_expense.ActivityExpenseEntry.EXTRA_EDIT_MODE"
        private const val EXTRA_EXPENSE_ID = "com.dasbikash.exp_man.activities.view_expense.ActivityExpenseEntry.EXTRA_EXPENSE_ID"
        private const val EXTRA_SHOPPING_LIST_ITEM_ID = "com.dasbikash.exp_man.activities.view_expense.ActivityExpenseEntry.EXTRA_SHOPPING_LIST_ITEM_ID"

        fun getViewIntent(context: Context, expenseEntry: ExpenseEntry):Intent{
            val intent = Intent(context.applicationContext,ActivityExpenseEntry::class.java)
            intent.putExtra(EXTRA_EXPENSE_ID,expenseEntry.id)
            return intent
        }

        fun getEditIntent(context: Context, expenseEntry: ExpenseEntry):Intent{
            val intent = Intent(context.applicationContext,ActivityExpenseEntry::class.java)
            intent.putExtra(EXTRA_EXPENSE_ID,expenseEntry.id)
            intent.putExtra(EXTRA_EDIT_MODE,EXTRA_EDIT_MODE)
            return intent
        }

        fun getAddIntent(context: Context):Intent{
            val intent = Intent(context.applicationContext,ActivityExpenseEntry::class.java)
            intent.putExtra(EXTRA_ADD_MODE,EXTRA_ADD_MODE)
            return intent
        }

        fun getShoppingListItemSaveIntent(context: Context,shoppingListItemId:String):Intent{
            val intent = Intent(context.applicationContext,ActivityExpenseEntry::class.java)
            intent.putExtra(EXTRA_SHOPPING_LIST_ITEM_ID,shoppingListItemId)
            return intent
        }
    }
}
