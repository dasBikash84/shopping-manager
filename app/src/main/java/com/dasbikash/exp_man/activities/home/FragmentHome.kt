package com.dasbikash.exp_man.activities.home

import android.content.Context
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

abstract class FragmentHome:Fragment() {
    @StringRes
    abstract fun getPageTitleId():Int
}