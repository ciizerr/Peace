package com.nami.peace.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.nami.peace.data.repository.UserPreferencesRepository
import com.nami.peace.domain.repository.ReminderRepository
import com.nami.peace.util.DataBackupHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val reminderRepository: ReminderRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val autoBackupEnabled = userPreferencesRepository.autoBackupEnabled.first()
            if (!autoBackupEnabled) return Result.success()

            val reminders = reminderRepository.getAllRemindersList()
            val history = reminderRepository.getAllHistoryList()
            
            val json = DataBackupHelper.exportToJson(reminders, history)
            
            // Fixed location in internal storage to overwrite current backup
            val folder = File(context.filesDir, "backups")
            if (!folder.exists()) folder.mkdirs()
            
            val backupFile = File(folder, "peace_auto_backup.json")
            FileOutputStream(backupFile).use { it.write(json.toByteArray()) }

            userPreferencesRepository.setLastBackupTime(System.currentTimeMillis())
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "peace_data_backup"

        fun schedule(context: Context, frequency: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresStorageNotLow(true)
                .build()

            val interval = when (frequency) {
                "Daily" -> 24L to TimeUnit.HOURS
                "Weekly" -> 7L to TimeUnit.DAYS
                "Monthly" -> 30L to TimeUnit.DAYS
                else -> 24L to TimeUnit.HOURS
            }

            val request = PeriodicWorkRequestBuilder<BackupWorker>(interval.first, interval.second)
                .setConstraints(constraints)
                .addTag("backup")
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
