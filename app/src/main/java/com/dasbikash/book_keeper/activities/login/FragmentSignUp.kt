package com.dasbikash.book_keeper.activities.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.hideKeyboard
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.utils.ValidationUtils
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentSignUp : Fragment(),WaitScreenOwner {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_back_to_login.setOnClickListener {
            runWithContext {
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    it.getString(R.string.quit_sign_up_prompt),
                    positiveButtonText = it.getString(R.string.yes),
                    negetiveButtonText = it.getString(R.string.no),
                    doOnPositivePress = {
                        runWithActivity {
                            (it as ActivityLogin).onBackPressed()
                        }
                    }
                ))
            }
        }

        btn_signup.setOnClickListener {
            hideKeyboard()
            runWithContext { NetworkMonitor.runWithNetwork(it) {signUpClickAction()}}
        }
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
