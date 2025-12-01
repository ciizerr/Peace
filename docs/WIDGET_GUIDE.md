# Peace Widgets Guide 🏠

This guide covers everything you need to know about Peace's home screen widgets, including setup, usage, customization, and troubleshooting.

## Table of Contents

1. [Overview](#overview)
2. [Widget Types](#widget-types)
3. [Setup Instructions](#setup-instructions)
4. [Widget Features](#widget-features)
5. [Customization](#customization)
6. [Update Behavior](#update-behavior)
7. [Troubleshooting](#troubleshooting)
8. [Best Practices](#best-practices)
9. [Technical Details](#technical-details)

---

## Overview

Peace offers three home screen widgets to keep you productive without opening the app:

1. **Today's Reminders Widget**: View all today's tasks
2. **Peace Garden Widget**: Track your progress and streak
3. **Quick-Add Widget**: Create reminders instantly

### Key Features

- **Glance-Based**: Built with Jetpack Glance for modern Android widgets
- **Auto-Updating**: Refreshes automatically when data changes
- **Theme-Aware**: Matches your app theme (light/dark)
- **Ionicons**: Uses custom Ionicons throughout
- **Interactive**: Tap to open app or perform actions
- **Resizable**: Adjust size to fit your home screen layout

### Requirements

- Android 8.0 (API 26) or higher
- Peace app installed
- Widgets enabled in Feature Settings

---

## Widget Types

### 1. Today's Reminders Widget

**Purpose**: Display all reminders scheduled for today

**What It Shows**:
- Task titles
- Scheduled times
- Priority indicators (🔴🟡🟢)
- Category icons
- Completion status
- Total count

**Interactions**:
- Tap any reminder → Opens reminder detail screen
- Tap widget header → Opens home screen
- Tap "No tasks" message → Opens add reminder screen

**Best For**:
- Morning planning
- Daily overview
- Quick task checking

**Recommended Size**: 4×2 or 4×3 cells

---

### 2. Peace Garden Widget

**Purpose**: Display your Peace Garden progress and streak

**What It Shows**:
- Current growth stage visual
- Growth stage name
- Current streak count
- Next milestone progress
- Theme-specific styling

**Interactions**:
- Tap widget → Opens Peace Garden screen
- Visual updates when garden grows

**Best For**:
- Motivation
- Streak tracking
- Visual progress
- Quick encouragement

**Recommended Size**: 2×2 or 3×2 cells

---

### 3. Quick-Add Widget

**Purpose**: Create reminders without opening the app

**What It Shows**:
- Text input field
- Add button
- Peace branding

**Interactions**:
- Type task description
- Tap Add button
- Gemini AI parses input
- Reminder created
- Confirmation toast appears

**Best For**:
- Rapid task capture
- Inbox-style collection
- Minimal friction
- On-the-go creation

**Recommended Size**: 4×1 or 4×2 cells

---

## Setup Instructions

### Adding a Widget (Android 12+)

1. **Long-press** on empty space on your home screen
2. Tap **Widgets** in the menu that appears
3. Scroll to find **Peace** in the widget list
4. Choose your desired widget:
   - Today's Reminders
   - Peace Garden
   - Quick-Add
5. **Long-press and drag** the widget to your home screen
6. **Release** to place the widget
7. **Resize** by dragging the corners (optional)

### Adding a Widget (Android 11 and earlier)

1. **Long-press** on empty space on your home screen
2. Tap **Widgets** in the menu
3. Find **Peace** in the alphabetical list
4. **Tap and hold** your desired widget
5. **Drag** to your home screen
6. **Release** to place
7. **Resize** if needed

### Removing a Widget

1. **Long-press** on the widget
2. **Drag** to the "Remove" area at the top
3. **Release** to remove

Or:

1. **Long-press** on the widget
2. Tap **Remove** in the menu

---

## Widget Features

### Today's Reminders Widget

#### Display Elements

**Header**:
- "Today's Tasks" title
- Task count badge
- Peace icon

**Task List**:
- Up to 10 tasks displayed
- Scrollable if more than 10
- Each task shows:
  - Priority indicator (colored dot)
  - Category icon (Ionicons)
  - Task title (truncated if long)
  - Scheduled time
  - Completion checkbox

**Empty State**:
- "No tasks for today" message
- Encouraging icon
- Tap to add new task

#### Sorting

Tasks are sorted by:
1. Priority (High → Medium → Low)
2. Scheduled time (earliest first)
3. Creation time (newest first)

#### Filtering

Shows only:
- Tasks scheduled for today
- Non-completed tasks
- Enabled reminders

---

### Peace Garden Widget

#### Display Elements

**Visual**:
- Growth stage illustration
- Theme-specific colors
- Animated transitions (on update)

**Text Information**:
- Growth stage name (e.g., "Blooming")
- Current streak: "🔥 X days"
- Next milestone: "Y days to next milestone"

**Theme Variations**:
- Zen: Minimalist, peaceful
- Forest: Lush, green
- Desert: Warm, sandy
- Ocean: Cool, blue

#### Update Triggers

Widget updates when:
- Task completed
- Streak changes
- Growth stage advances
- Milestone reached
- Theme changed

---

### Quick-Add Widget

#### Display Elements

**Input Field**:
- Placeholder: "What needs to be done?"
- Single-line text input
- Auto-focus on tap

**Add Button**:
- "Add" text or plus icon
- Disabled when input empty
- Enabled when text entered

**Branding**:
- Peace icon
- Subtle background

#### Parsing Behavior

When you tap Add:
1. Text sent to Gemini AI
2. AI parses:
   - Task title
   - Date/time
   - Priority
   - Category
   - Recurrence
3. Reminder created with parsed data
4. Input field cleared
5. Confirmation toast shown

**Example Inputs**:
- "Dentist tomorrow at 3pm" → Creates reminder for tomorrow at 3pm
- "Gym every Monday at 6am" → Creates recurring reminder
- "Call mom" → Creates reminder for today with default time

#### Error Handling

If parsing fails:
- Creates reminder with raw text as title
- Uses default settings (today, medium priority, general category)
- Still shows confirmation

---

## Customization

### Resizing Widgets

**Today's Reminders**:
- Minimum: 4×2 cells
- Maximum: 4×5 cells
- Recommended: 4×3 cells
- Larger sizes show more tasks

**Peace Garden**:
- Minimum: 2×2 cells
- Maximum: 4×3 cells
- Recommended: 2×2 or 3×2 cells
- Larger sizes show more detail

**Quick-Add**:
- Minimum: 4×1 cells
- Maximum: 4×2 cells
- Recommended: 4×1 cells
- Height doesn't affect functionality

### Theme Matching

Widgets automatically match your app theme:

**Light Mode**:
- Light backgrounds
- Dark text
- Subtle shadows
- High contrast

**Dark Mode**:
- Dark backgrounds
- Light text
- Elevated surfaces
- Reduced contrast

**System Default**:
- Follows device theme
- Switches automatically
- No manual configuration

### Icon Customization

Widgets use Ionicons throughout:
- Category icons match app
- Priority indicators consistent
- Garden theme icons match selected theme
- No manual icon selection needed

---

## Update Behavior

### Automatic Updates

Widgets update automatically when:

**Data Changes**:
- Reminder created
- Reminder updated
- Reminder completed
- Reminder deleted
- Garden state changes
- Streak updates

**Timing**:
- Updates within 5 seconds of change
- Throttled to max once per minute
- Prevents excessive battery drain

**Triggers**:
- App foreground → Immediate update
- Background change → Scheduled update
- System reboot → Update on next app launch

### Manual Refresh

Force a widget update:

1. Open Peace app
2. Navigate to relevant screen (Home/Garden)
3. Widgets update automatically
4. Close app

Or:

1. Remove widget
2. Re-add widget
3. Fresh data loaded

### Update Throttling

To preserve battery:
- Maximum 1 update per minute per widget
- Batched updates when multiple changes
- Deferred updates when screen off
- Immediate updates for user actions

---

## Troubleshooting

### Widget Not Appearing in List

**Possible Causes**:
- Widgets disabled in Feature Settings
- App not fully installed
- Android version too old

**Solutions**:
1. Open Peace → Settings → Feature Settings
2. Enable "Widgets" toggle
3. Restart device
4. Try adding widget again

If still not appearing:
- Check Android version (need 8.0+)
- Reinstall Peace app
- Clear launcher cache

---

### Widget Not Updating

**Possible Causes**:
- Battery optimization blocking updates
- Background data restricted
- Widget update service stopped
- App force-stopped

**Solutions**:

**Check Battery Optimization**:
1. Settings → Apps → Peace
2. Battery → Battery optimization
3. Select "Don't optimize"

**Check Background Data**:
1. Settings → Apps → Peace
2. Mobile data & Wi-Fi
3. Enable "Background data"

**Restart Widget Service**:
1. Open Peace app
2. Navigate to Home screen
3. Close app
4. Widget should update

**Force Refresh**:
1. Remove widget
2. Re-add widget
3. Should show current data

---

### Widget Showing Old Data

**Possible Causes**:
- Update throttling
- Cached data
- Sync delay

**Solutions**:
1. Open Peace app (triggers update)
2. Wait 1 minute (throttle period)
3. Check if data updated
4. If not, remove and re-add widget

---

### Quick-Add Widget Not Working

**Possible Causes**:
- No Gemini API key configured
- Network connection issues
- Input too complex for parsing

**Solutions**:

**Check API Key**:
1. Open Peace → Settings
2. Verify Gemini API Key is set
3. Test by creating reminder in app

**Check Network**:
1. Verify internet connection
2. Try simpler input first
3. Check if app can create reminders

**Simplify Input**:
- Use clear, simple language
- Include time explicitly
- Avoid complex recurrence patterns

**Fallback**:
- Widget creates reminder with raw text
- Edit in app to add details

---

### Widget Looks Wrong

**Possible Causes**:
- Theme mismatch
- Launcher compatibility
- Widget size too small

**Solutions**:

**Check Theme**:
1. Open Peace → Settings
2. Verify theme setting
3. Try switching theme
4. Widget should update

**Resize Widget**:
1. Long-press widget
2. Drag corners to resize
3. Use recommended sizes

**Launcher Compatibility**:
- Some launchers have widget limitations
- Try stock Android launcher
- Update launcher app

---

### Widget Draining Battery

**Possible Causes**:
- Too frequent updates
- Multiple widgets active
- Background processing issues

**Solutions**:

**Reduce Widgets**:
- Use only widgets you need
- Remove unused widgets

**Check Update Frequency**:
- Updates are throttled by default
- Should not cause significant drain
- If excessive, report as bug

**Monitor Battery**:
1. Settings → Battery
2. Check Peace usage
3. Should be minimal (<1% per day)

---

## Best Practices

### Widget Placement

**Today's Reminders**:
- Place on main home screen
- Top half for visibility
- Check every morning
- Resize to show 5-7 tasks

**Peace Garden**:
- Place on secondary screen
- Use as motivation
- Check when completing tasks
- Keep visible for streak awareness

**Quick-Add**:
- Place on main home screen
- Bottom for thumb reach
- Use for rapid capture
- Keep input field accessible

### Usage Patterns

**Morning Routine**:
1. Check Today's Reminders widget
2. Plan your day
3. Prioritize tasks
4. Check Peace Garden for motivation

**Throughout Day**:
1. Use Quick-Add for new tasks
2. Check Today's Reminders for next task
3. Complete tasks in app
4. Watch widgets update

**Evening Review**:
1. Check Today's Reminders for remaining tasks
2. Complete or reschedule
3. Check Peace Garden for progress
4. Celebrate streak if maintained

### Optimization Tips

1. **Use Appropriate Sizes**: Don't make widgets too large
2. **Limit Widget Count**: 1-2 widgets per home screen
3. **Strategic Placement**: Most-used widgets on main screen
4. **Regular Cleanup**: Remove widgets you don't use
5. **Theme Consistency**: Match widgets to wallpaper

---

## Technical Details

### Widget Technology

**Framework**: Jetpack Glance
- Modern declarative UI
- Compose-like syntax
- Efficient updates
- Material Design 3

**Update Mechanism**: WorkManager
- Reliable background updates
- Battery-efficient scheduling
- Respects system constraints
- Automatic retry on failure

**Data Source**: Room Database
- Direct database queries
- Reactive updates via Flow
- Efficient caching
- Minimal overhead

### Performance Characteristics

**Memory Usage**:
- Today's Reminders: ~2-5 MB
- Peace Garden: ~1-3 MB
- Quick-Add: ~1-2 MB

**Update Latency**:
- Foreground: <1 second
- Background: 1-5 seconds
- Throttled: Up to 60 seconds

**Battery Impact**:
- Per widget: <0.1% per day
- All widgets: <0.3% per day
- Negligible for most users

### Data Flow

```
User Action (Complete Task)
        ↓
Repository Updates Database
        ↓
Flow Emits New Data
        ↓
WidgetUpdateManager Notified
        ↓
WorkManager Schedules Update
        ↓
Widget Provider Queries Data
        ↓
Glance Renders New UI
        ↓
Widget Displays on Home Screen
```

### Update Scheduling

**Immediate Updates** (< 1 second):
- App in foreground
- User action in app
- Widget interaction

**Scheduled Updates** (1-5 seconds):
- App in background
- Data change detected
- Within throttle window

**Deferred Updates** (up to 60 seconds):
- Outside throttle window
- Screen off
- Battery saver active

### Widget Lifecycle

```
Widget Added
    ↓
Initial Data Load
    ↓
Render UI
    ↓
Register for Updates
    ↓
[Active State]
    ↓
Data Changes → Update UI
    ↓
[Repeat]
    ↓
Widget Removed
    ↓
Unregister Updates
    ↓
Cleanup Resources
```

---

## Advanced Topics

### Custom Widget Configurations

Currently, widgets use default configurations. Future versions may support:
- Custom task filters
- Custom garden themes
- Custom update frequencies
- Custom layouts

### Widget Interactions

**Supported Actions**:
- Tap to open app
- Tap specific items
- Text input (Quick-Add)
- Button clicks

**Not Supported** (Android limitations):
- Swipe gestures
- Long-press actions
- Drag-and-drop
- Complex animations

### Multiple Widget Instances

You can add multiple instances of the same widget:
- Each instance updates independently
- All show same data
- Useful for different home screens
- No performance penalty

### Widget Backup & Restore

Widget configurations are:
- Backed up with Android Auto Backup
- Restored on new device
- May need re-adding after restore
- Data syncs from app database

---

## FAQ

**Q: How many widgets can I add?**
A: No limit, but 3-5 total is recommended for performance.

**Q: Do widgets work offline?**
A: Yes, widgets show cached data when offline. Quick-Add requires internet for AI parsing.

**Q: Can I customize widget appearance?**
A: Widgets automatically match your app theme. Manual customization not currently supported.

**Q: Why isn't my widget updating?**
A: Check battery optimization and background data settings. Open app to force update.

**Q: Do widgets drain battery?**
A: Minimal impact (<0.3% per day for all widgets). Updates are throttled and efficient.

**Q: Can I use widgets without opening the app?**
A: Yes, but initial setup requires opening app once. After that, widgets work independently.

**Q: What if Quick-Add parsing fails?**
A: Widget creates reminder with raw text. You can edit details in the app.

**Q: Can I share widget configurations?**
A: Not currently supported. Each user must set up widgets individually.

**Q: Do widgets support all Android launchers?**
A: Most launchers support widgets. Some custom launchers may have limitations.

**Q: How do I report widget bugs?**
A: Open GitHub issue with device model, Android version, and description of problem.

---

## Conclusion

Peace widgets bring your productivity to your home screen, providing quick access to tasks, progress tracking, and rapid task creation—all without opening the app.

**Key Takeaways**:
- Three widget types for different needs
- Auto-updating with throttling
- Theme-aware and customizable
- Battery-efficient
- Easy to set up and use

Add widgets to your home screen today and experience Peace's calm engagement wherever you are!

**Happy widget-ing! 🏠🌿**
