package com.dasbikash.book_keeper_repo.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

internal object FireStoreConUtils {

    private lateinit var firebaseFirestore: FirebaseFirestore

    fun getDbConnection(): FirebaseFirestore {
        if (!FireStoreConUtils::firebaseFirestore.isInitialized) {
            firebaseFirestore = FirebaseFirestore.getInstance()
            val settings =
                FirebaseFirestoreSettings
                    .Builder()
                    .setPersistenceEnabled(true)
                    .build()
            firebaseFirestore.firestoreSettings = settings
        }
        return firebaseFirestore
    }

    fun getFsDocument(path: String) = getDbConnection().document(path)

    fun logOut() {
        FirebaseAuth.getInstance().signOut()
    }
}