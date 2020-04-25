package com.dasbikash.book_keeper.utils

import android.content.Context
import androidx.annotation.StringRes
import com.dasbikash.android_basic_utils.utils.DateUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.application.BookKeeperApp
import com.dasbikash.book_keeper_repo.model.SupportedLanguage
import java.text.NumberFormat
import java.util.*

object TranslatorUtils {

    private val MONTH_NAME_TABLE = arrayOf(
        arrayOf("Jan","জানুয়ারি","जनवरी"),
        arrayOf("Feb","ফেব্রুয়ারি","फरवरी"),
        arrayOf("Mar","মার্চ","मार्च"),
        arrayOf("Apr","এপ্রিল","अप्रैल"),
        arrayOf("May","মে","मई"),
        arrayOf("Jun","জুন","जून"),
        arrayOf("Jul","জুলাই","जुलाई"),
        arrayOf("Aug","আগষ্ট","अगस्त"),
        arrayOf("Sep","সেপ্টেম্বর","सितंबर"),
        arrayOf("Oct","অক্টোবর","अक्टूबर"),
        arrayOf("Nov","নভেম্বর","नवंबर"),
        arrayOf("Dec","ডিসেম্বর","दिसंबर")
    )

    private val AM_PM_MARKER_TABLE = arrayOf(
        arrayOf("পূর্বাহ্ণ", "AM"),
        arrayOf("অপরাহ্ণ", "PM"),
        arrayOf("পূর্বাহ্ণ", "am"),
        arrayOf("অপরাহ্ণ", "pm")
    )

    private val DAY_NAME_TABLE = arrayOf(
        arrayOf("Sat","শনিবার","शनिवार"),
        arrayOf("Sun","রবিবার","रविवार"),
        arrayOf("Mon","সোমবার","सोमवार"),
        arrayOf("Tue","মঙ্গলবার","मंगलवार"),
        arrayOf("Wed","বুধবার","बुधवार"),
        arrayOf("Thu","বৃহস্পতিবার","बृहस्पतिवार"),
        arrayOf("Fri","শুক্রবার","शुक्रवार")
    )

    private val BANGLA_UNICODE_ZERO: Char = 0x09E6.toChar()
    private val BANGLA_UNICODE_NINE: Char = 0x09EF.toChar()
    private val ENGLISH_UNICODE_ZERO: Char = 0x0030.toChar()
    private val ENGLISH_UNICODE_NINE: Char = 0x0039.toChar()

    private fun replaceEnglishDigits(string: String): String {
        val chars = string.toCharArray()
        for (i in chars.indices) {
            val ch = chars[i]
            if (ch <= ENGLISH_UNICODE_NINE && ch >= ENGLISH_UNICODE_ZERO) {
                chars[i] = (ch + BANGLA_UNICODE_ZERO.toInt() - ENGLISH_UNICODE_ZERO.toInt()).toChar()
            }
        }
        return String(chars)
    }


    private fun getTranslatedDateString(dateStringInput: String, language: SupportedLanguage): String {
        return replaceEnglishMonthName(dateStringInput,language).let {replaceEnglishDayName(it,language)}
    }


    fun getTranslatedDateString(context: Context,dateString: String): String {
        return getTranslatedDateString(dateString,BookKeeperApp.getLanguageSetting(context))
    }


    fun englishToBanglaNumberString(numberString: String): String = replaceEnglishDigits(numberString)

    private fun replaceAMPMMarkerEngToBan(str: String): String {
        for (i in AM_PM_MARKER_TABLE.indices) {
            if (str.contains(AM_PM_MARKER_TABLE[i][1])) {
                return str.replace(AM_PM_MARKER_TABLE[i][1], AM_PM_MARKER_TABLE[i][0])
            }
        }
        return str
    }

    private fun replaceEnglishMonthName(str: String,supportedLanguage: SupportedLanguage): String {
        var localStr = str
        for (i in MONTH_NAME_TABLE.indices) {
            localStr = when(supportedLanguage){
                SupportedLanguage.BANGLA -> localStr.replace(MONTH_NAME_TABLE[i][0], MONTH_NAME_TABLE[i][1])
                SupportedLanguage.HINDI -> localStr.replace(MONTH_NAME_TABLE[i][0], MONTH_NAME_TABLE[i][2])
                else -> localStr
            }
        }
        return localStr
    }

    private fun replaceEnglishDayName(str: String,language: SupportedLanguage): String {
        var localStr = str
        for (i in DAY_NAME_TABLE.indices) {
            localStr = when(language){
                SupportedLanguage.BANGLA -> localStr.replace(DAY_NAME_TABLE[i][0], DAY_NAME_TABLE[i][1])
                SupportedLanguage.HINDI -> localStr.replace(DAY_NAME_TABLE[i][0], DAY_NAME_TABLE[i][2])
                else -> localStr
            }
        }
        return localStr
    }
}

fun Date.toTranslatedString(context: Context, @StringRes format:Int= R.string.exp_entry_time_format):String{
    val dateString = DateUtils.getTimeString(this,context.getString(format))
    return TranslatorUtils.getTranslatedDateString(context, dateString)
}

fun Double.getCurrencyStringWithSymbol():String{
    return NumberFormat.getCurrencyInstance().format(this).substring(1)
}

//will insert req comma
fun Double.getCurrencyString():String{
    return NumberFormat.getCurrencyInstance().format(this).let {
        if (checkIfEnglishLanguageSelected()){
            it.substring(1)
        }else{
            it.substring(0,it.length-1)
        }
    }.stripTrailingZeros()
}

fun String.stripTrailingZeros():String{
    debugLog("stripTrailingZeros got: $this")
    Regex("(-?\\d+)(\\.0+)").matchEntire(this)?.destructured?.toList()?.get(0)?.let {
        return it.apply {
            debugLog("stripTrailingZeros matched 2: $this")
        }
    }
    Regex("(-?\\d+\\...+?)(0+)").matchEntire(this)?.destructured?.toList()?.get(0)?.let {
        return it.apply {
            debugLog("stripTrailingZeros matched 1: $this")
        }
    }
    return this
}

fun Double.formatForDisplay():String{
    return this.optimizedString().stripTrailingZeros()
}