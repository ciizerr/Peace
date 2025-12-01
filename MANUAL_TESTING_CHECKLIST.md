# Manual Testing Checklist - Peace App Enhancement

**Date:** December 1, 2025  
**Version:** 3.0.0  
**Tester:** _________________

## Overview

This document provides a comprehensive manual testing checklist for all enhanced features in the Peace app. Each section corresponds to a major feature area and includes specific test cases to verify functionality.

---

## 1. Ionicons Integration

### 1.1 Icon Rendering
- [ ] All icons throughout the app render correctly (no missing icons)
- [ ] Icons display at correct sizes (24dp standard, 32dp prominent)
- [ ] Icons scale properly on different screen densities (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi)
- [ ] No Material Icons are visible (all replaced with Ionicons)

### 1.2 Icon Theming
- [ ] Icons tint correctly in Light theme
- [ ] Icons tint correctly in Dark theme
- [ ] Theme switching updates icon colors immediately
- [ ] Category icons display correctly (Home, Work, Health, Study, General)

### 1.3 Fallback Behavior
- [ ] App doesn't crash when requesting non-existent icons
- [ ] Fallback icons display when Ionicons missing
- [ ] Warning logs appear for missing icons (check Logcat)

### 1.4 Specific Icon Locations
- [ ] Home screen: Navigation icons, category icons, action buttons
- [ ] Reminder detail: Category icon, priority indicator, action buttons
- [ ] Settings: All setting item icons
- [ ] Notifications: Reminder icon, action button icons
- [ ] Peace Garden: Growth stage icons, theme-specific icons
- [ ] Widgets: All widget icons

**Notes:**


---

## 2. Custom Fonts

### 2.1 Font Loading
- [ ] All custom fonts load correctly from res/font directory
- [ ] Font list displays in settings with preview text
- [ ] System font option available and works
- [ ] Selected font persists after app restart

### 2.2 Font Application
- [ ] Selected font applies to all text elements immediately
- [ ] Font displays correctly in: Headings (24sp), Subheadings (18sp), Body (14sp), Captions (12sp)
- [ ] Font renders correctly on all screens
- [ ] Font fallback to system font works on error

### 2.3 Font Padding
- [ ] Font padding slider works (0-20dp range)
- [ ] Padding applies to all text elements in real-time
- [ ] Padding value persists after app restart
- [ ] Line height and letter spacing adjust proportionally
- [ ] UI remains readable at all padding values

### 2.4 Font Preview
- [ ] Preview text displays correctly for each font
- [ ] Preview updates immediately when font selected
- [ ] Preview shows actual font rendering

**Notes:**

---

## 3. Subtasks and Checklists

### 3.1 Subtask Creation
- [ ] Can add subtasks to any reminder
- [ ] Subtask title input works correctly
- [ ] Subtasks save and persist
- [ ] Can add unlimited subtasks


### 3.2 Subtask Interaction
- [ ] Checkbox toggles completion state immediately
- [ ] Checkbox animation is smooth
- [ ] Completed subtasks show visual distinction
- [ ] Can edit subtask titles
- [ ] Can delete subtasks with confirmation

### 3.3 Progress Calculation
- [ ] Progress bar displays correct percentage
- [ ] Progress updates immediately when subtask checked/unchecked
- [ ] Progress recalculates correctly when subtask deleted
- [ ] 100% progress shows when all subtasks complete
- [ ] 0% progress shows when no subtasks complete

### 3.4 Subtask Ordering
- [ ] Subtasks display in correct order
- [ ] Drag and drop reordering works (if implemented)
- [ ] Order persists after app restart

### 3.5 Edge Cases
- [ ] Reminder with 0 subtasks shows no progress bar
- [ ] Reminder with 1 subtask calculates correctly (0% or 100%)
- [ ] Deleting all subtasks removes progress bar

**Notes:**

---

## 4. Notes and Attachments

### 4.1 Notes
- [ ] Can add notes to reminders
- [ ] Notes display with timestamps
- [ ] Notes sort chronologically (oldest first)
- [ ] Can delete notes with confirmation
- [ ] Notes persist after app restart
- [ ] Can add unlimited notes

### 4.2 Image Attachments
- [ ] Image picker opens correctly
- [ ] Can select images from gallery
- [ ] Thumbnails generate correctly (200x200)
- [ ] Full images display when tapped
- [ ] Can delete attachments with confirmation
- [ ] Attachments persist after app restart


### 4.3 Attachment Storage
- [ ] Images store in app-private directory
- [ ] File permissions restrict access to app only
- [ ] Large images compress to max 5MB
- [ ] Deleting attachment removes file from disk
- [ ] Thumbnails generate asynchronously without blocking UI

### 4.4 Chronological Ordering
- [ ] Notes and attachments display in chronological order
- [ ] Timestamps display correctly (relative time)
- [ ] Mixed notes and attachments sort correctly by timestamp

**Notes:**

---

## 5. Background Image Customization

### 5.1 Background Application
- [ ] Can set attachment image as background
- [ ] Background displays on current screen
- [ ] Background applies to: Home, Reminder Detail, Settings screens
- [ ] Can disable background (reverts to theme)

### 5.2 Blur Effect
- [ ] Blur intensity slider works (0-100 range)
- [ ] Blur updates in real-time as slider moves
- [ ] Blur value persists after app restart
- [ ] 0% blur shows clear image
- [ ] 100% blur shows heavily blurred image
- [ ] Blur doesn't impact performance significantly

### 5.3 Slideshow
- [ ] Slideshow toggle works
- [ ] Images cycle every 5 seconds when enabled
- [ ] Smooth transitions between images
- [ ] Slideshow pauses when user interacts
- [ ] Slideshow setting persists after app restart

### 5.4 Theme Fallback
- [ ] Disabling backgrounds shows theme background
- [ ] Theme background displays when no attachments exist
- [ ] Background respects theme colors

**Notes:**


---

## 6. Custom Alarm Sounds

### 6.1 Sound Selection
- [ ] Alarm sound picker displays system sounds
- [ ] Alarm sound picker displays custom sounds
- [ ] Can preview each sound before selecting
- [ ] Preview plays at configured volume
- [ ] Selected sound saves to reminder

### 6.2 Sound Playback
- [ ] Custom sound plays when alarm triggers
- [ ] Default sound plays when no custom sound set
- [ ] Sound fallback works if custom sound missing
- [ ] Sound stops when alarm dismissed
- [ ] Sound volume respects system settings

### 6.3 Sound Persistence
- [ ] Custom sound selection persists after app restart
- [ ] Sound URI and name store correctly in database
- [ ] Sound plays correctly after device reboot

### 6.4 Test Sound Button
- [ ] "Test sound" button works in settings
- [ ] Test plays full sound without triggering alarm
- [ ] Can stop test sound playback

**Notes:**

---

## 7. Language Selection

### 7.1 Language List
- [ ] All supported languages display in settings
- [ ] Current language highlighted
- [ ] "System Default" option available
- [ ] Language names display in their native script

### 7.2 Language Application
- [ ] Selecting language applies immediately (no restart)
- [ ] All UI text updates to selected language
- [ ] Language persists after app restart
- [ ] System default follows device language

### 7.3 Supported Languages
- [ ] English works correctly
- [ ] Spanish works correctly
- [ ] French works correctly
- [ ] German works correctly
- [ ] Hindi works correctly
- [ ] Japanese works correctly
- [ ] Portuguese works correctly
- [ ] Chinese works correctly


### 7.4 RTL Support (if applicable)
- [ ] RTL languages display correctly
- [ ] UI mirrors appropriately for RTL
- [ ] Icons and text align correctly

**Notes:**

---

## 8. Enhanced Peace Garden

### 8.1 Garden Themes
- [ ] All 4 themes available: Zen, Forest, Desert, Ocean
- [ ] Theme selector tabs work correctly
- [ ] Theme applies immediately when selected
- [ ] Theme-specific colors display correctly
- [ ] Theme-specific icons display correctly
- [ ] Theme selection persists after app restart

### 8.2 Growth Stages
- [ ] Garden starts at stage 0
- [ ] Completing tasks advances growth stage
- [ ] Growth visualization updates correctly
- [ ] All 10 growth stages display uniquely
- [ ] Celebration animation plays on stage advancement
- [ ] Haptic feedback triggers on advancement

### 8.3 Streak Tracking
- [ ] Current streak displays correctly
- [ ] Streak increments on consecutive day completions
- [ ] Streak resets when day skipped (24+ hours)
- [ ] Longest streak tracks correctly
- [ ] Streak persists after app restart

### 8.4 Milestones
- [ ] Milestone progress displays (7, 30, 100, 365 days)
- [ ] Achievement notification appears at milestones
- [ ] Milestone celebration animation plays
- [ ] Recent achievements list displays correctly

### 8.5 Statistics
- [ ] Total tasks completed displays correctly
- [ ] Last completion date displays correctly
- [ ] Progress to next stage shows correctly
- [ ] Days to next milestone calculates correctly

**Notes:**


---

## 9. Google Calendar Integration

### 9.1 Permission Flow
- [ ] Calendar sync toggle requests permissions
- [ ] Permission dialog displays correctly
- [ ] Granting permission enables sync
- [ ] Denying permission shows error message
- [ ] Can retry permission request

### 9.2 Calendar Creation
- [ ] "Peace Reminders" calendar creates successfully
- [ ] Calendar visible in Google Calendar app
- [ ] Calendar has correct color and settings

### 9.3 Manual Sync
- [ ] Manual sync button works
- [ ] All active reminders export to calendar
- [ ] Sync progress indicator displays
- [ ] Sync completion message shows
- [ ] Sync statistics display correctly

### 9.4 Automatic Sync
- [ ] Creating reminder syncs to calendar
- [ ] Updating reminder syncs changes to calendar
- [ ] Deleting reminder removes calendar event
- [ ] Sync respects enabled/disabled state

### 9.5 Error Handling
- [ ] Network errors display user-friendly message
- [ ] Offline sync queues for later
- [ ] Retry with exponential backoff works
- [ ] Sync errors log correctly

### 9.6 Sync Statistics
- [ ] Last sync time displays correctly
- [ ] Number of synced reminders displays
- [ ] Sync status indicator shows current state

**Notes:**

---

## 10. Deep Link Sharing

### 10.1 Share Functionality
- [ ] Share button appears on reminder detail
- [ ] Tapping share opens Android share sheet
- [ ] Share sheet shows available apps (SMS, WhatsApp, Email, etc.)
- [ ] Deep link generates correctly


### 10.2 Deep Link Format
- [ ] Deep link uses correct scheme (peace://)
- [ ] Deep link includes all reminder data
- [ ] Deep link encodes data correctly (Base64)
- [ ] Deep link size reasonable (<8KB)

### 10.3 Link Reception (App Installed)
- [ ] Opening deep link launches Peace app
- [ ] Reminder imports correctly with all data
- [ ] Import creates new independent copy
- [ ] Import confirmation toast displays
- [ ] Imported reminder appears in list

### 10.4 Link Reception (App Not Installed)
- [ ] Opening deep link prompts Play Store install
- [ ] After install, deep link data preserved
- [ ] Reminder imports after first app launch

### 10.5 Data Preservation
- [ ] Title preserves correctly
- [ ] Time preserves correctly
- [ ] Priority preserves correctly
- [ ] Category preserves correctly
- [ ] Recurrence preserves correctly
- [ ] Nag mode settings preserve correctly
- [ ] Custom alarm sound NOT preserved (expected)

### 10.6 Error Handling
- [ ] Invalid deep link shows error toast
- [ ] Corrupted data shows error message
- [ ] App doesn't crash on malformed links

### 10.7 Sharing Methods
- [ ] Share via SMS works
- [ ] Share via WhatsApp works
- [ ] Share via Email works
- [ ] Share via other messaging apps works

**Notes:**

---

## 11. ML Suggestions

### 11.1 Data Collection
- [ ] Completion events track correctly
- [ ] Task creation patterns track correctly
- [ ] Data stores for last 90 days only
- [ ] Old data cleans up automatically


### 11.2 Suggestion Generation
- [ ] Suggestions generate after 7 days of usage
- [ ] Optimal time suggestions appear
- [ ] Priority adjustment suggestions appear
- [ ] Recurring pattern suggestions appear
- [ ] Break reminder suggestions appear
- [ ] Habit formation suggestions appear
- [ ] Template creation suggestions appear
- [ ] Focus session suggestions appear

### 11.3 Suggestion Display
- [ ] Suggestions screen displays all suggestions
- [ ] Confidence scores display (0-100%)
- [ ] Suggestion explanations clear and helpful
- [ ] Empty state shows when insufficient data
- [ ] Suggestions sort by confidence score

### 11.4 Suggestion Actions
- [ ] Apply button works correctly
- [ ] Applying suggestion updates reminder
- [ ] Dismiss button works correctly
- [ ] Dismissing suggestion removes it
- [ ] Actions record for learning

### 11.5 Background Analysis
- [ ] Daily analysis runs automatically
- [ ] Analysis completes within 30 seconds
- [ ] New suggestion notification appears
- [ ] Analysis doesn't impact app performance

### 11.6 Learning System
- [ ] Accepted suggestions improve future suggestions
- [ ] Dismissed suggestions reduce similar suggestions
- [ ] Learning data persists correctly
- [ ] Suggestion frequency throttles appropriately

**Notes:**

---

## 12. Feature Toggle System

### 12.1 Toggle UI
- [ ] Feature settings section displays in settings
- [ ] All toggleable features listed
- [ ] Current toggle states display correctly
- [ ] Feature descriptions clear and helpful


### 12.2 Toggle Behavior
- [ ] Toggling feature applies immediately
- [ ] Toggle state persists after app restart
- [ ] Disabling feature hides all related UI
- [ ] Enabling feature shows all related UI

### 12.3 Specific Feature Toggles
- [ ] Subtasks toggle hides/shows subtask UI
- [ ] Attachments toggle hides/shows attachment UI
- [ ] ML Suggestions toggle stops/starts analysis
- [ ] Calendar Sync toggle stops/starts syncing
- [ ] Widgets toggle hides/shows widget options

### 12.4 Data Preservation
- [ ] Disabling feature preserves existing data
- [ ] Re-enabling feature restores access to data
- [ ] No data loss when toggling features

**Notes:**

---

## 13. Home Screen Widgets

### 13.1 Today's Reminders Widget
- [ ] Widget adds to home screen successfully
- [ ] Widget displays today's reminders
- [ ] Widget updates when reminders change
- [ ] Widget updates within 5 seconds of change
- [ ] Tapping reminder opens detail screen
- [ ] Quick add button works (if present)
- [ ] Widget respects theme colors

### 13.2 Peace Garden Widget
- [ ] Widget adds to home screen successfully
- [ ] Widget displays current garden state
- [ ] Widget shows growth stage visualization
- [ ] Widget displays current streak
- [ ] Widget updates when garden changes
- [ ] Tapping widget opens Peace Garden screen
- [ ] Widget respects theme colors

### 13.3 Quick-Add Widget
- [ ] Widget adds to home screen successfully
- [ ] Text input field works correctly
- [ ] Add button creates reminder
- [ ] Gemini AI parsing works (if implemented)
- [ ] Confirmation toast displays
- [ ] Widget clears input after adding


### 13.4 Widget Updates
- [ ] Widgets update max once per minute (throttling)
- [ ] Batch updates work for multiple widgets
- [ ] Widget updates don't drain battery excessively
- [ ] Widgets update after device reboot

### 13.5 Widget Sizing
- [ ] Widgets resize correctly on home screen
- [ ] Content scales appropriately for size
- [ ] Text remains readable at all sizes

**Notes:**

---

## 14. Enhanced Notification System

### 14.1 Notification Layout
- [ ] Custom notification layout displays
- [ ] Reminder title displays correctly
- [ ] Priority indicator shows (color/icon)
- [ ] Category icon displays
- [ ] Time displays correctly
- [ ] Peace branding visible

### 14.2 Nag Mode Display
- [ ] Nag mode progress displays (e.g., "2 of 5")
- [ ] Repetition counter updates correctly
- [ ] Panic loop indicator shows when active

### 14.3 Subtask Progress Display
- [ ] Subtask progress displays in notification
- [ ] Progress percentage shows correctly
- [ ] Progress updates when subtasks change

### 14.4 Notification Actions
- [ ] "Complete" button works reliably
- [ ] "Snooze" button works reliably
- [ ] "Dismiss" button works reliably
- [ ] Actions execute within 500ms
- [ ] Haptic feedback triggers on action
- [ ] Visual confirmation shows

### 14.5 Complete Action
- [ ] Tapping Complete marks reminder complete
- [ ] Peace Garden updates on completion
- [ ] Notification dismisses after completion
- [ ] Nag mode advances to next repetition (if applicable)


### 14.6 Snooze Action
- [ ] Tapping Snooze enters panic loop (nag mode)
- [ ] Next alarm schedules in 2 minutes
- [ ] Panic loop indicator shows in next notification
- [ ] Panic loop times out after 30 minutes
- [ ] Regular snooze works (non-nag mode)

### 14.7 Dismiss Action
- [ ] Tapping Dismiss cancels alarm
- [ ] Notification removes from drawer
- [ ] Alarm service stops
- [ ] No further alarms trigger for this instance

### 14.8 Notification Bundling
- [ ] Multiple simultaneous reminders bundle (1-min window)
- [ ] Bundled notification displays correctly
- [ ] Expanding shows all reminders
- [ ] Reminders sort by priority in bundle
- [ ] Individual actions work in bundle

### 14.9 Panic Loop Style
- [ ] Panic loop notification has distinct style
- [ ] Panic loop indicator clearly visible
- [ ] Notification text indicates panic loop state
- [ ] Panic loop notification more prominent

### 14.10 Error Handling
- [ ] Failed actions show fallback notification
- [ ] Error logs appear in Logcat
- [ ] App doesn't crash on action failure
- [ ] Timeout handling works (500ms)

**Notes:**

---

## 15. Performance Testing

### 15.1 App Startup
- [ ] App launches within 2 seconds (cold start)
- [ ] App launches within 1 second (warm start)
- [ ] No ANR (Application Not Responding) errors
- [ ] Splash screen displays smoothly

### 15.2 Icon Loading
- [ ] Icons load without blocking UI
- [ ] Icon cache improves subsequent loads
- [ ] No lag when scrolling icon-heavy screens


### 15.3 Image Loading
- [ ] Thumbnails load asynchronously
- [ ] Full images load without blocking UI
- [ ] Image cache improves performance
- [ ] Coil image loading works efficiently

### 15.4 Database Performance
- [ ] Queries execute quickly (<100ms)
- [ ] Large datasets don't cause lag
- [ ] Indexes improve query performance
- [ ] Pagination works for large lists

### 15.5 Widget Performance
- [ ] Widget updates don't drain battery
- [ ] Throttling prevents excessive updates
- [ ] Widget updates don't cause lag

### 15.6 ML Analysis Performance
- [ ] Analysis completes within 30 seconds
- [ ] Analysis doesn't block UI
- [ ] Analysis runs on background thread
- [ ] Analysis timeout works correctly

### 15.7 Memory Usage
- [ ] App memory usage reasonable (<200MB)
- [ ] No memory leaks detected
- [ ] Image cache manages memory well
- [ ] App doesn't crash on low memory

**Notes:**

---

## 16. Accessibility Testing

### 16.1 TalkBack Support
- [ ] All icons have content descriptions
- [ ] Screen reader announces all elements
- [ ] Navigation works with TalkBack
- [ ] State changes announced correctly

### 16.2 Touch Targets
- [ ] All interactive elements ≥48dp x 48dp
- [ ] Adequate spacing between buttons (≥8dp)
- [ ] Easy to tap without errors

### 16.3 Color Contrast
- [ ] Text contrast ≥4.5:1 (normal text)
- [ ] Text contrast ≥3:1 (large text)
- [ ] High contrast mode works
- [ ] Color not sole indicator of information


### 16.4 Keyboard Navigation
- [ ] Can navigate with external keyboard
- [ ] Tab order logical and sequential
- [ ] Focus indicators visible
- [ ] All actions accessible via keyboard

### 16.5 Font Scaling
- [ ] App works with large font sizes
- [ ] Text doesn't truncate at large sizes
- [ ] Layout adapts to font scaling
- [ ] Minimum font size readable

**Notes:**

---

## 17. Cross-Feature Integration

### 17.1 Subtasks + Notifications
- [ ] Notification shows subtask progress
- [ ] Completing reminder completes all subtasks
- [ ] Subtask progress updates in notification

### 17.2 Attachments + Background
- [ ] Can set attachment as background
- [ ] Background persists across screens
- [ ] Slideshow works with multiple attachments

### 17.3 Garden + Notifications
- [ ] Completing notification updates garden
- [ ] Garden growth triggers on completion
- [ ] Streak updates on daily completion

### 17.4 Calendar + Deep Links
- [ ] Shared reminders can sync to calendar
- [ ] Calendar events preserve after import
- [ ] Sync works for imported reminders

### 17.5 ML + Feature Toggles
- [ ] Disabling ML stops suggestions
- [ ] Re-enabling ML resumes analysis
- [ ] Suggestions respect feature state

### 17.6 Widgets + Feature Toggles
- [ ] Widgets respect feature toggle states
- [ ] Disabled features don't show in widgets
- [ ] Widget updates when features toggled

**Notes:**


---

## 18. Edge Cases and Error Scenarios

### 18.1 Network Errors
- [ ] Calendar sync handles offline gracefully
- [ ] Sync queues for later when offline
- [ ] Error messages user-friendly
- [ ] Retry mechanism works

### 18.2 Storage Errors
- [ ] Handles storage full scenario
- [ ] Prevents attachment when storage full
- [ ] Shows appropriate error message

### 18.3 Permission Errors
- [ ] Handles denied permissions gracefully
- [ ] Shows permission rationale
- [ ] Can retry permission request
- [ ] App functions without optional permissions

### 18.4 Data Corruption
- [ ] Handles corrupted deep links
- [ ] Handles corrupted database gracefully
- [ ] Fallback mechanisms work
- [ ] App doesn't crash on bad data

### 18.5 Extreme Values
- [ ] Handles 0 subtasks correctly
- [ ] Handles 100+ subtasks correctly
- [ ] Handles very long reminder titles
- [ ] Handles very long note content
- [ ] Handles 0-day streak correctly
- [ ] Handles 1000+ day streak correctly

### 18.6 Concurrent Operations
- [ ] Multiple simultaneous alarms work
- [ ] Concurrent database writes work
- [ ] Widget updates don't conflict
- [ ] Sync doesn't conflict with local changes

**Notes:**

---

## 19. Device Compatibility

### 19.1 Screen Sizes
- [ ] Works on small phones (5" screen)
- [ ] Works on medium phones (6" screen)
- [ ] Works on large phones (6.5"+ screen)
- [ ] Works on tablets (7"+ screen)
- [ ] Landscape orientation works


### 19.2 Android Versions
- [ ] Works on Android 8.0 (API 26)
- [ ] Works on Android 9.0 (API 28)
- [ ] Works on Android 10 (API 29)
- [ ] Works on Android 11 (API 30)
- [ ] Works on Android 12 (API 31)
- [ ] Works on Android 13 (API 33)
- [ ] Works on Android 14 (API 34)

### 19.3 Screen Densities
- [ ] Icons render correctly on mdpi
- [ ] Icons render correctly on hdpi
- [ ] Icons render correctly on xhdpi
- [ ] Icons render correctly on xxhdpi
- [ ] Icons render correctly on xxxhdpi

### 19.4 Device Manufacturers
- [ ] Works on Samsung devices
- [ ] Works on Google Pixel devices
- [ ] Works on OnePlus devices
- [ ] Works on Xiaomi devices
- [ ] Works on other manufacturers

**Notes:**

---

## 20. Regression Testing

### 20.1 Core Reminder Functionality
- [ ] Can create reminders
- [ ] Can edit reminders
- [ ] Can delete reminders
- [ ] Alarms trigger correctly
- [ ] Recurrence works correctly

### 20.2 Nag Mode (Existing Feature)
- [ ] Nag mode still works correctly
- [ ] Repetitions schedule correctly
- [ ] Flex mode works
- [ ] Strict mode works
- [ ] Panic loop still functions

### 20.3 Categories
- [ ] All categories work (Home, Work, Health, Study, General)
- [ ] Category icons display correctly
- [ ] Category filtering works

### 20.4 Priority Levels
- [ ] All priority levels work (High, Medium, Low)
- [ ] Priority indicators display correctly
- [ ] Priority sorting works


### 20.5 History
- [ ] Completed reminders move to history
- [ ] History displays correctly
- [ ] Can view history details
- [ ] Can restore from history

### 20.6 Themes
- [ ] Light theme works correctly
- [ ] Dark theme works correctly
- [ ] Theme switching works
- [ ] Theme persists after restart

**Notes:**

---

## 21. User Experience Testing

### 21.1 Onboarding
- [ ] First-time user experience smooth
- [ ] Feature discovery intuitive
- [ ] Help/tutorial accessible (if present)

### 21.2 Navigation
- [ ] Navigation intuitive and logical
- [ ] Back button works correctly
- [ ] Deep linking works correctly
- [ ] No dead ends in navigation

### 21.3 Feedback
- [ ] Loading states display appropriately
- [ ] Success messages clear
- [ ] Error messages helpful
- [ ] Progress indicators accurate

### 21.4 Animations
- [ ] Animations smooth (60fps)
- [ ] Animations not excessive
- [ ] Animations enhance UX
- [ ] Can disable animations (accessibility)

### 21.5 Consistency
- [ ] UI consistent across screens
- [ ] Terminology consistent
- [ ] Icon usage consistent
- [ ] Color usage consistent

**Notes:**

---

## 22. Security and Privacy

### 22.1 Data Storage
- [ ] Attachments in app-private directory
- [ ] File permissions restrict access
- [ ] No sensitive data in logs


### 22.2 ML Privacy
- [ ] All ML analysis on-device
- [ ] No data sent to external servers
- [ ] No telemetry collected
- [ ] User can disable ML features

### 22.3 Calendar Permissions
- [ ] Only requests necessary permissions
- [ ] Minimal scope (calendar write only)
- [ ] Permission denial handled gracefully

### 22.4 Deep Link Security
- [ ] Deep link data validated
- [ ] Text fields sanitized
- [ ] Size limits enforced (<8KB)
- [ ] No injection vulnerabilities

**Notes:**

---

## 23. Final Checks

### 23.1 Build Quality
- [ ] No compiler warnings
- [ ] No lint errors
- [ ] ProGuard rules correct (if used)
- [ ] APK size reasonable

### 23.2 Crash Testing
- [ ] No crashes during normal use
- [ ] No crashes during stress testing
- [ ] Crash reports clear and actionable
- [ ] Error handling comprehensive

### 23.3 Battery Usage
- [ ] Battery usage reasonable
- [ ] No wakelocks keeping device awake
- [ ] Background tasks optimized
- [ ] Doze mode compatibility

### 23.4 Documentation
- [ ] README updated with new features
- [ ] User guide available
- [ ] API documentation complete (if applicable)
- [ ] Change log updated

**Notes:**

---

## Summary

### Total Test Cases
- **Ionicons:** 20 test cases
- **Custom Fonts:** 15 test cases
- **Subtasks:** 20 test cases
- **Notes & Attachments:** 20 test cases
- **Background Images:** 15 test cases
- **Custom Alarm Sounds:** 15 test cases
- **Language Selection:** 15 test cases
- **Peace Garden:** 25 test cases
- **Calendar Integration:** 25 test cases
- **Deep Link Sharing:** 30 test cases
- **ML Suggestions:** 25 test cases
- **Feature Toggles:** 15 test cases
- **Widgets:** 20 test cases
- **Notifications:** 40 test cases
- **Performance:** 20 test cases
- **Accessibility:** 15 test cases
- **Integration:** 20 test cases
- **Edge Cases:** 20 test cases
- **Device Compatibility:** 20 test cases
- **Regression:** 20 test cases
- **UX:** 15 test cases
- **Security:** 10 test cases
- **Final Checks:** 10 test cases

**Total:** ~400+ test cases

### Testing Status

- [ ] All critical features tested
- [ ] All high-priority bugs fixed
- [ ] All medium-priority bugs triaged
- [ ] Performance acceptable
- [ ] Accessibility compliant
- [ ] Ready for release

### Sign-off

**Tester:** _________________  
**Date:** _________________  
**Signature:** _________________

**Notes:**

