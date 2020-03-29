package com.dasbikash.book_keeper.activities.shopping_list.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dasbikash.android_basic_utils.utils.debugLog

import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList
import com.dasbikash.book_keeper.activities.shopping_list.FragmentShoppingListDetails
import com.dasbikash.book_keeper.activities.shopping_list.edit.FragmentShoppingListEdit
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showShortSnack
import java.lang.IllegalStateException


class FragmentShoppingListView : FragmentShoppingListDetails() {

    private lateinit var viewModel: ViewModelShoppingListView

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
        return inflater.inflate(R.layout.fragment_shopping_list_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewModelShoppingListView::class.java)

        viewModel.getShoppingList().observe(this,object : Observer<ShoppingList>{
            override fun onChanged(shoppingList: ShoppingList?) {
                shoppingList?.let {
                    debugLog(it)
                    (activity as ActivityShoppingList?)?.apply { setPageTitle(it.title!!) }
                }
            }
        })

        viewModel.setShoppingListId(getShoppingListId())
    }

    private fun getShoppingListId():String = arguments!!.getString(ARG_SHOPPING_LIST_ID)!!

    override fun getOptionsMenu(context: Context): MenuView? {
        return MenuView().apply {
            add(
                MenuViewItem(
                    text = context.getString(R.string.edit),
                    task = {
                        (activity as ActivityShoppingList).addFragment(
                            FragmentShoppingListEdit.getInstance(
                                getShoppingListId()
                            )
                        )
                    }
                )
            )
            add(
                MenuViewItem(
                    text = "Share",
                    task = { showShortSnack("Sharing not implemented yet!") }
                )
            )
        }
    }

    companion object{

        private const val ARG_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.shopping_list.view.FragmentShoppingListView.ARG_SHOPPING_LIST_ID"

        fun getInstance(shoppingListId: String): FragmentShoppingListView {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ID,shoppingListId)
            val fragment =
                FragmentShoppingListView()
            fragment.arguments = arg
            return fragment
        }
    }

}
