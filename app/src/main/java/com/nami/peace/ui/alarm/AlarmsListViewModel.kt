package com.nami.peace.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.RecurrenceType
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.scheduler.AlarmScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmsListViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmsListUiState())
    val uiState: StateFlow<AlarmsListUiState> = _uiState.asStateFlow()

    private val _toastMessage = kotlinx.coroutines.channels.Channel<String>()
    val toastMessage = _toastMessage.receiveAsFlow()

    init {
        viewModelScope.launch {
            repository.getReminders().collectLatest { reminders ->
                val now = System.currentTimeMillis()
                
                // Active alarms: Not completed AND in the future
                // (Or all future active reminders, regardless of completion? 
                // Prompt said "active reminders (!isCompleted and time > now)")
                
                val activeSorted = reminders
                    .filter { !it.isCompleted && it.startTimeInMillis > now }
                    .sortedBy { it.startTimeInMillis }

                val next = activeSorted.firstOrNull { it.isEnabled }

                _uiState.value = AlarmsListUiState(
                    nextAlarm = next,
                    activeAlarms = activeSorted
                )
            }
        }
    }

    fun isNapActive(label: String): Boolean {
        val alreadyExists = _uiState.value.activeAlarms.any { 
            it.title == label && !it.isCompleted 
        }
        if (alreadyExists) {
            viewModelScope.launch {
                _toastMessage.send("$label already active")
            }
        }
        return alreadyExists
    }

    fun addQuickNap(minutes: Int, label: String) {
        // Double-check just in case, but silent return as toast handled by UI check
        if (isNapActive(label)) return

        viewModelScope.launch {
            val triggerTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
            
            val reminder = Reminder(
                title = label,
                startTimeInMillis = triggerTime,
                isEnabled = true,
                isCompleted = false,
                priority = PriorityLevel.HIGH,
                recurrenceType = RecurrenceType.ONE_TIME,
                isNagModeEnabled = false,
                nagIntervalInMillis = null,
                nagTotalRepetitions = 0,
                category = ReminderCategory.HEALTH,
                daysOfWeek = emptyList() // One-off
            )
            
            val id = repository.insertReminder(reminder)
            // Schedule it immediately
            // We need to fetch it back with ID or construct one with ID. 
            // Insert usually returns ID.
            // Assuming insertReminder returns Long (id).
            
            val savedReminder = reminder.copy(id = id.toInt())
            alarmScheduler.schedule(savedReminder)
        }
    }
    
    fun toggleReminder(reminder: Reminder) {
        viewModelScope.launch {
            val newStatus = !reminder.isEnabled
            val updatedReminder = reminder.copy(isEnabled = newStatus)
            repository.updateReminder(updatedReminder)
            
            if (newStatus) {
                alarmScheduler.schedule(updatedReminder)
                _toastMessage.send("Reminder resumed")
            } else {
                alarmScheduler.cancel(updatedReminder)
                _toastMessage.send("Reminder paused")
            }
        }
    }
}

data class AlarmsListUiState(
    val nextAlarm: Reminder? = null,
    val activeAlarms: List<Reminder> = emptyList()
)
