package com.dasbikash.book_keeper.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.utils.get2DecPoints
import com.dasbikash.book_keeper.utils.getCurrencyStringWithSymbol
import com.dasbikash.book_keeper.utils.toTranslatedString
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick

object ExpenseEntryDiffCallback: DiffUtil.ItemCallback<ExpenseEntry>(){
    override fun areItemsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ExpenseEntry, newItem: ExpenseEntry): Boolean {
        return oldItem==newItem
    }
}

class ExpenseEntryAdapter(val launchDetailView:(ExpenseEntry)->Unit,
                          val editTask:(ExpenseEntry)->Unit,
                          val deleteTask:(ExpenseEntry)->Unit,
                          val doOnBottomBind:()->Unit) :
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
        getItem(position)?.apply {
            holder.bind(this)
            if (position == itemCount + LOAD_REQUEST_POSITION) {
                doOnBottomBind()
            }
            holder.itemView.setOnClickListener { launchDetailView(this) }
        }
    }

    companion object{
        private val LOAD_REQUEST_POSITION = -3
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
//        debugLog(expenseEntry)
        this.expenseEntry = expenseEntry
        expenseEntry.apply {
            tv_entry_time_text.text = time!!.toDate().toTranslatedString(itemView.context)
            tv_exp_amount_text.text = (totalExpense ?: 0.0).getCurrencyStringWithSymbol(itemView.context)
            tv_exp_desc_text.text = details
            tv_exp_cat_text.text = categoryId.let { itemView.context.resources.getStringArray(R.array.expense_categories).get(it)}
        }
//        itemView.setOnClickListener { debugLog(expenseEntry) }
    }
}

class GuestExpenseEntryAdapter() :
    ListAdapter<ExpenseEntry, GuestExpenseEntryHolder>( ExpenseEntryDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestExpenseEntryHolder {
        return GuestExpenseEntryHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_guest_exp_entry, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: GuestExpenseEntryHolder, position: Int) {
        getItem(position)?.apply {
            holder.bind(this)
        }
    }
}

class GuestExpenseEntryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    private lateinit var expenseEntry: ExpenseEntry

    fun getEntry():ExpenseEntry = this.expenseEntry

    fun bind(expenseEntry: ExpenseEntry) {
//        debugLog(expenseEntry)
        this.expenseEntry = expenseEntry
        expenseEntry.apply {
            tv_entry_time_text.text = time!!.toDate().toTranslatedString(itemView.context)
            tv_exp_amount_text.text = (totalExpense ?: 0.0).getCurrencyStringWithSymbol(itemView.context)
            tv_exp_desc_text.text = details
            tv_exp_cat_text.text = categoryId.let { itemView.context.resources.getStringArray(R.array.expense_categories).get(it)}
        }
//        itemView.setOnClickListener { debugLog(expenseEntry) }
    }
}

class GuestExpEntryRemoveCallback(val dragSwipeAction: (ExpenseEntry)->Unit) :
    ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.RIGHT) {
        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            dragSwipeAction((viewHolder as GuestExpenseEntryHolder).getEntry())
        }
}