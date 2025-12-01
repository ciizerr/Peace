# Implementation Plan

## Phase 1: Foundation & Database

- [x] 1. Database migration and schema updates





  - Create migration from v1 to v2 with new tables (subtasks, notes, attachments, garden_state, suggestions)
  - Add new columns to reminders table (customAlarmSoundUri, customAlarmSoundName)
  - Create indexes for foreign keys
  - Initialize garden_state with default values
  - Test migration on existing database
  - _Requirements: 4.1, 5.1, 5.2, 6.5, 7.3, 18.3_

- [x] 1.1 Write property test for database migration


  - **Property 6: Subtask-reminder linkage**
  - **Validates: Requirements 4.1**

- [x] 2. Create data models and entities





  - Create SubtaskEntity, Subtask domain model, and DAO
  - Create NoteEntity, Note domain model, and DAO
  - Create AttachmentEntity, Attachment domain model, and DAO
  - Create GardenEntity, GardenState domain model, and DAO
  - Create SuggestionEntity, Suggestion domain model, and DAO
  - Add type converters for new enums (GardenTheme, SuggestionType, SuggestionStatus)
  - _Requirements: 4.1, 5.1, 5.2, 18.3, 12.2_

- [x] 2.1 Write property test for subtask linkage


  - **Property 6: Subtask-reminder linkage**
  - **Validates: Requirements 4.1**

- [x] 3. Update repositories





  - Create SubtaskRepository with CRUD operations
  - Create NoteRepository with CRUD operations
  - Create AttachmentRepository with CRUD operations
  - Create GardenRepository with state management
  - Create SuggestionRepository with CRUD operations
  - Update ReminderRepository to handle new fields
  - _Requirements: 4.1, 5.1, 5.2, 18.3, 12.2_

- [x] 4. Add new dependencies





  - Add Glance for widgets
  - Add Coil for image loading
  - Add Kotlinx Serialization for deep links
  - Add Google Calendar API dependencies
  - Add Kotest for property testing
  - Add WorkManager for background tasks
  - Add Accompanist Permissions
  - Sync Gradle and verify build
  - _Requirements: 5.2, 8.1, 9.2, 17.1_

- [x] 5. Create PreferencesRepository extensions





  - Add language selection preference
  - Add font selection preference
  - Add font padding preference
  - Add blur intensity preference
  - Add slideshow enabled preference
  - Add calendar sync enabled preference
  - Add ML suggestions enabled preference
  - Add feature toggle preferences (subtasks, attachments, widgets)
  - _Requirements: 1.4, 2.4, 2.6, 6.5, 13.4_

- [x] 5.1 Write property test for preference persistence


  - **Property 1: Language persistence round-trip**
  - **Property 2: Font persistence round-trip**
  - **Property 15: Blur intensity persistence**
  - **Property 25: Feature toggle persistence**
  - **Validates: Requirements 1.4, 2.4, 6.5, 13.4**

- [x] 6. Checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.

## Phase 2: Ionicons Integration

- [x] 7. Convert Ionicons SVG to Vector Drawables










  - Set up SVG to Vector Drawable conversion tool
  - Batch convert all SVG files from `C:\Users\mitsu\Downloads\ionicons.designerpack`
  - Place converted XML files in `app/src/main/res/drawable/`
  - Use naming convention: `ic_ionicons_[name].xml`
  - Verify all icons render correctly
  - _Requirements: 3.1, 3.2_

- [x] 8. Create IconManager system





  - Create IconManager interface
  - Implement IoniconsManager with resource loading
  - Create icon cache for performance
  - Implement fallback icon logic for missing icons
  - Create IconMapper for name-to-resource mapping
  - _Requirements: 3.2, 3.4_

- [x] 8.1 Write property test for icon loading


  - **Property 4: Ionicons usage consistency**
  - **Property 5: Icon fallback handling**
  - **Validates: Requirements 3.2, 3.4**

- [x] 9. Create Icon composable




  - Create custom Icon composable that uses IconManager
  - Support icon tinting based on theme
  - Add content descriptions for accessibility
  - _Requirements: 3.2, 3.6_


- [x] 10. Replace Material Icons with Ionicons




  - Update HomeScreen icons
  - Update AddEditReminderScreen icons
  - Update SettingsScreen icons
  - Update ReminderDetailScreen icons
  - Update notification icons
  - Update Peace Garden icons
  - _Requirements: 3.2, 3.5_

- [x] 11. Checkpoint - Ensure all tests pass




  - Ensure all tests pass, ask the user if questions arise.

## Phase 3: Custom Fonts Integration

- [x] 12. Prepare custom fonts




  - Copy font files from `C:\Users\mitsu\Downloads\Font_folder`
  - Place in `app/src/main/res/font/` directory
  - Use naming convention: lowercase with underscores
  - Create font family definitions for each font
  - _Requirements: 2.1, 2.3_


- [x] 13. Create FontManager system




  - Create FontManager interface
  - Implement FontManagerImpl with font loading
  - Create CustomFont data class with preview text
  - Load all fonts from res/font directory
  - Implement system font fallback
  - _Requirements: 2.1, 2.3, 2.5_


- [x] 14. Update theme system for custom fonts




  - Create rememberFontFamily() composable
  - Update PeaceTheme to accept custom typography
  - Implement font padding via CompositionLocal
  - Apply font to all text elements
  - _Requirements: 2.2, 2.5, 2.6_

- [x] 14.1 Write property test for font application







  - **Property 3: Font padding application**
  - **Validates: Requirements 2.6**

- [x] 15. Create font settings UI





  - Create FontSettingsScreen with font list
  - Display live preview for each font
  - Add font padding slider (0-20dp)
  - Implement immediate font application
  - Add system font option
  - _Requirements: 2.1, 2.2, 2.6_

- [x] 16. Checkpoint - Ensure all tests pass




  - Ensure all tests pass, ask the user if questions arise.

## Phase 4: Subtasks & Checklists

- [x] 17. Create subtask UI components




  - Create SubtaskItem composable with checkbox
  - Create SubtaskList composable
  - Create AddSubtaskDialog
  - Implement checkbox animations
  - Add reordering support (drag and drop)
  - _Requirements: 4.1, 4.2, 4.4_

- [x] 18. Implement subtask logic





  - Create AddSubtaskUseCase
  - Create UpdateSubtaskUseCase
  - Create DeleteSubtaskUseCase
  - Create GetSubtasksForReminderUseCase
  - Implement subtask ordering logic
  - _Requirements: 4.1, 4.2, 4.5_

- [x] 18.1 Write property test for subtask operations


  - **Property 7: Subtask completion state update**
  - **Validates: Requirements 4.2**


- [x] 19. Create progress calculation system






  - Create CalculateProgressUseCase
  - Implement progress percentage calculation
  - Create ProgressBar composable
  - Update progress on subtask changes
  - _Requirements: 4.3, 4.5, 4.6_

- [x] 19.1 Write property test for progress calculation



  - **Property 8: Progress calculation accuracy**
  - **Property 9: Progress recalculation on deletion**
  - **Validates: Requirements 4.3, 4.5**

- [ ] 20. Integrate subtasks into ReminderDetailScreen



  - Add subtasks section to detail screen
  - Display progress bar
  - Add "Add subtask" button
  - Implement real-time updates
  - Add delete confirmation dialog
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [ ] 21. Checkpoint - Ensure all tests pass

  - Ensure all tests pass, ask the user if questions arise.

## Phase 5: Notes & Attachments

- [x] 22. Create note UI components




  - Create NoteItem composable with timestamp
  - Create NoteList composable
  - Create AddNoteDialog
  - Implement note deletion with confirmation
  - _Requirements: 5.1, 5.3, 5.4_

- [x] 23. Implement note logic




  - Create AddNoteUseCase
  - Create DeleteNoteUseCase
  - Create GetNotesForReminderUseCase
  - Implement chronological sorting
  - _Requirements: 5.1, 5.3, 5.4_

- [x] 23.1 Write property test for note operations

  - **Property 10: Note timestamp inclusion**
  - **Property 12: Chronological attachment ordering**
  - **Validates: Requirements 5.1, 5.3**

- [x] 24. Create attachment system





  - Create AttachmentManager for file operations
  - Implement image picker integration
  - Create thumbnail generation logic
  - Implement file storage in app-private directory
  - Add file size validation (max 5MB)
  - _Requirements: 5.2, 5.4_
-

- [x] 25. Create attachment UI components



  - Create AttachmentItem composable with thumbnail
  - Create AttachmentGrid composable
  - Create ImagePickerDialog
  - Implement full-screen image viewer
  - Add delete confirmation dialog
  - _Requirements: 5.2, 5.3, 5.4_

- [x] 25.1 Write property test for attachment operations







  - **Property 11: Attachment storage and thumbnail**
  - **Property 13: Attachment deletion completeness**
  - **Validates: Requirements 5.2, 5.4**

- [x] 26. Integrate notes and attachments into ReminderDetailScreen




  - Add notes section to detail screen
  - Add attachments section to detail screen
  - Implement chronological ordering
  - Add "Add note" and "Add image" buttons
  - _Requirements: 5.1, 5.2, 5.3_

- [x] 27. Checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.

## Phase 6: Background Image Customization

- [x] 28. Create background image system





  - Create BackgroundImageManager
  - Implement image-to-background conversion
  - Create blur effect composable
  - Implement slideshow logic with 5-second intervals
  - Add background image cache
  - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 28.1 Write property test for background operations


  - **Property 14: Background image application**
  - **Validates: Requirements 6.1**

- [x] 29. Create background settings UI


  - Add background image toggle to settings
  - Create blur intensity slider (0-100)
  - Add slideshow toggle
  - Implement real-time blur preview
  - _Requirements: 6.1, 6.2, 6.3, 6.5, 6.6_

- [x] 30. Integrate background system into screens





  - Add background support to HomeScreen
  - Add background support to ReminderDetailScreen
  - Add background support to SettingsScreen
  - Implement theme fallback when disabled
  - _Requirements: 6.1, 6.6_

- [x] 31. Checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.

## Phase 7: Custom Alarm Sounds

- [x] 32. Create alarm sound system





  - Create AlarmSound data class
  - Create AlarmSoundManager for sound loading
  - Implement system sound picker integration
  - Add custom sound file picker
  - Create sound preview player
  - _Requirements: 7.1, 7.2, 7.5_

- [x] 33. Create alarm sound UI





  - Create AlarmSoundPickerDialog
  - Display system sounds and custom sounds
  - Add sound preview button for each sound
  - Implement sound selection persistence
  - _Requirements: 7.1, 7.2, 7.3_

- [x] 33.1 Write property test for alarm sound operations


  - **Property 16: Alarm sound association**
  - **Property 17: Alarm sound playback selection**
  - **Validates: Requirements 7.3, 7.4**

- [x] 34. Update alarm trigger logic





  - Update ReminderService to use custom sounds
  - Implement sound fallback to default
  - Add sound volume control
  - Test alarm sound playback
  - _Requirements: 7.4_

- [x] 35. Integrate alarm sound picker into AddEditReminderScreen




  - Add alarm sound selection field
  - Display selected sound name
  - Add "Test sound" button
  - _Requirements: 7.1, 7.2, 7.3, 7.5_

- [x] 36. Checkpoint - Ensure all tests pass




  - Ensure all tests pass, ask the user if questions arise.

## Phase 8: Language Selection

- [x] 37. Set up localization infrastructure




  - Create string resources for all supported languages
  - Set up locale configuration
  - Create language list (English, Spanish, French, German, Hindi, etc.)
  - _Requirements: 1.1, 1.3_
-

- [x] 38. Create language selection system



  - Create LanguageManager for locale management
  - Implement language change without restart
  - Add "System Default" option
  - Implement language persistence
  - _Requirements: 1.2, 1.3, 1.4, 1.5_

- [x] 39. Create language settings UI




  - Create LanguageSettingsScreen
  - Display all available languages
  - Highlight current selection
  - Implement immediate language application
  - _Requirements: 1.1, 1.2_

- [x] 40. Checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.

## Phase 9: Enhanced Peace Garden

- [x] 41. Create garden theme system





  - Define 4 garden themes (Zen, Forest, Desert, Ocean)
  - Create theme-specific color palettes
  - Create theme-specific icon sets
  - Implement theme switching logic
  - _Requirements: 18.1, 18.2, 18.9_

- [x] 41.1 Write property test for garden theme


  - **Property 30: Garden theme application**
  - **Validates: Requirements 18.2**

- [x] 42. Implement growth stage system




  - Define 10 growth stages
  - Create growth stage visualizations for each theme
  - Implement growth stage advancement logic
  - Add celebration animations
  - _Requirements: 18.3, 18.4_

- [x] 42.1 Write property test for growth stages

  - **Property 31: Growth stage advancement**
  - **Validates: Requirements 18.3**

- [x] 43. Implement streak tracking





  - Create streak calculation logic
  - Implement daily completion check
  - Add streak reset logic for missed days
  - Track longest streak
  - _Requirements: 18.5, 18.8_

- [x] 43.1 Write property test for streak tracking


  - **Property 32: Streak calculation**
  - **Property 34: Streak reset**
  - **Validates: Requirements 18.5, 18.8**

- [x] 44. Implement milestone system




  - Define milestones (7, 30, 100, 365 days)
  - Create milestone detection logic
  - Implement achievement notifications
  - Add milestone celebration animations
  - _Requirements: 18.6, 18.7_

- [x] 44.1 Write property test for milestones


  - **Property 33: Milestone detection**
  - **Validates: Requirements 18.6**

- [x] 45. Update Peace Garden UI




  - Add theme selector tabs
  - Update garden visualization
  - Add streak display
  - Add milestone progress display
  - Add recent achievements list
  - _Requirements: 18.1, 18.2, 18.3, 18.5, 18.6, 18.7_

- [x] 46. Integrate garden updates with task completion




  - Update garden state on task completion
  - Trigger growth stage advancement
  - Update streak counter
  - Check for milestone achievements
  - _Requirements: 18.3, 18.5, 18.6_

- [x] 47. Checkpoint - Ensure all tests pass




  - Ensure all tests pass, ask the user if questions arise.

## Phase 10: Google Calendar Integration

- [x] 48. Set up Google Calendar API




  - Add Google Play Services dependencies
  - Configure OAuth 2.0 credentials
  - Implement permission request flow
  - Create CalendarManager for API interactions
  - _Requirements: 8.1_

- [x] 49. Implement calendar sync logic





  - Create "Peace Reminders" calendar
  - Implement reminder-to-event conversion
  - Create sync all reminders function
  - Implement individual reminder sync
  - Add sync statistics tracking
  - _Requirements: 8.2, 8.3, 8.4, 8.5_

- [x] 49.1 Write property test for calendar sync


  - **Property 18: Calendar sync completeness**
  - **Property 19: Calendar event synchronization**
  - **Validates: Requirements 8.3, 8.4**

- [x] 50. Create calendar sync UI



  - Add calendar sync toggle to settings
  - Add manual sync button
  - Display sync statistics
  - Show last sync time
  - Add sync error handling UI
  - _Requirements: 8.3, 8.5, 8.6_

- [x] 51. Implement sync error handling





  - Handle permission denial
  - Handle network errors
  - Implement retry with exponential backoff
  - Add offline sync queue
  - _Requirements: 8.1, 8.3_
-

- [x] 52. Checkpoint - Ensure all tests pass



  - Ensure all tests pass, ask the user if questions arise.

## Phase 11: Deep Link Sharing

- [x] 53. Create deep link system





  - Create DeepLinkHandler for encoding/decoding
  - Implement reminder data serialization
  - Create deep link URI format
  - Add Base64 encoding for data
  - _Requirements: 9.2, 9.7_

- [x] 53.1 Write property test for deep links


  - **Property 20: Deep link round-trip**
  - **Property 21: Deep link import**
  - **Validates: Requirements 9.2, 9.3, 9.7**

- [x] 54. Implement deep link handling




  - Add intent filter for deep links in AndroidManifest
  - Create DeepLinkActivity for link processing
  - Implement reminder import logic
  - Add error handling for invalid links
  - _Requirements: 9.3, 9.4_


- [x] 55. Create sharing UI




  - Add share button to ReminderDetailScreen
  - Implement Android share sheet integration
  - Generate deep link on share
  - Add share confirmation toast
  - _Requirements: 9.1, 9.2_


- [x] 56. Test deep link sharing




  - Test sharing via SMS
  - Test sharing via WhatsApp
  - Test sharing via email
  - Test app-not-installed scenario
  - _Requirements: 9.1, 9.3, 9.4_


- [x] 57. Checkpoint - Ensure all tests pass






  - Ensure all tests pass, ask the user if questions arise.

## Phase 12: ML Suggestions System

- [x] 58. Create ML data collection




  - Create CompletionEvent data class
  - Implement completion tracking
  - Store completion history (last 90 days)
  - Track task creation patterns
  - _Requirements: 12.1_

- [x] 59. Implement pattern analysis




  - Create PatternAnalyzer interface
  - Implement completion time analysis
  - Implement priority pattern analysis
  - Implement recurring pattern detection
  - Implement focus session analysis
  - _Requirements: 12.1, 12.2, 12.3, 12.4, 12.8_

- [x] 60. Create suggestion generator




  - Create SuggestionGenerator
  - Implement optimal time suggestions
  - Implement priority adjustment suggestions
  - Implement recurring pattern suggestions
  - Implement break reminder suggestions
  - Implement habit formation suggestions
  - Implement template creation suggestions
  - Implement focus session suggestions
  - Add confidence score calculation
  - _Requirements: 12.2, 12.3, 12.4, 12.5, 12.6, 12.7, 12.8, 12.9_

- [x] 60.1 Write property test for suggestions



  - **Property 22: Suggestion confidence score validity**
  - **Validates: Requirements 12.9**

- [x] 61. Implement suggestion learning




  - Create LearningRepository
  - Track suggestion acceptance/dismissal
  - Implement feedback-based adjustment
  - Add suggestion frequency throttling
  - _Requirements: 12.10, 12.11_

- [x] 61.1 Write property test for suggestion learning


  - **Property 23: Suggestion application side effects**
  - **Validates: Requirements 12.10**

- [x] 62. Create ML suggestions UI



  - Create SuggestionsScreen
  - Display suggestions with confidence scores
  - Add apply/dismiss buttons
  - Show suggestion explanations
  - Add empty state for insufficient data
  - _Requirements: 12.2, 12.9, 12.10, 12.11_



- [x] 63. Implement background analysis


  - Create AnalysisWorker with WorkManager
  - Schedule daily analysis
  - Implement analysis timeout (30 seconds)
  - Add notification for new suggestions
  - _Requirements: 12.1_

- [x] 64. Checkpoint - Ensure all tests pass








  - Ensure all tests pass, ask the user if questions arise.

## Phase 13: Feature Toggle System

- [x] 65. Implement feature toggle logic





  - Create FeatureToggleManager
  - Implement toggle state management
  - Add toggle persistence
  - Create feature flag checks
  - _Requirements: 13.1, 13.4_

- [x] 65.1 Write property test for feature toggles


  - **Property 24: Feature toggle UI hiding**
  - **Property 25: Feature toggle persistence**
  - **Validates: Requirements 13.2, 13.4**
- [x] 66. Create feature settings UI









- [ ] 66. Create feature settings UI

  - Add feature toggles section to settings
  - Display all toggleable features
  - Implement immediate toggle effects
  - Add feature descriptions
  - _Requirements: 13.1, 13.2, 13.3_

- [x] 67. Integrate feature toggles throughout app





  - Hide subtasks UI when disabled
  - Hide attachments UI when disabled
  - Hide ML suggestions when disabled
  - Hide calendar sync when disabled
  - Hide widgets when disabled
  - _Requirements: 13.2, 13.5, 13.6, 13.7_

- [x] 68. Checkpoint - Ensure all tests pass



  - Ensure all tests pass, ask the user if questions arise.

## Phase 14: Home Screen Widgets


- [x] 69. Create Today's Reminders Widget



  - Create TodayWidgetProvider with Glance
  - Implement widget layout
  - Display today's reminders
  - Add click handling to open app
  - Implement widget updates
  - _Requirements: 17.1, 17.2, 17.3, 17.9_

- [x] 70. Create Peace Garden Widget




  - Create GardenWidgetProvider with Glance
  - Implement widget layout
  - Display garden state and streak
  - Add click handling to open garden
  - Implement widget updates
  - _Requirements: 17.4, 17.5, 17.9_

- [x] 71. Create Quick-Add Widget





  - Create QuickAddWidgetProvider with Glance
  - Implement widget layout with text input
  - Add Gemini AI parsing integration
  - Create reminder on button click
  - Show confirmation toast
  - _Requirements: 17.6, 17.7, 17.8_

- [x] 72. Implement widget update system




  - Create WidgetUpdateManager
  - Trigger updates on data changes
  - Implement throttling (max once per minute)
  - Add theme support for widgets
  - _Requirements: 17.2, 17.5, 17.10_

- [x] 73. Checkpoint - Ensure all tests pass


  - Ensure all tests pass, ask the user if questions arise.

## Phase 15: Enhanced Notification System

- [x] 74. Redesign notification layout



  - Create custom notification layout
  - Add priority indicator
  - Add category icon
  - Display nag mode progress
  - Display subtask progress
  - Add Peace branding
  - _Requirements: 14.1, 14.6, 19.6_

- [x] 75. Implement notification actions





  - Create "Complete" action with proper handling
  - Create "Snooze" action with panic loop
  - Create "Dismiss" action with alarm cancellation
  - Add haptic feedback for actions
  - Add visual confirmation
  - _Requirements: 14.2, 14.3, 14.4, 14.7, 19.1, 19.4, 19.5_

- [x] 75.1 Write property test for notification actions

  - **Property 26: Notification completion side effects**
  - **Property 28: Nag mode progression**
  - **Property 29: Panic loop activation**
  - **Validates: Requirements 14.2, 19.3, 19.4**

- [x] 76. Implement notification bundling



  - Detect simultaneous reminders (1-minute window)
  - Create bundled notification
  - Add expandable details
  - Sort by priority
  - _Requirements: 14.5_

- [x] 76.1 Write property test for notification bundling

  - **Property 27: Notification bundling**
  - **Validates: Requirements 14.5**

- [x] 77. Update nag mode notification style



  - Create distinct panic loop notification style
  - Add panic loop indicator
  - Update notification text for panic loop
  - _Requirements: 14.8_

- [x] 78. Fix notification action reliability





  - Debug and fix "Dismiss" button
  - Add error logging for failed actions
  - Implement fallback notification on errors
  - Add action timeout handling (500ms)
  - _Requirements: 19.1, 19.2_

- [x] 79. Checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.

## Phase 16: Testing & Polish

- [x] 80. Write remaining property tests





  - Set up Kotest property testing framework
  - Create custom generators (Arb.reminder(), Arb.subtask(), etc.)
  - Implement all 34 correctness properties as property tests
  - Configure 100+ iterations per test
  - Run full property test suite
  - _Requirements: All_

- [x] 81. Write integration tests




- [ ] 81. Write integration tests
  - Database integration tests (foreign keys, cascades, migrations)
  - Widget integration tests (updates, clicks)
  - Calendar integration tests (sync, errors)
  - Deep link integration tests (encoding, decoding, import)
  - _Requirements: All_

- [x] 82. Write UI tests



  - Subtask interaction tests
  - Progress bar rendering tests
  - Font preview tests
  - Icon display tests
  - Theme switching tests
  - Feature toggle tests
  - _Requirements: All_

- [x] 83. Performance optimization




- [ ] 83. Performance optimization
  - Optimize icon loading with caching
  - Optimize image loading with Coil
  - Optimize database queries with indexes
  - Optimize widget updates with throttling
  - Profile app startup time
  - _Requirements: All_
-

- [x] 84. Accessibility improvements



  - Add content descriptions to all icons
  - Verify touch target sizes (48dp minimum)
  - Test with TalkBack screen reader
  - Verify color contrast ratios
  - Test keyboard navigation
  - _Requirements: All_

- [x] 85. Manual testing





  - Complete manual testing checklist from design doc
  - Test all Ionicons render correctly
  - Test all custom fonts load correctly
  - Test subtasks, notes, attachments
  - Test widgets on home screen
  - Test Peace Garden themes and streaks
  - Test ML suggestions
  - Test notifications and actions
  - Test deep link sharing
  - Test calendar sync
  - _Requirements: All_
- [x] 86. Bug fixes and polish



- [ ] 86. Bug fixes and polish

  - Fix any bugs found during testing
  - Polish animations and transitions
  - Improve error messages
  - Add loading states
  - Optimize performance
  - _Requirements: All_

- [x] 87. Documentation




  - Update README with new features
  - Create user guide for new features
  - Document ML suggestion system
  - Document widget usage
  - Document deep link format
  - _Requirements: All_

- [x] 88. Final checkpoint - Ensure all tests pass




  - Ensure all tests pass, ask the user if questions arise.
