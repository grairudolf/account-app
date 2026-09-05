package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.repositories.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
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
    val weeklyActivity: List<DayActivity> = emptyList(),
    val todayTimeWithGodSeconds: Long = 0L,
    val totalTimeWithGodSeconds: Long = 0L,
    val totalDdewgCount: Int = 0,
    val totalThanksgivingTopics: Int = 0,
    val totalRequestTopics: Int = 0,
    val total15MinRetreats: Int = 0,
    val total15MinMorning: Int = 0,
    val total15MinNoon: Int = 0,
    val total15MinEvening: Int = 0,
    val total15MinNight: Int = 0,
    val totalBertouaPrayers: Int = 0,
    val totalProclamationRepetitions: Int = 0,
    val totalProclamationTopicsCount: Int = 0,
    val totalProclamationMinutes: Long = 0L
)

class StatisticsViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow("LAST_7_DAYS")
    val selectedTimeRange: StateFlow<String> = _selectedTimeRange.asStateFlow()

    val allEntries: StateFlow<List<AccountabilityEntryEntity>> = accountabilityRepository.allEntriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthDaysCompletionFlow: StateFlow<List<DayCompletionInfo>> = combine(
        accountabilityRepository.allEntriesFlow,
        _currentMonth,
        _selectedDate
    ) { entries, month, selDate ->
        val daysInMonth = month.lengthOfMonth()
        val today = LocalDate.now()
        val entriesByDate = entries.groupBy { it.dateIso }

        (1..daysInMonth).map { day ->
            val date = month.atDay(day)
            val dateIso = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val dayEntries = entriesByDate[dateIso] ?: emptyList()
            val uniqueDomains = dayEntries.map { it.domainId }.distinct().size

            DayCompletionInfo(
                dateIso = dateIso,
                entriesCount = dayEntries.size,
                completedDomainsCount = uniqueDomains,
                isToday = date == today,
                isSelected = date == selDate
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDateEntries: StateFlow<List<AccountabilityEntryEntity>> = combine(
        accountabilityRepository.allEntriesFlow,
        _selectedDate
    ) { entries, selDate ->
        val iso = selDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        entries.filter { it.dateIso == iso }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState: StateFlow<OverallStatisticsUiState> = combine(
        accountabilityRepository.allEntriesFlow,
        _selectedTimeRange
    ) { rawEntries, timeRange ->
        val today = LocalDate.now()
        val cutoffDate: LocalDate? = when (timeRange) {
            "LAST_7_DAYS" -> today.minusDays(7)
            "LAST_30_DAYS", "LAST_MONTH" -> today.minusDays(30)
            "LAST_3_MONTHS" -> today.minusMonths(3)
            "LAST_6_MONTHS" -> today.minusMonths(6)
            "LAST_1_YEAR" -> today.minusYears(1)
            else -> null
        }

        val todayIso = today.toString()
        val entries = if (cutoffDate != null) {
            val cutoffIso = cutoffDate.toString()
            rawEntries.filter { it.dateIso in cutoffIso..todayIso }
        } else {
            rawEntries
        }

        val streak = accountabilityRepository.calculateStreakStats(rawEntries) // streak uses raw entries
        val bible = accountabilityRepository.calculateBibleStats(entries)
        val soul = accountabilityRepository.calculateSoulWinningStats(entries)

        val totalPrayerSecs = entries.filter { it.domainId.startsWith("prayer") || it.domainId == "ddewg" }.sumOf { it.durationSeconds }
        val totalFasting = entries.filter { it.domainId == "fasting" }.sumOf { it.fastingDaysCount }

        val todayEntries = rawEntries.filter { it.dateIso == todayIso }

        val communionDomains = listOf("ddewg", "prayer_alone", "prayer_with_others", "bible_reading", "christian_lit", "christian_lit_mem", "bible_mem", "proclamation_importunity", "retreats")
        val todayTimeWithGod = todayEntries.filter { it.domainId in communionDomains }.sumOf { it.durationSeconds }
        val totalTimeWithGod = entries.filter { it.domainId in communionDomains }.sumOf { it.durationSeconds }

        val totalDdewg = entries.count { it.domainId == "ddewg" }
        val prayerAloneEntries = entries.filter { it.domainId == "prayer_alone" }
        val totalThanksgiving = prayerAloneEntries.filter {
            it.prayerType.contains("Thanksgiving", true) || it.notes.contains("Thanksgiving", true) || it.notes.contains("Grâce", true)
        }.sumOf { if (it.prayerTopicsCount > 0) it.prayerTopicsCount else 1 }
        val totalRequests = prayerAloneEntries.filter {
            it.prayerType.contains("Request", true) || it.notes.contains("Request", true) || it.notes.contains("Requête", true)
        }.sumOf { if (it.prayerTopicsCount > 0) it.prayerTopicsCount else 1 }
        val total15Min = prayerAloneEntries.count {
            it.prayerType.contains("15", true) || it.notes.contains("15", true)
        }
        val retreat15Entries = prayerAloneEntries.filter {
            it.prayerType.contains("15", true) || it.notes.contains("15", true)
        }
        val total15MinMorning = retreat15Entries.count { it.retreatPeriodOfDay.equals("Morning", ignoreCase = true) }
        val total15MinNoon = retreat15Entries.count { it.retreatPeriodOfDay.equals("Noon", ignoreCase = true) }
        val total15MinEvening = retreat15Entries.count { it.retreatPeriodOfDay.equals("Evening", ignoreCase = true) }
        val total15MinNight = retreat15Entries.count { it.retreatPeriodOfDay.equals("Night", ignoreCase = true) }

        val totalBertoua = prayerAloneEntries.count {
            it.prayerType.contains("Bertoua", true) || it.notes.contains("Bertoua", true)
        }

        val proclamationEntries = entries.filter { it.domainId == "proclamation_importunity" }
        val totalProclamations = proclamationEntries.sumOf { it.proclamationCount }
        val totalProclamationTopics = proclamationEntries.map { it.proclamationTopic.trim().lowercase() }.filter { it.isNotBlank() }.distinct().size
        val totalProclamationSecs = proclamationEntries.sumOf { it.durationSeconds }

        val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val weekDays = (0..6).map { monday.plusDays(it.toLong()) }
        val entriesByDate = rawEntries.groupBy { it.dateIso }

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
            weeklyActivity = weekly,
            todayTimeWithGodSeconds = todayTimeWithGod,
            totalTimeWithGodSeconds = totalTimeWithGod,
            totalDdewgCount = totalDdewg,
            totalThanksgivingTopics = totalThanksgiving,
            totalRequestTopics = totalRequests,
            total15MinRetreats = total15Min,
            total15MinMorning = total15MinMorning,
            total15MinNoon = total15MinNoon,
            total15MinEvening = total15MinEvening,
            total15MinNight = total15MinNight,
            totalBertouaPrayers = totalBertoua,
            totalProclamationRepetitions = totalProclamations,
            totalProclamationTopicsCount = totalProclamationTopics,
            totalProclamationMinutes = totalProclamationSecs / 60
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStatisticsUiState())

    fun setTimeRange(rangeKey: String) {
        _selectedTimeRange.value = rangeKey
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun setSelectedTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun selectRecentActivity(dateIso: String) {
        try {
            val parsed = LocalDate.parse(dateIso)
            _selectedDate.value = parsed
            _currentMonth.value = YearMonth.from(parsed)
        } catch (_: Exception) {}
        _selectedTab.value = 1 // Switch to History & Calendar tab
    }

    fun nextMonth() {
        _currentMonth.value = _currentMonth.value.plusMonths(1)
    }

    fun previousMonth() {
        _currentMonth.value = _currentMonth.value.minusMonths(1)
    }

    fun goToToday() {
        val today = LocalDate.now()
        _selectedDate.value = today
        _currentMonth.value = YearMonth.from(today)
    }

    fun updateEntry(entry: AccountabilityEntryEntity) {
        viewModelScope.launch {
            accountabilityRepository.saveEntry(entry)
        }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            accountabilityRepository.deleteEntry(entryId)
        }
    }
}


