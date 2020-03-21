package com.dasbikash.exp_man.activities.home

import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

abstract class FragmentHome:Fragment() {
    @CallSuper
    override fun onAttach(context: Context) {
        super.onAttach(context)
        context as ActivityHome
    }

    @StringRes
    abstract fun getPageTitleId():Int
}