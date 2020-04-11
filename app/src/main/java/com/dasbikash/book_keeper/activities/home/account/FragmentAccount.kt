package com.dasbikash.book_keeper.activities.home.account

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_basic_utils.utils.uriToFile
import com.dasbikash.android_camera_utils.CameraUtils
import com.dasbikash.android_extensions.*
import com.dasbikash.android_image_utils.ImageUtils
import com.dasbikash.android_image_utils.displayImageFile
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.login.ActivityLogin
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.utils.PermissionUtils
import com.dasbikash.book_keeper.utils.ValidationUtils
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.ImageRepo
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showShortSnack
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

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

        iv_user_image.setOnClickListener {
            runWithContext {importImageTask(it)}
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

    private fun importImageTask(context: Context) {
        val menuView = MenuView()
        menuView.add(getCaptureImageTask())
        menuView.add(getImportFromGalleryTask())
        menuView.add(getImportFromLinkTask())
        menuView.show(context)
    }

    private fun getCaptureImageTask(): MenuViewItem {
        return MenuViewItem(
            text = getString(R.string.capture_iamge_with_camera_prompt),
            task = {
                    runWithActivity { PermissionUtils.runWithCameraPermission(it,onPermissionGranted = {
                        CameraUtils.launchCameraForImage(this, REQUEST_TAKE_PHOTO)
                    })
                }
            }
        )
    }

    private fun getImportFromGalleryTask(): MenuViewItem {
        return MenuViewItem(
            text = getString(R.string.import_image_from_internal_storage_prompt),
            task = {
                runWithReadStoragePermission { launchImportFromGallery() }
            }
        )
    }

    private fun runWithReadStoragePermission(task:()->Unit) {
        runWithActivity {
            PermissionUtils.runWithReadStoragePermission(
                it,task,R.string.external_storage_permission_rational
            )
        }
    }

    private fun launchImportFromGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun getImportFromLinkTask(): MenuViewItem {
        return MenuViewItem(
            text = getString(R.string.import_from_link_prompt),
            task = {
                runWithContext {
                    val view = EditText(it)
                    view.hint = it.getString(R.string.image_url_prompt)
                    DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                        message = it.getString(R.string.image_url_dialog_message),
                        view = view,
                        doOnPositivePress = {
                            if (view.text.toString().isNotBlank()){
                                NetworkMonitor.runWithNetwork(it) {
                                    showWaitScreen()
                                    ImageUtils.fetchImageFromUrl(
                                        view.text.toString().trim(), this, it, {
                                            setUserImages(it)
                                            hideWaitScreen()
                                        }, {
                                            hideWaitScreen()
                                            showShortSnack(R.string.unknown_error_message)
                                        }
                                    )
                                }
                            }else{
                                showShortSnack(R.string.blank_url_message)
                            }
                        }
                    ))
                }
            }
        )
    }

    private fun setUserImages(file: File) {
        runWithContext {
            lifecycleScope.launch(Dispatchers.IO) {
                ImageRepo.uploadProfilePicture(it,file).let {
                    profilePictureEditTask(it)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_TAKE_PHOTO -> {
                    runWithContext {
                        CameraUtils.handleCapturedImageFile(
                            it,
                            { setUserImages(it) })
                    }
                }
                REQUEST_CODE_PICK_IMAGE ->{
                    debugLog("REQUEST_CODE_PICK_IMAGE")
                    data?.data?.let {
                        debugLog("Found image uri")
                        processGalleryImageUri(it)
                    }
                }
            }
        }
    }

    private fun processGalleryImageUri(imageUri: Uri) {
        runWithActivity {
            lifecycleScope.launch {
                it.uriToFile(imageUri).let {
                    if (it!=null) {
                        setUserImages(it)
                    }else{
                        showShortSnack(R.string.unknown_error_message)
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
                        if (isAdded && iv_user_image!=null){
                            iv_user_image.displayImageFile(it)
                        }
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

    private suspend fun profilePictureEditTask(imageUrl:String){
        context?.let {
            AuthRepo.profilePictureEditTask(it, imageUrl)
        }
    }

    companion object{
        private const val REQUEST_TAKE_PHOTO = 1321
        private const val REQUEST_CODE_PICK_IMAGE = 1867
    }
}
