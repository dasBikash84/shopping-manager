package com.dasbikash.book_keeper.activities.shopping_list.edit

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.hideKeyboard
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.shopping_list.ActivityShoppingList
import com.dasbikash.book_keeper.activities.shopping_list.FragmentShoppingListDetails
import com.dasbikash.book_keeper.activities.shopping_list.view.FragmentShoppingListView
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.date_time_picker.DateTimePicker
import com.dasbikash.snackbar_ext.showShortSnack
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_shopping_list_edit.*
import kotlinx.coroutines.launch
import java.lang.IllegalStateException
import java.util.*

class FragmentShoppingListEdit : FragmentShoppingListDetails() {

    private lateinit var shoppingList: ShoppingList

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            context as ActivityShoppingList
        } catch (ex: Throwable) {
            throw IllegalStateException("Should only be attached to shopping list activity!!")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        et_shopping_list_name.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                (s?.toString() ?: "").let {
                    shoppingList.title = it.trim()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        sl_deadline_tv_holder.setOnClickListener {
            runWithContext {
                hideKeyboard()
                val dateTimePicker = DateTimePicker(
                    date = shoppingList.deadLine,
                    minDate = Date(),
                    doOnDateTimeSet = {
                        setDeadLine(it)
                    }
                )
                dateTimePicker.display(it)
            }
        }

        spinner_reminder_interval_selector.setItems(
            ShoppingList.Companion.ReminderInterval.values().map {
                if (checkIfEnglishLanguageSelected()) {
                    it.text
                } else {
                    it.textBangla
                }
            }
        )

        spinner_reminder_interval_selector.setOnItemSelectedListener(object :
            MaterialSpinner.OnItemSelectedListener<String> {
            override fun onItemSelected(
                view: MaterialSpinner?,
                position: Int,
                id: Long,
                item: String?
            ) {
                hideKeyboard()
                debugLog("item: $item")
                item?.let {
                    ShoppingList.Companion.ReminderInterval.values()
                        .find { it.text == item || it.textBangla == item }?.let {
                            shoppingList.setReminderInterval(it.intervalMs)
                            debugLog(shoppingList)
                        }
                }
            }
        })

        cb_set_sl_remainder.setOnCheckedChangeListener(object :
            CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
                hideKeyboard()
                when (isChecked) {
                    true -> sl_remainder_set_block.show()
                    false -> {
                        runWithContext {
                            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                                message = it.getString(R.string.disable_sl_reminder_prompt),
                                doOnPositivePress = {
                                    shoppingList.setReminderInterval(null)
                                    shoppingList.setCountDownTime(null)
                                    sl_remainder_set_block.hide()
                                },
                                doOnNegetivePress = { cb_set_sl_remainder.isChecked = true }
                            ))
                        }
                    }
                }
            }
        })

        et_sl_count_down.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                (s?.toString() ?: "").let {
                    if (it.isBlank()){
                        0
                    }else{
                        it.trim().toInt()
                    }.let { shoppingList.setCountDownTime(it * DateUtils.MINUTE_IN_MS) }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btn_cancel.setOnClickListener {
            runWithContext {
                hideKeyboard()
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    message = it.getString(R.string.discard_and_exit_prompt),
                    doOnPositivePress = {
                        exit()
                    }
                ))
            }
        }

        btn_save_shopping_list.setOnClickListener {
            if (validateData()){
                hideKeyboard()
                runWithContext {
                    DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                        message = it.getString(R.string.save_shopping_list_prompt),
                        doOnPositivePress = {
                            saveAndExit()
                        }
                    ))
                }
            }
        }

        cb_set_sl_remainder.isChecked = false
        sl_remainder_set_block.hide()
    }

    private fun validateData(): Boolean {
        if (shoppingList.title.isNullOrBlank()){
            et_shopping_list_name.error = getString(R.string.shopping_list_name_error)
            return false
        }
        if (!shoppingList.validateCountDownTime()){
            et_sl_count_down.error = getString(R.string.sl_count_down_error)
            return false
        }
        return true
    }

    private fun saveAndExit() {
        runWithContext {
            lifecycleScope.launch {
                ShoppingListRepo.save(it, shoppingList)
                exit()
            }
        }
    }

    private fun exit() {
        (activity as ActivityShoppingList?)?.let{
            it.addFragmentClearingBackStack(FragmentShoppingListView.getInstance(getShoppingListId()))
        }
    }

    private fun setDeadLine(deadLine: Date) {
        if (ShoppingList.validateDeadLine(deadLine)) {
            shoppingList.deadLine = deadLine
            refreshView()
        } else {
            showShortSnack(getString(R.string.invalid_deadline_message))
        }
    }

    override fun onResume() {
        super.onResume()
        runWithContext {
            lifecycleScope.launch {
                if (!::shoppingList.isInitialized) {
                    shoppingList = ShoppingListRepo.findById(it, getShoppingListId())!!
                    (activity as ActivityShoppingList?)?.apply {
                        setPageTitle(
                            getString(
                                R.string.sl_edit_title,
                                shoppingList.title
                            )
                        )
                    }
                }
                refreshView()
            }
        }
    }

    private fun refreshView() {
        debugLog(shoppingList)
        et_shopping_list_name.setText(shoppingList.title)
        shoppingList.deadLine?.let {
            DateUtils.getTimeString(it, getString(R.string.exp_entry_time_format)).let {
                tv_sl_deadline.text = if (checkIfEnglishLanguageSelected()) {
                    it
                } else {
                    TranslatorUtils.englishToBanglaDateString(it)
                }
            }
        }
        if (shoppingList.getCountDownTime() != null) {
            et_sl_count_down.setText((shoppingList.getCountDownTime()!! / DateUtils.MINUTE_IN_MS).toString().apply { debugLog(this) })
            ShoppingList.Companion.ReminderInterval.values()
                .find { shoppingList.getReminderInterval() == it.intervalMs }?.let {
                    spinner_reminder_interval_selector.selectedIndex =
                        ShoppingList.Companion.ReminderInterval.values().indexOf(it)
                }
            cb_set_sl_remainder.isChecked = true
        } else {
            cb_set_sl_remainder.isChecked = false
        }
    }

    private fun getShoppingListId(): String = arguments!!.getString(ARG_SHOPPING_LIST_ID)!!

    companion object {
        private const val ARG_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.shopping_list.edit.FragmentShoppingListEdit.ARG_SHOPPING_LIST_ID"

        fun getInstance(shoppingListId: String): FragmentShoppingListEdit {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ID, shoppingListId)
            val fragment =
                FragmentShoppingListEdit()
            fragment.arguments = arg
            return fragment
        }
    }
}
