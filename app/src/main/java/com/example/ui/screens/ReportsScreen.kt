package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.example.core.localization.AppStrings
import com.example.data.local.entities.ReportRecordEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.*
import java.io.File

private data class ReportDomainItem(
    val id: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun ReportsScreen(
    strings: AppStrings,
    user: UserEntity?,
    selectedReportType: String,
    selectedDomains: Set<String> = emptySet(),
    targetDate: java.time.LocalDate = java.time.LocalDate.now(),
    startDate: java.time.LocalDate = java.time.LocalDate.now().minusDays(7),
    endDate: java.time.LocalDate = java.time.LocalDate.now(),
    reportHistory: List<ReportRecordEntity>,
    onSelectReportType: (String) -> Unit,
    onToggleDomainFilter: (String) -> Unit = {},
    onSelectAllDomains: () -> Unit = {},
    onSetTargetDate: (java.time.LocalDate) -> Unit = {},
    onSetDateRange: (java.time.LocalDate, java.time.LocalDate) -> Unit = { _, _ -> },
    onGeneratePdfReport: (Context, (File) -> Unit) -> Unit,
    onDeleteReport: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val isFrench = strings is com.example.core.localization.FrenchStrings
    val locale = if (isFrench) java.util.Locale.FRENCH else java.util.Locale.ENGLISH
    var lastGeneratedFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }
    var showCalendarDatePicker by remember { mutableStateOf(false) }
    var reportToDelete by remember { mutableStateOf<ReportRecordEntity?>(null) }

    fun sharePdfFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share PDF Report via:"))
        } catch (e: Exception) {
            Toast.makeText(context, "Unable to share PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    fun openPdfFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open PDF Report with:"))
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer app available", Toast.LENGTH_LONG).show()
        }
    }

    fun shareTextReport(platform: String?) {
        val userName = user?.fullName?.ifBlank { "A Disciple" } ?: "A Disciple"
        val assembly = user?.localAssembly?.ifBlank { "CMFI" } ?: "CMFI"
        val shareMessage = "✝️ CMFI Accap Summary\n" +
                "Disciple: $userName ($assembly)\n" +
                "Report Period: $selectedReportType\n\n" +
                "Logged via CMFI Accap.\n" +
                "\"He who holds himself accountable grows in grace.\" #CMFI #Discipleship"

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareMessage)
            type = "text/plain"
            if (platform != null) {
                `package` = platform
            }
        }
        val chooser = Intent.createChooser(sendIntent, "Share Account Summary via:")
        context.startActivity(chooser)
    }

    val primaryCanvasColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = primaryCanvasColor,
                radius = w * 0.5f,
                center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.15f)
            )
            drawCircle(
                color = AccentPurple.copy(alpha = 0.04f),
                radius = w * 0.55f,
                center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.8f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth()) {
                Text(
                    text = strings.accountabilityReports,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Generator Card - 28.dp rounded card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .testTag("reports_generator_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = strings.generatePdf,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = strings.reportDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Period selector chips (Scrollable Row with Dark Pill Active States for internationalization)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DAILY", "WEEKLY", "MONTHLY", "CUSTOM").forEach { type ->
                            val chipLabel = when (type) {
                                "DAILY" -> strings.dailyReport
                                "WEEKLY" -> strings.weeklyReport
                                "MONTHLY" -> strings.monthlyReport
                                else -> type
                            }
                            val selected = selectedReportType == type
                            val containerBg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            val borderStroke = if (selected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = containerBg,
                                border = borderStroke,
                                modifier = Modifier
                                    .clickable { onSelectReportType(type) }
                                    .testTag("report_type_chip_$type")
                            ) {
                                Text(
                                    text = chipLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = contentColor,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Exact Date / Date Range Selector
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = strings.exactDateSelection,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val monthNameRaw = targetDate.format(java.time.format.DateTimeFormatter.ofPattern("MMMM", locale))
                                val monthName = monthNameRaw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                                val dateText = when (selectedReportType) {
                                    "DAILY" -> "${strings.targetDayLabel}: ${targetDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", locale))}"
                                    "WEEKLY" -> "${strings.weekEndingLabel}: ${targetDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", locale))}"
                                    "MONTHLY" -> "${strings.monthLabel}: $monthName ${targetDate.year}"
                                    else -> "${strings.dateRangeLabel}: ${startDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM", locale))} — ${endDate.format(java.time.format.DateTimeFormatter.ofPattern("d MMM yyyy", locale))}"
                                }
                                Text(
                                    text = dateText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                OutlinedButton(
                                    onClick = { showCalendarDatePicker = true },
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(strings.changeDate, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }

                    // Domain Chips Selection Grid
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = strings.selectDomainsToInclude,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        val totalCount = 13
                                        Text(
                                            text = "${selectedDomains.size}/$totalCount",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = onSelectAllDomains,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = if (selectedDomains.size >= 13) "Deselect All" else strings.selectAll,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            val availableDomains = listOf(
                                ReportDomainItem("ddewg", strings.ddewgTitle, Icons.Default.AutoAwesome),
                                ReportDomainItem("bible_reading", strings.bibleReadingTitle, Icons.Default.MenuBook),
                                ReportDomainItem("prayer_alone", strings.prayerAloneTitle, Icons.Default.SelfImprovement),
                                ReportDomainItem("prayer_with_others", strings.prayerWithOthersTitle, Icons.Default.Groups),
                                ReportDomainItem("proclamation_importunity", strings.proclamationTitle, Icons.Default.Campaign),
                                ReportDomainItem("retreats", strings.retreatsTitle, Icons.Default.Landscape),
                                ReportDomainItem("fasting", strings.fastingTitle, Icons.Default.Timer),
                                ReportDomainItem("giving", strings.givingTitle, Icons.Default.VolunteerActivism),
                                ReportDomainItem("christian_lit", strings.christianLitTitle, Icons.Default.AutoStories),
                                ReportDomainItem("christian_lit_mem", strings.christianLitMemTitle, Icons.Default.Psychology),
                                ReportDomainItem("bible_mem", strings.bibleMemTitle, Icons.Default.FormatQuote),
                                ReportDomainItem("soul_winning", strings.soulWinningTitle, Icons.Default.GroupAdd),
                                ReportDomainItem("making_disciples", strings.makingDisciplesTitle, Icons.Default.People)
                            )

                            // Render in neat 2-column grid rows
                            availableDomains.chunked(2).forEach { rowPair ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowPair.forEach { item ->
                                        val isChecked = selectedDomains.contains(item.id)

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                            border = BorderStroke(
                                                width = if (isChecked) 1.5.dp else 1.dp,
                                                color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .clickable { onToggleDomainFilter(item.id) }
                                                .testTag("domain_chip_${item.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = item.icon,
                                                    contentDescription = null,
                                                    tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = if (isChecked) FontWeight.Bold else FontWeight.Medium,
                                                    color = if (isChecked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (isChecked) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = "Selected",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (rowPair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            isGenerating = true
                            onGeneratePdfReport(context) { file ->
                                lastGeneratedFile = file
                                isGenerating = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_pdf_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(26.dp),
                        enabled = !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            val btnText = String.format(strings.generatePdfButton, selectedReportType)
                            Text(btnText, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimary)
                        }
                    }

                    if (lastGeneratedFile != null) {
                        val file = lastGeneratedFile!!
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.pdfGeneratedTitle,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusSuccess
                                    )
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { openPdfFile(file) }) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = "Open PDF",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(onClick = { sharePdfFile(file) }) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share PDF File",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            try {
                                                if (file.exists()) file.delete()
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                            val matching = reportHistory.find { it.generatedFilePath == file.absolutePath }
                                            if (matching != null) {
                                                onDeleteReport(matching.id)
                                            }
                                            lastGeneratedFile = null
                                            Toast.makeText(context, "Report deleted", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("delete_last_generated_pdf_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Report",
                                            tint = StatusError,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Social Media & Messaging Share Card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
                    .widthIn(max = 840.dp)
                    .fillMaxWidth()
                    .testTag("reports_social_share_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = strings.shareAccountsTitle,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = strings.shareAccountsDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { shareTextReport(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.shareSummary, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { shareTextReport(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.copyLink)
                        }
                    }
                }
            }
        }

        // History Section
        item {
            Text(
                text = strings.generatedHistory,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (reportHistory.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.noReportHistory,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(reportHistory, key = { "rep_${it.id}" }) { record ->
                val savedFile = record.generatedFilePath?.let { File(it) }
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("report_history_item_${record.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${record.reportType} REPORT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = record.dateRangeLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (savedFile != null && savedFile.exists()) {
                                IconButton(onClick = { openPdfFile(savedFile) }) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Open PDF", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { sharePdfFile(savedFile) }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                IconButton(onClick = { shareTextReport(null) }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share Summary", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            IconButton(
                                onClick = { reportToDelete = record },
                                modifier = Modifier.testTag("delete_report_button_${record.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete Report", tint = StatusError)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCalendarDatePicker) {
        ReportDatePickerDialog(
            strings = strings,
            currentReportType = selectedReportType,
            currentTargetDate = targetDate,
            currentStartDate = startDate,
            currentEndDate = endDate,
            onDismiss = { showCalendarDatePicker = false },
            onConfirmDate = { date ->
                onSetTargetDate(date)
                showCalendarDatePicker = false
            },
            onConfirmRange = { start, end ->
                onSetDateRange(start, end)
                showCalendarDatePicker = false
            }
        )
    }

    if (reportToDelete != null) {
        val target = reportToDelete!!
        AlertDialog(
            onDismissRequest = { reportToDelete = null },
            title = { Text(if (isFrench) "Supprimer le rapport" else "Delete Report", fontWeight = FontWeight.Bold) },
            text = { Text(if (isFrench) "Êtes-vous sûr de vouloir supprimer ce rapport ${target.reportType} (${target.dateRangeLabel}) ? Cette action est irréversible." else "Are you sure you want to delete this ${target.reportType} report record (${target.dateRangeLabel})? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        target.generatedFilePath?.let { path ->
                            try {
                                val f = File(path)
                                if (f.exists()) f.delete()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        if (lastGeneratedFile?.absolutePath == target.generatedFilePath) {
                            lastGeneratedFile = null
                        }
                        onDeleteReport(target.id)
                        reportToDelete = null
                        Toast.makeText(context, if (isFrench) "Rapport supprimé" else "Report deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (isFrench) "Supprimer" else "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportToDelete = null }) {
                    Text(strings.cancel)
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}
}

@Composable
fun ReportDatePickerDialog(
    strings: AppStrings,
    currentReportType: String,
    currentTargetDate: java.time.LocalDate,
    currentStartDate: java.time.LocalDate,
    currentEndDate: java.time.LocalDate,
    onDismiss: () -> Unit,
    onConfirmDate: (java.time.LocalDate) -> Unit,
    onConfirmRange: (java.time.LocalDate, java.time.LocalDate) -> Unit
) {
    val isFrench = strings is com.example.core.localization.FrenchStrings
    var targetDateText by remember { mutableStateOf(currentTargetDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)) }
    var startDateText by remember { mutableStateOf(currentStartDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)) }
    var endDateText by remember { mutableStateOf(currentEndDate.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)) }

    val today = java.time.LocalDate.now()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (currentReportType == "CUSTOM") (if (isFrench) "Sélectionner la plage de dates" else "Select Custom Date Range") else (if (isFrench) "Sélectionner la date du rapport" else "Select Report Date"),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Quick Presets
                Text(if (isFrench) "Sélection rapide :" else "Quick Preset Selection:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            targetDateText = today.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                            startDateText = today.minusDays(7).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                            endDateText = today.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                        },
                        label = { Text(strings.today) },
                        shape = CircleShape
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            val yest = today.minusDays(1)
                            targetDateText = yest.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                            startDateText = today.minusDays(14).format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                            endDateText = yest.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
                        },
                        label = { Text(if (isFrench) "Hier" else "Yesterday") },
                        shape = CircleShape
                    )
                }

                HorizontalDivider(color = DividerColor)

                if (currentReportType == "CUSTOM") {
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { startDateText = it },
                        label = { Text(if (isFrench) "Date de début (AAAA-MM-JJ)" else "Start Date (YYYY-MM-DD)") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { endDateText = it },
                        label = { Text(if (isFrench) "Date de fin (AAAA-MM-JJ)" else "End Date (YYYY-MM-DD)") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    OutlinedTextField(
                        value = targetDateText,
                        onValueChange = { targetDateText = it },
                        label = { Text(if (isFrench) "Date cible (AAAA-MM-JJ)" else "Target Date (YYYY-MM-DD)") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    try {
                        if (currentReportType == "CUSTOM") {
                            val s = java.time.LocalDate.parse(startDateText)
                            val e = java.time.LocalDate.parse(endDateText)
                            onConfirmRange(s, e)
                        } else {
                            val d = java.time.LocalDate.parse(targetDateText)
                            onConfirmDate(d)
                        }
                    } catch (ex: Exception) {
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (isFrench) "Appliquer" else "Apply Date")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
