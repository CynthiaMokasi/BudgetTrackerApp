package com.example.budgettrackerapp.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.budgettrackerapp.R

class ExpenseReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        return try {
            showReminderNotification()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showReminderNotification() {
        val context = applicationContext
        val channelId = "expense_reminders"
        val notificationId = 1001

        val intent = Intent(context, Class.forName("com.example.budgettrackerapp.MainActivity"))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Daily Expense Reminder")
            .setContentText("Have you logged your expenses today?")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = androidx.core.app.NotificationManagerCompat.from(context)
        notificationManager.notify(notificationId, builder.build())
    }
}
