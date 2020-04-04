package com.dasbikash.book_keeper.activities.sl_import

import com.dasbikash.book_keeper.activities.templates.ActivityTemplate
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate

class ActivityShoppingListImport : ActivityTemplate() {
    override fun registerDefaultFragment(): FragmentTemplate = FragmentShoppingListImport()
}
