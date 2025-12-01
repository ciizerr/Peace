# Design Document

## Overview

This design document outlines the technical architecture for enhancing the Peace app with advanced customization, rich content support, intelligent suggestions, and improved user experience. The design maintains the existing MVVM + Clean Architecture pattern while adding new layers for ML analysis, widget support, and resource management.

### Design Principles

1. **Maintain Calm Engagement**: All new features should enhance, not overwhelm, the user experience
2. **Modular Architecture**: New features should be toggleable and independent
3. **Performance First**: Minimize impact on app startup and runtime performance
4. **Offline-First**: All features should work without internet except Calendar sync and ML suggestions
5. **Backward Compatibility**: Existing data and functionality must remain intact

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        Presentation Layer                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Screens │  │  Widgets │  │  Dialogs │  │  Themes  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                        ViewModel Layer                       │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Reminder │  │  Garden  │  │  Widget  │  │   ML     │   │
│  │ViewModel │  │ViewModel │  │ViewModel │  │ViewModel │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                        Domain Layer                          │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Use Cases│  │  Models  │  │Repository│  │   ML     │   │
│  │          │  │          │  │Interface │  │  Engine  │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                         Data Layer                           │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │   Room   │  │DataStore │  │  File    │  │ Calendar │   │
│  │ Database │  │  Prefs   │  │  Storage │  │   API    │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                      Infrastructure Layer                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │  Alarm   │  │  Widget  │  │  Icon    │  │   Font   │   │
│  │Scheduler │  │ Provider │  │  Manager │  │  Manager │   │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### New Components

#### 1. Resource Management Layer
- **IconManager**: Loads and provides Ionicons vector drawables
- **FontManager**: Manages custom font loading and application
- **ThemeManager**: Handles Peace Garden themes and visual styles

#### 2. ML Analysis Engine
- **PatternAnalyzer**: Analyzes user behavior patterns
- **SuggestionGenerator**: Creates ML-based suggestions with confidence scores
- **LearningRepository**: Stores user feedback on suggestions

#### 3. Widget System
- **TodayWidgetProvider**: Displays today's reminders
- **GardenWidgetProvider**: Shows Peace Garden progress
- **QuickAddWidgetProvider**: Provides quick reminder creation

#### 4. Content Management
- **AttachmentManager**: Handles image storage and retrieval
- **NoteRepository**: Manages notes with timestamps
- **SubtaskRepository**: Handles subtask CRUD operations

## Components and Interfaces

### 1. Icon Management

```kotlin
// IconManager.kt
interface IconManager {
    fun getIcon(iconName: String): ImageVector?
    fun getAllIcons(): Map<String, ImageVector>
    fun getFallbackIcon(iconName: String): ImageVector
}

class IoniconsManager @Inject constructor(
    @ApplicationContext private val context: Context
) : IconManager {
    private val iconCache = mutableMapOf<String, ImageVector>()
    
    override fun getIcon(iconName: String): ImageVector? {
        return iconCache.getOrPut(iconName) {
            loadIconFromResources(iconName)
        }
    }
    
    private fun loadIconFromResources(iconName: String): ImageVector {
        val resourceId = context.resources.getIdentifier(
            "ic_$iconName",
            "drawable",
            context.packageName
        )
        // Convert vector drawable to ImageVector
    }
}
```

### 2. Font Management

```kotlin
// FontManager.kt
data class CustomFont(
    val name: String,
    val fontFamily: FontFamily,
    val previewText: String = "The quick brown fox jumps over the lazy dog"
)

interface FontManager {
    fun getAllFonts(): List<CustomFont>
    fun getFont(fontName: String): FontFamily?
    fun getSystemFont(): FontFamily
}

class FontManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) : FontManager {
    private val fonts = mutableMapOf<String, FontFamily>()
    
    init {
        loadCustomFonts()
    }
    
    private fun loadCustomFonts() {
        // Load fonts from res/font directory
        val fontFiles = context.assets.list("fonts") ?: emptyArray()
        fontFiles.forEach { fileName ->
            val fontFamily = FontFamily(
                Font(context.assets, "fonts/$fileName")
            )
            fonts[fileName.removeSuffix(".ttf")] = fontFamily
        }
    }
}
```

### 3. Subtask Management

```kotlin
// Subtask.kt
data class Subtask(
    val id: Int = 0,
    val reminderId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int,
    val createdAt: Long = System.currentTimeMillis()
)

// SubtaskEntity.kt
@Entity(
    tableName = "subtasks",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reminderId")]
)
data class SubtaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reminderId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val order: Int,
    val createdAt: Long
)

// SubtaskDao.kt
@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtasks WHERE reminderId = :reminderId ORDER BY `order` ASC")
    fun getSubtasksForReminder(reminderId: Int): Flow<List<SubtaskEntity>>
    
    @Insert
    suspend fun insert(subtask: SubtaskEntity): Long
    
    @Update
    suspend fun update(subtask: SubtaskEntity)
    
    @Delete
    suspend fun delete(subtask: SubtaskEntity)
    
    @Query("SELECT COUNT(*) FROM subtasks WHERE reminderId = :reminderId")
    suspend fun getSubtaskCount(reminderId: Int): Int
    
    @Query("SELECT COUNT(*) FROM subtasks WHERE reminderId = :reminderId AND isCompleted = 1")
    suspend fun getCompletedSubtaskCount(reminderId: Int): Int
}
```

### 4. Notes and Attachments

```kotlin
// Note.kt
data class Note(
    val id: Int = 0,
    val reminderId: Int,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Attachment.kt
data class Attachment(
    val id: Int = 0,
    val reminderId: Int,
    val filePath: String,
    val thumbnailPath: String,
    val timestamp: Long = System.currentTimeMillis(),
    val mimeType: String = "image/*"
)

// NoteEntity.kt
@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reminderId")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reminderId: Int,
    val content: String,
    val timestamp: Long
)

// AttachmentEntity.kt
@Entity(
    tableName = "attachments",
    foreignKeys = [
        ForeignKey(
            entity = ReminderEntity::class,
            parentColumns = ["id"],
            childColumns = ["reminderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("reminderId")]
)
data class AttachmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reminderId: Int,
    val filePath: String,
    val thumbnailPath: String,
    val timestamp: Long,
    val mimeType: String
)
```

### 5. Custom Alarm Sounds

```kotlin
// AlarmSound.kt
data class AlarmSound(
    val id: String,
    val name: String,
    val uri: Uri,
    val isSystem: Boolean
)

// Update ReminderEntity
@Entity(tableName = "reminders")
data class ReminderEntity(
    // ... existing fields ...
    val customAlarmSoundUri: String? = null,
    val customAlarmSoundName: String? = null
)
```

### 6. Peace Garden Enhancement

```kotlin
// GardenTheme.kt
enum class GardenTheme {
    ZEN, FOREST, DESERT, OCEAN
}

// GardenState.kt
data class GardenState(
    val theme: GardenTheme = GardenTheme.ZEN,
    val growthStage: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletionDate: Long? = null,
    val totalTasksCompleted: Int = 0,
    val milestones: List<Int> = listOf(7, 30, 100, 365)
)

// GardenEntity.kt
@Entity(tableName = "garden_state")
data class GardenEntity(
    @PrimaryKey val id: Int = 1,
    val theme: GardenTheme,
    val growthStage: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val lastCompletionDate: Long?,
    val totalTasksCompleted: Int
)
```

### 7. ML Suggestion System

```kotlin
// Suggestion.kt
data class Suggestion(
    val id: Int = 0,
    val type: SuggestionType,
    val reminderId: Int?,
    val title: String,
    val description: String,
    val confidenceScore: Int, // 0-100
    val suggestedValue: String, // JSON-encoded suggestion data
    val createdAt: Long = System.currentTimeMillis(),
    val status: SuggestionStatus = SuggestionStatus.PENDING
)

enum class SuggestionType {
    OPTIMAL_TIME,
    PRIORITY_ADJUSTMENT,
    RECURRING_PATTERN,
    BREAK_REMINDER,
    HABIT_FORMATION,
    TEMPLATE_CREATION,
    FOCUS_SESSION
}

enum class SuggestionStatus {
    PENDING, APPLIED, DISMISSED
}

// PatternAnalyzer.kt
interface PatternAnalyzer {
    suspend fun analyzeCompletionPatterns(): List<Suggestion>
    suspend fun analyzePriorityPatterns(): List<Suggestion>
    suspend fun analyzeRecurringPatterns(): List<Suggestion>
    suspend fun analyzeFocusSessions(): List<Suggestion>
}
```

### 8. Widget Providers

```kotlin
// TodayWidgetProvider.kt
class TodayWidgetProvider : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            TodayWidgetContent()
        }
    }
}

@Composable
fun TodayWidgetContent() {
    val reminders = getTodayReminders()
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text("Today's Tasks", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        reminders.forEach { reminder ->
            ReminderWidgetItem(reminder)
        }
    }
}
```

### 9. Deep Link Handler

```kotlin
// DeepLinkHandler.kt
object DeepLinkHandler {
    private const val SCHEME = "peace"
    private const val HOST = "share"
    
    fun createShareLink(reminder: Reminder): String {
        val data = encodeReminderData(reminder)
        return "$SCHEME://$HOST?data=$data"
    }
    
    fun parseShareLink(uri: Uri): Reminder? {
        if (uri.scheme != SCHEME || uri.host != HOST) return null
        
        val data = uri.getQueryParameter("data") ?: return null
        return decodeReminderData(data)
    }
    
    private fun encodeReminderData(reminder: Reminder): String {
        val json = Json.encodeToString(reminder)
        return Base64.encodeToString(json.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)
    }
    
    private fun decodeReminderData(data: String): Reminder? {
        return try {
            val json = String(Base64.decode(data, Base64.URL_SAFE))
            Json.decodeFromString<Reminder>(json)
        } catch (e: Exception) {
            null
        }
    }
}
```

## Data Models

### Database Schema Updates

```kotlin
@Database(
    entities = [
        ReminderEntity::class,
        HistoryEntity::class,
        SubtaskEntity::class,
        NoteEntity::class,
        AttachmentEntity::class,
        GardenEntity::class,
        SuggestionEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PeaceDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao
    abstract fun historyDao(): HistoryDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun noteDao(): NoteDao
    abstract fun attachmentDao(): AttachmentDao
    abstract fun gardenDao(): GardenDao
    abstract fun suggestionDao(): SuggestionDao
}
```

### DataStore Preferences

```kotlin
// PreferencesKeys.kt
object PreferencesKeys {
    val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    val SELECTED_FONT = stringPreferencesKey("selected_font")
    val FONT_PADDING = intPreferencesKey("font_padding")
    val BLUR_INTENSITY = intPreferencesKey("blur_intensity")
    val SLIDESHOW_ENABLED = booleanPreferencesKey("slideshow_enabled")
    val CALENDAR_SYNC_ENABLED = booleanPreferencesKey("calendar_sync_enabled")
    val ML_SUGGESTIONS_ENABLED = booleanPreferencesKey("ml_suggestions_enabled")
    val SUBTASKS_ENABLED = booleanPreferencesKey("subtasks_enabled")
    val ATTACHMENTS_ENABLED = booleanPreferencesKey("attachments_enabled")
    val WIDGETS_ENABLED = booleanPreferencesKey("widgets_enabled")
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


### Property 1: Language persistence round-trip
*For any* language selection, if the user selects a language and restarts the app, the selected language should be active on restart.
**Validates: Requirements 1.4**

### Property 2: Font persistence round-trip
*For any* custom font selection, if the user selects a font and restarts the app, the selected font should be applied to all text elements on restart.
**Validates: Requirements 2.4**

### Property 3: Font padding application
*For any* font padding value (0-20dp), when applied, all text elements should reflect the padding value immediately.
**Validates: Requirements 2.6**

### Property 4: Ionicons usage consistency
*For any* UI component that renders an icon, the icon should come from the Ionicons pack, not Material Icons.
**Validates: Requirements 3.2**

### Property 5: Icon fallback handling
*For any* requested icon name that doesn't exist in Ionicons, the system should return a fallback icon without crashing.
**Validates: Requirements 3.4**

### Property 6: Subtask-reminder linkage
*For any* subtask created, it should be linked to exactly one parent reminder via foreign key relationship.
**Validates: Requirements 4.1**

### Property 7: Subtask completion state update
*For any* subtask checkbox interaction, the subtask's completion state should toggle immediately.
**Validates: Requirements 4.2**

### Property 8: Progress calculation accuracy
*For any* reminder with subtasks, the progress percentage should equal (completed subtasks / total subtasks) * 100.
**Validates: Requirements 4.3**

### Property 9: Progress recalculation on deletion
*For any* subtask deletion, the parent reminder's progress bar should recalculate immediately to reflect the new completion percentage.
**Validates: Requirements 4.5**

### Property 10: Note timestamp inclusion
*For any* note added to a reminder, the note should have a timestamp equal to or greater than the reminder's creation time.
**Validates: Requirements 5.1**

### Property 11: Attachment storage and thumbnail
*For any* image attachment, both the full image and thumbnail should be stored in local storage with valid file paths.
**Validates: Requirements 5.2**

### Property 12: Chronological attachment ordering
*For any* reminder with multiple attachments, when displayed, the attachments should be sorted by timestamp in ascending order.
**Validates: Requirements 5.3**

### Property 13: Attachment deletion completeness
*For any* attachment deletion, both the database record and the file on disk should be removed.
**Validates: Requirements 5.4**

### Property 14: Background image application
*For any* attachment image selected as background, the current screen should display that image as the background.
**Validates: Requirements 6.1**

### Property 15: Blur intensity persistence
*For any* blur intensity value (0-100), if set and the app restarts, the blur intensity should persist.
**Validates: Requirements 6.5**

### Property 16: Alarm sound association
*For any* custom alarm sound saved for a reminder, the reminder entity should store the sound URI and name.
**Validates: Requirements 7.3**

### Property 17: Alarm sound playback selection
*For any* reminder alarm trigger, if a custom sound is set, that sound should play; otherwise, the default sound should play.
**Validates: Requirements 7.4**

### Property 18: Calendar sync completeness
*For any* manual sync trigger, all active (non-completed, enabled) reminders should be exported to Google Calendar.
**Validates: Requirements 8.3**

### Property 19: Calendar event synchronization
*For any* reminder update when sync is enabled, the corresponding calendar event should be updated with the new data.
**Validates: Requirements 8.4**

### Property 20: Deep link round-trip
*For any* reminder, encoding it to a deep link and then decoding should produce an equivalent reminder with all fields preserved.
**Validates: Requirements 9.2, 9.7**

### Property 21: Deep link import
*For any* valid deep link opened in the app, a new reminder should be created in the local database with all data from the link.
**Validates: Requirements 9.3**

### Property 22: Suggestion confidence score validity
*For any* ML suggestion generated, the confidence score should be between 0 and 100 inclusive.
**Validates: Requirements 12.9**

### Property 23: Suggestion application side effects
*For any* suggestion applied, both the reminder should be updated AND a learning record should be created.
**Validates: Requirements 12.10**

### Property 24: Feature toggle UI hiding
*For any* feature disabled via toggle, all UI elements related to that feature should be hidden from all screens.
**Validates: Requirements 13.2**

### Property 25: Feature toggle persistence
*For any* feature toggle state change, if the app restarts, the toggle state should persist.
**Validates: Requirements 13.4**

### Property 26: Notification completion side effects
*For any* "Complete" button tap in a notification, both the reminder should be marked complete AND the Peace Garden should update.
**Validates: Requirements 14.2**

### Property 27: Notification bundling
*For any* set of reminders triggering within a 1-minute window, they should be displayed as a single bundled notification.
**Validates: Requirements 14.5**

### Property 28: Nag mode progression
*For any* nag mode reminder completion, if not the final repetition, the next repetition should be scheduled; if final, the reminder should be marked complete.
**Validates: Requirements 17.3**

### Property 29: Panic loop activation
*For any* snooze action during nag mode, the panic loop should activate with the next alarm scheduled in exactly 2 minutes.
**Validates: Requirements 17.4**

### Property 30: Garden theme application
*For any* garden theme selection, the Peace Garden should immediately display theme-specific icons and colors.
**Validates: Requirements 18.2**

### Property 31: Growth stage advancement
*For any* task completion, if the completion count reaches a growth stage threshold, the Peace Garden should advance to the next stage.
**Validates: Requirements 18.3**

### Property 32: Streak calculation
*For any* sequence of task completions, if completions occur on consecutive days, the streak counter should increment; if a day is skipped, it should reset to 1.
**Validates: Requirements 18.5**

### Property 33: Milestone detection
*For any* streak reaching a milestone value (7, 30, 100, 365), an achievement notification should be displayed.
**Validates: Requirements 18.6**

### Property 34: Streak reset
*For any* streak break (no completions for 24+ hours), the streak counter should reset to 0.
**Validates: Requirements 18.8**

## Error Handling

### Icon Loading Errors
- **Missing Icon**: Log warning and return fallback icon from Material Icons
- **Corrupted Vector Drawable**: Log error and return placeholder icon
- **Resource Not Found**: Use default icon and notify developer via logging

### Font Loading Errors
- **Missing Font File**: Fall back to system font
- **Invalid Font Format**: Log error and use system font
- **Font Loading Timeout**: Use system font after 2-second timeout

### Attachment Errors
- **Storage Full**: Display error toast and prevent attachment
- **File Not Found**: Remove database reference and show error
- **Thumbnail Generation Failed**: Use placeholder thumbnail
- **Image Too Large**: Compress before storing, max 5MB per image

### Calendar Sync Errors
- **Permission Denied**: Show permission request dialog
- **Network Unavailable**: Queue sync for later, show offline indicator
- **Calendar API Error**: Log error, show user-friendly message, retry with exponential backoff
- **Sync Conflict**: Use local data as source of truth (one-way sync)

### Deep Link Errors
- **Invalid Link Format**: Show "Invalid link" toast
- **Corrupted Data**: Show "Unable to import reminder" error
- **App Not Installed**: Android system handles Play Store redirect

### ML Suggestion Errors
- **Insufficient Data**: Don't generate suggestions until 7 days of usage
- **Analysis Timeout**: Skip suggestion generation for this cycle
- **Invalid Confidence Score**: Clamp to 0-100 range

### Widget Errors
- **Data Load Failure**: Show "Unable to load" message in widget
- **Update Timeout**: Retry update after 30 seconds
- **Widget Removed**: Clean up resources and cancel updates

## Testing Strategy

### Unit Testing

**Core Logic Tests:**
- Subtask progress calculation
- Streak calculation logic
- Deep link encoding/decoding
- Icon fallback logic
- Font padding application
- Blur intensity clamping (0-100)
- Confidence score validation (0-100)

**Repository Tests:**
- CRUD operations for subtasks, notes, attachments
- Foreign key cascade deletes
- Query correctness for chronological ordering
- Feature toggle persistence

**ViewModel Tests:**
- State management for new features
- Error handling flows
- Feature toggle effects on UI state

### Property-Based Testing

We will use **Kotest Property Testing** for Android/Kotlin. Each correctness property will be implemented as a property-based test.

**Property Test Configuration:**
- Minimum 100 iterations per property test
- Custom generators for domain models
- Shrinking enabled for failure case minimization

**Example Property Test Structure:**
```kotlin
class SubtaskPropertyTests : StringSpec({
    "Property 8: Progress calculation accuracy" {
        checkAll(100, Arb.reminderWithSubtasks()) { (reminder, subtasks) ->
            val completed = subtasks.count { it.isCompleted }
            val total = subtasks.size
            val expectedProgress = if (total > 0) (completed * 100) / total else 0
            
            val actualProgress = calculateProgress(reminder.id)
            
            actualProgress shouldBe expectedProgress
        }
    }
})
```

**Custom Generators:**
- `Arb.reminder()`: Generates random valid reminders
- `Arb.subtask()`: Generates random subtasks
- `Arb.reminderWithSubtasks()`: Generates reminder with 0-20 subtasks
- `Arb.note()`: Generates random notes
- `Arb.attachment()`: Generates random attachment metadata
- `Arb.gardenState()`: Generates random garden states
- `Arb.suggestion()`: Generates random ML suggestions

### Integration Testing

**Database Integration:**
- Foreign key cascade behavior
- Transaction rollback on errors
- Migration from version 1 to version 2

**Widget Integration:**
- Widget update triggers
- Data flow from database to widget
- User interaction handling

**Calendar Integration:**
- Permission flow
- Event creation and updates
- Sync error handling

**Deep Link Integration:**
- Intent handling
- Data import flow
- Error scenarios

### UI Testing

**Compose UI Tests:**
- Subtask checkbox interactions
- Progress bar rendering
- Font preview rendering
- Icon display verification
- Theme switching
- Feature toggle effects

**Widget UI Tests:**
- Widget layout rendering
- Data display accuracy
- Click handling

### Manual Testing Checklist

**Ionicons Integration:**
- [ ] All icons render correctly
- [ ] Fallback icons work for missing icons
- [ ] Icons scale properly at different densities
- [ ] Icons tint correctly with theme changes

**Custom Fonts:**
- [ ] All fonts load correctly
- [ ] Font preview displays accurately
- [ ] Font padding applies to all text
- [ ] System font fallback works

**Subtasks:**
- [ ] Add/edit/delete subtasks
- [ ] Progress bar updates in real-time
- [ ] Checkbox animations smooth
- [ ] Subtasks persist across app restarts

**Attachments:**
- [ ] Image picker works
- [ ] Thumbnails generate correctly
- [ ] Background images display with blur
- [ ] Slideshow transitions smoothly
- [ ] Deletion removes files from disk

**Widgets:**
- [ ] Widgets update when data changes
- [ ] Widget clicks open correct screens
- [ ] Quick-add widget creates reminders
- [ ] Widgets respect theme changes

**Peace Garden:**
- [ ] Theme switching works
- [ ] Growth stages advance correctly
- [ ] Streak calculation accurate
- [ ] Milestone notifications appear

**ML Suggestions:**
- [ ] Suggestions generate after 7 days
- [ ] Confidence scores display
- [ ] Apply/dismiss actions work
- [ ] Learning data persists

**Notifications:**
- [ ] Complete button works
- [ ] Snooze enters panic loop
- [ ] Dismiss cancels alarm
- [ ] Bundled notifications expand
- [ ] Nag mode progress displays

## Performance Considerations

### Icon Loading
- **Lazy Loading**: Load icons on-demand, not at app startup
- **Caching**: Cache loaded ImageVectors in memory
- **Background Loading**: Load icon pack on background thread

### Font Loading
- **Preloading**: Load selected font at app startup
- **Caching**: Keep font in memory once loaded
- **Fallback**: Immediate fallback to system font on error

### Image Attachments
- **Compression**: Compress images to max 5MB
- **Thumbnail Generation**: Generate 200x200 thumbnails asynchronously
- **Lazy Loading**: Load full images only when viewed
- **Cache Management**: Implement LRU cache for thumbnails

### ML Analysis
- **Background Processing**: Run analysis on background thread
- **Throttling**: Analyze patterns max once per day
- **Data Limits**: Analyze only last 90 days of data
- **Timeout**: Cancel analysis after 30 seconds

### Widget Updates
- **Throttling**: Update widgets max once per minute
- **Batch Updates**: Update all widgets in single operation
- **Work Manager**: Use WorkManager for periodic updates

### Database Queries
- **Indexing**: Add indexes on foreign keys and frequently queried columns
- **Pagination**: Load subtasks/notes/attachments in pages of 50
- **Caching**: Use Room's Flow for automatic caching

## Security Considerations

### Image Storage
- **Private Storage**: Store attachments in app-private directory
- **File Permissions**: Restrict file access to app only
- **Validation**: Validate image MIME types before storage

### Deep Links
- **Data Validation**: Validate all fields from deep link data
- **Size Limits**: Limit deep link data to 8KB
- **Sanitization**: Sanitize text fields to prevent injection

### Calendar Access
- **Permission Checks**: Always verify permissions before API calls
- **Minimal Scope**: Request only calendar write permission
- **Error Handling**: Handle permission denial gracefully

### ML Data
- **Local Only**: All ML analysis happens on-device
- **No Telemetry**: No usage data sent to external servers
- **User Control**: Allow users to disable ML features

## Migration Strategy

### Database Migration (v1 → v2)

```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Add subtasks table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS subtasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                reminderId INTEGER NOT NULL,
                title TEXT NOT NULL,
                isCompleted INTEGER NOT NULL DEFAULT 0,
                `order` INTEGER NOT NULL,
                createdAt INTEGER NOT NULL,
                FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
            )
        """)
        
        // Add notes table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS notes (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                reminderId INTEGER NOT NULL,
                content TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
            )
        """)
        
        // Add attachments table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS attachments (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                reminderId INTEGER NOT NULL,
                filePath TEXT NOT NULL,
                thumbnailPath TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                mimeType TEXT NOT NULL,
                FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE CASCADE
            )
        """)
        
        // Add garden_state table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS garden_state (
                id INTEGER PRIMARY KEY NOT NULL DEFAULT 1,
                theme TEXT NOT NULL DEFAULT 'ZEN',
                growthStage INTEGER NOT NULL DEFAULT 0,
                currentStreak INTEGER NOT NULL DEFAULT 0,
                longestStreak INTEGER NOT NULL DEFAULT 0,
                lastCompletionDate INTEGER,
                totalTasksCompleted INTEGER NOT NULL DEFAULT 0
            )
        """)
        
        // Add suggestions table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS suggestions (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                type TEXT NOT NULL,
                reminderId INTEGER,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                confidenceScore INTEGER NOT NULL,
                suggestedValue TEXT NOT NULL,
                createdAt INTEGER NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING'
            )
        """)
        
        // Add new columns to reminders table
        database.execSQL("ALTER TABLE reminders ADD COLUMN customAlarmSoundUri TEXT")
        database.execSQL("ALTER TABLE reminders ADD COLUMN customAlarmSoundName TEXT")
        
        // Create indexes
        database.execSQL("CREATE INDEX IF NOT EXISTS index_subtasks_reminderId ON subtasks(reminderId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_notes_reminderId ON notes(reminderId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_attachments_reminderId ON attachments(reminderId)")
        
        // Initialize garden state
        database.execSQL("INSERT INTO garden_state (id) VALUES (1)")
    }
}
```

### Ionicons Integration Steps

1. **SVG to Vector Drawable Conversion**
   - Use Android Studio's Vector Asset tool or svg2vector CLI
   - Batch convert all SVG files from `C:\Users\mitsu\Downloads\ionicons.designerpack`
   - Place converted XML files in `app/src/main/res/drawable/`
   - Naming convention: `ic_ionicons_[original_name].xml`

2. **Icon Mapping**
   - Create `IconMapper.kt` with mapping from icon names to resource IDs
   - Generate mapping automatically from drawable resources
   - Provide fallback mapping for common Material Icons

3. **Compose Integration**
   - Create `Icon` composable that uses IconManager
   - Replace all `Icon(Icons.*)` with `Icon(IconName.*)`
   - Update theme to use Ionicons for system icons

### Custom Fonts Integration Steps

1. **Font File Preparation**
   - Copy font files from `C:\Users\mitsu\Downloads\Font_folder`
   - Place in `app/src/main/res/font/` directory
   - Naming convention: lowercase with underscores (e.g., `roboto_regular.ttf`)

2. **Font Family Creation**
   - Create `FontFamily` objects for each font
   - Define font weights and styles
   - Create `Typography` variants for each font

3. **Theme Integration**
   - Update `PeaceTheme` to accept custom typography
   - Create `rememberFontFamily()` composable
   - Apply font padding via `LocalTextStyle`

## Implementation Phases

### Phase 1: Foundation (Weeks 1-2)
- Database migration
- Ionicons integration
- Custom fonts integration
- Feature toggle system

### Phase 2: Rich Content (Weeks 3-4)
- Subtasks implementation
- Notes implementation
- Attachments implementation
- Background image system

### Phase 3: Customization (Weeks 5-6)
- Language selection
- Font padding
- Custom alarm sounds
- Enhanced Peace Garden

### Phase 4: Integration (Weeks 7-8)
- Google Calendar sync
- Deep link sharing
- Widget implementation

### Phase 5: Intelligence (Weeks 9-10)
- ML pattern analyzer
- Suggestion generator
- Learning system

### Phase 6: Polish (Weeks 11-12)
- Notification redesign
- Performance optimization
- Testing and bug fixes
- Documentation

## Dependencies

### New Dependencies to Add

```kotlin
// build.gradle.kts (app)

// Glance for Widgets
implementation("androidx.glance:glance-appwidget:1.0.0")

// Coil for Image Loading
implementation("io.coil-kt:coil-compose:2.5.0")

// Kotlinx Serialization for Deep Links
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

// Google Calendar API
implementation("com.google.android.gms:play-services-auth:20.7.0")
implementation("com.google.apis:google-api-services-calendar:v3-rev20220715-2.0.0")

// Kotest for Property Testing
testImplementation("io.kotest:kotest-runner-junit5:5.8.0")
testImplementation("io.kotest:kotest-assertions-core:5.8.0")
testImplementation("io.kotest:kotest-property:5.8.0")

// WorkManager for Background Tasks
implementation("androidx.work:work-runtime-ktx:2.9.0")

// Accompanist for Permissions
implementation("com.google.accompanist:accompanist-permissions:0.32.0")
```

## UI Design Guidelines

### Design System

**Color Palette:**
- Maintain existing "Morning Light" and "Night Sky" themes
- Add theme-specific colors for Peace Garden themes:
  - **Zen**: Soft grays, whites, light blues (#F5F5F5, #E8EAF6, #90CAF9)
  - **Forest**: Greens, browns, earth tones (#4CAF50, #8BC34A, #795548)
  - **Desert**: Warm oranges, yellows, sandy tones (#FF9800, #FFC107, #FFECB3)
  - **Ocean**: Blues, teals, aqua tones (#00BCD4, #0097A7, #B2EBF2)

**Typography:**
- Use selected custom font throughout app
- Apply font padding consistently
- Font sizes: 
  - Heading: 24sp
  - Subheading: 18sp
  - Body: 14sp
  - Caption: 12sp

**Spacing:**
- Base unit: 8dp
- Small: 8dp
- Medium: 16dp
- Large: 24dp
- Extra Large: 32dp

**Icons:**
- All icons from Ionicons
- Icon size: 24dp (standard), 32dp (prominent actions)
- Icon tinting: Follow theme colors

### Screen Designs

#### 1. Enhanced Reminder Detail Screen

```
┌─────────────────────────────────────┐
│  ← Reminder Detail          ⋮       │ ← Back button, overflow menu
├─────────────────────────────────────┤
│  [Background Image with Blur]       │ ← Optional attachment as background
│                                     │
│  📝 Buy groceries                   │ ← Title with category icon
│  🔴 HIGH • Daily • 10:00 AM        │ ← Priority, recurrence, time
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Subtasks (2/5 complete)     │   │
│  │ ▓▓▓▓▓░░░░░ 40%             │   │ ← Progress bar
│  │                             │   │
│  │ ☑ Buy milk                  │   │ ← Completed subtask
│  │ ☐ Buy bread                 │   │ ← Incomplete subtask
│  │ ☐ Buy eggs                  │   │
│  │ ☐ Buy cheese                │   │
│  │ ☐ Buy butter                │   │
│  │ + Add subtask               │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Notes (3)                   │   │
│  │                             │   │
│  │ 📝 Don't forget organic     │   │
│  │    2 hours ago              │   │
│  │                             │   │
│  │ 📝 Check for sales          │   │
│  │    Yesterday                │   │
│  │                             │   │
│  │ + Add note                  │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Attachments (2)             │   │
│  │                             │   │
│  │ [📷 Thumbnail] [📷 Thumbnail]│   │
│  │                             │   │
│  │ + Add image                 │   │
│  └─────────────────────────────┘   │
│                                     │
│  🔔 Custom Sound: Gentle Bell      │ ← Alarm sound
│  🔁 Nag Mode: 5 times, 2h interval│ ← Nag mode info
│                                     │
│  [Share] [Edit] [Delete]           │ ← Action buttons
└─────────────────────────────────────┘
```

#### 2. Settings Screen (Enhanced)

```
┌─────────────────────────────────────┐
│  ← Settings                         │
├─────────────────────────────────────┤
│  Appearance                         │
│  ├─ 🌐 Language: English           │ ← Language selector
│  ├─ 🔤 Font: Roboto                │ ← Font selector
│  ├─ 📏 Font Padding: 4dp           │ ← Slider (0-20dp)
│  └─ 🎨 Theme: Night Sky            │ ← Theme toggle
│                                     │
│  Peace Garden                       │
│  ├─ 🌿 Garden Theme: Zen           │ ← Theme selector
│  ├─ 🔥 Current Streak: 12 days    │ ← Streak display
│  └─ 🏆 Next Milestone: 30 days    │ ← Milestone progress
│                                     │
│  Background                         │
│  ├─ 🖼️ Background Images: On       │ ← Toggle
│  ├─ 🌫️ Blur Intensity: 50%        │ ← Slider (0-100)
│  └─ 🎞️ Slideshow: On              │ ← Toggle
│                                     │
│  Features                           │
│  ├─ ✅ Subtasks: On                │ ← Feature toggle
│  ├─ 📎 Attachments: On             │ ← Feature toggle
│  ├─ 🤖 ML Suggestions: On          │ ← Feature toggle
│  ├─ 📅 Calendar Sync: Off          │ ← Feature toggle
│  └─ 📱 Widgets: On                 │ ← Feature toggle
│                                     │
│  Integration                        │
│  ├─ 📅 Sync to Calendar            │ ← Manual sync button
│  └─ 📊 Sync Stats: 15 reminders   │ ← Sync info
│                                     │
│  About                              │
│  └─ ℹ️ Version 3.0.0               │
└─────────────────────────────────────┘
```

#### 3. ML Suggestions Screen

```
┌─────────────────────────────────────┐
│  ← Smart Suggestions                │
├─────────────────────────────────────┤
│  💡 We've analyzed your patterns    │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🕐 Optimal Time              │   │
│  │ Confidence: 85%              │   │
│  │                             │   │
│  │ "Exercise" completes 80% of │   │
│  │ the time at 7am, but only   │   │
│  │ 20% at 9pm.                 │   │
│  │                             │   │
│  │ Suggested: Move to 7:00 AM  │   │
│  │                             │   │
│  │ [Apply] [Dismiss]           │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🎯 Priority Adjustment       │   │
│  │ Confidence: 65%              │   │
│  │                             │   │
│  │ "Check email" is marked HIGH│   │
│  │ but you complete it last.   │   │
│  │                             │   │
│  │ Suggested: Change to MEDIUM │   │
│  │                             │   │
│  │ [Apply] [Dismiss]           │   │
│  └─────────────────────────────┘   │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ 🔁 Recurring Pattern         │   │
│  │ Confidence: 90%              │   │
│  │                             │   │
│  │ You've created "Drink water"│   │
│  │ manually 5 times in 3 days. │   │
│  │                             │   │
│  │ Suggested: Make it recurring│   │
│  │ (Every 2 hours, 5 times)    │   │
│  │                             │   │
│  │ [Apply] [Dismiss]           │   │
│  └─────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### 4. Enhanced Peace Garden

```
┌─────────────────────────────────────┐
│  Peace Garden                       │
├─────────────────────────────────────┤
│  [Zen Theme Selector]               │ ← Theme tabs
│  Zen | Forest | Desert | Ocean      │
│                                     │
│         🌱                          │
│        🌿🌿                         │ ← Growth visualization
│       🌿🌿🌿                        │   (stage 3 of 10)
│      🌿🌿🌿🌿                       │
│     🌿🌿🌿🌿🌿                      │
│                                     │
│  ┌─────────────────────────────┐   │
│  │ Growth Stage: Sprouting     │   │
│  │ ▓▓▓▓▓▓░░░░ 60%             │   │ ← Progress to next stage
│  │                             │   │
│  │ 🔥 Current Streak: 12 days  │   │
│  │ 🏆 Longest Streak: 45 days  │   │
│  │ ✅ Total Completed: 234     │   │
│  │                             │   │
│  │ Next Milestone: 30 days     │   │
│  │ ▓▓▓▓▓▓▓▓░░ 18 days to go   │   │
│  └─────────────────────────────┘   │
│                                     │
│  Recent Achievements                │
│  🏆 7-Day Streak (Dec 15)          │
│  🌟 100 Tasks Completed (Dec 10)   │
└─────────────────────────────────────┘
```

#### 5. Enhanced Notification

```
┌─────────────────────────────────────┐
│  🌿 Peace                           │
│  📝 Buy groceries                   │ ← Title with icon
│  🔴 HIGH • 10:00 AM                │ ← Priority & time
│                                     │
│  Nag Mode: 2 of 5 complete         │ ← Repetition progress
│  Subtasks: 2/5 done (40%)          │ ← Subtask progress
│                                     │
│  [✓ I'm doing it] [⏰ Snooze]      │ ← Action buttons
└─────────────────────────────────────┘
```

#### 6. Widget Designs

**Today's Reminders Widget (4x2):**
```
┌─────────────────────────────────────┐
│  Today's Tasks              [+]     │ ← Quick add button
│                                     │
│  ☐ 🔴 Buy groceries - 10:00 AM     │
│  ☐ 🟡 Exercise - 7:00 AM           │
│  ☑ 🟢 Check email - 9:00 AM        │
│                                     │
│  3 tasks • 1 completed              │
└─────────────────────────────────────┘
```

**Peace Garden Widget (2x2):**
```
┌───────────────────┐
│  Peace Garden     │
│                   │
│      🌿🌿         │
│     🌿🌿🌿        │
│                   │
│  🔥 12 day streak │
│  Stage 3/10       │
└───────────────────┘
```

**Quick Add Widget (4x1):**
```
┌─────────────────────────────────────┐
│  [Type a reminder...]        [Add]  │
└─────────────────────────────────────┘
```

### Animation Guidelines

**Micro-interactions:**
- Checkbox toggle: Scale + fade (150ms)
- Progress bar update: Smooth fill animation (300ms)
- Subtask add: Slide in from bottom (200ms)
- Theme switch: Crossfade (400ms)
- Garden growth: Scale + bounce (500ms)
- Milestone achievement: Confetti + scale (1000ms)

**Transitions:**
- Screen navigation: Slide (300ms)
- Dialog appearance: Fade + scale (250ms)
- Bottom sheet: Slide up (300ms)

**Loading States:**
- Skeleton screens for image loading
- Shimmer effect for list loading
- Circular progress for sync operations

### Accessibility

**Touch Targets:**
- Minimum 48dp x 48dp for all interactive elements
- Adequate spacing between buttons (8dp minimum)

**Content Descriptions:**
- All icons have meaningful descriptions
- Progress bars announce percentage
- Checkboxes announce state changes

**Color Contrast:**
- Minimum 4.5:1 for normal text
- Minimum 3:1 for large text
- High contrast mode support

**Screen Reader Support:**
- Semantic ordering of elements
- Grouped related content
- Announced state changes

## Conclusion

This design provides a comprehensive architecture for enhancing the Peace app while maintaining its core philosophy of calm engagement. The modular approach allows features to be implemented incrementally and toggled independently, giving users complete control over their experience. The correctness properties ensure that each feature behaves correctly across all inputs, and the testing strategy provides confidence in the implementation.

The ML system uses simple on-device pattern analysis without external dependencies, ensuring privacy and offline functionality. The UI design maintains the minimalist aesthetic while adding powerful features that enhance rather than overwhelm the user experience.
