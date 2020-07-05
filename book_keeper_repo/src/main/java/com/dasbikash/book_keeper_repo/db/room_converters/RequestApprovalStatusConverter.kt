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
import com.dasbikash.book_keeper_repo.model.RequestApprovalStatus

internal object RequestApprovalStatusConverter {

    @TypeConverter
    @JvmStatic
    internal fun fromShoppingListApprovalStatus(requestApprovalStatus: RequestApprovalStatus?): Int? {
        requestApprovalStatus?.let {
            return it.ordinal
        }
        return null
    }

    @TypeConverter
    @JvmStatic
    internal fun toShoppingListApprovalStatus(value: Int?): RequestApprovalStatus? {
        value?.let {
            return RequestApprovalStatus.values()[it]
        }
        return null
    }
}
