package com.dasbikash.exp_man.activities.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.startActivity

import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.login.ActivityLogin
import kotlinx.android.synthetic.main.fragment_login_launcher.*

class FragmentLogInLauncher : FragmentHome() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login_launcher, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_login_launcher.setOnClickListener {
            activity?.finish()
            activity?.startActivity(ActivityLogin::class.java)
        }
        btn_why_login.setOnClickListener {
            runWithContext {
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    message = it.getString(R.string.why_login_text),
                    negetiveButtonText = ""
                ))
            }
        }
    }

    override fun getPageTitleId() = R.string.app_name
}
