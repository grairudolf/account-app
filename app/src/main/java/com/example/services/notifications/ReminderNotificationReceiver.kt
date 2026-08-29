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
import kotlinx.coroutines.launch

class ReminderNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || 
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED") {
            // Restore alarms on reboot
            val pendingResult = goAsync()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val db = com.example.data.local.AppDatabase.getInstance(context)
                    val reminders = db.reminderDao().getAllRemindersList()
                    for (reminder in reminders) {
                        if (reminder.isEnabled) {
                            ReminderManager.scheduleReminder(context, reminder)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
            return
        }

        val title = intent.getStringExtra("EXTRA_TITLE") ?: "CMFI Accap Reminder"
        val message = intent.getStringExtra("EXTRA_MESSAGE") ?: "Time for your daily spiritual discipline!"
        val reminderId = intent.getStringExtra("EXTRA_REMINDER_ID")
        val hour = intent.getIntExtra("EXTRA_HOUR", -1)
        val minute = intent.getIntExtra("EXTRA_MINUTE", -1)

        showNotification(context, title, message)

        // Reschedule for next day if this is a recurring reminder
        if (!reminderId.isNullOrBlank() && hour >= 0 && minute >= 0) {
            val pendingResult = goAsync()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val db = com.example.data.local.AppDatabase.getInstance(context)
                    val reminder = db.reminderDao().getReminderById(reminderId)
                    if (reminder != null && reminder.isEnabled) {
                        // Reschedule without showing confirmation notification
                        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
                        val nextIntent = Intent(context, ReminderNotificationReceiver::class.java).apply {
                            this.action = "com.example.action.SPIRITUAL_REMINDER"
                            putExtra("EXTRA_REMINDER_ID", reminder.id)
                            putExtra("EXTRA_TITLE", reminder.title)
                            putExtra("EXTRA_MESSAGE", reminder.message)
                            putExtra("EXTRA_HOUR", reminder.hour)
                            putExtra("EXTRA_MINUTE", reminder.minute)
                        }
                        val pi = PendingIntent.getBroadcast(
                            context,
                            reminder.id.hashCode(),
                            nextIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        val nextCal = java.util.Calendar.getInstance().apply {
                            timeInMillis = System.currentTimeMillis()
                            set(java.util.Calendar.HOUR_OF_DAY, reminder.hour)
                            set(java.util.Calendar.MINUTE, reminder.minute)
                            set(java.util.Calendar.SECOND, 0)
                            set(java.util.Calendar.MILLISECOND, 0)
                            add(java.util.Calendar.DAY_OF_YEAR, 1)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (alarmManager.canScheduleExactAlarms()) {
                                alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, nextCal.timeInMillis, pi)
                            } else {
                                alarmManager.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, nextCal.timeInMillis, pi)
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            alarmManager.setExactAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP, nextCal.timeInMillis, pi)
                        } else {
                            alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, nextCal.timeInMillis, pi)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "cmfi_reminders_channel"
        const val CHANNEL_NAME = "CMFI Spiritual Reminders"

        private fun isFrenchDevice(context: Context): Boolean {
            val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales[0]
            } else {
                @Suppress("DEPRECATION")
                context.resources.configuration.locale
            }
            return locale?.language?.lowercase()?.startsWith("fr") == true
        }

        private fun localizeTitle(title: String, isFrench: Boolean): String {
            if (!isFrench) return title
            return when {
                title == "CMFI Accap Reminder" -> "Rappel Cahier CMFI"
                title.startsWith("Reminder Set:") -> title.replace("Reminder Set:", "Rappel Programmé :")
                title.startsWith("Goal Reminder:") -> title.replace("Goal Reminder:", "Rappel d'Objectif :")
                title.startsWith("Live Session Active:") -> title.replace("Live Session Active:", "Session En Cours :")
                else -> title
            }
        }

        private fun localizeMessage(message: String, isFrench: Boolean): String {
            if (!isFrench) return message
            var text = message
            if (text == "Time for your daily spiritual discipline!") {
                return "C'est l'heure de votre discipline spirituelle quotidienne !"
            }
            if (text.startsWith("Daily reminder scheduled for")) {
                text = text.replace("Daily reminder scheduled for", "Rappel quotidien programmé pour")
            }
            if (text.startsWith("Time for your goal target")) {
                text = text.replace("Time for your goal target", "C'est l'heure d'atteindre votre objectif")
            }
            if (text.contains("Session counting in background")) {
                text = text.replace("Elapsed Time:", "Temps Écoulé :")
                    .replace("Session counting in background", "Chronométrage en arrière-plan")
            }
            text = text.replace("Daily Dynamic Encounter With God", "Rencontre Dynamique Quotidienne avec Dieu")
                .replace("Bible Reading", "Lecture Biblique")
                .replace("Prayer Alone", "Prière Seul")
                .replace("Prayer with Others", "Prière avec d'Autres")
                .replace("Proclamation & Importunity", "Proclamation & Importunité")
                .replace("Fasting", "Jeûne")
                .replace("Giving to God", "Offrandes et Libéralités")
                .replace("Christian Literature Reading", "Littérature Chrétienne")
                .replace("Soul Winning", "Gagnagisme d'Âmes")
                .replace("Spiritual Retreats", "Retraites Spirituelles")
            return text
        }

        fun showNotification(context: Context, title: String, message: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val isFrench = isFrenchDevice(context)
            val localizedTitle = localizeTitle(title, isFrench)
            val localizedMessage = localizeMessage(message, isFrench)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    if (isFrench) "Rappels Spirituels CMFI" else CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = if (isFrench) "Rappels pour la rencontre dynamique quotidienne avec Dieu et les prières" else "Reminders for daily dynamic encounters with God and prayer"
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
                .setContentTitle(localizedTitle)
                .setContentText(localizedMessage)
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

            val isFrench = isFrenchDevice(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    if (isFrench) "Rappels Spirituels CMFI" else CHANNEL_NAME,
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

            val rawTitle = "Live Session Active: $domainTitle"
            val rawMsg = "Elapsed Time: $formattedTime • Session counting in background"

            val localizedTitle = localizeTitle(rawTitle, isFrench)
            val localizedMsg = localizeMessage(rawMsg, isFrench)

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(com.example.R.drawable.app_logo)
                .setContentTitle(localizedTitle)
                .setContentText(localizedMsg)
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
