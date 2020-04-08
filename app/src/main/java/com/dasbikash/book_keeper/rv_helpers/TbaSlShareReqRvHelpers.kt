package com.dasbikash.book_keeper.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.models.TbaSlShareReq
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick

object TbaSlShareReqDiffCallback: DiffUtil.ItemCallback<TbaSlShareReq>(){
    override fun areItemsTheSame(oldItem: TbaSlShareReq, newItem: TbaSlShareReq) = oldItem.onlineSlShareReq.id == newItem.onlineSlShareReq.id
    override fun areContentsTheSame(oldItem: TbaSlShareReq, newItem: TbaSlShareReq): Boolean {
        return oldItem==newItem
    }
}

class TbaSlShareReqListAdapter(private val launchDetailView:(ShoppingList)->Unit,
                               private val approveTask:(TbaSlShareReq)->Unit,
                               private val declineTask:(TbaSlShareReq)->Unit) :
    ListAdapter<TbaSlShareReq, TbaSlShareReqListHolder>(
        TbaSlShareReqDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TbaSlShareReqListHolder {
        return TbaSlShareReqListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_tba_shopping_list_preview, parent, false
            ),approveTask,declineTask
        )
    }

    override fun onBindViewHolder(holder: TbaSlShareReqListHolder, position: Int) {
        val tbaSlShareReq = getItem(position)!!
        holder.bind(tbaSlShareReq)
        holder.itemView.setOnClickListener { launchDetailView(tbaSlShareReq.shoppingList) }
    }
}

class TbaSlShareReqListHolder(itemView: View,
                              val approveTask:(TbaSlShareReq)->Unit,
                              val declineTask:(TbaSlShareReq)->Unit) : RecyclerView.ViewHolder(itemView) {

    private val tv_sl_title_text: TextView = itemView.findViewById(
        R.id.tv_sl_title_text
    )
    private val tv_partner_details_text: TextView = itemView.findViewById(
        R.id.tv_partner_details_text
    )
    private val iv_sli_options: ImageView = itemView.findViewById(
        R.id.iv_sli_options
    )

    private lateinit var mTbaSlShareReq: TbaSlShareReq

    init {
        val menuViewItems = listOf<MenuViewItem>(
            MenuViewItem(
                text = itemView.context.getString(R.string.approve),
                task = { approveTask(mTbaSlShareReq) }
            ),
            MenuViewItem(
                text = itemView.context.getString(R.string.decline),
                task = { declineTask(mTbaSlShareReq) }
            )
        )
        val menuView = MenuView()
        menuView.addAll(menuViewItems)
        iv_sli_options.attachMenuViewForClick(menuView)
    }

    fun bind(tbaSlShareReq: TbaSlShareReq) {
        mTbaSlShareReq = tbaSlShareReq
        tv_sl_title_text.text = tbaSlShareReq.shoppingList.title
        tv_partner_details_text.text = tbaSlShareReq.partner.detailsText()
    }
}