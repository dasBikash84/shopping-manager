package com.dasbikash.exp_man.activities.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.home.ActivityHome
import kotlinx.android.synthetic.main.fragment_login.*


class FragmentLogin : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_guest_login.setOnClickListener {
            runWithActivity {
                it.finish()
                (it as ActivityLogin).startActivity(ActivityHome.getGuestInstance(it))
            }
        }
        btn_sign_up.setOnClickListener {
            runWithActivity {
                (it as ActivityLogin).addFragment(FragmentSignUp())
            }
        }
    }

}
