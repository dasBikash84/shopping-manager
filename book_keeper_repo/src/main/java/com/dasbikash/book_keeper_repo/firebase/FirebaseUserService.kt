package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.book_keeper_repo.model.User
import com.google.firebase.auth.FirebaseUser

internal object FirebaseUserService {
    private const val USER_COLLECTION_NAME = "users"

    suspend fun saveUser(user: User): User?{
        if (user.validateData()) {
            return FireStoreUtils.writeDocument("$USER_COLLECTION_NAME/${user.id}",user)
        }
        return null
    }

    suspend fun getUser(firebaseUser: FirebaseUser): User?{
        return FireStoreUtils.readDocument("$USER_COLLECTION_NAME/${firebaseUser.uid}",
            User::class.java)
    }

    suspend fun createUserForPhoneLogin(firebaseUser: FirebaseUser): User {
        val user = User(
            id = firebaseUser.uid,
            phone = firebaseUser.phoneNumber!!
        )
        return saveUser(user)!!
    }
}