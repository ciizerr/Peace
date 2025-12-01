# Deep Link System Implementation

## Overview
Implemented a complete deep link system for sharing reminders between Peace app users via messaging apps, SMS, WhatsApp, email, and other sharing methods.

## Implementation Details

### 1. DeepLinkHandler (`app/src/main/java/com/nami/peace/util/deeplink/DeepLinkHandler.kt`)

**Core Functionality:**
- Encodes reminders to deep link URIs with Base64-encoded JSON data
- Decodes deep link URIs back to reminder objects
- Validates deep link format and data integrity
- Enforces 8KB data size limit for compatibility

**Deep Link Format:**
```
peace://share?data=<base64_encoded_json>
```

**Key Features:**
- Uses kotlinx.serialization for JSON encoding/decoding
- Base64 URL-safe encoding for URI compatibility
- Excludes runtime state fields (id, completion status, snooze state)
- Preserves all shareable fields (title, priority, category, nag mode, custom sounds, etc.)
- Automatic validation of URI scheme, host, and data parameter

**ShareableReminder Data Class:**
- Serializable version of Reminder for deep link sharing
- Includes: title, priority, start time, recurrence, nag mode settings, category, scheduling mode, custom alarm sounds
- Excludes: id, current repetition index, completion status, enabled status, snooze state

### 2. Property-Based Tests (`app/src/test/java/com/nami/peace/util/deeplink/DeepLinkPropertyTest.kt`)

**Property 20: Deep link round-trip**
- Tests that encoding and decoding preserves all shareable fields
- Validates all priority levels, recurrence types, and categories
- Tests nag mode with various configurations
- Tests custom alarm sounds
- Tests weekly recurrence with days of week
- Tests special characters and unicode in titles
- Tests very long titles (up to 500 characters)
- Tests 50 random reminder configurations
- Verifies runtime state fields are reset (id=0, not completed, enabled, not in snooze)

**Property 21: Deep link import**
- Tests valid URI creates new reminder with id=0
- Tests invalid scheme returns null
- Tests invalid host returns null
- Tests missing data parameter returns null
- Tests corrupted data returns null
- Tests malformed JSON returns null
- Tests URI validation works correctly
- Tests data size limit is enforced (8KB)
- Tests complete workflow from share to import

**Test Results:**
- ✅ All 22 tests passed
- ✅ 0 failures
- ✅ 0 errors
- ✅ Total execution time: 7.1 seconds

## Requirements Validated

### Requirement 9.2: Deep Link Generation
✅ WHEN the user selects a sharing method THEN the Peace System SHALL generate a deep link with encoded reminder data
- Deep links are generated with `createShareLink(reminder)` method
- Data is Base64-encoded JSON in URL-safe format
- All shareable fields are preserved

### Requirement 9.3: Deep Link Import
✅ WHEN a recipient with the Peace app installed opens the deep link THEN the Peace System SHALL import the reminder into their app
- Deep links are parsed with `parseShareLink(uri)` method
- Invalid links return null gracefully
- Valid links create new reminder with id=0 for database insertion

### Requirement 9.7: Deep Link Data Encoding
✅ WHEN the deep link is generated THEN the Peace System SHALL include all reminder data (title, time, priority, category, recurrence, nag mode settings)
- All shareable fields are encoded in ShareableReminder
- Runtime state fields are excluded (id, completion, snooze state)
- Custom alarm sounds are preserved
- Nag mode settings are preserved
- Weekly recurrence days are preserved

## Usage Example

```kotlin
// Inject DeepLinkHandler
@Inject lateinit var deepLinkHandler: DeepLinkHandler

// Create a deep link for sharing
val reminder = // ... get reminder from database
val deepLink = deepLinkHandler.createShareLink(reminder)
// Result: "peace://share?data=eyJ0aXRsZSI6IlRlc3QgUmVtaW5kZXIiLCJwcmlvcml0eSI6Ik1FRElVTSIsLi4ufQ=="

// Share via Android share sheet
val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, deepLink)
}
startActivity(Intent.createChooser(shareIntent, "Share Reminder"))

// Parse incoming deep link
val uri = intent.data // From deep link intent
val importedReminder = deepLinkHandler.parseShareLink(uri)
if (importedReminder != null) {
    // Save to database
    reminderRepository.insert(importedReminder)
}

// Validate deep link
val isValid = deepLinkHandler.isValidDeepLink(uri)
```

## Security Considerations

1. **Data Size Limit**: 8KB maximum to prevent abuse
2. **Validation**: All fields are validated during deserialization
3. **Error Handling**: Invalid data returns null instead of crashing
4. **No Sensitive Data**: Runtime state and IDs are excluded
5. **URL-Safe Encoding**: Base64 URL-safe encoding prevents URI issues

## Next Steps

To complete the deep link sharing feature:

1. **Task 54**: Implement deep link handling in AndroidManifest and DeepLinkActivity
2. **Task 55**: Create sharing UI in ReminderDetailScreen
3. **Task 56**: Test deep link sharing across different apps

## Files Created

1. `app/src/main/java/com/nami/peace/util/deeplink/DeepLinkHandler.kt` - Core deep link handler
2. `app/src/test/java/com/nami/peace/util/deeplink/DeepLinkPropertyTest.kt` - Property-based tests

## Dependencies Used

- kotlinx.serialization.json - JSON encoding/decoding
- android.util.Base64 - Base64 encoding/decoding
- android.net.Uri - URI parsing
- javax.inject - Dependency injection

## Test Coverage

- ✅ Round-trip encoding/decoding
- ✅ All enum values (priority, recurrence, category)
- ✅ Nag mode configurations
- ✅ Custom alarm sounds
- ✅ Weekly recurrence with days
- ✅ Special characters and unicode
- ✅ Long titles
- ✅ Random configurations
- ✅ Runtime state reset
- ✅ Invalid URI handling
- ✅ Data size limits
- ✅ Complete workflow

## Status

✅ **Task 53: Create deep link system - COMPLETED**
✅ **Task 53.1: Write property test for deep links - COMPLETED**

All tests passing. Ready for integration with UI and intent handling.
