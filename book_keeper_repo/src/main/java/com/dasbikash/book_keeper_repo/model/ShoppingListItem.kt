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
        )
    ],
    indices = arrayOf(
        Index(value = ["shoppingListId"], unique = false),
//        Index(value = ["expenseEntryId"], unique = false),
        Index(value = ["categoryId"], unique = false)
    )
)
data class ShoppingListItem(
    @PrimaryKey
    var id:String= UUID.randomUUID().toString(),
    var name:String?=null,
    var shoppingListId:String?=null,
    var expenseEntryId:String?=null,
    var categoryId: Int=0,
    var details:String?=null,
    var minUnitPrice:Double?=null,
    var maxUnitPrice:Double?=null,
    var qty:Double=1.0,
    var uom:Int=0,
    var brandNameSuggestions:List<String>?=null,
    var images:List<String>?=null,
    var modified: Date = Date()
){
    fun updateModified(){this.modified = Date()}

    fun toExpenseEntry(user: User):ExpenseEntry{
        val expenseEntry = ExpenseEntry(userId = user.id,categoryId = categoryId,details = details ?: name,time = Date())
        val expenseItem = ExpenseItem(name=name,unitPrice = minUnitPrice ?: maxUnitPrice ?: 0.0,qty = qty,uom = uom)
        expenseEntry.expenseItems = listOf(expenseItem)
        return expenseEntry
    }

    fun calculatePriceRange():Pair<Double?,Double?>{
        return Pair(minUnitPrice?.let { it*qty },maxUnitPrice?.let { it*qty })
    }
    companion object{
        const val MAX_PRODUCT_IMAGE_COUNT = 4
    }
}