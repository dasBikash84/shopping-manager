package com.dasbikash.book_keeper.activities.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.show

import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.rv_helpers.ShoppingListAdapter
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import kotlinx.android.synthetic.main.fragment_shopping_list.*
import kotlinx.coroutines.launch

// User may also share shopping list with connected users/ by QR code.

class FragmentShoppingList : FragmentHome() {

    private val shoppingListAdapter = ShoppingListAdapter({launchDetailView(it)})

    private fun launchDetailView(shoppingList: ShoppingList){
        TODO()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_shopping_list.adapter = shoppingListAdapter
        shopping_list_name_entry_holder.hide()

        btn_add_shopping_list.setOnClickListener {
            shopping_list_name_entry_holder.show()
            btn_add_shopping_list.hide()
        }

        btn_shopping_list_add_cancel.setOnClickListener {
            shopping_list_name_entry_holder.hide()
            btn_add_shopping_list.show()
        }

        btn_shopping_list_add.setOnClickListener { shoppingListCreateAction() }
    }

    private fun shoppingListCreateAction() {
        shopping_list_name_entry_holder.hide()
        btn_add_shopping_list.show()
    }

    override fun onResume() {
        super.onResume()
        runWithContext {
            lifecycleScope.launch {
                shoppingListAdapter.submitList(ShoppingListRepo.getAllShoppingLists(it).sortedByDescending { it.deadLine })
            }
        }
    }

    override fun getPageTitleId() = R.string.shopping_list_title
}