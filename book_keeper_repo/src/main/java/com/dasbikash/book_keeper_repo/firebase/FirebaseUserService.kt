package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.SupportedLanguage
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.book_keeper_repo.utils.ValidationUtils
import com.google.firebase.firestore.Query
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FirebaseUserService {

    private const val MODIFIED_FIELD = "modified"
    private const val ID_FIELD = "id"
    private const val EMAIL_FIELD = "email"
    private const val PHONE_FIELD = "phone"
    private const val PHONE_LOGIN_FIELD = "mobileLogin"

    fun saveUser(user: User): User?{
        if (user.validateData()) {
            user.updateModified()
            user.phone = user.phone?.let { ValidationUtils.sanitizeNumber(it) }
            FireStoreRefUtils.getUserCollectionRef().document(user.id).set(user)
            return user
        }
        return null
    }

    suspend fun getUser(userId: String): User?{
        return suspendCoroutine {
            val continuation = it
            FireStoreRefUtils
                .getUserCollectionRef()
                .document(userId)
                .get()
                .addOnCompleteListener {
                    it.result.let {
                        if (it!=null){
                            continuation.resume(it.toObject(User::class.java))
                        }else{
                            continuation.resume(null)
                        }
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    continuation.resume(null)
                }
        }
    }

    fun createUserForPhoneLogin(phone: String,
                                language: SupportedLanguage
    ): User {
        val user = User(
            id = com.dasbikash.firebase_auth.FirebaseAuthService.getUserId()!!,
            phone = phone,
            mobileLogin = true,
            language = language
        )
        return saveUser(user)!!
    }

    suspend fun findUserById(userId:String):User?{

        debugLog("userId:$userId")

        val query = FireStoreRefUtils
                            .getUserCollectionRef()
                            .whereEqualTo(ID_FIELD,userId)
        processUserListQuery(query).let {
            if (it.isEmpty()){
                return null
            }else{
                return it.get(0)
            }
        }
    }

    suspend fun getUpdatedUserInfo(user: User):User?{

        debugLog("user:$user")

        val query = FireStoreRefUtils
                        .getUserCollectionRef()
                        .whereEqualTo(ID_FIELD,user.id)
                        .whereGreaterThan(MODIFIED_FIELD,user.modified)
        processUserListQuery(query).let {
            if (it.isEmpty()){
                return null
            }else{
                return it.get(0)
            }
        }
    }

    suspend fun findUsersByEmail(email: String):List<User>{
        debugLog("findUsersByEmail: $email")
        val query = FireStoreRefUtils
                            .getUserCollectionRef()
                            .whereEqualTo(EMAIL_FIELD,email.trim())

        return processUserListQuery(query).filter { it.id != AuthRepo.getUserId() }
    }

    suspend fun findUsersByPhone(phone: String):List<User>{
        debugLog("findUsersByPhone: $phone")
        val query = FireStoreRefUtils
                            .getUserCollectionRef()
                            .whereEqualTo(PHONE_FIELD,phone)

        return processUserListQuery(query).filter { it.id != AuthRepo.getUserId() }
    }

    suspend fun findEmailLoginUsersByPhone(phone: String):List<User>{
        debugLog("findUsersByPhone: $phone")
        val query = FireStoreRefUtils
                            .getUserCollectionRef()
                            .whereEqualTo(PHONE_FIELD,phone)
                            .whereEqualTo(PHONE_LOGIN_FIELD,false)

        return processUserListQuery(query)
    }

    private suspend fun processUserListQuery(query: Query): List<User> {
        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects(User::class.java))
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    continuation.resume(emptyList())
                }
        }
    }
}