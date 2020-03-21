package com.dasbikash.exp_man.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick
import java.nio.file.Files.delete

object ExpenseEntryDiffCallback: DiffUtil.ItemCallback<ExpenseEntry>(){
    override fun areItemsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry): Boolean {
        return oldItem==newItem
    }
}

class ExpenseEntryAdapter(val editTask:(ExpenseEntry)->Unit,val deleteTask:(ExpenseEntry)->Unit) :
    ListAdapter<ExpenseEntry, ExpenseEntryHolder>(
        ExpenseEntryDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseEntryHolder {
        return ExpenseEntryHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_exp_entry, parent, false
            ),editTask,deleteTask
        )
    }

    override fun onBindViewHolder(holder: ExpenseEntryHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}

class ExpenseEntryHolder(itemView: View,val editTask:(ExpenseEntry)->Unit,val deleteTask:(ExpenseEntry)->Unit) : RecyclerView.ViewHolder(itemView) {
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
    private val iv_exp_entry_options: ImageView = itemView.findViewById(
        R.id.iv_exp_entry_options
    )

    private lateinit var expenseEntry: ExpenseEntry

    init {
        val menuViewItems = listOf<MenuViewItem>(
            MenuViewItem(
                text = itemView.context.getString(R.string.edit),
                task = { editTask(expenseEntry) }
            ),
            MenuViewItem(
                text = itemView.context.getString(R.string.delete),
                task = { deleteTask(expenseEntry) }
            )
        )
        val menuView = MenuView()
        menuView.addAll(menuViewItems)
        iv_exp_entry_options.attachMenuViewForClick(menuView)
    }

    fun bind(expenseEntry: ExpenseEntry) {
        this.expenseEntry = expenseEntry
        expenseEntry.apply {
            tv_entry_time_text.text = DateUtils
                                        .getTimeString(time!!, itemView.context.getString(R.string.exp_entry_time_format))
                                        .let {
                                            if (checkIfEnglishLanguageSelected()) {
                                                it
                                            } else {
                                                DateTranslatorUtils.englishToBanglaDateString(it)
                                            }
                                        }
            tv_exp_amount_text.text = itemView.context.getString(R.string.double_2_dec_point,totalExpense)
            tv_exp_desc_text.text = details
            tv_exp_cat_text.text = expenseCategory?.let { if (checkIfEnglishLanguageSelected()) {it.name} else {it.nameBangla} }
        }
        itemView.setOnClickListener { debugLog(expenseEntry) }
    }
}