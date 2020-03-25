package com.dasbikash.book_keeper.activities.home

import android.content.Context
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.dasbikash.menu_view.MenuView

abstract class FragmentHome:Fragment() {
    @StringRes
    abstract fun getPageTitleId():Int

    open fun getOptionsMenu(context: Context):MenuView?{
        return null
    }
}