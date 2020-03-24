package com.dasbikash.book_keeper.utils

import android.content.Context
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper_repo.model.TimeBasedExpenseEntryGroup
import com.dasbikash.book_keeper_repo.model.TimeDuration
import java.util.*

fun Date.getWeekString():String{
    val cal = Calendar.getInstance()
    cal.time = this
    val currentDay = cal.get(Calendar.DAY_OF_WEEK)
    val firstDay = cal.clone() as Calendar
    val lastDay = cal.clone() as Calendar
    firstDay.add(Calendar.DAY_OF_WEEK,-(currentDay-1))
    lastDay.add(Calendar.DAY_OF_WEEK,(7-currentDay))
    return "${DateUtils.getShortDateString(firstDay.time)} - ${DateUtils.getShortDateString(lastDay.time)}"
}

fun Double.optimizedString(decimalPointCount:Int?=null):String{
    val maxDecimalPoints = 10
    val defaultDecimalPoints = 5
    val strFormat = "%2.${if (decimalPointCount==null || decimalPointCount>maxDecimalPoints) defaultDecimalPoints else decimalPointCount}f"
    debugLog("strFormat: $strFormat")
    if (this != this.toLong().toDouble()) {
        return String.format(strFormat, this).apply {
            debugLog("optimizedString return: $this")
        }
    }else{
        return this.toLong().toString().apply {
            debugLog("optimizedString return: $this")
        }
    }
}

fun checkIfEnglishLanguageSelected():Boolean{
    return Locale.getDefault().getDisplayLanguage().contains("english",true)
}

fun TimeBasedExpenseEntryGroup.getTitleString(context: Context):String{
    return when(timeDuration){
        TimeDuration.DAY -> DateUtils.getShortDateString(startTime)
        TimeDuration.WEEK -> startTime.getWeekString()
        TimeDuration.MONTH -> DateUtils.getTimeString(startTime,context.getString(R.string.month_title_format))
    }
}