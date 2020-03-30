package com.dasbikash.book_keeper.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper_repo.model.ShoppingListItem

object ShoppingListItemDiffCallback: DiffUtil.ItemCallback<ShoppingListItem>(){
    override fun areItemsTheSame(oldItem: ShoppingListItem, newItem: ShoppingListItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ShoppingListItem, newItem: ShoppingListItem): Boolean {
        return oldItem==newItem
    }
}

class ShoppingListItemAdapter(val launchDetailView:(ShoppingListItem)->Unit) :
    ListAdapter<ShoppingListItem, ShoppingListItemHolder>(
        ShoppingListItemDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListItemHolder {
        return ShoppingListItemHolder(TextView(parent.context)
//            LayoutInflater.from(parent.context).inflate(
//                R.layout.view_shopping_list_item_preview, parent, false
//            )
        )
    }

    override fun onBindViewHolder(holder: ShoppingListItemHolder, position: Int) {
        val shoppingList = getItem(position)!!
        holder.bind(shoppingList)
        holder.itemView.setOnClickListener { launchDetailView(shoppingList) }
    }
}

class ShoppingListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    /*private val tv_sl_title_text: TextView = itemView.findViewById(
        R.id.tv_sl_title_text
    )
    private val tv_exp_range_text: TextView = itemView.findViewById(
        R.id.tv_exp_range_text
    )
    private val tv_sl_item_count_text: TextView = itemView.findViewById(
        R.id.tv_sl_item_count_text
    )
    private val tv_sl_deadline_text: TextView = itemView.findViewById(
        R.id.tv_sl_deadline_text
    )*/

    fun bind(shoppingListItem: ShoppingListItem) {
        (itemView as TextView).text = shoppingListItem.toString()
        /*tv_sl_title_text.text = shoppingList.title
        tv_sl_item_count_text.text = getLangBasedNumberString(shoppingList.getShoppingListItemIds()?.size?.toString() ?: "0")
        if (shoppingList.deadLine !=null){
            tv_sl_deadline_text.text = DateUtils.getTimeString(shoppingList.deadLine!!,itemView.context.getString(R.string.exp_entry_time_format))
                                            .let {
                                                return@let when (checkIfEnglishLanguageSelected()) {
                                                    true -> it
                                                    false -> TranslatorUtils.englishToBanglaDateString(it)
                                                }
                                            }
            tv_sl_deadline_text.show()
        }else{
            tv_sl_deadline_text.hide()
        }*/
    }
}