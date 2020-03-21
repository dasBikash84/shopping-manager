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
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.exp_man_repo.model.ExpenseCategory
import com.dasbikash.exp_man_repo.utils.toSerializable
import com.dasbikash.exp_man_repo.utils.toSerializedString

internal object ExpenseCategoryConverter {

    @TypeConverter
    @JvmStatic
    internal fun fromSerializable(data: ExpenseCategory?): String?{
        debugLog("Before ser: $data")
        data?.toSerializedString().let {
            debugLog("ser: $it")
            return it
        }
    }

    @TypeConverter
    @JvmStatic
    internal fun toSerializable(serializedData: String?): ExpenseCategory?{
        debugLog("Before Dser: $serializedData")
        serializedData?.toSerializable(ExpenseCategory::class.java).let {
            debugLog("Dser: $it")
            return it
        }
    }
}
