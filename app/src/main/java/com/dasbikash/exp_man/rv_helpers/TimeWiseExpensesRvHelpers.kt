package com.dasbikash.exp_man.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.toggle
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.utils.getTitleString
import com.dasbikash.exp_man.utils.optimizedString
import com.dasbikash.exp_man_repo.model.ExpenseEntry
import com.dasbikash.exp_man_repo.model.TimeBasedExpenseEntryGroup
import io.reactivex.rxjava3.subjects.PublishSubject

object TimeBasedExpenseEntryGroupDiffCallback: DiffUtil.ItemCallback<TimeBasedExpenseEntryGroup>(){
    override fun areItemsTheSame(oldItem: TimeBasedExpenseEntryGroup, newItem: TimeBasedExpenseEntryGroup) = oldItem.startTime == newItem.startTime
    override fun areContentsTheSame(oldItem: TimeBasedExpenseEntryGroup, newItem: TimeBasedExpenseEntryGroup): Boolean {
        return oldItem==newItem
    }
}

class TimeBasedExpenseEntryGroupAdapter(val timePeriodTitleClickEventPublisher: PublishSubject<CharSequence>) :
    ListAdapter<TimeBasedExpenseEntryGroup, TimeBasedExpenseEntryGroupHolder>(
        TimeBasedExpenseEntryGroupDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeBasedExpenseEntryGroupHolder {
        return TimeBasedExpenseEntryGroupHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_time_wise_exp_list, parent, false
            ), timePeriodTitleClickEventPublisher
        )
    }

    override fun onBindViewHolder(holder: TimeBasedExpenseEntryGroupHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}

class TimeBasedExpenseEntryGroupHolder(itemView: View, val timePeriodTitleClickEventPublisher: PublishSubject<CharSequence>) : RecyclerView.ViewHolder(itemView) {
    private val time_period_text_holder: ViewGroup = itemView.findViewById(R.id.time_period_text_holder)
    private val tv_time_period_text: TextView = itemView.findViewById(R.id.tv_time_period_text)
    private val tv_total_expense: TextView = itemView.findViewById(R.id.tv_total_expense)
    private val tv_exp_count: TextView = itemView.findViewById(R.id.tv_exp_count)
    private val rv_time_wise_exp_holder: RecyclerView = itemView.findViewById(R.id.rv_time_wise_exp_holder)

    private val expHolderAdapter = ExpenseEntryAdapter({editTask(it)},{deleteTask(it)},{})

    private fun editTask(expenseEntry: ExpenseEntry){
        TODO()
    }
    private fun deleteTask(expenseEntry: ExpenseEntry){
        TODO()
    }

    init {
        rv_time_wise_exp_holder.adapter = expHolderAdapter
        rv_time_wise_exp_holder.hide()
        time_period_text_holder.setOnClickListener {
//            rv_time_wise_exp_holder.toggle()
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

    fun bind(timeBasedExpenseEntryGroup: TimeBasedExpenseEntryGroup) {
        tv_time_period_text.text = timeBasedExpenseEntryGroup.getTitleString(itemView.context)
        tv_total_expense.text = timeBasedExpenseEntryGroup.totalExpense.optimizedString(2)
        tv_exp_count.text = timeBasedExpenseEntryGroup.expenseEntryIds.size.toString()

//        expHolderAdapter.submitList(timeBasedExpenseEntryGroup.expenses)
    }
}