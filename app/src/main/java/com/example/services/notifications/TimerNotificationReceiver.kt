package com.example.services.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.local.entities.TimerSessionEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.services.timer.TimerServiceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class TimerNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val db = AppDatabase.getInstance(context)
        val timerManager = TimerServiceManager(db.timerSessionDao())
        val repository = AccountabilityRepository(
            db.entryDao(),
            db.goalDao(),
            db.customDomainDao(),
            db.reminderDao(),
            db.reportDao(),
            db.notificationDao(),
            db.proclamationTopicDao()
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val activeSession = timerManager.getActiveSession()
                when (action) {
                    ACTION_TIMER_PAUSE -> {
                        if (activeSession != null) {
                            val updated = timerManager.pauseTimer(activeSession)
                            updateOngoingTimerNotification(context, updated)
                        }
                    }
                    ACTION_TIMER_RESUME -> {
                        if (activeSession != null) {
                            val updated = timerManager.resumeTimer(activeSession)
                            updateOngoingTimerNotification(context, updated)
                        }
                    }
                    ACTION_TIMER_STOP -> {
                        if (activeSession != null) {
                            val durationMs = timerManager.stopTimer(activeSession)
                            val durationSeconds = durationMs / 1000L
                            val endMs = System.currentTimeMillis()
                            val timeFormatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                            val startFormatted = timeFormatter.format(java.util.Date(activeSession.startTimestampMs))
                            val endFormatted = timeFormatter.format(java.util.Date(endMs))

                            val entry = AccountabilityEntryEntity(
                                id = UUID.randomUUID().toString(),
                                userId = activeSession.userId,
                                domainId = activeSession.domainId,
                                dateIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                timestampMs = endMs,
                                timezoneId = activeSession.timezoneId,
                                durationSeconds = durationSeconds,
                                startTimeIso = startFormatted,
                                endTimeIso = endFormatted,
                                notes = activeSession.notes,
                                reflection = activeSession.reflection
                            )
                            repository.saveEntry(entry)
                            cancelTimerNotification(context)
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val TIMER_NOTIFICATION_ID = 99911
        const val TIMER_CHANNEL_ID = "cmfi_timer_channel"
        const val TIMER_CHANNEL_NAME = "CMFI Live Sessions & Timer"

        const val ACTION_TIMER_PAUSE = "com.example.action.TIMER_PAUSE"
        const val ACTION_TIMER_RESUME = "com.example.action.TIMER_RESUME"
        const val ACTION_TIMER_STOP = "com.example.action.TIMER_STOP"

        fun updateOngoingTimerNotification(context: Context, session: TimerSessionEntity?) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (session == null) {
                notificationManager.cancel(TIMER_NOTIFICATION_ID)
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    TIMER_CHANNEL_ID,
                    TIMER_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Ongoing timer for live spiritual discipline sessions"
                    setShowBadge(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Open App Intent
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("NAVIGATE_TO", "timer")
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                0,
                mainIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val domainTitle = when (session.domainId) {
                "prayer_alone" -> "Prayer Alone"
                "prayer_with_others" -> "Prayer with Others"
                "proclamation_importunity" -> "Proclamation & Importunity"
                "ddewg" -> "Daily Dynamic Encounter with God"
                "bible_reading" -> "Bible Reading"
                "fasting" -> "Fasting"
                "soul_winning" -> "Soul Winning"
                "giving" -> "Giving"
                else -> "Live Session"
            }

            // Pause / Resume Intent
            val toggleActionIntent = Intent(context, TimerNotificationReceiver::class.java).apply {
                action = if (session.isPaused) ACTION_TIMER_RESUME else ACTION_TIMER_PAUSE
            }
            val togglePendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                toggleActionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Save & Stop Intent
            val stopActionIntent = Intent(context, TimerNotificationReceiver::class.java).apply {
                action = ACTION_TIMER_STOP
            }
            val stopPendingIntent = PendingIntent.getBroadcast(
                context,
                102,
                stopActionIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val totalDurationMs = calculateCurrentDurationMs(session)
            val totalSecs = totalDurationMs / 1000
            val hours = totalSecs / 3600
            val mins = (totalSecs % 3600) / 60
            val secs = totalSecs % 60
            val formattedTime = if (hours > 0) String.format("%02d:%02d:%02d", hours, mins, secs) else String.format("%02d:%02d", mins, secs)

            val builder = NotificationCompat.Builder(context, TIMER_CHANNEL_ID)
                .setSmallIcon(com.example.R.drawable.ic_cmfi_app_logo)
                .setContentTitle("Live Session Active: $domainTitle")
                .setContentIntent(contentPendingIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)

            try {
                val largeIcon = android.graphics.BitmapFactory.decodeResource(
                    context.resources,
                    com.example.R.mipmap.ic_launcher
                )
                if (largeIcon != null) {
                    builder.setLargeIcon(largeIcon)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (!session.isPaused) {
                val elapsedRealtimeMs = SystemClock.elapsedRealtime()
                val chronometerBase = elapsedRealtimeMs - totalDurationMs
                builder.setUsesChronometer(true)
                    .setWhen(System.currentTimeMillis() - totalDurationMs)
                    .setContentText("Counting in background... • Tap Pause or Save below")
                    .addAction(
                        android.R.drawable.ic_media_pause,
                        "Pause",
                        togglePendingIntent
                    )
            } else {
                builder.setUsesChronometer(false)
                    .setContentText("Paused at $formattedTime • Tap Resume or Save below")
                    .addAction(
                        android.R.drawable.ic_media_play,
                        "Resume",
                        togglePendingIntent
                    )
            }

            builder.addAction(
                android.R.drawable.ic_menu_save,
                "Save & Stop",
                stopPendingIntent
            )

            try {
                notificationManager.notify(TIMER_NOTIFICATION_ID, builder.build())
            } catch (_: Exception) {}
        }

        fun cancelTimerNotification(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(TIMER_NOTIFICATION_ID)
        }

        private fun calculateCurrentDurationMs(session: TimerSessionEntity): Long {
            if (!session.isRunning) return session.accumulatedDurationMs
            return if (session.isPaused) {
                session.accumulatedDurationMs
            } else {
                val currentRealtime = SystemClock.elapsedRealtime()
                val currentSegment = currentRealtime - session.elapsedStartRealtimeMs
                session.accumulatedDurationMs + currentSegment
            }
        }
    }
}
