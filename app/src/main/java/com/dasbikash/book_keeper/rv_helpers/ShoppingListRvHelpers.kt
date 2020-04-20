package com.dasbikash.book_keeper.rv_helpers

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_extensions.show
import com.dasbikash.async_manager.AsyncTaskManager
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.ShoppingListUtils
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.utils.getDayCount
import com.dasbikash.menu_view.attachMenuViewForClick
import kotlinx.coroutines.runBlocking
import java.util.*

object ShoppingListDiffCallback: DiffUtil.ItemCallback<ShoppingList>(){
    override fun areItemsTheSame(oldItem: ShoppingList, newItem: ShoppingList) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ShoppingList, newItem: ShoppingList): Boolean {
        return oldItem==newItem
    }
}

class ShoppingListAdapter(val launchDetailView:(ShoppingList)->Unit) :
    ListAdapter<ShoppingList, ShoppingListHolder>(
        ShoppingListDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListHolder {
        return ShoppingListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_shopping_list_preview, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ShoppingListHolder, position: Int) {
        getItem(position)?.apply {
            holder.bind(this)
            holder.itemView.setOnClickListener { launchDetailView(this) }
        }
    }
}

class ShoppingListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tv_sl_title_text: TextView = itemView.findViewById(
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
    )
    private val iv_options: ImageView = itemView.findViewById(
        R.id.iv_options
    )
    private val content_holder: ViewGroup = itemView.findViewById(
        R.id.content_holder
    )

    fun bind(shoppingList: ShoppingList) {
        tv_sl_title_text.text = shoppingList.title
        tv_sl_item_count_text.text = shoppingList.shoppingListItemIds?.size?.toString() ?: "0"
        if (shoppingList.deadLine !=null){
            tv_sl_deadline_text.text = DateUtils.getTimeString(shoppingList.deadLine!!.toDate(),itemView.context.getString(R.string.exp_entry_time_format))
                                            .let {
                                                return@let when (checkIfEnglishLanguageSelected()) {
                                                    true -> it
                                                    false -> TranslatorUtils.englishToBanglaDateString(it)
                                                }
                                            }
            tv_sl_deadline_text.show()
        }else{
            tv_sl_deadline_text.hide()
        }

        AsyncTaskManager.addTask<Unit,Unit> {
            runBlocking {
                val (minExp, maxExp) = ShoppingList.calculateExpenseRange(itemView.context, shoppingList)
                runOnMainThread({tv_exp_range_text.text = itemView.context.resources.getString(R.string.sl_price_range, minExp, maxExp)})
                setBgColor(shoppingList)
            }
        }

        iv_options.attachMenuViewForClick(ShoppingListUtils.getShareOptionsMenu(itemView.context,shoppingList))
    }

    private suspend fun setBgColor(shoppingList: ShoppingList) {
        when{
            ShoppingListRepo.checkIfAllBought(itemView.context,shoppingList) -> setBgColor(R.color.bg_sl_all_bought)
            shoppingList.deadLine !=null && shoppingList.deadLine!!.toDate().time < System.currentTimeMillis() -> setBgColor(R.color.bg_sl_dl_expired)
            shoppingList.deadLine !=null && shoppingList.deadLine!!.toDate().getDayCount() == Date().getDayCount() -> setBgColor(R.color.bg_sl_dl_today)
            else -> setBgColor(R.color.bg_sl_dl_active)
        }
    }

    private fun setBgColor(@ColorRes colorResId: Int) {
        runOnMainThread({
            content_holder.setBackgroundColor(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    itemView.context.resources.getColor(colorResId,null)
                }else{
                    @Suppress("DEPRECATION")
                    itemView.context.resources.getColor(colorResId)
                }
            )
        })
    }
}