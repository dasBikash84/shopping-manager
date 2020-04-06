package com.dasbikash.book_keeper_repo.firebase

internal object FireStoreRefUtils {
    private const val USER_COLLECTION_NAME = "users"
    private const val EXPENSE_ENTRY_COLLECTION_NAME = "exp_entries"
    private const val SHOPPING_LIST_COLLECTION_NAME = "shopping_lists"
    private const val ONLINE_DOC_SHARE_REQ_COLLECTION_NAME = "online_doc_share_request"

    fun getUserCollectionRef() = FireStoreConUtils.getDbConnection().collection(USER_COLLECTION_NAME)

    fun getExpenseEntryCollectionRef() = FireStoreConUtils.getDbConnection().collection(EXPENSE_ENTRY_COLLECTION_NAME)

    fun getShoppingListCollectionRef() = FireStoreConUtils.getDbConnection().collection(SHOPPING_LIST_COLLECTION_NAME)

    fun getOnlineDocShareRequestCollectionRef() = FireStoreConUtils.getDbConnection().collection(ONLINE_DOC_SHARE_REQ_COLLECTION_NAME)

}