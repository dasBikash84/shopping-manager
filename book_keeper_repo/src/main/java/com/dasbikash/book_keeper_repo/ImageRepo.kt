package com.dasbikash.book_keeper_repo

import android.graphics.Bitmap
import com.dasbikash.book_keeper_repo.exceptions.FileDownloadException
import com.dasbikash.book_keeper_repo.exceptions.ImageDeletionException
import com.dasbikash.book_keeper_repo.exceptions.ImageUploadException
import com.dasbikash.book_keeper_repo.firebase.FirebaseStorageService
import java.io.File

object ImageRepo:BookKeeperRepo() {

    suspend fun downloadImageFile(imageUrl:String): File? {
        try {
            return FirebaseStorageService.downloadImageFile(imageUrl)
        }catch (ex: FileDownloadException){
            ex.printStackTrace()
            return null
        }
    }

    suspend fun uploadProductImage(imageBitmap: Bitmap): String? {
        try {
            return FirebaseStorageService.uploadProductImage(imageBitmap)
        }catch (ex: ImageUploadException){
            ex.printStackTrace()
            return null
        }
    }

    suspend fun deleteFileByUrl(url:String):Boolean{
        try {
            FirebaseStorageService.deleteFileByUrl(url)
            return true
        }catch (ex: ImageDeletionException){
            ex.printStackTrace()
            return false
        }
    }
}