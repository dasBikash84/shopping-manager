package com.dasbikash.book_keeper_repo.firebase

import android.graphics.Bitmap
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.exceptions.FileDownloadException
import com.dasbikash.book_keeper_repo.exceptions.ImageDeletionException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object FirebaseStorageService {
    private val PROFILE_PICTURE_DIR = "profile_pictures"
    private val PRODUCT_PICTURE_DIR = "product_pictures"

    private suspend fun deleteFileByStorageReference(storageReference: StorageReference){
        return suspendCoroutine {
            val continuation = it
            storageReference.delete().addOnSuccessListener {
                debugLog("it.isSuccessful")
                continuation.resume(Unit)
            }.addOnFailureListener {
                debugLog(it.message ?: it::class.java.simpleName)
                continuation.resumeWithException(ImageDeletionException(it))
            }
        }
    }

    private fun uploadImageBitmap(bitmap: Bitmap, dir:String,fileName:String,
                                          doOnUpload:suspend (String)->Unit,doOnError:suspend ()->Unit):String{
        debugLog("uploadImageBitmap")
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        debugLog("byteArray.size: ${byteArray.size}")

        val storageRef = FirebaseStorage.getInstance().reference
        val filepath = storageRef.child("$dir/${fileName}")
        val uploadTask = filepath.putBytes(byteArray)

        uploadTask.addOnSuccessListener {
            debugLog("inside uploadTask.addOnSuccessListener")
            GlobalScope.launch { doOnUpload(filepath.path) }
        }.addOnFailureListener {
            debugLog("uploadTask.addOnFailureListener")
            debugLog(it.message ?: it::class.java.simpleName)
            GlobalScope.launch { doOnError() }
        }
        return filepath.path
    }

    suspend fun downloadImageFile(imageUrl:String):File{
        return suspendCoroutine<File> {
            val continuation = it
            val storageRef = FirebaseStorage.getInstance().reference
            val islandRef = storageRef.child(imageUrl)
            val localFile = File.createTempFile("images", "jpg")

            islandRef.getFile(localFile).addOnSuccessListener {
                continuation.resume(localFile)
            }.addOnFailureListener {
                debugLog(it.message ?: it::class.java.simpleName)
                continuation.resumeWithException(FileDownloadException(it))
            }
        }
    }

    suspend fun deleteFileByUrl(url:String){
        return deleteFileByStorageReference(
            FirebaseStorage.getInstance().getReferenceFromUrl(url)
        )
    }

    fun uploadImage(imageBitmap: Bitmap,fileName:String,isProductImage:Boolean = true,
                                   doOnUpload:suspend (String)->Unit,doOnError:suspend ()->Unit):String{
        debugLog("uploadImage")
        return when(isProductImage) {
            true -> uploadImageBitmap(
                imageBitmap, PRODUCT_PICTURE_DIR,
                fileName, doOnUpload, doOnError
            )
            false -> uploadImageBitmap(
                imageBitmap, PROFILE_PICTURE_DIR,
                fileName, doOnUpload, doOnError
            )
        }
    }
}