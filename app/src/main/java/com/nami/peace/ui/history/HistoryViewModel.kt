package com.nami.peace.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nami.peace.data.local.HistoryDao
import com.nami.peace.data.local.HistoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val historyDao: HistoryDao
) : ViewModel() {

    val history: StateFlow<List<HistoryEntity>> = historyDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    fun deleteHistoryItem(item: HistoryEntity) {
        viewModelScope.launch {
            historyDao.delete(item)
        }
    }

    fun getHistoryItem(id: Int): kotlinx.coroutines.flow.Flow<HistoryEntity?> {
        return historyDao.getById(id)
    }
}
