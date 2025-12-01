package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.Note
import com.nami.peace.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for retrieving all notes for a specific reminder.
 * 
 * This use case handles:
 * - Fetching notes from the database
 * - Sorting notes chronologically by timestamp (ascending)
 * - Providing a reactive Flow of notes
 * 
 * Requirements: 5.3
 */
class GetNotesForReminderUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    /**
     * Gets all notes for a reminder, sorted chronologically.
     * 
     * @param reminderId The ID of the reminder
     * @return A Flow of notes sorted by timestamp in ascending order (oldest first)
     */
    operator fun invoke(reminderId: Int): Flow<List<Note>> {
        require(reminderId > 0) { "Invalid reminder ID" }
        
        // The repository already sorts by timestamp ASC in the DAO query
        // This ensures chronological ordering as per requirement 5.3
        return noteRepository.getNotesForReminder(reminderId)
    }
}
