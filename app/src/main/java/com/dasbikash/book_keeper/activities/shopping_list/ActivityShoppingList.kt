package com.dasbikash.book_keeper.activities.shopping_list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.edit.FragmentShoppingListEdit
import com.dasbikash.book_keeper.activities.shopping_list.view.FragmentShoppingListView
import com.dasbikash.menu_view.attachMenuViewForClick
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

    private fun getInitFragment():FragmentShoppingListDetails{
        when(isEditIntent(intent)){
            true -> getEditFragment(getShoppingListId(intent))
            false -> getViewFragment(getShoppingListId(intent))
        }.let {
            manageOptionsBtnBeforeChildLoading(it)
            return it
        }
    }

    private fun manageOptionsBtnBeforeChildLoading(fragment: FragmentShoppingListDetails) {
        if (fragment.getOptionsMenu(this) != null) {
            btn_options.attachMenuViewForClick(fragment.getOptionsMenu(this)!!)
            btn_options.show()
        } else {
            btn_options.setOnClickListener { }
            btn_options.hide()
        }
    }

    override fun addFragment(fragment: Fragment, doOnFragmentLoad: (() -> Unit)?) {
        manageOptionsBtnBeforeChildLoading(fragment as FragmentShoppingListDetails)
        super.addFragment(fragment, doOnFragmentLoad)
    }

    override fun addFragmentClearingBackStack(fragment: Fragment, doOnFragmentLoad: (() -> Unit)?) {
        manageOptionsBtnBeforeChildLoading(fragment as FragmentShoppingListDetails)
        super.addFragmentClearingBackStack(fragment, doOnFragmentLoad)
    }

    override fun getFragmentFromBackStack(): Fragment? {
        val fragment = super.getFragmentFromBackStack() as FragmentShoppingListDetails?
        fragment?.let {
            manageOptionsBtnBeforeChildLoading(fragment)
        }
        return fragment
    }

    private fun getViewFragment(shoppingListId: String): FragmentShoppingListView =
        FragmentShoppingListView.getInstance(shoppingListId)

    private fun getEditFragment(shoppingListId: String): FragmentShoppingListEdit =
        FragmentShoppingListEdit.getInstance(shoppingListId)

    fun setPageTitle(titleText:String) = page_title.setText(titleText)

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
