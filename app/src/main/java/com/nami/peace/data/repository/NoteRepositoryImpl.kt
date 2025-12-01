package com.nami.peace.data.repository

import com.nami.peace.data.local.NoteDao
import com.nami.peace.domain.model.Note
import com.nami.peace.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao
) : NoteRepository {

    override fun getNotesForReminder(reminderId: Int): Flow<List<Note>> {
        return dao.getNotesForReminder(reminderId).map { entities ->
            entities.map { Note.fromEntity(it) }
        }
    }

    override suspend fun insertNote(note: Note): Long {
        return dao.insert(note.toEntity())
    }

    override suspend fun deleteNote(note: Note) {
        dao.delete(note.toEntity())
    }
}
