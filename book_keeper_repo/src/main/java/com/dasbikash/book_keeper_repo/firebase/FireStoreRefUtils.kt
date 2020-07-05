package com.dasbikash.book_keeper_repo.firebase

internal object FireStoreRefUtils {
    private const val USER_COLLECTION_NAME = "users"
    private const val EXPENSE_ENTRY_COLLECTION_NAME = "exp_entries"
    const val SHOPPING_LIST_COLLECTION_NAME = "shopping_lists"
    private const val ONLINE_DOC_SHARE_REQ_COLLECTION_NAME = "sl_share_request"
    private const val CONNECTION_REQUEST_COLLECTION_NAME = "connection_request"
    private const val EVENT_NOTIFICATION_COLLECTION_NAME = "event_notification"
    private const val NOTE_ENTRY_COLLECTION_NAME = "note_entry"

    fun getUserCollectionRef() = FireStoreConUtils.getDbConnection().collection(USER_COLLECTION_NAME)

    fun getExpenseEntryCollectionRef() = FireStoreConUtils.getDbConnection().collection(EXPENSE_ENTRY_COLLECTION_NAME)

    fun getShoppingListCollectionRef() = FireStoreConUtils.getDbConnection().collection(SHOPPING_LIST_COLLECTION_NAME)

    fun getOnlineSlShareRequestCollectionRef() = FireStoreConUtils.getDbConnection().collection(ONLINE_DOC_SHARE_REQ_COLLECTION_NAME)

    fun getConnectionRequestCollectionRef() = FireStoreConUtils.getDbConnection().collection(CONNECTION_REQUEST_COLLECTION_NAME)

    fun getEventNotificationCollectionRef() = FireStoreConUtils.getDbConnection().collection(EVENT_NOTIFICATION_COLLECTION_NAME)

    fun getNoteEntryCollectionRef() = FireStoreConUtils.getDbConnection().collection(NOTE_ENTRY_COLLECTION_NAME)

}