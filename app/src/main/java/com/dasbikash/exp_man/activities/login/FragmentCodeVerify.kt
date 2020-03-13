package com.dasbikash.exp_man.activities.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope

import com.dasbikash.exp_man.R
import com.dasbikash.exp_man_repo.AuthRepo
import kotlinx.android.synthetic.main.fragment_code_verify.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentCodeVerify : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_code_verify, container, false)
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            refreshResendStatus()
        }
    }

    private suspend fun refreshResendStatus() {
        AuthRepo.codeResendWaitMs(context!!).let {
            startResendTimeTracker(it)
        }
    }

    private suspend fun startResendTimeTracker(waitTimeMs:Long) {
        if (waitTimeMs>0) {
            btn_send_code.isEnabled = false
            btn_send_code.setText(getString(R.string.resend_login_code_button,(waitTimeMs/1000).toString()))
            delay(1000)
            refreshResendStatus()
        }else{
            btn_send_code.isEnabled = true
            btn_send_code.setText(R.string.send_login_code_button)
        }
    }

}
