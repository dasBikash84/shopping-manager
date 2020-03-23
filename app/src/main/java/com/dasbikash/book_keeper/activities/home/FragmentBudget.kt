package com.dasbikash.book_keeper.activities.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.dasbikash.book_keeper.R

/**
 * A simple [Fragment] subclass.
 */
class FragmentBudget : FragmentHome() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_budget, container, false)
    }

    override fun getPageTitleId() = R.string.bmi_budget
}
