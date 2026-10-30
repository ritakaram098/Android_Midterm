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
        val taskTitle = inputData.getString("taskTitle") ?: "Care Task"
        val petName = inputData.getString("petName") ?: "Your Pet"

        showNotification(taskTitle, petName)
        return Result.success()
    }

    private fun showNotification(taskTitle: String, petName: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pet_care_reminders"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pet Care Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for pet care tasks"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Pet Care Reminder: $taskTitle")
            .setContentText("It's time for $petName's $taskTitle!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
