package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.GoalEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.data.local.entities.ReminderEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class GoalsViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    private val _selectedFrequency = MutableStateFlow("ALL") // ALL, DAILY, WEEKLY, MONTHLY, YEARLY
    val selectedFrequency: StateFlow<String> = _selectedFrequency.asStateFlow()

    val allGoalsWithProgressFlow: StateFlow<List<GoalWithProgress>> = combine(
        accountabilityRepository.allGoalsFlow,
        accountabilityRepository.allEntriesFlow
    ) { goals, entries ->
        goals.map { goal ->
            val progress = accountabilityRepository.calculateGoalProgress(goal, entries)
            val pct = if (goal.targetValue > 0) {
                ((progress / goal.targetValue) * 100).toInt().coerceIn(0, 100)
            } else 0
            GoalWithProgress(goal, progress, pct)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goalsWithProgressFlow: StateFlow<List<GoalWithProgress>> = combine(
        allGoalsWithProgressFlow,
        _selectedFrequency
    ) { allWithProgress, freq ->
        if (freq == "ALL") allWithProgress
        else allWithProgress.filter { it.goal.frequency.equals(freq, ignoreCase = true) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val frequencyCountsFlow: StateFlow<Map<String, Int>> = allGoalsWithProgressFlow.map { list ->
        mapOf(
            "ALL" to list.size,
            "DAILY" to list.count { it.goal.frequency.equals("DAILY", ignoreCase = true) },
            "WEEKLY" to list.count { it.goal.frequency.equals("WEEKLY", ignoreCase = true) },
            "MONTHLY" to list.count { it.goal.frequency.equals("MONTHLY", ignoreCase = true) },
            "YEARLY" to list.count { it.goal.frequency.equals("YEARLY", ignoreCase = true) || it.goal.frequency.equals("ANNUAL", ignoreCase = true) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    fun onFrequencySelected(frequency: String) {
        _selectedFrequency.value = frequency
    }

    fun addGoal(
        userId: String,
        domainId: String,
        title: String,
        targetValue: Double,
        unit: String,
        frequency: String,
        startDateIso: String,
        fastingType: String = "COMPLETE",
        periodDays: Int = 0,
        isDailyReminderEnabled: Boolean = false,
        reminderTimeIso: String = "08:00",
        context: android.content.Context? = null
    ) {
        viewModelScope.launch {
            val goalId = UUID.randomUUID().toString()
            val goal = GoalEntity(
                id = goalId,
                userId = userId,
                domainId = domainId,
                title = title,
                targetValue = targetValue,
                unit = unit,
                frequency = frequency,
                startDateIso = startDateIso,
                fastingType = fastingType,
                periodDays = periodDays,
                isDailyReminderEnabled = isDailyReminderEnabled,
                reminderTimeIso = reminderTimeIso
            )
            accountabilityRepository.saveGoal(goal)

            // Ensure the newly added goal is immediately visible to the user
            if (_selectedFrequency.value != "ALL" && !_selectedFrequency.value.equals(frequency, ignoreCase = true)) {
                _selectedFrequency.value = "ALL"
            }

            if (isDailyReminderEnabled) {
                val timeParts = reminderTimeIso.split(":")
                val rHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                val rMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                val reminder = ReminderEntity(
                    id = "reminder_goal_$goalId",
                    userId = userId,
                    title = "Goal Reminder: $title",
                    message = "Time for your goal target ($targetValue $unit)!",
                    hour = rHour,
                    minute = rMinute,
                    isEnabled = true,
                    domainId = domainId
                )
                accountabilityRepository.saveReminder(reminder)
                if (context != null) {
                    com.example.services.notifications.ReminderManager.scheduleReminder(context, reminder)
                }
            }
        }
    }

    fun toggleGoalReminder(goal: GoalEntity, enabled: Boolean, reminderTimeIso: String = "08:00", context: android.content.Context? = null) {
        viewModelScope.launch {
            val updated = goal.copy(isDailyReminderEnabled = enabled, reminderTimeIso = reminderTimeIso, updatedAtMs = System.currentTimeMillis())
            accountabilityRepository.saveGoal(updated)
            val reminderId = "reminder_goal_${goal.id}"
            if (enabled) {
                val timeParts = reminderTimeIso.split(":")
                val rHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
                val rMinute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                val reminder = ReminderEntity(
                    id = reminderId,
                    userId = goal.userId,
                    title = "Goal Reminder: ${goal.title}",
                    message = "Time for your goal target (${goal.targetValue} ${goal.unit})!",
                    hour = rHour,
                    minute = rMinute,
                    isEnabled = true,
                    domainId = goal.domainId
                )
                accountabilityRepository.saveReminder(reminder)
                if (context != null) {
                    com.example.services.notifications.ReminderManager.scheduleReminder(context, reminder)
                }
            } else {
                accountabilityRepository.deleteReminder(reminderId)
                if (context != null) {
                    com.example.services.notifications.ReminderManager.cancelReminder(context, reminderId)
                }
            }
        }
    }

    fun quickIncrementGoal(goal: GoalEntity, amount: Double = 1.0, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val today = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                val entryId = UUID.randomUUID().toString()
                val entry = when (goal.domainId) {
                    "bible_reading" -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        chaptersCount = amount.toInt().coerceAtLeast(1),
                        pagesRead = (amount * 3).toInt(),
                        durationSeconds = (amount * 300).toLong(),
                        notes = "Quick step for goal: ${goal.title}"
                    )
                    "prayer_alone", "prayer_with_others" -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        durationSeconds = if (goal.unit.contains("hour", ignoreCase = true) || goal.unit.contains("heure", ignoreCase = true)) 3600L else if (goal.unit.contains("15")) 900L else 1800L,
                        prayerTopicsCount = 1,
                        notes = "Quick prayer session logged for goal: ${goal.title}"
                    )
                    "ddewg" -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        durationSeconds = 1800L,
                        notes = "Daily encounter with God completed for goal: ${goal.title}"
                    )
                    "fasting" -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        fastingDaysCount = 1,
                        fastingType = if (goal.fastingType.isNotBlank()) goal.fastingType else "COMPLETE",
                        notes = "Fasting day logged for goal: ${goal.title}"
                    )
                    "christian_lit" -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        pagesRead = if (goal.unit.contains("book", ignoreCase = true) || goal.unit.contains("livre", ignoreCase = true)) 50 else amount.toInt().coerceAtLeast(1),
                        durationSeconds = 1800L,
                        notes = "Christian literature reading for goal: ${goal.title}"
                    )
                    "christian_lit_mem" -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        pagesMemorized = amount.toInt().coerceAtLeast(1),
                        litMemPassage = goal.title,
                        notes = "Literature memorization for goal: ${goal.title}"
                    )
                    "bible_mem" -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        bibleMemVerse = "Memory verse for ${goal.title}",
                        notes = "Scripture memorized for goal: ${goal.title}"
                    )
                    "soul_winning" -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        convertedCount = if (goal.unit.contains("convert", ignoreCase = true) || goal.title.contains("convert", ignoreCase = true) || goal.title.contains("won", ignoreCase = true) || goal.title.contains("gagn", ignoreCase = true)) 1 else 0,
                        preachedToCount = 1,
                        notes = "Soul winning logged for goal: ${goal.title}"
                    )
                    "proclamation_importunity" -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        proclamationCount = (if (amount > 1) amount else 50.0).toInt(),
                        proclamationTopic = goal.title,
                        notes = "Proclamations made for goal: ${goal.title}"
                    )
                    else -> com.example.data.local.entities.AccountabilityEntryEntity(
                        id = entryId,
                        userId = goal.userId,
                        domainId = goal.domainId,
                        dateIso = today,
                        timestampMs = System.currentTimeMillis(),
                        timezoneId = java.util.TimeZone.getDefault().id,
                        durationSeconds = 600L,
                        notes = "Spiritual practice logged for goal: ${goal.title}"
                    )
                }
                accountabilityRepository.saveEntry(entry)
                onComplete(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false)
            }
        }
    }

    fun deleteGoal(id: String, context: android.content.Context? = null) {
        viewModelScope.launch {
            accountabilityRepository.deleteGoal(id)
            val reminderId = "reminder_goal_$id"
            accountabilityRepository.deleteReminder(reminderId)
            if (context != null) {
                com.example.services.notifications.ReminderManager.cancelReminder(context, reminderId)
            }
        }
    }
}
