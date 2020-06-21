package com.dasbikash.book_keeper.activities.login

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import com.dasbikash.pop_up_message.DialogUtils
import com.dasbikash.android_extensions.hideKeyboard
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.application.BookKeeperApp
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.CountryRepo
import com.dasbikash.book_keeper_repo.model.Country
import com.dasbikash.pop_up_message.showIndefiniteSnack
import com.dasbikash.pop_up_message.showLongSnack
import com.dasbikash.pop_up_message.showShortSnack
import kotlinx.android.synthetic.main.fragment_sign_up.*
import kotlinx.android.synthetic.main.view_mobile_number_input.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentSignUp : FragmentTemplate(),WaitScreenOwner {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun getExitPrompt(): String? = mExitPrompt

    private var mExitPrompt:String?=null

    override fun getPageTitle(context: Context): String? {
        return getString(R.string.signup_heading)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_back_to_login.setOnClickListener {
            runWithActivity {
                (it as ActivityLogin).onBackPressed()
            }
        }

        btn_signup.setOnClickListener {
            hideKeyboard()
            runWithContext {
                NetworkMonitor
                    .runWithNetwork(it) {
                        lifecycleScope.launch {
                            signUpClickAction(it)
                        }
                    }
            }
        }

        initCallingCodeSelector()

        mExitPrompt = getString(R.string.quit_sign_up_prompt)
    }

    private val callingCodes = mutableListOf<String>()

    private fun initCallingCodeSelector() {
        phone_prefix_selector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                tv_calling_code.text = Country.getCallingCodeFromDisplayText(callingCodes.get(position))
                tv_calling_code.bringToFront()
            }
        }

        runWithContext {
            lifecycleScope.launchWhenCreated {
                val countries = CountryRepo.getCountryData(it)
                setCallingCodes(
                    countries,
                    CountryRepo.getCurrentCountry(it,countries)
                )
            }
        }
    }

    private fun setCallingCodes(countries: List<Country>, currentCountry: Country?) {
        callingCodes.addAll(countries.map { it.displayText() })
        val callingCodeListAdapter = ArrayAdapter<String>(
            context!!,
            R.layout.view_spinner_item,
            callingCodes.toList())
        callingCodeListAdapter.setDropDownViewResource(R.layout.view_spinner_item)
        phone_prefix_selector.adapter = callingCodeListAdapter
        currentCountry?.let {
            phone_prefix_selector.setSelection(
                countries.indexOf(it).let { if (it<0) {0} else {it} }
            )
        }
    }

    private fun signUpClickAction(context: Context) {

        if (et_password.text.isNullOrEmpty()){
            et_password.setError(getString(R.string.invalid_password_error))
            showShortSnack(R.string.invalid_password_error)
            return
        }
        if (et_confirm_password.text.isNullOrEmpty()){
            et_confirm_password.setError(getString(R.string.invalid_password_error))
            showShortSnack(R.string.invalid_password_error)
            return
        }
        if (et_confirm_password.text.toString() != et_password.text.toString()){
            et_confirm_password.setError(getString(R.string.password_mismatch_error))
            showShortSnack(R.string.password_mismatch_error)
            return
        }
        showSignUpPrompt(context)
    }

    private fun showSignUpPrompt(context: Context): AlertDialog {
        return DialogUtils.showAlertDialog(context, DialogUtils.AlertDialogDetails(
            title = getString(R.string.launch_sign_up_prompt),
            doOnPositivePress = {
                signUpTask(
                    et_email.text!!.trim().toString(),
                    et_password.text.toString(),
                    et_first_name.text!!.trim().toString(),
                    et_last_name.text?.trim().toString(),
                    et_mobile.text?.trim().toString()
                )
            }
        ))
    }

    private fun signUpTask(email:String,password:String,
                            firstName:String,lastName:String,mobile:String){
        runWithContext {
            lifecycleScope.launch {
                showWaitScreen()
                    try {
                        var fullNumber = mobile
                        if (mobile.isNotBlank()) {
                            val country =
                                Country.getCountryFromDisplayText(it,phone_prefix_selector.selectedItem as String)
                            fullNumber = country.fullNumber(mobile)
                            if (!country.checkNumber(mobile)){
                                et_mobile.setError(getString(R.string.number_format_error_prompt))
                                hideWaitScreen()
                                return@launch
                            }
                            if (AuthRepo.loginAnonymous()) {
                                AuthRepo.findUsersByPhone(fullNumber).let {
                                    if (it.isNotEmpty()) {
                                        et_mobile.setError(getString(R.string.mobile_number_taken_error))
                                        hideWaitScreen()
                                        return@launch
                                    }
                                }
                            }else{
                                hideWaitScreen()
                                showIndefiniteSnack(R.string.unknown_error_message)
                                return@launch
                            }
                        }
                        AuthRepo
                            .createUserWithEmailAndPassword(
                                it,email, password,firstName,
                                lastName,fullNumber,BookKeeperApp.getLanguageSetting(it)
                            ).apply {
                                showLongSnack(R.string.sign_up_success_mesage)
                                delay(2000)
                                runWithActivity {
                                    ActivityLogin.processLogin(it,this)
                                }
                            }
                    }catch (ex:Throwable){
                        AuthRepo.resolveSignUpException(ex).let {
                            showIndefiniteSnack(it)
                        }
                        hideWaitScreen()
                    }
            }
        }
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen
}
