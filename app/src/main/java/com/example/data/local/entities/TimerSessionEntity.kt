package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "timer_sessions")
data class TimerSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val domainId: String, // "ddewg", "prayer_alone", "prayer_with_others"
    val startTimestampMs: Long,
    val elapsedStartRealtimeMs: Long,
    val accumulatedDurationMs: Long = 0L,
    val pauseTimestampMs: Long = 0L,
    val isRunning: Boolean = true,
    val isPaused: Boolean = false,
    val timezoneId: String,
    val notes: String = "",
    val reflection: String = "",
    val createdAtMs: Long = System.currentTimeMillis()
)
