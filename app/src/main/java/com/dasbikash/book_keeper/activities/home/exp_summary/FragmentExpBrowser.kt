package com.dasbikash.book_keeper.activities.home.exp_summary

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.*
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.expense_entry.ActivityExpenseEntry
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.ExpenseEntryAdapter
import com.dasbikash.book_keeper.rv_helpers.TimeBasedExpenseEntryGroupAdapter
import com.dasbikash.book_keeper.utils.GetCalculatorMenuItem
import com.dasbikash.book_keeper.utils.OptionsIntentBuilderUtility
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.book_keeper_repo.model.TimeBasedExpenseEntryGroup
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showShortSnack
import com.jaredrummler.materialspinner.MaterialSpinner
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_exp_summary.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentExpBrowser : FragmentTemplate(),WaitScreenOwner {

    private lateinit var viewModel: ViewModelExpBrowser

    private val timePeriodTitleClickEventPublisher: PublishSubject<TimeBasedExpenseEntryGroup> = PublishSubject.create()

    private val expenseEntryAdapter = ExpenseEntryAdapter({launchDetailView(it)},{editTask(it)},{deleteTask(it)},{incrementExpenseFetchLimit()})

    private val expenseCategories = mutableListOf<String>()

    private lateinit var timeBasedExpenseEntryGroupAdapter:TimeBasedExpenseEntryGroupAdapter

    private fun editTask(expenseEntry: ExpenseEntry){
        runWithContext {
            activity?.startActivity(ActivityExpenseEntry.getEditIntent(it,expenseEntry))
        }
    }
    private fun deleteTask(expenseEntry: ExpenseEntry){
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.confirm_delete_prompt),
                doOnPositivePress = { lifecycleScope.launch {
                    ExpenseRepo.delete(it, expenseEntry)
                    showShortSnack(R.string.delete_confirmaion_message)
                } }
            ))
        }
    }

    private fun launchDetailView(expenseEntry: ExpenseEntry){
        debugLog("launchDetailView: $expenseEntry")
        runWithActivity {
            startActivity(ActivityExpenseEntry.getViewIntent(it,expenseEntry))
        }
    }

    private fun incrementExpenseFetchLimit() {
        viewModel.incrementExpenseFetchLimit()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exp_summary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ViewModelExpBrowser::class.java)

        timeBasedExpenseEntryGroupAdapter = TimeBasedExpenseEntryGroupAdapter(
            timePeriodTitleClickEventPublisher,
            {launchDetailView(it)},
            {editTask(it)},{deleteTask(it)},
            viewModel.getTimeBasedExpenseEntryGroupLiveData(),this)

        rv_exp_entry.adapter = expenseEntryAdapter
        rv_time_based_exp_group.adapter = timeBasedExpenseEntryGroupAdapter

        timePeriodTitleClickEventPublisher.subscribe {
            debugLog("subscribe: ${it.startTime}")
            viewModel.setTimeBasedExpenseEntryGroup(it)
        }

        chip_all.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    all_exp_scroller.show()
                    rv_time_wise_exp_holder.hide()
                }
            }
        })
        chip_sort_by_date.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    displayDateWiseExpenses()
                    all_exp_scroller.hide()
                }
            }
        })
        chip_sort_by_week.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    displayWeekWiseExpenses()
                    all_exp_scroller.hide()
                }
            }
        })
        chip_sort_by_month.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    displayMonthWiseExpenses()
                    all_exp_scroller.hide()
                }
            }
        })

        chip_dsc_date.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    timeBasedExpenseEntryGroupAdapter.currentList.let {
                        timeBasedExpenseEntryGroupAdapter.submitList(it.sortedByDescending { it.startTime })
                    }
                }
            }
        })
        chip_asc_date.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    timeBasedExpenseEntryGroupAdapter.currentList.let {
                        timeBasedExpenseEntryGroupAdapter.submitList(it.sortedBy { it.startTime })
                    }
                }
            }
        })
        chip_asc_expense.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    timeBasedExpenseEntryGroupAdapter.currentList.let {
                        timeBasedExpenseEntryGroupAdapter.submitList(it.sortedByDescending { it.totalExpense })
                    }
                }
            }
        })
        chip_dsc_expens.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, checked: Boolean) {
                if (checked){
                    timeBasedExpenseEntryGroupAdapter.currentList.let {
                        timeBasedExpenseEntryGroupAdapter.submitList(it.sortedBy { it.totalExpense })
                    }
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

        btn_launch_search.setOnClickListener {
            if (search_text_holder.isVisible){
                et_search_text.setText("")
                search_text_holder.hide()
            }else{
                et_search_text.setText("")
                search_text_holder.show()
            }
        }

        et_search_text.addTextChangedListener(object : TextWatcher{

            override fun afterTextChanged(editable: Editable?) {
                (editable?.toString()?.trim() ?: "").let {
                    debugLog("searchText: $it")
                    viewModel.setSearchText(it)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        viewModel.getAllExpenseEntryLiveData().observe(this,object : Observer<List<ExpenseEntry>>{
            override fun onChanged(list: List<ExpenseEntry>?) {
                list?.let {
                    debugLog(it)
                    expenseEntryAdapter.submitList(it)
                }
            }
        })

        setCategorySpinnerItems()
        all_exp_scroller.show()
        rv_time_wise_exp_holder.hide()

        sr_page_holder.setOnRefreshListener {
            runWithContext {
                NetworkMonitor.runWithNetwork(it){
                    lifecycleScope.launch(Dispatchers.IO) {
                        runOnMainThread({showWaitScreen()})
                        try {
                            ExpenseRepo.syncData(it)
                        } catch (ex: Throwable) {
                            ex.printStackTrace()
                        }
                        runOnMainThread({
                            sr_page_holder.isRefreshing = false
                            hideWaitScreen()
                        })
                    }
                }.let {
                    if (!it){
                        sr_page_holder.isRefreshing = false
                    }
                }
            }
        }
    }

    private fun displayMonthWiseExpenses() {
        lifecycleScope.launch {
            showWaitScreen()
            timeBasedExpenseEntryGroupAdapter.submitList(emptyList())
            ExpenseRepo.getMonthBasedExpenseEntryGroups(context!!).let {
                delay(100)
                timeBasedExpenseEntryGroupAdapter.submitList(it)
            }
            rv_time_wise_exp_holder.show()
            hideWaitScreen()
        }
    }

    private fun displayWeekWiseExpenses() {
        lifecycleScope.launch {
            showWaitScreen()
            timeBasedExpenseEntryGroupAdapter.submitList(emptyList())
            ExpenseRepo.getWeekBasedExpenseEntryGroups(context!!).let {
                delay(100)
                timeBasedExpenseEntryGroupAdapter.submitList(it)
            }
            rv_time_wise_exp_holder.show()
            hideWaitScreen()
        }
    }

    private fun displayDateWiseExpenses() {
        lifecycleScope.launch {
            showWaitScreen()
            timeBasedExpenseEntryGroupAdapter.submitList(emptyList())
            ExpenseRepo.getDayBasedExpenseEntryGroups(context!!).let {
                delay(100)
                timeBasedExpenseEntryGroupAdapter.submitList(it)
            }
            rv_time_wise_exp_holder.show()
            hideWaitScreen()
        }
    }

    private fun filterByCategory(categoryName: String) {
        debugLog("categoryName: $categoryName")
        if (categoryName == getString(R.string.all_text)){
            viewModel.setExpenseCategory(null)
        }else{
            viewModel.setExpenseCategory(expenseCategories.indexOf(categoryName) - 1)
        }
    }

    private fun setCategorySpinnerItems() {
        runWithContext {
            lifecycleScope.launch {
                expenseCategories.add(getString(R.string.all_text))
                expenseCategories.addAll(resources.getStringArray(R.array.expense_categories))
                spinner_category_selector.setItems(expenseCategories)
            }
        }
    }
    override fun registerWaitScreen(): ViewGroup = wait_screen
    override fun getPageTitle(context: Context):String? = context.getString(R.string.exp_browse_page_title)
    override fun getOptionsMenu(context: Context): MenuView? {
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
}