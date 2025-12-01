# Deep Link Format Specification 🔗

This document provides the technical specification for Peace's deep link sharing system, including format, encoding, security, and implementation details.

## Table of Contents

1. [Overview](#overview)
2. [Link Format](#link-format)
3. [Data Structure](#data-structure)
4. [Encoding & Decoding](#encoding--decoding)
5. [Security Considerations](#security-considerations)
6. [Implementation](#implementation)
7. [Error Handling](#error-handling)
8. [Testing](#testing)
9. [Examples](#examples)

---

## Overview

### Purpose

Peace's deep link system enables users to share reminders with other Peace users via any messaging platform (SMS, WhatsApp, email, etc.). Recipients can import shared reminders with a single tap.

### Key Features

- **Universal Sharing**: Works with any messaging app
- **One-Tap Import**: Recipients tap link to import
- **Independent Copies**: Imported reminders are independent (no live sync)
- **Secure Encoding**: Base64-encoded JSON data
- **Size Optimized**: Compact format for messaging limits
- **Fallback Handling**: Graceful degradation for invalid links

### Use Cases

- Sharing task lists with team members
- Coordinating family tasks
- Distributing event reminders
- Collaborative project planning
- Template sharing

---

## Link Format

### URI Scheme

Peace uses Android App Links with a custom URI scheme:

```
peace://share?data=<base64_encoded_json>
```

**Components**:
- **Scheme**: `peace://`
- **Host**: `share`
- **Query Parameter**: `data` (required)
- **Value**: Base64-encoded JSON string

### Example Link

```
peace://share?data=eyJ0aXRsZSI6IlRlYW0gTWVldGluZyIsInNjaGVkdWxlZEZvciI6MTcwMTM2MDAwMDAwMCwicHJpb3JpdHkiOiJISUdIIiwiY2F0ZWdvcnkiOiJXT1JLIn0=
```

### Link Characteristics

- **Maximum Length**: 8KB (8,192 characters)
- **URL-Safe**: Uses Base64 URL-safe encoding
- **No Padding**: Padding characters removed for compactness
- **Case-Sensitive**: Preserve case in encoding

---

## Data Structure

### JSON Schema

The encoded data follows this JSON structure:

```json
{
  "title": "string (required)",
  "scheduledFor": "long (required)",
  "priority": "string (required)",
  "category": "string (required)",
  "description": "string (optional)",
  "isRecurring": "boolean (optional)",
  "recurrencePattern": "string (optional)",
  "recurrenceInterval": "int (optional)",
  "recurrenceEndDate": "long (optional)",
  "isNagMode": "boolean (optional)",
  "nagModeRepetitions": "int (optional)",
  "nagModeInterval": "long (optional)",
  "nagModeIsFlexMode": "boolean (optional)",
  "subtasks": "array (optional)",
  "notes": "array (optional)"
}
```

### Field Specifications

#### Required Fields

**title** (String)
- Task title/description
- Maximum 200 characters
- Cannot be empty
- Example: `"Team Meeting"`

**scheduledFor** (Long)
- Unix timestamp in milliseconds
- Must be future date (validation optional)
- Example: `1701360000000` (Nov 30, 2023 12:00 PM UTC)

**priority** (String)
- Enum: `"HIGH"`, `"MEDIUM"`, `"LOW"`
- Case-sensitive
- Default: `"MEDIUM"` if invalid

**category** (String)
- Enum: `"WORK"`, `"STUDY"`, `"HEALTH"`, `"HOME"`, `"GENERAL"`
- Case-sensitive
- Default: `"GENERAL"` if invalid

#### Optional Fields

**description** (String)
- Additional task details
- Maximum 1000 characters
- Can be empty or null

**isRecurring** (Boolean)
- Whether task repeats
- Default: `false`

**recurrencePattern** (String)
- Enum: `"DAILY"`, `"WEEKLY"`, `"MONTHLY"`, `"YEARLY"`
- Required if `isRecurring` is `true`

**recurrenceInterval** (Int)
- Interval between recurrences
- Example: `2` for "every 2 weeks"
- Default: `1`

**recurrenceEndDate** (Long)
- Unix timestamp for recurrence end
- Optional (null = infinite)

**isNagMode** (Boolean)
- Whether nag mode is enabled
- Default: `false`

**nagModeRepetitions** (Int)
- Total repetitions in nag mode
- Required if `isNagMode` is `true`
- Range: 2-20

**nagModeInterval** (Long)
- Interval between repetitions (milliseconds)
- Required if `isNagMode` is `true`
- Example: `7200000` (2 hours)

**nagModeIsFlexMode** (Boolean)
- Flex mode vs strict mode
- Default: `false` (strict mode)

**subtasks** (Array)
- Array of subtask objects
- Maximum 50 subtasks
- Each subtask:
  ```json
  {
    "title": "string (required)",
    "order": "int (required)"
  }
  ```

**notes** (Array)
- Array of note strings
- Maximum 20 notes
- Each note: maximum 500 characters

### Excluded Fields

The following fields are NOT included in deep links:

- **Completion Status**: Always imported as incomplete
- **Custom Alarm Sounds**: Not shareable (file paths)
- **Image Attachments**: Not shareable (file data too large)
- **Internal IDs**: Generated on import
- **Timestamps**: Creation/update times set on import
- **User-Specific Data**: Settings, preferences

---

## Encoding & Decoding

### Encoding Process

```kotlin
fun encodeReminderToDeepLink(reminder: Reminder): String {
    // 1. Convert reminder to JSON
    val json = Json.encodeToString(reminder)
    
    // 2. Convert JSON to bytes
    val bytes = json.toByteArray(Charsets.UTF_8)
    
    // 3. Base64 encode (URL-safe, no padding)
    val encoded = Base64.encodeToString(
        bytes,
        Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
    )
    
    // 4. Construct URI
    return "peace://share?data=$encoded"
}
```

### Decoding Process

```kotlin
fun decodeDeepLinkToReminder(uri: Uri): Reminder? {
    try {
        // 1. Validate URI scheme and host
        if (uri.scheme != "peace" || uri.host != "share") {
            return null
        }
        
        // 2. Extract data parameter
        val encodedData = uri.getQueryParameter("data") ?: return null
        
        // 3. Base64 decode
        val bytes = Base64.decode(encodedData, Base64.URL_SAFE)
        
        // 4. Convert bytes to JSON string
        val json = String(bytes, Charsets.UTF_8)
        
        // 5. Parse JSON to Reminder object
        val reminder = Json.decodeFromString<Reminder>(json)
        
        // 6. Validate required fields
        if (!isValidReminder(reminder)) {
            return null
        }
        
        return reminder
    } catch (e: Exception) {
        Log.e("DeepLink", "Decoding failed", e)
        return null
    }
}
```

### Validation

```kotlin
fun isValidReminder(reminder: Reminder): Boolean {
    return reminder.title.isNotBlank() &&
           reminder.scheduledFor > 0 &&
           reminder.priority in listOf("HIGH", "MEDIUM", "LOW") &&
           reminder.category in listOf("WORK", "STUDY", "HEALTH", "HOME", "GENERAL") &&
           (!reminder.isNagMode || reminder.nagModeRepetitions in 2..20)
}
```

---

## Security Considerations

### Data Validation

**Input Sanitization**:
- Validate all fields before import
- Sanitize strings to prevent injection
- Clamp numeric values to valid ranges
- Reject malformed JSON

**Size Limits**:
- Maximum link size: 8KB
- Maximum title: 200 characters
- Maximum description: 1000 characters
- Maximum subtasks: 50
- Maximum notes: 20

### Privacy

**No Sensitive Data**:
- No user IDs or device IDs
- No authentication tokens
- No location data
- No personal information

**Public Links**:
- Assume links can be intercepted
- Don't include confidential information
- Links are not encrypted (Base64 is encoding, not encryption)

### Attack Vectors

**Malicious Links**:
- Validate JSON structure
- Reject oversized data
- Sanitize all text fields
- Limit array sizes

**Denial of Service**:
- Limit link processing time
- Reject extremely large payloads
- Rate-limit import operations

**Data Injection**:
- Use parameterized queries for database
- Escape special characters
- Validate enum values

---

## Implementation

### Android Manifest Configuration

```xml
<activity
    android:name=".ui.deeplink.DeepLinkActivity"
    android:exported="true"
    android:launchMode="singleTask">
    
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        
        <data
            android:scheme="peace"
            android:host="share" />
    </intent-filter>
</activity>
```

### DeepLinkActivity

```kotlin
class DeepLinkActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Handle deep link
        intent?.data?.let { uri ->
            handleDeepLink(uri)
        } ?: run {
            // No URI, close activity
            finish()
        }
    }
    
    private fun handleDeepLink(uri: Uri) {
        lifecycleScope.launch {
            try {
                // Decode reminder from URI
                val reminder = DeepLinkHandler.decodeDeepLink(uri)
                
                if (reminder != null) {
                    // Import reminder
                    val success = importReminderUseCase(reminder)
                    
                    if (success) {
                        // Show success and navigate to home
                        showToast("Reminder imported successfully")
                        navigateToHome()
                    } else {
                        showError("Failed to import reminder")
                    }
                } else {
                    showError("Invalid link format")
                }
            } catch (e: Exception) {
                Log.e("DeepLink", "Import failed", e)
                showError("Unable to import reminder")
            } finally {
                finish()
            }
        }
    }
}
```

### Sharing Implementation

```kotlin
fun shareReminder(context: Context, reminder: Reminder) {
    // Generate deep link
    val deepLink = DeepLinkHandler.encodeDeepLink(reminder)
    
    // Create share intent
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, buildShareMessage(reminder, deepLink))
        putExtra(Intent.EXTRA_SUBJECT, "Peace Reminder: ${reminder.title}")
    }
    
    // Show share sheet
    context.startActivity(
        Intent.createChooser(shareIntent, "Share reminder via")
    )
}

fun buildShareMessage(reminder: Reminder, deepLink: String): String {
    return """
        I'm sharing a reminder with you from Peace:
        
        ${reminder.title}
        ${formatDateTime(reminder.scheduledFor)}
        
        Tap this link to import it into your Peace app:
        $deepLink
        
        Don't have Peace? Get it here: [Play Store Link]
    """.trimIndent()
}
```

---

## Error Handling

### Error Types

**Invalid URI Format**:
```kotlin
// URI doesn't match peace://share?data=...
Result: Show "Invalid link" error
Action: Don't attempt import
```

**Decoding Failure**:
```kotlin
// Base64 decoding fails
Result: Show "Corrupted link" error
Action: Log error, don't import
```

**JSON Parsing Failure**:
```kotlin
// JSON is malformed
Result: Show "Invalid data format" error
Action: Log error, don't import
```

**Validation Failure**:
```kotlin
// Required fields missing or invalid
Result: Show "Incomplete reminder data" error
Action: Log validation errors, don't import
```

**Database Error**:
```kotlin
// Import to database fails
Result: Show "Failed to save reminder" error
Action: Log error, retry once
```

### Error Messages

User-friendly error messages:

```kotlin
sealed class DeepLinkError {
    object InvalidUri : DeepLinkError()
    object DecodingFailed : DeepLinkError()
    object ParsingFailed : DeepLinkError()
    object ValidationFailed : DeepLinkError()
    object ImportFailed : DeepLinkError()
}

fun getErrorMessage(error: DeepLinkError): String {
    return when (error) {
        InvalidUri -> "This link is not a valid Peace reminder link"
        DecodingFailed -> "Unable to read reminder data from link"
        ParsingFailed -> "Reminder data is corrupted or incomplete"
        ValidationFailed -> "Reminder data contains invalid values"
        ImportFailed -> "Failed to save reminder. Please try again"
    }
}
```

---

## Testing

### Unit Tests

```kotlin
class DeepLinkHandlerTest {
    
    @Test
    fun `encode and decode reminder preserves all fields`() {
        val original = createTestReminder()
        val link = DeepLinkHandler.encodeDeepLink(original)
        val decoded = DeepLinkHandler.decodeDeepLink(Uri.parse(link))
        
        assertEquals(original.title, decoded?.title)
        assertEquals(original.scheduledFor, decoded?.scheduledFor)
        assertEquals(original.priority, decoded?.priority)
        // ... assert all fields
    }
    
    @Test
    fun `decode invalid URI returns null`() {
        val invalidUri = Uri.parse("https://example.com")
        val result = DeepLinkHandler.decodeDeepLink(invalidUri)
        
        assertNull(result)
    }
    
    @Test
    fun `decode corrupted data returns null`() {
        val corruptedUri = Uri.parse("peace://share?data=invalid_base64!!!")
        val result = DeepLinkHandler.decodeDeepLink(corruptedUri)
        
        assertNull(result)
    }
}
```

### Integration Tests

```kotlin
class DeepLinkIntegrationTest {
    
    @Test
    fun `sharing and importing reminder creates independent copy`() {
        // Create original reminder
        val original = createAndSaveReminder()
        
        // Generate deep link
        val link = shareReminder(original)
        
        // Import from link
        val imported = importFromDeepLink(link)
        
        // Verify independence
        assertNotEquals(original.id, imported.id)
        assertEquals(original.title, imported.title)
        
        // Modify original
        updateReminder(original.copy(title = "Modified"))
        
        // Verify imported is unchanged
        val importedAgain = getReminder(imported.id)
        assertEquals(original.title, importedAgain.title)
    }
}
```

### Property-Based Tests

```kotlin
class DeepLinkPropertyTest : StringSpec({
    
    "round-trip encoding preserves reminder data" {
        checkAll(Arb.reminder()) { reminder ->
            val link = DeepLinkHandler.encodeDeepLink(reminder)
            val decoded = DeepLinkHandler.decodeDeepLink(Uri.parse(link))
            
            decoded shouldNotBe null
            decoded?.title shouldBe reminder.title
            decoded?.scheduledFor shouldBe reminder.scheduledFor
            // ... check all fields
        }
    }
    
    "encoded links are under 8KB" {
        checkAll(Arb.reminder()) { reminder ->
            val link = DeepLinkHandler.encodeDeepLink(reminder)
            link.length shouldBeLessThan 8192
        }
    }
})
```

---

## Examples

### Example 1: Simple Reminder

**Reminder**:
```kotlin
Reminder(
    title = "Team Meeting",
    scheduledFor = 1701360000000L, // Nov 30, 2023 12:00 PM
    priority = Priority.HIGH,
    category = Category.WORK
)
```

**JSON**:
```json
{
  "title": "Team Meeting",
  "scheduledFor": 1701360000000,
  "priority": "HIGH",
  "category": "WORK"
}
```

**Deep Link**:
```
peace://share?data=eyJ0aXRsZSI6IlRlYW0gTWVldGluZyIsInNjaGVkdWxlZEZvciI6MTcwMTM2MDAwMDAwMCwicHJpb3JpdHkiOiJISUdIIiwiY2F0ZWdvcnkiOiJXT1JLIn0
```

---

### Example 2: Recurring Reminder

**Reminder**:
```kotlin
Reminder(
    title = "Weekly Standup",
    scheduledFor = 1701360000000L,
    priority = Priority.MEDIUM,
    category = Category.WORK,
    isRecurring = true,
    recurrencePattern = RecurrencePattern.WEEKLY,
    recurrenceInterval = 1
)
```

**JSON**:
```json
{
  "title": "Weekly Standup",
  "scheduledFor": 1701360000000,
  "priority": "MEDIUM",
  "category": "WORK",
  "isRecurring": true,
  "recurrencePattern": "WEEKLY",
  "recurrenceInterval": 1
}
```

**Deep Link**:
```
peace://share?data=eyJ0aXRsZSI6IldlZWtseSBTdGFuZHVwIiwic2NoZWR1bGVkRm9yIjoxNzAxMzYwMDAwMDAwLCJwcmlvcml0eSI6Ik1FRElVTSIsImNhdGVnb3J5IjoiV09SSyIsImlzUmVjdXJyaW5nIjp0cnVlLCJyZWN1cnJlbmNlUGF0dGVybiI6IldFRUtMWSIsInJlY3VycmVuY2VJbnRlcnZhbCI6MX0
```

---

### Example 3: Nag Mode Reminder

**Reminder**:
```kotlin
Reminder(
    title = "Drink Water",
    scheduledFor = 1701360000000L,
    priority = Priority.LOW,
    category = Category.HEALTH,
    isNagMode = true,
    nagModeRepetitions = 5,
    nagModeInterval = 7200000L, // 2 hours
    nagModeIsFlexMode = true
)
```

**JSON**:
```json
{
  "title": "Drink Water",
  "scheduledFor": 1701360000000,
  "priority": "LOW",
  "category": "HEALTH",
  "isNagMode": true,
  "nagModeRepetitions": 5,
  "nagModeInterval": 7200000,
  "nagModeIsFlexMode": true
}
```

**Deep Link**:
```
peace://share?data=eyJ0aXRsZSI6IkRyaW5rIFdhdGVyIiwic2NoZWR1bGVkRm9yIjoxNzAxMzYwMDAwMDAwLCJwcmlvcml0eSI6IkxPVyIsImNhdGVnb3J5IjoiSEVBTFRIIiwiaXNOYWdNb2RlIjp0cnVlLCJuYWdNb2RlUmVwZXRpdGlvbnMiOjUsIm5hZ01vZGVJbnRlcnZhbCI6NzIwMDAwMCwibmFnTW9kZUlzRmxleE1vZGUiOnRydWV9
```

---

### Example 4: Reminder with Subtasks

**Reminder**:
```kotlin
Reminder(
    title = "Launch Product",
    scheduledFor = 1701360000000L,
    priority = Priority.HIGH,
    category = Category.WORK,
    subtasks = listOf(
        Subtask(title = "Finalize design", order = 0),
        Subtask(title = "Test features", order = 1),
        Subtask(title = "Deploy to production", order = 2)
    )
)
```

**JSON**:
```json
{
  "title": "Launch Product",
  "scheduledFor": 1701360000000,
  "priority": "HIGH",
  "category": "WORK",
  "subtasks": [
    {"title": "Finalize design", "order": 0},
    {"title": "Test features", "order": 1},
    {"title": "Deploy to production", "order": 2}
  ]
}
```

**Deep Link**:
```
peace://share?data=eyJ0aXRsZSI6IkxhdW5jaCBQcm9kdWN0Iiwic2NoZWR1bGVkRm9yIjoxNzAxMzYwMDAwMDAwLCJwcmlvcml0eSI6IkhJR0giLCJjYXRlZ29yeSI6IldPUksiLCJzdWJ0YXNrcyI6W3sidGl0bGUiOiJGaW5hbGl6ZSBkZXNpZ24iLCJvcmRlciI6MH0seyJ0aXRsZSI6IlRlc3QgZmVhdHVyZXMiLCJvcmRlciI6MX0seyJ0aXRsZSI6IkRlcGxveSB0byBwcm9kdWN0aW9uIiwib3JkZXIiOjJ9XX0
```

---

## Conclusion

Peace's deep link system provides a secure, efficient way to share reminders across messaging platforms. The Base64-encoded JSON format ensures compatibility while maintaining data integrity.

**Key Points**:
- URI format: `peace://share?data=<base64>`
- JSON structure with required and optional fields
- Base64 URL-safe encoding
- Maximum 8KB link size
- Validation and error handling
- Independent copies (no live sync)

For implementation details, see `DeepLinkHandler.kt` in the codebase.

**Happy sharing! 🔗🌿**
