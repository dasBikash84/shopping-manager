package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.SupportedLanguage
import com.dasbikash.book_keeper_repo.model.User
import com.dasbikash.book_keeper_repo.utils.ValidationUtils
import com.dasbikash.firebase_auth.FirebaseAuthService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Query
import java.util.*
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
            FireStoreRefUtils
                .getUserCollectionRef()
                .document(user.id)
                .set(user)
                .addOnSuccessListener {
                    debugLog("User data saved")
                }
                .addOnFailureListener {
                    debugLog("User data save failure")
                    it.printStackTrace()
                }
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
        val sanitizedEmail = email.trim().toLowerCase(Locale.ENGLISH)
        if (!ValidationUtils.validateEmailAddress(sanitizedEmail)){
            return emptyList()
        }
        debugLog("findUsersByEmail: $sanitizedEmail")
        val query = FireStoreRefUtils
                            .getUserCollectionRef()
                            .whereEqualTo(EMAIL_FIELD,sanitizedEmail)

        return processUserListQuery(query)
    }

    suspend fun findUsersByPhone(phone: String):List<User>{
        debugLog("findUsersByPhone: $phone")
        val sanitizedNumber = ValidationUtils.sanitizeNumber(phone)
        if (!ValidationUtils.validateMobileNumber(sanitizedNumber)){
            return emptyList()
        }
        debugLog("findUsersByPhone: $sanitizedNumber")
        val query = FireStoreRefUtils
                            .getUserCollectionRef()
                            .whereEqualTo(PHONE_FIELD,sanitizedNumber)

        return processUserListQuery(query)//.filter { it.id != AuthRepo.getUserId() }
    }

    //Used to check if the phone number is already used for login
    suspend fun findUsersForPhoneLogin(phone: String):List<User>{
        debugLog("findUsersForPhoneLogin: $phone")
        val query = FireStoreRefUtils
                            .getUserCollectionRef()
                            .whereEqualTo(PHONE_FIELD,ValidationUtils.sanitizeNumber(phone))
                            .whereEqualTo(PHONE_LOGIN_FIELD,true)

        return processUserListQuery(query)
    }

    //Used to check if the phone number is already used as contact by any existing users
    suspend fun findEmailLoginUsersByPhone(phone: String):List<User>{
        debugLog("findUsersByPhone: $phone")
        val query = FireStoreRefUtils
                            .getUserCollectionRef()
                            .whereEqualTo(PHONE_FIELD,ValidationUtils.sanitizeNumber(phone))
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

    suspend fun loginAnonymous():Boolean{
        debugLog("loginAnonymous")
        if (FirebaseAuth.getInstance().currentUser == null){
            return suspendCoroutine {
                val continuation = it
                FirebaseAuth
                    .getInstance()
                    .signInAnonymously()
                    .addOnSuccessListener {
                        debugLog("loginAnonymous success")
                        continuation.resume(true)
                    }
                    .addOnFailureListener {
                        debugLog("loginAnonymous failure")
                        it.printStackTrace()
                        continuation.resume(false)
                    }
            }
        }else{
            debugLog("loginAnonymous success")
            return true
        }
    }
}