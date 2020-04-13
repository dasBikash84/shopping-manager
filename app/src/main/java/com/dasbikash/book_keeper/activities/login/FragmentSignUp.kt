package com.dasbikash.book_keeper.activities.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.hideKeyboard
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.utils.ValidationUtils
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentSignUp : FragmentTemplate(),WaitScreenOwner {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun getExitPrompt(): String? = mExitPrompt

    private var mExitPrompt:String?=null

    override fun getPageTitle(context: Context): String? {
        return getString(R.string.signup_heading)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_back_to_login.setOnClickListener {
            runWithActivity {
                (it as ActivityLogin).onBackPressed()
            }
        }

        btn_signup.setOnClickListener {
            hideKeyboard()
            runWithContext { NetworkMonitor.runWithNetwork(it) {signUpClickAction()}}
        }

        mExitPrompt = getString(R.string.quit_sign_up_prompt)
    }

    private fun signUpClickAction() {
        if (!ValidationUtils.validateEmailAddress(et_email.text.toString())){
            et_email.setError(getString(R.string.invalid_email_error))
            return
        }
        if (et_password.text.isNullOrEmpty()){
            et_password.setError(getString(R.string.invalid_password_error))
            return
        }
        if (et_confirm_password.text.isNullOrEmpty()){
            et_confirm_password.setError(getString(R.string.invalid_password_error))
            return
        }
        if (et_confirm_password.text.toString() != et_password.text.toString()){
            et_confirm_password.setError(getString(R.string.password_mismatch_error))
            return
        }
        if (et_first_name.text.isNullOrEmpty()){
            et_first_name.setError(getString(R.string.invalid_first_name))
            return
        }
        if (!et_mobile.text.isNullOrBlank() &&
                !ValidationUtils.validateBdMobileNumber(et_mobile.text.toString())){
            et_mobile.setError(getString(R.string.invalid_mobile_number_error))
            return
        }
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                title = it.getString(R.string.launch_sign_up_prompt),
                doOnPositivePress = {
                    signUpTask(et_email.text!!.trim().toString(),et_password.text.toString(),
                        et_first_name.text!!.trim().toString(),
                        et_last_name.text?.trim().toString(),
                        et_mobile.text?.trim().toString())
                }
            ))
        }
    }

    private fun signUpTask(email:String,password:String,
                            firstName:String,lastName:String,mobile:String){
        runWithContext {
            lifecycleScope.launch {
                showWaitScreen()
                try {
                    AuthRepo
                        .createUserWithEmailAndPassword(email, password,firstName, lastName, mobile)
                    showShortSnack(R.string.sign_up_success_mesage)
                    delay(2000)
                    mExitPrompt = null
                    runWithActivity {
                        (it as ActivityLogin).onBackPressed()
                    }
                }catch (ex:Throwable){
                    AuthRepo.resolveSignUpException(ex).let {
                        showShortSnack(it)
                    }
                    hideWaitScreen()
                }
            }
        }
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen
}
