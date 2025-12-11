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
import javax.inject.Inject

data class HistoryUiState(
    val groupedItems: Map<String, List<Reminder>> = emptyMap(),
    val historyDates: Set<LocalDate> = emptySet(),
    val selectedDate: LocalDate? = null,
    val isCalendarExpanded: Boolean = false,
    val selectedReceipt: Reminder? = null,
    val currentMonth: YearMonth = YearMonth.now(),
    val userMessage: String? = null // For Snackbar
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow<LocalDate?>(null)
    private val _isCalendarExpanded = MutableStateFlow(false)
    private val _selectedReceipt = MutableStateFlow<Reminder?>(null)
    private val _currentMonth = MutableStateFlow(YearMonth.now())
    private val _userMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistoryUiState> = combine(
        combine(_selectedDate, _isCalendarExpanded, _selectedReceipt, _currentMonth, _userMessage) { date, expanded, receipt, month, msg ->
            FilterArgs(date, expanded, receipt, month, msg)
        },
        repository.getReminders()
    ) { filters, reminders ->
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
            userMessage = filters.userMessage
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
        val userMessage: String?
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
            repository.setTaskCompleted(reminder.id, false)
            _userMessage.value = "Task restored"
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
}
