package com.nami.peace.util.deeplink

import android.net.Uri
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.junit.Assert.*

/**
 * Property-based tests for deep link functionality.
 * 
 * **Feature: peace-app-enhancement, Property 20: Deep link round-trip**
 * **Feature: peace-app-enhancement, Property 21: Deep link import**
 * **Validates: Requirements 9.2, 9.3, 9.7**
 * 
 * Property 20: For any reminder, encoding it to a deep link and then decoding should produce 
 * an equivalent reminder with all fields preserved.
 * 
 * Property 21: For any valid deep link opened in the app, a new reminder should be created 
 * in the local database with all data from the link.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DeepLinkPropertyTest {
    
    private lateinit var deepLinkHandler: DeepLinkHandler
    
    @Before
    fun setup() {
        deepLinkHandler = DeepLinkHandler()
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - basic reminder preserves all fields`() {
        // Arrange: Create a basic reminder
        val original = createReminder(
            id = 42,
            title = "Test Reminder",
            priority = PriorityLevel.MEDIUM,
            category = ReminderCategory.GENERAL
        )
        
        // Act: Encode to deep link and decode back
        val deepLink = deepLinkHandler.createShareLink(original)
        val decoded = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert: Decoded reminder should match original (except id and runtime state)
        assertNotNull("Decoded reminder should not be null", decoded)
        assertReminderFieldsMatch(original, decoded!!)
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - all priority levels`() {
        // Test all priority levels
        PriorityLevel.values().forEach { priority ->
            // Arrange
            val original = createReminder(
                id = 1,
                title = "Priority Test",
                priority = priority
            )
            
            // Act
            val deepLink = deepLinkHandler.createShareLink(original)
            val decoded = deepLinkHandler.parseShareLink(deepLink)
            
            // Assert
            assertNotNull("Decoded reminder should not be null for priority $priority", decoded)
            assertEquals("Priority should match", priority, decoded!!.priority)
        }
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - all recurrence types`() {
        // Test all recurrence types
        RecurrenceType.values().forEach { recurrenceType ->
            // Arrange
            val original = createReminder(
                id = 1,
                title = "Recurrence Test",
                recurrenceType = recurrenceType
            )
            
            // Act
            val deepLink = deepLinkHandler.createShareLink(original)
            val decoded = deepLinkHandler.parseShareLink(deepLink)
            
            // Assert
            assertNotNull("Decoded reminder should not be null for recurrence $recurrenceType", decoded)
            assertEquals("Recurrence type should match", recurrenceType, decoded!!.recurrenceType)
        }
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - all categories`() {
        // Test all categories
        ReminderCategory.values().forEach { category ->
            // Arrange
            val original = createReminder(
                id = 1,
                title = "Category Test",
                category = category
            )
            
            // Act
            val deepLink = deepLinkHandler.createShareLink(original)
            val decoded = deepLinkHandler.parseShareLink(deepLink)
            
            // Assert
            assertNotNull("Decoded reminder should not be null for category $category", decoded)
            assertEquals("Category should match", category, decoded!!.category)
        }
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - nag mode enabled with all fields`() {
        // Arrange: Create reminder with nag mode
        val original = createReminder(
            id = 1,
            title = "Nag Mode Test",
            isNagModeEnabled = true,
            nagIntervalInMillis = 3600000L, // 1 hour
            nagTotalRepetitions = 5,
            isStrictSchedulingEnabled = true
        )
        
        // Act
        val deepLink = deepLinkHandler.createShareLink(original)
        val decoded = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert
        assertNotNull("Decoded reminder should not be null", decoded)
        assertEquals("Nag mode should be enabled", true, decoded!!.isNagModeEnabled)
        assertEquals("Nag interval should match", 3600000L, decoded.nagIntervalInMillis)
        assertEquals("Nag repetitions should match", 5, decoded.nagTotalRepetitions)
        assertEquals("Strict scheduling should match", true, decoded.isStrictSchedulingEnabled)
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - nag mode disabled`() {
        // Arrange: Create reminder without nag mode
        val original = createReminder(
            id = 1,
            title = "No Nag Mode",
            isNagModeEnabled = false,
            nagIntervalInMillis = null,
            nagTotalRepetitions = 1
        )
        
        // Act
        val deepLink = deepLinkHandler.createShareLink(original)
        val decoded = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert
        assertNotNull("Decoded reminder should not be null", decoded)
        assertEquals("Nag mode should be disabled", false, decoded!!.isNagModeEnabled)
        assertEquals("Nag interval should be null", null, decoded.nagIntervalInMillis)
        assertEquals("Nag repetitions should be 1", 1, decoded.nagTotalRepetitions)
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - custom alarm sounds`() {
        // Arrange: Create reminder with custom alarm sound
        val original = createReminder(
            id = 1,
            title = "Custom Sound Test",
            customAlarmSoundUri = "content://media/external/audio/media/123",
            customAlarmSoundName = "My Alarm.mp3"
        )
        
        // Act
        val deepLink = deepLinkHandler.createShareLink(original)
        val decoded = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert
        assertNotNull("Decoded reminder should not be null", decoded)
        assertEquals("Custom sound URI should match", original.customAlarmSoundUri, decoded!!.customAlarmSoundUri)
        assertEquals("Custom sound name should match", original.customAlarmSoundName, decoded.customAlarmSoundName)
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - weekly recurrence with days of week`() {
        // Arrange: Create weekly reminder with specific days
        val original = createReminder(
            id = 1,
            title = "Weekly Test",
            recurrenceType = RecurrenceType.WEEKLY,
            daysOfWeek = listOf(2, 4, 6), // Mon, Wed, Fri
            dateInMillis = System.currentTimeMillis()
        )
        
        // Act
        val deepLink = deepLinkHandler.createShareLink(original)
        val decoded = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert
        assertNotNull("Decoded reminder should not be null", decoded)
        assertEquals("Days of week should match", original.daysOfWeek, decoded!!.daysOfWeek)
        assertEquals("Date should match", original.dateInMillis, decoded.dateInMillis)
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - special characters in title`() {
        // Test various special characters
        val specialTitles = listOf(
            "Test & Reminder",
            "Test @ 3pm",
            "Test #1",
            "Test $100",
            "Test 50%",
            "Test (important)",
            "Test [urgent]",
            "Test {critical}",
            "Test | Reminder",
            "Test / Reminder",
            "Test \\ Reminder",
            "Test: Reminder",
            "Test; Reminder",
            "Test, Reminder",
            "Test. Reminder",
            "Test? Reminder",
            "Test! Reminder",
            "Test + Reminder",
            "Test = Reminder",
            "Test ~ Reminder",
            "Test ` Reminder",
            "Test ' Reminder",
            "Test \" Reminder"
        )
        
        specialTitles.forEach { title ->
            // Arrange
            val original = createReminder(id = 1, title = title)
            
            // Act
            val deepLink = deepLinkHandler.createShareLink(original)
            val decoded = deepLinkHandler.parseShareLink(deepLink)
            
            // Assert
            assertNotNull("Decoded reminder should not be null for title: $title", decoded)
            assertEquals("Title should match for: $title", title, decoded!!.title)
        }
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - unicode characters in title`() {
        // Test unicode characters
        val unicodeTitles = listOf(
            "Test 日本語",
            "Test 中文",
            "Test العربية",
            "Test हिन्दी",
            "Test Español",
            "Test Français",
            "Test Deutsch",
            "Test 한국어",
            "Test Emoji 😀🎉✨",
            "Test Math ∑∫∂√"
        )
        
        unicodeTitles.forEach { title ->
            // Arrange
            val original = createReminder(id = 1, title = title)
            
            // Act
            val deepLink = deepLinkHandler.createShareLink(original)
            val decoded = deepLinkHandler.parseShareLink(deepLink)
            
            // Assert
            assertNotNull("Decoded reminder should not be null for title: $title", decoded)
            assertEquals("Title should match for: $title", title, decoded!!.title)
        }
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - very long title`() {
        // Arrange: Create reminder with very long title (but within limits)
        val longTitle = "A".repeat(500)
        val original = createReminder(id = 1, title = longTitle)
        
        // Act
        val deepLink = deepLinkHandler.createShareLink(original)
        val decoded = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert
        assertNotNull("Decoded reminder should not be null", decoded)
        assertEquals("Long title should match", longTitle, decoded!!.title)
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - multiple random reminders`() {
        // Test 50 random reminder configurations
        repeat(50) { iteration ->
            // Arrange: Create random reminder
            val original = createRandomReminder(iteration)
            
            // Act
            val deepLink = deepLinkHandler.createShareLink(original)
            val decoded = deepLinkHandler.parseShareLink(deepLink)
            
            // Assert
            assertNotNull("Decoded reminder should not be null for iteration $iteration", decoded)
            assertReminderFieldsMatch(original, decoded!!, "Iteration $iteration")
        }
    }
    
    @Test
    fun `Property 20 - Deep link round-trip - runtime state fields are reset`() {
        // Arrange: Create reminder with runtime state
        val original = createReminder(
            id = 42,
            title = "Runtime State Test",
            currentRepetitionIndex = 3,
            isCompleted = true,
            isEnabled = false,
            isInNestedSnoozeLoop = true,
            nestedSnoozeStartTime = System.currentTimeMillis()
        )
        
        // Act
        val deepLink = deepLinkHandler.createShareLink(original)
        val decoded = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert: Runtime state should be reset
        assertNotNull("Decoded reminder should not be null", decoded)
        assertEquals("ID should be reset to 0", 0, decoded!!.id)
        assertEquals("Current repetition should be reset to 0", 0, decoded.currentRepetitionIndex)
        assertEquals("Should not be completed", false, decoded.isCompleted)
        assertEquals("Should be enabled", true, decoded.isEnabled)
        assertEquals("Should not be in snooze loop", false, decoded.isInNestedSnoozeLoop)
        assertEquals("Snooze start time should be null", null, decoded.nestedSnoozeStartTime)
    }
    
    @Test
    fun `Property 21 - Deep link import - valid URI creates new reminder`() {
        // Arrange: Create a valid deep link
        val original = createReminder(id = 1, title = "Import Test")
        val deepLink = deepLinkHandler.createShareLink(original)
        
        // Act: Parse the deep link
        val imported = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert: Should create a new reminder with id = 0
        assertNotNull("Imported reminder should not be null", imported)
        assertEquals("Imported reminder should have id = 0", 0, imported!!.id)
        assertEquals("Title should match", original.title, imported.title)
    }
    
    @Test
    fun `Property 21 - Deep link import - invalid scheme returns null`() {
        // Arrange: Create URI with wrong scheme
        val invalidUri = "http://share?data=test"
        
        // Act
        val imported = deepLinkHandler.parseShareLink(invalidUri)
        
        // Assert
        assertNull("Invalid scheme should return null", imported)
    }
    
    @Test
    fun `Property 21 - Deep link import - invalid host returns null`() {
        // Arrange: Create URI with wrong host
        val invalidUri = "peace://invalid?data=test"
        
        // Act
        val imported = deepLinkHandler.parseShareLink(invalidUri)
        
        // Assert
        assertNull("Invalid host should return null", imported)
    }
    
    @Test
    fun `Property 21 - Deep link import - missing data parameter returns null`() {
        // Arrange: Create URI without data parameter
        val invalidUri = "peace://share"
        
        // Act
        val imported = deepLinkHandler.parseShareLink(invalidUri)
        
        // Assert
        assertNull("Missing data parameter should return null", imported)
    }
    
    @Test
    fun `Property 21 - Deep link import - corrupted data returns null`() {
        // Arrange: Create URI with corrupted base64 data
        val invalidUri = "peace://share?data=!!!invalid_base64!!!"
        
        // Act
        val imported = deepLinkHandler.parseShareLink(invalidUri)
        
        // Assert
        assertNull("Corrupted data should return null", imported)
    }
    
    @Test
    fun `Property 21 - Deep link import - malformed JSON returns null`() {
        // Arrange: Create URI with valid base64 but invalid JSON
        val invalidData = android.util.Base64.encodeToString(
            "{invalid json}".toByteArray(),
            android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP
        )
        val invalidUri = "peace://share?data=$invalidData"
        
        // Act
        val imported = deepLinkHandler.parseShareLink(invalidUri)
        
        // Assert
        assertNull("Malformed JSON should return null", imported)
    }
    
    @Test
    fun `Property 21 - Deep link import - URI validation works correctly`() {
        // Test valid URI
        val validReminder = createReminder(id = 1, title = "Valid")
        val validLink = deepLinkHandler.createShareLink(validReminder)
        val validUri = Uri.parse(validLink)
        assertTrue("Valid URI should pass validation", deepLinkHandler.isValidDeepLink(validUri))
        
        // Test invalid URIs
        val invalidUris = listOf(
            Uri.parse("http://share?data=test"),
            Uri.parse("peace://invalid?data=test"),
            Uri.parse("peace://share"),
            Uri.parse("peace://share?other=param")
        )
        
        invalidUris.forEach { uri ->
            assertFalse("Invalid URI should fail validation: $uri", deepLinkHandler.isValidDeepLink(uri))
        }
    }
    
    @Test
    fun `Property 21 - Deep link import - data size limit is enforced`() {
        // Arrange: Create reminder with extremely long title (should exceed 8KB limit)
        val veryLongTitle = "A".repeat(10000)
        val original = createReminder(id = 1, title = veryLongTitle)
        
        // Act & Assert: Should throw exception
        try {
            deepLinkHandler.createShareLink(original)
            fail("Should throw exception for data exceeding size limit")
        } catch (e: IllegalArgumentException) {
            assertTrue(
                "Exception message should mention size limit",
                e.message?.contains("size limit", ignoreCase = true) == true
            )
        }
    }
    
    @Test
    fun `Property 20 and 21 - Complete workflow - share and import`() {
        // Arrange: Create a complete reminder
        val original = createReminder(
            id = 123,
            title = "Complete Workflow Test",
            priority = PriorityLevel.HIGH,
            category = ReminderCategory.WORK,
            recurrenceType = RecurrenceType.DAILY,
            isNagModeEnabled = true,
            nagIntervalInMillis = 7200000L,
            nagTotalRepetitions = 3,
            isStrictSchedulingEnabled = true,
            customAlarmSoundUri = "content://media/external/audio/media/456",
            customAlarmSoundName = "Work Alarm.mp3"
        )
        
        // Act: Share (encode)
        val deepLink = deepLinkHandler.createShareLink(original)
        assertNotNull("Deep link should not be null", deepLink)
        assertTrue("Deep link should start with peace://", deepLink.startsWith("peace://"))
        
        // Act: Import (decode)
        val imported = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert: Imported reminder should match original
        assertNotNull("Imported reminder should not be null", imported)
        assertReminderFieldsMatch(original, imported!!)
        
        // Assert: Imported reminder is ready for database insertion
        assertEquals("Imported reminder should have id = 0 for new insertion", 0, imported.id)
        assertEquals("Imported reminder should not be completed", false, imported.isCompleted)
        assertEquals("Imported reminder should be enabled", true, imported.isEnabled)
        assertEquals("Imported reminder should not be in snooze loop", false, imported.isInNestedSnoozeLoop)
    }
    
    // Helper function to create test reminders
    private fun createReminder(
        id: Int,
        title: String,
        priority: PriorityLevel = PriorityLevel.MEDIUM,
        category: ReminderCategory = ReminderCategory.GENERAL,
        recurrenceType: RecurrenceType = RecurrenceType.ONE_TIME,
        isNagModeEnabled: Boolean = false,
        nagIntervalInMillis: Long? = null,
        nagTotalRepetitions: Int = 1,
        currentRepetitionIndex: Int = 0,
        isCompleted: Boolean = false,
        isEnabled: Boolean = true,
        isInNestedSnoozeLoop: Boolean = false,
        nestedSnoozeStartTime: Long? = null,
        isStrictSchedulingEnabled: Boolean = false,
        dateInMillis: Long? = null,
        daysOfWeek: List<Int> = emptyList(),
        customAlarmSoundUri: String? = null,
        customAlarmSoundName: String? = null
    ): Reminder {
        val startTime = System.currentTimeMillis() + 3600000 // 1 hour from now
        return Reminder(
            id = id,
            title = title,
            priority = priority,
            startTimeInMillis = startTime,
            recurrenceType = recurrenceType,
            isNagModeEnabled = isNagModeEnabled,
            nagIntervalInMillis = nagIntervalInMillis,
            nagTotalRepetitions = nagTotalRepetitions,
            currentRepetitionIndex = currentRepetitionIndex,
            isCompleted = isCompleted,
            isEnabled = isEnabled,
            isInNestedSnoozeLoop = isInNestedSnoozeLoop,
            nestedSnoozeStartTime = nestedSnoozeStartTime,
            category = category,
            isStrictSchedulingEnabled = isStrictSchedulingEnabled,
            dateInMillis = dateInMillis,
            daysOfWeek = daysOfWeek,
            originalStartTimeInMillis = startTime,
            customAlarmSoundUri = customAlarmSoundUri,
            customAlarmSoundName = customAlarmSoundName
        )
    }
    
    // Helper function to create random reminders for property testing
    private fun createRandomReminder(seed: Int): Reminder {
        val random = java.util.Random(seed.toLong())
        
        return createReminder(
            id = random.nextInt(1000),
            title = "Random Reminder $seed",
            priority = PriorityLevel.values()[random.nextInt(PriorityLevel.values().size)],
            category = ReminderCategory.values()[random.nextInt(ReminderCategory.values().size)],
            recurrenceType = RecurrenceType.values()[random.nextInt(RecurrenceType.values().size)],
            isNagModeEnabled = random.nextBoolean(),
            nagIntervalInMillis = if (random.nextBoolean()) random.nextLong().coerceIn(60000, 86400000) else null,
            nagTotalRepetitions = random.nextInt(10) + 1,
            isStrictSchedulingEnabled = random.nextBoolean(),
            dateInMillis = if (random.nextBoolean()) System.currentTimeMillis() + random.nextInt(86400000) else null,
            daysOfWeek = if (random.nextBoolean()) {
                (1..7).shuffled(random).take(random.nextInt(7) + 1).sorted()
            } else {
                emptyList()
            },
            customAlarmSoundUri = if (random.nextBoolean()) "content://media/external/audio/media/${random.nextInt(1000)}" else null,
            customAlarmSoundName = if (random.nextBoolean()) "Alarm${random.nextInt(100)}.mp3" else null
        )
    }
    
    // Helper function to assert reminder fields match (excluding id and runtime state)
    private fun assertReminderFieldsMatch(original: Reminder, decoded: Reminder, context: String = "") {
        val prefix = if (context.isNotEmpty()) "$context: " else ""
        
        assertEquals("${prefix}Title should match", original.title, decoded.title)
        assertEquals("${prefix}Priority should match", original.priority, decoded.priority)
        assertEquals("${prefix}Start time should match", original.startTimeInMillis, decoded.startTimeInMillis)
        assertEquals("${prefix}Recurrence type should match", original.recurrenceType, decoded.recurrenceType)
        assertEquals("${prefix}Nag mode enabled should match", original.isNagModeEnabled, decoded.isNagModeEnabled)
        assertEquals("${prefix}Nag interval should match", original.nagIntervalInMillis, decoded.nagIntervalInMillis)
        assertEquals("${prefix}Nag total repetitions should match", original.nagTotalRepetitions, decoded.nagTotalRepetitions)
        assertEquals("${prefix}Category should match", original.category, decoded.category)
        assertEquals("${prefix}Strict scheduling should match", original.isStrictSchedulingEnabled, decoded.isStrictSchedulingEnabled)
        assertEquals("${prefix}Date should match", original.dateInMillis, decoded.dateInMillis)
        assertEquals("${prefix}Days of week should match", original.daysOfWeek, decoded.daysOfWeek)
        assertEquals("${prefix}Original start time should match", original.startTimeInMillis, decoded.originalStartTimeInMillis)
        assertEquals("${prefix}Custom alarm sound URI should match", original.customAlarmSoundUri, decoded.customAlarmSoundUri)
        assertEquals("${prefix}Custom alarm sound name should match", original.customAlarmSoundName, decoded.customAlarmSoundName)
    }
}
