package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.ReportRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY generatedAtMs DESC")
    fun getAllReportsFlow(): Flow<List<ReportRecordEntity>>

    @Query("SELECT * FROM reports")
    suspend fun getAllReportsList(): List<ReportRecordEntity>

    @Query("SELECT * FROM reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: String): ReportRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportRecordEntity)

    @Query("UPDATE reports SET userId = :newUserId")
    suspend fun migrateUserReports(newUserId: String)

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: String)

    @Query("DELETE FROM reports")
    suspend fun clearAllReports()
}
