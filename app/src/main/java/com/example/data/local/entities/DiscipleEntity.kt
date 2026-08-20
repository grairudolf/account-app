package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "disciples")
data class DiscipleEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "default_user",
    val name: String,
    val phone: String = "",
    val status: String = "Growing Disciple",
    val conversionDateIso: String = "",
    val prayerTopics: String = "",
    val notes: String = "",
    val topicsCovered: String = "",
    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis()
)
