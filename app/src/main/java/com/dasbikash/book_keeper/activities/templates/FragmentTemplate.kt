package com.dasbikash.book_keeper.activities.templates

import android.content.Context
import androidx.fragment.app.Fragment
import com.dasbikash.menu_view.MenuView

abstract class FragmentTemplate:Fragment() {

    open fun getOptionsMenu(context: Context):MenuView?{
        return null
    }

    open fun getPageTitle(context: Context):String?{
        return null
    }

    open fun getExitPrompt():String?{
        return null
    }

    open fun hidePageTitle():Boolean = false
}