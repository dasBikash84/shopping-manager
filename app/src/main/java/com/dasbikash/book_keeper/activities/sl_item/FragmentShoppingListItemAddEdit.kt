package com.dasbikash.book_keeper.activities.sl_item

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.hideKeyboard
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper_repo.SettingsRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ExpenseCategory
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import com.dasbikash.book_keeper_repo.model.UnitOfMeasure
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_shopping_list_item_add_edit.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.launch

class FragmentShoppingListItemAddEdit private constructor() : FragmentShoppingListItem(),
    WaitScreenOwner {

    override fun registerWaitScreen(): ViewGroup = wait_screen

    private var exitPrompt: String? = null
    private lateinit var shoppingListItem: ShoppingListItem

    private val expenseCategories = mutableListOf<ExpenseCategory>()
    private val uoms = mutableListOf<UnitOfMeasure>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list_item_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        exitPrompt = getString(R.string.discard_and_exit_prompt)
        et_sli_name.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.name = text?.toString()?.trim() ?: ""
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_sli_details.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.details = text?.toString()?.trim()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_sli_min_price.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.minUnitPrice = text?.toString()?.let { if (it.isNotBlank()) {it.toDouble()} else {null}}
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_sli_max_price.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.maxUnitPrice = text?.toString()?.let { if (it.isNotBlank()) {it.toDouble()} else {null}}
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_sli_quantity.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.qty = text?.toString()?.let { if (it.isNotBlank()) {it.toDouble()} else {0.0}} ?: 0.0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        sli_category_selector.setOnItemSelectedListener(MaterialSpinner.OnItemSelectedListener<String> { view, position, id, item ->
            hideKeyboard()
            shoppingListItem.categoryId = expenseCategories.find { if (checkIfEnglishLanguageSelected())
                                                                    {
                                                                        item == it.name
                                                                    } else {
                                                                        item == it.nameBangla
                                                                    }
                                                                }!!.id
        })
        uom_selector.setOnItemSelectedListener(MaterialSpinner.OnItemSelectedListener<String> { view, position, id, item ->
            hideKeyboard()
            uoms.find { if (checkIfEnglishLanguageSelected())
                            {
                                item == it.name
                            } else {
                                item == it.nameBangla
                            }
                        }!!.let{
                            shoppingListItem.uom = it.name
                            shoppingListItem.uomBangla = it.nameBangla
                        }
        })
        btn_cancel.setOnClickListener {
            hideKeyboard()
            activity?.onBackPressed()
        }
        btn_save_sl_item.setOnClickListener {
            hideKeyboard()
            saveShoppingListItem()
        }
    }

    private fun saveShoppingListItem() {
        if (validateData()){
            runWithContext {
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    message = it.getString(R.string.save_shopping_list_item_prompt),
                    doOnPositivePress = {
                        lifecycleScope.launch {
                            showWaitScreen()
                            exitPrompt = null
                            ShoppingListRepo.save(it,shoppingListItem)
                            activity?.onBackPressed()
                            hideWaitScreen()
                        }
                    }
                ))
            }
        }
    }

    private fun validateData(): Boolean {
        if (shoppingListItem.name.isNullOrBlank()){
            et_sli_name.error = getString(R.string.et_sli_name_empty_error)
            return false
        }
        if (shoppingListItem.qty == 0.0){
            et_sli_quantity.error = getString(R.string.quantity_error_message)
            return false
        }
        return true
    }

    override fun getPageTitle(context: Context): String? {
        if (arguments?.containsKey(ARG_SHOPPING_LIST_ITEM_ID) == true) {
            return null
        } else {
            return context.getString(R.string.shopping_list_item_create_title)
        }
    }

    override fun getExitPrompt(): String? {
        return exitPrompt
    }

    override fun onResume() {
        super.onResume()
        initShoppingListItem()
    }

    private fun initShoppingListItem() {
        runWithContext {
            lifecycleScope.launch {
                showWaitScreen()
                if (!::shoppingListItem.isInitialized) {
                    getShoppingListItemId().let {
                        shoppingListItem = if (it == null) {
                            ShoppingListItem(shoppingListId = getShoppingListId())
                        } else {
                            val item = ShoppingListRepo.findShoppingListItemById(context!!, it)!!
                            (activity as ActivityShoppingListItem?)?.setPageTitle(getString(R.string.edit_title,item.name))
                            return@let item
                        }
                    }
                }
                if (expenseCategories.isEmpty()) {
                    expenseCategories.addAll(SettingsRepo.getAllExpenseCategories(it))
                    sli_category_selector.setItems(expenseCategories.map {
                        if (checkIfEnglishLanguageSelected()) {
                            it.name
                        } else {
                            it.nameBangla
                        }
                    })
                }
                if (uoms.isEmpty()) {
                    uoms.addAll(SettingsRepo.getAllUoms(it))
                    uom_selector.setItems(uoms.map {
                        if (checkIfEnglishLanguageSelected()) {
                            it.name
                        } else {
                            it.nameBangla
                        }
                    })
                }
                refreshView()
                hideWaitScreen()
            }
        }
    }

    private fun refreshView() {
        shoppingListItem.name?.let { et_sli_name.setText(it) }
        sli_category_selector.selectedIndex = getCurrentCategoryIndex()
        shoppingListItem.details?.let { et_sli_details.setText(it) }
        shoppingListItem.minUnitPrice?.let { et_sli_min_price.setText(it.toString()) }
        shoppingListItem.maxUnitPrice?.let { et_sli_max_price.setText(it.toString()) }
        shoppingListItem.qty.let { et_sli_quantity.setText(it.toString()) }
        uom_selector.selectedIndex = getCurrentUomIndex()
//        TODO("Not yet implemented")
    }

    private fun getCurrentUomIndex(): Int {
        if (shoppingListItem.uom == null) {
            shoppingListItem.uom = uoms.get(0).name
            shoppingListItem.uomBangla = uoms.get(0).nameBangla
        }
        return uoms.map { it.name }.indexOf(shoppingListItem.uom).let {
            if (it < 0) {
                return@let 0
            } else {
                return@let it
            }
        }
    }

    private fun getCurrentCategoryIndex(): Int {
        if (shoppingListItem.categoryId == null) {
            shoppingListItem.categoryId = expenseCategories.get(0).id
        }
        return expenseCategories.map { it.id }.indexOf(shoppingListItem.categoryId!!).let {
            if (it < 0) {
                return@let 0
            } else {
                return@let it
            }
        }
    }

    private fun getShoppingListItemId(): String? = arguments?.getString(ARG_SHOPPING_LIST_ITEM_ID)
    private fun getShoppingListId(): String = arguments?.getString(ARG_SHOPPING_LIST_ID)!!

    companion object {

        private const val ARG_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.sl_item.FragmentShoppingListItemAddEdit.ARG_SHOPPING_LIST_ID"

        private const val ARG_SHOPPING_LIST_ITEM_ID =
            "com.dasbikash.book_keeper.activities.sl_item.FragmentShoppingListItemAddEdit.ARG_SHOPPING_LIST_ITEM_ID"

        fun getInstanceForEdit(shoppingListItemId: String): FragmentShoppingListItemAddEdit {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ITEM_ID, shoppingListItemId)
            val fragment = FragmentShoppingListItemAddEdit()
            fragment.arguments = arg
            return fragment
        }

        fun getInstance(shoppingListId: String): FragmentShoppingListItemAddEdit {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ID, shoppingListId)
            val fragment = FragmentShoppingListItemAddEdit()
            fragment.arguments = arg
            return fragment
        }
    }

}