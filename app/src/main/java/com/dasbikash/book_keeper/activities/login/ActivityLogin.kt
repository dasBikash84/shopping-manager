package com.dasbikash.book_keeper.activities.login

import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate

class ActivityLogin : ActivityTemplate() {
    override fun registerDefaultFragment(): FragmentTemplate = FragmentLogin()
}