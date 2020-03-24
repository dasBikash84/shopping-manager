package com.dasbikash.book_keeper_repo.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.*

@Keep
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = ShoppingList::class,
            parentColumns = ["id"],
            childColumns = ["shoppingListId"]
        ),
        ForeignKey(
            entity = ExpenseEntry::class,
            parentColumns = ["id"],
            childColumns = ["expenseEntryId"]
        ),
        ForeignKey(
            entity = ExpenseCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"]
        )
    ],
    indices = arrayOf(
        Index(value = ["shoppingListId"], unique = false),
        Index(value = ["expenseEntryId"], unique = false),
        Index(value = ["categoryId"], unique = false)
    )
)
data class ShoppingListItem(
    @PrimaryKey
    var id:String= "",
    var shoppingListId:String?=null,
    var expenseEntryId:String?=null,
    var categoryId: String?=null,
    var name:String?=null,
    var details:String?=null,
    var minUnitPrice:Double?=null,
    var maxUnitPrice:Double?=null,
    var qty:Double=1.0,
    var uom:String?=null,
    var uomBangla:String?=null,
    var brandNameSuggestions:List<String>?=null,
    var images:List<String>?=null,
    var modified: Date = Date()
)