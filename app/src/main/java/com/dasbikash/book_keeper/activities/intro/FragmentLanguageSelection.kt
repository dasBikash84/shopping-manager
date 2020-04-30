package com.dasbikash.book_keeper.activities.intro

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.application.BookKeeperApp
import com.dasbikash.book_keeper_repo.model.SupportedLanguage
import kotlinx.android.synthetic.main.fragment_lang_select.*

class FragmentLanguageSelection:FragmentTemplate() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_lang_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        spinner_language_selector.setItems(SupportedLanguage.values().map { it.displayName }.toList())
        btn_next.setOnClickListener {
            nextButtonTask()
        }
        runWithContext {
            BookKeeperApp.getLanguageSetting(it).apply {
                spinner_language_selector.selectedIndex = SupportedLanguage.values().indexOf(this)
            }
        }
    }

    private fun nextButtonTask() {
        runWithContext {
            lifecycleScope.launchWhenResumed {
                setLanguageTask(it)
                ActivityIntro.setLangSelectedFlag(context!!)
                (activity as ActivityIntro?)?.addFragmentClearingBackStack(FragmentAppFeatures())
            }
        }
    }

    private fun setLanguageTask(context: Context) {
        val language = SupportedLanguage.values().get(spinner_language_selector.selectedIndex)
        runWithActivity { BookKeeperApp.changeLanguageSettings(it,language,null)}
    }
}