package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "proclamation_topics")
data class ProclamationTopicEntity(
    @PrimaryKey val id: String, // UUID
    val userId: String,
    val topic: String, // e.g. "Jesus Christ is Lord", "Healing for the nations"
    val cumulativeCount: Int = 0, // All-time total proclamations made
    val targetCount: Int = 100, // Goal / target repetitions
    val totalDurationSeconds: Long = 0L,
    val lastPracticedIso: String = "", // "YYYY-MM-DD"
    val notes: String = "",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)
