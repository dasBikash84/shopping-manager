package com.dasbikash.book_keeper.activities.shopping_list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.dasbikash.book_keeper.R
import com.dasbikash.super_activity.SingleFragmentSuperActivity
import kotlinx.android.synthetic.main.activity_shopping_list.*

class ActivityShoppingList : SingleFragmentSuperActivity() {

    override fun getDefaultFragment(): Fragment = getInitFragment()
    override fun getLayoutID(): Int = R.layout.activity_shopping_list
    override fun getLoneFrameId(): Int = R.id.shopping_list_frame

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)
    }

    private fun getInitFragment():Fragment{
        return when(isEditIntent(intent)){
            true -> getEditFragment(getShoppingListId(intent))
            false -> getViewFragment(getShoppingListId(intent))
        }
    }

    private fun getViewFragment(shoppingListId: String): FragmentShoppingListView =
        FragmentShoppingListView.getInstance(shoppingListId)

    private fun getEditFragment(shoppingListId: String): FragmentShoppingListEdit =
        FragmentShoppingListEdit.getInstance(shoppingListId)

    fun setTitle(titleText:String) = page_title.setText(titleText)

    companion object{

        private const val EXTRA_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList.EXTRA_SHOPPING_LIST_ID"
        private const val EXTRA_SHOPPING_LIST_EDIT_MODE =
            "com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList.EXTRA_SHOPPING_LIST_EDIT_MODE"

        private fun isEditIntent(intent: Intent):Boolean =
            intent.hasExtra(EXTRA_SHOPPING_LIST_EDIT_MODE)

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
    }
}
