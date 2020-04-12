package com.dasbikash.book_keeper.activities.home.account.connections

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.hide
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_extensions.show
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.rv_helpers.ConnectionUserAdapter
import com.dasbikash.book_keeper.rv_helpers.SearchedUserAdapter
import com.dasbikash.book_keeper.utils.OptionsIntentBuilderUtility
import com.dasbikash.book_keeper.utils.ValidationUtils
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ConnectionRequestRepo
import com.dasbikash.book_keeper_repo.model.ConnectionRequest
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_connections.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class FragmentConnections : Fragment(),WaitScreenOwner {

    override fun registerWaitScreen(): ViewGroup = wait_screen

    private val userSearchReasultAdapter = SearchedUserAdapter({addUserAction(it)})
    private val connectedUserAdapter = ConnectionUserAdapter({context, user ->  getMenuViewForConnectedUsers(context, user)})
    private val connectionPendingUserAdapter = ConnectionUserAdapter({context, user ->  getMenuViewForConnectionPendingUsers(context, user)})
    private lateinit var viewModel: ViewModelConnections

    private fun addUserAction(user: User) {
        debugLog(user)
        runWithContext {
            NetworkMonitor.runWithNetwork(it){
                lifecycleScope.launch {
                    showWaitScreen()
                    ConnectionRequestRepo.submitNewRequest(it,user)
                    user_search_result_holder.hide()
                    hideWaitScreen()
                }
            }
        }
    }

    private fun getMenuViewForConnectedUsers(context: Context,user: User):MenuView{
        val menuView = MenuView()

        user.apply {
            if (email!=null){
                menuView.add(getEmailUserMenuItem(context, email!!))
            }
            if (phone!=null){
                menuView.add(getPhoneUserMenuItem(context,phone!!))
            }
            menuView.add(
                MenuViewItem(
                    text = context.getString(R.string.delete_user),
                    task = {
                        DialogUtils.showAlertDialog(context, DialogUtils.AlertDialogDetails(
                            message = context.getString(R.string.delete_user)+"?",
                            doOnPositivePress = {
                                NetworkMonitor.runWithNetwork(context) {
                                    lifecycleScope.launch {
                                        showWaitScreen()
                                        ConnectionRequestRepo.deleteApprovedConnection(context,user)
                                        hideWaitScreen()
                                    }
                                }
                            }
                        ))
                    }
                )
            )
        }
        return menuView
    }

    private fun getMenuViewForConnectionPendingUsers(context: Context,user: User):MenuView{
        debugLog("getMenuViewForConnectionPendingUsers: $user")
        val menuView = MenuView()

        user.apply {
            if (email!=null){
                menuView.add(getEmailUserMenuItem(context, email!!))
            }
            if (phone!=null){
                menuView.add(getPhoneUserMenuItem(context,phone!!))
            }
            menuView.add(
                MenuViewItem(
                    text = context.getString(R.string.delete_con_request),
                    task = {
                        DialogUtils.showAlertDialog(context, DialogUtils.AlertDialogDetails(
                            message = context.getString(R.string.delete_con_request)+"?",
                            doOnPositivePress = {
                                NetworkMonitor.runWithNetwork(context) {
                                    lifecycleScope.launch {
                                        showWaitScreen()
                                        ConnectionRequestRepo.deletePendingConnection(context,user)
                                        hideWaitScreen()
                                    }
                                }
                            }
                        ))
                    }
                )
            )
        }
        return menuView
    }

    private fun getEmailUserMenuItem(context: Context,email:String):MenuViewItem{
        return MenuViewItem(
            text = context.getString(R.string.email_prompt),
            task = {runWithActivity { OptionsIntentBuilderUtility.launchEmailDialog(it,email) }}
        )
    }

    private fun getPhoneUserMenuItem(context: Context,phone:String):MenuViewItem{
        return MenuViewItem(
            text = context.getString(R.string.call_prompt),
            task = {runWithActivity { OptionsIntentBuilderUtility.dialPartner(it,phone) }}
        )
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
        rv_connected_user_list.adapter = connectedUserAdapter
        rv_request_pending_user_list.adapter = connectionPendingUserAdapter
        viewModel = ViewModelProviders.of(this).get(ViewModelConnections::class.java)
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

        sr_page_holder.setOnRefreshListener {
            runWithContext {
                if (!syncConnectionRequestData(it)){
                    sr_page_holder.isRefreshing = false
                }
            }
        }

        viewModel.getRequestedPendingLiveData().observe(this,object : Observer<List<ConnectionRequest>>{
            override fun onChanged(list: List<ConnectionRequest>?) {
                (list ?: emptyList()).let {
                    debugLog(it)
                    if (!it.isEmpty()){
                        val connectionRequests = it
                        runWithContext {
                            lifecycleScope.launch {
                                connectionRequests.map {
                                    AuthRepo.findUserById(context!!,it.partnerUserId!!)!!
                                }.let {
                                    connectionPendingUserAdapter.submitList(it)
                                    request_pending_user_list_holder.show()
                                }
                            }
                        }
                    }else{
                        request_pending_user_list_holder.hide()
                    }
                }
            }
        })

        viewModel.getApprovedLiveData().observe(this,object : Observer<List<ConnectionRequest>>{
            override fun onChanged(list: List<ConnectionRequest>?) {
                (list ?: emptyList()).let {
                    debugLog(it)
                    if (!it.isEmpty()){
                        val connectionRequests = it
                        runWithContext {
                            lifecycleScope.launch {
                                connectionRequests.map {
                                    AuthRepo.findUserById(context!!,it.partnerUserId!!)!!
                                }.let {
                                    connectedUserAdapter.submitList(it)
                                    connected_user_list_holder.show()
                                }
                            }
                        }
                    }else{
                        connected_user_list_holder.hide()
                    }
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        runWithContext { syncConnectionRequestData(it)}
    }

    private fun syncConnectionRequestData(context:Context):Boolean {
        return NetworkMonitor.runWithNetwork(context) {
            lifecycleScope.launch {
                showWaitScreen()
                ConnectionRequestRepo.syncData(context)
                sr_page_holder.isRefreshing = false
                hideWaitScreen()
            }
        }
    }

    private fun runUserSearch(searchString: String) {

        user_search_result_holder.hide()
        userSearchReasultAdapter.submitList(emptyList())

        getUserSearchMethod(searchString).let{
            if (it!=null){
                when (it){
                    UserSearchMethod.EMAIL -> {
                        runWithContext {
                            lifecycleScope.launch {
                                AuthRepo.findUserByEmail(searchString.toLowerCase()).let {
                                    debugLog(it)
                                    if (it.isNotEmpty()) {
                                        val users = mutableSetOf<User>()
                                        users.addAll(it.filter { !ConnectionRequestRepo.checkIfOnList(context!!,it) })
                                        userSearchReasultAdapter.submitList(users.toList())
                                        if (users.isNotEmpty()) {
                                            user_search_result_holder.show()
                                            user_search_result_holder.bringToFront()
                                        }
                                    }
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
                                if (!ConnectionRequestRepo.checkIfOnList(context!!,it)) {
                                    users.add(it)
                                }
                                userSearchReasultAdapter.submitList(users.toList())
                                if (users.isNotEmpty()) {
                                    user_search_result_holder.show()
                                    user_search_result_holder.bringToFront()
                                }
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
