package com.dasbikash.book_keeper.activities.note_entry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dasbikash.pop_up_message.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.hideKeyboard
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.NoteEntryRepo
import com.dasbikash.book_keeper_repo.model.NoteEntry
import com.dasbikash.pop_up_message.showShortSnack
import kotlinx.android.synthetic.main.fragment_note_entry.*
import kotlinx.coroutines.launch

class FragmentNoteEntry() : FragmentTemplate() {

    private lateinit var noteEntry: NoteEntry

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when{
            isCreateInstance() -> {
                noteEntry = NoteEntry(
                    userId = AuthRepo.getUserId(),
                    active = true
                )
                showEditBlock()
                debugLog("isCreateInstance")
            }
            isEditInstance() -> {
                debugLog("isEditInstance: ${getNoteEntryId()}")
                runWithContext {
                    lifecycleScope.launchWhenCreated {
                        NoteEntryRepo.findNoteEntry(it,getNoteEntryId())!!.let {
                            noteEntry = it
                            setEditBlock()
                        }
                    }
                }
            }
            isViewInstance() -> {
                debugLog("isViewInstance ${getNoteEntryId()}")
                runWithContext {
                    lifecycleScope.launchWhenCreated {
                        NoteEntryRepo.findNoteEntry(it,getNoteEntryId())!!.let {
                            noteEntry = it
                            setViewBlock()
                        }
                    }
                }
            }
            else -> TODO()
        }

        display_block_holder.setOnClickListener {
            if (isViewInstance()){
                setEditBlock()
            }
        }

        btn_save.setOnClickListener {
            hideKeyboard()
            saveAction()
        }

        btn_cancel.setOnClickListener {
            hideKeyboard()
            activity?.onBackPressed()
        }
    }

    private fun saveAction() {

        if (et_title_text.text.isNullOrBlank()){
            showShortSnack(R.string.note_title_blank_error)
            return
        }

        if (et_note_content_text.text.isNullOrBlank()){
            showShortSnack(R.string.note_content_blank_error)
            return
        }

        if (!checkIfContentAdded()){
            activity?.finish()
            return
        }

        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.save_note_prompt),
                doOnPositivePress = {
                    noteEntry.title = et_title_text.text.toString().trim()
                    noteEntry.note = et_note_content_text.text.toString().trim()
                    lifecycleScope.launch {
                        if (isCreateInstance()) {
                            NoteEntryRepo.addNew(it,noteEntry)
                        }else{
                            NoteEntryRepo.update(it,noteEntry)
                        }
                        activity?.finish()
                    }
                }
            ))
        }
    }

    override fun getExitPrompt(): String? {
        if (!checkIfContentAdded()){
            return null
        }
        if (isCreateInstance() || isEditInstance()){
            return context?.getString(R.string.discard_and_exit_prompt)
        }
        return null
    }

    private fun checkIfContentAdded():Boolean{
        if (isCreateInstance()){
            if (et_title_text.text.isNullOrBlank() &&
                et_note_content_text.text.isNullOrBlank()){
                return false
            }
        }else if(isEditInstance()) {
            if (et_title_text.text.toString().trim() == noteEntry.title &&
                et_note_content_text.text.toString().trim() == noteEntry.note
            ) {
                return false
            }
        }
        return true
    }

    private fun setViewBlock() {
        tv_title_text.setText(noteEntry.title)
        tv_note_text.setText(noteEntry.note)
        showViewBlock()
    }

    private fun showViewBlock() {
        edit_block_holder.hide()
        display_block_holder.show()
    }

    private fun setEditBlock() {
        et_title_text.setText(noteEntry.title)
        et_note_content_text.setText(noteEntry.note)
        showEditBlock()
    }

    private fun showEditBlock() {
        edit_block_holder.show()
        display_block_holder.hide()
    }

    override fun getPageTitle(context: Context): String? {
        return context.getString(R.string.bmi_notepad)
    }

    private fun getNoteEntryId() = arguments?.getString(ARG_NOTE_ENTRY_ID)!!

    private fun isCreateInstance() = arguments?.containsKey(ARG_CREATE_MODE)==true
    private fun isEditInstance() = arguments?.containsKey(ARG_EDIT_MODE)==true || edit_block_holder.isVisible
    private fun isViewInstance() = arguments?.containsKey(ARG_VIEW_MODE)==true

    companion object{

        private const val ARG_NOTE_ENTRY_ID =
            "com.dasbikash.book_keeper.activities.note_entry.FragmentNoteEntry.ARG_NOTE_ENTRY_ID"
        private const val ARG_CREATE_MODE =
            "com.dasbikash.book_keeper.activities.note_entry.FragmentNoteEntry.ARG_CREATE_MODE"
        private const val ARG_EDIT_MODE =
            "com.dasbikash.book_keeper.activities.note_entry.FragmentNoteEntry.ARG_EDIT_MODE"
        private const val ARG_VIEW_MODE =
            "com.dasbikash.book_keeper.activities.note_entry.FragmentNoteEntry.ARG_VIEW_MODE"

        fun getCreateInstance(): FragmentNoteEntry {
            val arg = Bundle()
            arg.putSerializable(ARG_CREATE_MODE,ARG_CREATE_MODE)
            val fragment = FragmentNoteEntry()
            fragment.arguments = arg
            return fragment
        }

        fun getViewInstance(noteEntryId: String): FragmentNoteEntry {
            val arg = Bundle()
            arg.putSerializable(ARG_VIEW_MODE,ARG_VIEW_MODE)
            arg.putSerializable(ARG_NOTE_ENTRY_ID,noteEntryId)
            val fragment = FragmentNoteEntry()
            fragment.arguments = arg
            return fragment
        }

        fun getEditInstance(noteEntryId: String): FragmentNoteEntry {
            val arg = Bundle()
            arg.putSerializable(ARG_EDIT_MODE,ARG_EDIT_MODE)
            arg.putSerializable(ARG_NOTE_ENTRY_ID,noteEntryId)
            val fragment = FragmentNoteEntry()
            fragment.arguments = arg
            return fragment
        }
    }
}
