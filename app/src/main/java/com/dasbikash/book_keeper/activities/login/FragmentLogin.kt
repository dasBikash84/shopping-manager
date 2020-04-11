package com.dasbikash.book_keeper.activities.login

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.*
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.home.ActivityHome
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.application.BookKeeperApp
import com.dasbikash.book_keeper.models.SupportedLanguage
import com.dasbikash.book_keeper.rv_helpers.StringListAdapter
import com.dasbikash.book_keeper.utils.ValidationUtils
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.shared_preference_ext.SharedPreferenceUtils
import com.dasbikash.snackbar_ext.showLongSnack
import com.dasbikash.snackbar_ext.showShortSnack
import com.jaredrummler.materialspinner.MaterialSpinner
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.launch


class FragmentLogin : FragmentTemplate(),WaitScreenOwner {

    override fun hidePageTitle(): Boolean = true

    private val stringListAdapter = StringListAdapter({view,text->
        (view as TextView).text = text.trim()
    },{onSuggestionClick(it)})

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    private fun onSuggestionClick(suggestionText: String) {
        if (log_in_option_selector.selectedIndex == 0){
            et_mobile.setText(suggestionText)
        }else{
            et_email.setText(suggestionText)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        rv_user_id_suggestions.adapter = stringListAdapter

        btn_guest_login.setOnClickListener {
            runWithActivity {
                it.finish()
                (it as ActivityLogin).startActivity(ActivityHome::class.java)
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
                val dialogView = LayoutInflater.from(it).inflate(R.layout.view_login_benefits,null,false)
                val checkBox = dialogView.findViewById<AppCompatCheckBox>(R.id.cb_hide_login_benefits)
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    view = dialogView,
                    negetiveButtonText = "",
                    doOnPositivePress = {
                        if (checkBox.isChecked){
                            hideLoginBenefits(it)
                            btn_login_benefits.hide()
                        }
                    }
                ))
            }
        }

        log_in_option_selector.setOnItemSelectedListener(object : MaterialSpinner.OnItemSelectedListener<String>{
            override fun onItemSelected(
                view: MaterialSpinner?,
                position: Int,
                id: Long,
                item: String?
            ) {

                runWithContext {
                    hideKeyboard()
                    lifecycleScope.launch {
                        setLoginViewItems(position)
                    }
                }
            }
        })

        log_in_option_selector.setItems(resources.getStringArray(R.array.log_in_options).toList().apply { debugLog(this) })

        btn_send_code.setOnClickListener {
            hideKeyboard()
            sendCodeAction()
        }

        et_email.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(input: Editable?) {
                (input?.toString() ?: "").let {
                    viewModel.postUserIdInput(it)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        et_mobile.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(input: Editable?) {
                (input?.toString() ?: "").let {
                    viewModel.postUserIdInput(it)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btn_bn_lang.setOnClickListener {
            setBnLangAction()
        }

        btn_en_lang.setOnClickListener {
            setEnLangAction()
        }

        viewModel.getUserIdSuggestions().observe(this,object : Observer<List<String>>{
            override fun onChanged(suggestions: List<String>?) {
                (suggestions ?: emptyList()).let {
                    if (it.isEmpty()){
                        rv_user_id_suggestions.hide()
                    }else{
                        rv_user_id_suggestions.show()
                        rv_user_id_suggestions.bringToFront()
                    }
                    stringListAdapter.submitList(it)
                }
            }
        })

        enableSmsLoginViewItems()
    }

    private fun setEnLangAction() {
        runWithActivity {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.switch_to_en_prompt),
                doOnPositivePress = {
                    BookKeeperApp.changeLanguageSettings(it,SupportedLanguage.ENGLISH)
                }
            ))
        }
    }

    private fun setBnLangAction() {
        runWithActivity {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.switch_to_bn_prompt),
                doOnPositivePress = {
                    BookKeeperApp.changeLanguageSettings(it,SupportedLanguage.BANGLA)
                }
            ))
        }
    }

    override fun onResume() {
        super.onResume()
        displayLanguageOpButtons()
    }

    private fun displayLanguageOpButtons() {
        runWithContext {
            when (BookKeeperApp.getLanguageSetting(it)) {
                SupportedLanguage.ENGLISH -> {
                    btn_bn_lang.show()
                    btn_en_lang_inactive.show()
                    btn_en_lang.hide()
                    btn_bn_lang_inactive.hide()
                }
                SupportedLanguage.BANGLA -> {
                    btn_bn_lang.hide()
                    btn_en_lang_inactive.hide()
                    btn_en_lang.show()
                    btn_bn_lang_inactive.show()
                }
            }
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
        runWithActivity {
            (it as ActivityLogin).addFragment(FragmentSignUp())
        }
    }

    private fun sendCodeAction() {
//        runWithContext {
//            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
//                message = it.getString(R.string.send_login_code_prompt),
//                doOnPositivePress = {
                    et_mobile.text.toString().let {
                        if (ValidationUtils.validateBdMobileNumber(it)) {
                            sendCodeTask(it.trim())
                        } else {
                            et_mobile.error = getString(R.string.invalid_mobile_number_error)
                        }
                    }
//                }
//            ))
//        }
    }

    private fun sendCodeTask(phone:String) {
        runWithActivity {
            NetworkMonitor.runWithNetwork(it,{
                lifecycleScope.launch {
                    showWaitScreen()
                    try {
                        AuthRepo.sendLoginCodeToMobile(ValidationUtils.sanitizeNumber(phone).apply { debugLog(it) }, it)
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
        debugLog("setLoginViewItems: $position")
        if (position == 0) {
            enableSmsLoginViewItems()
        } else {
            enableEmailLoginViewItems()
        }
    }

    private fun enableEmailLoginViewItems() {
        debugLog("enableEmailLoginViewItems")
        et_email.setText("")
        et_mobile.setText("")
        et_email_holder.show()
        et_password_holder.show()
        btn_login.show()
        btn_sign_up.show()
        et_mobile_holder.hide()
        btn_send_code.hide()
    }

    private fun enableSmsLoginViewItems() {
        debugLog("enableSmsLoginViewItems")
        et_email.setText("")
        et_mobile.setText("")
        et_email_holder.hide()
        et_password_holder.hide()
        btn_login.hide()
        btn_sign_up.hide()
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
                LoginViewModel.saveUserId(context,et_email.text.toString())
                runWithActivity {
                    it.finish()
                    it.startActivity(ActivityHome::class.java)
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
        private const val LOGIN_BENEFITS_SHOW_FLAG_SP_KEY =
            "com.dasbikash.exp_man.activities.login.FragmentLogin.LOGIN_BENEFITS_SHOW_FLAG_SP_KEY"

        private fun hideLoginBenefits(context: Context) =
            SharedPreferenceUtils
                .getDefaultInstance()
                .saveDataSync(context,false, LOGIN_BENEFITS_SHOW_FLAG_SP_KEY)

        private fun isLoginBenefitsVisible(context: Context):Boolean{
            SharedPreferenceUtils
                .getDefaultInstance()
                .getData(context,LOGIN_BENEFITS_SHOW_FLAG_SP_KEY,Boolean::class.java).let {
                    if (it==null){
                        SharedPreferenceUtils
                            .getDefaultInstance()
                            .saveDataSync(context,true, LOGIN_BENEFITS_SHOW_FLAG_SP_KEY)
                        return true
                    }
                    return it
                }
        }
    }
}
