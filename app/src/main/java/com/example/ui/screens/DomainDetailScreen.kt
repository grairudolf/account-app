package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppStrings
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainDetailScreen(
    domainId: String,
    strings: AppStrings,
    onNavigateToTimer: (String) -> Unit,
    onSaveEntry: (AccountabilityEntryEntity) -> Unit,
    onBack: () -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }

    // Domain Specific Inputs
    var bibleBook by remember { mutableStateOf("Genesis") }
    var startChapter by remember { mutableStateOf("1") }
    var endChapter by remember { mutableStateOf("1") }

    var durationMins by remember { mutableStateOf("30") }
    var preachedToCount by remember { mutableStateOf("1") }
    var convertedCount by remember { mutableStateOf("0") }

    var givingAmount by remember { mutableStateOf("0.0") }
    var givingType by remember { mutableStateOf("Tithe") }

    var fastingDays by remember { mutableStateOf("1") }
    var fastingType by remember { mutableStateOf("Dry") }

    var isSaved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = domainId.replace("_", " ").uppercase(),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("domain_detail_back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Live Timer Launcher Card for Prayer / DDEWG
            if (domainId == "ddewg" || domainId.startsWith("prayer")) {
                item {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = LightBlueContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Live Timer Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                            Text(
                                text = "Track your session in real time with high accuracy.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryBlue
                            )
                            Button(
                                onClick = { onNavigateToTimer(domainId) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("start_live_timer_button")
                            ) {
                                Icon(Icons.Default.Timer, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Live Timer")
                            }
                        }
                    }
                }
            }

            // Manual Entry Form Card
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Manual Entry Record",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        when (domainId) {
                            "bible_reading" -> {
                                OutlinedTextField(
                                    value = bibleBook,
                                    onValueChange = { bibleBook = it },
                                    label = { Text("Bible Book") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("entry_bible_book"),
                                    singleLine = true
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = startChapter,
                                        onValueChange = { startChapter = it },
                                        label = { Text("Start Chapter") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("entry_start_chapter"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = endChapter,
                                        onValueChange = { endChapter = it },
                                        label = { Text("End Chapter") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("entry_end_chapter"),
                                        singleLine = true
                                    )
                                }
                            }
                            "soul_winning" -> {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    OutlinedTextField(
                                        value = preachedToCount,
                                        onValueChange = { preachedToCount = it },
                                        label = { Text("Preached To") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("entry_preached_to"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = convertedCount,
                                        onValueChange = { convertedCount = it },
                                        label = { Text("Converts") },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("entry_converts"),
                                        singleLine = true
                                    )
                                }
                            }
                            "giving" -> {
                                OutlinedTextField(
                                    value = givingAmount,
                                    onValueChange = { givingAmount = it },
                                    label = { Text("Amount ($)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("entry_giving_amount"),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = givingType,
                                    onValueChange = { givingType = it },
                                    label = { Text("Type (Tithe, Offering, Mission)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("entry_giving_type"),
                                    singleLine = true
                                )
                            }
                            "fasting" -> {
                                OutlinedTextField(
                                    value = fastingDays,
                                    onValueChange = { fastingDays = it },
                                    label = { Text("Fasting Days") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("entry_fasting_days"),
                                    singleLine = true
                                )
                            }
                            else -> {
                                OutlinedTextField(
                                    value = durationMins,
                                    onValueChange = { durationMins = it },
                                    label = { Text("Duration (Minutes)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("entry_duration_mins"),
                                    singleLine = true
                                )
                            }
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes / Observations") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entry_notes")
                        )

                        OutlinedTextField(
                            value = reflection,
                            onValueChange = { reflection = it },
                            label = { Text("Spiritual Reflection") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("entry_reflection")
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                val sCh = startChapter.toIntOrNull() ?: 1
                                val eCh = endChapter.toIntOrNull() ?: 1
                                val chCount = (eCh - sCh + 1).coerceAtLeast(1)
                                val durSecs = (durationMins.toLongOrNull() ?: 30L) * 60L

                                val entry = AccountabilityEntryEntity(
                                    id = UUID.randomUUID().toString(),
                                    userId = "guest_user",
                                    domainId = domainId,
                                    dateIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    timestampMs = System.currentTimeMillis(),
                                    timezoneId = java.time.ZoneId.systemDefault().id,
                                    durationSeconds = durSecs,
                                    bibleBook = bibleBook,
                                    startChapter = sCh,
                                    endChapter = eCh,
                                    chaptersCount = chCount,
                                    preachedToCount = preachedToCount.toIntOrNull() ?: 0,
                                    convertedCount = convertedCount.toIntOrNull() ?: 0,
                                    givingAmount = givingAmount.toDoubleOrNull() ?: 0.0,
                                    givingType = givingType,
                                    fastingDaysCount = fastingDays.toIntOrNull() ?: 1,
                                    fastingType = fastingType,
                                    notes = notes,
                                    reflection = reflection
                                )
                                onSaveEntry(entry)
                                isSaved = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("save_manual_entry_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text("Save Activity Record", fontWeight = FontWeight.Bold)
                        }

                        if (isSaved) {
                            Text(
                                text = "Activity recorded successfully!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StatusSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
