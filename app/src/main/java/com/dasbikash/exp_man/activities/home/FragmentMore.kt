package com.dasbikash.exp_man.activities.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.*

import com.dasbikash.exp_man.R
import com.dasbikash.exp_man.activities.calculator.ActivityCalculator
import com.dasbikash.exp_man.activities.login.ActivityLogin
import com.dasbikash.exp_man_repo.AuthRepo
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.coroutines.launch

class FragmentMore : FragmentHome() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tv_log_out.setOnClickListener {
            signOutAction()
        }
        tv_calculator.setOnClickListener{
            runWithActivity { it.startActivity(ActivityCalculator::class.java) }
        }
        initView()
    }

    private fun initView() {
        runWithContext {
            when(AuthRepo.checkLogIn()){
                true -> tv_log_out.show()
                false -> tv_log_out.hide()
            }
        }
    }

    private fun signOutAction() {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                title = it.getString(R.string.log_out_prompt),
                doOnPositivePress = {signOutTask()}
            ))
        }
    }

    private fun signOutTask() {
        runWithActivity {
            AuthRepo.signOut()
            it.finish()
            it.startActivity(ActivityLogin::class.java)
        }
    }

    override fun getPageTitleId() = R.string.app_name
}
