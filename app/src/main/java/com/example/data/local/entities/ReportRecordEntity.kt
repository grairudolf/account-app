package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportRecordEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val reportType: String, // "DAILY", "WEEKLY", "MONTHLY"
    val dateRangeLabel: String,
    val selectedDomainsCsv: String,
    val generatedFilePath: String = "",
    val generatedAtMs: Long = System.currentTimeMillis()
)
