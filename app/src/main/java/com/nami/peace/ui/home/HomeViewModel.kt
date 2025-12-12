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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ReminderRepository,
    private val alarmScheduler: AlarmScheduler,
    private val userPreferencesRepository: com.nami.peace.data.repository.UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedCategory = MutableStateFlow<com.nami.peace.domain.model.ReminderCategory?>(null)

    private val _toastMessage = kotlinx.coroutines.channels.Channel<String>()
    val toastMessage = _toastMessage.receiveAsFlow()

    init {
        viewModelScope.launch {
            // Combine reminders, category, and userProfile
            kotlinx.coroutines.flow.combine(
                repository.getReminders(),
                _selectedCategory,
                userPreferencesRepository.userProfile
            ) { reminders, category, userProfile ->
                Triple(reminders, category, userProfile)
            }.collectLatest { (reminders, category, userProfile) ->
                
                // 1. Separate Active vs Completed
                val completed = reminders.filter { it.isCompleted }
                val active = reminders.filter { !it.isCompleted }

                // 2. Filter Active (if category selected)
                val filteredActive = if (category != null) {
                    active.filter { it.category == category }
                } else {
                    active
                }
                
                // 3. Filter for TODAY ONLY (Dashboard Rule)
                // We only show items scheduled for today in the dashboard view (Focus/Buckets)
                // We also filter for ENABLED items. If a user pauses a task, it shouldn't block the Focus slot.
                val todayActive = filteredActive.filter { 
                    com.nami.peace.util.DateUtils.isToday(it.startTimeInMillis) && it.isEnabled 
                }
                val todayCompleted = completed.filter { com.nami.peace.util.DateUtils.isToday(it.startTimeInMillis) }

                // 4. Sort Today Tasks: Priority (High->Low), then Time (Earliest first)
                val sortedTodayActive = todayActive.sortedWith(
                    compareBy<Reminder> { it.priority.ordinal } 
                        .thenBy { it.startTimeInMillis }
                )

                // 5. Identify Focus Task (First item of Today)
                val focusTask = sortedTodayActive.firstOrNull()


                // 6. Bucket Remaining Tasks (Today Only)
                val remainingTasks = if (focusTask != null) sortedTodayActive.drop(1) else emptyList()
                
                val morningTasks = ArrayList<Reminder>()
                val afternoonTasks = ArrayList<Reminder>()
                val eveningTasks = ArrayList<Reminder>()

                remainingTasks.forEach { reminder ->
                    val bucket = getTaskBucket(reminder.startTimeInMillis)
                    when (bucket) {
                        BucketEnum.Morning -> morningTasks.add(reminder)
                        BucketEnum.Afternoon -> afternoonTasks.add(reminder)
                        BucketEnum.Evening -> eveningTasks.add(reminder)
                    }
                }

                // 7. Progress Stats (Today Only)
                val completedCount = todayCompleted.size
                val totalCount = todayActive.size + todayCompleted.size
                
                // 8. Dynamic Categories (Only show used categories)
                val usedCategories = reminders.map { it.category }.distinct().sortedBy { it.ordinal }

                _uiState.value = HomeUiState(
                    userProfile = userProfile,
                    greetingRes = getTimeBasedGreetingResource(),
                    focusTask = focusTask,
                    morningTasks = morningTasks,
                    afternoonTasks = afternoonTasks,
                    eveningTasks = eveningTasks,
                    completedCount = completedCount,
                    totalCount = totalCount,
                    selectedFilter = category,
                    availableCategories = usedCategories
                )
            }
        }
    }

    private enum class BucketEnum { Morning, Afternoon, Evening }

    private fun getTaskBucket(timeInMillis: Long): BucketEnum {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = timeInMillis
        val hour = cal.get(java.util.Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> BucketEnum.Morning
            hour < 18 -> BucketEnum.Afternoon
            else -> BucketEnum.Evening
        }
    }

    private fun getTimeBasedGreetingResource(): Int {
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 0..4 -> com.nami.peace.R.string.greeting_night
            in 5..11 -> com.nami.peace.R.string.greeting_morning
            in 12..17 -> com.nami.peace.R.string.greeting_afternoon
            else -> com.nami.peace.R.string.greeting_evening
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
                _toastMessage.send("Reminder resumed")
            } else {
                alarmScheduler.cancel(updatedReminder)
                com.nami.peace.util.DebugLogger.log("Reminder Toggled OFF: ${reminder.title}")
                _toastMessage.send("Reminder paused")
            }
        }
    }

    fun markAsDone(reminder: Reminder) {
        viewModelScope.launch {
            val updatedReminder = reminder.copy(isCompleted = true, completedTime = System.currentTimeMillis())
            repository.updateReminder(updatedReminder)
            alarmScheduler.cancel(updatedReminder)
            com.nami.peace.util.DebugLogger.log("Reminder Completed: ${reminder.title}")
            _toastMessage.send("Focus task completed")
        }
    }
    
    fun deleteReminder(reminder: Reminder) {
        viewModelScope.launch {
            // Soft Delete: Mark as Abandoned and Completed (so it leaves Active list)
            val abandonedReminder = reminder.copy(
                isAbandoned = true,
                isCompleted = true,
                completedTime = System.currentTimeMillis() // Track when it was abandoned
            )
            repository.updateReminder(abandonedReminder)
            
            // CRITICAL: Cancel the alarm so it doesn't ring as a ghost alarm
            alarmScheduler.cancel(reminder)
            com.nami.peace.util.DebugLogger.log("Reminder Abandoned: ${reminder.title}")
            _toastMessage.send("Task abandoned")
        }
    }

    fun deleteReminders(reminders: List<Reminder>) {
        viewModelScope.launch {
            reminders.forEach { reminder ->
                val abandonedReminder = reminder.copy(
                    isAbandoned = true,
                    isCompleted = true,
                    completedTime = System.currentTimeMillis()
                )
                repository.updateReminder(abandonedReminder)
                alarmScheduler.cancel(reminder)
            }
            _toastMessage.send("${reminders.size} tasks abandoned")
        }
    }

    fun updateUserProfile(profile: com.nami.peace.data.repository.UserProfile) {
        viewModelScope.launch {
            userPreferencesRepository.updateUserProfile(profile)
            _toastMessage.send("Profile saved")
        }
    }
}

data class HomeUiState(
    val userProfile: com.nami.peace.data.repository.UserProfile = com.nami.peace.data.repository.UserProfile(),
    val greetingRes: Int = com.nami.peace.R.string.coach_welcome,
    val focusTask: Reminder? = null,
    val morningTasks: List<Reminder> = emptyList(),
    val afternoonTasks: List<Reminder> = emptyList(),
    val eveningTasks: List<Reminder> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val selectedFilter: com.nami.peace.domain.model.ReminderCategory? = null,
    val availableCategories: List<com.nami.peace.domain.model.ReminderCategory> = emptyList()
)
