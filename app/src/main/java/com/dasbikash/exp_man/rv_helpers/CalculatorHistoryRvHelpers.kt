package com.dasbikash.exp_man.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.calculator.CalculatorHistory

object CalculatorHistoryDiffCallback: DiffUtil.ItemCallback<CalculatorHistory>(){
    override fun areItemsTheSame(oldItem: CalculatorHistory, newItem: CalculatorHistory) = oldItem.time == newItem.time
    override fun areContentsTheSame(oldItem: CalculatorHistory, newItem: CalculatorHistory): Boolean {
        return oldItem==newItem
    }
}

class CalculatorHistoryAdapter() :
    ListAdapter<CalculatorHistory, CalculatorHistoryHolder>(
        CalculatorHistoryDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalculatorHistoryHolder {
        return CalculatorHistoryHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_calc_history_entry,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CalculatorHistoryHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}

class CalculatorHistoryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tv_calc_history_string: TextView = itemView.findViewById(
        R.id.tv_calc_history_string
    )
    private val tv_calc_history_result: TextView = itemView.findViewById(
        R.id.tv_calc_history_result
    )

    fun bind(history: CalculatorHistory) {
        history.apply {
            tv_calc_history_string.text =
                itemView.context.getString(R.string.calc_history_string,leftOperand,operation,rightOperand)
            tv_calc_history_result.text =
                itemView.context.getString(R.string.calc_history_result,result)
        }
    }
}