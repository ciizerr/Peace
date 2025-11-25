package com.nami.peace.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: ReminderRepository,
    // private val completeReminderUseCase: CompleteReminderUseCase // To be implemented
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    fun loadActiveReminders() {
        viewModelScope.launch {
            // Load all active reminders that are due now or in the past (and not completed)
            // For simplicity, we just load all active reminders and filter/sort
            val now = System.currentTimeMillis()
            val reminders = repository.getActiveReminders(now)
            
            // Sort by Priority (HIGH -> MEDIUM -> LOW)
            val sortedReminders = reminders.sortedBy { it.priority.ordinal } // Enum ordinal: HIGH=0, MEDIUM=1, LOW=2
            
            if (sortedReminders.isNotEmpty()) {
                _uiState.value = AlarmUiState(
                    heroReminder = sortedReminders.first(),
                    bundledReminders = sortedReminders.drop(1)
                )
            } else {
                // No reminders? Close screen.
                _uiState.value = AlarmUiState(shouldFinish = true)
            }
        }
    }

    fun markDone(reminder: Reminder) {
        viewModelScope.launch {
            // Mark as completed
            val updated = reminder.copy(isCompleted = true, isInNestedSnoozeLoop = false)
            repository.updateReminder(updated)
            
            // Cancel alarms (handled by repository/usecase ideally, but here for now)
            // We should use a UseCase for this to ensure AlarmManager is also cancelled/updated
            
            // Refresh list
            loadActiveReminders()
        }
    }
    
    fun snooze(reminder: Reminder) {
        // User explicitly snoozed.
        // Logic B says: "If the user Dismisses or Ignores... Micro-Loop activates".
        // So explicit snooze might just close the screen and let the Micro-Loop take over?
        // Or we can add a specific snooze duration.
        // For "Nag Mode", we just let the Micro-Loop run.
        _uiState.value = _uiState.value.copy(shouldFinish = true)
    }
}

data class AlarmUiState(
    val heroReminder: Reminder? = null,
    val bundledReminders: List<Reminder> = emptyList(),
    val shouldFinish: Boolean = false
)
