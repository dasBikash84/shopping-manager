package com.dasbikash.exp_man.utils

import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
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
        return String.format(strFormat, this)
    }else{
        return this.toLong().toString()
    }
}

fun checkIfEnglishLanguageSelected():Boolean{
    return Locale.getDefault().getDisplayLanguage().contains("english",true)
}
