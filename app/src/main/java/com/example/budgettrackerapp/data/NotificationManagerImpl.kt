package com.example.budgettrackerapp.data

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.budgettrackerapp.R
import java.util.concurrent.TimeUnit
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager

object NotificationManager {
    private const val CHANNEL_ID = "expense_reminders"
    private const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Expense Reminders"
            val descriptionText = "Notifications for daily expense logging reminders"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(
                android.app.NotificationManager::class.java
            )
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, title: String, message: String) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build())
    }

    fun scheduleExpenseReminder(context: Context, intervalMinutes: Long = 1440) {
        val reminderWork = PeriodicWorkRequestBuilder<ExpenseReminderWorker>(
            intervalMinutes,
            TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "expense_reminder",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            reminderWork
        )
    }

    fun cancelExpenseReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("expense_reminder")
    }
}
