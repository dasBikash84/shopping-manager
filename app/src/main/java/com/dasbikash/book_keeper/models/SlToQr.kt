package com.dasbikash.book_keeper.models

import androidx.annotation.Keep
import com.dasbikash.book_keeper_repo.model.OnlineDocShareParams
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.google.gson.Gson

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
                    slShareMethod = SlShareMethod.OFF_LINE
                )
            )
        }

        private fun encodeShoppingListData(shoppingList: ShoppingList):String{
            return Gson().toJson(shoppingList)
        }

        fun decodeQrScanResult(qrData: String): SlToQr?{
            try {
                return Gson()
                    .fromJson(qrData, SlToQr::class.java)
            }catch (ex:Throwable){
                ex.printStackTrace()
                return null
            }
        }

        fun decodeOfflineShoppingList(slToQr: SlToQr): ShoppingList?{
            slToQr.data?.let {
                try {
                    return Gson().fromJson(it,
                        ShoppingList::class.java)
                }catch (ex:Throwable){
                    ex.printStackTrace()
                }
            }
            return null
        }

        fun decodeOnlineRequestPayload(slToQr: SlToQr): OnlineDocShareParams?{
            slToQr.data?.let {
                try {
                    Gson().fromJson(it,
                        OnlineDocShareParams::class.java)?.let {
                        it.validateData()
                        return it
                    }
                }catch (ex:Throwable){
                    ex.printStackTrace()
                }
            }
            return null
        }

        fun getPayloadForOnlineSharing(shoppingList: ShoppingList): String {
            return Gson().toJson(
                SlToQr(
                    data = dataForOnlineSharing(shoppingList),
                    slShareMethod = SlShareMethod.ON_LINE
                )
            )
        }

        private fun dataForOnlineSharing(shoppingList: ShoppingList): String {
            return Gson().toJson(
                OnlineDocShareParams.getInstanceForShoppingList(shoppingList)
            )
        }
    }
}