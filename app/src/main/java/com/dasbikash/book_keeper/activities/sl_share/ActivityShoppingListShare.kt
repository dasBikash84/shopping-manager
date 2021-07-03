package com.dasbikash.book_keeper.activities.sl_share

import android.content.Context
import android.content.Intent
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate

class ActivityShoppingListShare : ActivityTemplate() {
    override fun registerDefaultFragment(): FragmentTemplate {
        return if(isOfflineShareInstance()) {
            FragmentShareSlOffline.getInstance(getShoppingListId())
        }else if(isOnlineShareInstance()){
            FragmentShareSlOnline.getInstance(getShoppingListId())
        }else{
            TODO()
        }
    }

    private fun isOfflineShareInstance() = intent.hasExtra(EXTRA_OFFLINE_SHARE)
    private fun isOnlineShareInstance() = intent.hasExtra(EXTRA_ONLINE_SHARE)
    private fun getShoppingListId():String = intent.getStringExtra(EXTRA_SL_ID)!!

    companion object{
        private const val EXTRA_OFFLINE_SHARE =
            "com.dasbikash.book_keeper.activities.sl_share.ActivityShoppingListShare.EXTRA_OFFLINE_SHARE"
        private const val EXTRA_ONLINE_SHARE =
            "com.dasbikash.book_keeper.activities.sl_share.ActivityShoppingListShare.EXTRA_ONLINE_SHARE"
        private const val EXTRA_SL_ID =
            "com.dasbikash.book_keeper.activities.sl_share.ActivityShoppingListShare.EXTRA_SL_ID"

        fun getOfflineShareInstance(context: Context,shoppingListId:String):Intent{
            val intent = Intent(context.applicationContext,ActivityShoppingListShare::class.java)
            intent.putExtra(EXTRA_OFFLINE_SHARE,EXTRA_OFFLINE_SHARE)
            intent.putExtra(EXTRA_SL_ID,shoppingListId)
            return intent
        }

        fun getOnlineShareInstance(context: Context,shoppingListId:String):Intent{
            val intent = Intent(context.applicationContext,ActivityShoppingListShare::class.java)
            intent.putExtra(EXTRA_ONLINE_SHARE,EXTRA_ONLINE_SHARE)
            intent.putExtra(EXTRA_SL_ID,shoppingListId)
            return intent
        }
    }
}