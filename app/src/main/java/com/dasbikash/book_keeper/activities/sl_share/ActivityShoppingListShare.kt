package com.dasbikash.book_keeper.activities.sl_share

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.utils.byteArray
import com.dasbikash.book_keeper.utils.toCharArray
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.google.gson.Gson
import kotlin.experimental.xor

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

@Keep
data class SlToQr(
    var slShareMethod: SlShareMethod?=null,
    var data:String?=null
){
    companion object{
        private const val key: Byte = 0xDE.toByte()

        fun getPayloadForOfflineSharing(shoppingList: ShoppingList):String{
            return Gson().toJson(
                    SlToQr(
                        data = encodeShoppingListData(shoppingList),
                        slShareMethod = SlShareMethod.OFF_LINE))
        }

        private fun encodeShoppingListData(shoppingList: ShoppingList):String{
            return Gson().toJson(shoppingList)
        }

        fun decodeQrScanResult(qrData: String):SlToQr?{
            try {
                return Gson().fromJson(qrData,SlToQr::class.java)
            }catch (ex:Throwable){
                ex.printStackTrace()
                return null
            }
        }

        fun decodeOfflineShoppingList(slToQr: SlToQr):ShoppingList?{
            slToQr.data?.let {
                try {
                    return Gson().fromJson(it,ShoppingList::class.java)
                }catch (ex:Throwable){
                    ex.printStackTrace()
                }
            }
            return null
        }
    }
}

enum class SlShareMethod{
    OFF_LINE,ON_LINE
}