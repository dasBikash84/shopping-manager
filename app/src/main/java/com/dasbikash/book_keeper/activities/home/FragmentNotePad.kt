package com.dasbikash.book_keeper.activities.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate

class FragmentNotePad : FragmentTemplate() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_note_pad, container, false)
    }

    override fun getPageTitle(context: Context):String? = context.getString(R.string.bmi_notepad)
}
