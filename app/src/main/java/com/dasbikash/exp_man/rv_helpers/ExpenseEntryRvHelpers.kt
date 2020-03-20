package com.dasbikash.exp_man.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.utils.DateTranslatorUtils
import com.dasbikash.exp_man.utils.checkIfEnglishLanguageSelected
import com.dasbikash.exp_man_repo.model.ExpenseEntry

object ExpenseEntryDiffCallback: DiffUtil.ItemCallback<ExpenseEntry>(){
    override fun areItemsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry): Boolean {
        return oldItem==newItem
    }
}

class ExpenseEntryAdapter() :
    ListAdapter<ExpenseEntry, ExpenseEntryHolder>(
        ExpenseEntryDiffCallback
    ) {
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
    private val tv_entry_time_text: TextView = itemView.findViewById(
        R.id.tv_entry_time_text
    )
    private val tv_exp_amount_text: TextView = itemView.findViewById(
        R.id.tv_exp_amount_text
    )
    private val tv_exp_desc_text: TextView = itemView.findViewById(
        R.id.tv_exp_desc_text
    )
    private val tv_exp_cat_text: TextView = itemView.findViewById(
        R.id.tv_exp_cat_text
    )

    fun bind(expenseEntry: ExpenseEntry) {
        expenseEntry.apply {
            tv_entry_time_text.text = DateUtils.getLongDateString(
                time!!
            )
                .let { if (checkIfEnglishLanguageSelected()) {it} else {
                    DateTranslatorUtils.englishToBanglaDateString(
                        it
                    )
                } }
            tv_exp_amount_text.text = itemView.context.getString(R.string.double_2_dec_point,totalExpense)
            tv_exp_desc_text.text = details
            tv_exp_cat_text.text = expenseCategory?.let { if (checkIfEnglishLanguageSelected()) {it.name} else {it.nameBangla} }
        }
        itemView.setOnClickListener { debugLog(expenseEntry) }
    }
}