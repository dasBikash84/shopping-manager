package com.dasbikash.book_keeper_repo

import android.content.Context
import androidx.lifecycle.LiveData
import com.dasbikash.android_basic_utils.utils.debugLog
import com.dasbikash.book_keeper_repo.firebase.FireStoreNoteEntryService
import com.dasbikash.book_keeper_repo.model.NoteEntry

object NoteEntryRepo:BookKeeperRepo() {
    private fun getDao(context: Context) = getDatabase(context).noteEntryDao

    internal suspend fun syncData(context: Context){
        val latestUpdateTime = getDao(context).findAll().let {
            if (it.isNotEmpty()){
                it.sortedByDescending { it.modified }.map { it.modified }.first()
            }else{
                null
            }
        }
        FireStoreNoteEntryService
            .getLatestNoteEntriess(latestUpdateTime)
            .asSequence()
            .forEach {
                debugLog(it)
                getDao(context).add(it)
            }
    }

    suspend fun addNew(context: Context,
                       noteEntry: NoteEntry){
        FireStoreNoteEntryService
            .saveNoteEntry(
                noteEntry,
                { getDao(context).delete(noteEntry)}
            )
        getDao(context).add(noteEntry)
    }

    suspend fun update(context: Context,
                       noteEntry: NoteEntry){
        val copyOfOriginal = getDao(context).findById(noteEntry.id)!!
        FireStoreNoteEntryService
            .saveNoteEntry(
                noteEntry,
                { getDao(context).add(copyOfOriginal)}
            )
        getDao(context).add(noteEntry)
    }

    suspend fun delete(context: Context,
                       noteEntry: NoteEntry){
        val copyOfOriginal = noteEntry.copy()
        FireStoreNoteEntryService
            .deleteNoteEntry(
                noteEntry,
                { getDao(context).add(copyOfOriginal)}
            )
        getDao(context).delete(noteEntry)
    }

    fun findAllLd(context: Context): LiveData<List<NoteEntry>> =
        getDao(context).findAllLiveData()
}