package com.dasbikash.book_keeper_repo.firebase

internal object FireStoreRefUtils {
    private const val USER_COLLECTION_NAME = "users"
    private const val EXPENSE_ENTRY_COLLECTION_NAME = "exp_entries"
    private const val SHOPPING_LIST_COLLECTION_NAME = "shopping_lists"
    private const val SHOPPING_LIST_SHARE_REQ_COLLECTION_NAME = "shopping_list_share_request"
    private const val SHOPPING_LIST_SHARE_REQ_LOG_COLLECTION_NAME = "shopping_list_share_request_log"

    fun getUserCollectionRef() = FireStoreConUtils.getDbConnection().collection(USER_COLLECTION_NAME)

    fun getExpenseEntryCollectionRef() = FireStoreConUtils.getDbConnection().collection(EXPENSE_ENTRY_COLLECTION_NAME)

    fun getShoppingListCollectionRef() = FireStoreConUtils.getDbConnection().collection(SHOPPING_LIST_COLLECTION_NAME)

    fun getShoppingListShareRequestCollectionRef() = FireStoreConUtils.getDbConnection().collection(SHOPPING_LIST_SHARE_REQ_COLLECTION_NAME)

    fun getShoppingListShareRequestLogCollectionRef() = FireStoreConUtils.getDbConnection().collection(SHOPPING_LIST_SHARE_REQ_LOG_COLLECTION_NAME)
}