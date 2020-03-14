package com.dasbikash.exp_man.activities.login

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.*
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.home.ActivityHome
import com.dasbikash.exp_man.utils.ValidationUtils
import com.dasbikash.exp_man_repo.AuthRepo
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import com.dasbikash.snackbar_ext.showLongSnack
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
            launchSignUp()
        }

        btn_login.setOnClickListener {
            hideKeyboard()
            launchLogin()
        }

        btn_login_benefits.setOnClickListener {
            runWithContext {
                val loginBenefitsText = TextView(it)
                loginBenefitsText.setTextSize(TypedValue.COMPLEX_UNIT_SP,16.0f)
                loginBenefitsText.setTextColor(Color.BLACK)
                val padding = dpToPx(8,it).toInt()
                loginBenefitsText.setPadding(padding,padding,padding,padding)
                loginBenefitsText.displayHtmlText(it.getString(R.string.login_benefits_text))
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    view = loginBenefitsText,
                    negetiveButtonText = ""
                ))
            }
        }

        log_in_option_selector.setOnItemSelectedListener(object :AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                runWithContext {
                    hideKeyboard()
                    lifecycleScope.launch {
                        setLoginViewItems(position)
                    }
                }
            }
        })

        btn_send_code.setOnClickListener {
            hideKeyboard()
            sendCodeAction()
        }
    }

    private fun launchLogin() {
        if (!ValidationUtils.validateEmailAddress(et_email.text.toString())){
            et_email.setError(getString(R.string.invalid_email_error))
            return
        }
        if (et_password.text.isNullOrEmpty()){
            et_password.setError(getString(R.string.invalid_password_error))
            return
        }
        runWithContext {
            NetworkMonitor.runWithNetwork(it,{loginTask(it)})
        }
    }

    private fun launchSignUp() {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.load_sign_up_prompt),
                doOnPositivePress = {
                    runWithActivity {
                        (it as ActivityLogin).addFragment(FragmentSignUp())
                    }
                }
            ))
        }
    }

    private fun sendCodeAction() {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.send_login_code_prompt),
                doOnPositivePress = {
                    et_mobile.text.toString().let {
                        if (ValidationUtils.validateBdMobileNumber(it)) {
                            sendCodeTask(it.trim())
                        } else {
                            et_mobile.error = getString(R.string.invalid_mobile_number_error)
                        }
                    }
                }
            ))
        }
    }

    private fun sendCodeTask(phone:String) {
        runWithActivity {
            NetworkMonitor.runWithNetwork(it,{
                lifecycleScope.launch {
                    showWaitScreen()
                    try {
                        AuthRepo.sendLoginCodeToMobile(ValidationUtils.sanitizeNumber(phone), it)
                        loadCodeVerificationScreen()
                    }catch (ex:Throwable){
                        ex.printStackTrace()
                        showShortSnack(R.string.login_code_send_failure)
                        hideWaitScreen()
                    }
                }
            })
        }
    }

    private fun loadCodeVerificationScreen() {
        runWithActivity {
            (it as ActivityLogin).addFragment(FragmentCodeVerify())
        }
    }

    private fun setLoginViewItems(position: Int) {
        if (position == 0) {
            enableEmailLoginViewItems()
        } else {
            enableSmsLoginViewItems()
        }
    }

    private fun enableEmailLoginViewItems() {
        et_email_holder.show()
        et_password_holder.show()
        btn_login.show()
        et_mobile_holder.hide()
        btn_send_code.hide()
    }

    private fun enableSmsLoginViewItems() {
        et_email_holder.hide()
        et_password_holder.hide()
        btn_login.hide()
        et_mobile_holder.show()
        btn_send_code.show()
    }

    private fun loginTask(context: Context) {
        showWaitScreen()
        lifecycleScope.launch {
            try {
                AuthRepo
                    .logInUserWithEmailAndPassword(
                        context,
                        et_email.text!!.toString(), et_password.text!!.toString()
                    )
                runWithActivity {
                    it.finish()
                    (it as ActivityLogin).startActivity(ActivityHome.getUserInstance(it))
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
                hideWaitScreen()
                showLongSnack(
                    R.string.invalid_credentials_message,
                    context.getText(R.string.reset_password_text),
                    { resetPasswordLauncher() })
            }
        }
    }

    private fun resetPasswordLauncher() {
        runWithContext {
                val editText = EditText(it)
                editText.hint = it.getString(R.string.email_hint)
                editText.setText(et_email.text.toString().trim())
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                        title = it.getString(R.string.reset_password_hint),
                        view = editText,
                        positiveButtonText = it.getString(R.string.yes),
                        doOnPositivePress = {
                            NetworkMonitor.runWithNetwork(it){resetPassword(editText.text.toString().trim())}
                        }
                    ))
                }
        }

    private fun resetPassword(email:String) {
        lifecycleScope.launch {
            showWaitScreen()
            if (AuthRepo.sendPasswordResetEmail(email)){
                showShortSnack(R.string.password_reset_email_sent)
            }else{
                showShortSnack(R.string.invalid_email_error)
            }
            hideWaitScreen()
        }
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen

    companion object{
    }
}
