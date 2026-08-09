package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entities.TimerSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSessionDao {
    @Query("SELECT * FROM timer_sessions WHERE isRunning = 1 LIMIT 1")
    fun getActiveTimerSessionFlow(): Flow<TimerSessionEntity?>

    @Query("SELECT * FROM timer_sessions WHERE isRunning = 1 LIMIT 1")
    suspend fun getActiveTimerSession(): TimerSessionEntity?

    @Query("SELECT * FROM timer_sessions WHERE domainId = :domainId AND isRunning = 1 LIMIT 1")
    suspend fun getActiveSessionByDomain(domainId: String): TimerSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSession(session: TimerSessionEntity)

    @Query("DELETE FROM timer_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    @Query("DELETE FROM timer_sessions")
    suspend fun clearAllTimerSessions()
}
