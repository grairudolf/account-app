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

    private val _selectedFrequency = MutableStateFlow("ALL") // ALL, DAILY, WEEKLY, MONTHLY
    val selectedFrequency: StateFlow<String> = _selectedFrequency.asStateFlow()

    val goalsWithProgressFlow: StateFlow<List<GoalWithProgress>> = combine(
        accountabilityRepository.allGoalsFlow,
        accountabilityRepository.allEntriesFlow,
        _selectedFrequency
    ) { goals, entries, freq ->
        val filteredGoals = if (freq == "ALL") goals else goals.filter { it.frequency == freq }
        filteredGoals.map { goal ->
            val progress = accountabilityRepository.calculateGoalProgress(goal, entries)
            val pct = if (goal.targetValue > 0) {
                ((progress / goal.targetValue) * 100).toInt().coerceIn(0, 100)
            } else 0
            GoalWithProgress(goal, progress, pct)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
