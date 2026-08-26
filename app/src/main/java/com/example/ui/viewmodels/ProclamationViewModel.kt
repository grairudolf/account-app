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

    fun selectTopic(topic: ProclamationTopicEntity, resumeCount: Boolean = true) {
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
        _topicText.value = newTopic
        _selectedTopic.value = null
        _isResumedSession.value = false
        _startingCount.value = 0
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
        }
    }

    fun setStartingCount(value: Int) {
        val safeVal = value.coerceAtLeast(0)
        _startingCount.value = safeVal
        _counter.value = safeVal
        _isResumedSession.value = safeVal > 0
    }

    fun setTargetCount(target: Int) {
        _targetCount.value = target.coerceAtLeast(1)
    }

    fun resetCounter() {
        _counter.value = _startingCount.value
    }

    fun clearResumedSession() {
        _isResumedSession.value = false
        _startingCount.value = 0
        _counter.value = 0
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
            val finalTopic = _topicText.value.ifBlank { "Jesus Christ is Lord" }
            val currentTotal = _counter.value
            val starting = _startingCount.value
            val isResumed = _isResumedSession.value

            val addedCount = if (isResumed && currentTotal >= starting) {
                currentTotal - starting
            } else {
                currentTotal
            }

            val duration = _elapsedSeconds.value
            val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

            val sessionNote = if (isResumed && starting > 0) {
                if (_notes.value.isNotBlank()) "${_notes.value} (Continued session from $starting to $currentTotal, +$addedCount new)"
                else "Continued session from $starting to $currentTotal (+$addedCount new)"
            } else {
                _notes.value
            }

            val entry = AccountabilityEntryEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                domainId = "proclamation_importunity",
                dateIso = todayIso,
                timestampMs = System.currentTimeMillis(),
                timezoneId = java.util.TimeZone.getDefault().id,
                durationSeconds = duration,
                proclamationTopic = finalTopic,
                proclamationCount = if (isResumed) addedCount else currentTotal,
                proclamationTarget = _targetCount.value,
                notes = sessionNote,
                reflection = _reflection.value
            )

            accountabilityRepository.recordProclamationSession(entry)

            val notifMessage = if (isResumed && starting > 0) {
                "Proclaimed '$finalTopic': reached $currentTotal total (+$addedCount today, ${duration / 60}m). Victory in Jesus!"
            } else {
                "Proclaimed '$finalTopic' $currentTotal times (${duration / 60}m). Victory in Jesus!"
            }
            accountabilityRepository.logNotification(
                context = context,
                title = "Proclamation & Importunity Logged",
                message = notifMessage
            )

            // Reset session
            _counter.value = 0
            _startingCount.value = 0
            _isResumedSession.value = false
            _elapsedSeconds.value = 0L
            _notes.value = ""
            _reflection.value = ""
            _selectedTopic.value = null

            onSuccess()
        }
    }

    fun deleteTopic(topic: ProclamationTopicEntity) {
        viewModelScope.launch {
            accountabilityRepository.deleteProclamationTopic(topic)
            if (_selectedTopic.value?.id == topic.id) {
                _selectedTopic.value = null
            }
        }
    }

    fun updateTopicCount(topic: ProclamationTopicEntity, newCount: Int) {
        viewModelScope.launch {
            val updated = topic.copy(
                cumulativeCount = newCount.coerceAtLeast(0),
                updatedAtMs = System.currentTimeMillis()
            )
            accountabilityRepository.saveProclamationTopic(updated)
            if (_selectedTopic.value?.id == topic.id) {
                _selectedTopic.value = updated
                _counter.value = updated.cumulativeCount
            }
        }
    }

    fun saveManualSession(
        dateIso: String,
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

            val entry = AccountabilityEntryEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                domainId = "proclamation_importunity",
                dateIso = dateIso.ifBlank { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) },
                timestampMs = System.currentTimeMillis(),
                timezoneId = java.util.TimeZone.getDefault().id,
                durationSeconds = durationSecs,
                proclamationTopic = finalTopic,
                proclamationCount = count.coerceAtLeast(1),
                proclamationTarget = 100,
                notes = if (notes.isNotBlank()) "[Manual Log] $notes" else "[Manual Offline Session]",
                reflection = ""
            )

            val safeCount = count.coerceAtLeast(1)
            accountabilityRepository.recordProclamationSession(entry)

            val existingTopic = topicsFlow.value.find { it.topic.equals(finalTopic, ignoreCase = true) }
            if (existingTopic != null) {
                val updatedTopic = existingTopic.copy(
                    cumulativeCount = existingTopic.cumulativeCount + safeCount,
                    lastPracticedIso = dateIso,
                    updatedAtMs = System.currentTimeMillis()
                )
                accountabilityRepository.saveProclamationTopic(updatedTopic)
            } else {
                val newTopic = ProclamationTopicEntity(
                    id = UUID.randomUUID().toString(),
                    userId = user.id,
                    topic = finalTopic,
                    targetCount = 100,
                    cumulativeCount = safeCount,
                    lastPracticedIso = dateIso,
                    createdAtMs = System.currentTimeMillis(),
                    updatedAtMs = System.currentTimeMillis()
                )
                accountabilityRepository.saveProclamationTopic(newTopic)
            }

            accountabilityRepository.logNotification(
                context = context,
                title = "Manual Proclamation Session Logged",
                message = "Logged $count proclamations for '$finalTopic' on $dateIso ($durationMins min)."
            )
            onSuccess()
        }
    }

    fun savePrayerTopic(topicText: String, targetCount: Int = 100, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val user = userRepository.getOrCreateGuestUser()
            val finalTopic = topicText.trim()
            if (finalTopic.isNotBlank()) {
                val existing = accountabilityRepository.getProclamationTopics(user.id).find { it.topic.equals(finalTopic, ignoreCase = true) }
                if (existing == null) {
                    val newTopic = ProclamationTopicEntity(
                        id = UUID.randomUUID().toString(),
                        userId = user.id,
                        topic = finalTopic,
                        targetCount = targetCount.coerceAtLeast(1),
                        cumulativeCount = 0,
                        lastPracticedIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                        createdAtMs = System.currentTimeMillis(),
                        updatedAtMs = System.currentTimeMillis()
                    )
                    accountabilityRepository.saveProclamationTopic(newTopic)
                    _selectedTopic.value = newTopic
                    _topicText.value = newTopic.topic
                    _targetCount.value = newTopic.targetCount
                } else {
                    _selectedTopic.value = existing
                    _topicText.value = existing.topic
                    _targetCount.value = existing.targetCount
                }
            }
            onSuccess()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
