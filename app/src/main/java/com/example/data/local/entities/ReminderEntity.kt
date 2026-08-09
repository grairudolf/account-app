package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val domainId: String,
    val title: String,
    val message: String,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val daysOfWeekCsv: String = "1,2,3,4,5,6,7" // 1 = Monday.. 7 = Sunday
)
