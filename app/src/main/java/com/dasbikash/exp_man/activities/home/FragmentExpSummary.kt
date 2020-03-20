package com.dasbikash.exp_man.activities.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.show
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.async_manager.runSuspended
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.model.TimeWiseExpenses
import com.dasbikash.exp_man.rv_helpers.ExpenseEntryAdapter
import com.dasbikash.exp_man.rv_helpers.TimeWiseExpensesAdapter
import com.dasbikash.exp_man.utils.*
import com.dasbikash.exp_man_repo.ExpenseRepo
import com.dasbikash.exp_man_repo.SettingsRepo
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import com.jaredrummler.materialspinner.MaterialSpinner
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_exp_summary.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class FragmentExpSummary : Fragment(),WaitScreenOwner {

    private val timePeriodTitleClickEventPublisher: PublishSubject<CharSequence> = PublishSubject.create()

    private val expenseEntryAdapter =
        ExpenseEntryAdapter()
    private val expenseCategories = mutableListOf<ExpenseCategory>()
    private val expenseEntries = mutableListOf<ExpenseEntry>()

    private val timeWiseExpensesAdapter =
        TimeWiseExpensesAdapter(
            timePeriodTitleClickEventPublisher
        )

    private val timeWiseExpensesList = mutableListOf<TimeWiseExpenses>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exp_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_exp_entry.adapter = expenseEntryAdapter
        rv_time_wise_exp.adapter = timeWiseExpensesAdapter
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
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    displayDateWiseExpenses()
                }else{
                    rv_time_wise_exp_holder.hide()
                }
            }
        })
        chip_sort_by_week.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    displayWeekWiseExpenses()
                }else{
                    rv_time_wise_exp_holder.hide()
                }
            }
        })
        chip_sort_by_month.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    displayMonthWiseExpenses()
                }else{
                    rv_time_wise_exp_holder.hide()
                }
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
        rv_time_wise_exp_holder.hide()
        chip_all.isChecked = true
    }

    private fun displayMonthWiseExpenses() =
        displayTimeWiseExpenses({it.getMonthCount()},{DateUtils.getTimeString(it,getString(R.string.month_title_format))},{expenseEntry,date->
            expenseEntry.time!!.getMonthCount() == date.getMonthCount()
        })

    private fun displayWeekWiseExpenses() =
        displayTimeWiseExpenses({it.getWeekCount()},{it.getWeekString()},{expenseEntry,date->
            expenseEntry.time!!.getWeekCount() == date.getWeekCount()
        })

    private fun displayDateWiseExpenses() =
        displayTimeWiseExpenses({it.getDayCount()},{DateUtils.getShortDateString(it)},{expenseEntry,date->
            expenseEntry.time!!.getDayCount() == date.getDayCount()
        })

    private fun displayTimeWiseExpenses(
                                    dateToPeriod:(Date)->Int,
                                    titleStringGen:(Date)->String,
                                    groupResolver:(ExpenseEntry,Date)->Boolean
    ) {
        lifecycleScope.launch {
            timeWiseExpensesAdapter.submitList(emptyList())
            showWaitScreen()
            delay(100)
            runSuspended {
                timeWiseExpensesList.clear()
                val distinctDates = expenseEntries
                                                    .map { it.time!! }
                                                    .distinctBy {dateToPeriod(it)}
                                                    .sorted()
                                                    .reversed()
                distinctDates.asSequence().forEach { debugLog(it) }
                distinctDates.forEachIndexed({index,date->
                    val dateString = titleStringGen(date)
                    timeWiseExpensesList.add(index, TimeWiseExpenses(if (checkIfEnglishLanguageSelected()) {dateString} else {DateTranslatorUtils.englishToBanglaDateString(dateString)}))
                })
                timeWiseExpensesList.asSequence().forEach {debugLog(it.periodText) }
                expenseEntries.asSequence().forEach {
                    val expenseEntry= it
                    timeWiseExpensesList
                        .get(distinctDates.indexOfFirst { groupResolver(expenseEntry,it)})
                        .expenses.add(expenseEntry)
                }
                timeWiseExpensesList.asSequence().forEach {
                    it.expenses.sortByDescending { it.time }
                    debugLog(it.periodText)
                    it.expenses.forEach {debugLog(it)}
                }
            }
            timeWiseExpensesAdapter.submitList(timeWiseExpensesList)
            rv_time_wise_exp_holder.show()
            hideWaitScreen()
        }
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
                showWaitScreen()
                ExpenseRepo.getAllExpenseEntries(it).let {
                    expenseEntries.addAll(it)
                    expenseEntryAdapter.submitList(it)
                }
                hideWaitScreen()
            }
        }
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen
}