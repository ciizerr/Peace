# Calendar Sync Error Handling Implementation

## Overview

This document describes the implementation of comprehensive error handling for Google Calendar sync operations, including permission denial, network errors, exponential backoff retry, and offline sync queue.

## Components

### 1. Sync Queue System

#### SyncQueueEntity
- **Location**: `app/src/main/java/com/nami/peace/data/local/SyncQueueEntity.kt`
- **Purpose**: Represents a pending calendar sync operation
- **Fields**:
  - `reminderId`: ID of the reminder to sync
  - `operationType`: CREATE, UPDATE, or DELETE
  - `eventId`: Calendar event ID (for update/delete)
  - `retryCount`: Number of retry attempts
  - `queuedAt`: Timestamp when queued
  - `lastRetryAt`: Timestamp of last retry
  - `lastError`: Error message from last attempt
  - `isProcessing`: Whether currently being processed

#### SyncQueueDao
- **Location**: `app/src/main/java/com/nami/peace/data/local/SyncQueueDao.kt`
- **Purpose**: Database access for sync queue operations
- **Key Methods**:
  - `insert()`: Add new sync operation
  - `getPendingSyncs()`: Get all pending syncs
  - `update()`: Update sync after retry
  - `delete()`: Remove completed sync
  - `markAsProcessing()`: Mark sync as being processed

#### SyncQueueRepository
- **Location**: `app/src/main/java/com/nami/peace/data/repository/SyncQueueRepository.kt`
- **Purpose**: Repository layer for sync queue management
- **Key Methods**:
  - `queueSync()`: Add operation to queue
  - `updateAfterRetry()`: Update after retry attempt
  - `getPendingSyncCount()`: Get count of pending syncs
  - `clearFailedSyncs()`: Remove failed syncs

### 2. Retry Strategy

#### SyncRetryStrategy
- **Location**: `app/src/main/java/com/nami/peace/util/calendar/SyncRetryStrategy.kt`
- **Purpose**: Implements exponential backoff retry logic
- **Configuration**:
  - Max retry attempts: 5
  - Base delay: 1 second
  - Max delay: 5 minutes
  - Formula: `min(BASE_DELAY * 2^retryCount, MAX_DELAY)`

**Retry Delays**:
- Attempt 1: 1 second
- Attempt 2: 2 seconds
- Attempt 3: 4 seconds
- Attempt 4: 8 seconds
- Attempt 5: 16 seconds

**Error Classification**:
- **Retryable**: Network errors, timeouts, 503/429 errors
- **Non-retryable**: Permission errors, authentication errors, 401/403 errors

### 3. Enhanced CalendarManager

#### CalendarSyncException
Custom exception hierarchy for calendar sync errors:
- `PermissionDenied`: Calendar permissions not granted
- `NotAuthenticated`: Not signed in with Google
- `NetworkError`: Network connectivity issues
- `ServiceNotInitialized`: Calendar service not ready
- `Unknown`: Other errors

#### Error Handling Flow

**Permission Denial**:
1. Check permissions before sync
2. If denied, queue operation for later
3. Return `PermissionDenied` exception
4. User must grant permissions
5. Worker will retry when permissions available

**Network Errors**:
1. Catch `UnknownHostException` and network errors
2. Queue operation for retry
3. Return `NetworkError` exception
4. Worker will retry with exponential backoff

**Authentication Errors**:
1. Check authentication before sync
2. If not authenticated, queue operation
3. Return `NotAuthenticated` exception
4. User must sign in
5. Worker will retry when authenticated

### 4. Sync Queue Processing

#### ProcessSyncQueueUseCase
- **Location**: `app/src/main/java/com/nami/peace/domain/usecase/ProcessSyncQueueUseCase.kt`
- **Purpose**: Process pending sync operations with retry logic
- **Process**:
  1. Check permissions and authentication
  2. Get pending syncs from queue
  3. For each sync:
     - Check if should retry (max attempts)
     - Check if enough time passed (exponential backoff)
     - Mark as processing
     - Execute sync operation
     - Update queue based on result
  4. Return count of successful syncs

#### SyncQueueWorker
- **Location**: `app/src/main/java/com/nami/peace/worker/SyncQueueWorker.kt`
- **Purpose**: WorkManager worker for periodic sync processing
- **Schedule**: Every 15 minutes
- **Constraints**: Requires network connection
- **Backoff**: Exponential backoff on failure

**Usage**:
```kotlin
// Schedule periodic processing
SyncQueueWorker.schedule(context)

// Trigger immediate processing
SyncQueueWorker.triggerImmediate(context)

// Cancel processing
SyncQueueWorker.cancel(context)
```

### 5. Database Migration

#### Migration 8 to 9
- **Location**: `app/src/main/java/com/nami/peace/data/local/AppDatabase.kt`
- **Changes**:
  - Created `sync_queue` table
  - Added indexes on `reminderId` and `isProcessing`

## Usage Examples

### Handling Permission Denial

```kotlin
val result = calendarManager.syncReminder(reminder, calendarId)
if (result.isFailure) {
    when (val error = result.exceptionOrNull()) {
        is CalendarSyncException.PermissionDenied -> {
            // Show permission request dialog
            // Operation is queued and will retry when permissions granted
        }
    }
}
```

### Handling Network Errors

```kotlin
val result = calendarManager.syncAllReminders(reminders)
if (result.isFailure) {
    when (val error = result.exceptionOrNull()) {
        is CalendarSyncException.NetworkError -> {
            // Show "No network" message
            // Operations are queued and will retry when network available
        }
    }
}
```

### Processing Sync Queue

```kotlin
// Manually trigger sync queue processing
val result = processSyncQueueUseCase.processPendingSyncs()
if (result.isSuccess) {
    val successCount = result.getOrThrow()
    Log.d(TAG, "Processed $successCount syncs")
}

// Get pending sync count
val pendingCount = processSyncQueueUseCase.getPendingSyncCount()
```

### Observing Sync Queue

```kotlin
// In ViewModel
val pendingSyncCount = syncQueueRepository.observePendingSyncCount()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

// In UI
Text("Pending syncs: ${pendingSyncCount.value}")
```

## Error Recovery Scenarios

### Scenario 1: User Denies Permissions
1. User attempts to sync reminders
2. Permissions not granted
3. All reminders queued for sync
4. User sees "Permission required" message
5. User grants permissions
6. Worker automatically processes queue
7. All reminders synced successfully

### Scenario 2: Network Unavailable
1. User attempts to sync reminders
2. Network unavailable
3. All reminders queued for sync
4. User sees "No network" message
5. Network becomes available
6. Worker processes queue with exponential backoff
7. Reminders synced successfully

### Scenario 3: Temporary API Error
1. Sync operation fails with 503 error
2. Operation queued for retry
3. Worker retries after 1 second (attempt 1)
4. Still fails, retry after 2 seconds (attempt 2)
5. Still fails, retry after 4 seconds (attempt 3)
6. Succeeds on attempt 3
7. Operation removed from queue

### Scenario 4: Permanent Error
1. Sync operation fails with 401 (unauthorized)
2. Error classified as non-retryable
3. Operation removed from queue
4. User notified to re-authenticate
5. User signs in again
6. New sync attempt succeeds

## Testing

### Unit Tests
- Test retry delay calculation
- Test error classification (retryable vs non-retryable)
- Test queue operations (add, update, remove)
- Test exponential backoff logic

### Integration Tests
- Test sync queue processing with mock calendar API
- Test permission denial handling
- Test network error handling
- Test retry with exponential backoff

### Manual Testing
1. Disable network and attempt sync
2. Verify operations queued
3. Enable network
4. Verify automatic retry
5. Revoke permissions and attempt sync
6. Verify operations queued
7. Grant permissions
8. Verify automatic retry

## Configuration

### Retry Strategy
- Modify `SyncRetryStrategy.MAX_RETRY_ATTEMPTS` to change max retries
- Modify `SyncRetryStrategy.BASE_DELAY_MS` to change initial delay
- Modify `SyncRetryStrategy.MAX_DELAY_MS` to change maximum delay

### Worker Schedule
- Modify `SyncQueueWorker` repeat interval (default: 15 minutes)
- Modify network constraints (default: CONNECTED)
- Modify backoff policy (default: EXPONENTIAL)

## Requirements Validation

✅ **Requirement 8.1**: Handle permission denial
- Permissions checked before sync
- Operations queued when denied
- User prompted to grant permissions

✅ **Requirement 8.3**: Handle network errors
- Network errors caught and classified
- Operations queued for retry
- User notified of network issues

✅ **Requirement 8.3**: Implement retry with exponential backoff
- Exponential backoff implemented (1s, 2s, 4s, 8s, 16s)
- Max 5 retry attempts
- Retryable vs non-retryable error classification

✅ **Requirement 8.3**: Add offline sync queue
- Sync queue database table created
- Operations queued when offline
- Automatic processing when online
- WorkManager integration for periodic processing

## Future Enhancements

1. **User Notification**: Notify user when sync queue has pending items
2. **Manual Retry**: Add UI button to manually trigger sync queue processing
3. **Sync Status**: Show sync status for each reminder (synced, pending, failed)
4. **Conflict Resolution**: Handle conflicts when reminder updated while sync pending
5. **Batch Processing**: Optimize by batching multiple operations
6. **Analytics**: Track sync success/failure rates
