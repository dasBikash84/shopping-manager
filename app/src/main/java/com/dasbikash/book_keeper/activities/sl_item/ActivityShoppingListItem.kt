package com.dasbikash.book_keeper.activities.sl_item

import android.content.Context
import android.content.Intent
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate

class ActivityShoppingListItem: ActivityTemplate() {

    override fun registerDefaultFragment(): FragmentTemplate {
        return if (intent.hasExtra(EXTRA_SHOPPING_LIST_ITEM_ID)){
            if (intent.hasExtra(EXTRA_EDIT_MODE)){
                FragmentShoppingListItemAddEdit.getInstanceForEdit(getShoppingListItemId())
            }else{
                FragmentShoppingListItemView.getInstance(getShoppingListItemId())
            }
        }else{
            FragmentShoppingListItemAddEdit.getInstance(getShoppingListId())
        }
    }

    private fun getShoppingListItemId() = intent.getStringExtra(EXTRA_SHOPPING_LIST_ITEM_ID)!!
    private fun getShoppingListId() = intent.getStringExtra(EXTRA_SHOPPING_LIST_ID)!!

    fun setTitle(title:String){
        super.setPageTitle(title)
    }

    companion object {

        private const val EXTRA_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.sl_item.ActivityShoppingListItem.EXTRA_SHOPPING_LIST_ID"

        private const val EXTRA_SHOPPING_LIST_ITEM_ID =
            "com.dasbikash.book_keeper.activities.sl_item.ActivityShoppingListItem.SHOPPING_LIST_ITEM_ID"
        private const val EXTRA_EDIT_MODE =
            "com.dasbikash.book_keeper.activities.sl_item.ActivityShoppingListItem.EXTRA_EDIT_MODE"
        private const val EXTRA_VIEW_MODE =
            "com.dasbikash.book_keeper.activities.sl_item.ActivityShoppingListItem.EXTRA_VIEW_MODE"

        fun getCreateIntent(context: Context,shoppingListId: String): Intent {
            val intent = Intent(context.applicationContext, ActivityShoppingListItem::class.java)
            intent.putExtra(EXTRA_SHOPPING_LIST_ID,shoppingListId)
            return intent
        }

        fun getViewIntent(context: Context,shoppingListItemId: String): Intent {
            val intent = Intent(context.applicationContext, ActivityShoppingListItem::class.java)
            intent.putExtra(EXTRA_SHOPPING_LIST_ITEM_ID,shoppingListItemId)
            intent.putExtra(EXTRA_VIEW_MODE,EXTRA_VIEW_MODE)
            return intent
        }

        fun getEditIntent(context: Context,shoppingListItemId: String): Intent {
            val intent = Intent(context.applicationContext, ActivityShoppingListItem::class.java)
            intent.putExtra(EXTRA_SHOPPING_LIST_ITEM_ID,shoppingListItemId)
            intent.putExtra(EXTRA_EDIT_MODE,EXTRA_EDIT_MODE)
            return intent
        }
    }
}