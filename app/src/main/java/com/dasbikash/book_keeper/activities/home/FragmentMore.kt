package com.dasbikash.book_keeper.activities.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.*

import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.calculator.ActivityCalculator
import com.dasbikash.book_keeper.activities.login.ActivityLogin
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.AuthRepo
import kotlinx.android.synthetic.main.fragment_more.*
import kotlinx.coroutines.launch

class FragmentMore : FragmentTemplate() {

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
        tv_user_name.setOnClickListener {
            runWithContext {
                lifecycleScope.launch {
                    val userDetails = TextView(it)
                    userDetails.text = AuthRepo.getUser(it)?.toString()
                    DialogUtils.showAlertDialog(
                        it, DialogUtils.AlertDialogDetails(
                            negetiveButtonText = "",
                            view = userDetails
                        )
                    )
                }
            }
        }
        initView()
    }

    private fun initView() {
        runWithContext {
            when(AuthRepo.checkLogIn()){
                true -> tv_log_out.show()
                false -> tv_log_out.hide()
            }
            lifecycleScope.launch {
                AuthRepo.getUser(it)?.let { tv_user_name.text = it.email ?: it.phone }
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

    override fun getPageTitle(context: Context):String? = context.getString(R.string.app_name)
}
