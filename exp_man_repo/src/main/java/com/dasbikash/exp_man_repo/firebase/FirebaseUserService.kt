package com.dasbikash.exp_man_repo.firebase

import com.dasbikash.exp_man_repo.User
import com.google.firebase.auth.FirebaseUser

internal object FirebaseUserService {
    private const val USER_COLLECTION_NAME = "users"

    suspend fun saveUser(user: User):User?{
        if (user.validateData()) {
            return FireStoreUtils.writeDocument("$USER_COLLECTION_NAME/${user.id}",user)
        }
        return null
    }

    suspend fun getUser(firebaseUser: FirebaseUser):User?{
        return FireStoreUtils.readDocument("$USER_COLLECTION_NAME/${firebaseUser.uid}",User::class.java)
    }
}