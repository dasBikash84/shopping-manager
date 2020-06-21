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
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.toTranslatedString
import com.dasbikash.book_keeper_repo.model.NoteEntry
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.menu_view.attachMenuViewForClick

object NoteEntryDiffCallback: DiffUtil.ItemCallback<NoteEntry>(){
    override fun areItemsTheSame(oldItem: NoteEntry, newItem: NoteEntry) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: NoteEntry, newItem: NoteEntry): Boolean {
        return oldItem==newItem
    }
}

class NoteEntryPreviewAdapter(private val itemClickAction:(NoteEntry)->Unit,
                              private val deleteAction:(NoteEntry)->Unit,
                              private val editAction:(NoteEntry)->Unit) :
    ListAdapter<NoteEntry, NoteEntryPreviewHolder>(
        NoteEntryDiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteEntryPreviewHolder {
        return NoteEntryPreviewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.view_note_preview, parent, false
            ),deleteAction,editAction
        )
    }

    override fun onBindViewHolder(holder: NoteEntryPreviewHolder, position: Int) {
        getItem(position)?.apply {
            holder.bind(this)
            holder.itemView.setOnClickListener { itemClickAction(this) }
        }
    }
}

class NoteEntryPreviewHolder(itemView: View,
                             deleteAction:(NoteEntry)->Unit,
                             editAction:(NoteEntry)->Unit) : RecyclerView.ViewHolder(itemView) {

    private val tv_title_text: TextView = itemView.findViewById(
        R.id.tv_title_text
    )
    private val tv_note_text_preview: TextView = itemView.findViewById(
        R.id.tv_note_text_preview
    )
    private val tv_note_time: TextView = itemView.findViewById(
        R.id.tv_note_time
    )
    private val btn_options: ImageView = itemView.findViewById(
        R.id.btn_options
    )

    init {
        MenuView().apply {
            add(
                MenuViewItem(
                    text = itemView.context.getString(R.string.edit),
                    task = {editAction(noteEntry)}
                )
            )
            add(
                MenuViewItem(
                    text = itemView.context.getString(R.string.delete),
                    task = {deleteAction(noteEntry)}
                )
            )
        }.let {
            btn_options.attachMenuViewForClick(it)
        }
    }

    private lateinit var noteEntry: NoteEntry

    fun bind(noteEntry: NoteEntry) {
        this.noteEntry = noteEntry
        tv_title_text.text = noteEntry.title
        tv_note_text_preview.text = noteEntry.note
        tv_note_time.text = noteEntry.modified.toDate().toTranslatedString(itemView.context)
    }
}