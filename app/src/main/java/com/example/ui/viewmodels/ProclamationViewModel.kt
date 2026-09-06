package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.local.entities.ProclamationTopicEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.data.repositories.UserRepository
import com.example.services.notifications.TimerNotificationReceiver
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class ProclamationViewModel(
    private val context: Context,
    private val userRepository: UserRepository,
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val topicsFlow: StateFlow<List<ProclamationTopicEntity>> = userRepository.currentUserFlow.flatMapLatest { user ->
        accountabilityRepository.getProclamationTopicsFlow(user?.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTopic = MutableStateFlow<ProclamationTopicEntity?>(null)
    val selectedTopic: StateFlow<ProclamationTopicEntity?> = _selectedTopic.asStateFlow()

    private val _topicText = MutableStateFlow("Jesus Christ is Lord")
    val topicText: StateFlow<String> = _topicText.asStateFlow()

    private val _counter = MutableStateFlow(0)
    val counter: StateFlow<Int> = _counter.asStateFlow()

    private val _startingCount = MutableStateFlow(0)
    val startingCount: StateFlow<Int> = _startingCount.asStateFlow()

    private val _isResumedSession = MutableStateFlow(false)
    val isResumedSession: StateFlow<Boolean> = _isResumedSession.asStateFlow()

    private val _targetCount = MutableStateFlow(100)
    val targetCount: StateFlow<Int> = _targetCount.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds.asStateFlow()

    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _reflection = MutableStateFlow("")
    val reflection: StateFlow<String> = _reflection.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val user = userRepository.getOrCreateGuestUser()
            val allTopics = accountabilityRepository.getProclamationTopics(user.id)
            if (allTopics.isNotEmpty() && _selectedTopic.value == null) {
                val latest = allTopics.maxByOrNull { it.updatedAtMs } ?: allTopics.first()
                selectTopic(latest, resumeCount = true)
            }
        }
    }

    val sampleSuggestions = listOf(
        "Jesus Christ is Lord over all nations",
        "All authority in heaven and on earth belongs to Jesus",
        "God will supply all my needs according to His riches in glory",
        "The Lord is my light and my salvation; whom shall I fear?",
        "No weapon formed against me shall prosper",
        "By His stripes we are healed",
        "The Lord will fight for you, and you have only to be silent",
        "The harvest is plentiful, send forth laborers into Your harvest"
    )

    fun selectTopic(topic: ProclamationTopicEntity, resumeCount: Boolean = false) {
        _selectedTopic.value = topic
        _topicText.value = topic.topic
        _targetCount.value = if (topic.targetCount > 0) topic.targetCount else 100
        if (resumeCount && topic.cumulativeCount > 0) {
            _counter.value = topic.cumulativeCount
            _startingCount.value = topic.cumulativeCount
            _isResumedSession.value = true
        } else {
            _counter.value = 0
            _startingCount.value = 0
            _isResumedSession.value = false
        }
    }

    fun resumeTopicSession(topic: ProclamationTopicEntity) {
        selectTopic(topic, resumeCount = true)
    }

    fun startNewSessionForTopic(topic: ProclamationTopicEntity) {
        selectTopic(topic, resumeCount = false)
    }

    fun setTopicText(newTopic: String) {
        val trimmed = newTopic.trim()
        val oldTrimmed = _topicText.value.trim()
        _topicText.value = newTopic
        if (!trimmed.equals(oldTrimmed, ignoreCase = true)) {
            val existing = topicsFlow.value.find { it.topic.equals(trimmed, ignoreCase = true) }
            if (existing != null) {
                _selectedTopic.value = existing
                _targetCount.value = existing.targetCount
            } else {
                _selectedTopic.value = null
                _counter.value = 0
                _startingCount.value = 0
                _isResumedSession.value = false
                _elapsedSeconds.value = 0L
            }
        }
    }

    fun setNotes(newNotes: String) {
        _notes.value = newNotes
    }

    fun setReflection(newReflection: String) {
        _reflection.value = newReflection
    }

    fun incrementCounter(amount: Int = 1) {
        _counter.value = (_counter.value + amount).coerceAtLeast(0)
        // Auto start timer on first increment if not already running
        if (!_isTimerRunning.value && _counter.value > _startingCount.value) {
            startTimer()
        }
    }

    fun decrementCounter() {
        if (_counter.value > 0) {
            _counter.value = _counter.value - 1
        }
    }

    fun setCounterValue(value: Int, asStartingPoint: Boolean = false) {
        val safeVal = value.coerceAtLeast(0)
        _counter.value = safeVal
        if (asStartingPoint) {
            _startingCount.value = safeVal
            _isResumedSession.value = safeVal > 0
            val cur = _selectedTopic.value
            if (cur != null && safeVal > cur.cumulativeCount) {
                viewModelScope.launch {
                    val updated = cur.copy(
                        cumulativeCount = safeVal,
                        updatedAtMs = System.currentTimeMillis()
                    )
                    accountabilityRepository.saveProclamationTopic(updated)
                    _selectedTopic.value = updated
                }
            }
        }
    }

    fun setStartingCount(value: Int) {
        val safeVal = value.coerceAtLeast(0)
        _startingCount.value = safeVal
        _counter.value = safeVal
        _isResumedSession.value = safeVal > 0
        val cur = _selectedTopic.value
        if (cur != null && safeVal > cur.cumulativeCount) {
            viewModelScope.launch {
                val updated = cur.copy(
                    cumulativeCount = safeVal,
                    updatedAtMs = System.currentTimeMillis()
                )
                accountabilityRepository.saveProclamationTopic(updated)
                _selectedTopic.value = updated
            }
        }
    }

    fun setTargetCount(target: Int) {
        _targetCount.value = target.coerceAtLeast(1)
    }

    fun resetCounter(resetTopicAcrossApp: Boolean = false) {
        val currentTopic = _selectedTopic.value
        _counter.value = 0
        _startingCount.value = 0
        _isResumedSession.value = false
        _elapsedSeconds.value = 0L
        pauseTimer()

        if (resetTopicAcrossApp && currentTopic != null) {
            viewModelScope.launch {
                val updated = currentTopic.copy(
                    cumulativeCount = 0,
                    updatedAtMs = System.currentTimeMillis()
                )
                accountabilityRepository.saveProclamationTopic(updated)
                _selectedTopic.value = updated
            }
        }
    }

    fun clearResumedSession() {
        _isResumedSession.value = false
        _startingCount.value = 0
        _counter.value = 0
        _elapsedSeconds.value = 0L
        pauseTimer()
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _isTimerRunning.value) {
                delay(1000)
                _elapsedSeconds.value += 1
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _elapsedSeconds.value = 0L
    }

    fun saveSession(onSuccess: () -> Unit = {}) {
        pauseTimer()
        viewModelScope.launch {
            val user = userRepository.getOrCreateGuestUser()
            val finalTopic = _topicText.value.trim().ifBlank { "Jesus Christ is Lord" }
            val currentTotal = _counter.value
            val starting = _startingCount.value
            val isResumed = _isResumedSession.value

            val existing = _selectedTopic.value 
                ?: accountabilityRepository.getProclamationTopics(user.id).find { it.topic.trim().equals(finalTopic, ignoreCase = true) }

            val previousCumulative = existing?.cumulativeCount ?: 0

            // Repetitions proclaimed in this active session
            val sessionDelta = if (isResumed && currentTotal >= starting && starting > 0) {
                currentTotal - starting
            } else {
                currentTotal
            }

            // Offset if user stepped up their starting point beyond previous cumulative
            val offlineDelta = if (starting > previousCumulative) (starting - previousCumulative) else 0
            val effectiveProclamations = (sessionDelta + offlineDelta).coerceAtLeast(1)

            // Guaranteed new cumulative count: MUST be at least currentTotal
            val newCumulativeCount = maxOf(previousCumulative + sessionDelta + offlineDelta, currentTotal, previousCumulative + effectiveProclamations)

            val duration = _elapsedSeconds.value
            val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val sessionNote = if (isResumed && starting > 0) {
                if (_notes.value.isNotBlank()) "${_notes.value} (Continued session from $starting to $currentTotal, +$sessionDelta new)"
                else "Continued session from $starting to $currentTotal (+$sessionDelta new)"
            } else {
                _notes.value
            }

            // 1. Explicitly persist the topic with its accurate new cumulative count
            val topicToSave = if (existing != null) {
                existing.copy(
                    topic = finalTopic,
                    cumulativeCount = newCumulativeCount,
                    targetCount = if (_targetCount.value > 0) _targetCount.value else existing.targetCount,
                    totalDurationSeconds = existing.totalDurationSeconds + duration,
                    lastPracticedIso = todayIso,
                    updatedAtMs = System.currentTimeMillis()
                )
            } else {
                ProclamationTopicEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    topic = finalTopic,
                    cumulativeCount = newCumulativeCount,
                    targetCount = if (_targetCount.value > 0) _targetCount.value else 100,
                    totalDurationSeconds = duration,
                    lastPracticedIso = todayIso,
                    createdAtMs = System.currentTimeMillis(),
                    updatedAtMs = System.currentTimeMillis()
                )
            }
            accountabilityRepository.saveProclamationTopic(topicToSave)

            // 2. Create entry for accountability logs/reports
            val entry = AccountabilityEntryEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                domainId = "proclamation_importunity",
                dateIso = todayIso,
                timestampMs = System.currentTimeMillis(),
                timezoneId = java.util.TimeZone.getDefault().id,
                durationSeconds = duration,
                proclamationTopic = finalTopic,
                proclamationCount = effectiveProclamations,
                proclamationTarget = if (_targetCount.value > 0) _targetCount.value else 100,
                notes = sessionNote,
                reflection = _reflection.value
            )
            accountabilityRepository.saveEntry(entry)

            val notifMessage = "Proclaimed '$finalTopic': reached $newCumulativeCount total (+$effectiveProclamations, ${duration / 60}m). Victory in Jesus!"
            accountabilityRepository.logNotification(
                context = context,
                title = "Proclamation & Importunity Logged",
                message = notifMessage
            )

            // Retain updated topic state so returning or staying on screen preserves the new cumulative count
            _selectedTopic.value = topicToSave
            _counter.value = newCumulativeCount
            _startingCount.value = newCumulativeCount
            _isResumedSession.value = true
            _elapsedSeconds.value = 0L
            _notes.value = ""
            _reflection.value = ""

            onSuccess()
        }
    }

    fun editTopic(topic: ProclamationTopicEntity, newTopicTitle: String, newTargetCount: Int, newCumulativeCount: Int) {
        viewModelScope.launch {
            val trimmed = newTopicTitle.trim().ifBlank { topic.topic }
            val updated = topic.copy(
                topic = trimmed,
                targetCount = newTargetCount.coerceAtLeast(1),
                cumulativeCount = newCumulativeCount.coerceAtLeast(0),
                updatedAtMs = System.currentTimeMillis()
            )
            accountabilityRepository.saveProclamationTopic(updated)
            if (_selectedTopic.value?.id == topic.id) {
                _selectedTopic.value = updated
                _topicText.value = updated.topic
                _targetCount.value = updated.targetCount
                _counter.value = updated.cumulativeCount
                _startingCount.value = updated.cumulativeCount
                _isResumedSession.value = updated.cumulativeCount > 0
            }
        }
    }

    fun deleteTopic(topic: ProclamationTopicEntity) {
        viewModelScope.launch {
            accountabilityRepository.deleteProclamationTopic(topic)
            if (_selectedTopic.value?.id == topic.id) {
                val remaining = topicsFlow.value.filter { it.id != topic.id }
                if (remaining.isNotEmpty()) {
                    selectTopic(remaining.first(), resumeCount = true)
                } else {
                    _selectedTopic.value = null
                    _topicText.value = "Jesus Christ is Lord"
                    _counter.value = 0
                    _startingCount.value = 0
                    _isResumedSession.value = false
                    _elapsedSeconds.value = 0L
                }
            }
        }
    }

    fun updateTopicCount(topic: ProclamationTopicEntity, newCount: Int) {
        viewModelScope.launch {
            val safeCount = newCount.coerceAtLeast(0)
            val updated = topic.copy(
                cumulativeCount = safeCount,
                updatedAtMs = System.currentTimeMillis()
            )
            accountabilityRepository.saveProclamationTopic(updated)
            if (_selectedTopic.value?.id == topic.id) {
                _selectedTopic.value = updated
                _counter.value = safeCount
                _startingCount.value = safeCount
            }
        }
    }

    fun savePrayerTopic(topicText: String, targetCount: Int = 100, currentCount: Int = 0, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val user = userRepository.getOrCreateGuestUser()
            val finalTopic = topicText.trim().ifBlank { "Jesus Christ is Lord" }
            val existing = accountabilityRepository.getProclamationTopics(user.id).find { it.topic.equals(finalTopic, ignoreCase = true) }
            val safeCount = currentCount.coerceAtLeast(0)
            val updatedTopic = if (existing != null) {
                existing.copy(
                    cumulativeCount = if (safeCount > 0) safeCount else existing.cumulativeCount,
                    targetCount = targetCount.coerceAtLeast(1),
                    lastPracticedIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    updatedAtMs = System.currentTimeMillis()
                )
            } else {
                ProclamationTopicEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    topic = finalTopic,
                    targetCount = targetCount.coerceAtLeast(1),
                    cumulativeCount = safeCount,
                    lastPracticedIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                    createdAtMs = System.currentTimeMillis(),
                    updatedAtMs = System.currentTimeMillis()
                )
            }
            accountabilityRepository.saveProclamationTopic(updatedTopic)
            _selectedTopic.value = updatedTopic
            _topicText.value = updatedTopic.topic
            _targetCount.value = updatedTopic.targetCount
            if (safeCount > 0) {
                _counter.value = safeCount
                _startingCount.value = safeCount
                _isResumedSession.value = true
            }
            onSuccess()
        }
    }

    fun saveManualSession(
        dateIso: String,
        startTime: String = "",
        stopTime: String = "",
        topic: String,
        count: Int,
        durationMins: Long,
        notes: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val user = userRepository.getOrCreateGuestUser()
            val finalTopic = topic.ifBlank { "Jesus Christ is Lord" }
            val durationSecs = durationMins * 60
            val safeCount = count.coerceAtLeast(1)

            val entry = AccountabilityEntryEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                domainId = "proclamation_importunity",
                dateIso = dateIso.ifBlank { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) },
                timestampMs = System.currentTimeMillis(),
                timezoneId = java.util.TimeZone.getDefault().id,
                durationSeconds = durationSecs,
                startTimeIso = startTime,
                endTimeIso = stopTime,
                proclamationTopic = finalTopic,
                proclamationCount = safeCount,
                proclamationTarget = 100,
                notes = if (notes.isNotBlank()) "[Manual Log] $notes" else "[Manual Offline Session]",
                reflection = ""
            )
            // saveEntry saves to entries and updates topic cumulativeCount automatically
            accountabilityRepository.saveEntry(entry)

            val timeRangeNotice = if (startTime.isNotBlank() && stopTime.isNotBlank()) " ($startTime - $stopTime, $durationMins min)" else " ($durationMins min)"
            accountabilityRepository.logNotification(
                context = context,
                title = "Manual Proclamation Session Logged",
                message = "Logged $count proclamations for '$finalTopic' on $dateIso$timeRangeNotice."
            )
            onSuccess()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
