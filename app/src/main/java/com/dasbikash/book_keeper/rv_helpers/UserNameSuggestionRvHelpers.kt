package com.dasbikash.book_keeper.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dasbikash.book_keeper.R

object StringDiffCallback: DiffUtil.ItemCallback<String>(){
    override fun areItemsTheSame(oldItem: String, newItem: String) = oldItem == newItem
    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem==newItem
    }
}

class StringListAdapter(private val textClickAction: ((String)->Unit)?=null) :
    ListAdapter<String, StringHolder>(
        StringDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringHolder {
        return StringHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_single_line_text,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: StringHolder, position: Int) {
        val item = getItem(position)
        textClickAction?.apply {
            holder.itemView.setOnClickListener {
                this.invoke(item)
            }
        }
        holder.bind(item)
    }
}

class StringHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val tv_single_line: TextView = itemView.findViewById(R.id.tv_single_line)

    fun bind(text:String) {
        tv_single_line.text = text.trim()
    }
}