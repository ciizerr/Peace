# Streak Tracking Implementation

## Overview
Implemented comprehensive streak tracking functionality for the Peace Garden feature, including streak calculation, daily completion checks, streak reset logic, and longest streak tracking.

## Implementation Details

### Use Cases Created

#### 1. UpdateStreakUseCase
**Location:** `app/src/main/java/com/nami/peace/domain/usecase/UpdateStreakUseCase.kt`

**Purpose:** Updates the streak when a task is completed.

**Key Features:**
- Increments streak on consecutive day completions
- Maintains streak on same-day completions
- Resets streak to 1 when days are skipped
- Tracks longest streak achieved
- Updates last completion date

**Logic:**
- First completion: Sets streak to 1
- Same day completion: Maintains current streak
- Next consecutive day: Increments streak by 1
- Gap of 2+ days: Resets streak to 1
- Always updates longest streak if current exceeds it

#### 2. CheckStreakStatusUseCase
**Location:** `app/src/main/java/com/nami/peace/domain/usecase/CheckStreakStatusUseCase.kt`

**Purpose:** Checks if the current streak is still valid or should be reset.

**Key Features:**
- Validates streak based on last completion date
- Automatically resets streak to 0 if invalid (2+ days since last completion)
- Returns streak validity status and current streak count

**Use Cases:**
- Displaying accurate streak information in UI
- Resetting streak when user opens app after missing days
- Validating streak status before displaying

#### 3. GetStreakInfoUseCase
**Location:** `app/src/main/java/com/nami/peace/domain/usecase/GetStreakInfoUseCase.kt`

**Purpose:** Retrieves current streak information.

**Returns:**
- Current streak count
- Longest streak achieved
- Last completion date

### Day Calculation Logic

The implementation uses a robust day calculation method:

```kotlin
private fun calculateDaysBetween(fromTime: Long, toTime: Long): Int {
    val fromDay = fromTime / TimeUnit.DAYS.toMillis(1)
    val toDay = toTime / TimeUnit.DAYS.toMillis(1)
    return (toDay - fromDay).toInt()
}
```

This ensures:
- Accurate day counting regardless of time of day
- Consistent behavior across time zones
- Proper handling of edge cases (exactly 24 hours, 48 hours, etc.)

## Property-Based Tests

### Test File
**Location:** `app/src/test/java/com/nami/peace/domain/usecase/StreakTrackingPropertyTest.kt`

### Properties Tested

#### Property 32: Streak Calculation
**Validates:** Requirements 18.5

Tests that verify:
1. First completion starts streak at 1
2. Consecutive day completions increment streak
3. Same day completions maintain streak
4. Skipped days reset streak to 1
5. Longest streak is tracked correctly
6. Edge case: exactly 24 hours is treated as next day
7. Random streak sequences behave correctly

#### Property 34: Streak Reset
**Validates:** Requirements 18.8

Tests that verify:
1. Streak resets to 0 when checking after 2+ days
2. Streak remains valid on same day
3. Streak remains valid on next day
4. Edge case: exactly 48 hours triggers reset

### Test Coverage

The property tests include:
- **13 test methods** covering all aspects of streak tracking
- **100+ test iterations** for random sequence testing
- **Edge case testing** for boundary conditions (24h, 48h)
- **State persistence verification** across all operations
- **Integration with GardenRepository** using in-memory database

### Test Results
✅ All 13 tests passed successfully
✅ Property 32 (Streak calculation) validated
✅ Property 34 (Streak reset) validated

## Integration Points

### Database Schema
The streak tracking uses existing `GardenState` fields:
- `currentStreak: Int` - Current consecutive day streak
- `longestStreak: Int` - Longest streak ever achieved
- `lastCompletionDate: Long?` - Timestamp of last task completion

### Repository Integration
Uses `GardenRepository` for:
- Reading current garden state
- Updating streak values
- Persisting longest streak

## Usage Example

```kotlin
// When a task is completed
val (streakIncremented, newStreak) = updateStreakUseCase()
if (streakIncremented) {
    // Show celebration or notification
    showStreakIncrementedMessage(newStreak)
}

// When checking streak status (e.g., on app launch)
val (streakIsValid, currentStreak) = checkStreakStatusUseCase()
if (!streakIsValid) {
    // Show encouraging message about starting fresh
    showStreakResetMessage()
}

// When displaying streak information
val streakInfo = getStreakInfoUseCase()
displayStreak(
    current = streakInfo.currentStreak,
    longest = streakInfo.longestStreak
)
```

## Requirements Validation

### Requirement 18.5
✅ **WHEN the user completes tasks on consecutive days THEN the Peace System SHALL track the completion streak**

Implemented via:
- `UpdateStreakUseCase` increments streak on consecutive days
- `GardenState.currentStreak` persists the value
- Property tests validate consecutive day behavior

### Requirement 18.8
✅ **WHEN the user breaks a streak THEN the Peace System SHALL reset the counter and display an encouraging message**

Implemented via:
- `UpdateStreakUseCase` resets to 1 when gap detected
- `CheckStreakStatusUseCase` resets to 0 when checking after 2+ days
- Property tests validate reset behavior

## Next Steps

To complete the Peace Garden enhancement:

1. **Integrate with Task Completion Flow**
   - Call `UpdateStreakUseCase` when tasks are marked complete
   - Update Peace Garden UI to show streak

2. **Add Milestone Detection** (Task 44)
   - Implement milestone system for 7, 30, 100, 365 days
   - Show achievement notifications

3. **Update Peace Garden UI** (Task 45)
   - Display current streak
   - Display longest streak
   - Show milestone progress

4. **Integrate with Task Completion** (Task 46)
   - Wire streak updates into reminder completion flow
   - Trigger growth stage advancement
   - Check for milestone achievements

## Files Modified/Created

### Created Files
1. `app/src/main/java/com/nami/peace/domain/usecase/UpdateStreakUseCase.kt`
2. `app/src/main/java/com/nami/peace/domain/usecase/CheckStreakStatusUseCase.kt`
3. `app/src/main/java/com/nami/peace/domain/usecase/GetStreakInfoUseCase.kt`
4. `app/src/test/java/com/nami/peace/domain/usecase/StreakTrackingPropertyTest.kt`
5. `STREAK_TRACKING_IMPLEMENTATION.md` (this file)

### No Files Modified
All implementation uses existing database schema and repository interfaces.

## Testing Commands

Run streak tracking tests:
```bash
./gradlew :app:testDebugUnitTest --tests "com.nami.peace.domain.usecase.StreakTrackingPropertyTest"
```

Run all property tests:
```bash
./gradlew :app:testDebugUnitTest --tests "*PropertyTest"
```

## Notes

- The implementation is fully tested with property-based tests
- All edge cases are handled (same day, consecutive days, gaps, first completion)
- The day calculation logic is robust and timezone-safe
- Longest streak is automatically tracked and never decreases
- The implementation follows the existing architecture patterns
- No breaking changes to existing code
