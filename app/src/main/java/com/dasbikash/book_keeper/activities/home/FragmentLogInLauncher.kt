package com.dasbikash.book_keeper.activities.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dasbikash.pop_up_message.DialogUtils
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.startActivity

import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.login.ActivityLogin
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import kotlinx.android.synthetic.main.fragment_login_launcher.*

class FragmentLogInLauncher private constructor() : FragmentTemplate() {

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

    override fun getPageTitle(context: Context):String? = arguments?.getString(ARG_TITLE)// context.getString(R.string.app_name)

    companion object{
        private const val ARG_TITLE =
            "com.dasbikash.book_keeper.activities.home.FragmentLogInLauncher.ARG_TITLE"

        fun getInstance(title: String?): FragmentLogInLauncher {
            val arg = Bundle()
            arg.putString(ARG_TITLE, title)
            val fragment = FragmentLogInLauncher()
            fragment.arguments = arg
            return fragment
        }

    }
}
