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

    companion object {
        val ALL_SPIRITUAL_DOMAINS = setOf(
            "ddewg",
            "bible_reading",
            "prayer_alone",
            "prayer_with_others",
            "proclamation_importunity",
            "retreats",
            "fasting",
            "giving",
            "christian_lit",
            "christian_lit_mem",
            "bible_mem",
            "soul_winning",
            "making_disciples"
        )
    }

    private val _selectedDomains = MutableStateFlow<Set<String>>(ALL_SPIRITUAL_DOMAINS)
    val selectedDomains: StateFlow<Set<String>> = _selectedDomains.asStateFlow()

    private val _targetDate = MutableStateFlow(LocalDate.now())
    val targetDate: StateFlow<LocalDate> = _targetDate.asStateFlow()

    private val _startDate = MutableStateFlow(LocalDate.now().minusDays(7))
    val startDate: StateFlow<LocalDate> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(LocalDate.now())
    val endDate: StateFlow<LocalDate> = _endDate.asStateFlow()

    fun selectReportType(type: String) {
        _selectedReportType.value = type
    }

    fun setTargetDate(date: LocalDate) {
        _targetDate.value = date
    }

    fun setDateRange(start: LocalDate, end: LocalDate) {
        _startDate.value = start
        _endDate.value = end
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
        if (_selectedDomains.value.size >= ALL_SPIRITUAL_DOMAINS.size) {
            _selectedDomains.value = setOf("ddewg") // Keep at least one domain
        } else {
            _selectedDomains.value = ALL_SPIRITUAL_DOMAINS
        }
    }

    private fun parseEntryDate(entry: com.example.data.local.entities.AccountabilityEntryEntity): LocalDate {
        if (entry.dateIso.isNotBlank()) {
            try {
                return LocalDate.parse(entry.dateIso.trim().take(10), DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (_: Exception) {}
        }
        if (entry.timestampMs > 0) {
            try {
                return java.time.Instant.ofEpochMilli(entry.timestampMs)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
            } catch (_: Exception) {}
        }
        return LocalDate.now()
    }

    private fun normalizeDomainId(domainId: String): String {
        val lower = domainId.lowercase().trim()
        return when {
            lower == "ddewg" || lower == "dreqd" -> "ddewg"
            lower.startsWith("bible_read") || lower == "bible" -> "bible_reading"
            lower.startsWith("bible_mem") -> "bible_mem"
            lower == "prayer_alone" || lower == "prayer" -> "prayer_alone"
            lower.startsWith("prayer_with") || lower.startsWith("prayer_group") -> "prayer_with_others"
            lower.startsWith("proclamation") -> "proclamation_importunity"
            lower.startsWith("christian_lit_mem") || lower.startsWith("lit_mem") -> "christian_lit_mem"
            lower.startsWith("christian_lit") || lower.startsWith("literature") -> "christian_lit"
            lower.startsWith("soul") || lower.startsWith("evangelism") -> "soul_winning"
            lower.startsWith("making_disciple") || lower.startsWith("disciple") || lower == "accountability" -> "making_disciples"
            lower.startsWith("fast") -> "fasting"
            lower.startsWith("giv") || lower.startsWith("offrand") -> "giving"
            lower.startsWith("retreat") -> "retreats"
            else -> lower
        }
    }

    fun generatePdfReport(context: Context, onPdfGenerated: (File) -> Unit) {
        viewModelScope.launch {
            try {
                val user = userRepository.currentUserFlow.first() ?: userRepository.getOrCreateGuestUser()
                val entries = withContext(Dispatchers.IO) {
                    try {
                        val directList = accountabilityRepository.getAllEntriesList()
                        if (directList.isNotEmpty()) directList else accountabilityRepository.allEntriesFlow.first()
                    } catch (_: Exception) {
                        try {
                            accountabilityRepository.allEntriesFlow.first()
                        } catch (_: Exception) {
                            emptyList<com.example.data.local.entities.AccountabilityEntryEntity>()
                        }
                    }
                }
                val reportType = _selectedReportType.value
                val activeDomains = _selectedDomains.value
                val refDate = _targetDate.value

                val startD = _startDate.value
                val endD = _endDate.value

                val currentAppLang = try {
                    userRepository.currentLanguageFlow.first()
                } catch (_: Exception) {
                    com.example.core.localization.AppLanguage.ENGLISH
                }
                val isFrench = currentAppLang == com.example.core.localization.AppLanguage.FRENCH

                val dateRangeLabel = when (reportType) {
                    "DAILY" -> refDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    "WEEKLY" -> if (isFrench) "Semaine du ${refDate.minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE)} au ${refDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                                else "Week of ${refDate.minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE)} to ${refDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                    "MONTHLY" -> {
                        val mName = refDate.format(DateTimeFormatter.ofPattern("MMMM", if (isFrench) java.util.Locale.FRENCH else java.util.Locale.ENGLISH))
                        "$mName ${refDate.year}"
                    }
                    "CUSTOM" -> if (isFrench) "Du ${startD.format(DateTimeFormatter.ISO_LOCAL_DATE)} au ${endD.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                                else "${startD.format(DateTimeFormatter.ISO_LOCAL_DATE)} to ${endD.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                    else -> refDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                }

                val filteredByPeriod = when (reportType) {
                    "DAILY" -> {
                        val targetIso = refDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        entries.filter { 
                            val parsed = parseEntryDate(it)
                            parsed == refDate || it.dateIso.trim().take(10) == targetIso
                        }
                    }
                    "WEEKLY" -> {
                        val startOfWeek = refDate.minusDays(6)
                        entries.filter {
                            val parsed = parseEntryDate(it)
                            !parsed.isBefore(startOfWeek) && !parsed.isAfter(refDate)
                        }
                    }
                    "MONTHLY" -> {
                        entries.filter {
                            val parsed = parseEntryDate(it)
                            parsed.month == refDate.month && parsed.year == refDate.year
                        }
                    }
                    "CUSTOM" -> {
                        val actualStart = if (startD.isAfter(endD)) endD else startD
                        val actualEnd = if (endD.isBefore(startD)) startD else endD
                        entries.filter {
                            val parsed = parseEntryDate(it)
                            !parsed.isBefore(actualStart) && !parsed.isAfter(actualEnd)
                        }
                    }
                    else -> entries
                }

                // Filter by checked domains with normalization
                val isAllSelected = activeDomains.size >= ALL_SPIRITUAL_DOMAINS.size
                val finalEntries = if (isAllSelected) {
                    filteredByPeriod
                } else {
                    filteredByPeriod.filter { entry ->
                        val norm = normalizeDomainId(entry.domainId)
                        activeDomains.contains(entry.domainId) || activeDomains.contains(norm)
                    }
                }

                val pdfFile = withContext(Dispatchers.IO) {
                    PdfReportGenerator.generatePdfReport(
                        context = context,
                        user = user,
                        reportType = reportType,
                        dateRangeLabel = dateRangeLabel,
                        entries = finalEntries,
                        isFrench = isFrench
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

                accountabilityRepository.logNotification(
                    context = context,
                    title = if (isFrench) "Rapport Généré" else "Report Generated",
                    message = if (isFrench) "Rapport PDF $reportType généré ($dateRangeLabel)" else "Generated $reportType PDF report ($dateRangeLabel)",
                    type = "REPORT"
                )

                onPdfGenerated(pdfFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteReport(id: String) {
        viewModelScope.launch {
            val record = reportHistory.value.find { it.id == id }
            record?.generatedFilePath?.let { path ->
                try {
                    val f = File(path)
                    if (f.exists()) f.delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            accountabilityRepository.deleteReportRecord(id)
        }
    }
}
