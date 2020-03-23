package com.dasbikash.book_keeper.activities.home

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

abstract class FragmentHome:Fragment() {
    @StringRes
    abstract fun getPageTitleId():Int
}