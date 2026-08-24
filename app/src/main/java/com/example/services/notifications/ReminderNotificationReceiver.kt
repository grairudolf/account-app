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
