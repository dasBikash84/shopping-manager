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
import com.dasbikash.exp_man_repo.model.ExpenseItem
import com.dasbikash.exp_man_repo.utils.toSerializable
import com.dasbikash.exp_man_repo.utils.toSerializedString
import java.util.*

internal object ExpenseItemListConverter {

    private val DATA_BRIDGE = "@#@#@#"

    @TypeConverter
    @JvmStatic
    internal fun fromExpenseItemList(entry: List<ExpenseItem>?): String? {
        entry?.map { it.toSerializedString() }?.let {
            val stringBuilder = StringBuilder("")
            for (i in entry.indices) {
                stringBuilder.append(entry[i])
                if (i != entry.size - 1) {
                    stringBuilder.append(DATA_BRIDGE)
                }
            }
            return stringBuilder.toString()
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    internal fun toExpenseItemList(entryListString: String?): List<ExpenseItem>? {
        entryListString?.let {
            return it.split(DATA_BRIDGE)
                        .asSequence()
                        .map { it.toSerializable(ExpenseItem::class.java) }
                        .filter { it!=null }
                        .map { it!! }
                        .toList()
        }
        return null
    }
}
