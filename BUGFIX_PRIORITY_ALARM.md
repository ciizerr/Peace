# Bug Fix: Multiple Simultaneous Alarms with Priority Sorting

## Problem Description
When multiple reminders were scheduled at the same time with different priorities, only the first alarm would trigger and display. After marking it as done, the other reminders remained untouched and were not marked as complete.

**Expected Behavior:**
- All reminders scheduled at the same time should be bundled together
- They should be displayed in priority order (HIGH → MEDIUM → LOW)
- Marking as "I'm doing it" should complete ALL bundled reminders

**Actual Behavior:**
- Only the first reminder triggered
- Other simultaneous reminders were ignored
- Only the displayed reminder was marked as complete

## Root Cause
The alarm system was designed to handle one reminder at a time:
1. `AlarmReceiver` only processed the single reminder ID from the intent
2. `ReminderService` only showed notification for one reminder
3. `AlarmActivity` only displayed one reminder
4. Completion action only marked one reminder as done

## Solution Implemented

### 1. AlarmReceiver.kt - Bundling Detection
**Changes:**
- When `ACTION_ALARM_TRIGGER` fires, the receiver now queries all incomplete reminders
- Finds all reminders within a 1-minute time window of the triggered reminder
- Filters for enabled, non-completed reminders
- Sorts by priority (HIGH=0, MEDIUM=1, LOW=2)
- Passes bundled reminder IDs to `ReminderService`

```kotlin
// Find all reminders due at the same time (within 1 minute window)
val allReminders = repository.getIncompleteReminders()
val timeWindow = 60 * 1000L // 1 minute
val bundledReminderIds = allReminders
    .filter { 
        kotlin.math.abs(it.startTimeInMillis - reminder.startTimeInMillis) < timeWindow &&
        !it.isCompleted && it.isEnabled
    }
    .sortedBy { it.priority.ordinal } // HIGH=0, MEDIUM=1, LOW=2
    .map { it.id }
```

### 2. ReminderService.kt - Bundle Propagation
**Changes:**
- Modified `onStartCommand` to accept bundled reminder IDs
- Updated `showNotification` to pass bundled IDs to `AlarmActivity`
- Notification now includes all bundled reminder IDs in the full-screen intent

### 3. AlarmActivity.kt - Multi-Reminder Display
**Major Changes:**

#### New ViewModel: `BundledAlarmViewModel`
- Loads all bundled reminders from the database
- Sorts them by priority for display

#### New Composable: `AlarmScreenWithViewModel`
- Receives bundled reminder IDs from intent
- Uses ViewModel to load full reminder objects
- Shows loading state while fetching data

#### New Composable: `AlarmScreenMultiple`
- Displays all reminders in a scrollable list
- Shows count when multiple reminders exist
- Uses highest priority for gradient color scheme
- Each reminder shown in a card with priority indicator

#### New Composable: `ReminderCard`
- Individual reminder display with title and priority
- Color-coded priority strip (RED=High, BLUE=Medium, GREEN=Low)

#### Updated Action Handling: `sendActionForAll`
- Broadcasts completion/snooze action for ALL bundled reminders
- Ensures all simultaneous reminders are processed together

## Testing Scenarios

### Test Case 1: Three Simultaneous Alarms
**Setup:**
- Alarm 1: 3:30 PM, Medium Priority
- Alarm 2: 3:30 PM, High Priority  
- Alarm 3: 3:30 PM, Low Priority

**Expected Result:**
- Alarm screen shows all 3 reminders
- Order: High → Medium → Low
- "I'M DOING IT" marks all 3 as complete

### Test Case 2: Single Alarm
**Setup:**
- Alarm 1: 4:00 PM, High Priority

**Expected Result:**
- Alarm screen shows 1 reminder
- Works exactly as before
- No regression in single-alarm behavior

### Test Case 3: Time Window Edge Case
**Setup:**
- Alarm 1: 5:00:00 PM
- Alarm 2: 5:00:45 PM (45 seconds later)

**Expected Result:**
- Both alarms bundled (within 1-minute window)
- Both displayed and completed together

## Files Modified

1. **app/src/main/java/com/nami/peace/scheduler/AlarmReceiver.kt**
   - Added bundling logic in `ACTION_ALARM_TRIGGER` handler

2. **app/src/main/java/com/nami/peace/scheduler/ReminderService.kt**
   - Updated `onStartCommand` to accept bundled IDs
   - Modified `showNotification` signature

3. **app/src/main/java/com/nami/peace/ui/alarm/AlarmActivity.kt**
   - Added `BundledAlarmViewModel`
   - Created `AlarmScreenWithViewModel` composable
   - Created `AlarmScreenMultiple` composable
   - Created `ReminderCard` composable
   - Updated action handling to process all bundled reminders

4. **app/src/main/java/com/nami/peace/ui/home/HomeViewModel.kt**
   - Updated "Next Up" logic to prioritize by both time AND priority
   - When multiple reminders scheduled at same time, highest priority shown in hero section

## Benefits

1. **Correct Priority Handling**: High-priority reminders always shown first (both in alarm screen AND home screen hero section)
2. **No Lost Reminders**: All simultaneous reminders are processed
3. **Better UX**: Users see all due tasks at once
4. **Efficient Completion**: One action completes all bundled tasks
5. **Backward Compatible**: Single reminders work exactly as before
6. **Smart Hero Section**: "Next Up" now shows the highest priority reminder when multiple reminders are scheduled at the same time

## Notes

- The 1-minute time window accounts for slight scheduling variations
- Priority sorting uses enum ordinal (HIGH=0, MEDIUM=1, LOW=2)
- All bundled reminders share the same snooze/complete action
- The UI adapts: shows count for multiple, simpler view for single reminder
