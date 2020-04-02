package com.dasbikash.book_keeper.activities.shopping_list

import android.content.Context
import android.content.Intent
import com.dasbikash.book_keeper.activities.shopping_list.edit.FragmentShoppingListAddEdit
import com.dasbikash.book_keeper.activities.shopping_list.view.FragmentShoppingListView
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate

class ActivityShoppingList : ActivityTemplate() {

    override fun getDefaultFragment(): FragmentTemplate = getInitFragment()

    override fun registerDefaultFragment(): FragmentTemplate =
        getInitFragment()

    private fun getInitFragment():FragmentTemplate{
        return if (isCreateIntent(intent)){
            getCreateFragment()
        }else if (isEditIntent(intent)){
            getEditFragment(getShoppingListId(intent))
        }else{
            getViewFragment(getShoppingListId(intent))
        }
    }

    private fun getViewFragment(shoppingListId: String): FragmentShoppingListView =
        FragmentShoppingListView.getInstance(shoppingListId)

    private fun getEditFragment(shoppingListId: String): FragmentShoppingListAddEdit =
        FragmentShoppingListAddEdit.getEditInstance(shoppingListId)

    private fun getCreateFragment(): FragmentShoppingListAddEdit =
        FragmentShoppingListAddEdit.getCreateInstance()

    fun setTitle(titleText:String){
        super.setPageTitle(titleText)
    }

    companion object{

        private const val EXTRA_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList.EXTRA_SHOPPING_LIST_ID"
        private const val EXTRA_SHOPPING_LIST_EDIT_MODE =
            "com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList.EXTRA_SHOPPING_LIST_EDIT_MODE"
        private const val EXTRA_SHOPPING_LIST_CREATE_MODE =
            "com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList.EXTRA_SHOPPING_LIST_CREATE_MODE"

        private fun isEditIntent(intent: Intent):Boolean =
            intent.hasExtra(EXTRA_SHOPPING_LIST_EDIT_MODE)

        private fun isCreateIntent(intent: Intent):Boolean =
            intent.hasExtra(EXTRA_SHOPPING_LIST_CREATE_MODE)

        private fun getShoppingListId(intent: Intent): String =
            intent.getStringExtra(EXTRA_SHOPPING_LIST_ID)!!

        private fun getIntent(
            context: Context,
            shoppingListId: String
        ): Intent {
            val intent = Intent(
                context,
                ActivityShoppingList::class.java
            )
            intent.putExtra(EXTRA_SHOPPING_LIST_ID, shoppingListId)
            return intent
        }

        fun getViewIntent(context: Context,shoppingListId:String):Intent{
            return getIntent(context, shoppingListId)
        }

        fun getEditIntent(context: Context,shoppingListId:String):Intent{
            val intent = getIntent(context, shoppingListId)
            intent.putExtra(
                EXTRA_SHOPPING_LIST_EDIT_MODE,
                EXTRA_SHOPPING_LIST_EDIT_MODE
            )
            return intent
        }

        fun getCreateIntent(context: Context):Intent{
            val intent = Intent(
                context,
                ActivityShoppingList::class.java
            )
            intent.putExtra(
                EXTRA_SHOPPING_LIST_CREATE_MODE,
                EXTRA_SHOPPING_LIST_CREATE_MODE
            )
            return intent
        }
    }
}
