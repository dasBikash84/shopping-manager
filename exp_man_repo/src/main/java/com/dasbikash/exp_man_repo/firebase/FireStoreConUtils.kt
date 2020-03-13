package com.dasbikash.exp_man_repo.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

internal object FireStoreConUtils {

    private lateinit var firebaseFirestore: FirebaseFirestore

    private fun getDbConnection(): FirebaseFirestore {
        if (!FireStoreConUtils::firebaseFirestore.isInitialized) {
            firebaseFirestore = FirebaseFirestore.getInstance()
            val settings =
                FirebaseFirestoreSettings
                    .Builder()
                    .setPersistenceEnabled(false) //cache diabled
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