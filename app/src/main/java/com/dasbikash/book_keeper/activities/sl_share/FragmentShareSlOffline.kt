package com.dasbikash.book_keeper.activities.sl_share

import android.content.Context
import android.os.Bundle
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.models.SlToQr
import com.dasbikash.book_keeper_repo.model.ShoppingList

class FragmentShareSlOffline: FragmentShareShoppingList() {

    override suspend fun getDataForQrCode(shoppingList: ShoppingList): String {
        return SlToQr.getPayloadForOfflineSharing(shoppingList)
    }

    override fun getTitle(context: Context): String {
        return context.getString(R.string.offline_share_page_title)
    }

    override fun getShoppingListId():String = arguments!!.getString(ARG_SL_ID)!!

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