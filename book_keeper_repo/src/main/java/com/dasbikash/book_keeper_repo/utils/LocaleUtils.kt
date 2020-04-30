package com.dasbikash.book_keeper_repo.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.telephony.TelephonyManager
import androidx.core.os.ConfigurationCompat
import java.lang.reflect.Method


object LocaleUtils {

    fun getCountryCode(context: Context):String?{

        // try to get country code from TelephonyManager service
        (context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?)?.let {
            val telephonyManager = it

            telephonyManager.simCountryIso?.let {
                if (it.length == 2){
                    return it.toLowerCase()
                }
            }

            if (telephonyManager.phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                // special case for CDMA Devices
                getCDMACountryIso()
            } else {
                // for 3G devices (with SIM) query getNetworkCountryIso()
                telephonyManager.networkCountryIso
            }?.let {
                if (it.length == 2){
                    return it.toLowerCase()
                }
            }
        }
        return null
        // if network country not available (tablets maybe), get country code from Locale class
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            context.resources.configuration.locales[0].country
//        } else {
//            context.resources.configuration.locale.country
//        }.let {
//            if (it.length == 2){
//                return@let it.toLowerCase()
//            }
//            return@let null
//        }
    }

    @SuppressLint("PrivateApi")
    private fun getCDMACountryIso(): String? {
        try {
            // try to get country code from SystemProperties private class
            val systemProperties =
                Class.forName("android.os.SystemProperties")
            val get: Method = systemProperties.getMethod("get", String::class.java)

            // get homeOperator that contain MCC + MNC
            val homeOperator = get.invoke(
                systemProperties,
                "ro.cdma.home.operator.numeric"
            ) as String

            // first 3 chars (MCC) from homeOperator represents the country code
            val mcc = homeOperator.substring(0, 3).toInt()
            when (mcc) {
                330 -> return "PR"
                310 -> return "US"
                311 -> return "US"
                312 -> return "US"
                316 -> return "US"
                283 -> return "AM"
                460 -> return "CN"
                455 -> return "MO"
                414 -> return "MM"
                619 -> return "SL"
                450 -> return "KR"
                634 -> return "SD"
                434 -> return "UZ"
                232 -> return "AT"
                204 -> return "NL"
                262 -> return "DE"
                247 -> return "LV"
                255 -> return "UA"
            }
        } catch (ex:Throwable) {
            ex.printStackTrace()
        }
        return null
    }

    fun getSelectedDisplayLanguage():String?{
        return ConfigurationCompat.getLocales(Resources.getSystem().getConfiguration()).let {
            if (!it.isEmpty){
                it.get(0).language
            }else{
                null
            }
        }
    }
}