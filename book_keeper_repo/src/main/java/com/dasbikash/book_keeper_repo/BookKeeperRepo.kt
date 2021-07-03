package com.dasbikash.book_keeper_repo

import android.content.Context
import com.dasbikash.book_keeper_repo.db.EMDatabase
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils

open class BookKeeperRepo {
    internal fun getDatabase(context: Context) = EMDatabase.getDatabase(context)
    companion object{
        private const val DISABLE_IMPORT_GUEST_DATA_SP_KEY =
            "com.dasbikash.book_keeper_repo.BookKeeperRepo.DISABLE_IMPORT_GUEST_DATA_SP_KEY"

        fun disableGuestDataImport(context: Context){
            SharedPreferenceUtils.getDefaultInstance().saveDataSync(
                context,DISABLE_IMPORT_GUEST_DATA_SP_KEY,DISABLE_IMPORT_GUEST_DATA_SP_KEY
            )
        }

        fun enableGuestDataImport(context: Context){
            SharedPreferenceUtils.getDefaultInstance().removeKey(
                context,DISABLE_IMPORT_GUEST_DATA_SP_KEY
            )
        }

        fun isGuestDataImportEnabled(context: Context):Boolean{
            return !SharedPreferenceUtils.getDefaultInstance().checkIfExists(
                context,DISABLE_IMPORT_GUEST_DATA_SP_KEY
            )
        }
    }
}