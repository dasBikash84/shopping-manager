package com.dasbikash.book_keeper.activities.home.account.connections

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.*
import com.dasbikash.android_image_utils.displayImageFile
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.rv_helpers.ConnectionUserAdapter
import com.dasbikash.book_keeper.rv_helpers.SearchedUserAdapter
import com.dasbikash.book_keeper.utils.OptionsIntentBuilderUtility
import com.dasbikash.book_keeper_repo.*
import com.dasbikash.book_keeper_repo.model.ConnectionRequest
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.book_keeper_repo.utils.ValidationUtils
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showIndefiniteSnack
import com.dasbikash.snackbar_ext.showLongSnack
import kotlinx.android.synthetic.main.fragment_connections.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentConnections : Fragment(),WaitScreenOwner {

    override fun registerWaitScreen(): ViewGroup = wait_screen

    private val userSearchReasultAdapter = SearchedUserAdapter({addUserAction(it)},{displayProfilePicFull(it)})
    private val connectedUserAdapter = ConnectionUserAdapter({context, user ->  getMenuViewForConnectedUsers(context, user)},{displayProfilePicFull(it)})
    private val connectionPendingFromMeUserAdapter = ConnectionUserAdapter({ context, user ->  getMenuViewForConnectionPendingFromMeUsers(context, user)},{displayProfilePicFull(it)})
    private val connectionPendingToMeUserAdapter = ConnectionUserAdapter({ context, user ->  getMenuViewForConnectionPendingToMeUsers(context, user)},{displayProfilePicFull(it)})
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
            if (!email.isNullOrBlank()){
                menuView.add(getEmailUserMenuItem(context, email!!))
            }
            if (!phone.isNullOrBlank()){
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

    private fun getMenuViewForConnectionPendingToMeUsers(context: Context, user: User):MenuView{
        debugLog("getMenuViewForConnectionPendingUsers: $user")
        val menuView = MenuView()

        user.apply {
            menuView.add(
                MenuViewItem(
                    text = context.getString(R.string.approve),
                    task = {
                        DialogUtils.showAlertDialog(context, DialogUtils.AlertDialogDetails(
                            message = context.getString(R.string.approve)+"?",
                            doOnPositivePress = {
                                NetworkMonitor.runWithNetwork(context) {
                                    lifecycleScope.launch {
                                        showWaitScreen()
                                        ConnectionRequestRepo.approveRequest(context,user)
                                        hideWaitScreen()
                                    }
                                }
                            }
                        ))
                    }
                )
            )
            menuView.add(
                MenuViewItem(
                    text = context.getString(R.string.decline),
                    task = {
                        DialogUtils.showAlertDialog(context, DialogUtils.AlertDialogDetails(
                            message = context.getString(R.string.decline)+"?",
                            doOnPositivePress = {
                                NetworkMonitor.runWithNetwork(context) {
                                    lifecycleScope.launch {
                                        showWaitScreen()
                                        ConnectionRequestRepo.declineRequest(context,user)
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

    private fun getMenuViewForConnectionPendingFromMeUsers(context: Context, user: User):MenuView{
        debugLog("getMenuViewForConnectionPendingUsers: $user")
        val menuView = MenuView()

        user.apply {
            if (!email.isNullOrBlank()){
                menuView.add(getEmailUserMenuItem(context, email!!))
            }
            if (!phone.isNullOrBlank()){
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
        rv_request_pending_from_me_user_list.adapter = connectionPendingFromMeUserAdapter
        rv_request_pending_to_me_user_list.adapter = connectionPendingToMeUserAdapter
        viewModel = ViewModelProviders.of(this).get(ViewModelConnections::class.java)
        btn_search_user.setOnClickListener {
            hideKeyboard()
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
            syncConnectionRequestData()
        }

        viewModel.getReceivedPendingLiveData().observe(this,object : Observer<List<ConnectionRequest>> {
            override fun onChanged(list: List<ConnectionRequest>?) {
                (list ?: emptyList()).let {
                    debugLog(it)
                    if (!it.isEmpty()){
                        val connectionRequests = it
                        runWithContext {
                            lifecycleScope.launch {
                                connectionRequests.map {
                                    AuthRepo.findUserById(context!!,it.requesterUserId!!)
                                }.let {
                                    it.filter { it!=null }.map { it!! }.let {
                                        if (it.isNotEmpty()) {
                                            connectionPendingToMeUserAdapter.submitList(it)
                                            request_pending_to_me_user_list_holder.show()
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        request_pending_to_me_user_list_holder.hide()
                    }
                }
            }
        })

        viewModel.getRequestedPendingLiveData().observe(this,object : Observer<List<ConnectionRequest>>{
            override fun onChanged(list: List<ConnectionRequest>?) {
                (list ?: emptyList()).let {
                    debugLog(it)
                    if (!it.isEmpty()){
                        val connectionRequests = it
                        runWithContext {
                            lifecycleScope.launch {
                                connectionRequests.map {
                                    AuthRepo.findUserById(context!!,it.partnerUserId!!)
                                }.let {
                                    it.filter { it!=null }.map { it!! }.let {
                                        if (it.isNotEmpty()) {
                                            connectionPendingFromMeUserAdapter.submitList(it)
                                            request_pending_from_me_user_list_holder.show()
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        request_pending_from_me_user_list_holder.hide()
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
                                    AuthRepo.findUserById(context!!,if (it.requesterUserId==AuthRepo.getUserId()) {it.partnerUserId!!} else {it.requesterUserId!!})
                                }.let {
                                    it.filter { it!=null }.map { it!! }.let {
                                        if (it.isNotEmpty()) {
                                            connectedUserAdapter.submitList(it)
                                            connected_user_list_holder.show()
                                        }
                                    }
                                }
                            }
                        }
                    }else{
                        connected_user_list_holder.hide()
                    }
                }
            }
        })

        btn_close_profile_pic_full.setOnClickListener { profile_pic_full_holder.hide() }
        profile_pic_full_holder.setOnClickListener {  }
    }

    private fun displayProfilePicFull(user: User){
        runWithContext {
            NetworkMonitor.runWithNetwork(it) {
                user.photoUrl?.apply {
                    showWaitScreen()
                    ImageRepo
                        .downloadImageFile(it, this, doOnDownload = {
                            iv_profile_pic_full?.displayImageFile(it)
                            profile_pic_full_holder?.bringToFront()
                            profile_pic_full_holder?.show()
                            wait_screen?.hide()
                        }, doOnError = { wait_screen?.hide() })
                }
            }
        }
    }

    private fun syncConnectionRequestData() {
        runWithContext {
            NetworkMonitor.runWithNetwork(it) {
                lifecycleScope.launch(Dispatchers.IO) {
                    DataSyncService.syncConnectionRequestData(it)
                    runOnMainThread({sr_page_holder?.isRefreshing = false})
                }
            }.let {
                if (!it){
                    sr_page_holder?.isRefreshing = false
                }
            }
        }
    }

    private fun runUserSearch(searchString: String) {

        user_search_result_holder.hide()
        userSearchReasultAdapter.submitList(emptyList())

        runWithContext {
            NetworkMonitor.runWithNetwork(it) {
                lifecycleScope.launch {
                    val userSearchString = sanitizeUserSearchString(it,searchString)
                    if (!(ValidationUtils.validateEmailAddress(userSearchString) ||
                            ValidationUtils.validateMobileNumber(userSearchString))){
                        showLongSnack("${it.getString(R.string.search_user_hint)}!!")
                        return@launch
                    }
                    showWaitScreen()
                    AuthRepo.searchUser(userSearchString).let {
                        debugLog(it)
                        val users = mutableSetOf<User>()
                        users.addAll(it.filter { !ConnectionRequestRepo.checkIfOnList(context!!,it) })
                        if (users.isNotEmpty()) {
                            userSearchReasultAdapter.submitList(users.toList())
                            user_search_result_holder.show()
                            user_search_result_holder.bringToFront()
                        }else{
                            showLongSnack(R.string.no_new_user_found)
                        }
                        hideWaitScreen()
                    }
                }
            }
        }
    }

    private suspend fun sanitizeUserSearchString(context: Context,searchString: String): String {
        if (allNumberPattern.matches(searchString) &&
                !ValidationUtils.validateMobileNumber(searchString)) {
            CountryRepo.getCurrentCountry(context)?.apply {
                if (!searchString.startsWith(callingCode!!) &&
                        searchString.length>=phoneNumberLength!!){
                    return "${callingCode}${searchString.substring((searchString.length-phoneNumberLength!!),searchString.length)}"
                }
            }
        }
        return searchString
    }

    companion object{
        private val allNumberPattern = Regex("\\d+")
    }
}
