package com.example.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class ReminderNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("EXTRA_TITLE") ?: "CMFI Accap Reminder"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Time for your daily spiritual discipline!"

        showNotification(context, title, message)
    }

    companion object {
        const val CHANNEL_ID = "cmfi_reminders_channel"
        const val CHANNEL_NAME = "CMFI Spiritual Reminders"

        fun showNotification(context: Context, title: String, message: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Reminders for daily dynamic encounters with God and prayer"
                }
                notificationManager.createNotificationChannel(channel)
            }

            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(com.example.R.drawable.app_logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            try {
                val largeIcon = android.graphics.BitmapFactory.decodeResource(
                    context.resources,
                    com.example.R.drawable.app_logo
                )
                if (largeIcon != null) {
                    builder.setLargeIcon(largeIcon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
        }

        const val TIMER_NOTIFICATION_ID = 99911

        fun showOngoingTimerNotification(context: Context, domainTitle: String, formattedTime: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                )
                notificationManager.createNotificationChannel(channel)
            }

            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(com.example.R.drawable.app_logo)
                .setContentTitle("Live Session Active: $domainTitle")
                .setContentText("Elapsed Time: $formattedTime • Session counting in background")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .setContentIntent(pendingIntent)

            try {
                val largeIcon = android.graphics.BitmapFactory.decodeResource(
                    context.resources,
                    com.example.R.drawable.app_logo
                )
                if (largeIcon != null) {
                    builder.setLargeIcon(largeIcon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            notificationManager.notify(TIMER_NOTIFICATION_ID, builder.build())
        }

        fun cancelTimerNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(TIMER_NOTIFICATION_ID)
        }
    }
}
