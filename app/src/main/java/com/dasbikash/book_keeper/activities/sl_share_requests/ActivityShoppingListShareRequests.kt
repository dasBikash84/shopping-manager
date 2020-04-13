package com.dasbikash.book_keeper.activities.sl_share_requests

import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate

class ActivityShoppingListShareRequests : ActivityTemplate() {

    override fun getLayoutID(): Int = R.layout.activity_shopping_list_share_requests
    override fun registerDefaultFragment(): FragmentTemplate = FragmentPendingSharedLists()

    override fun getDefaultTitle(): String? = getString(R.string.pending_sl_share_requests)
}
