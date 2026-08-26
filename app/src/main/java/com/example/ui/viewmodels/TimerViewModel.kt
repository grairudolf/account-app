package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.local.entities.TimerSessionEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.services.notifications.TimerNotificationReceiver
import com.example.services.timer.TimerServiceManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class TimerViewModel(
    private val context: Context,
    private val timerServiceManager: TimerServiceManager,
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    val activeSession: StateFlow<TimerSessionEntity?> = timerServiceManager.activeSessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allEntries: StateFlow<List<AccountabilityEntryEntity>> = accountabilityRepository.allEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    init {
        viewModelScope.launch {
            activeSession.collectLatest { session ->
                TimerNotificationReceiver.updateOngoingTimerNotification(context, session)
                if (session != null && session.isRunning && !session.isPaused) {
                    while (coroutineContext.isActive) {
                        checkAndHandleDayCrossing(session)
                        val durationMs = timerServiceManager.calculateCurrentDurationMs(session)
                        _elapsedSeconds.value = durationMs / 1000L
                        delay(1000)
                    }
                } else if (session != null) {
                    val durationMs = timerServiceManager.calculateCurrentDurationMs(session)
                    _elapsedSeconds.value = durationMs / 1000L
                } else {
                    _elapsedSeconds.value = 0L
                }
            }
        }
    }

    private suspend fun checkAndHandleDayCrossing(session: TimerSessionEntity) {
        val nowMs = System.currentTimeMillis()
        val zone = java.time.ZoneId.systemDefault()
        val sessionStartDate = java.time.Instant.ofEpochMilli(session.startTimestampMs).atZone(zone).toLocalDate()
        val currentDate = java.time.Instant.ofEpochMilli(nowMs).atZone(zone).toLocalDate()

        if (currentDate.isAfter(sessionStartDate)) {
            // Timer crossed midnight! Auto-save entry for previous day up to 23:59:59
            val endOfPrevDay = sessionStartDate.atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
            val prevDayDurationMs = (endOfPrevDay - session.startTimestampMs).coerceAtLeast(1000L)
            val prevDayDurationSecs = prevDayDurationMs / 1000L

            val timeFormatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val startFormatted = timeFormatter.format(java.util.Date(session.startTimestampMs))

            val prevDayEntry = AccountabilityEntryEntity(
                id = UUID.randomUUID().toString(),
                userId = session.userId,
                domainId = session.domainId,
                dateIso = sessionStartDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                timestampMs = endOfPrevDay,
                timezoneId = session.timezoneId,
                durationSeconds = prevDayDurationSecs,
                startTimeIso = startFormatted,
                endTimeIso = "23:59",
                notes = "Automated Day-Crossing Split (Part 1)",
                reflection = "Activity continued into the new day"
            )
            accountabilityRepository.saveEntry(prevDayEntry)

            // Reset active session for the new day starting at 00:00:00
            val startOfNewDayMs = currentDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val nowRealtimeMs = android.os.SystemClock.elapsedRealtime()
            val updatedSession = session.copy(
                startTimestampMs = startOfNewDayMs,
                elapsedStartRealtimeMs = nowRealtimeMs,
                accumulatedDurationMs = 0L
            )
            timerServiceManager.updateTimerSession(updatedSession)
        }
    }

    fun startTimer(userId: String, domainId: String) {
        viewModelScope.launch {
            val session = timerServiceManager.startTimer(userId, domainId)
            TimerNotificationReceiver.updateOngoingTimerNotification(context, session)
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            val updated = timerServiceManager.pauseTimer(session)
            TimerNotificationReceiver.updateOngoingTimerNotification(context, updated)
        }
    }

    fun resumeTimer() {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            val updated = timerServiceManager.resumeTimer(session)
            TimerNotificationReceiver.updateOngoingTimerNotification(context, updated)
        }
    }

    fun stopAndSaveTimer(
        entryBuilder: (session: TimerSessionEntity, durationSeconds: Long, startFormatted: String, endFormatted: String, endMs: Long) -> AccountabilityEntryEntity
    ) {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            val durationMs = timerServiceManager.stopTimer(session)
            val durationSeconds = durationMs / 1000L

            val endMs = System.currentTimeMillis()
            val timeFormatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            val startFormatted = timeFormatter.format(java.util.Date(session.startTimestampMs))
            val endFormatted = timeFormatter.format(java.util.Date(endMs))

            val entry = entryBuilder(session, durationSeconds, startFormatted, endFormatted, endMs)

            if (entry.domainId == "proclamation_importunity") {
                accountabilityRepository.recordProclamationSession(entry)
            } else {
                accountabilityRepository.saveEntry(entry)
            }
            TimerNotificationReceiver.cancelTimerNotification(context)
        }
    }

    fun stopAndSaveTimer(notes: String, reflection: String, prayerType: String = "", participantsCount: Int = 1) {
        stopAndSaveTimer { session, durationSeconds, startFormatted, endFormatted, endMs ->
            AccountabilityEntryEntity(
                id = UUID.randomUUID().toString(),
                userId = session.userId,
                domainId = session.domainId,
                dateIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                timestampMs = endMs,
                timezoneId = session.timezoneId,
                durationSeconds = durationSeconds,
                startTimeIso = startFormatted,
                endTimeIso = endFormatted,
                notes = notes,
                reflection = reflection,
                prayerType = prayerType,
                prayerParticipantsCount = participantsCount
            )
        }
    }

    fun saveEntryAndStopTimer(entry: AccountabilityEntryEntity) {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            timerServiceManager.stopTimer(session)
            if (entry.domainId == "proclamation_importunity") {
                accountabilityRepository.recordProclamationSession(entry)
            } else {
                accountabilityRepository.saveEntry(entry)
            }
            TimerNotificationReceiver.cancelTimerNotification(context)
        }
    }

    fun discardTimer() {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            timerServiceManager.discardSession(session)
            TimerNotificationReceiver.cancelTimerNotification(context)
        }
    }
}
