package com.dasbikash.book_keeper.activities.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.dasbikash.book_keeper.R

// Basically Shopping list will be a list of Shopping items( sub class of Expense items)
// but price range in place of fixed price
// New param for product hint/details
// Sample image option
// User can have multiple shopping lists
// User can check/close each shopping item individually and that will be added to expenses automatically
// User may also share shopping list with connected users/ by QR code.
// Optional remainder


class FragmentShoppingList : FragmentHome() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list, container, false)
    }

    override fun getPageTitleId() = R.string.shopping_list_title
}