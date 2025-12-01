package com.nami.peace.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.domain.model.Language
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.domain.usecase.SyncToCalendarUseCase
import com.nami.peace.util.calendar.CalendarManager
import com.nami.peace.util.font.CustomFont
import com.nami.peace.util.font.FontManager
import com.nami.peace.util.language.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val fontManager: FontManager,
    private val languageManager: LanguageManager,
    private val calendarManager: CalendarManager,
    private val syncToCalendarUseCase: SyncToCalendarUseCase,
    private val reminderRepository: ReminderRepository,
    private val featureToggleManager: com.nami.peace.util.feature.FeatureToggleManager
) : ViewModel() {

    // Font-related state
    val selectedFont: StateFlow<String?> = userPreferencesRepository.selectedFont
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val fontPadding: StateFlow<Int> = userPreferencesRepository.fontPadding
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val availableFonts: StateFlow<List<CustomFont>> = kotlinx.coroutines.flow.flowOf(
        fontManager.getAllFonts()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Background-related state
    val blurIntensity: StateFlow<Int> = userPreferencesRepository.blurIntensity
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val slideshowEnabled: StateFlow<Boolean> = userPreferencesRepository.slideshowEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    // Language-related state
    val currentLanguage: StateFlow<Language> = languageManager.currentLanguage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Language.SYSTEM_DEFAULT
        )

    val availableLanguages: StateFlow<List<Language>> = kotlinx.coroutines.flow.flowOf(
        languageManager.getAvailableLanguages()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    
    // Feature toggle state
    val subtasksEnabled: StateFlow<Boolean> = featureToggleManager.subtasksEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val attachmentsEnabled: StateFlow<Boolean> = featureToggleManager.attachmentsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val widgetsEnabled: StateFlow<Boolean> = featureToggleManager.widgetsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    val mlSuggestionsEnabled: StateFlow<Boolean> = featureToggleManager.mlSuggestionsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    fun setSelectedFont(fontName: String?) {
        viewModelScope.launch {
            userPreferencesRepository.setSelectedFont(fontName)
        }
    }

    fun setFontPadding(padding: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setFontPadding(padding)
        }
    }

    fun setBlurIntensity(intensity: Int) {
        viewModelScope.launch {
            userPreferencesRepository.setBlurIntensity(intensity)
        }
    }

    fun setSlideshowEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setSlideshowEnabled(enabled)
        }
    }

    fun setLanguage(language: Language) {
        viewModelScope.launch {
            languageManager.setLanguage(language)
        }
    }
    
    // Calendar Sync State
    val calendarSyncEnabled: StateFlow<Boolean> = userPreferencesRepository.calendarSyncEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )
    
    private val _isCalendarAuthenticated = MutableStateFlow(false)
    val isCalendarAuthenticated: StateFlow<Boolean> = _isCalendarAuthenticated.asStateFlow()
    
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()
    
    private val _lastSyncTime = MutableStateFlow<Long?>(null)
    val lastSyncTime: StateFlow<Long?> = _lastSyncTime.asStateFlow()
    
    private val _syncedCount = MutableStateFlow(0)
    val syncedCount: StateFlow<Int> = _syncedCount.asStateFlow()
    
    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()
    
    init {
        // Check authentication status on init
        viewModelScope.launch {
            checkAuthenticationStatus()
            loadSyncStats()
        }
    }
    
    private suspend fun checkAuthenticationStatus() {
        _isCalendarAuthenticated.value = calendarManager.isAuthenticated()
    }
    
    private suspend fun loadSyncStats() {
        val (lastSync, count) = syncToCalendarUseCase.getSyncStats()
        _lastSyncTime.value = lastSync
        _syncedCount.value = count
    }
    
    fun setCalendarSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setCalendarSyncEnabled(enabled)
            if (!enabled) {
                _syncError.value = null
            }
        }
    }
    
    fun requestGoogleAuthentication() {
        viewModelScope.launch {
            try {
                val result = calendarManager.requestAuthentication()
                if (result.isSuccess) {
                    _isCalendarAuthenticated.value = true
                    _syncError.value = null
                    // Enable sync after successful authentication
                    setCalendarSyncEnabled(true)
                } else {
                    _syncError.value = result.exceptionOrNull()?.message 
                        ?: "Authentication failed"
                }
            } catch (e: Exception) {
                _syncError.value = e.message ?: "Authentication error"
            }
        }
    }
    
    fun syncNow() {
        viewModelScope.launch {
            _isSyncing.value = true
            _syncError.value = null
            
            try {
                // Get all reminders
                val reminders = reminderRepository.getReminders().first()
                
                // Sync to calendar
                val result = syncToCalendarUseCase.syncAllReminders(reminders)
                
                if (result.isSuccess) {
                    val syncedCount = result.getOrThrow()
                    _syncedCount.value = syncedCount
                    _lastSyncTime.value = System.currentTimeMillis()
                    _syncError.value = null
                } else {
                    val error = result.exceptionOrNull()
                    _syncError.value = when {
                        error?.message?.contains("authenticated", ignoreCase = true) == true ->
                            "Not authenticated with Google"
                        error?.message?.contains("network", ignoreCase = true) == true ->
                            "Network error. Please check your connection."
                        error?.message?.contains("permission", ignoreCase = true) == true ->
                            "Calendar permission denied"
                        else -> error?.message ?: "Sync failed"
                    }
                }
            } catch (e: Exception) {
                _syncError.value = e.message ?: "Sync error"
            } finally {
                _isSyncing.value = false
            }
        }
    }
    
    fun clearSyncError() {
        _syncError.value = null
    }
    
    // Feature toggle methods
    fun setFeatureEnabled(feature: com.nami.peace.util.feature.FeatureToggleManager.Feature, enabled: Boolean) {
        viewModelScope.launch {
            featureToggleManager.setFeatureEnabled(feature, enabled)
        }
    }
}
