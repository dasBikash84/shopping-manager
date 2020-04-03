package com.dasbikash.book_keeper.activities.sl_share

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate

class FragmentShareSlOffline:FragmentTemplate() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_share_sl_offline, container, false)
    }

    override fun getPageTitle(context: Context): String? {
        return context.getString(R.string.offline_share_page_title)
    }
    companion object{
        private const val ARG_SL_ID =
            "com.dasbikash.book_keeper.activities.sl_share.FragmentShareSlOffline.ARG_SL_ID"

        fun getInstance(shoppingListId:String):FragmentShareSlOffline{
            val arg = Bundle()
            arg.putString(ARG_SL_ID,shoppingListId)
            val fragment = FragmentShareSlOffline()
            fragment.arguments = arg
            return fragment
        }
    }
}