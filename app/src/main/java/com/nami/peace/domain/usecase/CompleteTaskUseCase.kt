package com.nami.peace.domain.usecase

import com.nami.peace.domain.model.GrowthStage
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.widget.WidgetUpdateManager
import javax.inject.Inject

/**
 * Use case for completing a task and triggering all related garden updates.
 * 
 * This orchestrates:
 * 1. Marking the reminder as complete
 * 2. Updating the streak counter
 * 3. Advancing the growth stage
 * 4. Checking for milestone achievements
 * 5. Updating widgets
 * 
 * This is the central point for task completion that ensures all garden-related
 * side effects are properly triggered.
 */
class CompleteTaskUseCase @Inject constructor(
    private val reminderRepository: ReminderRepository,
    private val updateStreakUseCase: UpdateStreakUseCase,
    private val advanceGrowthStageUseCase: AdvanceGrowthStageUseCase,
    private val checkMilestoneUseCase: CheckMilestoneUseCase,
    private val trackCompletionEventUseCase: TrackCompletionEventUseCase,
    private val widgetUpdateManager: WidgetUpdateManager
) {
    /**
     * Completes a task and updates all garden-related state.
     * 
     * @param reminderId The ID of the reminder to complete
     * @param completionTime The timestamp of completion (defaults to current time)
     * @return TaskCompletionResult containing information about what changed
     */
    suspend operator fun invoke(
        reminderId: Int,
        completionTime: Long = System.currentTimeMillis()
    ): TaskCompletionResult {
        // 1. Get the reminder details before marking complete (for ML tracking)
        val reminder = reminderRepository.getReminderById(reminderId)
        
        // 2. Mark the reminder as complete
        reminderRepository.setTaskCompleted(reminderId, true)
        
        // 3. Track completion event for ML pattern analysis
        if (reminder != null) {
            trackCompletionEventUseCase(reminder, completionTime)
        }
        
        // 4. Update streak counter
        val (streakIncremented, newStreak) = updateStreakUseCase(completionTime)
        
        // 5. Advance growth stage
        val (stageAdvanced, newStage) = advanceGrowthStageUseCase()
        
        // 6. Check for milestone achievements
        val milestoneReached = if (streakIncremented) {
            checkMilestoneUseCase()
        } else {
            null
        }
        
        // 7. Update widgets to reflect the completion
        widgetUpdateManager.onReminderDataChanged()
        widgetUpdateManager.onGardenStateChanged()
        
        return TaskCompletionResult(
            streakIncremented = streakIncremented,
            newStreak = newStreak,
            stageAdvanced = stageAdvanced,
            newStage = newStage,
            milestoneReached = milestoneReached
        )
    }
}

/**
 * Result of completing a task, containing all garden update information.
 * 
 * @property streakIncremented Whether the streak was incremented (true) or maintained/reset (false)
 * @property newStreak The new streak value after completion
 * @property stageAdvanced Whether the growth stage advanced to a new level
 * @property newStage The new growth stage if advancement occurred, null otherwise
 * @property milestoneReached The milestone value if a milestone was reached (7, 30, 100, 365), null otherwise
 */
data class TaskCompletionResult(
    val streakIncremented: Boolean,
    val newStreak: Int,
    val stageAdvanced: Boolean,
    val newStage: GrowthStage?,
    val milestoneReached: Int?
)
