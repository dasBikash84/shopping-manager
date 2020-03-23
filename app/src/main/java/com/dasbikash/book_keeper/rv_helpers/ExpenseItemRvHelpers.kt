package com.dasbikash.book_keeper.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper.utils.optimizedString
import com.dasbikash.book_keeper_repo.model.ExpenseItem

object ExpenseItemDiffCallback: DiffUtil.ItemCallback<ExpenseItem>(){
    override fun areItemsTheSame(oldItem: ExpenseItem, newItem: ExpenseItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ExpenseItem, newItem: ExpenseItem): Boolean {
        return oldItem==newItem
    }
}

class ExpenseItemAdapter(val optionsClickAction:((ExpenseItem)->Unit)?=null) :
    ListAdapter<ExpenseItem, ExpenseItemHolder>(ExpenseItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseItemHolder {
        return ExpenseItemHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_exp_item, parent, false
            ),optionsClickAction
        )
    }

    override fun onBindViewHolder(holder: ExpenseItemHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}

class ExpenseItemHolder(itemView: View,val optionsClickAction:((ExpenseItem)->Unit)?) : RecyclerView.ViewHolder(itemView) {
    private val tv_exp_item_name_text: TextView = itemView.findViewById(R.id.tv_exp_item_name_text)
    private val tv_exp_item_brand_name_text: TextView = itemView.findViewById(R.id.tv_exp_item_brand_name_text)
    private val tv_exp_item_unit_price_text: TextView = itemView.findViewById(R.id.tv_exp_item_unit_price_text)
    private val tv_exp_item_total_price_text: TextView = itemView.findViewById(R.id.tv_exp_item_total_price_text)
    private val tv_exp_item_qty_text: TextView = itemView.findViewById(R.id.tv_exp_item_qty_text)
    private val tv_exp_item_uom_text: TextView = itemView.findViewById(R.id.tv_exp_item_uom_text)
    private val exp_item_brand_name_holder: ViewGroup = itemView.findViewById(R.id.exp_item_brand_name_holder)
    private val exp_item_options:ImageView = itemView.findViewById(R.id.exp_item_options)

    fun bind(expenseItem: ExpenseItem) {
        expenseItem.apply {
            tv_exp_item_name_text.text = name
            tv_exp_item_unit_price_text.text = unitPrice.optimizedString(2)
            tv_exp_item_qty_text.text = qty.optimizedString(2)
            tv_exp_item_total_price_text.text = (unitPrice*qty).optimizedString(2)
            tv_exp_item_uom_text.text = if (checkIfEnglishLanguageSelected()) {uom} else {uomBangla}
            if (!brandName.isNullOrBlank()) {
                tv_exp_item_brand_name_text.text = brandName
                exp_item_brand_name_holder.show()
            }else{
                exp_item_brand_name_holder.hide()
            }
        }
        if (optionsClickAction !=null) {
            exp_item_options.setOnClickListener { optionsClickAction.invoke(expenseItem) }
        }else{
            exp_item_options.hide()
        }
    }
}