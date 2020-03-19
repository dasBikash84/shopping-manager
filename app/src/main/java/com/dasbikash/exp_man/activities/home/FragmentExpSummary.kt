package com.dasbikash.exp_man.activities.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.show

import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.launcher.checkIfEnglishLanguageSelected
import com.dasbikash.exp_man.utils.ExpenseEntryAdapter
import com.dasbikash.exp_man_repo.ExpenseRepo
import com.dasbikash.exp_man_repo.SettingsRepo
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_exp_summary.*
import kotlinx.coroutines.launch
import com.google.android.material.snackbar.Snackbar

import com.jaredrummler.materialspinner.MaterialSpinner




/**
 * A simple [Fragment] subclass.
 */
class FragmentExpSummary : Fragment() {

    private val expenseEntryAdapter = ExpenseEntryAdapter()
    private val expenseCategories = mutableListOf<ExpenseCategory>()
    private val expenseEntries = mutableListOf<ExpenseEntry>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exp_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_exp_entry.adapter = expenseEntryAdapter
        chip_all.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    displayAllExpEntries()
                }else{
                    all_exp_scroller.hide()
                }
            }
        })
        chip_sort_by_date.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                debugLog("chip_sort_by_date: $p1")
            }
        })
        chip_sort_by_week.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                debugLog("chip_sort_by_week: $p1")
            }
        })
        chip_sort_by_month.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
                debugLog("chip_sort_by_month: $p1")
            }
        })

        spinner_category_selector.setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener<String>{
            override fun onItemSelected(
                view: MaterialSpinner?,
                position: Int,
                id: Long,
                item: String?
            ) {
                item?.let { filterByCategory(it) }
            }
        })

        setCategorySpinnerItems()

        chip_all.isChecked = true
    }

    private fun filterByCategory(categoryName: String) {
        if (categoryName == getString(R.string.all_text)){
            expenseEntryAdapter.submitList(expenseEntries.toList())
        }else{
            if (checkIfEnglishLanguageSelected()) {
                expenseCategories.find { it.name==categoryName }!!
            } else {
                expenseCategories.find { it.nameBangla==categoryName }!!
            }.let {
                val expenseCategory = it
                expenseEntryAdapter.submitList(expenseEntries.filter { it.expenseCategory == expenseCategory })
            }
        }
    }

    private fun setCategorySpinnerItems() {
        runWithContext {
            lifecycleScope.launch {
                val categoryNames= mutableListOf<String>(getString(R.string.all_text))
                expenseCategories.addAll(SettingsRepo.getAllExpenseCategories(it))
                categoryNames.addAll(expenseCategories.sortedBy { it.name }.map { if (checkIfEnglishLanguageSelected()) {it.name!!} else {it.nameBangla!!} })
                spinner_category_selector.setItems(categoryNames)
            }
        }
    }

    private fun displayAllExpEntries() {
        if (expenseEntries.isEmpty()){
            loadAllExpenseEntries()
        }
        all_exp_scroller.show()
    }

    private fun loadAllExpenseEntries() {
        runWithContext {
            lifecycleScope.launch {
                ExpenseRepo.getAllExpenseEntries(it).let {
                    expenseEntries.addAll(it)
                    expenseEntryAdapter.submitList(it)
                }
            }
        }
    }

}
