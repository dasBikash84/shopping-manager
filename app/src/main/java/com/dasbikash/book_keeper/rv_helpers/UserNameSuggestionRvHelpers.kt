package com.dasbikash.book_keeper.rv_helpers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
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

class StringListAdapter(private val bindTask:(View,String)->Unit,
                        private val textClickAction: ((String)->Unit)?=null,
                        @LayoutRes private val layoutId:Int = R.layout.view_single_line_text) :
    ListAdapter<String, StringHolder>(
        StringDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StringHolder {
        return StringHolder(
            LayoutInflater.from(parent.context).inflate(
                layoutId,
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
        bindTask(holder.itemView,item)
    }
}

class StringHolder(itemView: View) : RecyclerView.ViewHolder(itemView)