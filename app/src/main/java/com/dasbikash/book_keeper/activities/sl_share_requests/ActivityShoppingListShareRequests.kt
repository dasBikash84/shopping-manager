package com.dasbikash.book_keeper.activities.sl_share_requests

import android.graphics.Color
import android.os.Build
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import kotlinx.android.synthetic.main.activity_shopping_list_share_requests.*

class ActivityShoppingListShareRequests : ActivityTemplate() {

    override fun getLayoutID(): Int = R.layout.activity_shopping_list_share_requests
    override fun registerDefaultFragment(): FragmentTemplate = FragmentPendingRequestedLists()

    private fun loadFragmentPendingRequest(){
        addFragmentClearingBackStack(FragmentPendingRequestedLists())
        tv_send_requests_selector.setBackgroundColor(getActiveTabColor())
        tv_received_requests_selector.setBackgroundColor(getInActiveTabColor())
    }

    private fun loadFragmentPendingSharedLists(){
        addFragmentClearingBackStack(FragmentPendingSharedLists())
        tv_send_requests_selector.setBackgroundColor(getInActiveTabColor())
        tv_received_requests_selector.setBackgroundColor(getActiveTabColor())
    }

    override fun onResume() {
        super.onResume()
        tv_send_requests_selector.setOnClickListener { loadFragmentPendingRequest() }
        tv_received_requests_selector.setOnClickListener { loadFragmentPendingSharedLists() }
    }

    @Suppress("DEPRECATION")
    private fun getActiveTabColor():Int = Color.WHITE

    @Suppress("DEPRECATION")
    private fun getInActiveTabColor():Int{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resources.getColor(R.color.snow_white2,null)
        } else {
            resources.getColor(R.color.snow_white2)
        }
    }

    override fun getDefaultTitle(): String? = getString(R.string.pending_sl_share_requests)
}
