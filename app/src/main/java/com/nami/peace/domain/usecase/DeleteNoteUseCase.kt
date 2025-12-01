package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Note
import com.nami.peace.domain.repository.NoteRepository
import javax.inject.Inject

/**
 * Use case for deleting a note from a reminder.
 * 
 * This use case handles:
 * - Removing a note from the database
 * - Ensuring the note is properly deleted
 * 
 * Requirements: 5.4
 */
class DeleteNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Deletes a note from a reminder.
     * 
     * @param note The note to delete
     */
    suspend operator fun invoke(note: Note) {
        require(note.id > 0) { "Cannot delete note with invalid ID" }
        
        noteRepository.deleteNote(note)
    }
}
