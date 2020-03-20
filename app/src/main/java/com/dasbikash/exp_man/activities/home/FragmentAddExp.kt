package com.dasbikash.exp_man.activities.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_extensions.*
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.date_time_picker.DateTimePicker
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.calculator.ActivityCalculator
import com.dasbikash.exp_man.rv_helpers.ExpenseItemAdapter
import com.dasbikash.exp_man.utils.DateTranslatorUtils
import com.dasbikash.exp_man.utils.checkIfEnglishLanguageSelected
import com.dasbikash.exp_man.utils.optimizedString
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.exp_man_repo.ExpenseRepo
import com.dasbikash.exp_man_repo.SettingsRepo
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import com.dasbikash.exp_man_repo.model.ExpenseItem
import com.dasbikash.exp_man_repo.model.UnitOfMeasure
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick
import com.dasbikash.snackbar_ext.showShortSnack
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_add_exp.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class FragmentAddExp : Fragment(),WaitScreenOwner {

    private val TIME_REFRESH_INTERVAL = 1000L
    private val mEntryTime = Calendar.getInstance()
    private var timeAutoUpdateOn = true

    private var viewModel:AddExpViewModel?=null

    private val expenseCategories = mutableListOf<ExpenseCategory>()
    private val uoms = mutableListOf<UnitOfMeasure>()
    private val expenseItemAdapter= ExpenseItemAdapter({expenseItemOptionsClickAction(it)})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_exp, container, false)
    }

    private fun updateTime(){
        tv_entry_add.text = DateUtils.getTimeString(mEntryTime.time,getString(R.string.exp_entry_time_format)).let {
            return@let when(checkIfEnglishLanguageSelected()){
                true -> it
                false -> DateTranslatorUtils.englishToBanglaDateString(it)
            }
        }
    }

    private fun expenseItemOptionsClickAction(expenseItem: ExpenseItem){
        val menuViewItems = listOf<MenuViewItem>(
            MenuViewItem(
                text = getString(R.string.edit),
                task = {editExpenseItem(expenseItem)}
            ),
            MenuViewItem(
                text = getString(R.string.remove_text),
                task = {removeExpenseItem(expenseItem)}
            )
        )
        val menuView = MenuView()
        menuView.addAll(menuViewItems)
        runWithContext {menuView.show(it)}
    }

    override fun onResume() {
        super.onResume()
        refreshTime()
    }

    private fun refreshTime(){
        lifecycleScope.launch {
            if (timeAutoUpdateOn){
                mEntryTime.time = Date()
                updateTime()
                delay(TIME_REFRESH_INTERVAL)
                refreshTime()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(AddExpViewModel::class.java)
        rv_expense_items.adapter = expenseItemAdapter

        tv_entry_add_holder.setOnClickListener {
            runWithContext {
                val dateTimePicker = DateTimePicker(
                    date = mEntryTime.time,
                    doOnDateTimeSet = {
                        timeAutoUpdateOn = false
                        mEntryTime.time = it
                        updateTime()
                    }
                )
                dateTimePicker.display(it)
            }
        }

        spinner_category_selector.setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener<String>{
            override fun onItemSelected(
                view: MaterialSpinner?,
                position: Int,
                id: Long,
                item: String?
            ) {
                viewModel?.setExpenseCategory(expenseCategories.get(position))
            }
        })

        page_options.attachMenuViewForClick(getOptionsMenu())

        btn_add_exp_item.setOnClickListener {
            addExpItem()
        }

        viewModel?.getExpenseCategory()?.observe(this,object : Observer<ExpenseCategory>{
            override fun onChanged(expenseCategory: ExpenseCategory?) {
                expenseCategory?.let {
                    if (it.name?.contains(MISCELLANEOUS_TEXT,true) ?: false){
                        et_category_proposal_holder.show()
                    }else{
                        et_category_proposal.setText("")
                        et_category_proposal_holder.hide()
                    }
                }
            }
        })

        viewModel?.getExpenseItems()?.observe(this,object : Observer<List<ExpenseItem>>{
            override fun onChanged(expenseItems: List<ExpenseItem>?) {
                expenseItems?.let {
                    expenseItemAdapter.submitList(it)
                    var totalExpense = 0.0
                    it.forEach { totalExpense += it.qty*it.unitPrice }
                    et_total_expense.setText(totalExpense.optimizedString(2))
                    et_total_expense.isEnabled = false
                    if (it.isNotEmpty()){
                        cb_set_expense_manually.hide()
                        expense_item_list_holder.show()
                    }else{
                        cb_set_expense_manually.show()
                        expense_item_list_holder.hide()
                    }
                }
            }
        })

        btn_save_exp_entry.setOnClickListener { saveExpenseAction() }

        cb_set_expense_manually.setOnCheckedChangeListener({buttonView, isChecked ->
            et_total_expense.isEnabled = isChecked
        })

        initData()
    }

    private fun addExpItem() {
        if (et_product_name.text.isNullOrBlank()){
            et_product_name.error = getString(R.string.product_name_empty_error)
            return
        }
        if (et_unit_price.text.isNullOrBlank()){
            et_unit_price.error = getString(R.string.unit_price_empty_error)
            return
        }
        if (et_quantity.text.isNullOrBlank() ||
            et_quantity.text.toString().toDouble() == 0.0){
            et_unit_price.error = getString(R.string.quantity_empty_error)
            return
        }
        viewModel?.addExpenseItem(
            ExpenseItem(
                name = et_product_name.text?.trim()?.toString(),
                unitOfMeasure = getSelectedUom(),
                qty = et_quantity.text?.toString()?.toDouble()!!,
                unitPrice = et_unit_price.text?.toString()?.toDouble()!!,
                brandName = et_brand_name.text?.toString()
            )
        )
        et_product_name.setText("")
        et_brand_name.setText("")
        et_unit_price.setText(getString(R.string.default_unit_price))
        et_quantity.setText(getString(R.string.default_qty))
    }

    private fun editExpenseItem(expenseItem: ExpenseItem){
        expenseItem.apply {
            et_product_name.setText(name ?: "")
            et_brand_name.setText(brandName ?: "")
            et_unit_price.setText(unitPrice.toString())
            et_quantity.setText(qty.toString())
        }
        removeExpenseItem(expenseItem)
    }
    private fun removeExpenseItem(expenseItem: ExpenseItem){
        viewModel?.removeExpenseItem(expenseItem)
    }

    private fun getSelectedUom() = uoms.get(uom_selector.selectedItemPosition)

    private fun saveExpenseAction() {
        if (checkDataCorrectness()){
            runWithContext {
                lifecycleScope.launch {
                    val user = AuthRepo.getUser(it)
                    if (user!=null){
                        NetworkMonitor.runWithNetwork(it){ saveExpenseTask()}
                    }else {
                        saveExpenseTask()
                    }
                }
            }
        }
    }

    private fun saveExpenseTask() {
        runWithContext {
            lifecycleScope.launch {
                val expenseEntry = ExpenseEntry(
                    id = UUID.randomUUID().toString(),
                    time = mEntryTime.time,
                    categoryId = getSelectedExpenseCategory().id,
                    expenseCategory = getSelectedExpenseCategory(),
                    categoryProposal = et_category_proposal.text?.toString(),
                    description = et_description.text?.toString(),
                    expenseItems = expenseItemAdapter.currentList,
                    totalExpense = et_total_expense.text?.toString()?.toDouble()
                )
                ExpenseRepo.saveExpenseEntry(it,expenseEntry)
                showShortSnack(R.string.expense_saved_message)
                resetView()
            }
        }
    }

    private fun getSelectedExpenseCategory(): ExpenseCategory {
        return viewModel?.getExpenseCategory()?.value!!
    }

    private fun resetView() {
        runWithActivity {
            (it as ActivityHome).loadHomeFragment()
        }
    }

    private fun checkDataCorrectness(): Boolean {
        if (et_total_expense.text.isNullOrBlank() ||
                et_total_expense.text.toString().toDouble() == 0.0){
            et_total_expense.error = getString(R.string.total_expense_error_message)
            return false
        }

        if (et_description.text.isNullOrBlank()){
            et_description.error = getString(R.string.description_error_message)
            return false
        }
        return true
    }

    private fun initData() {
        runWithContext {
            lifecycleScope.launch {
                SettingsRepo.getAllExpenseCategories(it).apply {
                    expenseCategories.addAll(this.sortedBy { it.name })
                    spinner_category_selector.setItems(expenseCategories.map {
                        if (checkIfEnglishLanguageSelected()) {
                            it.name
                        }else{
                            it.nameBangla
                        }
                    })
                }

                SettingsRepo.getAllUoms(it).apply {
                    uoms.addAll(this.sortedBy { it.name })
                    val uomListAdapter = ArrayAdapter<String>(it, R.layout.view_spinner_item, uoms.map {
                            if (checkIfEnglishLanguageSelected()) {
                                it.name
                            }else{
                                it.nameBangla
                            }
                        }
                    )
                    uomListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    uom_selector.adapter = uomListAdapter
                }
            }
        }
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen

    private fun getOptionsMenu(): MenuView {
        val menuViewItems = listOf<MenuViewItem>(
            MenuViewItem(
                text = getString(R.string.calculator_title),
                task = {runWithActivity { it.startActivity(ActivityCalculator::class.java) }}
            )
        )

        val menuView = MenuView(menuItemFontBg = Color.BLACK,menuItemFontColor = Color.WHITE,menuItemFontSize = OPTIONS_MENU_ITEM_FONT_SIZE)
        menuView.addAll(menuViewItems)
        return menuView
    }

    companion object{
        private const val MISCELLANEOUS_TEXT = "Miscellaneous"
        private val OPTIONS_MENU_ITEM_FONT_SIZE = 20.00f
    }
}