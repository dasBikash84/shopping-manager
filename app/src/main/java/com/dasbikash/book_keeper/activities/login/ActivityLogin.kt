package com.dasbikash.book_keeper.activities.login

import androidx.fragment.app.Fragment
import com.dasbikash.book_keeper.R
import com.dasbikash.super_activity.SingleFragmentSuperActivity

class ActivityLogin : SingleFragmentSuperActivity() {

    override fun getDefaultFragment(): Fragment = FragmentLogin()

    override fun getLayoutID(): Int = R.layout.activity_login

    override fun getLoneFrameId(): Int = R.id.lone_frame
}