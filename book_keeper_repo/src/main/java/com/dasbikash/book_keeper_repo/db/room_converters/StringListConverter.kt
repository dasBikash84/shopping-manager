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

package com.dasbikash.book_keeper_repo.db.room_converters

import androidx.room.TypeConverter
import com.dasbikash.android_basic_utils.utils.LoggerUtils
import java.util.*

internal object StringListConverter {

    private val DATA_BRIDGE = "@#@#@#"

    @TypeConverter
    @JvmStatic
    internal fun fromStringList(entry: List<String>?): String {

        val stringBuilder = StringBuilder("")
        entry?.let {
            for (i in entry.indices) {
                @Suppress("SENSELESS_COMPARISON")
                if (entry[i] == null) continue
                stringBuilder.append(entry[i])
                if (i != entry.size - 1) {
                    stringBuilder.append(DATA_BRIDGE)
                }
            }
        }

        return stringBuilder.toString()
    }

    @TypeConverter
    @JvmStatic
    internal fun toStringList(entryListString: String?): List<String> {

        val entry = ArrayList<String>()

        if (entryListString != null) {
            for (entryStr in entryListString.split(DATA_BRIDGE.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()) {
                try {
                    entry.add(entryStr)
                } catch (e: NumberFormatException) {
                    LoggerUtils.debugStackTrace(e)
                }

            }
        }

        return entry
    }
}
