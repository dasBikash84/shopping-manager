package com.dasbikash.book_keeper.activities.home.add_exp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.*
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.calculator.ActivityCalculator
import com.dasbikash.book_keeper.activities.home.ActivityHome
import com.dasbikash.book_keeper.activities.home.FragmentHome
import com.dasbikash.book_keeper.rv_helpers.ExpenseItemAdapter
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper.utils.optimizedString
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.SettingsRepo
import com.dasbikash.book_keeper_repo.model.ExpenseCategory
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.book_keeper_repo.model.ExpenseItem
import com.dasbikash.book_keeper_repo.model.UnitOfMeasure
import com.dasbikash.date_time_picker.DateTimePicker
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showShortSnack
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_add_exp.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class FragmentAddExp : FragmentHome(), WaitScreenOwner {

    private val TIME_REFRESH_INTERVAL = 1000L
    private val mEntryTime = Calendar.getInstance()
    private var timeAutoUpdateOn = true

    private var expenseEntry:ExpenseEntry?=null

    private var viewModel: ViewModelAddExp? = null

    private val expenseCategories = mutableListOf<ExpenseCategory>()
    private val uoms = mutableListOf<UnitOfMeasure>()
    private val expenseItemAdapter = ExpenseItemAdapter({ expenseItemOptionsClickAction(it) })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_exp, container, false)
    }

    private fun updateTime() {
        tv_entry_add.text =
            DateUtils.getTimeString(mEntryTime.time, getString(R.string.exp_entry_time_format))
                .let {
                    return@let when (checkIfEnglishLanguageSelected()) {
                        true -> it
                        false -> TranslatorUtils.englishToBanglaDateString(it)
                    }
                }
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
                viewModel?.setExpenseCategory(expenseCategories.get(position))
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

        viewModel?.getExpenseCategory()?.observe(this, object : Observer<ExpenseCategory> {
            override fun onChanged(expenseCategory: ExpenseCategory?) {
                expenseCategory?.let {
                    if (it.name?.contains(MISCELLANEOUS_TEXT, true) ?: false) {
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

        cb_set_expense_manually.setOnCheckedChangeListener({ buttonView, isChecked ->
            et_total_expense.isEnabled = isChecked
        })

        initData()
    }

    private fun setTime(it: Date) {
        timeAutoUpdateOn = false
        mEntryTime.time = it
        updateTime()
    }

    private fun addExpItem() {
        if (et_product_name.text.isNullOrBlank()) {
            et_product_name.error = getString(R.string.product_name_empty_error)
            return
        }
        if (et_unit_price.text.isNullOrBlank() || et_unit_price.text.toString().toDouble() <= 0.0) {
            et_unit_price.error = getString(R.string.unit_price_empty_error)
            return
        }
        if (et_quantity.text.isNullOrBlank() ||
            et_quantity.text.toString().toDouble() <= 0.0
        ) {
            et_quantity.error = getString(R.string.quantity_empty_error)
            return
        }
        viewModel?.addExpenseItem(
            ExpenseItem(
                name = et_product_name.text?.trim()?.toString(),
                qty = et_quantity.text?.toString()?.toDouble()!!,
                unitPrice = et_unit_price.text?.toString()?.toDouble()!!,
                brandName = et_brand_name.text?.toString(),
                uom = getSelectedUom().name,
                uomBangla = getSelectedUom().nameBangla
            )
        )
        et_product_name.setText("")
        et_brand_name.setText("")
        et_unit_price.setText(getString(R.string.default_unit_price))
        et_quantity.setText(getString(R.string.default_qty))
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
            et_unit_price.setText(unitPrice.toString())
            et_quantity.setText(qty.toString())
            uom_selector.selectedIndex = uoms.map { if (checkIfEnglishLanguageSelected()) {it.name} else {it.nameBangla} }.indexOf(if (checkIfEnglishLanguageSelected()) {uom} else {uomBangla})
        }
        removeExpenseItem(expenseItem)
    }

    private fun removeExpenseItem(expenseItem: ExpenseItem) {
        viewModel?.removeExpenseItem(expenseItem)
    }

    private fun getSelectedUom() = uoms.get(uom_selector.selectedIndex)

    private fun saveExpenseTask() {
        if (checkDataCorrectness()) {
            runWithContext {
                DialogUtils.showAlertDialog(
                    it, DialogUtils.AlertDialogDetails(
                        message = it.getString(R.string.save_exp_entry_prompt),
                        doOnPositivePress = {
                            lifecycleScope.launch {
                                if (expenseEntry == null){
                                    expenseEntry = ExpenseEntry()
                                }
                                expenseEntry?.apply {
                                    time = mEntryTime.time
                                    categoryId = getSelectedExpenseCategory().id
                                    expenseCategory = getSelectedExpenseCategory()
                                    categoryProposal = et_category_proposal.text?.toString()
                                    details = et_description.text?.toString()
                                    expenseItems = expenseItemAdapter.currentList
                                    totalExpense = et_total_expense.text?.toString()?.toDouble()
                                    taxVat = viewModel?.getVatTax()?.value ?: 0.0
                                    updateModified()
                                }?.apply {
                                    ExpenseRepo.saveExpenseEntry(it, this)
                                    showShortSnack(R.string.expense_saved_message)
                                }
                                resetView()
                            }
                        })
                )
            }
        }
    }

    private fun getSelectedExpenseCategory(): ExpenseCategory {
        return viewModel?.getExpenseCategory()?.value ?: expenseCategories.get(0)
    }

    private fun resetView() {
        runWithActivity {
            if (!isEditFragment()) {
                (it as ActivityHome).loadHomeFragment()
            }else{
                activity?.finish()
            }
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
            lifecycleScope.launch(Dispatchers.IO) {
                SettingsRepo.getAllExpenseCategories(it).apply {
                    expenseCategories.addAll(this.sortedBy { it.name })
                    runOnMainThread({
                        spinner_category_selector.setItems(expenseCategories.map {
                            if (checkIfEnglishLanguageSelected()) {
                                it.name
                            } else {
                                it.nameBangla
                            }
                        })
                    })
                }

                SettingsRepo.getAllUoms(it).apply {
                    uoms.addAll(this.sortedBy { it.name })
                    runOnMainThread({
                        uom_selector.setItems(uoms.map {
                            if (checkIfEnglishLanguageSelected()) {
                                it.name
                            } else {
                                it.nameBangla
                            }
                        })
                    })
                }
                withContext(Dispatchers.Main) {
                    getExpenseEntry()?.let {
                        expenseEntry = it
                        debugLog(it)
                        setTime(it.time!!)
                        et_description.setText(it.details)
                        et_vat_ait.setText(it.taxVat.toString())
                        if (!it.expenseItems.isNullOrEmpty()) {
                            it.expenseItems?.let {
                                debugLog("$it")
                                viewModel?.addExpenseItems(it)
                                expense_item_list_holder.show()
                            }
                        }else {
                            cb_set_expense_manually.isChecked = true
                            runOnMainThread({et_total_expense.setText(it.totalExpense?.optimizedString(2))},100L)
                        }
                        it.expenseCategory?.let {
                            spinner_category_selector.selectedIndex = expenseCategories.indexOf(it).let { if (it==-1) {0} else {it} }
                        }
                        btn_cancel.show()
                        btn_cancel.setOnClickListener {
                            activity?.onBackPressed()
                        }
                    }
                    hideWaitScreen()
                }
            }
        }
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen

    override fun getPageTitleId():Int{
        return when(isEditFragment()){
            true -> R.string.edit_expense_title
            false -> R.string.add_expense_title
        }
    }

    private fun isEditFragment():Boolean{
        return arguments?.getString(EXTRA_EXP_EDIT_MODE) !=null
    }

    private suspend fun getExpenseEntry():ExpenseEntry?{
        arguments?.getString(EXTRA_EXP_ENTRY_ID)?.let {
            val id = it
            return ExpenseRepo.getExpenseEntryById(context!!,id)
        }
        return null
    }

    override fun getOptionsMenu(context: Context): MenuView? {
        val menuView = MenuView()
        menuView.add(
            MenuViewItem(context.getString(R.string.calculator_title),{activity?.let { startActivity(
                Intent(it,ActivityCalculator::class.java)
            ) }})
        )
        menuView.add(
            MenuViewItem("Notepad",{})
        )
        return menuView
    }

    companion object {
        private const val MISCELLANEOUS_TEXT = "Miscellaneous"

        private const val EXTRA_EXP_EDIT_MODE =
            "com.dasbikash.exp_man.activities.home.add_exp.FragmentAddExp.EXTRA_EXP_EDIT_MODE"
        private const val EXTRA_EXP_ENTRY_ID =
            "com.dasbikash.exp_man.activities.home.add_exp.FragmentAddExp.EXTRA_EXP_ENTRY_ID"

        fun getEditInstance(expenseEntryId: String):FragmentAddExp{
            val fragment = FragmentAddExp()
            val bundle = Bundle()
            bundle.putString(EXTRA_EXP_EDIT_MODE,EXTRA_EXP_EDIT_MODE)
            bundle.putString(EXTRA_EXP_ENTRY_ID,expenseEntryId)
            fragment.arguments = bundle
            return fragment
        }
    }
}