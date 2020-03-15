package com.dasbikash.exp_man.activities.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.date_time_picker.DateTimePicker
import com.dasbikash.exp_man.R
import kotlinx.android.synthetic.main.fragment_add_exp.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class FragmentAddExp : Fragment() {
    private val TIME_REFRESH_INTERVAL = 1000L
    private val mEntryTime = Calendar.getInstance()
    private var timeAutoUpdateOn = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_exp, container, false)
    }

    private fun updateTime(){
        tv_entry_add.text = DateUtils.getLongDateString(mEntryTime.time)
    }

    override fun onResume() {
        super.onResume()
        refreshTime()
    }

    private fun refreshTime(){
        lifecycleScope.launch {
            if (timeAutoUpdateOn){
                mEntryTime.time = Date()
                updateTime()
                delay(TIME_REFRESH_INTERVAL)
                refreshTime()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_entry_add_holder.setOnClickListener {
            runWithContext {
                val dateTimePicker = DateTimePicker(
                    date = mEntryTime.time,
                    doOnDateTimeSet = {
                        timeAutoUpdateOn = false
                        mEntryTime.time = it
                        updateTime()
                    }
                )
                dateTimePicker.display(it)
            }
        }

    }
}