package com.nami.peace.ui.garden

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.data.local.GardenTheme
import com.nami.peace.domain.usecase.*
import com.nami.peace.ui.theme.GardenThemeConfig
import com.nami.peace.ui.theme.getAllGardenThemeConfigs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Peace Garden screen.
 * 
 * Manages:
 * - Garden theme selection and configuration
 * - Growth stage information and visualization
 * - Streak tracking and display
 * - Milestone progress and achievements
 * 
 * Requirements: 18.1, 18.2, 18.3, 18.5, 18.6, 18.7
 */
@HiltViewModel
class PeaceGardenViewModel @Inject constructor(
    private val getGardenThemeConfigUseCase: GetGardenThemeConfigUseCase,
    private val updateGardenThemeUseCase: UpdateGardenThemeUseCase,
    private val getGrowthStageInfoUseCase: GetGrowthStageInfoUseCase,
    private val getStreakInfoUseCase: GetStreakInfoUseCase,
    private val checkMilestoneUseCase: CheckMilestoneUseCase
) : ViewModel() {

    // Current theme configuration
    private val _currentThemeConfig = MutableStateFlow<GardenThemeConfig?>(null)
    val currentThemeConfig: StateFlow<GardenThemeConfig?> = _currentThemeConfig.asStateFlow()

    // All available themes
    val availableThemes: List<GardenThemeConfig> = getAllGardenThemeConfigs()

    // Growth stage information
    private val _growthStageInfo = MutableStateFlow<GrowthStageInfo?>(null)
    val growthStageInfo: StateFlow<GrowthStageInfo?> = _growthStageInfo.asStateFlow()

    // Streak information
    private val _streakInfo = MutableStateFlow<StreakInfo?>(null)
    val streakInfo: StateFlow<StreakInfo?> = _streakInfo.asStateFlow()

    // Milestone information
    private val _nextMilestone = MutableStateFlow<Int?>(null)
    val nextMilestone: StateFlow<Int?> = _nextMilestone.asStateFlow()

    private val _achievedMilestones = MutableStateFlow<List<Int>>(emptyList())
    val achievedMilestones: StateFlow<List<Int>> = _achievedMilestones.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadGardenData()
    }

    /**
     * Load all garden data including theme, growth stage, streak, and milestones.
     */
    private fun loadGardenData() {
        viewModelScope.launch {
            _isLoading.value = true

            // Load theme configuration
            getGardenThemeConfigUseCase().collect { config ->
                _currentThemeConfig.value = config
            }
        }

        viewModelScope.launch {
            // Load growth stage info
            getGrowthStageInfoUseCase().collect { info ->
                _growthStageInfo.value = info
            }
        }

        viewModelScope.launch {
            // Load streak info
            val streakInfo = getStreakInfoUseCase()
            _streakInfo.value = streakInfo

            // Load milestone info
            val nextMilestone = checkMilestoneUseCase.getNextMilestone()
            _nextMilestone.value = nextMilestone

            val achievedMilestones = checkMilestoneUseCase.getAchievedMilestones()
            _achievedMilestones.value = achievedMilestones

            _isLoading.value = false
        }
    }

    /**
     * Update the garden theme.
     * 
     * @param theme The new theme to apply
     */
    fun updateTheme(theme: GardenTheme) {
        viewModelScope.launch {
            try {
                updateGardenThemeUseCase(theme)
            } catch (e: Exception) {
                // Log error but don't crash
                e.printStackTrace()
            }
        }
    }

    /**
     * Refresh all garden data.
     */
    fun refresh() {
        loadGardenData()
    }
}
