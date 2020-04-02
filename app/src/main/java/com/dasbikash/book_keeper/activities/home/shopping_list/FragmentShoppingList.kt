package com.dasbikash.book_keeper.activities.home.shopping_list

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.ShoppingListAdapter
import com.dasbikash.book_keeper_repo.model.ShoppingList
import kotlinx.android.synthetic.main.fragment_shopping_list.*
import kotlinx.android.synthetic.main.view_wait_screen.*

// User may also share shopping list with connected users/ by QR code.

class FragmentShoppingList : FragmentTemplate(),WaitScreenOwner {
    private lateinit var viewModel: ViewModelShoppingList
    override fun registerWaitScreen(): ViewGroup = wait_screen

    private val shoppingListAdapter = ShoppingListAdapter({launchDetailView(it)})

    private fun launchDetailView(shoppingList: ShoppingList){
        runWithContext {
            startActivity(ActivityShoppingList.getViewIntent(it,shoppingList.id))
        }
    }

    private fun launchEditView(shoppingList: ShoppingList){
        runWithContext {
            startActivity(ActivityShoppingList.getEditIntent(it,shoppingList.id))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ViewModelShoppingList::class.java)
        rv_shopping_list.adapter = shoppingListAdapter

        btn_add_shopping_list.setOnClickListener { showListAddDialog() }

        viewModel.getShoppingListLiveData().observe(this,object : Observer<List<ShoppingList>>{
            override fun onChanged(list: List<ShoppingList>?) {
                (list ?: emptyList()).let {
                    shoppingListAdapter.submitList(it.sortedByDescending { it.deadLine })
                }
            }
        })
    }

    private fun showListAddDialog() {
        runWithContext {
            startActivity(ActivityShoppingList.getCreateIntent(it))
        }
    }
    override fun getPageTitle(context: Context):String? = context.getString(R.string.shopping_list_title)
}