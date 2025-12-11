package com.nami.peace.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import com.nami.peace.util.DateUtils

data class HistoryUiState(
    val groupedItems: Map<String, List<Reminder>> = emptyMap(),
    val selectedItem: Reminder? = null
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: ReminderRepository
) : ViewModel() {

    private val _selectedItem = MutableStateFlow<Reminder?>(null)
    
    val uiState: StateFlow<HistoryUiState> = kotlinx.coroutines.flow.combine(
        repository.getReminders(),
        _selectedItem
    ) { reminders, selectedItem ->
            val completed = reminders.filter { it.isCompleted }
                .sortedByDescending { it.completedTime ?: 0L }
            
            val groups = completed.groupBy { reminder ->
                 val time = reminder.completedTime ?: 0L
                 when {
                     DateUtils.isToday(time) -> "Today"
                     DateUtils.isYesterday(time) -> "Yesterday"
                     else -> SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(time))
                 }
            }
            HistoryUiState(groupedItems = groups, selectedItem = selectedItem)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState()
        )

    fun selectItem(reminder: Reminder) {
        _selectedItem.value = reminder
    }

    fun dismissSheet() {
        _selectedItem.value = null
    }

    fun restoreItem(reminder: Reminder) {
        viewModelScope.launch {
            repository.setTaskCompleted(reminder.id, false)
            dismissSheet()
        }
    }

    fun deleteItem(reminder: Reminder) {
        viewModelScope.launch {
            repository.deleteReminder(reminder)
            dismissSheet()
        }
    }

    fun repeatItem(reminder: Reminder) {
        viewModelScope.launch {
            // Create a copy for specific date? Or just next occurrence?
            // "Repeat" usually means "Do it again". 
            // We'll reset ID and completed status.
            val newReminder = reminder.copy(
                id = 0,
                isCompleted = false,
                startTimeInMillis = System.currentTimeMillis(),
                originalStartTimeInMillis = System.currentTimeMillis(),
                completedTime = null
            )
            repository.insertReminder(newReminder)
            dismissSheet()
        }
    }
}
