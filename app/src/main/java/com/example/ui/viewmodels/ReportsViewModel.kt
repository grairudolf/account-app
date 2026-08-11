package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ReportRecordEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.data.repositories.UserRepository
import com.example.services.reports.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class ReportsViewModel(
    private val userRepository: UserRepository,
    private val accountabilityRepository: AccountabilityRepository
) : ViewModel() {

    val currentUser: StateFlow<UserEntity?> = userRepository.currentUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val reportHistory: StateFlow<List<ReportRecordEntity>> = accountabilityRepository.reportsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedReportType = MutableStateFlow("DAILY") // DAILY, WEEKLY, MONTHLY, CUSTOM
    val selectedReportType: StateFlow<String> = _selectedReportType.asStateFlow()

    private val _selectedDomains = MutableStateFlow<Set<String>>(
        setOf(
            "ddewg", "bible_reading", "prayer_alone", "prayer_with_others",
            "fasting", "giving", "christian_lit", "soul_winning"
        )
    )
    val selectedDomains: StateFlow<Set<String>> = _selectedDomains.asStateFlow()

    private val _targetDate = MutableStateFlow(LocalDate.now())
    val targetDate: StateFlow<LocalDate> = _targetDate.asStateFlow()

    fun selectReportType(type: String) {
        _selectedReportType.value = type
    }

    fun setTargetDate(date: LocalDate) {
        _targetDate.value = date
    }

    fun toggleDomainFilter(domainId: String) {
        val current = _selectedDomains.value.toMutableSet()
        if (current.contains(domainId)) {
            if (current.size > 1) { // Keep at least one domain
                current.remove(domainId)
            }
        } else {
            current.add(domainId)
        }
        _selectedDomains.value = current
    }

    fun selectAllDomains() {
        _selectedDomains.value = setOf(
            "ddewg", "bible_reading", "prayer_alone", "prayer_with_others",
            "fasting", "giving", "christian_lit", "soul_winning"
        )
    }

    fun generatePdfReport(context: Context, onPdfGenerated: (File) -> Unit) {
        viewModelScope.launch {
            val user = userRepository.getOrCreateGuestUser()
            val entries = accountabilityRepository.allEntriesFlow.first()
            val reportType = _selectedReportType.value
            val activeDomains = _selectedDomains.value
            val refDate = _targetDate.value

            val dateRangeLabel = when (reportType) {
                "DAILY" -> refDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                "WEEKLY" -> "Week of ${refDate.minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE)} to ${refDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                "MONTHLY" -> "${refDate.month} ${refDate.year}"
                else -> refDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
            }

            val filteredByPeriod = when (reportType) {
                "DAILY" -> {
                    val targetIso = refDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    entries.filter { it.dateIso == targetIso }
                }
                "WEEKLY" -> {
                    val startOfWeek = refDate.minusDays(6)
                    entries.filter {
                        try {
                            val d = LocalDate.parse(it.dateIso)
                            !d.isBefore(startOfWeek) && !d.isAfter(refDate)
                        } catch (e: Exception) { false }
                    }
                }
                "MONTHLY" -> {
                    entries.filter {
                        try {
                            val d = LocalDate.parse(it.dateIso)
                            d.month == refDate.month && d.year == refDate.year
                        } catch (e: Exception) { false }
                    }
                }
                else -> entries
            }

            // Filter by checked domains
            val finalEntries = filteredByPeriod.filter { activeDomains.contains(it.domainId) }

            val pdfFile = withContext(Dispatchers.IO) {
                PdfReportGenerator.generatePdfReport(
                    context = context,
                    user = user,
                    reportType = reportType,
                    dateRangeLabel = dateRangeLabel,
                    entries = finalEntries
                )
            }

            val record = ReportRecordEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                reportType = reportType,
                dateRangeLabel = dateRangeLabel,
                selectedDomainsCsv = activeDomains.joinToString(","),
                generatedFilePath = pdfFile.absolutePath
            )
            accountabilityRepository.saveReportRecord(record)

            onPdfGenerated(pdfFile)
        }
    }
}
