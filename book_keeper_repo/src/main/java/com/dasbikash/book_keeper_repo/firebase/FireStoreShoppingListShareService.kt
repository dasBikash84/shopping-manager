package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.book_keeper_repo.model.ShoppingListShareReqLog
import com.dasbikash.book_keeper_repo.model.User

internal object FireStoreShoppingListShareService {

    fun postRequest(user: User,shoppingListShareReqLog: ShoppingListShareReqLog) {
        shoppingListShareReqLog.shareReq(user).let {
            it.dbRef(shoppingListShareReqLog.ownerUserId!!).set(it)
        }
        shoppingListShareReqLog.dbRef(user).set(shoppingListShareReqLog)
    }
}