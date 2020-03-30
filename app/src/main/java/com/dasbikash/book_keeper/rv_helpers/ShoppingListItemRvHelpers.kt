package com.dasbikash.book_keeper.rv_helpers

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper_repo.SettingsRepo
import com.dasbikash.book_keeper_repo.model.ExpenseCategory
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import kotlin.random.Random

object ShoppingListItemDiffCallback: DiffUtil.ItemCallback<ShoppingListItem>(){
    override fun areItemsTheSame(oldItem: ShoppingListItem, newItem: ShoppingListItem) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ShoppingListItem, newItem: ShoppingListItem): Boolean {
        return oldItem==newItem
    }
}

class ShoppingListItemAdapter(
                        val launchDetailView:(ShoppingListItem)->Unit,
                        val editTask:(ShoppingListItem)->Unit,
                        val deleteTask:(ShoppingListItem)->Unit,
                        val closeTask:(ShoppingListItem)->Unit)
    :ListAdapter<ShoppingListItem, ShoppingListItemHolder>(ShoppingListItemDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListItemHolder {
        return ShoppingListItemHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_shopping_list_item_preview, parent, false
            ),editTask, deleteTask, closeTask
        )
    }

    override fun onBindViewHolder(holder: ShoppingListItemHolder, position: Int) {
        val shoppingList = getItem(position)!!
        holder.bind(shoppingList)
        holder.itemView.setOnClickListener { launchDetailView(shoppingList) }
    }
}

class ShoppingListItemHolder(itemView: View,
                             val editTask:(ShoppingListItem)->Unit,
                             val deleteTask:(ShoppingListItem)->Unit,
                             val closeTask:(ShoppingListItem)->Unit) : RecyclerView.ViewHolder(itemView) {

    private val iv_sli_options: ImageView = itemView.findViewById(R.id.iv_sli_options)
    private val tv_sli_name: TextView = itemView.findViewById(R.id.tv_sli_name)
    private val tv_sli_category: TextView = itemView.findViewById(R.id.tv_sli_category)
    private val tv_sli_details: TextView = itemView.findViewById(R.id.tv_sli_details)
    private val tv_sli_price_range: TextView = itemView.findViewById(R.id.tv_sli_price_range)
    private val tv_sli_qty: TextView = itemView.findViewById(R.id.tv_sli_qty)
    private val tv_sli_uom: TextView = itemView.findViewById(R.id.tv_sli_uom)
    private val sli_details_holder: ViewGroup = itemView.findViewById(R.id.sli_details_holder)
    private val sli_price_range_holder: ViewGroup = itemView.findViewById(R.id.sli_price_range_holder)

    private lateinit var mShoppingListItem: ShoppingListItem

    init {
        val menuViewItems = listOf<MenuViewItem>(
            MenuViewItem(
                text = itemView.context.getString(R.string.edit),
                task = { editTask(mShoppingListItem) }
            ),
            MenuViewItem(
                text = itemView.context.getString(R.string.delete),
                task = { deleteTask(mShoppingListItem) }
            ),
            MenuViewItem(
                text = itemView.context.getString(R.string.mark_as_close),
                task = { closeTask(mShoppingListItem) }
            )
        )
        val menuView = MenuView()
        menuView.addAll(menuViewItems)
        iv_sli_options.attachMenuViewForClick(menuView)
    }

    fun bind(shoppingListItem: ShoppingListItem) {
        this.mShoppingListItem = shoppingListItem
        iv_sli_options.hide()
        sli_details_holder.hide()
        sli_price_range_holder.hide()
        GlobalScope.launch {
            delay(Random(System.currentTimeMillis()).nextLong(100L))
            getExpenseCategory(itemView.context,shoppingListItem.categoryId!!).let {
                runOnMainThread({
                    tv_sli_category.text = if(checkIfEnglishLanguageSelected()) {it.name} else {it.nameBangla}
                })
            }
        }
        tv_sli_name.text = shoppingListItem.name
        shoppingListItem.details?.let {
            tv_sli_details.text = it
            sli_details_holder.show()
        }

        if (shoppingListItem.minUnitPrice!=null ||
            shoppingListItem.maxUnitPrice!=null){
            val priceBuilder = StringBuilder("")
            shoppingListItem.minUnitPrice?.let {
                priceBuilder.append(it.toString())
                if (shoppingListItem.maxUnitPrice!=null) {
                    priceBuilder.append(" - ")
                }
            }
            shoppingListItem.maxUnitPrice?.let {
                priceBuilder.append(it.toString())
            }
            val priceText = priceBuilder.toString().let {
                if (checkIfEnglishLanguageSelected()) {it} else {TranslatorUtils.englishToBanglaNumberString(it)}
            }
            tv_sli_price_range.text = priceText
            sli_price_range_holder.show()
        }

        tv_sli_qty.text = shoppingListItem.qty.toString().let {
            if (checkIfEnglishLanguageSelected()) {it} else {TranslatorUtils.englishToBanglaNumberString(it)}
        }
        tv_sli_uom.text = if (checkIfEnglishLanguageSelected()) {shoppingListItem.uom} else {shoppingListItem.uomBangla}
        if (shoppingListItem.expenseEntryId==null){
            iv_sli_options.show()
        }else{
            iv_sli_options.hide()
        }
    }

    companion object{
        private val expenseCategories = mutableSetOf<ExpenseCategory>()

        private suspend fun getExpenseCategory(context:Context,categoryId:String):ExpenseCategory{
            if (expenseCategories.isEmpty()){
                expenseCategories.addAll(SettingsRepo.getAllExpenseCategories(context))
            }
            return expenseCategories.find { it.id==categoryId }!!
        }
    }
}