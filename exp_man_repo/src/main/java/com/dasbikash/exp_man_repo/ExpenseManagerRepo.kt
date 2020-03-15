package com.dasbikash.exp_man_repo

import android.content.Context
import com.dasbikash.exp_man_repo.db.EMDatabase

open class ExpenseManagerRepo {
    internal fun getDatabase(context: Context) = EMDatabase.getDatabase(context)
}