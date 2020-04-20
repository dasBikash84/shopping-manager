package com.dasbikash.book_keeper.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.models.TbaSlShareReq
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick

object TbaSlShareReqDiffCallback : DiffUtil.ItemCallback<TbaSlShareReq>() {
    override fun areItemsTheSame(oldItem: TbaSlShareReq, newItem: TbaSlShareReq) =
        oldItem.onlineSlShareReq.id == newItem.onlineSlShareReq.id

    override fun areContentsTheSame(oldItem: TbaSlShareReq, newItem: TbaSlShareReq): Boolean {
        return oldItem == newItem
    }
}

class TbaSlShareReqListAdapter(
    private val approveTask: (TbaSlShareReq) -> Unit,
    private val declineTask: (TbaSlShareReq) -> Unit
) :
    ListAdapter<TbaSlShareReq, TbaSlShareReqListHolder>(
        TbaSlShareReqDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TbaSlShareReqListHolder {
        return TbaSlShareReqListHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_tba_shopping_list_preview, parent, false
            ), approveTask, declineTask
        )
    }

    override fun onBindViewHolder(holder: TbaSlShareReqListHolder, position: Int) {
        getItem(position).let { holder.bind(it) }
    }
}

class TbaSlShareReqListHolder(
    itemView: View,
    val approveTask: (TbaSlShareReq) -> Unit,
    val declineTask: (TbaSlShareReq) -> Unit
) : RecyclerView.ViewHolder(itemView) {

    private val tv_sl_title_text: TextView = itemView.findViewById(
        R.id.tv_sl_title_text
    )
    private val tv_partner_details_text: TextView = itemView.findViewById(
        R.id.tv_partner_details_text
    )
    private val tv_req_time: TextView = itemView.findViewById(
        R.id.tv_req_time
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
        tv_partner_details_text.text = getUserDetails(tbaSlShareReq.requester)
        tv_req_time.text = DateUtils
                                .getTimeString(tbaSlShareReq.onlineSlShareReq.modified.toDate(),itemView.context.getString(R.string.exp_entry_time_format))
                                .let {
                                    if (checkIfEnglishLanguageSelected()){
                                        it
                                    }else{
                                        TranslatorUtils.englishToBanglaDateString(it)
                                    }
                                }
    }

    private fun getUserDetails(user: User): String {
        val userDetails = StringBuilder("")
        user.apply {
            if (firstName != null || lastName != null) {
                userDetails.append(itemView.context.getString(R.string.name_prompt))
                firstName?.let {
                    userDetails.append(" ")
                    userDetails.append(it.trim())
                    userDetails.append(" ")
                }
                lastName?.let {
                    userDetails.append(" ")
                    userDetails.append(it.trim())
                }
            }
            email?.let {
                userDetails.append("\n")
                userDetails.append(itemView.context.getString(R.string.email_hint))
                userDetails.append(": ")
                userDetails.append(it.trim())
            }
            phone?.let {
                userDetails.append("\n")
                userDetails.append(itemView.context.getString(R.string.phone_hint))
                userDetails.append(": ")
                userDetails.append(it.trim())
            }
        }

        return userDetails.toString()
    }
}