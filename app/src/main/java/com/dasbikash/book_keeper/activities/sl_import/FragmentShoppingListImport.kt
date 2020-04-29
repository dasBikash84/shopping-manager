package com.dasbikash.book_keeper.activities.sl_import

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
import com.dasbikash.android_extensions.BuildConfig
import com.dasbikash.android_extensions.runOnMainThread
import com.dasbikash.android_extensions.runWithActivity
import com.dasbikash.android_extensions.runWithContext
import com.dasbikash.android_view_utils.utils.WaitScreenOwner
import com.dasbikash.book_keeper.R
import com.dasbikash.book_keeper.activities.templates.FragmentTemplate
import com.dasbikash.book_keeper.models.SlShareMethod
import com.dasbikash.book_keeper.models.SlToQr
import com.dasbikash.book_keeper.utils.PermissionUtils
import com.dasbikash.book_keeper_repo.ShoppingListRepo
import com.dasbikash.book_keeper_repo.model.OnlineDocShareParams
import com.dasbikash.snackbar_ext.showIndefiniteSnack
import com.dasbikash.snackbar_ext.showShortSnack
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.fragment_shopping_list_import.*
import kotlinx.android.synthetic.main.view_wait_screen.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FragmentShoppingListImport() : FragmentTemplate(),WaitScreenOwner {

    private lateinit var codeScanner: CodeScanner

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
                                processOnLineSlQrScanResult(it)
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
            if(BuildConfig.DEBUG) {
                showShortSnack("QR scanner error")
            }
            it.printStackTrace()
            showResultErrorScreen()
        }

        scanner_view.isAutoFocusButtonVisible=false
        scanner_view.isFlashButtonVisible=false

        showScannerPreview()
    }

    private fun postOnlineSlShareRequest(onlineDocShareParams: OnlineDocShareParams) {
        runWithContext {
            showWaitScreen()
            lifecycleScope.launch {
                ShoppingListRepo.isShareRequestValid(it,onlineDocShareParams.documentPath!!).let {
                    if (it){
                        ShoppingListRepo.postOnlineSlShareRequest(context!!,onlineDocShareParams)
                        showShortSnack(getString(R.string.shopping_list_share_request_posted))
                        delay(2000L)
                        exit()
                    }else{
                        showIndefiniteSnack(getString(R.string.duplicate_shopping_list_or_share_req_message))
                        hideWaitScreen()
                        showScannerPreview()
                    }
                }
            }
        }
    }

    private fun processOnLineSlQrScanResult(slToQr: SlToQr) {
        SlToQr.decodeOnlineRequestPayload(slToQr).let {
            if (it!=null){
                val onlineDocShareParams = it
                runWithContext {
                    DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                        message = it.getString(R.string.online_shopping_list_import_prompt),
                        positiveButtonText = it.getString(R.string.online_shopping_list_import_req_text),
                        negetiveButtonText = it.getString(R.string.cancel),
                        doOnPositivePress = {postOnlineSlShareRequest(onlineDocShareParams)},
                        doOnNegetivePress = {showScannerPreview()},
                        isCancelable = false
                    ))
                }
            }else{
                showResultErrorScreen()
            }
        }
    }

    private fun processOffLineSlQrScanResult(slToQr: SlToQr) {
        SlToQr.decodeOfflineShoppingList(slToQr).let {
            if (it==null){
                showResultErrorScreen()
            }else{
                val offlineShoppingList = it
                offlineShoppingList.partnerIds=null
                runWithContext {
                    DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                        message = it.getString(R.string.offline_shopping_list_save_prompt),
                        positiveButtonText = it.getString(R.string.save_text),
                        negetiveButtonText = it.getString(R.string.cancel),
                        doOnPositivePress = {
                            lifecycleScope.launch {
                                showWaitScreen()
                                ShoppingListRepo.saveOfflineShoppingList(it,offlineShoppingList)
                                exit()
                            }
                        },
                        doOnNegetivePress = {showScannerPreview()},
                        isCancelable = false
                    ))
                }
            }
        }
    }

    private fun showResultErrorScreen() {
        runWithContext {
            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                message = it.getString(R.string.qr_format_error_prompt),
                positiveButtonText = it.getString(R.string.rescan_text),
                negetiveButtonText = it.getString(R.string.exit_text),
                doOnPositivePress = {showScannerPreview()},
                doOnNegetivePress = {exit()},
                isCancelable = false
            ))
        }
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

    override fun registerWaitScreen(): ViewGroup = wait_screen

    private fun runWithCameraPermission(task:()->Unit) {
        runWithActivity {
            PermissionUtils.runWithCameraPermission(
                it,
                onPermissionGranted = {task()},
                onPermissionDenied = {exit()})

        }
    }
    private fun exit(){
        debugLog("Exit")
        lifecycleScope.launch {
            delay(50)
            activity?.finish()
        }
    }
}
