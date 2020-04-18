package com.dasbikash.book_keeper.activities.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.GuestExpEntryRemoveCallback
import com.dasbikash.book_keeper.rv_helpers.GuestExpenseEntryAdapter
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ExpenseRepo
import com.dasbikash.book_keeper_repo.model.ExpenseEntry
import kotlinx.android.synthetic.main.fragment_handle_guest_data.*
import kotlinx.coroutines.launch

class FragmentHandleGuestData : FragmentTemplate() {

    private val guestExpenseEntryAdapter = GuestExpenseEntryAdapter()
    private val guestEntries = mutableListOf<ExpenseEntry>()

    private var exitPromptText:String?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_handle_guest_data, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_guest_exp_entry.adapter = guestExpenseEntryAdapter
        btn_exit.setOnClickListener { exitTask() }
        btn_save.setOnClickListener { saveAndExit() }
        btn_reset.setOnClickListener { guestExpenseEntryAdapter.submitList(guestEntries.toList()) }
        runWithContext {
            lifecycleScope.launch {
                guestEntries.addAll(ExpenseRepo.getGuestData(it))
                guestExpenseEntryAdapter.submitList(guestEntries.toList())
            }
        }
        exitPromptText = getString(R.string.import_guest_data_exit_prompt)
        ItemTouchHelper(GuestExpEntryRemoveCallback({removeExpEntry(it)})).attachToRecyclerView(rv_guest_exp_entry)
    }

    private fun removeExpEntry(expenseEntry: ExpenseEntry){
        guestExpenseEntryAdapter
            .currentList
            .toMutableList().let {
                it.remove(expenseEntry)
                guestExpenseEntryAdapter.submitList(it.toList())
            }
    }

    private fun saveAndExit() {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.guest_data_save_exit_message),
                doOnPositivePress = {
                    saveAndExitTask()
                }
            ))
        }
    }

    private fun saveAndExitTask() {
        runWithContext {
            lifecycleScope.launch {
                val entriesForSave = guestExpenseEntryAdapter.currentList
                guestEntries
                    .filter { !entriesForSave.contains(it) }
                    .asSequence()
                    .forEach {
                        ExpenseRepo.delete(context!!, it)
                    }
                entriesForSave.asSequence().forEach {
                    it.userId = AuthRepo.getUserId()
                    ExpenseRepo.saveExpenseEntry(context!!,it)
                }
                exitPromptText = null
                activity?.onBackPressed()
            }
        }
    }

    private fun exitTask() {
        exitPromptText = getString(R.string.import_guest_data_exit_prompt)
        activity?.onBackPressed()
    }
    override fun getPageTitle(context: Context): String? {
        return context.getString(R.string.import_guest_data)
    }
    override fun getExitPrompt(): String? {
        return exitPromptText
    }
}
