package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppStrings
import com.example.data.local.entities.ReportRecordEntity
import com.example.data.local.entities.UserEntity
import com.example.ui.theme.*
import java.io.File

@Composable
fun ReportsScreen(
    strings: AppStrings,
    user: UserEntity?,
    selectedReportType: String,
    selectedDomains: Set<String> = emptySet(),
    targetDate: java.time.LocalDate = java.time.LocalDate.now(),
    reportHistory: List<ReportRecordEntity>,
    onSelectReportType: (String) -> Unit,
    onToggleDomainFilter: (String) -> Unit = {},
    onSelectAllDomains: () -> Unit = {},
    onSetTargetDate: (java.time.LocalDate) -> Unit = {},
    onGeneratePdfReport: (Context, (File) -> Unit) -> Unit
) {
    val context = LocalContext.current
    var lastGeneratedFile by remember { mutableStateOf<File?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

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
        val shareMessage = "✝️ CMFI Spiritual Accountability Account Summary\n" +
                "Disciple: $userName ($assembly)\n" +
                "Report Period: $selectedReportType\n\n" +
                "Logged via CMFI Spiritual Accountability App.\n" +
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

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.05f),
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            Text(
                text = strings.accountabilityReports,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        // Generator Card - 28.dp rounded card
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
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
                        text = "Generate a formatted CMFI PDF report summarizing your spiritual disciplines for your Disciple Maker.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Period selector chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("DAILY", "WEEKLY", "MONTHLY").forEach { type ->
                            FilterChip(
                                selected = selectedReportType == type,
                                onClick = { onSelectReportType(type) },
                                label = { Text(type) },
                                shape = RoundedCornerShape(20.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("report_type_chip_$type")
                            )
                        }
                    }

                    // Domain Checkboxes Selection
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Select Exact Domains to Include:",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                TextButton(onClick = onSelectAllDomains) {
                                    Text("Select All", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            val availableDomains = listOf(
                                "ddewg" to "DDEWG (Daily Encounter)",
                                "bible_reading" to "Bible Reading",
                                "prayer_alone" to "Prayer Alone",
                                "prayer_with_others" to "Prayer With Others",
                                "fasting" to "Fasting",
                                "giving" to "Giving & Tithes",
                                "christian_lit" to "Christian Literature",
                                "soul_winning" to "Soul Winning & Evangelism"
                            )

                            availableDomains.forEach { (id, title) ->
                                val checked = selectedDomains.contains(id)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onToggleDomainFilter(id) }
                                        .padding(vertical = 4.dp)
                                ) {
                                    Checkbox(
                                        checked = checked,
                                        onCheckedChange = { onToggleDomainFilter(id) },
                                        colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (checked) FontWeight.Bold else FontWeight.Normal
                                    )
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
                            .height(50.dp)
                            .testTag("generate_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(20.dp),
                        enabled = !isGenerating
                    ) {
                        if (isGenerating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Generate $selectedReportType PDF", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (lastGeneratedFile != null) {
                        val file = lastGeneratedFile!!
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = LightBlueContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "PDF Generated!",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusSuccess
                                    )
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrimaryBlueDark
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { openPdfFile(file) }) {
                                        Icon(
                                            imageVector = Icons.Default.PictureAsPdf,
                                            contentDescription = "Open PDF",
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    IconButton(onClick = { sharePdfFile(file) }) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share PDF File",
                                            tint = PrimaryBlue,
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
                            tint = PrimaryBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Share Accounts to Social Media & Messaging",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Send summary links or reports directly to your Disciple Maker, WhatsApp, or Social Platforms:",
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
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Summary")
                        }
                        OutlinedButton(
                            onClick = { shareTextReport(null) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Link")
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
            items(reportHistory) { record ->
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
                                .background(LightBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, tint = PrimaryBlue)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${record.reportType} REPORT",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
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
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = "Open PDF", tint = PrimaryBlue)
                                }
                                IconButton(onClick = { sharePdfFile(savedFile) }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share PDF", tint = PrimaryBlue)
                                }
                            } else {
                                IconButton(onClick = { shareTextReport(null) }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share Summary", tint = PrimaryBlue)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
}
