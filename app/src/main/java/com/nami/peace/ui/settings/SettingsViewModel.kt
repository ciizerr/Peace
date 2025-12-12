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
}
