package com.dasbikash.book_keeper.models

import androidx.annotation.Keep
import com.dasbikash.book_keeper_repo.model.OnlineSlShareReq
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.book_keeper_repo.model.User

@Keep
data class TbaSlShareReq(
    val shoppingList:ShoppingList,
    val onlineSlShareReq: OnlineSlShareReq,
    val requester:User
)