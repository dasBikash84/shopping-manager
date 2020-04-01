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
    var id:String= UUID.randomUUID().toString(),
    var name:String?=null,
    var shoppingListId:String?=null,
    var expenseEntryId:String?=null,
    var categoryId: Int?=null,
    var details:String?=null,
    var minUnitPrice:Double?=null,
    var maxUnitPrice:Double?=null,
    var qty:Double=1.0,
    var uom:Int?=null,
    var brandNameSuggestions:List<String>?=null,
    var images:List<String>?=null,
    var modified: Date = Date()
){
    fun updateModified(){this.modified = Date()}
    companion object{
        const val MAX_PRODUCT_IMAGE_COUNT = 4
    }
}