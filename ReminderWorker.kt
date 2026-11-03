
package com.example.petcare.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.petcare.R
import com.example.petcare.data.AppDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("taskId", 0L)
        if (taskId <= 0L) return Result.success()

        val dao = AppDatabase
            .getDatabase(applicationContext)
            .careTaskDao()

        val task = dao.getTaskById(taskId)
            ?: return Result.success()

        val dueAt = inputData.getLong("dueAt", 0L)
        if (dueAt <= 0L) return Result.success()

        val currentDueAt =
            parseReminderDate(task.nextReminder)
                ?: return Result.success()

        // Ignore old reminder requests.
        if (currentDueAt != dueAt) {
            return Result.success()
        }

        if (System.currentTimeMillis() < dueAt) {
            return Result.retry()
        }

        val recurring = task.frequency in listOf(
            "Daily", "Weekly", "Monthly"
        )

        // Completed one-time tasks do not send reminders.
        if (!recurring && task.isCompleted) {
            return Result.success()
        }

        // Make a recurring task available again.
        if (recurring && task.isCompleted) {
            dao.updateTask(
                task.copy(isCompleted = false)
            )
        }

        val petName =
            dao.getPetById(task.petId)?.name ?: "Your Pet"

        showNotification(
            taskTitle = task.title,
            petName = petName,
            taskId = task.id
        )

        // Automatically schedule the next occurrence.
        if (recurring) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = dueAt
            }

            val now = System.currentTimeMillis()

            do {
                when (task.frequency) {
                    "Daily" ->
                        calendar.add(Calendar.DAY_OF_YEAR, 1)

                    "Weekly" ->
                        calendar.add(Calendar.WEEK_OF_YEAR, 1)

                    "Monthly" ->
                        calendar.add(Calendar.MONTH, 1)
                }
            } while (calendar.timeInMillis <= now)

            val nextMillis = calendar.timeInMillis

            val nextDate = SimpleDateFormat(
                "yyyy-MM-dd h:mm a",
                Locale.ENGLISH
            ).format(calendar.time)

            dao.updateTask(
                task.copy(
                    nextReminder = nextDate,
                    isCompleted = false
                )
            )

            val nextWork =
                OneTimeWorkRequestBuilder<ReminderWorker>()
                    .setInputData(
                        Data.Builder()
                            .putLong("taskId", task.id)
                            .putLong("dueAt", nextMillis)
                            .build()
                    )
                    .setInitialDelay(
                        (nextMillis - System.currentTimeMillis())
                            .coerceAtLeast(0L),
                        TimeUnit.MILLISECONDS
                    )
                    .build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    "care_reminder_${task.id}",
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    nextWork
                )
        }

        return Result.success()
    }

    private fun parseReminderDate(value: String): Long? {
        val formats = listOf(
            "yyyy-MM-dd h:mm a",
            "yyyy-MM-dd hh:mm a",
            "yyyy-MM-dd HH:mm"
        )

        for (pattern in formats) {
            try {
                val formatter = SimpleDateFormat(
                    pattern,
                    Locale.ENGLISH
                ).apply {
                    isLenient = false
                }

                val date = formatter.parse(value.trim())
                if (date != null) return date.time
            } catch (_: Exception) {
                // Try another date format.
            }
        }

        return null
    }

    private fun showNotification(
        taskTitle: String,
        petName: String,
        taskId: Long
    ) {
        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {
            val permission =
                ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                )

            if (permission !=
                PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val manager =
            applicationContext.getSystemService(
                Context.NOTIFICATION_SERVICE
            ) as NotificationManager

        val channelId = "pet_care_reminders"

        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {
            manager.createNotificationChannel(
                NotificationChannel(
                    channelId,
                    "Pet Care Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                )
            )
        }

        val notification =
            NotificationCompat.Builder(
                applicationContext,
                channelId
            )
                .setSmallIcon(
                    R.drawable.ic_launcher_foreground
                )
                .setContentTitle(
                    "Pet Care Reminder: $taskTitle"
                )
                .setContentText(
                    "It's time for $petName's $taskTitle!"
                )
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)
                .build()

        val notificationId =
            (taskId xor (taskId ushr 32)).toInt()

        manager.notify(notificationId, notification)
    }
}
