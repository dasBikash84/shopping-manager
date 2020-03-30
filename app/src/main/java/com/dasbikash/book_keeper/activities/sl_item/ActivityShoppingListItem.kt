package com.dasbikash.book_keeper.activities.sl_item

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.menu_view.attachMenuViewForClick
import com.dasbikash.super_activity.SingleFragmentSuperActivity
import kotlinx.android.synthetic.main.activity_shopping_list_item.*

class ActivityShoppingListItem: SingleFragmentSuperActivity() {

    override fun getDefaultFragment(): Fragment = getChildInstance()

    private fun getChildInstance(): FragmentShoppingListItem {
        val fragment:FragmentShoppingListItem
        if (intent.hasExtra(EXTRA_SHOPPING_LIST_ITEM_ID)){
            if (intent.hasExtra(EXTRA_EDIT_MODE)){
                fragment = FragmentShoppingListItemAddEdit.getInstanceForEdit(getShoppingListItemId())
            }else{
                fragment = FragmentShoppingListItemView.getInstance(getShoppingListItemId())
            }
        }else{
            fragment = FragmentShoppingListItemAddEdit.getInstance()
        }
        fragment.getPageTitle(this).let {
            if (it.isNullOrBlank()){
                setPageTitle(getString(R.string.shopping_list_item_title))
            }else {
                setPageTitle(it)
            }
        }
        fragment.getOptionsMenu(this).let {
            if (it != null){
                btn_options.attachMenuViewForClick(it)
                btn_options.show()
            }else{
                btn_options.hide()
            }
        }
        return fragment
    }

    override fun getLayoutID(): Int = R.layout.activity_shopping_list_item
    override fun getLoneFrameId(): Int = R.id.sl_item_frame

    private fun getShoppingListItemId() = intent.getStringExtra(EXTRA_SHOPPING_LIST_ITEM_ID)!!

    fun setPageTitle(title:String) = page_title.setText(title)

    override fun onBackPressed() {
        (getCurrentFragment() as FragmentShoppingListItem?)?.let {
            if (it.getExitPrompt() == null){
                super.onBackPressed()
            }else{
                DialogUtils.showAlertDialog(this, DialogUtils.AlertDialogDetails(
                    message = it.getExitPrompt()!!,
                    doOnPositivePress = {
                        super.onBackPressed()
                    }
                ))
            }
        }
    }

    companion object {

        private const val EXTRA_SHOPPING_LIST_ITEM_ID =
            "com.dasbikash.book_keeper.activities.sl_item.ActivityShoppingListItem.SHOPPING_LIST_ITEM_ID"
        private const val EXTRA_EDIT_MODE =
            "com.dasbikash.book_keeper.activities.sl_item.ActivityShoppingListItem.EXTRA_EDIT_MODE"
        private const val EXTRA_VIEW_MODE =
            "com.dasbikash.book_keeper.activities.sl_item.ActivityShoppingListItem.EXTRA_VIEW_MODE"

        fun getCreateIntent(context: Context): Intent {
            val intent = Intent(context.applicationContext, ActivityShoppingListItem::class.java)
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