package com.dasbikash.book_keeper.activities.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.NoteEntryPreviewAdapter
import com.dasbikash.book_keeper_repo.NoteEntryRepo
import com.dasbikash.book_keeper_repo.model.NoteEntry
import kotlinx.android.synthetic.main.fragment_note_pad.*

class FragmentNotePad : FragmentTemplate() {

    private val noteEntryPreviewAdapter = NoteEntryPreviewAdapter({detailViewAction(it)},{deleteAction(it)},{editAction(it)})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_pad, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_add_note.setOnClickListener { addNoteAction() }
        rv_note_list.adapter = noteEntryPreviewAdapter

        runWithContext {
            NoteEntryRepo.findAllLd(it).observe(this,object : Observer<List<NoteEntry>>{
                override fun onChanged(list: List<NoteEntry>?) {
                    (list ?: emptyList()).let {
                        it.forEach { debugLog("from fragment: ${it}") }
                        noteEntryPreviewAdapter.submitList(it)
                    }
                }
            })
        }
    }

    override fun getPageTitle(context: Context):String? = context.getString(R.string.bmi_notepad)

    private fun addNoteAction(){
        TODO()
    }

    private fun editAction(noteEntry: NoteEntry){
        TODO()
    }

    private fun deleteAction(noteEntry: NoteEntry){
        TODO()
    }

    private fun detailViewAction(noteEntry: NoteEntry){
        TODO()
    }
}
