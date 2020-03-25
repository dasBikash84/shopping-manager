package com.dasbikash.book_keeper.activities.home.shopping_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.hideKeyboard
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.FragmentHome
import com.dasbikash.book_keeper.rv_helpers.ShoppingListAdapter
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_shopping_list.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.launch

// User may also share shopping list with connected users/ by QR code.

class FragmentShoppingList : FragmentHome(),WaitScreenOwner {
    private lateinit var viewModel: ViewModelShoppingList
    override fun registerWaitScreen(): ViewGroup = wait_screen

    private val shoppingListAdapter = ShoppingListAdapter({launchDetailView(it)})

    private fun launchDetailView(shoppingList: ShoppingList){

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
            val view = LayoutInflater.from(it).inflate(R.layout.view_add_shopping_list,null,false)
            val editText = view.findViewById<EditText>(R.id.et_shopping_list_name)
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = "${it.getString(R.string.add_shopping_list)}?",
                view = view,
                positiveButtonText = getString(R.string.save_text),
                doOnPositivePress = {
                    hideKeyboard()
                    shoppingListCreateAction(editText.text.toString())
                }
            ))
        }
    }

    private fun shoppingListCreateAction(shoppingListName:String) {
        if (shoppingListName.isNotBlank()) {
            runWithContext {
                lifecycleScope.launch {
                    showWaitScreen()
                    ShoppingListRepo.createList(it, shoppingListName.trim()).let {
                        if (it == null) {
                            showShortSnack(R.string.duplicate_shopping_list_name)
                        } else {
                            launchDetailView(it)
                        }
                    }
                    hideWaitScreen()
                }
            }
        }
    }
    override fun getPageTitleId() = R.string.shopping_list_title
}