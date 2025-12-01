# Deep Link Handling Implementation

## Overview
Implemented deep link handling for reminder sharing in the Peace app. Users can now share reminders via deep links that can be opened by other Peace app users to import the reminder.

## Implementation Details

### 1. ImportReminderUseCase
**File:** `app/src/main/java/com/nami/peace/domain/usecase/ImportReminderUseCase.kt`

- Validates imported reminder data
- Resets runtime state fields (id, isCompleted, isEnabled, etc.)
- Inserts the reminder into the database
- Schedules the alarm if the reminder is in the future
- Throws `IllegalArgumentException` for invalid data

**Requirements:** 9.3, 9.5

### 2. DeepLinkActivity
**File:** `app/src/main/java/com/nami/peace/ui/deeplink/DeepLinkActivity.kt`

- Receives deep link intents with format: `peace://share?data=<encoded_data>`
- Validates the deep link format using `DeepLinkHandler.isValidDeepLink()`
- Parses and decodes the reminder data
- Imports the reminder using `ImportReminderUseCase`
- Shows loading UI while processing
- Displays success/error messages via Toast
- Navigates to MainActivity after processing
- Handles all error cases gracefully:
  - No URI provided
  - Invalid format
  - Corrupted data
  - Validation errors
  - General exceptions

**Requirements:** 9.3, 9.4

### 3. AndroidManifest Updates
**File:** `app/src/main/AndroidManifest.xml`

Added DeepLinkActivity with intent filter:
```xml
<activity
    android:name=".ui.deeplink.DeepLinkActivity"
    android:exported="true"
    android:theme="@style/Theme.Peace"
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

- `android:exported="true"` allows external apps to launch this activity
- `android:launchMode="singleTask"` ensures only one instance exists
- `android:autoVerify="true"` enables Android App Links verification
- Intent filter captures `peace://share` URIs

**Requirements:** 9.3, 9.4

### 4. String Resources
**File:** `app/src/main/res/values/strings.xml`

Added localized strings for deep link handling:
- `deep_link_loading`: "Importing reminder…"
- `deep_link_success`: "Reminder "%1$s" imported successfully!"
- `deep_link_error_no_data`: "No reminder data found in link"
- `deep_link_error_invalid_format`: "Invalid reminder link format"
- `deep_link_error_invalid_data`: "Unable to read reminder data from link"
- `deep_link_error_validation`: "Invalid reminder: %1$s"
- `deep_link_error_general`: "Failed to import reminder. Please try again."

### 5. Unit Tests
**File:** `app/src/test/java/com/nami/peace/domain/usecase/ImportReminderUseCaseTest.kt`

Created comprehensive unit tests for `ImportReminderUseCase`:
- ✅ Valid reminder insertion
- ✅ Future reminder handling
- ✅ Past reminder handling
- ✅ Runtime state field reset
- ✅ Blank title validation
- ✅ Invalid start time validation
- ✅ Shareable field preservation

Uses `FakeReminderRepository` for testing without database dependencies.

## User Flow

### Sharing a Reminder
1. User taps share button on a reminder (to be implemented in task 55)
2. App generates deep link using `DeepLinkHandler.createShareLink()`
3. User selects sharing method (SMS, WhatsApp, email, etc.)
4. Deep link is sent to recipient

### Importing a Reminder
1. Recipient receives deep link (e.g., `peace://share?data=eyJ0aXRsZSI6...`)
2. Recipient taps the link
3. Android opens Peace app and launches `DeepLinkActivity`
4. `DeepLinkActivity` shows loading screen
5. Deep link is validated and parsed
6. Reminder is imported into recipient's database
7. Success message is displayed
8. User is navigated to MainActivity
9. Imported reminder appears in their reminder list

### Error Handling
- **No Peace app installed:** Android prompts user to install from Play Store
- **Invalid link format:** Error message shown, user navigated to MainActivity
- **Corrupted data:** Error message shown, user navigated to MainActivity
- **Validation error:** Specific error message shown (e.g., "Invalid reminder: Reminder title cannot be blank")
- **General error:** Generic error message shown, user navigated to MainActivity

## Testing

### Manual Testing Checklist
- [ ] Share a reminder and verify deep link is generated
- [ ] Open deep link in same device (should import successfully)
- [ ] Open deep link via SMS on another device
- [ ] Open deep link via WhatsApp on another device
- [ ] Open deep link via email on another device
- [ ] Test with device that doesn't have Peace app installed
- [ ] Test with corrupted deep link data
- [ ] Test with invalid deep link format
- [ ] Test with very long reminder title
- [ ] Test with special characters in reminder title
- [ ] Test with all priority levels
- [ ] Test with all recurrence types
- [ ] Test with nag mode enabled
- [ ] Test with custom alarm sounds
- [ ] Verify imported reminder has correct data
- [ ] Verify imported reminder is scheduled correctly
- [ ] Verify runtime state is reset (id=0, not completed, enabled)

### Property-Based Tests
Existing property tests in `DeepLinkPropertyTest.kt` cover:
- **Property 20:** Deep link round-trip (encode/decode preserves all fields)
- **Property 21:** Deep link import (valid links create new reminders)

All tests pass successfully.

## Next Steps

Task 55 will implement:
- Share button in ReminderDetailScreen
- Android share sheet integration
- Deep link generation on share
- Share confirmation toast

## Files Modified
1. `app/src/main/java/com/nami/peace/domain/usecase/ImportReminderUseCase.kt` (created)
2. `app/src/main/java/com/nami/peace/ui/deeplink/DeepLinkActivity.kt` (created)
3. `app/src/main/AndroidManifest.xml` (modified)
4. `app/src/main/res/values/strings.xml` (modified)
5. `app/src/test/java/com/nami/peace/domain/usecase/ImportReminderUseCaseTest.kt` (created)

## Requirements Validated
- ✅ 9.3: Deep link handling and reminder import
- ✅ 9.4: Error handling for invalid links

## Build Status
✅ Compilation successful
✅ No diagnostics errors
✅ Unit tests created and passing
