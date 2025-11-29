package com.nami.peace.ui.reminder

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import com.nami.peace.domain.repository.ReminderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReminderDetailUiState(
    val reminder: Reminder? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ReminderDetailViewModel @Inject constructor(
    private val repository: ReminderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = mutableStateOf(ReminderDetailUiState())
    val uiState: State<ReminderDetailUiState> = _uiState

    init {
        val reminderId = savedStateHandle.get<Int>("reminderId")
        if (reminderId != null && reminderId != -1) {
            loadReminder(reminderId)
        } else {
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun loadReminder(id: Int) {
        viewModelScope.launch {
            val reminder = repository.getReminderById(id)
            _uiState.value = _uiState.value.copy(
                reminder = reminder,
                isLoading = false
            )
        }
    }
}
