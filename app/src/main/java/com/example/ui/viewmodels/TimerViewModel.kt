package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.local.entities.TimerSessionEntity
import com.example.data.repositories.AccountabilityRepository
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
    private val timerServiceManager: TimerServiceManager,
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    val activeSession: StateFlow<TimerSessionEntity?> = timerServiceManager.activeSessionFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    init {
        viewModelScope.launch {
            activeSession.collectLatest { session ->
                if (session != null && session.isRunning && !session.isPaused) {
                    while (coroutineContext.isActive) {
                        val durationMs = timerServiceManager.calculateCurrentDurationMs(session)
                        _elapsedSeconds.value = durationMs / 1000L
                        delay(500)
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

    fun startTimer(userId: String, domainId: String) {
        viewModelScope.launch {
            timerServiceManager.startTimer(userId, domainId)
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            timerServiceManager.pauseTimer(session)
        }
    }

    fun resumeTimer() {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            timerServiceManager.resumeTimer(session)
        }
    }

    fun stopAndSaveTimer(notes: String, reflection: String, prayerType: String = "", participantsCount: Int = 1) {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            val durationMs = timerServiceManager.stopTimer(session)
            val durationSeconds = durationMs / 1000L

            val entry = AccountabilityEntryEntity(
                id = UUID.randomUUID().toString(),
                userId = session.userId,
                domainId = session.domainId,
                dateIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                timestampMs = System.currentTimeMillis(),
                timezoneId = session.timezoneId,
                durationSeconds = durationSeconds,
                notes = notes,
                reflection = reflection,
                prayerType = prayerType,
                prayerParticipantsCount = participantsCount
            )

            accountabilityRepository.saveEntry(entry)
        }
    }

    fun discardTimer() {
        viewModelScope.launch {
            val session = activeSession.value ?: return@launch
            timerServiceManager.discardSession(session)
        }
    }
}
