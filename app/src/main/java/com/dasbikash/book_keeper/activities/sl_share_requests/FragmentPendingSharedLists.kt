package com.dasbikash.book_keeper.activities.sl_share_requests

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dasbikash.android_basic_utils.utils.debugLog

import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.models.TbaSlShareReq
import com.dasbikash.book_keeper.rv_helpers.TbaSlShareReqListAdapter
import kotlinx.android.synthetic.main.fragment_pending_shared_lists.*

class FragmentPendingSharedLists : FragmentTemplate() {

    private lateinit var mViewModel:ViewModelShoppingListShareRequests
    private val tbaSlShareReqListAdapter = TbaSlShareReqListAdapter({debugLog(it)},{debugLog(it)},{debugLog(it)})

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pending_shared_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewModel = ViewModelProviders.of(activity!!).get(ViewModelShoppingListShareRequests::class.java)
        rv_pending_shared_lists.adapter = tbaSlShareReqListAdapter
        mViewModel.getTbaSlShareReqLiveData().observe(this,object : Observer<List<TbaSlShareReq>>{
            override fun onChanged(list: List<TbaSlShareReq>?) {
                (list ?: emptyList()).let {
                    tbaSlShareReqListAdapter.submitList(it)
                }
            }
        })
    }
}
