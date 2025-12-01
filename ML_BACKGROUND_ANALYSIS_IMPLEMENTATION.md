# ML Background Analysis Implementation

## Overview

This document describes the implementation of the background ML analysis system that runs daily to generate productivity suggestions based on user behavior patterns.

## Implementation Summary

### 1. AnalysisWorker

**Location:** `app/src/main/java/com/nami/peace/worker/AnalysisWorker.kt`

**Purpose:** Background worker that performs daily ML analysis and generates suggestions.

**Key Features:**
- Runs daily using WorkManager
- Implements 30-second timeout for analysis
- Generates all types of ML suggestions
- Stores suggestions in database
- Sends notification when new suggestions are available
- Handles errors with retry logic

**Implementation Details:**
```kotlin
@HiltWorker
class AnalysisWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val suggestionGenerator: SuggestionGenerator,
    private val suggestionRepository: SuggestionRepository,
    private val suggestionNotificationHelper: SuggestionNotificationHelper
) : CoroutineWorker(context, workerParams)
```

**Workflow:**
1. Runs `suggestionGenerator.generateAllSuggestions()` with 30-second timeout
2. Stores each generated suggestion in the database
3. Counts successfully stored suggestions
4. Sends notification if new suggestions are available
5. Returns success/retry/failure based on outcome

**Error Handling:**
- **Timeout:** Returns `Result.failure()` (don't retry, wait for next scheduled run)
- **Other Errors:** Returns `Result.retry()` (retry with exponential backoff)
- **Individual Suggestion Errors:** Logged but don't fail the entire analysis

### 2. SuggestionNotificationHelper

**Location:** `app/src/main/java/com/nami/peace/util/notification/SuggestionNotificationHelper.kt`

**Purpose:** Creates and displays notifications for new ML suggestions.

**Key Features:**
- Creates dedicated notification channel for ML suggestions
- Shows notification with count of new suggestions
- Opens suggestions screen when tapped
- Uses appropriate notification priority (DEFAULT)

**Notification Content:**
- **Single Suggestion:** "New Productivity Suggestion"
- **Multiple Suggestions:** "X New Productivity Suggestions"
- **Message:** Encourages user to view suggestions
- **Action:** Opens MainActivity with `open_suggestions` extra

### 3. SuggestionRepositoryImpl

**Location:** `app/src/main/java/com/nami/peace/data/repository/SuggestionRepositoryImpl.kt`

**Purpose:** Implementation of SuggestionRepository interface for database operations.

**Key Features:**
- Provides CRUD operations for suggestions
- Converts between domain models and entities
- Uses Flow for reactive data
- Singleton scope for dependency injection

**Methods:**
- `getPendingSuggestions()`: Returns Flow of pending suggestions
- `getSuggestionById(id)`: Gets specific suggestion
- `insertSuggestion(suggestion)`: Stores new suggestion
- `updateSuggestion(suggestion)`: Updates existing suggestion
- `deleteSuggestion(suggestion)`: Removes suggestion

### 4. Worker Scheduling

**Location:** `app/src/main/java/com/nami/peace/PeaceApplication.kt`

**Purpose:** Schedules the AnalysisWorker to run daily.

**Configuration:**
```kotlin
private fun scheduleMLAnalysis() {
    val constraints = Constraints.Builder()
        .setRequiresBatteryNotLow(true)
        .build()
    
    val analysisRequest = PeriodicWorkRequestBuilder<AnalysisWorker>(
        1, TimeUnit.DAYS
    )
        .setConstraints(constraints)
        .build()
    
    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        AnalysisWorker.WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        analysisRequest
    )
}
```

**Constraints:**
- Requires battery not low (to avoid draining battery)
- Runs once per day
- Uses KEEP policy (doesn't replace existing work)

## Data Flow

```
┌─────────────────────────────────────┐
│ 1. WorkManager triggers daily       │
│    (AnalysisWorker)                 │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ 2. Generate suggestions             │
│    (SuggestionGenerator)            │
│    - Analyze completion patterns    │
│    - Analyze priority patterns      │
│    - Analyze recurring patterns     │
│    - Analyze focus sessions         │
│    - Timeout after 30 seconds       │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ 3. Store suggestions                │
│    (SuggestionRepository)           │
│    - Insert each suggestion         │
│    - Count successful inserts       │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ 4. Send notification                │
│    (SuggestionNotificationHelper)   │
│    - If new suggestions > 0         │
│    - Show count in notification     │
└─────────────────────────────────────┘
```

## Requirements Validation

### Requirement 12.1: ML Data Collection and Analysis

✅ **WHEN the Peace System analyzes user behavior THEN the Peace System SHALL identify patterns in task completion times, priorities, and recurrence**

- AnalysisWorker runs daily analysis
- Uses SuggestionGenerator to analyze all patterns
- Processes completion events from last 90 days

## Testing

### Manual Testing

1. **Trigger Analysis Manually:**
   ```kotlin
   // In debug build, trigger worker immediately
   WorkManager.getInstance(context)
       .enqueueUniqueWork(
           AnalysisWorker.WORK_NAME,
           ExistingWorkPolicy.REPLACE,
           OneTimeWorkRequestBuilder<AnalysisWorker>().build()
       )
   ```

2. **Verify Notification:**
   - Complete several tasks over multiple days
   - Trigger analysis worker
   - Check notification appears with suggestion count
   - Tap notification to verify it opens suggestions screen

3. **Verify Timeout:**
   - Mock SuggestionGenerator to delay > 30 seconds
   - Verify worker fails (doesn't retry)
   - Check logs for timeout message

4. **Verify Error Handling:**
   - Mock SuggestionGenerator to throw exception
   - Verify worker retries
   - Check logs for error message

### Integration Testing

The worker integrates with:
- **SuggestionGenerator:** Generates suggestions from patterns
- **SuggestionRepository:** Stores suggestions in database
- **SuggestionNotificationHelper:** Displays notifications
- **WorkManager:** Schedules daily execution

## Configuration

### Work Constraints

- **Battery:** Requires battery not low
- **Frequency:** Once per day
- **Policy:** KEEP (doesn't replace existing work)

### Timeout

- **Duration:** 30 seconds
- **Behavior:** Fails without retry on timeout

### Notification Channel

- **ID:** `ml_suggestions`
- **Name:** "ML Suggestions"
- **Importance:** DEFAULT
- **Vibration:** Enabled

## Files Created/Modified

### Created Files

1. `app/src/main/java/com/nami/peace/worker/AnalysisWorker.kt`
   - Background worker for ML analysis

2. `app/src/main/java/com/nami/peace/util/notification/SuggestionNotificationHelper.kt`
   - Notification helper for suggestions

3. `app/src/main/java/com/nami/peace/data/repository/SuggestionRepositoryImpl.kt`
   - Repository implementation for suggestions

### Modified Files

1. `app/src/main/java/com/nami/peace/PeaceApplication.kt`
   - Added `scheduleMLAnalysis()` method
   - Calls scheduling in `onCreate()`

## Future Enhancements

1. **Adaptive Scheduling:**
   - Analyze when user is most active
   - Schedule analysis during low-usage periods

2. **Progressive Analysis:**
   - Break analysis into smaller chunks
   - Run different analyses on different days

3. **User Preferences:**
   - Allow users to configure analysis frequency
   - Option to disable background analysis

4. **Analytics:**
   - Track analysis duration
   - Monitor suggestion acceptance rates
   - Adjust confidence thresholds based on feedback

## Dependencies

- **WorkManager:** For background task scheduling
- **Hilt:** For dependency injection
- **Room:** For database operations
- **Kotlin Coroutines:** For async operations
- **kotlinx.serialization:** For JSON encoding

## Conclusion

The background analysis system is now fully implemented and integrated. It runs daily to analyze user behavior patterns and generate actionable productivity suggestions. The system includes proper error handling, timeout protection, and user notifications, ensuring a reliable and user-friendly experience.
