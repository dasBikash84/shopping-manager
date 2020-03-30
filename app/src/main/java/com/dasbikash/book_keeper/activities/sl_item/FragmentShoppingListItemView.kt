package com.dasbikash.book_keeper.activities.sl_item

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dasbikash.book_keeper.R

class FragmentShoppingListItemView private constructor():FragmentShoppingListItem() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return  inflater.inflate(R.layout.fragment_shopping_list_item_view, container, false)
    }

    companion object {
        private const val ARG_SHOPPING_LIST_ITEM_ID =
            "com.dasbikash.book_keeper.activities.sl_item.FragmentShoppingListItemView.ARG_SHOPPING_LIST_ITEM_ID"

        fun getInstance(shoppingListItemId: String): FragmentShoppingListItemView {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ITEM_ID, shoppingListItemId)
            val fragment = FragmentShoppingListItemView()
            fragment.arguments = arg
            return fragment
        }
    }

}