package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.exceptions.FbDocumentReadException
import com.dasbikash.book_keeper_repo.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseUser
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal object FirebaseUserService {

    private const val MODIFIED_FIELD = "modified"
    private const val ID_FIELD = "id"

    fun saveUser(user: User): User?{
        if (user.validateData()) {
            user.updateModified()
            FireStoreRefUtils.getUserCollectionRef().document(user.id).set(user)
            return user
        }
        return null
    }

    suspend fun getUser(firebaseUser: FirebaseUser): User?{
        return FireStoreUtils.readDocument(FireStoreRefUtils.getUserCollectionRef().document(firebaseUser.uid).path,User::class.java)
    }

    fun createUserForPhoneLogin(firebaseUser: FirebaseUser): User {
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

    suspend fun getUpdatedUserInfo(user: User):User?{

        debugLog("user:$user")

        val query = FireStoreRefUtils
                        .getUserCollectionRef()
                        .whereEqualTo(ID_FIELD,user.id)
                        .whereGreaterThan(MODIFIED_FIELD,user.modified)

        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnCompleteListener(OnCompleteListener {
                    if(it.isSuccessful){
                        try {
                            it.result!!.toObjects(User::class.java).let {
                                if (it.isNotEmpty()){
                                    continuation.resume(it.get(0))
                                }else{
                                    continuation.resume(null)
                                }
                            }
                        }catch (ex:Throwable){
                            ex.printStackTrace()
                            continuation.resume(null)
                        }
                    }else{
                        continuation.resume(null)
                    }
                })
        }
    }
}