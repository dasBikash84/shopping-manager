package com.dasbikash.exp_man.activities.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.home.ActivityHome
import com.dasbikash.exp_man.utils.AsyncUtils
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.launch


class FragmentLogin : Fragment(),WaitScreenOwner {

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

        btn_login.setOnClickListener {
            runWithContext {
                AsyncUtils.runWithNetwork(
                    {
                        logInTask(it)
                    }, it
                )
            }
        }
    }

    private fun logInTask(context: Context) {
        showWaitScreen()
        lifecycleScope.launch {
            try {
                AuthRepo
                    .logInUserWithEmailAndPassword(context,
                        et_email.text!!.toString(),et_password.text!!.toString())
                runWithActivity {
                    it.finish()
                    (it as ActivityLogin).startActivity(ActivityHome.getUserInstance(it))
                }
            }catch (ex:Throwable){
                ex.printStackTrace()
                hideWaitScreen()
                showShortSnack(R.string.invalid_credentials_message)
            }
        }
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen
}
