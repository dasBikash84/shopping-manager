package com.dasbikash.exp_man.activities.login

import androidx.fragment.app.Fragment
import com.dasbikash.exp_man.R
import com.dasbikash.super_activity.SingleFragmentSuperActivity

class ActivityLogin : SingleFragmentSuperActivity() {

    override fun getDefaultFragment(): Fragment = FragmentLogin()

    override fun getLayoutID(): Int = R.layout.activity_login

    override fun getLoneFrameId(): Int = R.id.lone_frame
}