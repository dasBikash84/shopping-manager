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
import com.dasbikash.book_keeper.activities.shopping_list.view.FragmentShoppingListView
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.bg_tasks.ShoppingListReminderScheduler
import com.dasbikash.book_keeper.utils.GetCalculatorMenuItem
import com.dasbikash.book_keeper.utils.TranslatorUtils
import com.dasbikash.book_keeper.utils.checkIfEnglishLanguageSelected
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.date_time_picker.DateTimePicker
import com.dasbikash.menu_view.MenuView
import com.dasbikash.snackbar_ext.showShortSnack
import com.google.firebase.Timestamp
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_shopping_list_edit.*
import kotlinx.coroutines.launch
import java.util.*

class FragmentShoppingListAddEdit : FragmentTemplate() {

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

        et_shopping_list_note.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                (s?.toString() ?: "").let {
                    if (it.isBlank()){
                        shoppingList.note = null
                    }else {
                        shoppingList.note = it.trim()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        sl_deadline_tv_holder.setOnClickListener {
            runWithContext {
                hideKeyboard()
                val dateTimePicker = DateTimePicker(
                    date = shoppingList.deadLine?.toDate(),
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

        spinner_reminder_unit_selector.setItems(resources.getStringArray(R.array.reminder_time_units).toList())

        spinner_reminder_unit_selector.setOnItemSelectedListener(object :
            MaterialSpinner.OnItemSelectedListener<String> {
            override fun onItemSelected(
                view: MaterialSpinner?,
                position: Int,
                id: Long,
                item: String?
            ) {
                updateReminderInterval(et_sl_count_down.text.toString(),position)
            }
        })

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
                    true -> {
                        if (shoppingList.deadLine!=null) {
                            sl_remainder_set_block.show()
                        }else{
                            cb_set_sl_remainder.isChecked = false
                            showShortSnack(R.string.deadline_first_message)
                        }
                    }
                    false -> {
                        if (shoppingList.deadLine!=null) {
                            runWithContext {
                                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                                    message = it.getString(R.string.disable_sl_reminder_prompt),
                                    doOnPositivePress = {
                                        disableReminder()
                                    },
                                    doOnNegetivePress = { cb_set_sl_remainder.isChecked = true }
                                ))
                            }
                        }
                    }
                }
            }
        })

        cb_disable_deadline.setOnCheckedChangeListener({ _, isChecked ->
            if (isChecked){
                runWithContext {
                    DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                        message = it.getString(R.string.disable_deadline_prompt),
                        positiveButtonText = it.getString(R.string.yes),
                        doOnPositivePress = {
                            disableDeadline()
                        },
                        doOnNegetivePress = {
                            cb_disable_deadline.isChecked = false
                        }
                    ))
                }
            }
        })

        et_sl_count_down.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                (s?.toString() ?: "").let {
                    updateReminderInterval(it)
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
                        if (isEditMode()){
                            exit()
                        }else {
                            activity?.finish()
                        }
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

    private fun updateReminderInterval(timeText: String,position: Int?=null) {
        if (timeText.isBlank()) {
            0
        } else {
            timeText.trim().toInt()
        }.let { shoppingList.setCountDownTime(it * reminderUnitPeriods[position ?: spinner_reminder_unit_selector.selectedIndex]) }
    }

    private fun disableDeadline() {
        shoppingList.deadLine = null
        disableReminder()
        cb_set_sl_remainder.isChecked = false
        refreshView()
    }

    private fun disableReminder() {
        shoppingList.setReminderInterval(null)
        shoppingList.setCountDownTime(null)
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
                ShoppingListReminderScheduler.runReminderScheduler(it.applicationContext)
                exit()
            }
        }
    }

    private fun exit() {
        (activity as ActivityShoppingList?)?.let{
            it.addFragmentClearingBackStack(FragmentShoppingListView.getInstance(shoppingList.id))
        }
    }

    private fun setDeadLine(deadLine: Date) {
        if (ShoppingList.validateDeadLine(deadLine)) {
            shoppingList.deadLine = Timestamp(deadLine)
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
                    if (isEditMode()) {
                        shoppingList = ShoppingListRepo.findInLocalById(it, getShoppingListId())!!
                        (activity as ActivityShoppingList?)?.apply {
                            setTitle(
                                getString(
                                    R.string.edit_title,
                                    shoppingList.title
                                )
                            )
                        }
                    }else{
                        shoppingList = ShoppingList(userId = AuthRepo.getUser(it)?.id)
                        (activity as ActivityShoppingList?)?.apply {
                            setTitle(
                                getString(R.string.add_shopping_list)
                            )
                        }
                    }
                }
                refreshView()
            }
        }
    }

    private fun refreshView() {
        debugLog(shoppingList)
        et_shopping_list_name.setText(shoppingList.title)
        et_shopping_list_note.setText(shoppingList.note)
        if (shoppingList.deadLine == null){
            tv_sl_deadline.text = getString(R.string.click_to_set_prompt)
            cb_disable_deadline.hide()
            cb_set_sl_remainder.hide()
        }else{
            cb_disable_deadline.isChecked = false
            cb_disable_deadline.show()
            cb_set_sl_remainder.show()

            DateUtils.getTimeString(shoppingList.deadLine!!.toDate(), getString(R.string.exp_entry_time_format)).let {
                tv_sl_deadline.text = if (checkIfEnglishLanguageSelected()) {
                    it
                } else {
                    TranslatorUtils.englishToBanglaDateString(it)
                }
            }

            if (shoppingList.getCountDownTime() != null) {
                shoppingList.getCountDownTime()?.let {
                    if(it > 0L && it % DateUtils.HOUR_IN_MS == 0L){
                        et_sl_count_down.setText((it/reminderUnitPeriods[1]).toString())
                        spinner_reminder_unit_selector.selectedIndex = 1
                    }else{
                        et_sl_count_down.setText((it/reminderUnitPeriods[0]).toString())
                        spinner_reminder_unit_selector.selectedIndex = 0
                    }
                }
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
    }

    private fun getShoppingListId(): String = arguments!!.getString(ARG_SHOPPING_LIST_ID)!!
    private fun isEditMode():Boolean = arguments?.containsKey(ARG_SHOPPING_LIST_ID) ?: false

    override fun getExitPrompt(): String? {
        return getString(R.string.discard_and_exit_prompt)
    }

    override fun getOptionsMenu(context: Context): MenuView? {
        val menuView = MenuView(menuItemFontSize = 20.00f)
        menuView.add(GetCalculatorMenuItem(context))
        return menuView
    }

    companion object {

        val reminderUnitPeriods = arrayOf<Long>(DateUtils.MINUTE_IN_MS,DateUtils.HOUR_IN_MS)

        private const val ARG_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.shopping_list.edit.FragmentShoppingListEdit.ARG_SHOPPING_LIST_ID"

        fun getEditInstance(shoppingListId: String): FragmentShoppingListAddEdit {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ID, shoppingListId)
            val fragment =
                FragmentShoppingListAddEdit()
            fragment.arguments = arg
            return fragment
        }

        fun getCreateInstance(): FragmentShoppingListAddEdit {
            return FragmentShoppingListAddEdit()
        }
    }
}
