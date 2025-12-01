package com.nami.peace.domain.usecase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.data.local.AppDatabase
import com.nami.peace.data.local.AttachmentDao
import com.nami.peace.data.local.ReminderDao
import com.nami.peace.data.local.ReminderEntity
import com.nami.peace.data.repository.AttachmentRepositoryImpl
import com.nami.peace.domain.model.Attachment
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.util.attachment.AttachmentManager
import com.nami.peace.util.attachment.AttachmentManagerImpl
import com.nami.peace.util.attachment.AttachmentPaths
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

/**
 * Property-based tests for attachment operations.
 * 
 * Tests the following correctness properties:
 * - Property 11: Attachment storage and thumbnail
 * - Property 13: Attachment deletion completeness
 * 
 * Feature: peace-app-enhancement
 * Validates: Requirements 5.2, 5.4
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class AttachmentOperationsPropertyTest {
    
    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var reminderDao: ReminderDao
    private lateinit var attachmentDao: AttachmentDao
    private lateinit var attachmentRepository: AttachmentRepositoryImpl
    private lateinit var attachmentManager: AttachmentManager
    private lateinit var addAttachmentUseCase: AddAttachmentUseCase
    private lateinit var deleteAttachmentUseCase: DeleteAttachmentUseCase
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        
        reminderDao = database.reminderDao()
        attachmentDao = database.attachmentDao()
        attachmentRepository = AttachmentRepositoryImpl(attachmentDao)
        attachmentManager = AttachmentManagerImpl(context)
        addAttachmentUseCase = AddAttachmentUseCase(attachmentRepository, attachmentManager)
        deleteAttachmentUseCase = DeleteAttachmentUseCase(attachmentRepository, attachmentManager)
    }
    
    @After
    fun teardown() {
        database.close()
        // Clean up any test files
        cleanupTestFiles()
    }
    
    /**
     * Property 11: Attachment storage and thumbnail
     * 
     * For any image attachment, both the full image and thumbnail should be stored 
     * in local storage with valid file paths.
     * 
     * Validates: Requirements 5.2
     */
    @Test
    fun `Property 11 - Attachment storage and thumbnail - for any attachment, both full image and thumbnail are stored with valid paths`() = runBlocking {
        // Run 100 iterations with random data
        repeat(100) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Create a test image file
            val testImageUri = createTestImageFile()
            
            // Add attachment using the use case
            val attachmentId = addAttachmentUseCase(testImageUri, reminderId)
            
            // Retrieve the attachment from database
            val attachments = attachmentDao.getAttachmentsForReminder(reminderId).first()
            assertEquals("Should have exactly one attachment", 1, attachments.size)
            
            val attachmentEntity = attachments[0]
            val attachment = Attachment.fromEntity(attachmentEntity)
            
            // Property 11: Both paths should be non-empty and valid
            assertNotEquals("Full image path should not be empty", "", attachment.filePath)
            assertNotEquals("Thumbnail path should not be empty", "", attachment.thumbnailPath)
            
            // Paths should be different
            assertNotEquals("Full image and thumbnail paths should be different", 
                attachment.filePath, attachment.thumbnailPath)
            
            // Both files should actually exist on disk
            val fullImageFile = File(attachment.filePath)
            val thumbnailFile = File(attachment.thumbnailPath)
            
            assertTrue("Full image file should exist on disk", fullImageFile.exists())
            assertTrue("Thumbnail file should exist on disk", thumbnailFile.exists())
            
            // Both files should have non-zero size
            assertTrue("Full image should have non-zero size", fullImageFile.length() > 0)
            assertTrue("Thumbnail should have non-zero size", thumbnailFile.length() > 0)
            
            // Note: We don't assert thumbnail size < full image size because for very small test images,
            // JPEG compression overhead can make the thumbnail larger than the original
            
            // Paths should be absolute (Unix starts with /, Windows starts with drive letter)
            val isAbsolutePath = { path: String ->
                path.startsWith("/") || (path.length >= 3 && path[1] == ':')
            }
            assertTrue("Full image path should be absolute", isAbsolutePath(attachment.filePath))
            assertTrue("Thumbnail path should be absolute", isAbsolutePath(attachment.thumbnailPath))
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
            fullImageFile.delete()
            thumbnailFile.delete()
        }
    }
    
    /**
     * Property 13: Attachment deletion completeness
     * 
     * For any attachment deletion, both the database record and the files on disk 
     * should be removed.
     * 
     * Validates: Requirements 5.4
     */
    @Test
    fun `Property 13 - Attachment deletion completeness - for any attachment, both database record and files are deleted`() = runBlocking {
        // Run 100 iterations with random data
        repeat(100) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Create a test image file
            val testImageUri = createTestImageFile()
            
            // Add attachment
            val attachmentId = addAttachmentUseCase(testImageUri, reminderId)
            
            // Retrieve the attachment
            val attachmentsBefore = attachmentDao.getAttachmentsForReminder(reminderId).first()
            assertEquals("Should have one attachment before deletion", 1, attachmentsBefore.size)
            
            val attachment = Attachment.fromEntity(attachmentsBefore[0])
            val fullImagePath = attachment.filePath
            val thumbnailPath = attachment.thumbnailPath
            
            // Verify files exist before deletion
            val fullImageFile = File(fullImagePath)
            val thumbnailFile = File(thumbnailPath)
            assertTrue("Full image should exist before deletion", fullImageFile.exists())
            assertTrue("Thumbnail should exist before deletion", thumbnailFile.exists())
            
            // Delete the attachment using the use case
            deleteAttachmentUseCase(attachment)
            
            // Property 13: Verify database record is deleted
            val attachmentsAfter = attachmentDao.getAttachmentsForReminder(reminderId).first()
            assertEquals("Database should have no attachments after deletion", 0, attachmentsAfter.size)
            
            // Property 13: Verify both files are deleted from disk
            assertFalse("Full image file should be deleted from disk", fullImageFile.exists())
            assertFalse("Thumbnail file should be deleted from disk", thumbnailFile.exists())
            
            // Clean up
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    /**
     * Property 11 (Edge Case): Multiple attachments for same reminder
     * 
     * For any reminder with multiple attachments, each should have unique file paths.
     */
    @Test
    fun `Property 11 - Multiple attachments have unique file paths`() = runBlocking {
        // Run 50 iterations
        repeat(50) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Add multiple attachments
            val attachmentCount = Random.nextInt(2, 5)
            val attachmentIds = mutableListOf<Long>()
            
            repeat(attachmentCount) {
                val testImageUri = createTestImageFile()
                val attachmentId = addAttachmentUseCase(testImageUri, reminderId)
                attachmentIds.add(attachmentId)
                // Small delay to ensure unique timestamps
                Thread.sleep(10)
            }
            
            // Retrieve all attachments
            val attachments = attachmentDao.getAttachmentsForReminder(reminderId).first()
            assertEquals("Should have all attachments", attachmentCount, attachments.size)
            
            // Verify all file paths are unique
            val fullImagePaths = attachments.map { it.filePath }.toSet()
            val thumbnailPaths = attachments.map { it.thumbnailPath }.toSet()
            
            assertEquals("All full image paths should be unique", attachmentCount, fullImagePaths.size)
            assertEquals("All thumbnail paths should be unique", attachmentCount, thumbnailPaths.size)
            
            // Verify all files exist
            attachments.forEach { attachment ->
                assertTrue("Full image should exist", File(attachment.filePath).exists())
                assertTrue("Thumbnail should exist", File(attachment.thumbnailPath).exists())
            }
            
            // Clean up
            attachments.forEach { attachment ->
                File(attachment.filePath).delete()
                File(attachment.thumbnailPath).delete()
            }
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    /**
     * Property 13 (Edge Case): Deleting one attachment doesn't affect others
     * 
     * For any reminder with multiple attachments, deleting one should not affect the others.
     */
    @Test
    fun `Property 13 - Deleting one attachment does not affect others`() = runBlocking {
        // Run 50 iterations
        repeat(50) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Add multiple attachments
            val attachmentCount = Random.nextInt(3, 6)
            repeat(attachmentCount) {
                val testImageUri = createTestImageFile()
                addAttachmentUseCase(testImageUri, reminderId)
                Thread.sleep(10)
            }
            
            // Get all attachments
            val attachmentsBefore = attachmentDao.getAttachmentsForReminder(reminderId).first()
            assertEquals("Should have all attachments", attachmentCount, attachmentsBefore.size)
            
            // Pick a random attachment to delete
            val indexToDelete = Random.nextInt(0, attachmentCount)
            val attachmentToDelete = Attachment.fromEntity(attachmentsBefore[indexToDelete])
            val pathsToDelete = Pair(attachmentToDelete.filePath, attachmentToDelete.thumbnailPath)
            
            // Store paths of other attachments
            val otherAttachmentPaths = attachmentsBefore
                .filterIndexed { index, _ -> index != indexToDelete }
                .map { Pair(it.filePath, it.thumbnailPath) }
            
            // Delete the selected attachment
            deleteAttachmentUseCase(attachmentToDelete)
            
            // Verify the deleted attachment is gone
            assertFalse("Deleted full image should not exist", File(pathsToDelete.first).exists())
            assertFalse("Deleted thumbnail should not exist", File(pathsToDelete.second).exists())
            
            // Verify other attachments still exist in database
            val attachmentsAfter = attachmentDao.getAttachmentsForReminder(reminderId).first()
            assertEquals("Should have one less attachment", attachmentCount - 1, attachmentsAfter.size)
            
            // Verify other attachments' files still exist on disk
            otherAttachmentPaths.forEach { (fullPath, thumbPath) ->
                assertTrue("Other attachment's full image should still exist", File(fullPath).exists())
                assertTrue("Other attachment's thumbnail should still exist", File(thumbPath).exists())
            }
            
            // Clean up
            attachmentsAfter.forEach { attachment ->
                File(attachment.filePath).delete()
                File(attachment.thumbnailPath).delete()
            }
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    /**
     * Property 11 (Consistency): Attachment paths follow naming convention
     * 
     * For any attachment, the thumbnail path should be related to the full image path.
     */
    @Test
    fun `Property 11 - Thumbnail path follows naming convention`() = runBlocking {
        // Run 100 iterations
        repeat(100) {
            // Create a random reminder
            val reminderEntity = generateRandomReminder()
            val reminderId = reminderDao.insertReminder(reminderEntity).toInt()
            
            // Create and add attachment
            val testImageUri = createTestImageFile()
            addAttachmentUseCase(testImageUri, reminderId)
            
            // Retrieve the attachment
            val attachments = attachmentDao.getAttachmentsForReminder(reminderId).first()
            val attachment = Attachment.fromEntity(attachments[0])
            
            // Extract filenames
            val fullImageFile = File(attachment.filePath)
            val thumbnailFile = File(attachment.thumbnailPath)
            
            val fullImageName = fullImageFile.name
            val thumbnailName = thumbnailFile.name
            
            // Verify thumbnail name starts with "thumb_"
            assertTrue("Thumbnail filename should start with 'thumb_'", 
                thumbnailName.startsWith("thumb_"))
            
            // Verify both are in appropriate directories
            assertTrue("Full image should be in attachments directory", 
                attachment.filePath.contains("attachments"))
            assertTrue("Thumbnail should be in thumbnails directory", 
                attachment.thumbnailPath.contains("thumbnails"))
            
            // Clean up
            fullImageFile.delete()
            thumbnailFile.delete()
            reminderDao.deleteReminder(reminderEntity.copy(id = reminderId))
        }
    }
    
    // Helper functions
    
    private fun generateRandomReminder(): ReminderEntity {
        val currentTime = System.currentTimeMillis()
        val hasNagMode = Random.nextBoolean()
        return ReminderEntity(
            id = 0,
            title = generateRandomString(5, 50),
            priority = PriorityLevel.values().random(),
            startTimeInMillis = currentTime + Random.nextLong(0, 86400000),
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
            dateInMillis = if (Random.nextBoolean()) currentTime + Random.nextLong(0, 86400000) else null,
            daysOfWeek = List(Random.nextInt(0, 7)) { Random.nextInt(1, 8) },
            originalStartTimeInMillis = currentTime
        )
    }
    
    private fun generateRandomString(minLength: Int, maxLength: Int): String {
        val length = Random.nextInt(minLength, maxLength + 1)
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + ' '
        return (1..length).map { chars.random() }.joinToString("")
    }
    
    /**
     * Creates a test image file and returns its URI.
     * The image is a simple colored bitmap saved to a temporary file.
     */
    private fun createTestImageFile(): Uri {
        // Create a simple test bitmap
        val bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.rgb(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256)))
        
        // Save to temporary file
        val tempFile = File.createTempFile("test_image_", ".jpg", context.cacheDir)
        FileOutputStream(tempFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        bitmap.recycle()
        
        return Uri.fromFile(tempFile)
    }
    
    /**
     * Cleans up any test files left in the attachments and thumbnails directories.
     */
    private fun cleanupTestFiles() {
        val attachmentsDir = File(context.filesDir, "attachments")
        val thumbnailsDir = File(context.filesDir, "thumbnails")
        
        attachmentsDir.listFiles()?.forEach { it.delete() }
        thumbnailsDir.listFiles()?.forEach { it.delete() }
        
        // Clean up temp files
        context.cacheDir.listFiles()?.filter { it.name.startsWith("test_image_") }?.forEach { it.delete() }
    }
}
