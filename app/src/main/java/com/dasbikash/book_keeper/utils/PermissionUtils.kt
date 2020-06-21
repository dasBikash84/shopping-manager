package com.dasbikash.book_keeper.utils

import android.Manifest
import android.app.Activity
import androidx.annotation.StringRes
import com.dasbikash.android_extensions.openAppSettings
import com.dasbikash.pop_up_message.DialogUtils
import com.dasbikash.book_keeper.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener

object PermissionUtils {
    fun runWithReadStoragePermission(
                                activity: Activity,task:()->Unit,
                                @StringRes permissionRationalId:Int) {
        Dexter.withContext(activity)
            .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    task()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    DialogUtils.showAlertDialog(activity, DialogUtils.AlertDialogDetails(
                        message = activity.getString(permissionRationalId),
                        doOnPositivePress = {
                            token?.continuePermissionRequest()
                        },
                        doOnNegetivePress = {
                            token?.cancelPermissionRequest()
                        },
                        positiveButtonText = activity.getString(R.string.yes),
                        negetiveButtonText = activity.getString(R.string.no)
                    ))

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if (response?.isPermanentlyDenied == true){
                        DialogUtils.showAlertDialog(activity, DialogUtils.AlertDialogDetails(
                            message = activity.getString(R.string.open_settings_prompt_for_esp),
                            doOnPositivePress = {activity.openAppSettings()},
                            positiveButtonText = activity.getString(R.string.yes),
                            negetiveButtonText = activity.getString(R.string.no)
                        ))
                    }
                }
            }).check()
    }

    fun runWithCameraPermission(activity: Activity,
                                onPermissionGranted:()->Unit,
                                onPermissionDenied:(()->Unit)?=null) {
        activity.let {
            Dexter.withContext(it)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object : PermissionListener {

                    override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                        onPermissionGranted()
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permission: PermissionRequest?,
                        token: PermissionToken?
                    ) {
                        DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                            message = it.getString(R.string.camera_permission_rational),
                            doOnPositivePress = {
                                token?.continuePermissionRequest()
                            },
                            doOnNegetivePress = {
                                token?.cancelPermissionRequest()
                                onPermissionDenied?.invoke()
                            },
                            positiveButtonText = it.getString(R.string.show_permission_dialog),
                            negetiveButtonText = it.getString(R.string.exit_text)
                        ))

                    }

                    override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                        if (response?.isPermanentlyDenied == true){
                            DialogUtils.showAlertDialog(it, DialogUtils.AlertDialogDetails(
                                message = it.getString(R.string.open_settings_prompt_for_cam),
                                doOnPositivePress = {
                                    it.openAppSettings()
                                },
                                doOnNegetivePress = {
                                    onPermissionDenied?.invoke()
                                },
                                positiveButtonText = it.getString(R.string.yes),
                                negetiveButtonText = it.getString(R.string.no)
                            ))
                        }else{
                            onPermissionDenied?.invoke()
                        }
                    }
                }).check()
        }
    }

    fun runWithWriteStoragePermission(
        activity: Activity,task:()->Unit,
        @StringRes permissionRationalId:Int) {
        Dexter.withContext(activity)
            .withPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .withListener(object : PermissionListener {
                override fun onPermissionGranted(response: PermissionGrantedResponse?) {
                    task()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    DialogUtils.showAlertDialog(activity, DialogUtils.AlertDialogDetails(
                        message = activity.getString(permissionRationalId),
                        doOnPositivePress = {
                            token?.continuePermissionRequest()
                        },
                        doOnNegetivePress = {
                            token?.cancelPermissionRequest()
                        },
                        positiveButtonText = activity.getString(R.string.yes),
                        negetiveButtonText = activity.getString(R.string.no)
                    ))

                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    if (response?.isPermanentlyDenied == true){
                        DialogUtils.showAlertDialog(activity, DialogUtils.AlertDialogDetails(
                            message = activity.getString(R.string.open_settings_prompt_for_eswp),
                            doOnPositivePress = {activity.openAppSettings()},
                            positiveButtonText = activity.getString(R.string.yes),
                            negetiveButtonText = activity.getString(R.string.no)
                        ))
                    }
                }
            }).check()
    }
}