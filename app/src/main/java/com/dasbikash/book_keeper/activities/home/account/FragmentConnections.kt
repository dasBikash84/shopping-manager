package com.dasbikash.book_keeper.activities.home.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.utils.ValidationUtils
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_connections.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FragmentConnections : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_connections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_search_user.setOnClickListener {
            et_search_user.text.toString().trim().let {
                if (it.isNotBlank()){
                    runUserSearch(it)
                }
            }
        }
    }

    private fun runUserSearch(searchString: String) {
        getUserSearchMethod(searchString).let{
            if (it!=null){
                when (it){
                    UserSearchMethod.EMAIL -> {
                        lifecycleScope.launch {
                            AuthRepo.findUserByEmail(searchString).forEach {
                                debugLog(it)
                            }
                        }
                    }
                    UserSearchMethod.PHONE -> {
                        lifecycleScope.launch {
                            AuthRepo.findUserByPhone(searchString).collect{
                                debugLog(it)
                            }
                        }
                    }
                }
            }else{
                showShortSnack(getString(R.string.invalid_user_search_string))
            }
        }
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
