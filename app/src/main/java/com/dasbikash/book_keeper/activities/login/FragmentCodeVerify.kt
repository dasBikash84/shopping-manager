package com.dasbikash.book_keeper.activities.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.startActivity
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.ActivityHome
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_code_verify.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentCodeVerify : FragmentTemplate(),WaitScreenOwner {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_code_verify, container, false)
    }

    override fun getExitPrompt(): String? {
        return getString(R.string.phone_code_verify_fragment_exit_prompt)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_verify_code.setOnClickListener {
            verifyCodeAction()
        }

        btn_resend_code.setOnClickListener {
            resendCodeAction()
        }
    }

    private fun resendCodeAction() {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.send_login_code_prompt),
                doOnPositivePress = {
                    sendCodeTask()
                }
            ))
        }
    }

    private fun sendCodeTask() {
        runWithActivity {
            NetworkMonitor.runWithNetwork(it,{
                lifecycleScope.launch {
                    showWaitScreen()
                    try {
                        AuthRepo.getCurrentMobileNumber(it)?.apply {
                            println(this)
                            AuthRepo.sendLoginCodeToMobile(this, it)
                            showShortSnack(R.string.login_code_resend)
                            hideWaitScreen()
                            refreshResendStatus()
                        }
                    }catch (ex:Throwable){
                        ex.printStackTrace()
                        hideWaitScreen()
                        showShortSnack(R.string.login_code_send_failure)
                    }
                }
            })
        }
    }

    private fun verifyCodeAction() {
        if (et_verification_code.text.toString().isBlank()){
            et_verification_code.error = getString(R.string.empty_verification_code_error)
            return
        }
        runWithContext {
            NetworkMonitor.runWithNetwork(it){verifyCodeTask(it,et_verification_code.text.toString().trim())}
        }
    }

    private fun verifyCodeTask(context: Context,code:String) {
        lifecycleScope.launch {
            showWaitScreen()
            try {
                AuthRepo.logInUserWithVerificationCode(context,code).let {
                    processLogin(context,it)
                }
            }catch (ex:Throwable){
                ex.printStackTrace()
                showShortSnack(R.string.invalid_verification_code_message)
                hideWaitScreen()
            }
        }
    }

    private fun processLogin(context: Context,user: User){
        (user.phone!!).let {
            LoginViewModel.saveUserId(context,it)
        }
        runWithActivity {
            it.finish()
            it.startActivity(ActivityHome::class.java)
        }
    }

    override fun onResume() {
        super.onResume()
        runWithContext {
            lifecycleScope.launch {
                showWaitScreen()
                AuthRepo.checkIfAlreadyVerified(it)?.apply {
                    processLogin(it,this)
                }
                hideWaitScreen()
                refreshResendStatus()
            }
        }
    }

    private suspend fun refreshResendStatus() {
        AuthRepo.codeResendWaitMs(context!!).let {
            startResendTimeTracker(it)
        }
    }

    private suspend fun startResendTimeTracker(waitTimeMs:Long) {
        if (waitTimeMs>0) {
            btn_resend_code.isEnabled = false
            btn_resend_code.setText(getString(R.string.resend_login_code_button,(waitTimeMs/1000).toString()))
            delay(1000)
            refreshResendStatus()
        }else{
            btn_resend_code.isEnabled = true
            btn_resend_code.setText(R.string.resend_code_button)
        }
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen
}
