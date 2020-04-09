package com.dasbikash.book_keeper.activities.home.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_extensions.*
import com.dasbikash.android_image_utils.displayImageFile
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.login.ActivityLogin
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.utils.ValidationUtils
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ImageRepo
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentAccount : FragmentTemplate(),WaitScreenOwner {

    override fun registerWaitScreen(): ViewGroup = wait_screen
    private lateinit var viewModel:ViewModelUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ViewModelUser::class.java)

        logout_block.setOnClickListener {
            signOutAction()
        }

        viewModel.getUserLiveData().observe(this,object : Observer<User>{
            override fun onChanged(user: User?) {
                user?.apply {
                    runWithContext {
                        lifecycleScope.launch {
                            refreshView(it,this@apply)
                        }
                    }
                }
            }
        })

        iv_edit_email.setOnClickListener {
            launchUserParamEditDialog(
                {emailEditTask(it)},
                R.string.email_edit_prompt,
                R.string.email_hint,
                R.string.invalid_email_error
            )
        }

        iv_edit_phone_num.setOnClickListener {
            launchUserParamEditDialog(
                {phoneEditTask(it)},
                R.string.phone_edit_prompt,
                R.string.phone_hint
            )
        }

        iv_edit_first_name.setOnClickListener {
            launchUserParamEditDialog(
                {firstNameEditTask(it)},
                R.string.first_name_edit_prompt,
                R.string.first_name_hint
            )
        }

        iv_edit_last_name.setOnClickListener {
            launchUserParamEditDialog(
                {lastNameEditTask(it)},
                R.string.last_name_edit_prompt,
                R.string.last_name_prompt
            )
        }


        sr_page_holder.setOnRefreshListener {
            runWithContext {
                NetworkMonitor.runWithNetwork(it){
                    lifecycleScope.launch(Dispatchers.IO) {
                        runOnMainThread({showWaitScreen()})
                        AuthRepo.refreshUserData(it)
                        runOnMainThread({
                            sr_page_holder.isRefreshing = false
                            hideWaitScreen()
                        })
                    }
                }.let {
                    if (!it){
                        sr_page_holder.isRefreshing = false
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        runWithContext {
            if (NetworkMonitor.isConnected()){
                lifecycleScope.launch(Dispatchers.IO) {
                    AuthRepo.refreshUserData(it)
                }
            }
        }
    }

    private suspend fun refreshView(context: Context,user: User) {
        when(AuthRepo.isPhoneLogin()){
            true -> {
                iv_edit_email.show()
                iv_edit_phone_num.hide()
            }
            false -> {
                iv_edit_email.hide()
                iv_edit_phone_num.show()
            }
        }
        user.apply {
            tv_email_id_text.text = email?.trim() ?: ""
            tv_phone_num.text = phone?.trim() ?: ""
            tv_first_name.text = firstName?.trim() ?: ""
            tv_last_name.text = lastName?.trim() ?: ""
            photoUrl?.let {
                ImageRepo
                    .downloadImageFile(context,it)
                    ?.let {
                        iv_user_image.displayImageFile(it)
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

    override fun getPageTitle(context: Context):String? = context.getString(R.string.app_name)

    private fun launchUserParamEditDialog(paramEditTask:suspend (String)->Unit, @StringRes promptId:Int,
                                          @StringRes hintId:Int?,@StringRes errorMessageId:Int?=null){
        runWithContext {
            val view = EditText(it)
            hintId?.apply { view.hint = it.getString(this) }
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(promptId),
                positiveButtonText = it.getString(R.string.save_text),
                negetiveButtonText = it.getString(R.string.cancel),
                view = view,
                doOnPositivePress = {
                    lifecycleScope.launch {
                        if (view.text.toString().isNotBlank()) {
                            showWaitScreen()
                            paramEditTask(view.text.toString().trim())
                            hideWaitScreen()
                        }else{
                            showShortSnack(errorMessageId?.let { context!!.getString(it) } ?: it.getString(R.string.empty_input_message))
                        }
                    }
                }
            ))
        }
    }

    private suspend fun emailEditTask(inputEmail:String){
        if (ValidationUtils.validateEmailAddress(inputEmail)) {
            context?.let { AuthRepo.updateUserEmail(it,inputEmail)}
        } else {
            showShortSnack(R.string.invalid_email_error)
        }
    }

    private suspend fun phoneEditTask(inputPhone:String){
        context?.let {
            AuthRepo.updatePhone(it, inputPhone)
        }
    }

    private suspend fun firstNameEditTask(inputFirstName:String){
        context?.let {
            AuthRepo.updateFirstName(it, inputFirstName)
        }
    }

    private suspend fun lastNameEditTask(inputLastName:String){
        context?.let {
            AuthRepo.updateLastName(it, inputLastName)
        }
    }
}
