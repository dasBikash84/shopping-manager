/*
 * Copyright 2019 das.bikash.dev@gmail.com. All rights reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dasbikash.exp_man_repo.db.room_converters

import androidx.room.TypeConverter
import com.dasbikash.android_basic_utils.utils.LoggerUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.exp_man_repo.model.ExpenseItem
import com.dasbikash.exp_man_repo.utils.toSerializable
import com.dasbikash.exp_man_repo.utils.toSerializedString
import java.util.*

internal object ExpenseItemListConverter {

    private val DATA_BRIDGE = "@#@#@#"

    @TypeConverter
    @JvmStatic
    internal fun fromExpenseItemList(entry: List<ExpenseItem>?): String? {
        debugLog("fromExpenseItemList")
        entry?.forEach { debugLog("on top: $it") }
        entry?.forEach { debugLog("on top ser: ${it.toSerializedString()}") }
        entry?.map { it.toSerializedString() }?.let {
//            it.forEach { debugLog("after ser: ${it}") }
            val stringBuilder = StringBuilder("")
            for (i in it.indices) {
                stringBuilder.append(it[i])
                if (i != it.size - 1) {
                    stringBuilder.append(DATA_BRIDGE)
                }
            }
            debugLog("stringBuilder.toString():${stringBuilder.toString()}")
            return stringBuilder.toString()
        }
        debugLog("fromExpenseItemList: null")
        return null
    }

    @TypeConverter
    @JvmStatic
    internal fun toExpenseItemList(entryListString: String?): List<ExpenseItem>? {
        debugLog("toExpenseItemList")
        debugLog("entryListString:$entryListString")
        entryListString?.let {
            return it.split(DATA_BRIDGE)
                        .asSequence()
                        .map {
                            debugLog("before convert: $it")
                            it.toSerializable(ExpenseItem::class.java)
                        }
                        .filter {
                            debugLog("filter: $it")
                            it!=null
                        }
                        .map {
                            debugLog("map 2: $it")
                            it!!
                        }
                        .toList()
        }
        return null
    }
}
