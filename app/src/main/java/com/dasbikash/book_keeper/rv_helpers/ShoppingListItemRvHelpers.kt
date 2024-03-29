package com.dasbikash.book_keeper.rv_helpers

import android.content.Context
import android.graphics.Color
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
import com.dasbikash.book_keeper.utils.getCurrencyStringWithSymbol
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick

object ShoppingListItemDiffCallback: DiffUtil.ItemCallback<ShoppingListItem>(){
    override fun areItemsTheSame(oldItem: ShoppingListItem, newItem: ShoppingListItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ShoppingListItem, newItem: ShoppingListItem): Boolean {
        return oldItem==newItem
    }
}

class ShoppingListItemAdapter(
                        private val shoppingList: ShoppingList,
                        private val launchDetailView:(ShoppingListItem)->Unit,
                        private val closeTask:(ShoppingListItem)->Unit,
                        private val editTask:(ShoppingListItem)->Unit,
                        private val deleteTask:(ShoppingListItem)->Unit)
    :ListAdapter<ShoppingListItem, ShoppingListItemHolder>(ShoppingListItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListItemHolder {
        return ShoppingListItemHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_shopping_list_item_preview, parent, false
            ),shoppingList,closeTask,editTask, deleteTask
        )
    }

    override fun onBindViewHolder(holder: ShoppingListItemHolder, position: Int) {
        getItem(position)?.apply {
            holder.bind(this)
            holder.itemView.setOnClickListener { launchDetailView(this) }
        }
    }
}

class ShoppingListItemHolder(itemView: View,
                             val shoppingList: ShoppingList,
                             private val closeTask:(ShoppingListItem)->Unit,
                             private val editTask:(ShoppingListItem)->Unit,
                             private val deleteTask:(ShoppingListItem)->Unit) : RecyclerView.ViewHolder(itemView) {

    private val iv_tick_mark: ImageView = itemView.findViewById(R.id.iv_tick_mark)
    private val iv_sli_options: ImageView = itemView.findViewById(R.id.iv_sli_options)
    private val tv_sli_name: TextView = itemView.findViewById(R.id.tv_sli_name)
    private val tv_sli_category: TextView = itemView.findViewById(R.id.tv_sli_category)
    private val tv_sli_details: TextView = itemView.findViewById(R.id.tv_sli_details)
    private val tv_sli_price_range: TextView = itemView.findViewById(R.id.tv_sli_price_range)
    private val tv_sli_qty: TextView = itemView.findViewById(R.id.tv_sli_qty)
    private val tv_sli_uom: TextView = itemView.findViewById(R.id.tv_sli_uom)
    private val sli_details_holder: ViewGroup = itemView.findViewById(R.id.sli_details_holder)
    private val sli_price_range_holder: ViewGroup = itemView.findViewById(R.id.sli_price_range_holder)
    private val preview_holder: ViewGroup = itemView.findViewById(R.id.preview_holder)

    private lateinit var mShoppingListItem: ShoppingListItem

    fun attachMenuTask(){
        iv_sli_options.attachMenuViewForClick(
            MenuView().apply {
                if (shoppingList.userId == AuthRepo.getUserId() ||
                    mShoppingListItem.creatorId == AuthRepo.getUserId()){
                    add(
                        MenuViewItem(
                            text = itemView.context.getString(R.string.edit),
                            task = { editTask(mShoppingListItem) }
                        )
                    )

                    add(
                        MenuViewItem(
                            text = itemView.context.getString(R.string.delete),
                            task = { deleteTask(mShoppingListItem) }
                        )
                    )
                }
                add(
                    MenuViewItem(
                        text = itemView.context.getString(R.string.mark_as_close),
                        task = { closeTask(mShoppingListItem) }
                    )
                )
            }
        )
    }

    fun bind(shoppingListItem: ShoppingListItem) {
        this.mShoppingListItem = shoppingListItem
        iv_sli_options.hide()
        sli_details_holder.hide()
        sli_price_range_holder.hide()
        getExpenseCategory(itemView.context,shoppingListItem.categoryId).let {
            tv_sli_category.text = it
        }
        tv_sli_name.text = shoppingListItem.name
        shoppingListItem.details?.let {
            tv_sli_details.text = it
            sli_details_holder.show()
        }

        shoppingListItem.calculatePriceRange().apply {
            val priceBuilder = StringBuilder("")
            first?.let {
                priceBuilder.append(it.getCurrencyStringWithSymbol(itemView.context))
                if (second!=null){
                    priceBuilder.append(" - ")
                }
            }
            second?.let {
                priceBuilder.append(it.getCurrencyStringWithSymbol(itemView.context))
            }
            val priceText = priceBuilder.toString()
            tv_sli_price_range.text = priceText
            sli_price_range_holder.show()
        }

        tv_sli_qty.text = shoppingListItem.qty.toString()
        tv_sli_uom.text = itemView.resources.getStringArray(R.array.uoms).get(shoppingListItem.uom)
        if (shoppingListItem.expenseEntryId==null){
            iv_sli_options.show()
            attachMenuTask()
            iv_tick_mark.hide()
        }else{
            iv_sli_options.hide()
            markPurchased()
        }
    }

    private fun markPurchased() {
        iv_tick_mark.show()
        iv_tick_mark.bringToFront()
        preview_holder.setBackgroundColor(Color.GREEN)
    }

    companion object{
        private val expenseCategories = mutableListOf<String>()

        private fun getExpenseCategory(context:Context,categoryId:Int):String{
            if (expenseCategories.isEmpty()){
                expenseCategories.addAll(context.resources.getStringArray(R.array.expense_categories))
            }
            return expenseCategories.get(categoryId)
        }
    }
}