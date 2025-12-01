package com.nami.peace.ui.reminder

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.AlarmSound
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.domain.usecase.CalculateMaxRepetitionsUseCase
import com.nami.peace.domain.usecase.GetAlarmSoundsUseCase
import com.nami.peace.scheduler.AlarmScheduler
import com.nami.peace.util.alarm.AlarmSoundManager
import com.nami.peace.widget.WidgetUpdateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class AddEditReminderViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val calculateMaxRepetitionsUseCase: CalculateMaxRepetitionsUseCase,
    private val alarmScheduler: AlarmScheduler,
    private val getAlarmSoundsUseCase: GetAlarmSoundsUseCase,
    private val alarmSoundManager: AlarmSoundManager,
    private val widgetUpdateManager: WidgetUpdateManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditReminderUiState())
    val uiState: StateFlow<AddEditReminderUiState> = _uiState.asStateFlow()

    init {
        // Load available alarm sounds
        viewModelScope.launch {
            val sounds = getAlarmSoundsUseCase.getAllSystemSounds()
            _uiState.value = _uiState.value.copy(availableAlarmSounds = sounds)
        }
        
        val reminderId = savedStateHandle.get<Int>("reminderId")
        if (reminderId != null && reminderId != -1) {
            viewModelScope.launch {
                repository.getReminderById(reminderId)?.let { reminder ->
                    // Reverse calculate interval
                    val (intervalValue, intervalUnit) = if (reminder.nagIntervalInMillis != null) {
                        if (reminder.nagIntervalInMillis % (60 * 60 * 1000L) == 0L) {
                            (reminder.nagIntervalInMillis / (60 * 60 * 1000L)).toString() to TimeUnit.HOURS
                        } else {
                            (reminder.nagIntervalInMillis / (60 * 1000L)).toString() to TimeUnit.MINUTES
                        }
                    } else {
                        "15" to TimeUnit.MINUTES
                    }

                    // Load custom alarm sound if set
                    val customSound = if (reminder.customAlarmSoundUri != null && reminder.customAlarmSoundName != null) {
                        alarmSoundManager.createCustomSound(
                            Uri.parse(reminder.customAlarmSoundUri),
                            reminder.customAlarmSoundName
                        )
                    } else {
                        null
                    }

                    _uiState.value = _uiState.value.copy(
                        id = reminder.id,
                        title = reminder.title,
                        priority = reminder.priority,
                        startTimeInMillis = reminder.startTimeInMillis,
                        recurrenceType = reminder.recurrenceType,
                        isNagModeEnabled = reminder.isNagModeEnabled,
                        nagIntervalInMillis = reminder.nagIntervalInMillis,
                        nagIntervalValue = intervalValue,
                        nagIntervalUnit = intervalUnit,
                        nagTotalRepetitions = reminder.nagTotalRepetitions,
                        category = reminder.category,
                        isStrictSchedulingEnabled = reminder.isStrictSchedulingEnabled,
                        dateInMillis = reminder.dateInMillis,
                        daysOfWeek = reminder.daysOfWeek,
                        selectedAlarmSound = customSound
                    )
                    recalculateMaxRepetitions()
                }
            }
        } else {
            // New Reminder: Default to Today
            _uiState.value = _uiState.value.copy(
                dateInMillis = System.currentTimeMillis()
            )
        }
    }

    fun onEvent(event: AddEditReminderEvent) {
        when (event) {
            is AddEditReminderEvent.TitleChanged -> {
                _uiState.value = _uiState.value.copy(title = event.title)
            }
            is AddEditReminderEvent.PriorityChanged -> {
                _uiState.value = _uiState.value.copy(priority = event.priority)
            }
            is AddEditReminderEvent.CategoryChanged -> {
                _uiState.value = _uiState.value.copy(category = event.category)
            }
            is AddEditReminderEvent.StrictModeToggled -> {
                _uiState.value = _uiState.value.copy(isStrictSchedulingEnabled = event.isEnabled)
            }
            is AddEditReminderEvent.RecurrenceChanged -> {
                _uiState.value = _uiState.value.copy(
                    recurrenceType = event.recurrenceType
                )
                recalculateMaxRepetitions()
            }
            is AddEditReminderEvent.StartTimeChanged -> {
                _uiState.value = _uiState.value.copy(startTimeInMillis = event.time)
                recalculateMaxRepetitions()
            }
            is AddEditReminderEvent.NagModeToggled -> {
                if (event.isEnabled) {
                    // Check Soft Warning Condition
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = _uiState.value.startTimeInMillis
                    val hour = calendar.get(Calendar.HOUR_OF_DAY)
                    
                    val isNightTime = hour >= 21 || hour < 4
                    val isDaily = _uiState.value.recurrenceType == RecurrenceType.DAILY
                    
                    if (isDaily && isNightTime) {
                        _uiState.value = _uiState.value.copy(showSoftWarningDialog = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isNagModeEnabled = true)
                        recalculateMaxRepetitions()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isNagModeEnabled = false)
                }
            }
            is AddEditReminderEvent.DismissWarningDialog -> {
                _uiState.value = _uiState.value.copy(showSoftWarningDialog = false)
            }
            is AddEditReminderEvent.ConfirmWarningDialog -> {
                _uiState.value = _uiState.value.copy(
                    showSoftWarningDialog = false,
                    isNagModeEnabled = true
                )
                recalculateMaxRepetitions()
            }
            is AddEditReminderEvent.PermissionStateChanged -> {
                _uiState.value = _uiState.value.copy(showPermissionBanner = !event.hasPermission)
            }
            is AddEditReminderEvent.NagIntervalValueChanged -> {
                _uiState.value = _uiState.value.copy(nagIntervalValue = event.value)
                updateNagIntervalInMillis()
            }
            is AddEditReminderEvent.NagIntervalUnitChanged -> {
                _uiState.value = _uiState.value.copy(nagIntervalUnit = event.unit)
                updateNagIntervalInMillis()
            }
            is AddEditReminderEvent.NagRepetitionsChanged -> {
                _uiState.value = _uiState.value.copy(nagTotalRepetitions = event.repetitions)
            }
            is AddEditReminderEvent.DateChanged -> {
                _uiState.value = _uiState.value.copy(dateInMillis = event.dateInMillis)
            }
            is AddEditReminderEvent.DayToggled -> {
                val currentDays = _uiState.value.daysOfWeek.toMutableList()
                if (currentDays.contains(event.day)) {
                    currentDays.remove(event.day)
                } else {
                    currentDays.add(event.day)
                }
                _uiState.value = _uiState.value.copy(daysOfWeek = currentDays)
            }
            is AddEditReminderEvent.AlarmSoundSelected -> {
                _uiState.value = _uiState.value.copy(selectedAlarmSound = event.sound)
            }
            is AddEditReminderEvent.ShowAlarmSoundPicker -> {
                _uiState.value = _uiState.value.copy(showAlarmSoundPicker = event.show)
            }
            is AddEditReminderEvent.PlayAlarmSoundPreview -> {
                alarmSoundManager.playPreview(event.sound)
            }
            is AddEditReminderEvent.StopAlarmSoundPreview -> {
                alarmSoundManager.stopPreview()
            }
            is AddEditReminderEvent.CustomAlarmSoundPicked -> {
                val customSound = alarmSoundManager.createCustomSound(event.uri, event.name)
                _uiState.value = _uiState.value.copy(selectedAlarmSound = customSound)
            }
            is AddEditReminderEvent.SaveReminder -> {
                saveReminder()
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // Stop any playing preview when ViewModel is cleared
        alarmSoundManager.stopPreview()
    }

    private fun updateNagIntervalInMillis() {
        val value = _uiState.value.nagIntervalValue.toLongOrNull() ?: 0L
        val multiplier = when (_uiState.value.nagIntervalUnit) {
            TimeUnit.MINUTES -> 60 * 1000L
            TimeUnit.HOURS -> 60 * 60 * 1000L
        }
        val millis = if (value > 0) value * multiplier else null
        
        _uiState.value = _uiState.value.copy(nagIntervalInMillis = millis)
        recalculateMaxRepetitions()
    }

    private fun recalculateMaxRepetitions() {
        val state = _uiState.value
        if (state.isNagModeEnabled && state.nagIntervalInMillis != null) {
            val maxReps = calculateMaxRepetitionsUseCase(state.startTimeInMillis, state.nagIntervalInMillis)
            _uiState.value = state.copy(
                maxAllowedRepetitions = maxReps,
                nagTotalRepetitions = if (state.nagTotalRepetitions > maxReps) maxReps else state.nagTotalRepetitions
            )
        }
    }

    private fun saveReminder() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // Construct a temporary reminder to calculate the correct next trigger time
            val tempReminder = Reminder(
                id = state.id,
                title = state.title,
                priority = state.priority,
                startTimeInMillis = state.startTimeInMillis, // This is just the time picker value (today's date + selected time)
                recurrenceType = state.recurrenceType,
                isNagModeEnabled = state.isNagModeEnabled,
                nagIntervalInMillis = state.nagIntervalInMillis,
                nagTotalRepetitions = state.nagTotalRepetitions,
                category = state.category,
                isStrictSchedulingEnabled = state.isStrictSchedulingEnabled,
                dateInMillis = state.dateInMillis,
                daysOfWeek = state.daysOfWeek
            )

            // Calculate the actual next start time (e.g., next Monday)
            val calculatedStartTime = alarmScheduler.calculateNextTriggerTime(tempReminder)

            // When saving manually, we reset the anchor (originalStartTime) to the new calculated start time.
            val reminder = tempReminder.copy(
                startTimeInMillis = calculatedStartTime,
                originalStartTimeInMillis = calculatedStartTime,
                customAlarmSoundUri = state.selectedAlarmSound?.uri?.toString(),
                customAlarmSoundName = state.selectedAlarmSound?.name
            )
            
            val id = repository.insertReminder(reminder)
            
            // Schedule Alarm
            alarmScheduler.schedule(reminder.copy(id = id.toInt()))
            
            // Update widgets to reflect the new reminder
            widgetUpdateManager.onReminderDataChanged()
            
            // Navigate back (handled by UI via effect or callback)
        }
    }
}

data class AddEditReminderUiState(
    val id: Int = 0,
    val title: String = "",
    val category: ReminderCategory = ReminderCategory.GENERAL,
    val isStrictSchedulingEnabled: Boolean = false,
    val priority: PriorityLevel = PriorityLevel.MEDIUM,
    val startTimeInMillis: Long = System.currentTimeMillis(),
    val recurrenceType: RecurrenceType = RecurrenceType.ONE_TIME,
    val isNagModeEnabled: Boolean = false,
    val nagIntervalInMillis: Long? = null,
    val nagIntervalValue: String = "15",
    val nagIntervalUnit: TimeUnit = TimeUnit.MINUTES,
    val nagTotalRepetitions: Int = 0,
    val maxAllowedRepetitions: Int = 0,
    val showSoftWarningDialog: Boolean = false,
    val showPermissionBanner: Boolean = false,
    val dateInMillis: Long? = null,
    val daysOfWeek: List<Int> = emptyList(),
    val selectedAlarmSound: AlarmSound? = null,
    val availableAlarmSounds: List<AlarmSound> = emptyList(),
    val showAlarmSoundPicker: Boolean = false
)

sealed class AddEditReminderEvent {
    data class TitleChanged(val title: String) : AddEditReminderEvent()
    data class CategoryChanged(val category: ReminderCategory) : AddEditReminderEvent()
    data class StrictModeToggled(val isEnabled: Boolean) : AddEditReminderEvent()
    data class PriorityChanged(val priority: PriorityLevel) : AddEditReminderEvent()
    data class RecurrenceChanged(val recurrenceType: RecurrenceType) : AddEditReminderEvent()
    data class StartTimeChanged(val time: Long) : AddEditReminderEvent()
    data class NagModeToggled(val isEnabled: Boolean) : AddEditReminderEvent()
    data class NagIntervalValueChanged(val value: String) : AddEditReminderEvent()
    data class NagIntervalUnitChanged(val unit: TimeUnit) : AddEditReminderEvent()
    data class NagRepetitionsChanged(val repetitions: Int) : AddEditReminderEvent()
    object DismissWarningDialog : AddEditReminderEvent()
    object ConfirmWarningDialog : AddEditReminderEvent()
    data class PermissionStateChanged(val hasPermission: Boolean) : AddEditReminderEvent()
    data class DateChanged(val dateInMillis: Long?) : AddEditReminderEvent()
    data class DayToggled(val day: Int) : AddEditReminderEvent()
    data class AlarmSoundSelected(val sound: AlarmSound?) : AddEditReminderEvent()
    data class ShowAlarmSoundPicker(val show: Boolean) : AddEditReminderEvent()
    data class PlayAlarmSoundPreview(val sound: AlarmSound) : AddEditReminderEvent()
    object StopAlarmSoundPreview : AddEditReminderEvent()
    data class CustomAlarmSoundPicked(val uri: Uri, val name: String) : AddEditReminderEvent()
    object SaveReminder : AddEditReminderEvent()
}

enum class TimeUnit {
    MINUTES, HOURS
}
