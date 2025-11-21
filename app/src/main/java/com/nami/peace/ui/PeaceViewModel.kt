package com.nami.peace.ui

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.BuildConfig
import com.nami.peace.PeaceApplication
import com.nami.peace.alarm.AndroidAlarmScheduler
import com.nami.peace.data.Category
import com.nami.peace.data.Frequency
import com.nami.peace.data.GeminiRepository
import com.nami.peace.data.Reminder
import com.nami.peace.data.ReminderType
import com.nami.peace.data.SettingsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class PeaceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = (application as PeaceApplication).repository
    private val geminiRepository = GeminiRepository()
    private val settingsManager = SettingsManager(application)
    private val alarmScheduler = AndroidAlarmScheduler(application)

    // --- Data Flows ---
    val allReminders: StateFlow<List<Reminder>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gardenProgress: StateFlow<Float> = allReminders.map { reminders ->
        val essentialReminders = reminders.filter { it.isEssential }
        if (essentialReminders.isEmpty()) 0f
        else essentialReminders.count { it.isCompleted }.toFloat() / essentialReminders.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // --- Settings Flows ---
    val isDarkMode = settingsManager.isDarkModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isHapticsEnabled = settingsManager.isHapticsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val currentSound = settingsManager.soundFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Calm Breeze")

    val userApiKeyFlow = settingsManager.userApiKeyFlow

    val userName = settingsManager.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Friend")

    val profileImageUri = settingsManager.userProfileUriFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isPrivacyModeEnabled = settingsManager.isPrivacyModeEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val themeAccent = settingsManager.themeAccentFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Purple")

    val focusAreas = settingsManager.focusAreasFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())
    
    val wakeUpTime = settingsManager.wakeUpTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "07:00")

    val bedTime = settingsManager.bedTimeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "22:00")

    // --- UI State ---
    var reminderTitle by mutableStateOf("")
    var reminderTime by mutableStateOf("")
    var reminderType by mutableStateOf(ReminderType.Notification)
    var reminderFrequency by mutableStateOf(Frequency.Once)
    var reminderCategoryId by mutableStateOf<Long?>(null)
    var currentReminderId by mutableStateOf<Long?>(null)
    var isEssential by mutableStateOf(false)

    var isAiLoading by mutableStateOf(false)
    var coachQuote by mutableStateOf("The task is only the quiet folding of paper; let your focused intention be the light that shows you the crease.")
        private set

    init {
        refreshCoachQuote()
    }

    fun refreshCoachQuote() {
        viewModelScope.launch {
            try {
                val apiKey = userApiKeyFlow.first() ?: BuildConfig.GEMINI_API_KEY
                val name = userName.first()
                val areas = focusAreas.first()
                val quote = geminiRepository.getCoachQuote(name, areas, apiKey)
                coachQuote = quote
            } catch (e: Exception) {
                // Keep default
            }
        }
    }

    fun onSparkleClick(input: String) {
        if (input.isBlank()) return
        isAiLoading = true
        viewModelScope.launch {
            try {
                val apiKey = userApiKeyFlow.first() ?: BuildConfig.GEMINI_API_KEY
                val parsed = geminiRepository.parseReminder(input, apiKey)
                if (parsed != null) {
                    reminderTitle = parsed.title
                    reminderTime = parsed.time ?: ""
                    // TODO: Match category
                }
            } catch (e: Exception) {
                Log.e("PeaceViewModel", "AI Error", e)
            } finally {
                isAiLoading = false
            }
        }
    }

    // --- Actions ---

    fun saveReminder() {
        val title = reminderTitle
        val timeString = reminderTime
        if (title.isBlank() || timeString.isBlank()) return

        viewModelScope.launch {
            // Parse time string to millis (today's date + time)
            val timeParts = timeString.split(":")
            val hour = timeParts[0].toIntOrNull() ?: 0
            val minute = timeParts[1].toIntOrNull() ?: 0
            
            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                if (timeInMillis < System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            val reminder = Reminder(
                id = currentReminderId ?: 0,
                title = title,
                timeInMillis = calendar.timeInMillis,
                type = reminderType,
                frequency = reminderFrequency,
                categoryId = reminderCategoryId,
                isEssential = isEssential
            )

            repository.insertReminder(reminder)
            
            // Schedule Alarm
            // We need the ID for scheduling.
            // For now, we will just reset the UI.
            resetReminderState()
        }
    }

    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            alarmScheduler.cancel(reminder)
        }
    }

    fun onEditClick(reminder: Reminder) {
        reminderTitle = reminder.title
        currentReminderId = reminder.id
        reminderType = reminder.type
        reminderFrequency = reminder.frequency
        reminderCategoryId = reminder.categoryId
        isEssential = reminder.isEssential
        
        val calendar = Calendar.getInstance().apply { timeInMillis = reminder.timeInMillis }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        reminderTime = String.format("%02d:%02d", hour, minute)
    }

    fun resetReminderState() {
        reminderTitle = ""
        reminderTime = ""
        reminderType = ReminderType.Notification
        reminderFrequency = Frequency.Once
        reminderCategoryId = null
        currentReminderId = null
        isEssential = false
    }

    fun saveWakeUpTime(time: String) {
        viewModelScope.launch { settingsManager.saveWakeUpTime(time) }
    }

    fun saveBedTime(time: String) {
        viewModelScope.launch { settingsManager.saveBedTime(time) }
    }

    fun saveFocusAreas(areas: Set<String>) {
        viewModelScope.launch { settingsManager.saveFocusAreas(areas) }
    }

    fun saveUserName(name: String) {
        viewModelScope.launch { settingsManager.saveUserName(name) }
    }

    fun saveProfileUri(uri: String) {
        viewModelScope.launch { settingsManager.saveProfileUri(uri) }
    }

    fun saveApiKey(key: String) {
        viewModelScope.launch { settingsManager.saveUserApiKey(key) }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { settingsManager.saveDarkMode(enabled) }
    }

    fun toggleHaptics(enabled: Boolean) {
        viewModelScope.launch { settingsManager.saveHaptics(enabled) }
    }

    fun updateSound(sound: String) {
        viewModelScope.launch { settingsManager.saveSound(sound) }
    }

    fun togglePrivacyMode(enabled: Boolean) {
        viewModelScope.launch { settingsManager.savePrivacyMode(enabled) }
    }

    fun updateThemeAccent(accent: String) {
        viewModelScope.launch { settingsManager.saveThemeAccent(accent) }
    }

    fun nukeData() {
        viewModelScope.launch {
            repository.nukeData()
        }
    }

    fun getAppVersion(): String {
        return try {
            val pInfo = getApplication<Application>().packageManager.getPackageInfo(getApplication<Application>().packageName, 0)
            "v${pInfo.versionName}"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getStorageUsage(): String {
        val dbName = "peace_database"
        val dbFile = getApplication<Application>().getDatabasePath(dbName)
        return if (dbFile.exists()) {
            val sizeInBytes = dbFile.length()
            val sizeInKb = sizeInBytes / 1024
            if (sizeInKb > 1024) {
                "${String.format("%.2f", sizeInKb / 1024.0)} MB"
            } else {
                "$sizeInKb KB"
            }
        } else {
            "0 KB"
        }
    }

    fun backupData() { }
    fun restoreData() { }
    fun exportData() { }
}