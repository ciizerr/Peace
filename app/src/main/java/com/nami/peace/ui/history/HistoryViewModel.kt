package com.nami.peace.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import com.nami.peace.scheduler.AlarmScheduler
import javax.inject.Inject

data class HistoryUiState(
    val groupedItems: Map<String, List<Reminder>> = emptyMap(),
    val historyDates: Set<LocalDate> = emptySet(),
    val selectedDate: LocalDate? = null,
    val isCalendarExpanded: Boolean = false,
    val selectedReceipt: Reminder? = null,
    val currentMonth: YearMonth = YearMonth.now(),
    val userMessage: String? = null, // For Snackbar
    val selectedIds: Set<Int> = emptySet()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _isCalendarExpanded = MutableStateFlow(false)
    private val _selectedReceipt = MutableStateFlow<Reminder?>(null)
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _selectedIds = MutableStateFlow<Set<Int>>(emptySet())

    val uiState: StateFlow<HistoryUiState> = combine(
        _selectedDate, _isCalendarExpanded, _selectedReceipt, _currentMonth, _userMessage
    ) { date, expanded, receipt, month, msg ->
        FilterArgs(date, expanded, receipt, month, msg, emptySet())
    }.combine(_selectedIds) { args, selIds ->
        args.copy(selectedIds = selIds)
    }.combine(repository.getReminders()) { filters, reminders ->
        val completed = reminders.filter { it.isCompleted }
            .sortedByDescending { it.completedTime ?: 0L }

        // Populate set of dates with history
        val historyDates = completed.mapNotNull { reminder ->
            reminder.completedTime?.let { time ->
                Instant.ofEpochMilli(time).atZone(ZoneId.systemDefault()).toLocalDate()
            }
        }.toSet()

        // Filter items
        val filteredItems = if (filters.selectedDate != null) {
            completed.filter {
                val itemDate = Instant.ofEpochMilli(it.completedTime ?: 0L)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                itemDate == filters.selectedDate
            }
        } else {
            completed
        }

        // Group items
        val groups = filteredItems.groupBy { reminder ->
            val time = reminder.completedTime ?: 0L
            when {
                DateUtils.isToday(time) -> "Today"
                DateUtils.isYesterday(time) -> "Yesterday"
                filters.selectedDate != null -> SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(time))
                else -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(time))
            }
        }

        HistoryUiState(
            groupedItems = groups,
            historyDates = historyDates,
            selectedDate = filters.selectedDate,
            isCalendarExpanded = filters.isExpanded,
            selectedReceipt = filters.selectedReceipt,
            currentMonth = filters.currentMonth,
            userMessage = filters.userMessage,
            selectedIds = filters.selectedIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )
    
    private data class FilterArgs(
        val selectedDate: LocalDate?,
        val isExpanded: Boolean,
        val selectedReceipt: Reminder?,
        val currentMonth: YearMonth,
        val userMessage: String?,
        val selectedIds: Set<Int>
    )

    fun toggleCalendar() {
        _isCalendarExpanded.update { !it }
    }

    fun selectDate(date: LocalDate) {
        if (_selectedDate.value == date) {
             // Deselect if already selected
            _selectedDate.value = null
        } else {
            _selectedDate.value = date
        }
    }

    fun clearDateFilter() {
        _selectedDate.value = null
    }

    fun changeMonth(amount: Long) {
        _currentMonth.update { it.plusMonths(amount) }
    }

    fun openReceipt(reminder: Reminder) {
        _selectedReceipt.value = reminder
    }

    fun dismissReceipt() {
        _selectedReceipt.value = null
    }

    fun messageShown() {
        _userMessage.value = null
    }

    fun restoreTask(reminder: Reminder) {
        viewModelScope.launch {
            // Restore to TODAY logic
            // 1. Get today's date + original time
            val originalCal = java.util.Calendar.getInstance().apply { timeInMillis = reminder.startTimeInMillis }
            val todayCal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, originalCal.get(java.util.Calendar.HOUR_OF_DAY))
                set(java.util.Calendar.MINUTE, originalCal.get(java.util.Calendar.MINUTE))
                set(java.util.Calendar.SECOND, 0)
                set(java.util.Calendar.MILLISECOND, 0)
            }
            
            // If the time has already passed today, should we push to tomorrow? 
            // The prompt says "You likely intend to do the task now." 
            // So moving to Today (even if technically past hour) puts it in "Today" list which is what we want.
            // But if it's strict scheduling, it might trigger immediately? That's acceptable.
            
            val newTime = todayCal.timeInMillis
            
            val restoredReminder = reminder.copy(
                isCompleted = false,
                isAbandoned = false, // usage: clear abandonment
                completedTime = null,
                startTimeInMillis = newTime,
                originalStartTimeInMillis = newTime // Update anchor? Yes, effectively a "new" task for today.
            )
            
            repository.updateReminder(restoredReminder)
            
            // Re-schedule alarm
            alarmScheduler.schedule(restoredReminder)
            
            _userMessage.value = "Task restored to Today"
            dismissReceipt()
        }
    }

    fun deleteTask(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            _userMessage.value = "Task deleted"
            dismissReceipt()
        }
    }

    fun repeatTask(reminder: Reminder) {
        viewModelScope.launch {
            val newReminder = reminder.copy(
                id = 0, // content copy means new ID
                isCompleted = false,
                startTimeInMillis = System.currentTimeMillis(),
                originalStartTimeInMillis = System.currentTimeMillis(),
                completedTime = null
            )
            repository.insertReminder(newReminder)
            _userMessage.value = "Task repeated"
            dismissReceipt()
        }
    }

    // Selection Mode Logic
    // _selectedIds moved to top

    fun toggleSelection(id: Int) {
        _selectedIds.update { current ->
            if (current.contains(id)) current - id else current + id
        }
    }

    fun selectAll() {
        val allIds = uiState.value.groupedItems.values.flatten().map { it.id }.toSet()
        _selectedIds.value = allIds
    }

    fun clearSelection() {
        _selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            val idsToDelete = _selectedIds.value
            if (idsToDelete.isNotEmpty()) {
                val remindersToDelete = uiState.value.groupedItems.values.flatten().filter { it.id in idsToDelete }
                remindersToDelete.forEach { repository.deleteReminder(it) }
                _selectedIds.value = emptySet()
                _userMessage.value = "${idsToDelete.size} tasks deleted"
            }
        }
    }
}
