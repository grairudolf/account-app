package com.example.services.timer

import android.os.SystemClock
import com.example.data.local.dao.TimerSessionDao
import com.example.data.local.entities.TimerSessionEntity
import kotlinx.coroutines.flow.Flow
import java.time.ZoneId
import java.util.UUID

class TimerServiceManager(private val timerSessionDao: TimerSessionDao) {

    val activeSessionFlow: Flow<TimerSessionEntity?> = timerSessionDao.getActiveTimerSessionFlow()

    suspend fun getActiveSession(): TimerSessionEntity? {
        return timerSessionDao.getActiveTimerSession()
    }

    suspend fun updateTimerSession(session: TimerSessionEntity) {
        timerSessionDao.insertOrUpdateSession(session)
    }

    suspend fun startTimer(userId: String, domainId: String): TimerSessionEntity {
        // Discard any previous active timer if exists
        val currentActive = timerSessionDao.getActiveTimerSession()
        if (currentActive != null) {
            timerSessionDao.deleteSessionById(currentActive.id)
        }

        val nowMs = System.currentTimeMillis()
        val nowRealtimeMs = SystemClock.elapsedRealtime()
        val session = TimerSessionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            domainId = domainId,
            startTimestampMs = nowMs,
            elapsedStartRealtimeMs = nowRealtimeMs,
            accumulatedDurationMs = 0L,
            pauseTimestampMs = 0L,
            isRunning = true,
            isPaused = false,
            timezoneId = ZoneId.systemDefault().id
        )
        timerSessionDao.insertOrUpdateSession(session)
        return session
    }

    suspend fun pauseTimer(session: TimerSessionEntity): TimerSessionEntity {
        if (!session.isRunning || session.isPaused) return session

        val currentRealtimeMs = SystemClock.elapsedRealtime()
        val activeSegmentDuration = currentRealtimeMs - session.elapsedStartRealtimeMs
        val newAccumulated = session.accumulatedDurationMs + activeSegmentDuration

        val updated = session.copy(
            accumulatedDurationMs = newAccumulated,
            pauseTimestampMs = currentRealtimeMs,
            isPaused = true
        )
        timerSessionDao.insertOrUpdateSession(updated)
        return updated
    }

    suspend fun resumeTimer(session: TimerSessionEntity): TimerSessionEntity {
        if (!session.isRunning || !session.isPaused) return session

        val nowRealtimeMs = SystemClock.elapsedRealtime()
        val updated = session.copy(
            elapsedStartRealtimeMs = nowRealtimeMs,
            pauseTimestampMs = 0L,
            isPaused = false
        )
        timerSessionDao.insertOrUpdateSession(updated)
        return updated
    }

    suspend fun stopTimer(session: TimerSessionEntity): Long {
        val totalDurationMs = calculateCurrentDurationMs(session)
        timerSessionDao.deleteSessionById(session.id)
        return totalDurationMs
    }

    suspend fun discardSession(session: TimerSessionEntity) {
        timerSessionDao.deleteSessionById(session.id)
    }

    fun calculateCurrentDurationMs(session: TimerSessionEntity): Long {
        if (!session.isRunning) return session.accumulatedDurationMs

        return if (session.isPaused) {
            session.accumulatedDurationMs
        } else {
            val currentRealtime = SystemClock.elapsedRealtime()
            val currentSegment = currentRealtime - session.elapsedStartRealtimeMs
            session.accumulatedDurationMs + currentSegment
        }
    }
}
