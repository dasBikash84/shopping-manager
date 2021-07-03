package com.dasbikash.book_keeper_repo.firebase

import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.AuthRepo
import com.dasbikash.book_keeper_repo.model.EventNotification
import com.dasbikash.book_keeper_repo.model.NoteEntry
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Query
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal object FireStoreNoteEntryService {

    private const val MODIFIED_FIELD = "modified"
    private const val USER_ID_FIELD = "userId"
    private const val ACTIVE_FIELD = "active"

    fun saveNoteEntry(noteEntry: NoteEntry,doOnError:suspend ()->Unit) {
        noteEntry.updateModified()
        debugLog(noteEntry)
        FireStoreRefUtils
            .getNoteEntryCollectionRef()
            .document(noteEntry.id)
            .set(noteEntry)
            .addOnSuccessListener { debugLog("noteEntry saved") }
            .addOnFailureListener {
                debugLog("noteEntry save failure")
                GlobalScope.launch { doOnError()}
                it.printStackTrace()
            }
    }

    fun deleteNoteEntry(noteEntry: NoteEntry,doOnError:suspend ()->Unit) {
        noteEntry.active=false
        return saveNoteEntry(noteEntry, doOnError)
    }

    suspend fun getLatestNoteEntriess(lastUpdated: Timestamp?):List<NoteEntry>{

        debugLog("lastUpdated:$lastUpdated")

        var query = FireStoreRefUtils
                                .getNoteEntryCollectionRef()
                                .whereEqualTo(USER_ID_FIELD, AuthRepo.getUserId())

        if (lastUpdated!=null){
            query = query.whereGreaterThan(MODIFIED_FIELD,lastUpdated)
        }else{
            query = query.whereEqualTo(ACTIVE_FIELD,true)
        }

        return executeQuery(query)
    }

    private suspend fun executeQuery(query: Query): List<NoteEntry> {
        return suspendCoroutine {
            val continuation = it
            query.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects(NoteEntry::class.java))
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    continuation.resume(emptyList())
                }
        }
    }
}