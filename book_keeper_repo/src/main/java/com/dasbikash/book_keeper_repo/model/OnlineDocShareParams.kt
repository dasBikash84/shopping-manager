package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import java.util.*

@Keep
data class OnlineDocShareParams(
    var shareReqDocId:String = UUID.randomUUID().toString(),
    var partnerId:String?=null,
    var documentPath:String?=null
){
    fun validateData(){
        if (partnerId.isNullOrBlank() ||
            documentPath.isNullOrBlank()){
            throw IllegalArgumentException()
        }
    }

    companion object{
        fun getInstanceForShoppingList(shoppingList: ShoppingList)
            : OnlineDocShareParams {
            val shoppingListShareParams =
                OnlineDocShareParams(
                    partnerId = AuthRepo.getUserId(),
                    documentPath = ShoppingListRepo.getFbPath(shoppingList)
                )
            return shoppingListShareParams
        }
    }
}