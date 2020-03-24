package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.book_keeper_repo.exceptions.FbDocumentReadException
import com.dasbikash.book_keeper_repo.exceptions.FbDocumentWriteException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class FireStoreUtils {

    companion object {
        suspend fun <T> readDocument(path: String?, type: Class<T>): T? {
            return suspendCoroutine {
                println("readDocument: $path")
                val continuation = it
                if (path != null) {
                    FireStoreConUtils.getFsDocument(path)
                        .get().addOnCompleteListener {
                            if (it.isSuccessful) {
                                println("Fb document read is Successful.")
                                continuation.resume(it.result?.toObject(type))
                            } else {
                                println("Fb document read failed.")
                                continuation.resume(null)
//                                continuation.resumeWithException(
//                                    FbDocumentReadException(
//                                        it.exception
//                                    )
//                                )
                            }
                        }
                } else {
                    continuation.resume(null)
                }
            }
        }

        suspend fun <T : Any> writeDocument(path: String?, payload: T?): T? {
            return suspendCoroutine {
                println("Going to write: $payload at $path")
                val continuation = it
                if (path != null && payload != null) {
                    FireStoreConUtils.getFsDocument(path)
                        .set(payload)
                        .addOnCompleteListener {
                            if (!it.isSuccessful) {
                                continuation.resumeWithException(
                                    FbDocumentWriteException(
                                        it.exception
                                    )
                                )
                            } else {
                                continuation.resume(payload)
                            }
                        }
                } else {
                    it.resume(null)
                }

            }
        }
    }
}