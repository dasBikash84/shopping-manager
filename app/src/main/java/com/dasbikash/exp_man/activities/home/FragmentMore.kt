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
import com.dasbikash.exp_man.activities.login.ActivityLogin
import com.dasbikash.exp_man_repo.AuthRepo
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.coroutines.launch

class FragmentMore : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bt_log_out.setOnClickListener {
            signOutAction()
        }
        initView()
    }

    private fun initView() {
        lifecycleScope.launch {
            runWithContext {
                when(AuthRepo.checkLogIn(it)){
                    true -> bt_log_out.show()
                    false -> bt_log_out.hide()
                }
            }
        }
    }

    private fun signOutAction() {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                title = it.getString(R.string.sign_out_prompt),
                doOnPositivePress = {signOutTask()}
            ))
        }
    }

    private fun signOutTask() {
        runWithActivity {
            AuthRepo.signOut(it)
            it.finish()
            it.startActivity(ActivityLogin::class.java)
        }
    }

}
