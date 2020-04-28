package com.dasbikash.book_keeper_repo

import android.content.Context
import com.dasbikash.async_manager.runSuspended
import com.dasbikash.book_keeper_repo.model.Country
import com.dasbikash.book_keeper_repo.model.CountryData
import com.dasbikash.book_keeper_repo.model.Currency
import com.dasbikash.book_keeper_repo.utils.LocaleUtils
import com.google.gson.Gson
import java.io.InputStreamReader

object CountryRepo {

    private const val DEFAULT_COUNTRY_CODE = "us"

    private suspend fun getAllCountryData(context: Context):List<Country>{
        return runSuspended {
            Gson().fromJson(
                InputStreamReader(context.resources.openRawResource(R.raw.country_data)),
                CountryData::class.java
            ).let {
                it.countryList?.let {
                    return@runSuspended it
                }
                return@runSuspended emptyList()
            }
        }
    }

    suspend fun getCountryData(context: Context):List<Country> =
        getAllCountryData(context).filter { it.enabled }

    suspend fun getCurrentCountry(context: Context):Country?{
        LocaleUtils.getCountryCode(context)?.toLowerCase()?.let {
            val countryCode = it
            getCountryData(context).find { it.countryCode.toLowerCase().trim() == countryCode.trim()}?.let {
                return it
            }
        }
        return null
    }

    fun getCurrentCountry(context: Context,countries: List<Country>):Country?{
        (LocaleUtils.getCountryCode(context) ?: DEFAULT_COUNTRY_CODE).toLowerCase().let {
            val countryCode = it
            countries.find { it.countryCode.toLowerCase().trim() == countryCode.trim()}?.let {
                return it
            }
        }
        return null
    }

    suspend fun getCurrencies(context: Context):List<Currency>{
        val currencies = mutableListOf<Currency>()
        getCountryData(context)
            .flatMap { it.currencies ?: emptyList() }
            .asSequence().forEach {
                val currency = it
                if (currencies.count { it.code== currency.code}==0){
                    currencies.add(currency)
                }
            }
        return currencies
    }

    suspend fun findCurrencyByPhoneNumber(context: Context,phoneNumber:String):Currency?{
        return getCountryData(context)
                .find { it.callingCode?.let { phoneNumber.startsWith(it)} ?: false }
                ?.let {
                    it.currencies?.let {
                        if (it.isNotEmpty()) {
                            it.first()
                        } else {
                            null
                        }
                    }
                }
    }

    suspend fun getCurrentCountryCurrency(context: Context):Currency?{
        return getCurrentCountry(context)
                    ?.currencies
                    ?.let {
                        if (it.isNotEmpty()) {
                            it.first()
                        } else {
                            null
                        }
                    }
    }
}