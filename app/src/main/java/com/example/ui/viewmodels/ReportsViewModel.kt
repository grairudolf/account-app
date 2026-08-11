package com.example.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ReportRecordEntity
import com.example.data.local.entities.UserEntity
import com.example.data.repositories.AccountabilityRepository
import com.example.data.repositories.UserRepository
import com.example.services.reports.PdfReportGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    private val _selectedReportType = MutableStateFlow("DAILY") // DAILY, WEEKLY, MONTHLY
    val selectedReportType: StateFlow<String> = _selectedReportType.asStateFlow()

    fun selectReportType(type: String) {
        _selectedReportType.value = type
    }

    fun generatePdfReport(context: Context, onPdfGenerated: (File) -> Unit) {
        viewModelScope.launch {
            val user = userRepository.getOrCreateGuestUser()
            val entries = accountabilityRepository.allEntriesFlow.first()
            val reportType = _selectedReportType.value

            val today = LocalDate.now()
            val dateRangeLabel = when (reportType) {
                "DAILY" -> today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                "WEEKLY" -> "Week of ${today.minusDays(6).format(DateTimeFormatter.ISO_LOCAL_DATE)} to ${today.format(DateTimeFormatter.ISO_LOCAL_DATE)}"
                "MONTHLY" -> "${today.month} ${today.year}"
                else -> today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            }

            val filteredEntries = when (reportType) {
                "DAILY" -> {
                    val todayIso = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    entries.filter { it.dateIso == todayIso }
                }
                "WEEKLY" -> {
                    val startOfWeek = today.minusDays(6)
                    entries.filter {
                        try {
                            val d = LocalDate.parse(it.dateIso)
                            !d.isBefore(startOfWeek) && !d.isAfter(today)
                        } catch (e: Exception) { false }
                    }
                }
                "MONTHLY" -> {
                    entries.filter {
                        try {
                            val d = LocalDate.parse(it.dateIso)
                            d.month == today.month && d.year == today.year
                        } catch (e: Exception) { false }
                    }
                }
                else -> entries
            }

            val pdfFile = PdfReportGenerator.generatePdfReport(
                context = context,
                user = user,
                reportType = reportType,
                dateRangeLabel = dateRangeLabel,
                entries = filteredEntries
            )

            val record = ReportRecordEntity(
                id = UUID.randomUUID().toString(),
                userId = user.id,
                reportType = reportType,
                dateRangeLabel = dateRangeLabel,
                selectedDomainsCsv = "ALL",
                generatedFilePath = pdfFile.absolutePath
            )
            accountabilityRepository.saveReportRecord(record)

            onPdfGenerated(pdfFile)
        }
    }
}
