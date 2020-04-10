package com.dasbikash.book_keeper.activities.home

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
import com.dasbikash.book_keeper.activities.launcher.ActivityLauncher
import com.dasbikash.book_keeper.activities.login.ActivityLogin
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_wait_for_vefification.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.launch

class FragmentWaitForVerification : FragmentTemplate(),WaitScreenOwner {

    override fun registerWaitScreen(): ViewGroup = wait_screen

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wait_for_vefification, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sr_page_holder.setOnRefreshListener {
            refreshAction()
        }

        logout_block.setOnClickListener {
            signOutAction()
        }

        btn_resend_verification_email.setOnClickListener {
            runWithContext {
                NetworkMonitor.runWithNetwork(it) {
                    lifecycleScope.launch {
                        showWaitScreen()
                        AuthRepo.sendEmailVerificationLink(it).let {
                            if (it == null) {
                                showShortSnack(R.string.email_verification_link_sent)
                            } else {
                                showShortSnack(
                                    context!!.getString(
                                        R.string.email_verification_sent_error,
                                        it / 1000
                                    )
                                )
                            }
                        }
                        hideWaitScreen()
                    }
                }
            }
        }

        runWithContext {
            lifecycleScope.launch {
                AuthRepo.getUser(it)?.apply {
                    tv_verification_instruction.text = it.getString(R.string.email_verification_instruction_with_email,email)
                }
            }
        }
    }

    private fun refreshAction() {
        runWithActivity {
            NetworkMonitor.runWithNetwork(it) {
                lifecycleScope.launch {
                    showWaitScreen()
                    if (AuthRepo.refreshLogin()){
                        if (AuthRepo.isVerified()){
                            it.finish()
                            it.startActivity(ActivityLauncher::class.java)
                        }
                    }
                    hideWaitScreen()
                    sr_page_holder.isRefreshing = false
                }
            }.let {
                if (!it){
                    sr_page_holder.isRefreshing = false
                }
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
            AuthRepo.signOut(it)
            it.finish()
            it.startActivity(ActivityLogin::class.java)
        }
    }

    override fun getPageTitle(context: Context):String? = context.getString(R.string.email_verification_prompt)
}
