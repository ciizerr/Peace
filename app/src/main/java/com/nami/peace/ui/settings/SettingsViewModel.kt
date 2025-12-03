package com.nami.peace.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val blurEnabled: StateFlow<Boolean> = userPreferencesRepository.blurEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val shadowsEnabled: StateFlow<Boolean> = userPreferencesRepository.shadowsEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val blurStrength: StateFlow<Float> = userPreferencesRepository.blurStrength
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 12f)

    val shadowStyle: StateFlow<String> = userPreferencesRepository.shadowStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Subtle")

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

    fun setShadowStyle(style: String) {
        viewModelScope.launch {
            userPreferencesRepository.setShadowStyle(style)
        }
    }
}
