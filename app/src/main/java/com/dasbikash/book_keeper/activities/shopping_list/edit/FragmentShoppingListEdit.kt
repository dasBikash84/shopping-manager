package com.dasbikash.book_keeper.activities.shopping_list.edit

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList
import com.dasbikash.book_keeper.activities.shopping_list.FragmentShoppingListDetails
import java.lang.IllegalStateException

class FragmentShoppingListEdit : FragmentShoppingListDetails() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            context as ActivityShoppingList
        }catch (ex:Throwable){
            throw IllegalStateException("Should only be attached to shopping list activity!!")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list_edit, container, false)
    }

    private fun getShoppingListId():String = arguments!!.getString(ARG_SHOPPING_LIST_ID)!!

    companion object{
        private const val ARG_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.shopping_list.edit.FragmentShoppingListEdit.ARG_SHOPPING_LIST_ID"

        fun getInstance(shoppingListId: String): FragmentShoppingListEdit {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ID,shoppingListId)
            val fragment =
                FragmentShoppingListEdit()
            fragment.arguments = arg
            return fragment
        }
    }

}
