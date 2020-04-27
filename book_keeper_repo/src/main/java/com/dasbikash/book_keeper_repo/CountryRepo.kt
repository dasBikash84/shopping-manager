package com.dasbikash.book_keeper_repo

import android.content.Context
import com.dasbikash.async_manager.runSuspended
import com.dasbikash.book_keeper_repo.model.Country
import com.dasbikash.book_keeper_repo.model.CountryData
import com.dasbikash.book_keeper_repo.utils.LocaleUtils
import com.google.gson.Gson
import java.io.InputStreamReader

object CountryRepo {

    private suspend fun getAllCountryData(context: Context):List<Country>{
        return runSuspended {
            Gson().fromJson(
                InputStreamReader(context.resources.openRawResource(R.raw.country_code)),
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
        LocaleUtils.getCountryCode(context).toLowerCase().let {
            val countryCode = it
            getCountryData(context).find { it.countryCode.toLowerCase().trim() == countryCode.trim()}?.let {
                return it
            }
        }
        return null
    }

    fun getCurrentCountry(context: Context,countries: List<Country>):Country?{
        LocaleUtils.getCountryCode(context).toLowerCase().let {
            val countryCode = it
            countries.find { it.countryCode.toLowerCase().trim() == countryCode.trim()}?.let {
                return it
            }
        }
        return null
    }
}