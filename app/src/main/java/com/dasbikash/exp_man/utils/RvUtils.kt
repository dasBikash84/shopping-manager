package com.dasbikash.exp_man.utils

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.toggle
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.calculator.CalculatorHistory
import com.dasbikash.exp_man.activities.launcher.checkIfEnglishLanguageSelected
import com.dasbikash.exp_man.model.TimeWiseExpenses
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import io.reactivex.rxjava3.subjects.PublishSubject

object CalculatorHistoryDiffCallback: DiffUtil.ItemCallback<CalculatorHistory>(){
    override fun areItemsTheSame(oldItem: CalculatorHistory, newItem: CalculatorHistory) = oldItem.time == newItem.time
    override fun areContentsTheSame(oldItem: CalculatorHistory, newItem: CalculatorHistory): Boolean {
        return oldItem==newItem
    }
}

class CalculatorHistoryAdapter() :
    ListAdapter<CalculatorHistory, CalculatorHistoryHolder>(CalculatorHistoryDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalculatorHistoryHolder {
        return CalculatorHistoryHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_calc_history_entry, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: CalculatorHistoryHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}

class CalculatorHistoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tv_calc_history_string: TextView = itemView.findViewById(R.id.tv_calc_history_string)
    private val tv_calc_history_result: TextView = itemView.findViewById(R.id.tv_calc_history_result)

    fun bind(history: CalculatorHistory) {
        history.apply {
            tv_calc_history_string.text =
                itemView.context.getString(R.string.calc_history_string,leftOperand,operation,rightOperand)
            tv_calc_history_result.text =
                itemView.context.getString(R.string.calc_history_result,result)
        }
    }
}

object ExpenseEntryDiffCallback: DiffUtil.ItemCallback<ExpenseEntry>(){
    override fun areItemsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry): Boolean {
        return oldItem==newItem
    }
}

class ExpenseEntryAdapter() :
    ListAdapter<ExpenseEntry, ExpenseEntryHolder>(ExpenseEntryDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseEntryHolder {
        return ExpenseEntryHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_exp_entry, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ExpenseEntryHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}

class ExpenseEntryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tv_entry_time_text: TextView = itemView.findViewById(R.id.tv_entry_time_text)
    private val tv_exp_amount_text: TextView = itemView.findViewById(R.id.tv_exp_amount_text)
    private val tv_exp_qty_text: TextView = itemView.findViewById(R.id.tv_exp_qty_text)
    private val tv_exp_desc_text: TextView = itemView.findViewById(R.id.tv_exp_desc_text)
    private val tv_exp_cat_text: TextView = itemView.findViewById(R.id.tv_exp_cat_text)

    fun bind(expenseEntry: ExpenseEntry) {
        expenseEntry.apply {
            tv_entry_time_text.text = DateUtils.getLongDateString(time!!).let { if (checkIfEnglishLanguageSelected()) {it} else {DateTranslatorUtils.englishToBanglaDateString(it)} }
            tv_exp_amount_text.text = itemView.context.getString(R.string.double_2_dec_point,unitPrice*qty)
            tv_exp_qty_text.text = itemView.context.getString(R.string.exp_qty_text,qty,unitOfMeasure?.let { if (checkIfEnglishLanguageSelected()) {it.name} else {it.nameBangla} })
            tv_exp_desc_text.text = description
            tv_exp_cat_text.text = expenseCategory?.let { if (checkIfEnglishLanguageSelected()) {it.name} else {it.nameBangla} }
        }
    }
}

object TimeWiseExpensesDiffCallback: DiffUtil.ItemCallback<TimeWiseExpenses>(){
    override fun areItemsTheSame(oldItem: TimeWiseExpenses, newItem: TimeWiseExpenses) = oldItem.periodText == newItem.periodText
    override fun areContentsTheSame(oldItem: TimeWiseExpenses, newItem: TimeWiseExpenses): Boolean {
        return oldItem==newItem
    }
}

class TimeWiseExpensesAdapter(val timePeriodTitleClickEventPublisher: PublishSubject<CharSequence>) :
    ListAdapter<TimeWiseExpenses, TimeWiseExpensesHolder>(TimeWiseExpensesDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeWiseExpensesHolder {
        return TimeWiseExpensesHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_time_wise_exp_list, parent, false
            ),timePeriodTitleClickEventPublisher
        )
    }

    override fun onBindViewHolder(holder: TimeWiseExpensesHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}

class TimeWiseExpensesHolder(itemView: View,val timePeriodTitleClickEventPublisher: PublishSubject<CharSequence>) : RecyclerView.ViewHolder(itemView) {
    private val time_period_text_holder: ViewGroup = itemView.findViewById(R.id.time_period_text_holder)
    private val tv_time_period_text: TextView = itemView.findViewById(R.id.tv_time_period_text)
    private val rv_time_wise_exp_holder: RecyclerView = itemView.findViewById(R.id.rv_time_wise_exp_holder)

    private val expHolderAdapter = ExpenseEntryAdapter()

    init {
        rv_time_wise_exp_holder.adapter = expHolderAdapter
        rv_time_wise_exp_holder.hide()
        time_period_text_holder.setOnClickListener {
            rv_time_wise_exp_holder.toggle()
            timePeriodTitleClickEventPublisher.onNext(tv_time_period_text.text)
        }
        timePeriodTitleClickEventPublisher.subscribe {
            if (rv_time_wise_exp_holder.isVisible){
                if (tv_time_period_text.text != it){
                    rv_time_wise_exp_holder.hide()
                }
            }
        }
    }

    fun bind(timeWiseExpenses: TimeWiseExpenses) {
        tv_time_period_text.text = timeWiseExpenses.periodText
        expHolderAdapter.submitList(timeWiseExpenses.expenses)
    }
}