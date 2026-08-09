package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repositories.*
import kotlinx.coroutines.flow.*

data class OverallStatisticsUiState(
    val streakStats: StreakStats = StreakStats(0, 0),
    val bibleStats: BibleStats = BibleStats(0, 0f, 0f, 0),
    val soulWinningStats: SoulWinningStats = SoulWinningStats(0, 0, 0, 0),
    val totalPrayerMinutes: Long = 0L,
    val totalFastingDays: Int = 0,
    val totalEntriesCount: Int = 0
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

        OverallStatisticsUiState(
            streakStats = streak,
            bibleStats = bible,
            soulWinningStats = soul,
            totalPrayerMinutes = totalPrayerSecs / 60,
            totalFastingDays = totalFasting,
            totalEntriesCount = entries.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStatisticsUiState())
}
