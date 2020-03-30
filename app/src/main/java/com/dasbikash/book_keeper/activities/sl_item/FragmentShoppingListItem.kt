package com.dasbikash.book_keeper.activities.sl_item

import android.content.Context
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import com.dasbikash.menu_view.MenuView
import java.lang.IllegalStateException

abstract class FragmentShoppingListItem:Fragment() {

    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            context as ActivityShoppingListItem
        }catch (ex:Throwable){
            throw IllegalStateException(ERROR_MESSAGE_FOR_WRONG_ACTIVITY)
        }
    }

    open fun getOptionsMenu(context: Context):MenuView?{
        return null
    }

    open fun getPageTitle(context: Context):String?{
        return null
    }

    companion object{
        private const val ERROR_MESSAGE_FOR_WRONG_ACTIVITY = "Not attached to ActivityShoppingListItem!!"
    }
}