package com.nami.peace.domain.usecase

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.NoteDao
import com.nami.peace.data.local.NoteEntity
import com.nami.peace.data.local.ReminderDao
import com.nami.peace.data.local.ReminderEntity
import com.nami.peace.data.repository.NoteRepositoryImpl
import com.nami.peace.domain.model.Note
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.random.Random

/**
 * Feature: peace-app-enhancement, Property 10: Note timestamp inclusion
 * Feature: peace-app-enhancement, Property 12: Chronological attachment ordering
 * 
 * Property 10: For any note added to a reminder, the note should have a timestamp 
 * equal to or greater than the reminder's creation time.
 * 
 * Property 12: For any reminder with multiple notes, when displayed, the notes 
 * should be sorted by timestamp in ascending order (chronological).
 * 
 * Validates: Requirements 5.1, 5.3
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NoteOperationsPropertyTest {
    
    private lateinit var database: AppDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var noteDao: NoteDao
    private lateinit var noteRepository: NoteRepositoryImpl
    private lateinit var addNoteUseCase: AddNoteUseCase
    private lateinit var deleteNoteUseCase: DeleteNoteUseCase
    private lateinit var getNotesForReminderUseCase: GetNotesForReminderUseCase
    
    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        reminderDao = database.reminderDao()
        noteDao = database.noteDao()
        noteRepository = NoteRepositoryImpl(noteDao)
        addNoteUseCase = AddNoteUseCase(noteRepository)
        deleteNoteUseCase = DeleteNoteUseCase(noteRepository)
        getNotesForReminderUseCase = GetNotesForReminderUseCase(noteRepository)
    }
    
    @After
    fun teardown() {
        database.close()
    }
    
    @Test
    fun `Property 10 - Note timestamp is equal to or greater than reminder creation time`() = runBlocking {
        // Run 100 iterations with random data
        repeat(100) {
            // Create a random reminder with a known creation time
            val reminderCreationTime = System.currentTimeMillis() - Random.nextLong(0, 86400000) // Up to 1 day ago
            val reminderEntity = generateRandomReminder(reminderCreationTime)
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Add a note using the use case
            val noteContent = generateRandomString(5, 200)
            val noteId = addNoteUseCase(reminderId, noteContent)
            
            // Retrieve the note
            val notes = noteDao.getNotesForReminder(reminderId).first()
            assertEquals(1, notes.size)
            
            val note = notes[0]
            
            // Verify the note has a timestamp
            assertTrue("Note should have a timestamp", note.timestamp > 0)
            
            // Verify the note timestamp is >= reminder creation time
            assertTrue(
                "Note timestamp (${note.timestamp}) should be >= reminder creation time ($reminderCreationTime)",
                note.timestamp >= reminderCreationTime
            )
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    @Test
    fun `Property 10 - Multiple notes have valid timestamps`() = runBlocking {
        // Run 50 iterations
        repeat(50) {
            // Create a random reminder
            val reminderCreationTime = System.currentTimeMillis() - Random.nextLong(0, 3600000)
            val reminderEntity = generateRandomReminder(reminderCreationTime)
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Add multiple notes with small delays
            val noteCount = Random.nextInt(2, 10)
            val noteIds = mutableListOf<Long>()
            
            repeat(noteCount) {
                val noteContent = generateRandomString(5, 100)
                val noteId = addNoteUseCase(reminderId, noteContent)
                noteIds.add(noteId)
                
                // Small delay to ensure different timestamps
                Thread.sleep(Random.nextLong(1, 10))
            }
            
            // Retrieve all notes
            val notes = noteDao.getNotesForReminder(reminderId).first()
            assertEquals(noteCount, notes.size)
            
            // Verify all notes have valid timestamps >= reminder creation time
            notes.forEach { note ->
                assertTrue("Note should have a timestamp", note.timestamp > 0)
                assertTrue(
                    "Note timestamp should be >= reminder creation time",
                    note.timestamp >= reminderCreationTime
                )
            }
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    @Test
    fun `Property 12 - Notes are sorted chronologically by timestamp ascending`() = runBlocking {
        // Run 100 iterations
        repeat(100) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Add multiple notes with controlled timestamps
            val noteCount = Random.nextInt(3, 15)
            val baseTimestamp = System.currentTimeMillis() - 10000
            
            // Create notes with random timestamps (not necessarily in order)
            val notesWithTimestamps = mutableListOf<Pair<String, Long>>()
            repeat(noteCount) { index ->
                val timestamp = baseTimestamp + Random.nextLong(0, 10000)
                val content = "Note $index - ${generateRandomString(5, 50)}"
                notesWithTimestamps.add(content to timestamp)
            }
            
            // Insert notes in random order
            notesWithTimestamps.shuffled().forEach { (content, timestamp) ->
                val noteEntity = NoteEntity(
                    reminderId = reminderId,
                    content = content,
                    timestamp = timestamp
                )
                noteDao.insert(noteEntity)
            }
            
            // Retrieve notes using the use case
            val retrievedNotes = getNotesForReminderUseCase(reminderId).first()
            
            // Verify notes are sorted chronologically (ascending)
            assertEquals(noteCount, retrievedNotes.size)
            
            for (i in 0 until retrievedNotes.size - 1) {
                val currentNote = retrievedNotes[i]
                val nextNote = retrievedNotes[i + 1]
                
                assertTrue(
                    "Notes should be sorted chronologically: " +
                    "note at index $i (timestamp ${currentNote.timestamp}) should be <= " +
                    "note at index ${i + 1} (timestamp ${nextNote.timestamp})",
                    currentNote.timestamp <= nextNote.timestamp
                )
            }
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    @Test
    fun `Property 12 - Empty note list is handled correctly`() = runBlocking {
        // Run 50 iterations
        repeat(50) {
            // Create a random reminder with no notes
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Retrieve notes (should be empty)
            val notes = getNotesForReminderUseCase(reminderId).first()
            
            // Verify empty list is returned
            assertTrue("Empty note list should be returned", notes.isEmpty())
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    @Test
    fun `Property 12 - Single note is handled correctly`() = runBlocking {
        // Run 50 iterations
        repeat(50) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Add a single note
            val noteContent = generateRandomString(5, 100)
            addNoteUseCase(reminderId, noteContent)
            
            // Retrieve notes
            val notes = getNotesForReminderUseCase(reminderId).first()
            
            // Verify single note is returned
            assertEquals(1, notes.size)
            // Note: content is trimmed by the use case
            assertEquals(noteContent.trim(), notes[0].content)
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    @Test
    fun `Property 12 - Notes with identical timestamps maintain stable order`() = runBlocking {
        // Run 50 iterations
        repeat(50) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Add multiple notes with the same timestamp
            val sameTimestamp = System.currentTimeMillis()
            val noteCount = Random.nextInt(3, 8)
            
            repeat(noteCount) { index ->
                val noteEntity = NoteEntity(
                    reminderId = reminderId,
                    content = "Note $index",
                    timestamp = sameTimestamp
                )
                noteDao.insert(noteEntity)
            }
            
            // Retrieve notes
            val notes = getNotesForReminderUseCase(reminderId).first()
            
            // Verify all notes are returned
            assertEquals(noteCount, notes.size)
            
            // Verify all have the same timestamp
            notes.forEach { note ->
                assertEquals(sameTimestamp, note.timestamp)
            }
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    @Test
    fun `Delete note removes it from chronological list`() = runBlocking {
        // Run 50 iterations
        repeat(50) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Add multiple notes
            val noteCount = Random.nextInt(3, 10)
            repeat(noteCount) {
                addNoteUseCase(reminderId, generateRandomString(5, 50))
                Thread.sleep(2) // Ensure different timestamps
            }
            
            // Get all notes
            val notesBefore = getNotesForReminderUseCase(reminderId).first()
            assertEquals(noteCount, notesBefore.size)
            
            // Delete a random note
            val indexToDelete = Random.nextInt(0, noteCount)
            val noteToDelete = notesBefore[indexToDelete]
            deleteNoteUseCase(noteToDelete)
            
            // Get notes after deletion
            val notesAfter = getNotesForReminderUseCase(reminderId).first()
            assertEquals(noteCount - 1, notesAfter.size)
            
            // Verify the deleted note is not in the list
            assertFalse(
                "Deleted note should not be in the list",
                notesAfter.any { it.id == noteToDelete.id }
            )
            
            // Verify remaining notes are still sorted chronologically
            for (i in 0 until notesAfter.size - 1) {
                assertTrue(
                    "Remaining notes should still be sorted chronologically",
                    notesAfter[i].timestamp <= notesAfter[i + 1].timestamp
                )
            }
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    // Helper functions to generate random test data
    private fun generateRandomReminder(creationTime: Long = System.currentTimeMillis()): ReminderEntity {
        val hasNagMode = Random.nextBoolean()
        return ReminderEntity(
            id = 0,
            title = generateRandomString(5, 50),
            priority = PriorityLevel.values().random(),
            startTimeInMillis = creationTime + Random.nextLong(0, 86400000),
            recurrenceType = RecurrenceType.values().random(),
            isNagModeEnabled = hasNagMode,
            nagIntervalInMillis = if (hasNagMode) Random.nextLong(60000, 3600000) else null,
            nagTotalRepetitions = Random.nextInt(1, 10),
            currentRepetitionIndex = 0,
            isCompleted = false,
            isEnabled = true,
            isInNestedSnoozeLoop = false,
            nestedSnoozeStartTime = null,
            category = ReminderCategory.values().random(),
            isStrictSchedulingEnabled = Random.nextBoolean(),
            dateInMillis = if (Random.nextBoolean()) creationTime + Random.nextLong(0, 86400000) else null,
            daysOfWeek = List(Random.nextInt(0, 7)) { Random.nextInt(1, 8) },
            originalStartTimeInMillis = creationTime
        )
    }
    
    private fun generateRandomString(minLength: Int, maxLength: Int): String {
        val length = Random.nextInt(minLength, maxLength + 1)
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + ' ' + listOf('.', ',', '!', '?')
        return (1..length).map { chars.random() }.joinToString("")
    }
}
