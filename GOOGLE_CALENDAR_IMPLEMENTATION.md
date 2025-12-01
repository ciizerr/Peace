# Google Calendar API Implementation

## Overview

This document summarizes the implementation of Google Calendar API integration for the Peace app, enabling users to sync their reminders to Google Calendar.

## Implementation Status: ✅ COMPLETE

Task 48 from the implementation plan has been successfully completed.

## What Was Implemented

### 1. Dependencies ✅

All required dependencies were already present in `app/build.gradle.kts`:
- `com.google.android.gms:play-services-auth:21.2.0` - Google Sign-In
- `com.google.api-client:google-api-client-android:2.6.0` - Google API Client
- `com.google.apis:google-api-services-calendar:v3-rev20240517-2.0.0` - Calendar API
- `com.google.accompanist:accompanist-permissions:0.36.0` - Permission handling

### 2. Permissions Configuration ✅

Added to `AndroidManifest.xml`:
```xml
<!-- Google Calendar permissions -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
<uses-permission android:name="android.permission.READ_CALENDAR" />
<uses-permission android:name="android.permission.WRITE_CALENDAR" />
```

### 3. CalendarManager Interface ✅

Created `app/src/main/java/com/nami/peace/util/calendar/CalendarManager.kt`:

**Key Methods:**
- `hasCalendarPermissions()` - Check if calendar permissions are granted
- `isAuthenticated()` - Check if user is signed in with Google
- `getAuthenticatedAccount()` - Get current Google account
- `requestAuthentication()` - Initiate Google Sign-In flow
- `signOut()` - Sign out from Google account
- `getOrCreatePeaceCalendar()` - Create or retrieve "Peace Reminders" calendar
- `syncReminder()` - Sync a single reminder to calendar
- `syncAllReminders()` - Sync all active reminders
- `updateCalendarEvent()` - Update existing calendar event
- `deleteCalendarEvent()` - Delete calendar event
- `getSyncStats()` - Get sync statistics (last sync time, count)

### 4. CalendarManager Implementation ✅

Created `app/src/main/java/com/nami/peace/util/calendar/CalendarManagerImpl.kt`:

**Features:**
- OAuth 2.0 authentication flow using Google Sign-In
- Automatic "Peace Reminders" calendar creation
- Reminder-to-event conversion with proper formatting
- Event description includes priority, category, recurrence, and nag mode info
- Sync statistics tracking in DataStore preferences
- Comprehensive error handling with Result types
- Calendar ID caching to avoid repeated lookups

**Event Mapping:**
- Title: Reminder title
- Start time: Reminder start time
- End time: 1 hour after start (default)
- Description: Priority, category, recurrence, nag mode details
- Reminder: Popup notification at event time
- Time zone: Device default time zone

### 5. Permission Helper ✅

Created `app/src/main/java/com/nami/peace/util/calendar/CalendarPermissionHelper.kt`:

**Features:**
- Compose-friendly permission handling
- Integration with Accompanist Permissions library
- Google Sign-In launcher management
- Permission state checking
- Activity result handling for sign-in flow

**Composable Function:**
```kotlin
@Composable
fun rememberCalendarPermissionHelper(
    onSignInSuccess: () -> Unit,
    onSignInFailure: (Exception) -> Unit
): CalendarPermissionHelper
```

### 6. Use Cases ✅

Created `app/src/main/java/com/nami/peace/domain/usecase/SyncToCalendarUseCase.kt`:

**Methods:**
- `syncReminder(reminder)` - Sync single reminder with authentication check
- `syncAllReminders(reminders)` - Sync all active reminders (filters completed/disabled)
- `getSyncStats()` - Retrieve sync statistics

### 7. Dependency Injection ✅

Created `app/src/main/java/com/nami/peace/di/CalendarModule.kt`:

Provides singleton `CalendarManager` instance via Hilt.

### 8. UserPreferencesRepository Extensions ✅

Added generic methods to `UserPreferencesRepository.kt`:
- `saveString(key, value)` / `getString(key)` - For calendar ID storage
- `saveInt(key, value)` / `getInt(key)` - For sync count
- `saveLong(key, value)` / `getLong(key)` - For last sync timestamp

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                    │
│  ┌──────────────────────────────────────────────────┐  │
│  │  CalendarPermissionHelper                        │  │
│  │  - Permission requests                           │  │
│  │  - Google Sign-In launcher                       │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Domain Layer                           │
│  ┌──────────────────────────────────────────────────┐  │
│  │  SyncToCalendarUseCase                           │  │
│  │  - Business logic for syncing                    │  │
│  │  - Filters active reminders                      │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Util Layer                             │
│  ┌──────────────────────────────────────────────────┐  │
│  │  CalendarManager (Interface)                     │  │
│  │  CalendarManagerImpl                             │  │
│  │  - OAuth authentication                          │  │
│  │  - Calendar API interactions                     │  │
│  │  - Event CRUD operations                         │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│              External Services                           │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Google Sign-In API                              │  │
│  │  Google Calendar API                             │  │
│  │  DataStore (Preferences)                         │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## OAuth 2.0 Configuration Required

⚠️ **IMPORTANT:** Before the calendar sync feature can be used, OAuth 2.0 credentials must be configured in Google Cloud Console.

See `GOOGLE_CALENDAR_SETUP.md` for detailed instructions on:
1. Creating a Google Cloud project
2. Enabling Google Calendar API
3. Configuring OAuth consent screen
4. Creating OAuth 2.0 credentials (Android)
5. Getting SHA-1 fingerprints
6. Testing with test users

## Usage Flow

### 1. Initial Setup (First Time)
```
User enables calendar sync in Settings
    ↓
App checks calendar permissions
    ↓
If not granted → Request READ_CALENDAR and WRITE_CALENDAR
    ↓
Check Google Sign-In status
    ↓
If not signed in → Launch Google Sign-In with Calendar scope
    ↓
User signs in and grants calendar access
    ↓
CalendarManager creates "Peace Reminders" calendar
    ↓
Ready to sync
```

### 2. Syncing Reminders
```
User triggers manual sync OR reminder is created/updated
    ↓
SyncToCalendarUseCase filters active reminders
    ↓
CalendarManager converts each reminder to calendar event
    ↓
Events are created/updated in "Peace Reminders" calendar
    ↓
Sync statistics updated (timestamp, count)
    ↓
User sees confirmation
```

### 3. Automatic Sync (Future Enhancement)
```
Reminder created/updated
    ↓
If calendar sync enabled
    ↓
Background sync triggered
    ↓
Event created/updated in calendar
```

## Data Mapping

### Reminder → Calendar Event

| Reminder Field | Calendar Event Field | Notes |
|----------------|---------------------|-------|
| `title` | `summary` | Direct mapping |
| `startTimeInMillis` | `start.dateTime` | Converted to DateTime |
| `startTimeInMillis + 1h` | `end.dateTime` | Default 1-hour duration |
| `priority` | `description` | Included in description |
| `category` | `description` | Included in description |
| `recurrenceType` | `description` | Included in description |
| `isNagModeEnabled` | `description` | Nag mode details in description |
| Device timezone | `start.timeZone`, `end.timeZone` | Uses device default |

### Event Description Format
```
Priority: HIGH
Category: WORK
Recurrence: DAILY
Nag Mode: 5 times, 120 min interval

Synced from Peace app
```

## Stored Preferences

The following keys are stored in DataStore:

| Key | Type | Purpose |
|-----|------|---------|
| `peace_calendar_id` | String | ID of "Peace Reminders" calendar |
| `calendar_last_sync` | Long | Timestamp of last successful sync |
| `calendar_synced_count` | Int | Number of events synced in last operation |

## Error Handling

All CalendarManager methods return `Result<T>` for proper error handling:

**Common Errors:**
- `"Calendar service not initialized"` - User not authenticated
- `"Not authenticated with Google"` - Sign-in required
- `"Failed to get calendar"` - Calendar creation/retrieval failed
- Network errors - No internet connection
- Permission errors - Calendar permissions not granted
- API errors - Google Calendar API issues

**Error Recovery:**
- All errors are wrapped in Result.failure()
- UI can display user-friendly error messages
- Retry logic can be implemented at use case level
- Offline queue can be added for failed syncs

## Testing Considerations

### Unit Tests (To Be Implemented)
- CalendarManager mock for testing use cases
- Permission state testing
- Event conversion logic testing
- Error handling scenarios

### Integration Tests (To Be Implemented)
- End-to-end sync flow
- OAuth flow testing
- Calendar creation testing
- Event CRUD operations

### Manual Testing Checklist
- [ ] Request calendar permissions
- [ ] Google Sign-In flow
- [ ] Calendar creation
- [ ] Single reminder sync
- [ ] Bulk reminder sync
- [ ] Event update
- [ ] Event deletion
- [ ] Sync statistics display
- [ ] Error handling (no internet, permission denied, etc.)
- [ ] Sign out and re-authenticate

## Known Limitations

1. **One-way sync only**: Changes in Google Calendar don't sync back to Peace app
2. **No conflict resolution**: If event is modified in both places, last write wins
3. **Fixed event duration**: All events are 1 hour long (could be configurable)
4. **No recurrence rules**: Recurring reminders create single events (not recurring events)
5. **Deprecated API**: GoogleSignIn is deprecated but still functional (migration to Credential Manager recommended for future)

## Future Enhancements

1. **Automatic sync on reminder changes**
   - Use WorkManager to sync in background
   - Implement sync queue for offline changes

2. **Two-way sync**
   - Listen for calendar changes
   - Update reminders when events change

3. **Configurable event duration**
   - Let users set default event duration
   - Infer duration from reminder type

4. **Recurring event support**
   - Convert recurring reminders to recurring calendar events
   - Use RRULE format for recurrence

5. **Selective sync**
   - Let users choose which reminders to sync
   - Sync by category or priority

6. **Multiple calendar support**
   - Sync different categories to different calendars
   - Let users choose target calendar

7. **Conflict resolution**
   - Detect conflicts between Peace and Calendar
   - Let users choose which version to keep

8. **Migration to Credential Manager**
   - Replace deprecated GoogleSignIn
   - Use modern authentication flow

## Security Considerations

1. **OAuth 2.0 credentials are not in source code**
   - Configured in Google Cloud Console
   - Linked to app via package name and SHA-1

2. **Permissions are runtime-requested**
   - User must explicitly grant calendar access
   - Can be revoked at any time

3. **Minimal scope requested**
   - Only Calendar scope, not full Google account access
   - Read and write calendar only

4. **Local credential storage**
   - Google Sign-In handles token management
   - Tokens stored securely by Google Play Services

5. **HTTPS only**
   - All API calls use HTTPS
   - No sensitive data in URLs

## Documentation

- `GOOGLE_CALENDAR_SETUP.md` - OAuth 2.0 configuration guide
- `GOOGLE_CALENDAR_IMPLEMENTATION.md` - This file
- Code comments in all calendar-related files

## Files Created/Modified

### Created Files
1. `app/src/main/java/com/nami/peace/util/calendar/CalendarManager.kt`
2. `app/src/main/java/com/nami/peace/util/calendar/CalendarManagerImpl.kt`
3. `app/src/main/java/com/nami/peace/util/calendar/CalendarPermissionHelper.kt`
4. `app/src/main/java/com/nami/peace/domain/usecase/SyncToCalendarUseCase.kt`
5. `app/src/main/java/com/nami/peace/di/CalendarModule.kt`
6. `GOOGLE_CALENDAR_SETUP.md`
7. `GOOGLE_CALENDAR_IMPLEMENTATION.md`

### Modified Files
1. `app/src/main/AndroidManifest.xml` - Added calendar permissions
2. `app/src/main/java/com/nami/peace/data/repository/UserPreferencesRepository.kt` - Added generic save/get methods

## Build Status

✅ **Build Successful**

The implementation compiles without errors. Some deprecation warnings are present for GoogleSignIn API, which is expected as Google is transitioning to newer authentication methods. The current implementation remains functional.

## Next Steps

To complete the calendar integration feature:

1. **Configure OAuth 2.0** (Required before testing)
   - Follow `GOOGLE_CALENDAR_SETUP.md`
   - Create Google Cloud project
   - Enable Calendar API
   - Configure OAuth credentials

2. **Implement UI** (Task 50)
   - Add calendar sync toggle to Settings
   - Add manual sync button
   - Display sync statistics
   - Show last sync time
   - Add sync error handling UI

3. **Implement Sync Logic** (Task 49)
   - Create background sync worker
   - Implement automatic sync on reminder changes
   - Add sync queue for offline changes
   - Implement retry with exponential backoff

4. **Testing**
   - Write unit tests for CalendarManager
   - Write integration tests for sync flow
   - Manual testing with real Google account
   - Test error scenarios

5. **Error Handling** (Task 51)
   - Handle permission denial gracefully
   - Handle network errors with retry
   - Implement offline sync queue
   - Add user-friendly error messages

## Conclusion

Task 48 (Set up Google Calendar API) has been successfully completed. The foundation for Google Calendar integration is now in place, including:

- ✅ OAuth 2.0 authentication flow
- ✅ Calendar API client setup
- ✅ Permission request flow
- ✅ CalendarManager for API interactions
- ✅ Use cases for business logic
- ✅ Dependency injection setup
- ✅ Comprehensive documentation

The implementation follows clean architecture principles, uses proper error handling with Result types, and is ready for UI integration and testing once OAuth 2.0 credentials are configured.
