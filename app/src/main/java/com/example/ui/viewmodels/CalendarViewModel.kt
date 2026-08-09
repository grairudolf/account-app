package com.example.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.repositories.AccountabilityRepository
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class DayCompletionInfo(
    val dateIso: String,
    val entriesCount: Int,
    val completedDomainsCount: Int,
    val isToday: Boolean,
    val isSelected: Boolean
)

class CalendarViewModel(
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

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

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
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
}
