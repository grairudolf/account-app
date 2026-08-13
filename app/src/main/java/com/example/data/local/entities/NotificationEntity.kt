package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications_log")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: String, // "ENTRY", "TIMER", "REPORT", "GOAL", "REMINDER", "SYSTEM"
    val timestampMs: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
