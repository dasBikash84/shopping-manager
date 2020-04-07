package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.model.User
import com.google.firebase.auth.FirebaseUser
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FirebaseUserService {

    suspend fun saveUser(user: User): User?{
        if (user.validateData()) {
            return FireStoreUtils.writeDocument(FireStoreRefUtils.getUserCollectionRef().document(user.id).path,user)
        }
        return null
    }

    suspend fun getUser(firebaseUser: FirebaseUser): User?{
        return FireStoreUtils.readDocument(FireStoreRefUtils.getUserCollectionRef().document(firebaseUser.uid).path,User::class.java)
    }

    suspend fun createUserForPhoneLogin(firebaseUser: FirebaseUser): User {
        val user = User(
            id = firebaseUser.uid,
            phone = firebaseUser.phoneNumber!!
        )
        return saveUser(user)!!
    }

    suspend fun findUserById(userId:String):User?{
        debugLog("findUserById: ${userId}")
        return suspendCoroutine<User?> {
            val continuation = it
            FireStoreRefUtils
                .getUserCollectionRef()
                .document(userId)
                .get()
                .addOnSuccessListener {
                    it.toObject(User::class.java).let {
                        if (it!=null){
                            debugLog("User found")
                            continuation.resume(it)
                        }else{
                            debugLog("User not found")
                            continuation.resume(null)
                        }
                    }
                }.addOnFailureListener {
                    debugLog("User not found")
                    it.printStackTrace()
                    continuation.resume(null)
                }

        }
    }
}