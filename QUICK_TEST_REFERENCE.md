# Quick Test Reference Card - Peace App

## Critical Test Paths (Must Test First)

### 1. Basic Reminder Flow
1. Create reminder → Set time → Save
2. Wait for alarm → Notification appears
3. Tap "Complete" → Reminder marked done
4. Check Peace Garden updated

### 2. Subtasks Flow
1. Open reminder → Add subtask
2. Check subtask → Progress bar updates
3. Complete all subtasks → 100% progress
4. Delete subtask → Progress recalculates

### 3. Notification Actions
1. Trigger alarm → Notification appears
2. Test "Complete" button → Works reliably
3. Test "Snooze" button → Panic loop activates
4. Test "Dismiss" button → Alarm cancels

### 4. Peace Garden
1. Complete task → Garden updates
2. Complete daily → Streak increments
3. Reach milestone → Celebration shows
4. Switch theme → Visual changes

### 5. Calendar Sync
1. Enable sync → Permission granted
2. Manual sync → Reminders export
3. Create reminder → Auto syncs
4. Check Google Calendar → Events appear

## Quick Checks (5 Minutes)

- [ ] App launches without crash
- [ ] All icons are Ionicons (no Material Icons)
- [ ] Custom font applies everywhere
- [ ] Notifications show and actions work
- [ ] Subtasks add and check correctly
- [ ] Peace Garden displays correctly
- [ ] No obvious visual bugs

## Common Issues to Watch For

### Icons
- ❌ Material Icons still visible
- ❌ Missing icons (blank spaces)
- ❌ Icons wrong color in dark theme

### Fonts
- ❌ Font doesn't apply to all text
- ❌ Font padding not working
- ❌ Text truncated at large sizes

### Subtasks
- ❌ Progress calculation wrong
- ❌ Progress doesn't update immediately
- ❌ Subtasks don't persist

### Notifications
- ❌ "Dismiss" button doesn't work
- ❌ Actions take >500ms to execute
- ❌ Panic loop doesn't activate

### Calendar
- ❌ Sync fails silently
- ❌ Events don't appear in calendar
- ❌ Permission errors not handled

### Deep Links
- ❌ Link doesn't open app
- ❌ Data doesn't import correctly
- ❌ App crashes on malformed link

## Performance Red Flags

- ⚠️ App takes >3s to launch
- ⚠️ UI lags when scrolling
- ⚠️ Images take >2s to load
- ⚠️ Memory usage >300MB
- ⚠️ Battery drains quickly

## Accessibility Red Flags

- ⚠️ TalkBack can't navigate
- ⚠️ Touch targets <48dp
- ⚠️ Text contrast too low
- ⚠️ Text truncated at large font

## Test Device Setup

### Minimum Setup:
- 1 Android device (8.0+)
- Google account for calendar
- Test images for attachments
- WhatsApp/SMS for deep links

### Recommended Setup:
- 2-3 devices (different sizes/versions)
- Multiple Google accounts
- Various test data prepared
- Screen recording tool

## Bug Severity Guide

**Critical** 🔴
- App crashes
- Data loss
- Security issue
- Core feature broken

**High** 🟠
- Major feature broken
- Poor UX
- Workaround difficult

**Medium** 🟡
- Minor feature issue
- Workaround exists
- Cosmetic but noticeable

**Low** 🟢
- Minor cosmetic issue
- Rare edge case
- Minimal impact

## Quick Bug Report Template

```
Title: [Brief description]

Steps:
1. [Step 1]
2. [Step 2]
3. [Step 3]

Expected: [What should happen]
Actual: [What actually happened]

Device: [Model and Android version]
Severity: [Critical/High/Medium/Low]
Screenshot: [If applicable]
```

## Testing Shortcuts

### Fast Feature Toggle Test:
Settings → Features → Toggle all → Check UI updates

### Fast Theme Test:
Settings → Theme → Switch → Check colors/icons

### Fast Widget Test:
Add widget → Complete task → Check widget updates

### Fast Calendar Test:
Enable sync → Create reminder → Check Google Calendar

### Fast Deep Link Test:
Share reminder → Send to self → Open link

## Time Estimates

- Full checklist: 40-60 hours
- Core features only: 8-10 hours
- Quick smoke test: 30 minutes
- Regression test: 4-6 hours
- Single feature deep dive: 2-3 hours

## Priority Order

1. **P0** (Must work): Reminders, Notifications, Alarms
2. **P1** (Critical): Subtasks, Garden, Ionicons
3. **P2** (Important): Calendar, Deep Links, Widgets
4. **P3** (Nice to have): ML, Fonts, Backgrounds

## Contact for Issues

- Critical bugs: [Immediate escalation]
- Questions: [Team lead]
- Clarifications: [Product owner]
- Technical issues: [Dev team]

---

**Keep this card handy during testing!**

