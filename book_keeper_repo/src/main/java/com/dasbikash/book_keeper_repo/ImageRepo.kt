package com.dasbikash.book_keeper_repo

import android.content.Context
import android.graphics.Bitmap
import com.dasbikash.android_basic_utils.utils.FileUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_image_utils.ImageUtils
import com.dasbikash.book_keeper_repo.bg_tasks.ImageUploaderTaskUtils
import com.dasbikash.book_keeper_repo.exceptions.ImageDeletionException
import com.dasbikash.book_keeper_repo.firebase.FirebaseStorageService
import com.dasbikash.book_keeper_repo.model.RemoteImageInfo
import com.dasbikash.book_keeper_repo.utils.scaled
import java.io.File

object ImageRepo:BookKeeperRepo() {

    fun downloadImageFile(context: Context,imageUrl:String,
                          doOnDownload:(File)->Unit,doOnError: (() -> Unit)?=null) {
        imageUrl.split("/").last().let {
            val fileName = it
            if (it.isNotEmpty()){
                FileUtils.readFileFromInternalStorage(context,fileName)?.let {
                    debugLog("Found on local storage")
                    doOnDownload(it)
                }
            }
            FirebaseStorageService
                .downloadImageFile(
                    imageUrl,fileName, {
                        FileUtils.saveFileOnInternalStorage(it,context,fileName)
                        doOnDownload(it)
                        it.delete()
                    }, doOnError)
        }
    }

    suspend fun uploadProductImage(context: Context,image: File):String {
        ImageUtils.getBitmapFromFileSuspended(image)!!.let {
            return uploadImage(context,it.scaled())
        }
    }

    suspend fun uploadProfilePicture(context: Context,image: File):Pair<String,String> {
        ImageUtils.getBitmapFromFileSuspended(image)!!.let {
            val mainUrl = uploadImage(context,it.scaled(384.00f),false)
            val thumbUrl = uploadImage(context,it.scaled(96.00f),false)
            return Pair(mainUrl,thumbUrl)
        }
    }

    private suspend fun uploadImage(context: Context, imageBitmap: Bitmap,
                                    isProductImage:Boolean = true):String {
        debugLog("uploadProductImage")
        val remoteImageInfo = RemoteImageInfo(isProductImage = isProductImage)
        val imageFile = ImageUtils.getPngFromBitmap(imageBitmap,remoteImageInfo.localName,context)
        FileUtils.saveFileOnInternalStorage(imageFile,context,remoteImageInfo.localName)
        return scheduleImageUpload(imageBitmap, remoteImageInfo, context)
    }

    internal suspend fun scheduleImageUpload(
        imageBitmap: Bitmap?=null,
        remoteImageInfo: RemoteImageInfo,
        context: Context
    ):String {
        val bitmapForUpload = imageBitmap ?: ImageUtils.getBitmapFromFile(FileUtils.readFileFromInternalStorage(context,remoteImageInfo.localName)!!)!!
        val remotePath =
            FirebaseStorageService
                .uploadImage(
                    bitmapForUpload,
                    remoteImageInfo.localName,
                    remoteImageInfo.isProductImage,
                    { markUploaded(context.applicationContext, remoteImageInfo, it) },
                    { markUploadError(context.applicationContext, remoteImageInfo) }
                )
        remoteImageInfo.prepForUpload()
        getRemoteImageInfoDao(context).add(remoteImageInfo)
        ImageUploaderTaskUtils.initTask(context)
        return remotePath
    }

    internal suspend fun getPendingImageUploadCount(context: Context):Int =
        getRemoteImageInfoDao(context).getPendingUploadCount()

    internal suspend fun getAllPendingUploadInfo(context: Context,minModified:Long):List<RemoteImageInfo> =
        getRemoteImageInfoDao(context).getAllPendingUploadInfo(minModified)

    suspend fun deleteFileByUrl(url:String):Boolean{
        try {
            FirebaseStorageService.deleteFileByUrl(url)
            return true
        }catch (ex: ImageDeletionException){
            ex.printStackTrace()
            return false
        }
    }

    internal suspend fun markUploaded(context: Context,remoteImageInfo: RemoteImageInfo,remotePath:String){
        remoteImageInfo.remotePath = remotePath
        remoteImageInfo.uploadRunning = false
        getRemoteImageInfoDao(context).add(remoteImageInfo)
    }

    internal suspend fun markUploadError(context: Context,remoteImageInfo: RemoteImageInfo){
        remoteImageInfo.uploadRunning = false
        getRemoteImageInfoDao(context).add(remoteImageInfo)
    }

    private fun getRemoteImageInfoDao(context: Context) =
        getDatabase(context).remoteImageInfoDao
}