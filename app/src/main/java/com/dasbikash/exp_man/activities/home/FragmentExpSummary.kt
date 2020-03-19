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
import com.dasbikash.exp_man.utils.ExpenseEntryAdapter
import com.dasbikash.exp_man_repo.ExpenseRepo
import kotlinx.android.synthetic.main.fragment_exp_summary.*
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 */
class FragmentExpSummary : Fragment() {

    private val expenseEntryAdapter = ExpenseEntryAdapter()

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
                    rv_exp_entry.hide()
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
        chip_all.isChecked = true
    }

    private fun displayAllExpEntries() {
        if (expenseEntryAdapter.itemCount == 0){
            loadAllExpenseEntries()
        }
        rv_exp_entry.show()
    }

    private fun loadAllExpenseEntries() {
        runWithContext {
            lifecycleScope.launch {
                ExpenseRepo.getAllExpenseEntries(it).let {
                    expenseEntryAdapter.submitList(it)
                }
            }
        }
    }

}
