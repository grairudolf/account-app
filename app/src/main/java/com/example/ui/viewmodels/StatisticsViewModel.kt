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
    val totalBertouaPrayers: Int = 0
)

class StatisticsViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _selectedTab = MutableStateFlow(1)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

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

    val uiState: StateFlow<OverallStatisticsUiState> = accountabilityRepository.allEntriesFlow.map { entries ->
        val streak = accountabilityRepository.calculateStreakStats(entries)
        val bible = accountabilityRepository.calculateBibleStats(entries)
        val soul = accountabilityRepository.calculateSoulWinningStats(entries)

        val totalPrayerSecs = entries.filter { it.domainId.startsWith("prayer") || it.domainId == "ddewg" }.sumOf { it.durationSeconds }
        val totalFasting = entries.filter { it.domainId == "fasting" }.sumOf { it.fastingDaysCount }

        val today = LocalDate.now()
        val todayIso = today.toString()
        val todayEntries = entries.filter { it.dateIso == todayIso }

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
        val totalBertoua = prayerAloneEntries.count {
            it.prayerType.contains("Bertoua", true) || it.notes.contains("Bertoua", true)
        }

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
            weeklyActivity = weekly,
            todayTimeWithGodSeconds = todayTimeWithGod,
            totalTimeWithGodSeconds = totalTimeWithGod,
            totalDdewgCount = totalDdewg,
            totalThanksgivingTopics = totalThanksgiving,
            totalRequestTopics = totalRequests,
            total15MinRetreats = total15Min,
            totalBertouaPrayers = totalBertoua
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), OverallStatisticsUiState())

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


