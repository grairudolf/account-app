package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_domains")
data class CustomDomainEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val description: String,
    val iconName: String, // e.g. "school", "fitness_center", "schedule"
    val measurementType: String, // "COUNT", "DURATION", "PERCENTAGE", "BOOLEAN", "TEXT"
    val frequency: String = "DAILY",
    val createdAtMs: Long = System.currentTimeMillis()
)
