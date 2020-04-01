package com.dasbikash.book_keeper.utils

import com.dasbikash.android_basic_utils.utils.debugLog
import java.text.NumberFormat

object TranslatorUtils {

    private val MONTH_NAME_TABLE = arrayOf(
        arrayOf("জানুয়ারী", "Jan"),
        arrayOf("জানুয়ারি", "Jan"),
        arrayOf("ফেব্রুয়ারী", "Feb"),
        arrayOf("ফেব্রুয়ারি", "Feb"),
        arrayOf("মার্চ", "Mar"),
        arrayOf("এপ্রিল", "Apr"),
        arrayOf("মে", "May"),
        arrayOf("জুন", "Jun"),
        arrayOf("জুলাই", "Jul"),
        arrayOf("আগস্ট", "Aug"),
        arrayOf("আগষ্ট", "Aug"),
        arrayOf("অগস্ট", "Aug"),
        arrayOf("সেপ্টেম্বর", "Sep"),
        arrayOf("অক্টোবর", "Oct"),
        arrayOf("নভেম্বর", "Nov"),
        arrayOf("ডিসেম্বর", "Dec")
    )

    private val AM_PM_MARKER_TABLE = arrayOf(
        arrayOf("পূর্বাহ্ণ", "AM"),
        arrayOf("অপরাহ্ণ", "PM"),
        arrayOf("পূর্বাহ্ণ", "am"),
        arrayOf("অপরাহ্ণ", "pm")
    )

    private val DAY_NAME_TABLE = arrayOf(
        arrayOf("শনিবার", "Sat"),
        arrayOf("রবিবার", "Sun"),
        arrayOf("সোমবার", "Mon"),
        arrayOf("মঙ্গলবার", "Tue"),
        arrayOf("বুধবার", "Wed"),
        arrayOf("বৃহস্পতিবার", "Thu"),
        arrayOf("শুক্রবার", "Fri")
    )

    private val BANGLA_UNICODE_ZERO: Char = 0x09E6.toChar()
    private val BANGLA_UNICODE_NINE: Char = 0x09EF.toChar()
    private val ENGLISH_UNICODE_ZERO: Char = 0x0030.toChar()
    private val ENGLISH_UNICODE_NINE: Char = 0x0039.toChar()

    private fun convertToBanglaTimeString(englishTimeString: String): String {
        replaceEnglishDigits(englishTimeString).let {
            return englishToBanglaDateString(it)
        }
    }

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


    fun englishToBanglaDateString(dateStringInput: String): String {
        replaceEnglishMonthName(dateStringInput).let {
//            replaceEnglishDigits(it).let {
                replaceEnglishDayName(it).let {
                    return it//replaceAMPMMarkerEngToBan(it)
                }
//            }
        }
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

    private fun replaceEnglishMonthName(str: String): String {
        var localStr = str
        for (i in MONTH_NAME_TABLE.indices) {
            localStr = localStr.replace(MONTH_NAME_TABLE[i][1], MONTH_NAME_TABLE[i][0])
        }
        return localStr
    }

    private fun replaceEnglishDayName(str: String): String {
        for (i in DAY_NAME_TABLE.indices) {
            if (str.contains(DAY_NAME_TABLE[i][1])) {
                return str.replace(DAY_NAME_TABLE[i][1], DAY_NAME_TABLE[i][0])
            }
        }
        return str
    }
}

//will insert req comma
fun Double.getCurrencyString():String{
    return NumberFormat.getCurrencyInstance().format(this).let {
            if (checkIfEnglishLanguageSelected()){
                it.substring(1)
            }else{
                it.substring(0,it.length-1)
            }
        }.let { it.stripTrailingZeros() }
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