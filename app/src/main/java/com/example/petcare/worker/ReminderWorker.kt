package com.example.petcare.worker

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong("taskId", 0L)
        val taskTitle = inputData.getString("taskTitle") ?: "Care Task"
        val petName = inputData.getString("petName") ?: "Your Pet"
        val category = inputData.getString("category") ?: "Care"
        val description = inputData.getString("description") ?: ""

        showNotification(taskId, taskTitle, petName, category, description)
        return Result.success()
    }

    private fun showNotification(
        taskId: Long,
        taskTitle: String,
        petName: String,
        category: String,
        description: String
    ) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pet_care_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pet Care Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                this.description = "Notifications and reminders for pet care tasks"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentText = if (description.isNotBlank()) {
            "It's time for $petName's $taskTitle ($category)!\nNotes: $description"
        } else {
            "It's time for $petName's $taskTitle ($category)!"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🐾 Pet Care Reminder: $taskTitle")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationId = if (taskId > 0L) taskId.toInt() else System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)
    }
}
