package com.dasbikash.book_keeper.activities.expense_entry.add_exp

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.pop_up_message.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.*
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.ActivityHome
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.ExpenseItemAdapter
import com.dasbikash.book_keeper.utils.*
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.book_keeper_repo.model.ExpenseItem
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import com.dasbikash.date_time_picker.DateTimePicker
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.pop_up_message.showShortSnack
import com.google.firebase.Timestamp
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_add_exp.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class FragmentExpAddEdit : FragmentTemplate(), WaitScreenOwner {

    private val TIME_REFRESH_INTERVAL = 1000L
    private val mEntryTime = Calendar.getInstance()
    private var timeAutoUpdateOn = true

    private lateinit var expenseEntry:ExpenseEntry
    private var initHashcode:Int=0

    private var viewModel: ViewModelAddExp? = null

    private val expenseCategories = mutableListOf<String>()
    private val uoms = mutableListOf<String>()
    private val expenseItemAdapter = ExpenseItemAdapter({ expenseItemOptionsClickAction(it) })

    private var shoppingListItem:ShoppingListItem?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_exp, container, false)
    }

    private fun updateTime(@StringRes timeFormatStringId:Int=R.string.exp_entry_time_format_secs) {
        tv_entry_add.text = mEntryTime.time.toTranslatedString(context!!,timeFormatStringId)
    }

    private fun expenseItemOptionsClickAction(expenseItem: ExpenseItem) {
        val menuViewItems = listOf<MenuViewItem>(
            MenuViewItem(
                text = getString(R.string.edit),
                task = { editExpenseItem(expenseItem) }
            ),
            MenuViewItem(
                text = getString(R.string.remove_text),
                task = { removeExpenseItem(expenseItem) }
            )
        )
        val menuView = MenuView()
        menuView.addAll(menuViewItems)
        runWithContext { menuView.show(it) }
    }

    override fun onResume() {
        super.onResume()
        if (!isEditFragment()) {
            refreshTime()
        }
    }

    private fun refreshTime() {
        lifecycleScope.launch {
            if (timeAutoUpdateOn) {
                mEntryTime.time = Date()
                updateTime()
                delay(TIME_REFRESH_INTERVAL)
                refreshTime()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ViewModelAddExp::class.java)
        rv_expense_items.adapter = expenseItemAdapter

        tv_entry_add_holder.setOnClickListener {
            runWithContext {
                val dateTimePicker = DateTimePicker(
                    date = mEntryTime.time,
                    maxDate = Date(),
                    doOnDateTimeSet = {
                        setTime(it)
                    }
                )
                dateTimePicker.display(it)
            }
        }

        spinner_category_selector.setOnItemSelectedListener(object :
            MaterialSpinner.OnItemSelectedListener<String> {
            override fun onItemSelected(
                view: MaterialSpinner?,
                position: Int,
                id: Long,
                item: String?
            ) {
                viewModel?.setExpenseCategory(position)
                hideKeyboard()
            }
        })

        uom_selector.setOnItemSelectedListener(object :
            MaterialSpinner.OnItemSelectedListener<String> {
            override fun onItemSelected(
                view: MaterialSpinner?,
                position: Int,
                id: Long,
                item: String?
            ) {
                hideKeyboard()
            }
        })

        price_input_process_selector.setOnItemSelectedListener(object :
            MaterialSpinner.OnItemSelectedListener<String> {
            override fun onItemSelected(
                view: MaterialSpinner?,
                position: Int,
                id: Long,
                item: String?
            ) {
                hideKeyboard()
            }
        })

        btn_add_exp_item.setOnClickListener {
            hideKeyboard()
            addExpItem()
        }

        et_vat_ait.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(editable: Editable?) {
                editable?.toString()?.let {
                    if (it.isNotBlank()){
                        it.toDouble().let {
                            if (it<0){
                                et_vat_ait.error = getString(R.string.tax_vat_error)
                            }else{
                                viewModel?.setVatTax(it)
                            }
                        }
                    }else{
                        viewModel?.setVatTax(0.0)
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        viewModel?.getExpenseCategory()?.observe(this, object : Observer<Int> {
            override fun onChanged(expenseCategory: Int?) {
                expenseCategory?.let {
                    if (expenseCategories.get(it).contains(MISCELLANEOUS_TEXT, true)) {
                        et_category_proposal_holder.show()
                    } else {
                        et_category_proposal.setText("")
                        et_category_proposal_holder.hide()
                    }
                }
            }
        })

        viewModel?.getExpenseItems()?.observe(this, object : Observer<List<ExpenseItem>> {
            override fun onChanged(expenseItems: List<ExpenseItem>?) {
                expenseItems?.let {
                    expenseItemAdapter.submitList(it)
                    calculateTotalExpense(viewModel?.getVatTax()?.value ?: 0.0,it)
                }
            }
        })

        viewModel?.getVatTax()?.observe(this,object : Observer<Double>{
            override fun onChanged(vatTax: Double?) {
                vatTax?.let {
                    calculateTotalExpense(it,expenseItemAdapter.currentList)
                }
            }
        })

        btn_save_exp_entry.setOnClickListener {
            hideKeyboard()
            saveExpenseTask()
        }

        cb_set_expense_manually.setOnCheckedChangeListener({ _, isChecked ->
            et_total_expense.isEnabled = isChecked
        })

        initData()
    }

    private fun setTime(it: Date) {
        timeAutoUpdateOn = false
        mEntryTime.time = it
        updateTime(R.string.exp_entry_time_format)
    }

    private fun readUnitPrice():Double{
        try {
            return when {
                price_input_process_selector.selectedIndex==0 -> et_product_price.text!!.toString().toDouble().get2DecPoints()
                else -> (et_product_price.text!!.toString().toDouble()/readQuantity()).get2DecPoints()
            }
        }catch (ex:Throwable){
            ex.printStackTrace()
            return 0.0
        }
    }

    private fun readQuantity():Double{
        try {
            return when{
                et_quantity.text!!.isBlank() == true -> 0.0
                else -> et_quantity.text!!.toString().toDouble().get2DecPoints()
            }
        }catch (ex:Throwable){
            ex.printStackTrace()
            return 0.0
        }
    }

    private fun addExpItem() {
        if (et_product_name.text.isNullOrBlank()) {
            et_product_name.error = getString(R.string.product_name_empty_error)
            return
        }
        if (readQuantity() == 0.0) {
            et_quantity.error = getString(R.string.quantity_empty_error)
            return
        }
        if (readUnitPrice() == 0.0) {
            et_product_price.error = getString(R.string.unit_price_empty_error)
            return
        }
        viewModel?.addExpenseItem(
            ExpenseItem(
                name = et_product_name.text?.trim()?.toString(),
                qty = readQuantity(),
                unitPrice = readUnitPrice(),
                brandName = et_brand_name.text?.toString(),
                uom = getSelectedUom()
            )
        )
        et_product_name.setText("")
        et_brand_name.setText("")
        et_product_price.setText(getString(R.string.default_unit_price))
        et_quantity.setText(getString(R.string.default_qty))
        price_input_process_selector.selectedIndex = 0
    }

    private fun calculateTotalExpense(vatTax:Double,expenseItems:List<ExpenseItem>){
        var totalExpense = 0.0
        expenseItems.forEach { totalExpense += it.qty * it.unitPrice }
        totalExpense *= (1+vatTax/100)
        et_total_expense.setText(totalExpense.optimizedString(2))
        et_total_expense.isEnabled = false
        if (expenseItems.isNotEmpty()) {
            cb_set_expense_manually.hide()
            expense_item_list_holder.show()
        } else {
            cb_set_expense_manually.show()
            expense_item_list_holder.hide()
        }
    }

    private fun editExpenseItem(expenseItem: ExpenseItem) {
        expenseItem.apply {
            et_product_name.setText(name ?: "")
            et_brand_name.setText(brandName ?: "")
            et_product_price.setText(unitPrice.toString())
            et_quantity.setText(qty.toString())
            uom_selector.selectedIndex = uom
            price_input_process_selector.selectedIndex = 0
        }
        removeExpenseItem(expenseItem)
    }

    private fun removeExpenseItem(expenseItem: ExpenseItem) {
        viewModel?.removeExpenseItem(expenseItem)
    }

    private fun getSelectedUom() = uom_selector.selectedIndex

    private fun saveExpenseTask() {
        if (checkDataCorrectness()) {
            runWithContext {
                DialogUtils.showAlertDialog(
                    it, DialogUtils.AlertDialogDetails(
                        message = it.getString(R.string.save_exp_entry_prompt),
                        doOnPositivePress = {
                            lifecycleScope.launch {
                                accumulateData()
                                ExpenseRepo.saveExpenseEntry(it, expenseEntry)
                                debugLog("Final hashcode: ${expenseEntry.hashCode()}")
                                if (shoppingListItem!=null){
                                    shoppingListItem!!.expenseEntryId = expenseEntry.id
                                    ShoppingListRepo.save(it,shoppingListItem!!)
                                }
                                resetView()
                            }
                        })
                )
            }
        }
    }

    private fun accumulateData(){
        expenseEntry.apply {
            time = Timestamp(mEntryTime.time)
            categoryId = getSelectedExpenseCategory()
            categoryProposal = et_category_proposal.text?.toString()
            details = et_description.text?.toString()
            expenseItems = expenseItemAdapter.currentList
            totalExpense = et_total_expense.text?.toString()?.toDouble()
            taxVat = viewModel?.getVatTax()?.value ?: 0.0
        }
    }

    private fun getCurrentHashcode():Int{
        accumulateData()
        debugLog("Current hashcode: ${expenseEntry.hashCode()}")
        return expenseEntry.hashCode()
    }

    private fun getSelectedExpenseCategory(): Int {
        return viewModel?.getExpenseCategory()?.value ?: 0
    }

    private fun resetView() {
        runWithActivity {
            if (it is ActivityHome){
                showShortSnack(R.string.expense_saved_message)
                it.loadExpAddFragment()
            }else {
                activity?.finish()
            }
        }
    }

    override fun getExitPrompt(): String? {
        if (getCurrentHashcode() != initHashcode) {
            return getString(R.string.discard_and_exit_prompt)
        }else{
            return null
        }
    }

    private fun checkDataCorrectness(): Boolean {
        if (et_total_expense.text.isNullOrBlank() ||
            et_total_expense.text.toString().toDouble() == 0.0
        ) {
            et_total_expense.error = getString(R.string.total_expense_error_message)
            return false
        }

        if (et_description.text.isNullOrBlank()) {
            et_description.error = getString(R.string.description_error_message)
            return false
        }
        return true
    }

    private fun initData() {
        runWithContext {
            showWaitScreen()
            lifecycleScope.launch {
                expenseCategories.addAll(resources.getStringArray(R.array.expense_categories))
                spinner_category_selector.setItems(expenseCategories)
                uoms.addAll(resources.getStringArray(R.array.uoms))
                uom_selector.setItems(uoms)
                price_input_process_selector.setItems(resources.getStringArray(R.array.price_input_process).toList())

                getExpenseEntry().let {
                    if (it!=null) {
                        expenseEntry = it
                        debugLog(it)
                        it.time?.let { setTime(it.toDate()) }
                        et_description.setText(it.details)
                        et_vat_ait.setText(it.taxVat.toString())
                        if (!it.expenseItems.isNullOrEmpty()) {
                            it.expenseItems?.let {
                                debugLog("$it")
                                viewModel?.addExpenseItems(it)
                                expense_item_list_holder.show()
                            }
                        } else {
                            cb_set_expense_manually.isChecked = true
                            runOnMainThread({
                                et_total_expense.setText(
                                    it.totalExpense?.optimizedString(
                                        2
                                    )
                                )
                            }, 100L)
                        }
                        it.categoryId.let {
                            spinner_category_selector.selectedIndex = it
                            viewModel?.setExpenseCategory(it)
                        }
                        btn_cancel.show()
                        btn_cancel.setOnClickListener {
                            activity?.onBackPressed()
                        }
                    }else{
                        expenseEntry = ExpenseEntry()
                    }
                    initHashcode = expenseEntry.hashCode()
                    debugLog("Init hashcode: ${initHashcode}")
                }

                hideWaitScreen()

            }
        }
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen

    override fun getPageTitle(context: Context):String?{
        return when(isEditFragment()){
            true -> context.getString(R.string.edit_expense_title)
            false -> context.getString(R.string.add_expense_title)
        }
    }

    private fun isEditFragment():Boolean{
        return arguments?.getString(ARG_EXP_EDIT_MODE) !=null
    }

    private suspend fun getExpenseEntry():ExpenseEntry?{
        if (context==null){return null}
        arguments?.getString(ARG_EXP_ENTRY_ID)?.let {
            val id = it
            return ExpenseRepo.getExpenseEntryById(context!!,id)
        }
        arguments?.getString(ARG_SHOPPING_LIST_ITEM_ID)?.let {
            return ShoppingListRepo.findShoppingListItemById(context!!,it)?.let {
                shoppingListItem = it
                AuthRepo.getUser(context!!)?.let {
                    shoppingListItem?.toExpenseEntry(it)
                }
            }
        }
        return null
    }

    override suspend fun getOptionsMenu(context: Context): MenuView? {
        val menuView = MenuView()
        menuView.add(GetCalculatorMenuItem(context))
        menuView.add(
            MenuViewItem(
                text = context.getString(R.string.share_app_text),
                task = {
                    runWithActivity {
                        startActivity(OptionsIntentBuilderUtility.getShareAppIntent(it))
                    }
                }
            )
        )
        return menuView
    }

    companion object {
        private const val MISCELLANEOUS_TEXT = "Miscellaneous"

        private const val ARG_EXP_EDIT_MODE =
            "com.dasbikash.book_keeper.activities.expense_entry.add_exp.FragmentExpAddEdit.ARG_EXP_EDIT_MODE"
        private const val ARG_EXP_ENTRY_ID =
            "com.dasbikash.book_keeper.activities.expense_entry.add_exp.FragmentExpAddEdit.ARG_EXP_ENTRY_ID"
        private const val ARG_SHOPPING_LIST_ITEM_ID =
            "com.dasbikash.book_keeper.activities.expense_entry.add_exp.FragmentExpAddEdit.ARG_SHOPPING_LIST_ITEM_ID"

        fun getEditInstance(expenseEntryId: String): FragmentExpAddEdit {
            val fragment =
                FragmentExpAddEdit()
            val bundle = Bundle()
            bundle.putString(
                ARG_EXP_EDIT_MODE,
                ARG_EXP_EDIT_MODE
            )
            bundle.putString(ARG_EXP_ENTRY_ID,expenseEntryId)
            fragment.arguments = bundle
            return fragment
        }

        fun getShoppingListItemSaveInstance(shoppingListItemId:String): FragmentExpAddEdit {
            val fragment = FragmentExpAddEdit()
            val bundle = Bundle()
            bundle.putString(
                ARG_SHOPPING_LIST_ITEM_ID,
                shoppingListItemId
            )
            fragment.arguments = bundle
            return fragment
        }
    }
}
