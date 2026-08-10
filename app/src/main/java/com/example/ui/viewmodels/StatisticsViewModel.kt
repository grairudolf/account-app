package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.*
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class DayActivity(
    val dayLabel: String,
    val dateIso: String,
    val count: Int
)

data class OverallStatisticsUiState(
    val streakStats: StreakStats = StreakStats(0, 0),
    val bibleStats: BibleStats = BibleStats(0, 0f, 0f, 0),
    val soulWinningStats: SoulWinningStats = SoulWinningStats(0, 0, 0, 0),
    val totalPrayerMinutes: Long = 0L,
    val totalFastingDays: Int = 0,
    val totalEntriesCount: Int = 0,
    val weeklyActivity: List<DayActivity> = emptyList()
)

class StatisticsViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    val uiState: StateFlow<OverallStatisticsUiState> = accountabilityRepository.allEntriesFlow.map { entries ->
        val streak = accountabilityRepository.calculateStreakStats(entries)
        val bible = accountabilityRepository.calculateBibleStats(entries)
        val soul = accountabilityRepository.calculateSoulWinningStats(entries)

        val totalPrayerSecs = entries.filter { it.domainId.startsWith("prayer") || it.domainId == "ddewg" }.sumOf { it.durationSeconds }
        val totalFasting = entries.filter { it.domainId == "fasting" }.sumOf { it.fastingDaysCount }

        // Compute actual weekly activity (Monday to Sunday of current week)
        val today = LocalDate.now()
        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
        val entriesByDate = entries.groupBy { it.dateIso }

        val weekly = weekDays.map { date ->
            val dateStr = date.toString()
            val dayCount = entriesByDate[dateStr]?.size ?: 0
            val label = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            DayActivity(
                dayLabel = label,
                dateIso = dateStr,
                count = dayCount
            )
        }

        OverallStatisticsUiState(
            streakStats = streak,
            bibleStats = bible,
            soulWinningStats = soul,
            totalPrayerMinutes = totalPrayerSecs / 60,
            totalFastingDays = totalFasting,
            totalEntriesCount = entries.size,
            weeklyActivity = weekly
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStatisticsUiState())
}

