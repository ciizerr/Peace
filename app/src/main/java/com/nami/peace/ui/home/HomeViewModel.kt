package com.nami.peace.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.scheduler.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getReminders().collectLatest { reminders ->
                // Filter out completed reminders and sort by time
                val activeList = reminders.filter { !it.isCompleted }.sortedBy { it.startTimeInMillis }
                
                // Identify Next Up (The very first one that is ENABLED)
                val nextUp = activeList.filter { it.isEnabled }.firstOrNull()
                
                // Group the rest by date headers
                // User wants Next Up to appear in the list as well, so we use activeList directly
                val sections = activeList.groupBy { 
                    com.nami.peace.util.DateUtils.formatDateHeader(it.startTimeInMillis) 
                }

                _uiState.value = HomeUiState(
                    nextUp = nextUp,
                    sections = sections
                )
            }
        }
    }

    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val newStatus = !reminder.isEnabled
            val updatedReminder = reminder.copy(isEnabled = newStatus)
            repository.updateReminder(updatedReminder)
            
            if (newStatus) {
                alarmScheduler.schedule(updatedReminder)
                com.nami.peace.util.DebugLogger.log("Reminder Toggled ON: ${reminder.title}")
            } else {
                alarmScheduler.cancel(updatedReminder)
                com.nami.peace.util.DebugLogger.log("Reminder Toggled OFF: ${reminder.title}")
            }
        }
    }
    
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            alarmScheduler.cancel(reminder)
        }
    }
}

data class HomeUiState(
    val nextUp: Reminder? = null,
    val sections: Map<String, List<Reminder>> = emptyMap()
)
