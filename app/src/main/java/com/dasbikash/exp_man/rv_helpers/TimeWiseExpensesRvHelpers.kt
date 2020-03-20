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
import com.dasbikash.exp_man.model.TimeWiseExpenses
import io.reactivex.rxjava3.subjects.PublishSubject

object TimeWiseExpensesDiffCallback: DiffUtil.ItemCallback<TimeWiseExpenses>(){
    override fun areItemsTheSame(oldItem: TimeWiseExpenses, newItem: TimeWiseExpenses) = oldItem.periodText == newItem.periodText
    override fun areContentsTheSame(oldItem: TimeWiseExpenses, newItem: TimeWiseExpenses): Boolean {
        return oldItem==newItem
    }
}

class TimeWiseExpensesAdapter(val timePeriodTitleClickEventPublisher: PublishSubject<CharSequence>) :
    ListAdapter<TimeWiseExpenses, TimeWiseExpensesHolder>(
        TimeWiseExpensesDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeWiseExpensesHolder {
        return TimeWiseExpensesHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_time_wise_exp_list, parent, false
            ), timePeriodTitleClickEventPublisher
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

    private val expHolderAdapter =
        ExpenseEntryAdapter()

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