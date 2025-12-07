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

    private val _selectedCategory = MutableStateFlow<com.nami.peace.domain.model.ReminderCategory?>(null)

    init {
        viewModelScope.launch {
            // Combine reminders and validation of selected category
            kotlinx.coroutines.flow.combine(
                repository.getReminders(),
                _selectedCategory
            ) { reminders, category ->
                Pair(reminders, category)
            }.collectLatest { (reminders, category) ->
                
                // 1. Separate Active vs Completed
                val completed = reminders.filter { it.isCompleted }
                val rawActive = reminders.filter { !it.isCompleted }.sortedBy { it.startTimeInMillis }
                
                // 2. Calculate Stats
                // Streak: Current logic uses total completed count as a proxy for "seeds planted/grown"
                val streakDays = completed.size 
                
                // 3. Determine Coach Message
                val coachMessage = when {
                    completed.isEmpty() && rawActive.isEmpty() -> com.nami.peace.R.string.coach_welcome
                    rawActive.isEmpty() -> com.nami.peace.R.string.coach_all_done
                    else -> getGreetingMessage(rawActive.size)
                }

                // 4. Filter Active List
                val filteredActive = if (category != null) {
                    rawActive.filter { it.category == category }
                } else {
                    rawActive
                }
                
                // 5. Identify Next Up (from the filtered list)
                val enabledReminders = filteredActive.filter { it.isEnabled }
                val nextUp = if (enabledReminders.isNotEmpty()) {
                    val earliestTime = enabledReminders.first().startTimeInMillis
                    val timeWindow = 60 * 1000L 
                    val simultaneousReminders = enabledReminders.filter { 
                        kotlin.math.abs(it.startTimeInMillis - earliestTime) < timeWindow
                    }
                    simultaneousReminders.minByOrNull { it.priority.ordinal }
                } else {
                    null
                }
                
                // 6. Group by Date
                val sections = filteredActive.groupBy { 
                    com.nami.peace.util.DateUtils.formatDateHeader(it.startTimeInMillis) 
                }

                _uiState.value = HomeUiState(
                    nextUp = nextUp,
                    sections = sections,
                    selectedFilter = category,
                    streakDays = streakDays,
                    coachMessage = coachMessage,
                    activeCount = rawActive.size
                )
            }
        }
    }

    private fun getGreetingMessage(activeCount: Int): Int {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..4 -> com.nami.peace.R.string.coach_night
            in 5..11 -> com.nami.peace.R.string.coach_morning
            in 12..17 -> com.nami.peace.R.string.coach_afternoon
            else -> com.nami.peace.R.string.coach_evening
        }
    }

    fun onFilterSelected(category: com.nami.peace.domain.model.ReminderCategory?) {
        _selectedCategory.value = category
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

    fun deleteReminders(reminders: List<Reminder>) {
        viewModelScope.launch {
            reminders.forEach { reminder ->
                repository.deleteReminder(reminder)
                alarmScheduler.cancel(reminder)
            }
        }
    }
}

data class HomeUiState(
    val nextUp: Reminder? = null,
    val sections: Map<String, List<Reminder>> = emptyMap(),
    val selectedFilter: com.nami.peace.domain.model.ReminderCategory? = null,
    val streakDays: Int = 0,
    val coachMessage: Int = com.nami.peace.R.string.coach_welcome,
    val activeCount: Int = 0
)
