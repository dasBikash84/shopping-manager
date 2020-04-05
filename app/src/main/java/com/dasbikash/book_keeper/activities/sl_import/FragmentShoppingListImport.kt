package com.dasbikash.book_keeper.activities.sl_import

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.dasbikash.android_basic_utils.utils.DialogUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_extensions.*
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.sl_item.openAppSettings
import com.dasbikash.book_keeper.activities.sl_share.SlShareMethod
import com.dasbikash.book_keeper.activities.sl_share.SlToQr
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.ShoppingList
import com.dasbikash.snackbar_ext.showShortSnack
import com.google.zxing.BarcodeFormat
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import kotlinx.android.synthetic.main.fragment_shopping_list_import.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentShoppingListImport : FragmentTemplate(),WaitScreenOwner {

    private lateinit var codeScanner: CodeScanner
    private var exitMessage:String?=null
    private lateinit var offlineShoppingList:ShoppingList

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list_import, container, false)
    }

    override fun getPageTitle(context: Context): String? {
        return context.getString(R.string.shopping_list_import_title)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        codeScanner = CodeScanner(activity!!, scanner_view)
        codeScanner.formats = listOf(BarcodeFormat.QR_CODE)

        codeScanner.decodeCallback = DecodeCallback {
            runOnMainThread( {
                debugLog(it.text)
                SlToQr.decodeQrScanResult(it.text).let {
                    if (it?.slShareMethod !=null && it.data!=null){
                        when(it.slShareMethod){
                            SlShareMethod.ON_LINE -> {
                                TODO()
                            }
                            SlShareMethod.OFF_LINE ->{
                                processOffLineSlQrScanResult(it)
                            }
                        }
                    }else{
                        showResultErrorScreen()
                    }
                }
            })
        }

        codeScanner.errorCallback= ErrorCallback {
            showShortSnack("QR scanner error")
            it.printStackTrace()
        }

        btn_qr_format_error_rescan.setOnClickListener {
            qr_format_error_block.hide()
            showScannerPreview()
        }

        btn_save_offline_sl.setOnClickListener {
            runWithContext {
                lifecycleScope.launch {
                    showWaitScreen()
                    ShoppingListRepo.saveOfflineShoppingList(it,offlineShoppingList)
                    exit()
                }
            }
        }

        btn_qr_format_error_rescan_exit.setOnClickListener { activity?.onBackPressed() }
        btn_save_offline_sl_cancel.setOnClickListener { activity?.onBackPressed() }
        showScannerPreview()
    }

    private fun processOffLineSlQrScanResult(slToQr: SlToQr) {
        off_line_share_block.show()
        on_line_share_block.hide()
        qr_format_error_block.hide()
        exitMessage = getString(R.string.discard_and_exit_prompt)
        SlToQr.decodeOfflineShoppingList(slToQr).let {
            if (it==null){
                showResultErrorScreen()
            }else{
                offlineShoppingList = it
            }
        }
    }

    private fun showResultErrorScreen() {
        off_line_share_block.hide()
        on_line_share_block.hide()
        qr_format_error_block.show()
        exitMessage = null
    }

    private fun showScannerPreview() {
        runOnMainThread({
            showWaitScreen()
            runWithCameraPermission {
                hideWaitScreen()
                codeScanner.startPreview()
            }
        })
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }

    override fun getExitPrompt(): String? {
        return exitMessage
    }

    override fun registerWaitScreen(): ViewGroup = wait_screen

    private fun runWithCameraPermission(task:()->Unit) {
        runWithActivity {
            Dexter.withActivity(it)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {

                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        task()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        runWithContext {
                            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                                message = it.getString(R.string.camera_permission_rational),
                                doOnPositivePress = {
                                    token?.continuePermissionRequest()
                                },
                                doOnNegetivePress = {
                                    token?.cancelPermissionRequest()
                                    exit()
                                },
                                positiveButtonText = it.getString(R.string.show_permission_dialog),
                                negetiveButtonText = it.getString(R.string.exit_text)
                            ))
                        }

                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        if (response?.isPermanentlyDenied == true){
                            runWithContext {
                                DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                                    message = it.getString(R.string.open_settings_prompt_for_cam),
                                    doOnPositivePress = {
                                        runWithActivity { it.openAppSettings()}
                                    },
                                    doOnNegetivePress = {
                                        exit()
                                    },
                                    positiveButtonText = it.getString(R.string.yes),
                                    negetiveButtonText = it.getString(R.string.no)
                                ))
                            }
                        }else{
                            exit()
                        }
                    }
                }).check()
        }
    }
    private fun exit(){
        debugLog("Exit")
        lifecycleScope.launch {
            delay(500)
            exitMessage = null
            activity?.finish()
        }
    }
}
