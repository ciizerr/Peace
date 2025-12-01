package com.nami.peace.widget

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.LocalContext
import com.nami.peace.MainActivity
import com.nami.peace.R
import com.nami.peace.domain.model.PriorityLevel
import com.nami.peace.domain.model.Reminder
import com.nami.peace.domain.model.ReminderCategory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Main content composable for the Today's Reminders widget.
 * Displays all reminders scheduled for the current day.
 * Implements Requirements 17.1, 17.2, 17.3, 17.9
 */
@Composable
fun TodayWidgetContent() {
    val context = LocalContext.current
    val repository = WidgetDataProvider.getReminderRepository(context)
    val reminders by repository.getReminders().collectAsState(initial = emptyList())
    
    val todayReminders = reminders.filter { reminder ->
        isTodayReminder(reminder) && reminder.isEnabled && !reminder.isCompleted
    }.sortedBy { it.startTimeInMillis }
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(16.dp)
            .cornerRadius(16.dp)
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_ionicons_calendar),
                contentDescription = "Calendar",
                modifier = GlanceModifier.size(24.dp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = "Today's Tasks",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
        }
        
        Spacer(modifier = GlanceModifier.height(12.dp))
        
        // Reminders list
        if (todayReminders.isEmpty()) {
            EmptyStateContent()
        } else {
            todayReminders.take(5).forEach { reminder ->
                ReminderWidgetItem(reminder)
                Spacer(modifier = GlanceModifier.height(8.dp))
            }
            
            if (todayReminders.size > 5) {
                Text(
                    text = "+${todayReminders.size - 5} more",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    ),
                    modifier = GlanceModifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Displays a single reminder item in the widget.
 */
@Composable
private fun ReminderWidgetItem(reminder: Reminder) {
    val context = LocalContext.current
    val intent = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_VIEW
        putExtra("reminder_id", reminder.id)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }
    
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(GlanceTheme.colors.surfaceVariant)
            .cornerRadius(8.dp)
            .padding(12.dp)
            .clickable(actionStartActivity(intent)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Priority indicator
        Image(
            provider = ImageProvider(getPriorityIcon(reminder.priority)),
            contentDescription = "Priority",
            modifier = GlanceModifier.size(20.dp)
        )
        
        Spacer(modifier = GlanceModifier.width(8.dp))
        
        // Reminder details
        Column(
            modifier = GlanceModifier.defaultWeight()
        ) {
            Text(
                text = reminder.title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = GlanceTheme.colors.onSurface
                ),
                maxLines = 1
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTime(reminder.startTimeInMillis),
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
                
                if (reminder.isNagModeEnabled) {
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Text(
                        text = "${reminder.currentRepetitionIndex + 1}/${reminder.nagTotalRepetitions}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = GlanceModifier.width(8.dp))
        
        // Category icon
        Image(
            provider = ImageProvider(getCategoryIcon(reminder.category)),
            contentDescription = "Category",
            modifier = GlanceModifier.size(20.dp)
        )
    }
}

/**
 * Displays empty state when no reminders are scheduled for today.
 */
@Composable
private fun EmptyStateContent() {
    Column(
        modifier = GlanceModifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_ionicons_checkmark_circle),
            contentDescription = "No tasks",
            modifier = GlanceModifier.size(48.dp)
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = "No tasks for today",
            style = TextStyle(
                fontSize = 14.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
        Text(
            text = "Enjoy your day!",
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            )
        )
    }
}

/**
 * Checks if a reminder is scheduled for today.
 */
private fun isTodayReminder(reminder: Reminder): Boolean {
    val calendar = Calendar.getInstance()
    val today = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val tomorrow = calendar.apply {
        add(Calendar.DAY_OF_MONTH, 1)
    }.timeInMillis
    
    return reminder.startTimeInMillis in today until tomorrow
}

/**
 * Formats time in HH:mm format.
 */
private fun formatTime(timeInMillis: Long): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date(timeInMillis))
}

/**
 * Gets the appropriate icon for a priority level.
 */
private fun getPriorityIcon(priority: PriorityLevel): Int {
    return when (priority) {
        PriorityLevel.HIGH -> R.drawable.ic_ionicons_alert_circle
        PriorityLevel.MEDIUM -> R.drawable.ic_ionicons_alert
        PriorityLevel.LOW -> R.drawable.ic_ionicons_information_circle
    }
}

/**
 * Gets the appropriate icon for a reminder category.
 */
private fun getCategoryIcon(category: ReminderCategory): Int {
    return when (category) {
        ReminderCategory.WORK -> R.drawable.ic_cat_work
        ReminderCategory.STUDY -> R.drawable.ic_cat_study
        ReminderCategory.HEALTH -> R.drawable.ic_cat_health
        ReminderCategory.HOME -> R.drawable.ic_cat_home
        ReminderCategory.GENERAL -> R.drawable.ic_cat_general
    }
}
