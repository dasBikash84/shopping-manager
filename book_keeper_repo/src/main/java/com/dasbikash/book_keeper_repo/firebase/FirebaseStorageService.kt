package com.dasbikash.book_keeper_repo.firebase

import android.graphics.Bitmap
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.exceptions.FileDownloadException
import com.dasbikash.book_keeper_repo.exceptions.ImageDeletionException
import com.dasbikash.book_keeper_repo.exceptions.ImageUploadException
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*
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

    private suspend fun uploadFile(
        uploadTask: UploadTask,
        filepath: StorageReference
    ): String {
        return suspendCoroutine<String> {
            val continuation = it
            uploadTask.addOnSuccessListener {
                filepath.downloadUrl.addOnSuccessListener {
                    continuation.resume(it.toString())
                }.addOnFailureListener {
                    debugLog(it.message ?: it::class.java.simpleName)
                    continuation.resumeWithException(ImageUploadException(it))
                }
            }.addOnFailureListener {
                debugLog(it.message ?: it::class.java.simpleName)
                continuation.resumeWithException(ImageUploadException(it))
            }
        }
    }

    private suspend fun uploadImageBitmap(bitmap: Bitmap, dir:String):String{

        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()

        val storageRef = FirebaseStorage.getInstance().reference
        val namePrefix = UUID.randomUUID().toString().let { it.substring(it.length-12,it.length) }
        val filepath = storageRef.child("$dir/${namePrefix}_${System.nanoTime()}")
        val uploadTask = filepath.putBytes(byteArray)

        return uploadFile(
            uploadTask,
            filepath
        )
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

    suspend fun uploadProductImage(imageBitmap: Bitmap): String {
        return uploadImageBitmap(
            imageBitmap,
            PRODUCT_PICTURE_DIR
        )
    }

    suspend fun uploadProfilePicture(imageBitmap: Bitmap): String {
        return uploadImageBitmap(
            imageBitmap,
            PROFILE_PICTURE_DIR
        )
    }
}