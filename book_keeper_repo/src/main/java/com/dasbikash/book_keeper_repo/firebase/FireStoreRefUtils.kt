package com.dasbikash.book_keeper_repo.firebase

internal object FireStoreRefUtils {
    private const val USER_COLLECTION_NAME = "users"
    private const val CATEGORY_COLLECTION_NAME = "exp_categories"
    private const val UOM_COLLECTION_NAME = "uoms"
    private const val EXPENSE_ENTRY_COLLECTION_NAME = "exp_entries"
    private const val SHOPPING_LIST_COLLECTION_NAME = "shopping_lists"

    fun getUserCollectionRef() = FireStoreConUtils.getDbConnection().collection(USER_COLLECTION_NAME)

    fun getExpCategoriesCollectionRef() = FireStoreConUtils.getDbConnection().collection(CATEGORY_COLLECTION_NAME)

    fun getUomCollectionRef() = FireStoreConUtils.getDbConnection().collection(UOM_COLLECTION_NAME)

    fun getExpenseEntryCollectionRef() = FireStoreConUtils.getDbConnection().collection(EXPENSE_ENTRY_COLLECTION_NAME)

    fun getShoppingListCollectionRef() = FireStoreConUtils.getDbConnection().collection(SHOPPING_LIST_COLLECTION_NAME)
}