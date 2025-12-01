# ML Suggestions System Guide 🤖

This guide provides a comprehensive overview of Peace's intelligent ML suggestion system, how it works, and how to get the most value from it.

## Table of Contents

1. [Overview](#overview)
2. [How It Works](#how-it-works)
3. [Data Collection](#data-collection)
4. [Pattern Analysis](#pattern-analysis)
5. [Suggestion Types](#suggestion-types)
6. [Confidence Scores](#confidence-scores)
7. [Learning System](#learning-system)
8. [Privacy & Security](#privacy--security)
9. [Best Practices](#best-practices)
10. [Troubleshooting](#troubleshooting)

---

## Overview

Peace's ML suggestion system is an on-device machine learning engine that analyzes your task completion patterns and provides personalized recommendations to optimize your productivity.

### Key Features

- **100% On-Device**: All analysis happens locally on your device
- **Privacy-First**: No data sent to external servers
- **Adaptive Learning**: Improves based on your feedback
- **Confidence Scoring**: Each suggestion includes reliability percentage
- **7 Suggestion Types**: Covers time optimization, priorities, habits, and more
- **Minimum Data Requirement**: Needs 7 days of usage before generating suggestions

### Philosophy

The ML system follows Peace's core philosophy of "Calm Engagement":
- Non-intrusive suggestions
- Respects your workflow
- Learns from your behavior, not prescriptive rules
- Helps you work smarter, not harder

---

## How It Works

### The ML Pipeline

```
User Completes Tasks
        ↓
Data Collection (CompletionEvents)
        ↓
Pattern Analysis (Daily Background Job)
        ↓
Suggestion Generation (With Confidence Scores)
        ↓
User Feedback (Apply/Dismiss)
        ↓
Learning & Adaptation
```

### Workflow

1. **Usage Phase** (Days 1-7): Peace collects completion data
2. **Analysis Phase** (Day 7+): Background analysis runs daily
3. **Suggestion Phase**: Suggestions appear in the Suggestions screen
4. **Feedback Phase**: You apply or dismiss suggestions
5. **Learning Phase**: System adapts based on your choices

---

## Data Collection

### What Data Is Collected?

Peace tracks **CompletionEvents** when you complete tasks:

```kotlin
CompletionEvent {
    reminderId: Int           // Which task was completed
    completedAt: Long         // When it was completed
    scheduledFor: Long        // When it was scheduled
    priority: Priority        // Task priority level
    category: Category        // Task category
    wasOnTime: Boolean        // Completed before/after scheduled time
    completionDelay: Long     // How late (if any)
}
```

### Data Retention

- **Storage Duration**: Last 90 days only
- **Automatic Cleanup**: Older data is automatically deleted
- **Local Storage**: Stored in Room database on your device
- **No Cloud Sync**: Never leaves your device

### What Is NOT Collected

- Task titles or descriptions
- Notes or attachments
- Personal information
- Location data
- Device identifiers
- Usage outside the app

---

## Pattern Analysis

### Analysis Schedule

- **Frequency**: Once per day
- **Timing**: Background job during low-usage periods
- **Duration**: Typically 5-15 seconds
- **Timeout**: 30 seconds maximum
- **Battery Impact**: Minimal (uses WorkManager)

### Analysis Types

#### 1. Completion Time Analysis

Identifies when you typically complete specific tasks:

```
Example:
- "Gym workout" completed at 7:00am (15 times)
- "Gym workout" completed at 7:15am (12 times)
- "Gym workout" completed at 6:45am (8 times)

Pattern: You prefer morning workouts around 7am
Suggestion: Schedule "Gym workout" at 7:00am
```

#### 2. Priority Pattern Analysis

Detects mismatches between priority settings and actual behavior:

```
Example:
- "Team standup" set as Medium priority
- But you complete it first 90% of the time
- And you're never late to it

Pattern: You treat it as high priority in practice
Suggestion: Change priority to High
```

#### 3. Recurring Pattern Detection

Finds manually repeated tasks that should be recurring:

```
Example:
- "Water plants" created 4 times in last 30 days
- Always on Sundays
- Similar time (around 10am)

Pattern: Weekly manual task creation
Suggestion: Convert to recurring reminder (every Sunday at 10am)
```

#### 4. Focus Session Analysis

Analyzes work session durations and effectiveness:

```
Example:
- Work sessions averaging 90 minutes
- Followed by 10-minute breaks
- High completion rate during these sessions

Pattern: 90-minute focus sessions work well for you
Suggestion: Structure tasks in 90-minute blocks
```

### Minimum Data Requirements

Each analysis type requires minimum data:

| Analysis Type | Minimum Completions | Minimum Days |
|---------------|---------------------|--------------|
| Completion Time | 5 completions | 7 days |
| Priority Pattern | 10 completions | 14 days |
| Recurring Pattern | 3 repetitions | 21 days |
| Focus Session | 5 sessions | 7 days |

---

## Suggestion Types

### 1. Optimal Time Suggestions

**Purpose**: Recommend better scheduling times based on completion patterns

**Example**:
```
Title: "Optimize 'Morning Meditation' timing"
Description: "You typically complete this task at 6:30am. 
Consider scheduling it at that time for better consistency."
Confidence: 85%
```

**When Generated**:
- Task completed at consistent times (±30 minutes)
- At least 5 completions
- Current schedule differs from typical completion time

**Application Effect**:
- Updates reminder's scheduled time
- Maintains all other settings
- Records feedback for learning

### 2. Priority Adjustment Suggestions

**Purpose**: Align priority settings with actual behavior

**Example**:
```
Title: "Increase priority for 'Client Review'"
Description: "You complete this task first 80% of the time 
and are never late. Consider marking it High priority."
Confidence: 78%
```

**When Generated**:
- Completion order doesn't match priority
- Consistently completed early or first
- At least 10 completions

**Application Effect**:
- Updates reminder's priority level
- Affects notification styling
- Influences future suggestions

### 3. Recurring Pattern Suggestions

**Purpose**: Convert manually repeated tasks to recurring reminders

**Example**:
```
Title: "Make 'Weekly Report' recurring"
Description: "You've created this task 4 times in the last month, 
always on Fridays. Make it a recurring reminder?"
Confidence: 92%
```

**When Generated**:
- Similar task titles created multiple times
- Consistent day/time pattern
- At least 3 repetitions in 30 days

**Application Effect**:
- Converts to recurring reminder
- Sets recurrence pattern automatically
- Deletes duplicate manual entries

### 4. Break Reminder Suggestions

**Purpose**: Prevent burnout with timely break suggestions

**Example**:
```
Title: "Time for a break"
Description: "You've been focused for 2 hours. 
Research shows 10-minute breaks improve productivity."
Confidence: 70%
```

**When Generated**:
- Continuous focus session exceeds 90 minutes
- No break taken in last 2 hours
- Based on general productivity research

**Application Effect**:
- Creates a break reminder
- Schedules for immediate future
- Suggests 10-minute duration

### 5. Habit Formation Suggestions

**Purpose**: Encourage and reinforce emerging habits

**Example**:
```
Title: "You're building a habit!"
Description: "You've completed 'Morning Pages' 7 days in a row. 
Keep it up! Consider making this a permanent habit."
Confidence: 88%
```

**When Generated**:
- Task completed on consecutive days (7+ days)
- Consistent time pattern
- High completion rate

**Application Effect**:
- Provides encouragement
- Suggests making task recurring
- Tracks habit streak

### 6. Template Creation Suggestions

**Purpose**: Streamline creation of similar tasks

**Example**:
```
Title: "Create template for 'Client Meeting'"
Description: "You often create similar client meeting tasks. 
A template would save time."
Confidence: 75%
```

**When Generated**:
- Similar task patterns detected
- Common subtasks or notes
- At least 5 similar tasks created

**Application Effect**:
- Creates reusable template
- Pre-fills common fields
- Saves time on future creation

### 7. Focus Session Optimization

**Purpose**: Recommend optimal work session durations

**Example**:
```
Title: "Optimize focus session length"
Description: "Your most productive sessions are 90 minutes long. 
Try structuring work in 90-minute blocks."
Confidence: 82%
```

**When Generated**:
- Multiple focus sessions tracked
- Clear pattern in effective durations
- At least 5 sessions analyzed

**Application Effect**:
- Suggests session duration
- Can create focus session reminders
- Personalizes to your rhythm

---

## Confidence Scores

### What Are Confidence Scores?

Confidence scores (0-100%) indicate how reliable a suggestion is based on:
- Amount of data analyzed
- Consistency of patterns
- Statistical significance
- Historical accuracy

### Score Ranges

| Range | Interpretation | Recommendation |
|-------|----------------|----------------|
| 90-100% | Very High Confidence | Strongly consider applying |
| 75-89% | High Confidence | Likely beneficial |
| 60-74% | Moderate Confidence | Review carefully |
| 40-59% | Low Confidence | Use caution |
| 0-39% | Very Low Confidence | Probably not useful |

### How Scores Are Calculated

```kotlin
confidenceScore = baseConfidence 
    × dataQualityFactor 
    × consistencyFactor 
    × historicalAccuracyFactor
```

**Factors**:

1. **Base Confidence**: Inherent reliability of the analysis type
   - Completion time: 80%
   - Priority pattern: 75%
   - Recurring pattern: 85%
   - Focus session: 70%

2. **Data Quality Factor**: Based on amount of data
   - Minimum data: 0.5×
   - Adequate data: 1.0×
   - Abundant data: 1.2×

3. **Consistency Factor**: Pattern consistency
   - High variance: 0.7×
   - Moderate variance: 1.0×
   - Low variance: 1.3×

4. **Historical Accuracy**: Past suggestion success rate
   - New user: 1.0× (neutral)
   - High acceptance: 1.2×
   - Low acceptance: 0.8×

### Example Calculation

```
Suggestion: Optimal time for "Gym workout"
- Base confidence: 80%
- Data quality: 15 completions (adequate) = 1.0×
- Consistency: Always 7am ±15min (low variance) = 1.3×
- Historical: 85% acceptance rate = 1.1×

Final Score: 80% × 1.0 × 1.3 × 1.1 = 114.4% → capped at 100%
```

---

## Learning System

### How Learning Works

The system learns from your feedback on suggestions:

```
Apply Suggestion → Positive Feedback → Increase similar suggestions
Dismiss Suggestion → Negative Feedback → Decrease similar suggestions
```

### Feedback Storage

```kotlin
SuggestionFeedback {
    suggestionId: Int
    suggestionType: SuggestionType
    action: FeedbackAction (APPLIED/DISMISSED)
    timestamp: Long
    reminderId: Int?
}
```

### Adaptation Mechanisms

#### 1. Type-Level Learning

Tracks acceptance rate per suggestion type:

```
Example:
- Optimal Time suggestions: 85% acceptance
- Priority Adjustment suggestions: 45% acceptance

Adaptation:
- Generate more Optimal Time suggestions
- Generate fewer Priority Adjustment suggestions
- Increase confidence for Optimal Time
- Decrease confidence for Priority Adjustment
```

#### 2. Pattern-Level Learning

Learns which specific patterns you value:

```
Example:
- Morning time suggestions: 90% acceptance
- Evening time suggestions: 30% acceptance

Adaptation:
- Prioritize morning-related patterns
- Reduce evening-related suggestions
```

#### 3. Threshold Adjustment

Adjusts minimum confidence thresholds:

```
Example:
- You accept suggestions with 60%+ confidence
- You dismiss suggestions with <60% confidence

Adaptation:
- Only show suggestions with 60%+ confidence
- Filter out lower-confidence suggestions
```

### Learning Timeline

- **Week 1**: Baseline (no adaptation)
- **Week 2-4**: Initial learning (small adjustments)
- **Month 2+**: Mature learning (significant personalization)
- **Month 6+**: Highly personalized (optimal for your workflow)

---

## Privacy & Security

### On-Device Processing

**Everything happens locally**:
- Data collection: Local database
- Pattern analysis: On-device computation
- Suggestion generation: Local algorithms
- Learning: Local feedback storage

**No external communication**:
- No API calls for ML
- No cloud processing
- No data uploads
- No telemetry

### Data Minimization

**Only essential data collected**:
- Completion timestamps
- Priority and category
- Timing information

**Not collected**:
- Task content
- Personal information
- Location
- Contacts

### User Control

**Full control over ML features**:
- Toggle ML suggestions on/off in Settings
- Disable stops all data collection
- Delete all ML data (clear app data)
- No penalty for disabling

### Security Measures

- **Local Storage**: SQLite database with app-private access
- **No Encryption Needed**: Data never leaves device
- **Automatic Cleanup**: Old data auto-deleted after 90 days
- **No Identifiers**: No user IDs or device IDs stored

---

## Best Practices

### Getting Started

1. **Use Peace Normally**: Don't change behavior for ML
2. **Wait 7 Days**: Let system collect baseline data
3. **Review Suggestions**: Check suggestions screen after day 7
4. **Start with High Confidence**: Apply suggestions with 80%+ confidence first
5. **Provide Feedback**: Apply or dismiss—don't ignore

### Maximizing Value

1. **Be Consistent**: Regular usage improves pattern detection
2. **Complete Tasks**: Completion data is the foundation
3. **Use Categories**: Helps system understand context
4. **Set Priorities**: Enables priority pattern analysis
5. **Review Weekly**: Check suggestions once per week

### Feedback Strategy

**When to Apply**:
- High confidence (75%+)
- Suggestion makes intuitive sense
- Aligns with your goals
- Low effort to implement

**When to Dismiss**:
- Low confidence (<60%)
- Doesn't fit your workflow
- Based on temporary patterns
- Conflicts with your preferences

**Don't Ignore**:
- Ignoring suggestions doesn't provide feedback
- System can't learn from non-action
- Always apply or dismiss

### Optimization Tips

1. **Focus on One Type**: Start with one suggestion type (e.g., optimal times)
2. **Track Results**: Note if applied suggestions help
3. **Adjust Gradually**: Don't apply all suggestions at once
4. **Give It Time**: Allow 2-3 weeks to see benefits
5. **Iterate**: Dismiss what doesn't work, apply what does

---

## Troubleshooting

### No Suggestions Appearing

**Possible Causes**:
- Less than 7 days of usage
- ML suggestions disabled in Settings
- Insufficient completion data
- No clear patterns detected

**Solutions**:
1. Check Settings → Feature Settings → ML Suggestions (enabled?)
2. Verify you've used Peace for 7+ days
3. Complete more tasks to build data
4. Check if analysis worker is running (Settings → About)

### Low Confidence Scores

**Possible Causes**:
- Inconsistent patterns
- Limited data
- High variance in behavior
- New user (no historical accuracy)

**Solutions**:
1. Use Peace more consistently
2. Complete tasks at similar times
3. Wait for more data accumulation
4. Focus on high-confidence suggestions only

### Irrelevant Suggestions

**Possible Causes**:
- Temporary patterns detected
- Insufficient learning data
- Unusual usage period analyzed

**Solutions**:
1. Dismiss irrelevant suggestions (helps learning)
2. Continue using Peace normally
3. System will adapt over 2-4 weeks
4. Check if patterns have changed recently

### Suggestions Not Updating

**Possible Causes**:
- Background analysis not running
- Battery optimization blocking worker
- Analysis timeout
- No new patterns detected

**Solutions**:
1. Open app to trigger analysis
2. Check battery optimization settings
3. Verify background data is enabled
4. Wait 24 hours for next analysis cycle

### Applied Suggestion Didn't Help

**Response**:
1. Revert the change manually
2. Dismiss similar future suggestions
3. System will learn from this feedback
4. Try suggestions with higher confidence next time

---

## Advanced Topics

### Suggestion Algorithm Details

For developers and curious users, here's how suggestions are generated:

#### Optimal Time Algorithm

```
1. Group completions by task
2. Calculate completion time distribution
3. Find mode (most common time)
4. Calculate variance
5. If variance < 30 minutes AND count >= 5:
   - Generate suggestion
   - Confidence = f(count, variance, historical)
```

#### Priority Pattern Algorithm

```
1. Track completion order for each priority level
2. Calculate "effective priority" from behavior
3. Compare to set priority
4. If mismatch > 1 level AND consistent:
   - Generate suggestion
   - Confidence = f(consistency, count, historical)
```

#### Recurring Pattern Algorithm

```
1. Extract task title keywords
2. Find similar titles in history
3. Analyze temporal patterns (day, time)
4. If pattern detected (3+ occurrences):
   - Generate suggestion
   - Confidence = f(similarity, consistency, count)
```

### Performance Optimization

The ML system is optimized for performance:

- **Lazy Loading**: Analysis only when needed
- **Incremental Processing**: Processes new data only
- **Caching**: Caches intermediate results
- **Timeout Protection**: 30-second maximum analysis time
- **Background Execution**: Uses WorkManager for efficiency

### Future Enhancements

Planned improvements:

- **More Suggestion Types**: Collaboration patterns, energy levels
- **Improved Confidence**: Better statistical models
- **Faster Learning**: Quicker adaptation to preferences
- **Explainability**: Detailed reasoning for each suggestion
- **A/B Testing**: Test suggestion effectiveness

---

## FAQ

**Q: Is my data sent to external servers?**
A: No. All ML processing happens on your device. No data leaves your phone.

**Q: Can I see what data is collected?**
A: Yes. Check Settings → About → ML Data to view completion events.

**Q: How do I delete ML data?**
A: Disable ML suggestions in Settings, or clear app data to delete everything.

**Q: Why do I need 7 days before suggestions?**
A: Patterns require time to emerge. 7 days provides minimum reliable data.

**Q: Can I export suggestions?**
A: Not currently, but this feature is planned for future releases.

**Q: Do suggestions work offline?**
A: Yes. All ML features work completely offline.

**Q: How much battery does ML use?**
A: Minimal. Analysis runs once daily for 5-15 seconds in the background.

**Q: Can I customize suggestion types?**
A: Not yet, but granular control is planned for future versions.

**Q: What if I don't want ML features?**
A: Simply disable ML Suggestions in Feature Settings. No data will be collected.

**Q: How accurate are confidence scores?**
A: Confidence scores are calibrated to be conservative. 80%+ suggestions are typically very reliable.

---

## Conclusion

Peace's ML suggestion system is designed to enhance your productivity without being intrusive. By learning from your behavior and respecting your privacy, it provides personalized recommendations that help you work smarter.

**Key Takeaways**:
- 100% on-device, privacy-first
- Requires 7 days of usage
- Provides 7 types of suggestions
- Includes confidence scores
- Learns from your feedback
- Completely optional

Start using Peace normally, and let the ML system discover patterns that help you optimize your workflow naturally.

**Happy optimizing! 🤖🌿**
