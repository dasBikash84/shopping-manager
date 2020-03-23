package com.dasbikash.book_keeper_repo

import android.content.Context
import com.dasbikash.book_keeper_repo.db.EMDatabase

open class BookKeeperRepo {
    internal fun getDatabase(context: Context) = EMDatabase.getDatabase(context)
}