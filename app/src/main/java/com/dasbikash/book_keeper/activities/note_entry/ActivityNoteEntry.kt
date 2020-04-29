package com.dasbikash.book_keeper.activities.note_entry

import android.content.Context
import android.content.Intent
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.model.NoteEntry

class ActivityNoteEntry: ActivityTemplate() {

    override fun registerDefaultFragment(): FragmentTemplate {

        return when{
            isCreateIntent() -> FragmentNoteEntry.getCreateInstance()
            isEditIntent() -> FragmentNoteEntry.getEditInstance(getNoteEntryId())
            isViewIntent() -> FragmentNoteEntry.getViewInstance(getNoteEntryId())
            else -> TODO()
        }
    }

    private fun getNoteEntryId() = intent.getStringExtra(EXTRA_NOTE_ENTRY_ID)!!

    private fun isCreateIntent() = intent?.hasExtra(EXTRA_CREATE_MODE)==true
    private fun isEditIntent() = intent?.hasExtra(EXTRA_EDIT_MODE)==true
    private fun isViewIntent() = intent?.hasExtra(EXTRA_VIEW_MODE)==true

    companion object {

        private const val EXTRA_NOTE_ENTRY_ID =
            "com.dasbikash.book_keeper.activities.note_entry.ActivityNoteEntry.EXTRA_NOTE_ENTRY_ID"
        private const val EXTRA_CREATE_MODE =
            "com.dasbikash.book_keeper.activities.note_entry.ActivityNoteEntry.EXTRA_CREATE_MODE"
        private const val EXTRA_EDIT_MODE =
            "com.dasbikash.book_keeper.activities.note_entry.ActivityNoteEntry.EXTRA_EDIT_MODE"
        private const val EXTRA_VIEW_MODE =
            "com.dasbikash.book_keeper.activities.note_entry.ActivityNoteEntry.EXTRA_VIEW_MODE"

        fun getCreateIntent(context: Context): Intent {
            val intent = Intent(context.applicationContext, ActivityNoteEntry::class.java)
            intent.putExtra(EXTRA_CREATE_MODE,EXTRA_CREATE_MODE)
            return intent
        }

        fun getViewIntent(context: Context,noteEntry: NoteEntry): Intent {
            val intent = Intent(context.applicationContext, ActivityNoteEntry::class.java)
            intent.putExtra(EXTRA_VIEW_MODE,EXTRA_VIEW_MODE)
            intent.putExtra(EXTRA_NOTE_ENTRY_ID,noteEntry.id)
            return intent
        }

        fun getEditIntent(context: Context,noteEntry: NoteEntry): Intent {
            val intent = Intent(context.applicationContext, ActivityNoteEntry::class.java)
            intent.putExtra(EXTRA_EDIT_MODE,EXTRA_EDIT_MODE)
            intent.putExtra(EXTRA_NOTE_ENTRY_ID,noteEntry.id)
            return intent
        }
    }
}