package com.dasbikash.book_keeper.activities.sl_item

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_basic_utils.utils.uriToFile
import com.dasbikash.android_camera_utils.CameraUtils
import com.dasbikash.android_camera_utils.CameraUtils.Companion.launchCameraForImage
import com.dasbikash.android_extensions.*
import com.dasbikash.android_image_utils.ImageUtils
import com.dasbikash.android_network_monitor.NetworkMonitor
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.rv_helpers.StringListAdapter
import com.dasbikash.book_keeper.utils.rotateIfRequired
import com.dasbikash.book_keeper_repo.ImageRepo
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingListItem
import com.dasbikash.menu_view.MenuView
import com.dasbikash.menu_view.MenuViewItem
import com.dasbikash.snackbar_ext.showShortSnack
import com.jaredrummler.materialspinner.MaterialSpinner
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.fragment_shopping_list_item_add_edit.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FragmentShoppingListItemAddEdit private constructor() : FragmentTemplate(),
    WaitScreenOwner {
    private lateinit var viewModel:ViewModelShoppingListItem
    override fun registerWaitScreen(): ViewGroup = wait_screen

    private var exitPrompt: String? = null
    private lateinit var shoppingListItem: ShoppingListItem

    private val expenseCategories = mutableListOf<String>()
    private val uoms = mutableListOf<String>()

    private val imageListAdapter = StringListAdapter({view,text->
        runWithContext {
            lifecycleScope.launch {
                ImageRepo.downloadImageFile(it, text)?.let {
                    ImageUtils.getBitmapFromFile(it)?.let {
                        (view as ImageView).setImageBitmap(it.rotateIfRequired())
                    }
                }
            }
        }
    },{doOnProductImageClick(it)},R.layout.view_single_preview_image)

    private val brandSuggestionsAdapter = StringListAdapter({view, text ->
        (view as TextView).text = text
    },{deleteBrandSugAction(it)})

    private fun doOnProductImageClick(url: String) {
        debugLog(url)
        val fragmentManager: FragmentManager = activity!!.getSupportFragmentManager()
        vp_product_image.adapter = object : FragmentStatePagerAdapter(fragmentManager,BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return FragmentProductImage.getEditInstance(shoppingListItem.images!!.get(position))
            }
            override fun getCount(): Int = shoppingListItem.images?.size ?: 0
        }
        vp_product_image.setCurrentItem(shoppingListItem.images!!.indexOf(url),true)
        vp_product_image.bringToFront()
        vp_product_image.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list_item_add_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_sli_images.adapter = imageListAdapter
        rv_sli_brand_sug.adapter = brandSuggestionsAdapter
        viewModel = ViewModelProviders.of(activity!!).get(ViewModelShoppingListItem::class.java)
        exitPrompt = getString(R.string.discard_and_exit_prompt)
        et_sli_name.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.name = text?.toString()?.trim() ?: ""
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_sli_details.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.details = text?.toString()?.trim()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_sli_min_price.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.minUnitPrice = text?.toString()?.let { if (it.isNotBlank()) {it.toDouble()} else {null}}
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_sli_max_price.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.maxUnitPrice = text?.toString()?.let { if (it.isNotBlank()) {it.toDouble()} else {null}}
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        et_sli_quantity.addTextChangedListener(object:TextWatcher{
            override fun afterTextChanged(text: Editable?) {
                shoppingListItem.qty = text?.toString()?.let { if (it.isNotBlank()) {it.toDouble()} else {0.0}} ?: 0.0
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        sli_category_selector.setOnItemSelectedListener(MaterialSpinner.OnItemSelectedListener<String> { view, position, id, item ->
            hideKeyboard()
            shoppingListItem.categoryId = position
        })
        uom_selector.setOnItemSelectedListener(MaterialSpinner.OnItemSelectedListener<String> { view, position, id, item ->
            hideKeyboard()
            shoppingListItem.uom = position
        })
        btn_cancel.setOnClickListener {
            hideKeyboard()
            activity?.onBackPressed()
        }
        btn_save_sl_item.setOnClickListener {
            hideKeyboard()
            saveShoppingListItem()
        }
        btn_add_product_image.setOnClickListener {
            runWithContext {
                NetworkMonitor.runWithNetwork(it){importImageTask(it)}
            }
        }
        btn_add_brand_sug.setOnClickListener {
            addBrandSugAction()
        }
        viewModel.getShoppingListItem().observe(this,object : Observer<ShoppingListItem>{
            override fun onChanged(item: ShoppingListItem?) {
                item?.let {
                    shoppingListItem = it
                    refreshView()
                }
            }
        })

        initShoppingListItem()
    }

    private fun importImageTask(context: Context) {
        val menuView = MenuView()
        menuView.add(getCaptureImageTask())
        menuView.add(getImportFromGalleryTask())
        menuView.add(getImportFromLinkTask())
        menuView.show(context)
    }

    private fun addBrandSugAction() {
        runWithContext {
            val view = EditText(it)
            view.hint = it.getString(R.string.brand_name_sug_hint)
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.add_brand_sug_btn_text),
                view = view,
                doOnPositivePress = {
                    val name = view.text.toString().trim()
                    if (name.isBlank()){
                        showShortSnack(it.getString(R.string.blank_brand_name_sug_error))
                    }else if(shoppingListItem.brandNameSuggestions?.contains(name)==true){
                        showShortSnack(it.getString(R.string.duplicate_brand_name_sug_error))
                    }else{
                        viewModel. addBrandSuggestion(name)
                    }
                }
            ))
        }
    }

    private fun deleteBrandSugAction(name: String) {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.remove_brand_sug_prompt,name),
                doOnPositivePress = {
                    viewModel. removeBrandNameSuggestion(name)
                }
            ))
        }
    }

    private fun saveShoppingListItem() {
        if (validateData()){
            runWithContext {
                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                    message = it.getString(R.string.save_shopping_list_item_prompt),
                    doOnPositivePress = {
                        lifecycleScope.launch {
                            showWaitScreen()
                            exitPrompt = null
                            ShoppingListRepo.save(it,shoppingListItem)
                            activity?.onBackPressed()
                            hideWaitScreen()
                        }
                    }
                ))
            }
        }
    }

    private fun validateData(): Boolean {
        if (shoppingListItem.name.isNullOrBlank()){
            et_sli_name.error = getString(R.string.et_sli_name_empty_error)
            return false
        }
        if (shoppingListItem.qty == 0.0){
            et_sli_quantity.error = getString(R.string.quantity_error_message)
            return false
        }
        return true
    }

    override fun getPageTitle(context: Context): String? {
        if (arguments?.containsKey(ARG_SHOPPING_LIST_ITEM_ID) == true) {
            return null
        } else {
            return context.getString(R.string.shopping_list_item_create_title)
        }
    }

    override fun getExitPrompt(): String? {
        return exitPrompt
    }

    private fun initShoppingListItem() {
        runWithContext {
            lifecycleScope.launch {
                showWaitScreen()
                if (expenseCategories.isEmpty()) {
                    expenseCategories.addAll(resources.getStringArray(R.array.expense_categories))
                    sli_category_selector.setItems(expenseCategories)
                }
                if (uoms.isEmpty()) {
                    uoms.addAll(resources.getStringArray(R.array.uoms))
                    uom_selector.setItems(uoms)
                }
                if (!::shoppingListItem.isInitialized) {
                    getShoppingListItemId().let {
                        shoppingListItem = if (it == null) {
                            ShoppingListItem(shoppingListId = getShoppingListId())
                        } else {
                            val item:ShoppingListItem
                            activity!!.apply {
                                item = ShoppingListRepo.findShoppingListItemById(this, it)!!
                                (this as ActivityShoppingListItem).setTitle(getString(R.string.edit_title,item.name))
                            }
                            item
                        }
                    }
                }
                viewModel.setShoppingListItem(shoppingListItem)
                hideWaitScreen()
            }
        }
    }

    private fun refreshView() {
        if (::shoppingListItem.isInitialized) {
            debugLog(shoppingListItem)
            vp_product_image.hide()
            shoppingListItem.name?.let { et_sli_name.setText(it) }
            sli_category_selector.selectedIndex = getCurrentCategoryIndex()
            shoppingListItem.details?.let { et_sli_details.setText(it) }
            shoppingListItem.minUnitPrice?.let { et_sli_min_price.setText(it.toString()) }
            shoppingListItem.maxUnitPrice?.let { et_sli_max_price.setText(it.toString()) }
            shoppingListItem.qty.let { et_sli_quantity.setText(it.toString()) }
            uom_selector.selectedIndex = getCurrentUomIndex()
            (shoppingListItem.images ?: emptyList()).let {
                if (it.size>=ShoppingListItem.MAX_PRODUCT_IMAGE_COUNT){
                    btn_add_product_image.hide()
                }else{
                    btn_add_product_image.show()
                }
                imageListAdapter.submitList(it)
            }
            (shoppingListItem.brandNameSuggestions ?: emptyList()).let {
                if (!it.isEmpty()){
                    brandSuggestionsAdapter.submitList(it)
                    sli_brand_sug_holder.show()
                }else{
                    sli_brand_sug_holder.hide()
                }
            }
        }
    }

    private fun getCurrentUomIndex(): Int =  shoppingListItem.uom ?: 0

    private fun getCurrentCategoryIndex(): Int {
        if (shoppingListItem.categoryId == null) {
            shoppingListItem.categoryId = 0
        }
        return shoppingListItem.categoryId!!
    }

    private fun getShoppingListItemId(): String? = arguments?.getString(ARG_SHOPPING_LIST_ITEM_ID)
    private fun getShoppingListId(): String = arguments?.getString(ARG_SHOPPING_LIST_ID)!!

    private fun runWithReadStoragePermission(task:()->Unit) {
        var onPermissionRationaleShouldBeShownCalled = false
        runWithActivity {
            Dexter.withActivity(it)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener{

                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        task()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        onPermissionRationaleShouldBeShownCalled = true
                        runWithContext {
                            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                                message = it.getString(R.string.external_storage_permission_rational),
                                doOnPositivePress = {
                                    token?.continuePermissionRequest()
                                },
                                doOnNegetivePress = {
                                    token?.cancelPermissionRequest()
                                },
                                positiveButtonText = it.getString(R.string.yes),
                                negetiveButtonText = it.getString(R.string.no)
                            ))
                        }

                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        if (!onPermissionRationaleShouldBeShownCalled){
                            runWithContext {
                                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                                    message = it.getString(R.string.open_settings_prompt_for_esp),
                                    doOnPositivePress = {
                                        openAppSettings()
                                    },
                                    positiveButtonText = it.getString(R.string.yes),
                                    negetiveButtonText = it.getString(R.string.no)
                                ))
                            }
                        }
                    }
                }).check()
        }
    }

    private fun openAppSettings() {
        activity?.let {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", it.packageName, null)
            intent.data = uri
            startActivity(intent)
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
                            { addToProductImages(it) })
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
            showWaitScreen()
            lifecycleScope.launch {
                it.uriToFile(imageUri).let {
                    if (it!=null) {
                        addToProductImages(it)
                    }else{
                        showShortSnack(R.string.unknown_error_message)
                        hideWaitScreen()
                    }
                }
            }
        }
    }

    private fun addToProductImages(file: File) {
        showWaitScreen()
        lifecycleScope.launch(Dispatchers.IO) {
            ImageRepo.uploadProductImage(context!!,file).let {
                runOnMainThread({
                    viewModel.addProductImage(it)
                    hideWaitScreen()
                })
            }
        }
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
                                showWaitScreen()
                                ImageUtils.fetchImageFromUrl(
                                    view.text.toString().trim(),this,it,{
                                        addToProductImages(it)
                                    },{
                                        hideWaitScreen()
                                        showShortSnack(R.string.unknown_error_message)
                                    }
                                )
                            }else{
                                showShortSnack(R.string.blank_url_message)
                            }
                        }
                    ))
                }
            }
        )
    }

    private fun getCaptureImageTask(): MenuViewItem {
        return MenuViewItem(
            text = getString(R.string.capture_iamge_with_camera_prompt),
            task = {
                launchCameraForImage(this, REQUEST_TAKE_PHOTO)
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

    private fun launchImportFromGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, REQUEST_CODE_PICK_IMAGE)
    }

    companion object {
        private const val REQUEST_TAKE_PHOTO = 4654
        private const val REQUEST_CODE_PICK_IMAGE = 3454

        private const val ARG_SHOPPING_LIST_ID =
            "com.dasbikash.book_keeper.activities.sl_item.FragmentShoppingListItemAddEdit.ARG_SHOPPING_LIST_ID"

        private const val ARG_SHOPPING_LIST_ITEM_ID =
            "com.dasbikash.book_keeper.activities.sl_item.FragmentShoppingListItemAddEdit.ARG_SHOPPING_LIST_ITEM_ID"

        fun getInstanceForEdit(shoppingListItemId: String): FragmentShoppingListItemAddEdit {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ITEM_ID, shoppingListItemId)
            val fragment = FragmentShoppingListItemAddEdit()
            fragment.arguments = arg
            return fragment
        }

        fun getInstance(shoppingListId: String): FragmentShoppingListItemAddEdit {
            val arg = Bundle()
            arg.putString(ARG_SHOPPING_LIST_ID, shoppingListId)
            val fragment = FragmentShoppingListItemAddEdit()
            fragment.arguments = arg
            return fragment
        }
    }

}