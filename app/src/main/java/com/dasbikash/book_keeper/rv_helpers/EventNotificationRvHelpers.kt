package com.dasbikash.book_keeper.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper_repo.model.EventNotification

object EventNotificationDiffCallback: DiffUtil.ItemCallback<EventNotification>(){
    override fun areItemsTheSame(oldItem: EventNotification, newItem: EventNotification) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: EventNotification, newItem: EventNotification): Boolean {
        return oldItem==newItem
    }
}

class EventNotificationAdapter(val itemClickAction:(EventNotification)->Unit) :
    ListAdapter<EventNotification, EventNotificationEntryHolder>(
        EventNotificationDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventNotificationEntryHolder {
        return EventNotificationEntryHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_event_notification, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: EventNotificationEntryHolder, position: Int) {
        getItem(position)?.apply {
            holder.bind(this)
            holder.itemView.setOnClickListener { itemClickAction(this) }
        }
    }
}

class EventNotificationEntryHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tv_event_title_text: TextView = itemView.findViewById(
        R.id.tv_event_title_text
    )
    private val tv_event_desc_text: TextView = itemView.findViewById(
        R.id.tv_event_desc_text
    )
    private val tv_event_time_text: TextView = itemView.findViewById(
        R.id.tv_event_time_text
    )

    private lateinit var eventNotification: EventNotification

    fun bind(eventNotification: EventNotification) {
        this.eventNotification = eventNotification
        tv_event_title_text.text = eventNotification.title
        tv_event_desc_text.text = eventNotification.description
        tv_event_time_text.text = DateUtils.getTimeString(eventNotification.created!!.toDate(),itemView.context.getString(R.string.exp_entry_time_format)).let {
            if (!checkIfEnglishLanguageSelected()){
                TranslatorUtils.englishToBanglaDateString(it)
            }else{
                it
            }
        }
    }

    fun getEntry():EventNotification = eventNotification
}

class EventNotificationRemoveCallback(val dragSwipeAction: (EventNotification)->Unit) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        dragSwipeAction((viewHolder as EventNotificationEntryHolder).getEntry())
    }
}