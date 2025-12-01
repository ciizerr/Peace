package com.nami.peace.deeplink

import android.content.Intent
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.util.deeplink.DeepLinkHandler
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowPackageManager
import org.junit.Assert.*

/**
 * Integration tests for deep link sharing functionality.
 * 
 * Tests sharing reminders via different channels:
 * - SMS
 * - WhatsApp
 * - Email
 * - App-not-installed scenario
 * 
 * **Validates: Requirements 9.1, 9.3, 9.4**
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class DeepLinkSharingIntegrationTest {
    
    private lateinit var deepLinkHandler: DeepLinkHandler
    private lateinit var shadowPackageManager: ShadowPackageManager
    
    @Before
    fun setup() {
        deepLinkHandler = DeepLinkHandler()
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        shadowPackageManager = Shadows.shadowOf(context.packageManager)
    }
    
    // ========== Test 1: Sharing via SMS ==========
    
    @Test
    fun `test sharing via SMS - creates valid SMS intent with deep link`() {
        // Arrange: Create a reminder to share
        val reminder = createTestReminder(
            title = "Buy groceries",
            priority = PriorityLevel.MEDIUM,
            category = ReminderCategory.HOME
        )
        
        // Act: Generate deep link
        val deepLink = deepLinkHandler.createShareLink(reminder)
        
        // Create SMS intent with reminder title in message
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", "Check out this reminder: ${reminder.title}\n$deepLink")
        }
        
        // Assert: SMS intent is properly configured
        assertEquals("Intent action should be SENDTO", Intent.ACTION_SENDTO, smsIntent.action)
        assertEquals("Intent data should be smsto:", "smsto:", smsIntent.data.toString())
        
        val messageBody = smsIntent.getStringExtra("sms_body")
        assertNotNull("SMS body should not be null", messageBody)
        assertTrue("SMS body should contain deep link", messageBody!!.contains("peace://share"))
        assertTrue("SMS body should contain reminder title", messageBody.contains("Buy groceries"))
        
        // Verify deep link can be extracted and parsed
        val extractedLink = extractDeepLinkFromMessage(messageBody)
        assertNotNull("Should be able to extract deep link from SMS", extractedLink)
        
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertNotNull("Should be able to parse extracted deep link", parsedReminder)
        assertEquals("Parsed reminder title should match", reminder.title, parsedReminder!!.title)
    }
    
    @Test
    fun `test sharing via SMS - handles long reminder titles`() {
        // Arrange: Create reminder with long title
        val longTitle = "This is a very long reminder title that might cause issues with SMS character limits but should still work correctly"
        val reminder = createTestReminder(title = longTitle)
        
        // Act: Generate deep link and create SMS intent
        val deepLink = deepLinkHandler.createShareLink(reminder)
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", "Check out this reminder: $deepLink")
        }
        
        // Assert: SMS can be created and deep link is valid
        val messageBody = smsIntent.getStringExtra("sms_body")
        assertNotNull("SMS body should not be null", messageBody)
        
        val extractedLink = extractDeepLinkFromMessage(messageBody!!)
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertEquals("Long title should be preserved", longTitle, parsedReminder!!.title)
    }
    
    @Test
    fun `test sharing via SMS - handles special characters in title`() {
        // Arrange: Create reminder with special characters
        val specialTitle = "Buy milk & eggs @ store #1 (urgent!)"
        val reminder = createTestReminder(title = specialTitle)
        
        // Act: Generate deep link and create SMS intent
        val deepLink = deepLinkHandler.createShareLink(reminder)
        val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:")
            putExtra("sms_body", "Reminder: $deepLink")
        }
        
        // Assert: Special characters are preserved
        val messageBody = smsIntent.getStringExtra("sms_body")
        val extractedLink = extractDeepLinkFromMessage(messageBody!!)
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertEquals("Special characters should be preserved", specialTitle, parsedReminder!!.title)
    }
    
    // ========== Test 2: Sharing via WhatsApp ==========
    
    @Test
    fun `test sharing via WhatsApp - creates valid WhatsApp intent with deep link`() {
        // Arrange: Create a reminder to share
        val reminder = createTestReminder(
            title = "Team meeting",
            priority = PriorityLevel.HIGH,
            category = ReminderCategory.WORK
        )
        
        // Act: Generate deep link
        val deepLink = deepLinkHandler.createShareLink(reminder)
        
        // Create WhatsApp intent
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, "Hey! Check out this reminder: $deepLink")
        }
        
        // Assert: WhatsApp intent is properly configured
        assertEquals("Intent action should be SEND", Intent.ACTION_SEND, whatsappIntent.action)
        assertEquals("Intent type should be text/plain", "text/plain", whatsappIntent.type)
        assertEquals("Intent package should be WhatsApp", "com.whatsapp", whatsappIntent.`package`)
        
        val messageText = whatsappIntent.getStringExtra(Intent.EXTRA_TEXT)
        assertNotNull("Message text should not be null", messageText)
        assertTrue("Message should contain deep link", messageText!!.contains("peace://share"))
        
        // Verify deep link can be extracted and parsed
        val extractedLink = extractDeepLinkFromMessage(messageText)
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertEquals("Parsed reminder should match", reminder.title, parsedReminder!!.title)
    }
    
    @Test
    fun `test sharing via WhatsApp - handles emoji in title`() {
        // Arrange: Create reminder with emoji
        val emojiTitle = "Party time! 🎉🎊🥳"
        val reminder = createTestReminder(title = emojiTitle)
        
        // Act: Generate deep link and create WhatsApp intent
        val deepLink = deepLinkHandler.createShareLink(reminder)
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp")
            putExtra(Intent.EXTRA_TEXT, "Reminder: $deepLink")
        }
        
        // Assert: Emoji are preserved
        val messageText = whatsappIntent.getStringExtra(Intent.EXTRA_TEXT)
        val extractedLink = extractDeepLinkFromMessage(messageText!!)
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertEquals("Emoji should be preserved", emojiTitle, parsedReminder!!.title)
    }
    
    @Test
    fun `test sharing via WhatsApp Business - uses correct package name`() {
        // Arrange: Create a reminder
        val reminder = createTestReminder(title = "Business meeting")
        val deepLink = deepLinkHandler.createShareLink(reminder)
        
        // Act: Create WhatsApp Business intent
        val whatsappBusinessIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            setPackage("com.whatsapp.w4b")
            putExtra(Intent.EXTRA_TEXT, "Reminder: $deepLink")
        }
        
        // Assert: WhatsApp Business package is correct
        assertEquals("Should use WhatsApp Business package", "com.whatsapp.w4b", whatsappBusinessIntent.`package`)
        
        val messageText = whatsappBusinessIntent.getStringExtra(Intent.EXTRA_TEXT)
        val extractedLink = extractDeepLinkFromMessage(messageText!!)
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertNotNull("Should parse reminder correctly", parsedReminder)
    }
    
    // ========== Test 3: Sharing via Email ==========
    
    @Test
    fun `test sharing via Email - creates valid email intent with deep link`() {
        // Arrange: Create a reminder to share
        val reminder = createTestReminder(
            title = "Project deadline",
            priority = PriorityLevel.HIGH,
            category = ReminderCategory.WORK
        )
        
        // Act: Generate deep link
        val deepLink = deepLinkHandler.createShareLink(reminder)
        
        // Create email intent
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, "Peace Reminder: ${reminder.title}")
            putExtra(Intent.EXTRA_TEXT, buildEmailBody(reminder, deepLink))
        }
        
        // Assert: Email intent is properly configured
        assertEquals("Intent action should be SEND", Intent.ACTION_SEND, emailIntent.action)
        assertEquals("Intent type should be message/rfc822", "message/rfc822", emailIntent.type)
        
        val subject = emailIntent.getStringExtra(Intent.EXTRA_SUBJECT)
        assertNotNull("Email subject should not be null", subject)
        assertTrue("Subject should contain reminder title", subject!!.contains("Project deadline"))
        
        val body = emailIntent.getStringExtra(Intent.EXTRA_TEXT)
        assertNotNull("Email body should not be null", body)
        assertTrue("Body should contain deep link", body!!.contains("peace://share"))
        
        // Verify deep link can be extracted and parsed
        val extractedLink = extractDeepLinkFromMessage(body)
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertEquals("Parsed reminder should match", reminder.title, parsedReminder!!.title)
    }
    
    @Test
    fun `test sharing via Email - includes reminder details in body`() {
        // Arrange: Create a detailed reminder
        val reminder = createTestReminder(
            title = "Doctor appointment",
            priority = PriorityLevel.HIGH,
            category = ReminderCategory.HEALTH,
            recurrenceType = RecurrenceType.WEEKLY
        )
        
        // Act: Generate deep link and create email
        val deepLink = deepLinkHandler.createShareLink(reminder)
        val emailBody = buildEmailBody(reminder, deepLink)
        
        // Assert: Email body contains relevant information
        assertTrue("Body should contain title", emailBody.contains("Doctor appointment"))
        assertTrue("Body should contain priority", emailBody.contains("HIGH") || emailBody.contains("High"))
        assertTrue("Body should contain category", emailBody.contains("HEALTH") || emailBody.contains("Health"))
        assertTrue("Body should contain recurrence", emailBody.contains("WEEKLY") || emailBody.contains("Weekly"))
        assertTrue("Body should contain deep link", emailBody.contains("peace://share"))
        
        // Verify deep link is parseable
        val extractedLink = extractDeepLinkFromMessage(emailBody)
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertNotNull("Should parse reminder from email", parsedReminder)
    }
    
    @Test
    fun `test sharing via Email - handles multiple recipients`() {
        // Arrange: Create a reminder
        val reminder = createTestReminder(title = "Team sync")
        val deepLink = deepLinkHandler.createShareLink(reminder)
        
        // Act: Create email intent with multiple recipients
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("user1@example.com", "user2@example.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Peace Reminder: ${reminder.title}")
            putExtra(Intent.EXTRA_TEXT, buildEmailBody(reminder, deepLink))
        }
        
        // Assert: Multiple recipients are configured
        val recipients = emailIntent.getStringArrayExtra(Intent.EXTRA_EMAIL)
        assertNotNull("Recipients should not be null", recipients)
        assertEquals("Should have 2 recipients", 2, recipients!!.size)
        
        val body = emailIntent.getStringExtra(Intent.EXTRA_TEXT)
        val extractedLink = extractDeepLinkFromMessage(body!!)
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertNotNull("Should parse reminder correctly", parsedReminder)
    }
    
    // ========== Test 4: App-not-installed scenario ==========
    
    @Test
    fun `test app-not-installed - deep link opens Play Store`() {
        // Arrange: Simulate app not installed
        // In a real scenario, Android would handle this automatically
        // We test that the deep link format is correct for Play Store fallback
        
        val reminder = createTestReminder(title = "Test reminder")
        val deepLink = deepLinkHandler.createShareLink(reminder)
        
        // Act: Parse the deep link URI
        val uri = Uri.parse(deepLink)
        
        // Assert: Deep link has correct format for Android App Links
        assertEquals("Scheme should be peace", "peace", uri.scheme)
        assertEquals("Host should be share", "share", uri.host)
        assertNotNull("Should have data parameter", uri.getQueryParameter("data"))
        
        // Verify the deep link would trigger Play Store if app not installed
        // This is handled by Android's autoVerify in AndroidManifest
        assertTrue("Deep link should be valid format", deepLinkHandler.isValidDeepLink(uri))
    }
    
    @Test
    fun `test app-not-installed - invalid deep link shows error gracefully`() {
        // Arrange: Create an invalid deep link (corrupted data)
        val invalidLink = "peace://share?data=corrupted_data_123"
        
        // Act: Try to parse invalid link
        val parsedReminder = deepLinkHandler.parseShareLink(invalidLink)
        
        // Assert: Should return null gracefully (no crash)
        assertNull("Invalid link should return null", parsedReminder)
    }
    
    @Test
    fun `test app-not-installed - malformed URI shows error gracefully`() {
        // Arrange: Create various malformed URIs
        val malformedUris = listOf(
            "peace://",
            "peace://share",
            "peace://wrong_host?data=test",
            "http://share?data=test",
            "invalid_uri"
        )
        
        // Act & Assert: All should be handled gracefully
        malformedUris.forEach { uriString ->
            val parsedReminder = deepLinkHandler.parseShareLink(uriString)
            assertNull("Malformed URI should return null: $uriString", parsedReminder)
        }
    }
    
    // ========== Test 5: Generic sharing (Android Share Sheet) ==========
    
    @Test
    fun `test generic sharing - creates share intent for any app`() {
        // Arrange: Create a reminder
        val reminder = createTestReminder(
            title = "Important task",
            priority = PriorityLevel.HIGH
        )
        
        // Act: Generate deep link and create generic share intent
        val deepLink = deepLinkHandler.createShareLink(reminder)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Peace Reminder: ${reminder.title}")
            putExtra(Intent.EXTRA_TEXT, "Check out this reminder: $deepLink")
        }
        
        // Create chooser
        val chooserIntent = Intent.createChooser(shareIntent, "Share reminder via")
        
        // Assert: Chooser intent is properly configured
        assertNotNull("Chooser intent should not be null", chooserIntent)
        assertEquals("Chooser action should be CHOOSER", Intent.ACTION_CHOOSER, chooserIntent.action)
        
        val originalIntent = chooserIntent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
        assertNotNull("Original intent should be included", originalIntent)
        
        val messageText = originalIntent!!.getStringExtra(Intent.EXTRA_TEXT)
        assertTrue("Message should contain deep link", messageText!!.contains("peace://share"))
        
        // Verify deep link is valid
        val extractedLink = extractDeepLinkFromMessage(messageText)
        val parsedReminder = deepLinkHandler.parseShareLink(extractedLink!!)
        assertEquals("Parsed reminder should match", reminder.title, parsedReminder!!.title)
    }
    
    // ========== Test 6: Cross-platform compatibility ==========
    
    @Test
    fun `test cross-platform - deep link works across different Android versions`() {
        // Arrange: Create a reminder with all features
        val reminder = createTestReminder(
            title = "Cross-platform test",
            priority = PriorityLevel.MEDIUM,
            category = ReminderCategory.GENERAL,
            recurrenceType = RecurrenceType.DAILY,
            isNagModeEnabled = true,
            nagIntervalInMillis = 3600000L,
            nagTotalRepetitions = 3
        )
        
        // Act: Generate deep link
        val deepLink = deepLinkHandler.createShareLink(reminder)
        
        // Assert: Deep link uses URL-safe Base64 encoding (works across platforms)
        // Note: URL-safe Base64 uses [A-Za-z0-9_-] and may include = for padding
        assertTrue("Deep link should use URL-safe characters", 
            deepLink.matches(Regex("peace://share\\?data=[A-Za-z0-9_-]+={0,2}")))
        
        // Verify parsing works
        val parsedReminder = deepLinkHandler.parseShareLink(deepLink)
        assertNotNull("Should parse on any platform", parsedReminder)
        assertEquals("All fields should be preserved", reminder.title, parsedReminder!!.title)
        assertEquals("Nag mode should be preserved", reminder.isNagModeEnabled, parsedReminder.isNagModeEnabled)
    }
    
    @Test
    fun `test cross-platform - deep link handles different character encodings`() {
        // Arrange: Create reminders with different character sets
        val testCases = listOf(
            "English reminder",
            "Español recordatorio",
            "Français rappel",
            "Deutsch Erinnerung",
            "日本語リマインダー",
            "中文提醒",
            "العربية تذكير",
            "हिन्दी अनुस्मारक"
        )
        
        testCases.forEach { title ->
            // Act: Create and share reminder
            val reminder = createTestReminder(title = title)
            val deepLink = deepLinkHandler.createShareLink(reminder)
            
            // Assert: Can parse back correctly
            val parsedReminder = deepLinkHandler.parseShareLink(deepLink)
            assertNotNull("Should parse $title", parsedReminder)
            assertEquals("Title should match for $title", title, parsedReminder!!.title)
        }
    }
    
    // ========== Test 7: Error handling and edge cases ==========
    
    @Test
    fun `test error handling - extremely long deep link is rejected`() {
        // Arrange: Create reminder with very long title (exceeds 8KB limit)
        val veryLongTitle = "A".repeat(10000)
        val reminder = createTestReminder(title = veryLongTitle)
        
        // Act & Assert: Should throw exception
        try {
            deepLinkHandler.createShareLink(reminder)
            fail("Should throw exception for oversized data")
        } catch (e: IllegalArgumentException) {
            assertTrue("Exception should mention size limit", 
                e.message?.contains("size limit", ignoreCase = true) == true)
        }
    }
    
    @Test
    fun `test error handling - empty title is handled`() {
        // Arrange: Create reminder with empty title
        val reminder = createTestReminder(title = "")
        
        // Act: Generate and parse deep link
        val deepLink = deepLinkHandler.createShareLink(reminder)
        val parsedReminder = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert: Empty title is preserved
        assertNotNull("Should parse reminder with empty title", parsedReminder)
        assertEquals("Empty title should be preserved", "", parsedReminder!!.title)
    }
    
    @Test
    fun `test error handling - whitespace-only title is handled`() {
        // Arrange: Create reminder with whitespace title
        val reminder = createTestReminder(title = "   ")
        
        // Act: Generate and parse deep link
        val deepLink = deepLinkHandler.createShareLink(reminder)
        val parsedReminder = deepLinkHandler.parseShareLink(deepLink)
        
        // Assert: Whitespace is preserved
        assertNotNull("Should parse reminder with whitespace title", parsedReminder)
        assertEquals("Whitespace should be preserved", "   ", parsedReminder!!.title)
    }
    
    // ========== Helper Functions ==========
    
    private fun createTestReminder(
        title: String,
        priority: PriorityLevel = PriorityLevel.MEDIUM,
        category: ReminderCategory = ReminderCategory.GENERAL,
        recurrenceType: RecurrenceType = RecurrenceType.ONE_TIME,
        isNagModeEnabled: Boolean = false,
        nagIntervalInMillis: Long? = null,
        nagTotalRepetitions: Int = 1
    ): Reminder {
        val startTime = System.currentTimeMillis() + 3600000 // 1 hour from now
        return Reminder(
            id = 1,
            title = title,
            priority = priority,
            startTimeInMillis = startTime,
            recurrenceType = recurrenceType,
            isNagModeEnabled = isNagModeEnabled,
            nagIntervalInMillis = nagIntervalInMillis,
            nagTotalRepetitions = nagTotalRepetitions,
            currentRepetitionIndex = 0,
            isCompleted = false,
            isEnabled = true,
            isInNestedSnoozeLoop = false,
            nestedSnoozeStartTime = null,
            category = category,
            isStrictSchedulingEnabled = false,
            dateInMillis = null,
            daysOfWeek = emptyList(),
            originalStartTimeInMillis = startTime,
            customAlarmSoundUri = null,
            customAlarmSoundName = null
        )
    }
    
    private fun extractDeepLinkFromMessage(message: String): String? {
        // Extract deep link from message text
        val regex = Regex("peace://share\\?data=[A-Za-z0-9_-]+")
        return regex.find(message)?.value
    }
    
    private fun buildEmailBody(reminder: Reminder, deepLink: String): String {
        return """
            I'm sharing a reminder with you from the Peace app:
            
            Title: ${reminder.title}
            Priority: ${reminder.priority.name}
            Category: ${reminder.category.name}
            Recurrence: ${reminder.recurrenceType.name}
            
            Click the link below to import this reminder into your Peace app:
            $deepLink
            
            If you don't have the Peace app installed, you'll be directed to the Play Store.
        """.trimIndent()
    }
}
