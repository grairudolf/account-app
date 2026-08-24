package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val domainId: String,
    val title: String,
    val targetValue: Double,
    val unit: String, // "chapters", "minutes", "pages", "people", "days", "verses"
    val frequency: String, // "DAILY", "WEEKLY", "MONTHLY"
    val startDateIso: String,
    val endDateIso: String = "",
    val isPaused: Boolean = false,
    val fastingType: String = "COMPLETE", // "COMPLETE", "PARTIAL", "WATER_ONLY"
    val periodDays: Int = 0, // e.g., 3 days fast in a 30-day period
    val isDailyReminderEnabled: Boolean = false,
    val reminderTimeIso: String = "08:00",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)
