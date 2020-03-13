package com.dasbikash.exp_man.activities.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.dasbikash.exp_man.R

/**
 * A simple [Fragment] subclass.
 */
class FragmentExpSummary : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_exp_summary, container, false)
    }

}
