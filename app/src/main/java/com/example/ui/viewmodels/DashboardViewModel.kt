package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.local.entities.GoalEntity
import com.example.data.local.entities.ReminderEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.data.repositories.DailyProgressStats
import com.example.data.repositories.StreakStats
import com.example.data.repositories.UserRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class DashboardUiState(
    val user: UserEntity? = null,
    val dailyProgress: DailyProgressStats = DailyProgressStats(0, 11, 0),
    val streakStats: StreakStats = StreakStats(0, 0),
    val goalsWithProgress: List<GoalWithProgress> = emptyList(),
    val recentActivities: List<AccountabilityEntryEntity> = emptyList(),
    val allEntries: List<AccountabilityEntryEntity> = emptyList(),
    val upcomingReminders: List<ReminderEntity> = emptyList()
)

data class GoalWithProgress(
    val goal: GoalEntity,
    val currentProgress: Double,
    val progressPercentage: Int
)

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    private val todayIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val uiState: StateFlow<DashboardUiState> = combine(
        userRepository.currentUserFlow,
        accountabilityRepository.allEntriesFlow,
        accountabilityRepository.allGoalsFlow,
        accountabilityRepository.remindersFlow
    ) { user, entries, goals, reminders ->
        val todayEntries = entries.filter { it.dateIso == todayIso }
        val dailyProgress = accountabilityRepository.calculateDailyProgress(todayEntries)
        val streakStats = accountabilityRepository.calculateStreakStats(entries)
        
        val goalsProgress = goals.map { goal ->
            val progress = accountabilityRepository.calculateGoalProgress(goal, entries)
            val pct = ((progress / goal.targetValue) * 100).toInt().coerceIn(0, 100)
            GoalWithProgress(goal, progress, pct)
        }

        DashboardUiState(
            user = user,
            dailyProgress = dailyProgress,
            streakStats = streakStats,
            goalsWithProgress = goalsProgress,
            recentActivities = entries.take(5),
            allEntries = entries,
            upcomingReminders = reminders.filter { it.isEnabled }.take(3)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardUiState()
    )
}
