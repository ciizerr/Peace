# Requirements Document

## Introduction

This document outlines the comprehensive enhancement plan for the Peace app, transforming it from a minimalist reminder application into a full-featured productivity suite while maintaining its core philosophy of "Calm Engagement." The enhancements include advanced customization options, collaborative features, intelligent suggestions, and rich media support, all designed to give users complete control over their experience.

## Glossary

- **Peace System**: The complete Peace application including all UI, data, and service components
- **User**: Any person using the Peace application on their Android device
- **Reminder**: A scheduled task or notification with associated metadata (title, time, priority, recurrence)
- **Nag Mode**: A repetition system allowing users to schedule multiple occurrences of a task (e.g., "drink water 5 times every 2 hours") with a panic loop (2-minute intervals for 30 minutes) when snoozed
- **Repetition**: A single occurrence within a nag mode sequence (e.g., the 3rd water reminder out of 5 total)
- **Flex Mode**: Scheduling mode where each repetition is scheduled relative to when the previous repetition was completed
- **Strict Mode**: Scheduling mode where all repetitions are anchored to the original start time, maintaining fixed intervals regardless of completion time
- **Panic Loop**: The nested snooze behavior that triggers every 2 minutes for up to 30 minutes when a user snoozes instead of completing
- **Peace Garden**: A visual progress tracker that grows as users complete tasks
- **Subtask**: A child task nested under a parent reminder with its own completion state
- **Attachment**: An image or note associated with a reminder
- **Custom Sound**: A user-selected audio file used as an alarm tone for a specific reminder
- **Calendar Sync**: One-way synchronization from Peace reminders to Google Calendar
- **Deep Link**: An Android App Link that opens the Peace app and imports shared reminder data
- **ML Suggestion**: An AI-generated recommendation based on user behavior patterns
- **System Font**: The default Android system typeface
- **Custom Font**: Application-bundled typefaces available for user selection
- **Font Padding**: Additional spacing applied to text elements, measured in density-independent pixels (dp)
- **Icon Pack**: A complete set of custom vector icons (Ionicons) bundled with the application to replace default Material Icons
- **Vector Drawable**: Android's XML-based vector graphics format used for scalable icons
- **Fallback Icon**: A placeholder icon from an alternative source used when Ionicons is missing a required icon
- **Widget**: A home screen component that displays app information and provides quick actions without opening the app
- **Peace Garden Theme**: A visual style for the Peace Garden (zen, forest, desert, or ocean) with unique colors and icons
- **Streak**: The number of consecutive days a user has completed at least one task
- **Growth Stage**: A visual representation of progress in the Peace Garden, advancing as tasks are completed
- **Milestone**: A significant streak achievement (7, 30, 100, or 365 consecutive days)
- **Notification Area**: The Android notification drawer and notification UI components
- **Blur Intensity**: A numerical value (0-100) controlling the gaussian blur effect on background images
- **Slideshow**: Automatic cycling through multiple attachment images as backgrounds
- **Confidence Score**: A percentage (0-100) indicating ML suggestion reliability

## Requirements

### Requirement 1: In-App Language Selection

**User Story:** As a user, I want to change the app language from within the app settings, so that I can use my preferred language without changing my device settings.

#### Acceptance Criteria

1. WHEN the user opens the language settings THEN the Peace System SHALL display all available languages with the current selection highlighted
2. WHEN the user selects a new language THEN the Peace System SHALL apply the language change immediately without requiring an app restart
3. WHEN the user selects "System Default" THEN the Peace System SHALL use the device's current language setting
4. WHEN the language is changed THEN the Peace System SHALL persist the selection across app restarts
5. WHEN the app launches THEN the Peace System SHALL load the previously selected language before displaying any UI

### Requirement 2: Custom Font System

**User Story:** As a user, I want to choose between system fonts and custom bundled fonts with adjustable padding, so that I can personalize the visual appearance and spacing of text in the app.

#### Acceptance Criteria

1. WHEN the user opens font settings THEN the Peace System SHALL display all available fonts with live preview text
2. WHEN the user selects a font THEN the Peace System SHALL apply it to all text elements immediately
3. WHEN the user selects "System Font" THEN the Peace System SHALL use the Android default typeface
4. WHEN a custom font is selected THEN the Peace System SHALL persist the selection across app restarts
5. WHEN the app launches with a custom font selected THEN the Peace System SHALL load the font before rendering any text
6. WHEN the user adjusts font padding THEN the Peace System SHALL apply the padding value to all text elements in real-time
7. WHEN the user sets font padding THEN the Peace System SHALL persist the padding value (0-20dp) across app restarts
8. WHEN the font padding is changed THEN the Peace System SHALL update line height and letter spacing proportionally

### Requirement 3: Ionicons Integration

**User Story:** As a developer, I want to integrate the Ionicons icon pack throughout the entire application, so that the app has a unique visual identity instead of using default Material Icons.

#### Acceptance Criteria

1. WHEN the Peace System builds THEN the Peace System SHALL convert all Ionicons SVG files to Android Vector Drawables
2. WHEN the Peace System renders any UI icon THEN the Peace System SHALL use icons from the Ionicons pack instead of Material Icons
3. WHEN a UI component requests an icon THEN the Peace System SHALL provide the corresponding Ionicons vector drawable from resources
4. WHEN the Ionicons pack is missing a required icon THEN the Peace System SHALL log a warning and use a fallback icon from Material Icons or other sources
5. WHEN the Peace Garden renders THEN the Peace System SHALL use Ionicons for all visual elements including growth stages and category indicators
6. WHEN the app theme changes THEN the Peace System SHALL apply appropriate tinting to Ionicons to match the theme colors

### Requirement 4: Subtasks and Checklists

**User Story:** As a user, I want to break down reminders into unlimited subtasks with checkboxes, so that I can track progress on complex tasks.

#### Acceptance Criteria

1. WHEN the user adds a subtask to a reminder THEN the Peace System SHALL create a new subtask entity linked to the parent reminder
2. WHEN the user checks a subtask checkbox THEN the Peace System SHALL update the subtask completion state in real-time
3. WHEN subtasks are completed THEN the Peace System SHALL display a visual progress bar showing completion percentage
4. WHEN the user views a reminder with subtasks THEN the Peace System SHALL display all subtasks with their current completion states
5. WHEN the user deletes a subtask THEN the Peace System SHALL remove it and recalculate the progress bar immediately
6. WHEN all subtasks are completed THEN the Peace System SHALL update the parent reminder progress to 100%

### Requirement 5: Notes and Attachments

**User Story:** As a user, I want to add multiple notes and image attachments to reminders, so that I can provide rich context for my tasks.

#### Acceptance Criteria

1. WHEN the user adds a note to a reminder THEN the Peace System SHALL store the note with a timestamp
2. WHEN the user attaches an image THEN the Peace System SHALL store the image locally and display a thumbnail preview
3. WHEN the user views a reminder with attachments THEN the Peace System SHALL display all notes and images in chronological order
4. WHEN the user deletes a note or attachment THEN the Peace System SHALL remove it from storage immediately
5. WHEN the user adds multiple images THEN the Peace System SHALL allow unlimited image attachments per reminder

### Requirement 6: Background Image Customization

**User Story:** As a user, I want to set attachment images as screen backgrounds with adjustable blur, so that I can create a personalized visual experience.

#### Acceptance Criteria

1. WHEN the user selects an attachment image as background THEN the Peace System SHALL apply it to the current screen
2. WHEN the user adjusts blur intensity THEN the Peace System SHALL update the background blur in real-time
3. WHEN multiple images are set as background THEN the Peace System SHALL cycle through them in a slideshow
4. WHEN the slideshow is active THEN the Peace System SHALL transition between images every 5 seconds
5. WHEN the user sets blur intensity THEN the Peace System SHALL persist the value (0-100) in settings
6. WHEN the user disables background images THEN the Peace System SHALL revert to the default theme background

### Requirement 7: Custom Alarm Sounds

**User Story:** As a user, I want to choose custom alarm sounds for each reminder, so that I can differentiate between different types of tasks.

#### Acceptance Criteria

1. WHEN the user opens alarm sound settings for a reminder THEN the Peace System SHALL display all available system sounds and custom sounds
2. WHEN the user selects a sound THEN the Peace System SHALL play a preview of the sound
3. WHEN the user saves a custom sound selection THEN the Peace System SHALL associate it with the specific reminder
4. WHEN a reminder alarm triggers THEN the Peace System SHALL play the custom sound if one is set, otherwise the default sound
5. WHEN the user tests a sound THEN the Peace System SHALL play it at the configured volume without triggering a full alarm

### Requirement 8: Google Calendar Integration

**User Story:** As a user, I want to sync my reminders to Google Calendar, so that I can view them alongside my other calendar events.

#### Acceptance Criteria

1. WHEN the user enables calendar sync THEN the Peace System SHALL request Google Calendar permissions
2. WHEN permissions are granted THEN the Peace System SHALL create a dedicated "Peace Reminders" calendar
3. WHEN the user manually triggers sync THEN the Peace System SHALL export all active reminders to Google Calendar as events
4. WHEN a reminder is created or updated THEN the Peace System SHALL update the corresponding calendar event if sync is enabled
5. WHEN the user views sync statistics THEN the Peace System SHALL display the last sync time and number of synced reminders
6. WHEN the user disables sync THEN the Peace System SHALL stop creating new calendar events but preserve existing ones

### Requirement 9: Reminder Sharing via Deep Links

**User Story:** As a user, I want to share reminders with other Peace app users via messaging apps, so that I can coordinate on tasks.

#### Acceptance Criteria

1. WHEN the user initiates reminder sharing THEN the Peace System SHALL display available sharing methods (SMS, WhatsApp, email, other messaging apps)
2. WHEN the user selects a sharing method THEN the Peace System SHALL generate a deep link with encoded reminder data
3. WHEN a recipient with the Peace app installed opens the deep link THEN the Peace System SHALL import the reminder into their app
4. WHEN a recipient without the Peace app opens the deep link THEN the Android system SHALL prompt them to install the app from the Play Store
5. WHEN the recipient imports a shared reminder THEN the Peace System SHALL create a new independent copy in their local database
6. WHEN the sharing user updates their original reminder THEN the Peace System SHALL NOT sync changes to the recipient's copy
7. WHEN the deep link is generated THEN the Peace System SHALL include all reminder data (title, time, priority, category, recurrence, nag mode settings)

### Requirement 12: Smart ML Suggestions

**User Story:** As a user, I want to receive intelligent suggestions based on my usage patterns, so that I can optimize my productivity.

#### Acceptance Criteria

1. WHEN the Peace System analyzes user behavior THEN the Peace System SHALL identify patterns in task completion times, priorities, and recurrence
2. WHEN the Peace System detects a suboptimal reminder time THEN the Peace System SHALL suggest an optimal time with confidence score
3. WHEN the Peace System detects incorrect priority levels THEN the Peace System SHALL suggest priority adjustments based on completion patterns
4. WHEN the Peace System detects recurring manual task creation THEN the Peace System SHALL suggest converting to a recurring reminder
5. WHEN the user works for extended periods THEN the Peace System SHALL suggest break reminders based on focus session duration
6. WHEN the Peace System detects consistent task completion THEN the Peace System SHALL suggest habit formation reminders
7. WHEN the user frequently creates similar reminders THEN the Peace System SHALL suggest creating a template
8. WHEN the user has long focus sessions THEN the Peace System SHALL suggest optimal focus session durations
9. WHEN a suggestion is displayed THEN the Peace System SHALL include a confidence score (0-100%)
10. WHEN the user applies a suggestion THEN the Peace System SHALL update the reminder and record the acceptance for future learning
11. WHEN the user dismisses a suggestion THEN the Peace System SHALL record the dismissal and adjust future suggestion algorithms

### Requirement 13: Feature Toggle System

**User Story:** As a user, I want to enable or disable advanced features in settings, so that I can customize my app experience.

#### Acceptance Criteria

1. WHEN the user opens feature settings THEN the Peace System SHALL display all toggleable features with their current states
2. WHEN the user disables a feature THEN the Peace System SHALL hide all UI elements related to that feature immediately
3. WHEN the user enables a feature THEN the Peace System SHALL make all related UI elements accessible immediately
4. WHEN a feature is toggled THEN the Peace System SHALL persist the state across app restarts
5. WHEN the user disables ML suggestions THEN the Peace System SHALL stop analyzing patterns and displaying suggestions
6. WHEN the user disables collaborative features THEN the Peace System SHALL hide sharing and collaboration options
7. WHEN the user disables calendar sync THEN the Peace System SHALL stop syncing but preserve existing calendar events

### Requirement 14: Enhanced Notification System

**User Story:** As a user, I want a redesigned notification area with working action buttons, so that I can interact with reminders effectively from notifications.

#### Acceptance Criteria

1. WHEN a reminder alarm triggers THEN the Peace System SHALL display a notification with custom layout and branding
2. WHEN the user taps the "Complete" button in a notification THEN the Peace System SHALL mark the reminder as complete and update the Peace Garden
3. WHEN the user taps the "Snooze" button in a notification THEN the Peace System SHALL enter nag mode if enabled, otherwise snooze for the configured duration
4. WHEN the user taps the "Dismiss" button in a notification THEN the Peace System SHALL cancel the alarm and remove the notification
5. WHEN multiple reminders trigger simultaneously THEN the Peace System SHALL display a bundled notification with expandable details
6. WHEN the notification is displayed THEN the Peace System SHALL show the reminder title, time, priority indicator, and category icon
7. WHEN the user interacts with notification actions THEN the Peace System SHALL provide haptic feedback and visual confirmation
8. WHEN nag mode is active THEN the Peace System SHALL display a distinct notification style indicating the panic loop state

### Requirement 15: Nag Mode Repetition System

**Note:** This feature is already implemented in the current app. This requirement documents the existing behavior for reference.

**User Story:** As a user, I want to create reminders with multiple repetitions at custom intervals, so that I can track recurring tasks throughout the day without creating separate reminders.

#### Acceptance Criteria

1. WHEN the user enables nag mode for a reminder THEN the Peace System SHALL allow setting the total number of repetitions and interval duration between repetitions
2. WHEN the user completes a repetition THEN the Peace System SHALL increment the repetition counter and schedule the next repetition based on the selected mode
3. WHEN flex mode is enabled THEN the Peace System SHALL schedule each repetition relative to the completion time of the previous repetition
4. WHEN strict mode is enabled THEN the Peace System SHALL schedule all repetitions anchored to the original start time with fixed intervals
5. WHEN the user completes the final repetition THEN the Peace System SHALL mark the entire reminder as complete and archive it to history
6. WHEN the user views a nag mode reminder THEN the Peace System SHALL display the current repetition number and total repetitions (e.g., "3/5")
7. WHEN strict mode detects a missed repetition THEN the Peace System SHALL skip the missed repetition and schedule the next future repetition
8. WHEN the user snoozes a nag mode reminder THEN the Peace System SHALL enter panic loop mode for that specific repetition

### Requirement 16: Panic Loop Behavior

**Note:** This feature is already implemented in the current app. This requirement documents the existing behavior for reference.

**User Story:** As a user, I want persistent reminders when I snooze, so that I cannot indefinitely postpone important tasks.

#### Acceptance Criteria

1. WHEN the user snoozes a reminder THEN the Peace System SHALL enter panic loop mode and schedule the next alarm in 2 minutes
2. WHEN panic loop is active THEN the Peace System SHALL track the panic loop start time
3. WHEN 30 minutes elapse in panic loop THEN the Peace System SHALL exit panic loop and advance to the next repetition or mark complete
4. WHEN the user completes a task during panic loop THEN the Peace System SHALL exit panic loop and schedule the next repetition normally
5. WHEN the user snoozes during panic loop THEN the Peace System SHALL reschedule the alarm for 2 minutes later while maintaining the original panic loop start time
6. WHEN panic loop times out on the final repetition THEN the Peace System SHALL mark the entire reminder as complete

### Requirement 17: Home Screen Widgets

**User Story:** As a user, I want home screen widgets to view and manage reminders without opening the app, so that I can stay productive from my home screen.

#### Acceptance Criteria

1. WHEN the user adds the Today's Reminders widget THEN the Peace System SHALL display all reminders scheduled for the current day
2. WHEN a reminder is completed or updated THEN the Peace System SHALL refresh the widget display within 5 seconds
3. WHEN the user taps a reminder in the widget THEN the Peace System SHALL open the app to the reminder detail screen
4. WHEN the user adds the Peace Garden widget THEN the Peace System SHALL display the current garden state and completion streak
5. WHEN the Peace Garden updates THEN the Peace System SHALL refresh the widget to show the new growth stage
6. WHEN the user adds the Quick-Add widget THEN the Peace System SHALL provide a text input field and add button
7. WHEN the user enters text in the Quick-Add widget THEN the Peace System SHALL parse it using Gemini AI and create a reminder
8. WHEN the user taps the Quick-Add button THEN the Peace System SHALL create the reminder and display a confirmation toast
9. WHEN widgets are displayed THEN the Peace System SHALL use Ionicons for all widget icons
10. WHEN the app theme changes THEN the Peace System SHALL update widget styling to match the current theme

### Requirement 18: Enhanced Peace Garden

**User Story:** As a user, I want an enhanced Peace Garden with multiple themes and streak tracking, so that I can visualize my progress in different ways and stay motivated.

#### Acceptance Criteria

1. WHEN the user opens Peace Garden settings THEN the Peace System SHALL display all available themes (zen, forest, desert, ocean)
2. WHEN the user selects a garden theme THEN the Peace System SHALL apply the theme immediately with theme-specific icons and colors
3. WHEN the user completes tasks THEN the Peace System SHALL advance through multiple growth stages with visual progression
4. WHEN the Peace Garden reaches a new growth stage THEN the Peace System SHALL display a celebration animation with haptic feedback
5. WHEN the user completes tasks on consecutive days THEN the Peace System SHALL track the completion streak
6. WHEN the user reaches streak milestones THEN the Peace System SHALL display achievement notifications (7, 30, 100, 365 days)
7. WHEN the user views the Peace Garden THEN the Peace System SHALL display the current streak count and next milestone
8. WHEN the user breaks a streak THEN the Peace System SHALL reset the counter and display an encouraging message
9. WHEN the Peace Garden theme is changed THEN the Peace System SHALL persist the selection across app restarts
10. WHEN the Peace Garden renders THEN the Peace System SHALL use Ionicons for growth stage indicators and theme-specific elements

### Requirement 19: Notification Action Reliability

**User Story:** As a user, I want all notification buttons to work correctly, so that I can manage reminders without opening the app.

#### Acceptance Criteria

1. WHEN the user taps any notification action button THEN the Peace System SHALL execute the corresponding action within 500 milliseconds
2. WHEN a notification action fails THEN the Peace System SHALL log the error and display a fallback notification prompting the user to open the app
3. WHEN the user taps "Complete" on a multi-repetition nag mode reminder THEN the Peace System SHALL advance to the next repetition or mark complete if final
4. WHEN the user taps "Snooze" during nag mode THEN the Peace System SHALL enter panic loop mode and schedule the next alarm in 2 minutes
5. WHEN the user taps "Dismiss" THEN the Peace System SHALL stop the alarm service and cancel all pending alarms for that reminder instance
6. WHEN the notification displays a nag mode reminder THEN the Peace System SHALL show the current repetition progress (e.g., "2 of 5 complete")
