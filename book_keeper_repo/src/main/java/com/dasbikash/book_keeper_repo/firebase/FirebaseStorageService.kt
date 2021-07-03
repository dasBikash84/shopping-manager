package com.dasbikash.book_keeper_repo.firebase

import android.graphics.Bitmap
import com.dasbikash.android_basic_utils.utils.debugLog
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

    fun downloadImageFile(imageUrl:String,fileName: String,
                            doOnDownload:(File)->Unit,doOnError: (() -> Unit)?){

        val (prefix,suffix) = fileName.split(".").let {
            if (it.isEmpty()){
                return
            }else{
                if (it.size==1){
                    return@let Pair(it.get(0),"")
                }else{
                    return@let Pair(it.get(0),it.get(1))
                }
            }
        }.apply { debugLog(this) }
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child(imageUrl)
        val localFile = File.createTempFile(prefix, suffix)
        fileRef
            .getFile(localFile)
            .addOnSuccessListener{
                try {
                    doOnDownload(localFile)
                }catch (ex:Throwable){
                    ex.printStackTrace()
                }
            }
            .addOnFailureListener {
                debugLog(it.message ?: it::class.java.simpleName)
                it.printStackTrace()
                doOnError?.invoke()
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