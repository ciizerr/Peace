# Hero Section Priority Fix

## Problem
The "Next Up" hero section on the home screen only showed the earliest reminder by time, without considering priority. If you had:
- 3:30 PM - Medium Priority Task
- 3:30 PM - High Priority Task
- 3:30 PM - Low Priority Task

The hero section would show whichever was created first, not the highest priority one.

## Solution
Updated `HomeViewModel` to use smart priority selection:

### New Logic
1. **Find earliest time** among enabled reminders
2. **Identify simultaneous reminders** (within 1-minute window)
3. **Select highest priority** from simultaneous reminders
4. **Display in hero section**

### Code Implementation
```kotlin
val enabledReminders = activeList.filter { it.isEnabled }
val nextUp = if (enabledReminders.isNotEmpty()) {
    // Get the earliest time
    val earliestTime = enabledReminders.first().startTimeInMillis
    val timeWindow = 60 * 1000L // 1 minute window
    
    // Find all reminders within the time window of the earliest
    val simultaneousReminders = enabledReminders.filter { 
        kotlin.math.abs(it.startTimeInMillis - earliestTime) < timeWindow
    }
    
    // If multiple reminders at same time, pick highest priority
    // Priority enum: HIGH=0, MEDIUM=1, LOW=2 (lower ordinal = higher priority)
    simultaneousReminders.minByOrNull { it.priority.ordinal }
} else {
    null
}
```

## Examples

### Example 1: Same Time, Different Priorities
**Reminders:**
- 3:30 PM - "Workout" - LOW
- 3:30 PM - "Take Medicine" - HIGH
- 3:30 PM - "Check Email" - MEDIUM

**Hero Section Shows:** "Take Medicine" (HIGH priority)

### Example 2: Different Times
**Reminders:**
- 3:00 PM - "Task A" - LOW
- 4:00 PM - "Task B" - HIGH

**Hero Section Shows:** "Task A" (earliest time, even though lower priority)

### Example 3: Within Time Window
**Reminders:**
- 3:30:00 PM - "Task A" - MEDIUM
- 3:30:45 PM - "Task B" - HIGH (45 seconds later)

**Hero Section Shows:** "Task B" (within 1-minute window, higher priority)

## Benefits

1. **Priority Awareness**: Users see their most important upcoming task
2. **Consistent with Alarm Screen**: Same priority logic used everywhere
3. **Smart Bundling**: Handles simultaneous reminders intelligently
4. **Time-First Approach**: Still respects chronological order when priorities are equal

## Visual Impact

### Before Fix
```
┌─────────────────────────┐
│ Next Up                 │
│                         │
│ 03:30 PM               │
│ Check Email            │  ← Medium (shown first)
│ Repetition 1/1         │
└─────────────────────────┘
```

### After Fix
```
┌─────────────────────────┐
│ Next Up                 │
│                         │
│ 03:30 PM               │
│ Take Medicine          │  ← High (highest priority)
│ Repetition 1/1         │
└─────────────────────────┘
```

## Testing

### Test Scenario
1. Create 3 reminders at 3:30 PM:
   - "Low Task" - LOW priority
   - "Medium Task" - MEDIUM priority
   - "High Task" - HIGH priority

2. Go to home screen

3. **Expected**: Hero section shows "High Task"

4. Disable "High Task"

5. **Expected**: Hero section now shows "Medium Task"

6. Disable "Medium Task"

7. **Expected**: Hero section now shows "Low Task"

## Edge Cases Handled

1. **All same priority**: Shows first by time
2. **One disabled**: Skips disabled, shows next highest priority
3. **Time window boundary**: 1-minute tolerance for "simultaneous"
4. **No enabled reminders**: Hero section hidden (null)

## File Modified
- `app/src/main/java/com/nami/peace/ui/home/HomeViewModel.kt`
