package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Note
import com.nami.peace.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Use case for adding a new note to a reminder.
 * 
 * This use case handles:
 * - Creating a new note with a timestamp
 * - Linking the note to its parent reminder
 * - Persisting the note to the database
 * 
 * Requirements: 5.1
 */
class AddNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Adds a new note to a reminder.
     * 
     * @param reminderId The ID of the parent reminder
     * @param content The text content of the note
     * @return The ID of the newly created note
     */
    suspend operator fun invoke(
        reminderId: Int,
        content: String
    ): Long {
        require(content.isNotBlank()) { "Note content cannot be blank" }
        require(reminderId > 0) { "Invalid reminder ID" }
        
        val note = Note(
            reminderId = reminderId,
            content = content.trim(),
            timestamp = System.currentTimeMillis()
        )
        
        return noteRepository.insertNote(note)
    }
}
