# Pattern Analysis Implementation

## Overview
Implemented the PatternAnalyzer system for analyzing user behavior patterns and generating ML-based suggestions. This is part of the ML Suggestions feature (Requirements 12.1-12.4, 12.8).

## Implementation Details

### 1. PatternAnalyzer Interface
**File:** `app/src/main/java/com/nami/peace/domain/ml/PatternAnalyzer.kt`

Created the interface with four analysis methods:
- `analyzeCompletionPatterns()` - Analyzes when tasks are most likely to be completed on time
- `analyzePriorityPatterns()` - Identifies tasks with incorrect priority levels based on completion behavior
- `analyzeRecurringPatterns()` - Detects manually created tasks that should be recurring
- `analyzeFocusSessions()` - Identifies long work sessions and suggests break reminders

### 2. PatternAnalyzerImpl
**File:** `app/src/main/java/com/nami/peace/domain/ml/PatternAnalyzerImpl.kt`

Implemented comprehensive pattern analysis algorithms:

#### Completion Time Analysis
- Groups events by reminder ID and analyzes completion times by hour
- Calculates completion rates for each hour (on-time completion within 15 minutes)
- Suggests optimal scheduling times when completion rate >= 70%
- Generates confidence scores based on completion consistency

#### Priority Pattern Analysis
- Analyzes average completion delays for each reminder
- Detects mismatched priorities:
  - HIGH priority tasks completed late (>2 hours) → suggest MEDIUM
  - MEDIUM priority tasks completed very late (>4 hours) → suggest LOW
  - LOW priority tasks completed early (<30 min) → suggest MEDIUM
  - MEDIUM priority tasks completed early (<-1 hour) → suggest HIGH
- Calculates confidence based on delay variance (consistency)

#### Recurring Pattern Detection
- Groups non-recurring tasks by similar titles (case-insensitive)
- Detects patterns when same task created 3+ times in 7 days
- Calculates average interval between completions
- Suggests recurrence type based on interval:
  - <6 hours → HOURLY
  - <30 hours → DAILY
  - <200 hours → WEEKLY
  - else → MONTHLY
- Confidence based on interval consistency

#### Focus Session Analysis
- Identifies clusters of task completions (max 30-minute gaps)
- Calculates average focus session duration
- Suggests break intervals for sessions >= 2 hours:
  - >=4 hours → 60-minute breaks
  - >=3 hours → 90-minute breaks
  - >=2 hours → 120-minute breaks
- Confidence increases with number of detected sessions

### 3. Dependency Injection
**File:** `app/src/main/java/com/nami/peace/di/AppModule.kt`

Added MLModule to bind PatternAnalyzer interface to implementation:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class MLModule {
    @Binds
    @Singleton
    abstract fun bindPatternAnalyzer(
        patternAnalyzerImpl: PatternAnalyzerImpl
    ): PatternAnalyzer
}
```

## Key Features

### Minimum Data Requirements
- Requires minimum 7 completion events for analysis
- Requires minimum 3 completions per reminder for individual patterns
- Requires minimum 3 occurrences for recurring pattern detection
- Requires minimum 2-hour sessions for focus session suggestions

### Confidence Scoring
- All suggestions include confidence scores (0-100)
- Minimum confidence threshold of 50 for suggestion generation
- Confidence based on:
  - Completion rate consistency
  - Delay variance
  - Interval consistency
  - Number of detected patterns

### Data Structures
Created serializable suggestion data classes:
- `OptimalTimeSuggestion` - Current vs suggested hour, completion rate
- `PriorityAdjustmentSuggestion` - Current vs suggested priority, average delay
- `RecurringPatternSuggestion` - Task title, occurrences, interval, recurrence type
- `FocusSessionSuggestion` - Average session duration, suggested break interval

## Requirements Validated

✅ **Requirement 12.1** - Analyzes user behavior patterns (completion times, priorities, recurrence)
✅ **Requirement 12.2** - Suggests optimal scheduling times with confidence scores
✅ **Requirement 12.3** - Suggests priority adjustments based on completion patterns
✅ **Requirement 12.4** - Suggests converting manual tasks to recurring
✅ **Requirement 12.8** - Suggests optimal focus session durations and break intervals

## Technical Decisions

1. **Statistical Approach**: Uses simple statistical analysis (averages, variance) rather than complex ML models for on-device processing
2. **Confidence Thresholds**: Set minimum thresholds to avoid low-quality suggestions
3. **Time Windows**: Analyzes last 90 days of data (managed by CompletionEventRepository)
4. **Pattern Detection**: Uses case-insensitive title matching for recurring pattern detection
5. **JSON Serialization**: Stores suggestion data as JSON for flexibility in UI presentation

## Build Status
✅ Compilation successful
✅ All dependencies properly injected
✅ Ready for integration with SuggestionGenerator (Task 60)

## Next Steps
1. Implement SuggestionGenerator (Task 60) to create Suggestion entities from analysis results
2. Implement LearningRepository (Task 61) to track suggestion acceptance/dismissal
3. Create ML suggestions UI (Task 62) to display suggestions to users
4. Implement background analysis worker (Task 63) for daily pattern analysis

## Files Created
- `app/src/main/java/com/nami/peace/domain/ml/PatternAnalyzer.kt`
- `app/src/main/java/com/nami/peace/domain/ml/PatternAnalyzerImpl.kt`

## Files Modified
- `app/src/main/java/com/nami/peace/di/AppModule.kt` - Added MLModule for dependency injection
