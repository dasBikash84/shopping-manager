package com.dasbikash.book_keeper.activities.sl_share_requests

import androidx.lifecycle.lifecycleScope
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityShoppingListShareRequests : ActivityTemplate() {

    override fun getLayoutID(): Int = R.layout.activity_shopping_list_share_requests
    override fun registerDefaultFragment(): FragmentTemplate = FragmentPendingSharedLists()

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                ShoppingListRepo.syncSlShareRequestData(this@ActivityShoppingListShareRequests)
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    override fun getDefaultTitle(): String? = getString(R.string.pending_sl_share_requests)
}
