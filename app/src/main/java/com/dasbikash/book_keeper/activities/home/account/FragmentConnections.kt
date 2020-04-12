package com.dasbikash.book_keeper.activities.home.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.show
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.rv_helpers.SearchedUserAdapter
import com.dasbikash.book_keeper.utils.ValidationUtils
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_connections.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FragmentConnections : Fragment() {

    private val userSearchReasultAdapter = SearchedUserAdapter({doOnSearchedUserClick(it)})

    private fun doOnSearchedUserClick(user: User) {
        debugLog(user)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_user_search_result.adapter = userSearchReasultAdapter
        btn_search_user.setOnClickListener {
            et_search_user.text.toString().trim().let {
                if (it.isNotBlank()){
                    runUserSearch(it)
                }
            }
        }

        btn_close_user_search_result.setOnClickListener {
            user_search_result_holder.hide()
        }
    }

    private fun runUserSearch(searchString: String) {

        user_search_result_holder.hide()
        userSearchReasultAdapter.submitList(emptyList())

        getUserSearchMethod(searchString).let{
            if (it!=null){
                when (it){
                    UserSearchMethod.EMAIL -> {
                        lifecycleScope.launch {
                            AuthRepo.findUserByEmail(searchString.toLowerCase()).let {
                                debugLog(it)
                                if (it.isNotEmpty()){
                                    val users = mutableSetOf<User>()
                                    users.addAll(it)
                                    userSearchReasultAdapter.submitList(users.toList())
                                    user_search_result_holder.show()
                                    user_search_result_holder.bringToFront()
                                }
                            }
                        }
                    }
                    UserSearchMethod.PHONE -> {
                        lifecycleScope.launch {
                            AuthRepo.findUserByPhone(getSanitizedMobileNumber(searchString)).collect{
                                debugLog(it)
                                val users = mutableSetOf<User>()
                                users.addAll(userSearchReasultAdapter.currentList)
                                users.add(it)
                                userSearchReasultAdapter.submitList(users.toList())
                                user_search_result_holder.show()
                                user_search_result_holder.bringToFront()
                            }
                        }
                    }
                }
            }else{
                showShortSnack(getString(R.string.invalid_user_search_string))
            }
        }
    }

    private fun getSanitizedMobileNumber(mobileNumber:String):String{
        if (ValidationUtils.validateBdMobileNumber(mobileNumber)){
            return ValidationUtils.sanitizeNumber(mobileNumber)
        }
        return mobileNumber
    }

    private fun getUserSearchMethod(searchString: String): UserSearchMethod? {
        return if (ValidationUtils.validateEmailAddress(searchString)){
            UserSearchMethod.EMAIL
        }else if (ValidationUtils.validateMobileNumber(searchString)){
            UserSearchMethod.PHONE
        }else{
            null
        }
    }

    @Keep
    private enum class UserSearchMethod{EMAIL,PHONE}

}
