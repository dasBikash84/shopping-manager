package com.dasbikash.exp_man.activities.home

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.show
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.date_time_picker.DateTimePicker
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.utils.DateTranslatorUtils
import com.dasbikash.exp_man.utils.checkIfEnglishLanguageSelected
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.exp_man_repo.ExpenseRepo
import com.dasbikash.exp_man_repo.SettingsRepo
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import com.dasbikash.exp_man_repo.model.UnitOfMeasure
import com.dasbikash.snackbar_ext.showShortSnack
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

    private var totalExpense = 0.0
    private var qty = 1
    private var unitPrice = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_exp, container, false)
    }

    private fun updateTime(){
        tv_entry_add.text = DateUtils.getLongDateString(mEntryTime.time).let {
            return@let when(checkIfEnglishLanguageSelected()){
                true -> it
                false -> DateTranslatorUtils.englishToBanglaDateString(it)
            }
        }
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

        et_total_expense.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(totalExpenseData: Editable?) {
                totalExpenseData?.toString()?.let { viewModel?.setTotalExpense(it) }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        et_quantity.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(quantityText: Editable?) {
                quantityText?.toString()?.let { viewModel?.setQuantity(it) }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        category_selector.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel?.setExpenseCategory(getSelectedExpenseCategory())
            }
        })

        viewModel?.getTotalExpense()?.observe(this,object : Observer<Double>{
            override fun onChanged(totalExpense: Double?) {
                totalExpense?.let {
                    this@FragmentAddExp.totalExpense = it
                    refreshUnitPriceDisplay()
                }
            }
        })

        viewModel?.getQuantity()?.observe(this,object : Observer<Int>{
            override fun onChanged(quantity: Int?) {
                quantity?.let {
                    this@FragmentAddExp.qty = it
                    refreshUnitPriceDisplay()
                }
            }
        })

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

        btn_save_exp_entry.setOnClickListener { saveExpenseAction() }

        refreshUnitPriceDisplay()
        initData()
    }

    private fun getSelectedExpenseCategory() = expenseCategories.get(category_selector.selectedItemPosition)
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
                    unitPrice = unitPrice,
                    qty = qty,
                    unitId = getSelectedUom().id,
                    description = et_description.text?.toString(),
                    categoryId = getSelectedExpenseCategory().id,
                    categoryProposal = et_category_proposal.text?.toString(),
                    productName = et_product_name.text?.toString(),
                    created = System.currentTimeMillis(),
                    modified = System.currentTimeMillis()
                )
                showWaitScreen()
                ExpenseRepo.saveExpenseEntry(it,expenseEntry)
                showShortSnack(R.string.expense_saved_message)
                resetView()
                hideWaitScreen()
            }
        }
    }

    private fun resetView() {
        mEntryTime.time = Date()
        timeAutoUpdateOn = true
        refreshTime()
        et_total_expense.setText(getString(R.string.total_expense_default_value))
        et_quantity.setText(getString(R.string.quantity_default_value))
        et_description.setText("")
        et_category_proposal.setText("")
        et_category_proposal.hide()
        et_product_name.setText("")
        refreshUnitPriceDisplay()
    }

    private fun checkDataCorrectness(): Boolean {
        if (et_total_expense.text.isNullOrBlank() ||
                et_total_expense.text.toString().toDouble() == 0.0){
            et_total_expense.error = getString(R.string.total_expense_error_message)
            return false
        }
        if (et_quantity.text.isNullOrBlank() ||
            et_quantity.text.toString().toInt() == 0){
            et_quantity.error = getString(R.string.quantity_error_message)
            return false
        }
        if (et_description.text.isNullOrBlank()){
            et_description.error = getString(R.string.description_error_message)
            return false
        }
        return true
    }

    private fun refreshUnitPriceDisplay(){
        unitPrice = totalExpense/qty
        tv_unit_price.text = unitPrice.toString()
    }

    private fun initData() {
        runWithContext {
            lifecycleScope.launch {
                SettingsRepo.getAllExpenseCategories(it).apply {
                    expenseCategories.addAll(this.sortedBy { it.name })
                    val categoriesAdapter = ArrayAdapter<String>(it, R.layout.view_spinner_item, expenseCategories.map {
                        if (checkIfEnglishLanguageSelected()) {
                            it.name
                        }else{
                            it.nameBangla
                        }
                    })
                    categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    category_selector.adapter = categoriesAdapter
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

    companion object{
        private const val MISCELLANEOUS_TEXT = "Miscellaneous"
    }
}