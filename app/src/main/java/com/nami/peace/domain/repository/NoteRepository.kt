package com.nami.peace.domain.repository

import com.nami.peace.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getNotesForReminder(reminderId: Int): Flow<List<Note>>
    suspend fun insertNote(note: Note): Long
    suspend fun deleteNote(note: Note)
}
