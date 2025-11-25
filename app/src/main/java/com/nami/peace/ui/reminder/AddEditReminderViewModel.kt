package com.nami.peace.ui.reminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.domain.usecase.CalculateMaxRepetitionsUseCase
import com.nami.peace.scheduler.AlarmScheduler
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
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditReminderUiState())
    val uiState: StateFlow<AddEditReminderUiState> = _uiState.asStateFlow()

    fun onEvent(event: AddEditReminderEvent) {
        when (event) {
            is AddEditReminderEvent.TitleChanged -> {
                _uiState.value = _uiState.value.copy(title = event.title)
            }
            is AddEditReminderEvent.PriorityChanged -> {
                _uiState.value = _uiState.value.copy(priority = event.priority)
            }
            is AddEditReminderEvent.RecurrenceChanged -> {
                _uiState.value = _uiState.value.copy(
                    recurrenceType = event.recurrenceType,
                    // Rule 1: Nag Mode disabled if DAILY
                    isNagModeEnabled = if (event.recurrenceType == RecurrenceType.DAILY) false else _uiState.value.isNagModeEnabled
                )
                recalculateMaxRepetitions()
            }
            is AddEditReminderEvent.StartTimeChanged -> {
                _uiState.value = _uiState.value.copy(startTimeInMillis = event.time)
                recalculateMaxRepetitions()
            }
            is AddEditReminderEvent.NagModeToggled -> {
                _uiState.value = _uiState.value.copy(isNagModeEnabled = event.isEnabled)
            }
            is AddEditReminderEvent.NagIntervalChanged -> {
                _uiState.value = _uiState.value.copy(nagIntervalInMillis = event.interval)
                recalculateMaxRepetitions()
            }
            is AddEditReminderEvent.SaveReminder -> {
                saveReminder()
            }
        }
    }

    private fun recalculateMaxRepetitions() {
        val state = _uiState.value
        if (state.isNagModeEnabled && state.nagIntervalInMillis != null) {
            val maxReps = calculateMaxRepetitionsUseCase(state.startTimeInMillis, state.nagIntervalInMillis)
            _uiState.value = state.copy(nagTotalRepetitions = maxReps)
        }
    }

    private fun saveReminder() {
        viewModelScope.launch {
            val state = _uiState.value
            val reminder = Reminder(
                title = state.title,
                priority = state.priority,
                startTimeInMillis = state.startTimeInMillis,
                recurrenceType = state.recurrenceType,
                isNagModeEnabled = state.isNagModeEnabled,
                nagIntervalInMillis = state.nagIntervalInMillis,
                nagTotalRepetitions = state.nagTotalRepetitions
            )
            val id = repository.insertReminder(reminder)
            
            // Schedule Alarm
            alarmScheduler.schedule(reminder.copy(id = id.toInt()))
            
            // Navigate back (handled by UI via effect or callback)
        }
    }
}

data class AddEditReminderUiState(
    val title: String = "",
    val priority: PriorityLevel = PriorityLevel.MEDIUM,
    val startTimeInMillis: Long = System.currentTimeMillis(),
    val recurrenceType: RecurrenceType = RecurrenceType.ONE_TIME,
    val isNagModeEnabled: Boolean = false,
    val nagIntervalInMillis: Long? = null,
    val nagTotalRepetitions: Int = 0
)

sealed class AddEditReminderEvent {
    data class TitleChanged(val title: String) : AddEditReminderEvent()
    data class PriorityChanged(val priority: PriorityLevel) : AddEditReminderEvent()
    data class RecurrenceChanged(val recurrenceType: RecurrenceType) : AddEditReminderEvent()
    data class StartTimeChanged(val time: Long) : AddEditReminderEvent()
    data class NagModeToggled(val isEnabled: Boolean) : AddEditReminderEvent()
    data class NagIntervalChanged(val interval: Long) : AddEditReminderEvent()
    object SaveReminder : AddEditReminderEvent()
}
