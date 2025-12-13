package com.nami.peace.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val appUpdater: com.nami.peace.data.updater.AppUpdater
) : ViewModel() {

    fun updateUserProfile(profile: com.nami.peace.data.repository.UserProfile) {
        viewModelScope.launch {
            userPreferencesRepository.updateUserProfile(profile)
        }
    }

    val blurEnabled: StateFlow<Boolean> = userPreferencesRepository.blurEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val shadowsEnabled: StateFlow<Boolean> = userPreferencesRepository.shadowsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val blurStrength: StateFlow<Float> = userPreferencesRepository.blurStrength
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 12f)

    val blurTintAlpha: StateFlow<Float> = userPreferencesRepository.blurTintAlpha
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    val shadowStyle: StateFlow<String> = userPreferencesRepository.shadowStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Subtle")

    // Update State
    private val _updateStatus = kotlinx.coroutines.flow.MutableStateFlow<com.nami.peace.data.updater.UpdateState>(com.nami.peace.data.updater.UpdateState.Idle)
    val updateStatus: StateFlow<com.nami.peace.data.updater.UpdateState> = _updateStatus.asStateFlow()
    
    val userProfile: kotlinx.coroutines.flow.Flow<com.nami.peace.data.repository.UserProfile> = userPreferencesRepository.userProfile

    fun checkForUpdates() {
        viewModelScope.launch {
            _updateStatus.value = com.nami.peace.data.updater.UpdateState.Checking
            appUpdater.checkForUpdate().collect { state ->
                _updateStatus.value = state
            }
        }
    }

    fun startUpdate(url: String) {
        viewModelScope.launch {
            appUpdater.downloadApk(url).collect { status ->
               when(status) {
                   is com.nami.peace.data.updater.DownloadStatus.Downloading -> _updateStatus.value = com.nami.peace.data.updater.UpdateState.Downloading(0)
                   is com.nami.peace.data.updater.DownloadStatus.Progress -> _updateStatus.value = com.nami.peace.data.updater.UpdateState.Downloading(status.percent)
                   is com.nami.peace.data.updater.DownloadStatus.ReadyToInstall -> {
                       _updateStatus.value = com.nami.peace.data.updater.UpdateState.ReadyToInstall(status.file)
                       appUpdater.installApk(status.file) // Trigger install immediately
                   }
                   is com.nami.peace.data.updater.DownloadStatus.Error -> _updateStatus.value = com.nami.peace.data.updater.UpdateState.Error(status.message)
                   else -> {}
               }
            }
        }
    }

    fun resetUpdateState() {
        _updateStatus.value = com.nami.peace.data.updater.UpdateState.Idle
    }

    // Consolidated Appearance State
    val themeMode: StateFlow<String> = userPreferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Auto")

    val moodColor: StateFlow<String> = userPreferencesRepository.moodColor
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Ocean")

    val isBoldText: StateFlow<Boolean> = userPreferencesRepository.isBoldText
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val reduceMotion: StateFlow<Boolean> = userPreferencesRepository.reduceMotion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val shadowStrength: StateFlow<Float> = userPreferencesRepository.shadowStrength
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.5f)

    fun setThemeMode(mode: String) {
        viewModelScope.launch { userPreferencesRepository.setThemeMode(mode) }
    }

    fun setMoodColor(color: String) {
        viewModelScope.launch { userPreferencesRepository.setMoodColor(color) }
    }

    fun setBoldText(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setBoldText(enabled) }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { userPreferencesRepository.setReduceMotion(enabled) }
    }

    fun setShadowStrength(strength: Float) {
        viewModelScope.launch { userPreferencesRepository.setShadowStrength(strength) }
    }

    fun setBlurEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setBlurEnabled(enabled)
        }
    }

    fun setShadowsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setShadowsEnabled(enabled)
        }
    }

    fun setBlurStrength(strength: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setBlurStrength(strength)
        }
    }

    fun setBlurTintAlpha(alpha: Float) {
        viewModelScope.launch {
            userPreferencesRepository.setBlurTintAlpha(alpha)
        }
    }

    fun setShadowStyle(style: String) {
        viewModelScope.launch {
            userPreferencesRepository.setShadowStyle(style)
        }
    }
    val fontFamily: StateFlow<String> = userPreferencesRepository.fontFamily
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "System")

    fun setFontFamily(font: String) {
        viewModelScope.launch { userPreferencesRepository.setFontFamily(font) }
    }

    // Language State
    fun getCurrentLanguageCode(): String {
        val locales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) locales.toLanguageTags() else "Auto"
    }

    private val _currentLanguageCode = kotlinx.coroutines.flow.MutableStateFlow(getCurrentLanguageCode())
    val currentLanguageCode: StateFlow<String> = _currentLanguageCode.asStateFlow()

    fun setLanguage(code: String) {
        val localeList = if (code == "Auto" || code.isEmpty()) {
            androidx.core.os.LocaleListCompat.getEmptyLocaleList()
        } else {
            androidx.core.os.LocaleListCompat.forLanguageTags(code)
        }
        androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
        _currentLanguageCode.value = code
    }
}
