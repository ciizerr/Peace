# Testing Guide: Priority Alarm Bug Fix

## How to Test the Fix

### Setup Test Scenario
1. Open the Peace app
2. Create three reminders for the SAME time (e.g., 3:30 PM today):
   - **Reminder 1**: "Medium Task" - Priority: MEDIUM - Time: 3:30 PM
   - **Reminder 2**: "High Task" - Priority: HIGH - Time: 3:30 PM  
   - **Reminder 3**: "Low Task" - Priority: LOW - Time: 3:30 PM

### Expected Behavior at 3:30 PM

#### Before Fix ❌
- Only "Medium Task" alarm would ring
- Only "Medium Task" would be shown
- After marking done, "High Task" and "Low Task" remain incomplete
- User would miss the other two reminders

#### After Fix ✅
- All three alarms trigger together
- Alarm screen shows:
  ```
  🕐 15:30
  🔔 3 Reminders Due
  
  ┌─────────────────────────┐
  │ 🔴 High Task     HIGH   │
  └─────────────────────────┘
  
  ┌─────────────────────────┐
  │ 🔵 Medium Task   MEDIUM │
  └─────────────────────────┘
  
  ┌─────────────────────────┐
  │ 🟢 Low Task      LOW    │
  └─────────────────────────┘
  
  [I'M DOING IT (STOP ALL)]
  [SNOOZE ALL]
  ```
- Clicking "I'M DOING IT" marks ALL three as complete
- All three reminders removed from active list

## Visual Changes

### Old Alarm Screen (Single Reminder)
```
┌─────────────────────────────┐
│         15:30               │
│                             │
│         🔔                  │
│                             │
│      Medium Task            │
│                             │
│  [I'M DOING IT (STOP)]     │
│  [SNOOZE]                  │
└─────────────────────────────┘
```

### New Alarm Screen (Multiple Reminders)
```
┌─────────────────────────────┐
│         15:30               │
│         🔔                  │
│   3 Reminders Due           │
│                             │
│  ┌─ High Task    HIGH ─┐   │
│  ┌─ Medium Task  MEDIUM─┐  │
│  ┌─ Low Task     LOW   ─┐  │
│                             │
│  [I'M DOING IT (STOP ALL)] │
│  [SNOOZE ALL]              │
└─────────────────────────────┘
```

## Additional Test Cases

### Test Case 1: Different Times (Should NOT Bundle)
- Reminder A: 3:00 PM
- Reminder B: 3:30 PM
- **Expected**: Two separate alarms at different times

### Test Case 2: Within 1-Minute Window (Should Bundle)
- Reminder A: 3:30:00 PM
- Reminder B: 3:30:45 PM
- **Expected**: Both shown together (within 60-second window)

### Test Case 3: One Disabled (Should NOT Include)
- Reminder A: 4:00 PM, Enabled
- Reminder B: 4:00 PM, Disabled (toggle off)
- **Expected**: Only Reminder A triggers

### Test Case 4: One Already Completed (Should NOT Include)
- Reminder A: 5:00 PM, Not completed
- Reminder B: 5:00 PM, Already marked done
- **Expected**: Only Reminder A triggers

### Test Case 5: Mixed Priorities (Should Sort Correctly)
- Create 5 reminders at same time with random priorities
- **Expected**: Displayed in order: HIGH → MEDIUM → LOW

## Verification Checklist

- [ ] Multiple simultaneous reminders trigger together
- [ ] Reminders sorted by priority (HIGH first, LOW last)
- [ ] "I'M DOING IT" completes all bundled reminders
- [ ] "SNOOZE ALL" snoozes all bundled reminders
- [ ] Single reminder still works as before (no regression)
- [ ] Disabled reminders not included in bundle
- [ ] Completed reminders not included in bundle
- [ ] Time window works correctly (±1 minute)

## Debug Logging

Check logcat for these messages:
```
Bundled X reminders for simultaneous alarm
Scheduling Alarm for Reminder: [Title] (ID: X) at [Time]
```

## Known Limitations

1. **Time Window**: Reminders must be within 60 seconds to bundle
2. **Snooze Behavior**: All bundled reminders snooze together (no individual snooze)
3. **Nag Mode**: If one reminder has nag mode, all bundled reminders follow the same pattern

## Rollback Plan

If issues occur, revert these three files:
1. `AlarmReceiver.kt`
2. `ReminderService.kt`
3. `AlarmActivity.kt`

The changes are isolated to alarm triggering and display logic.
