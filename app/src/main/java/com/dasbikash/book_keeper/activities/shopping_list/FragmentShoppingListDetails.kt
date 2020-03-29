package com.dasbikash.book_keeper.activities.shopping_list

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.dasbikash.menu_view.MenuView

abstract class FragmentShoppingListDetails:Fragment() {
    open fun getOptionsMenu(context: Context):MenuView?{
        return null
    }
}