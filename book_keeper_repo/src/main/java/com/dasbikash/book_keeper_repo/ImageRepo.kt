package com.dasbikash.book_keeper_repo

import android.content.Context
import android.graphics.Bitmap
import com.dasbikash.android_basic_utils.utils.FileUtils
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.android_image_utils.ImageUtils
import com.dasbikash.book_keeper_repo.bg_tasks.ImageUploaderTaskUtils
import com.dasbikash.book_keeper_repo.exceptions.FileDownloadException
import com.dasbikash.book_keeper_repo.exceptions.ImageDeletionException
import com.dasbikash.book_keeper_repo.firebase.FirebaseStorageService
import com.dasbikash.book_keeper_repo.model.RemoteImageInfo
import com.dasbikash.book_keeper_repo.utils.scaled
import java.io.File

object ImageRepo:BookKeeperRepo() {

    suspend fun downloadImageFile(context: Context,imageUrl:String): File? {
        try {
            imageUrl.split("/").let {
                if (it.isNotEmpty()){
                    FileUtils.readFileFromInternalStorage(context,it.last())?.let {
                        debugLog("Found on local storage")
                        return it
                    }
                }
            }
            return FirebaseStorageService.downloadImageFile(imageUrl).apply {
                imageUrl.split("/").last().let {
                    FileUtils.saveFileOnInternalStorage(this,context,it,true)
                    debugLog("Saved on local storage")
                }
            }
        }catch (ex: FileDownloadException){
            ex.printStackTrace()
            return null
        }
    }

    suspend fun uploadProductImage(context: Context,image: File):String {
        ImageUtils.getBitmapFromFileSuspended(image)!!.let {
            return uploadProductImage(context,it)
        }
    }
    suspend fun uploadProductImage(context: Context,imageBitmap: Bitmap):String {
        debugLog("uploadProductImage")
        val scaledBitmap = imageBitmap.scaled()
        val remoteImageInfo = RemoteImageInfo()
        val imageFile = ImageUtils.getPngFromBitmap(scaledBitmap,remoteImageInfo.localName,context)
        FileUtils.saveFileOnInternalStorage(imageFile,context,remoteImageInfo.localName)
        return scheduleImageUpload(scaledBitmap, remoteImageInfo, context)
    }

    internal suspend fun scheduleImageUpload(
        imageBitmap: Bitmap?=null,
        remoteImageInfo: RemoteImageInfo,
        context: Context
    ):String {
        val bitmapForUpload = imageBitmap ?: ImageUtils.getBitmapFromFile(FileUtils.readFileFromInternalStorage(context,remoteImageInfo.localName)!!)!!
        val remotePath =
            FirebaseStorageService
                .uploadProductImage(
                    bitmapForUpload,
                    remoteImageInfo.localName,
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