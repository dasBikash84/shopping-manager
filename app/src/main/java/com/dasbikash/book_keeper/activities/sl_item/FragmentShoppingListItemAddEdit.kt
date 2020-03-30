package com.dasbikash.book_keeper.activities.sl_item

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dasbikash.book_keeper.R

class FragmentShoppingListItemAddEdit private constructor():FragmentShoppingListItem() {

    private var exitPrompt:String?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.fragment_shopping_list_item_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exitPrompt = getString(R.string.discard_and_exit_prompt)
    }

    override fun getPageTitle(context: Context): String? {
        if (arguments?.containsKey(ARG_SHOPPING_LIST_ITEM_ID)==true){
            return null
        }else{
            return context.getString(R.string.shopping_list_item_create_title)
        }
    }

    override fun getExitPrompt(): String? {
        return exitPrompt
    }

    companion object {
        private const val ARG_SHOPPING_LIST_ITEM_ID =
            "com.dasbikash.book_keeper.activities.sl_item.FragmentShoppingListItemAddEdit.ARG_SHOPPING_LIST_ITEM_ID"

        fun getInstanceForEdit(shoppingListItemId: String): FragmentShoppingListItemAddEdit {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ITEM_ID, shoppingListItemId)
            val fragment = FragmentShoppingListItemAddEdit()
            fragment.arguments = arg
            return fragment
        }

        fun getInstance(): FragmentShoppingListItemAddEdit {
            return FragmentShoppingListItemAddEdit()
        }
    }

}