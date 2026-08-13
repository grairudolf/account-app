package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppStrings
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.DayCompletionInfo
import com.example.ui.viewmodels.OverallStatisticsUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun StatisticsScreen(
    strings: AppStrings,
    uiState: OverallStatisticsUiState,
    selectedDate: LocalDate = LocalDate.now(),
    currentMonth: YearMonth = YearMonth.now(),
    monthDaysCompletion: List<DayCompletionInfo> = emptyList(),
    selectedDateEntries: List<AccountabilityEntryEntity> = emptyList(),
    allEntries: List<AccountabilityEntryEntity> = emptyList(),
    onSelectDate: (LocalDate) -> Unit = {},
    onNextMonth: () -> Unit = {},
    onPreviousMonth: () -> Unit = {},
    onGoToToday: () -> Unit = {},
    onUpdateEntry: (AccountabilityEntryEntity) -> Unit = {},
    onDeleteEntry: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Analytics Overview, 1: History Calendar & Logs
    var editingEntry by remember { mutableStateOf<AccountabilityEntryEntity?>(null) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = StreakGold.copy(alpha = 0.05f),
                radius = w * 0.5f,
                center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.15f)
            )
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.04f),
                radius = w * 0.55f,
                center = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.85f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Section Tabs: Overview vs History Calendar
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryBlue,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .testTag("statistics_tab_row")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Overview Analytics", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("History & Calendar", fontWeight = FontWeight.Bold) },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Text(
                            text = strings.spiritualAnalytics,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = strings.currentStreak,
                                value = "${uiState.streakStats.currentStreakDays} Days",
                                subtitle = "Longest: ${uiState.streakStats.longestStreakDays} Days",
                                icon = Icons.Default.LocalFireDepartment,
                                iconBg = StreakGoldContainer,
                                testTag = "stat_card_streak"
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Total Records",
                                value = "${uiState.totalEntriesCount}",
                                subtitle = "Logged Discipline Activities",
                                icon = Icons.Default.List,
                                iconBg = LightBlueContainer,
                                testTag = "stat_card_total"
                            )
                        }
                    }

                    // Weekly Activity Chart
                    item {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, DividerColor),
                            modifier = Modifier.fillMaxWidth().testTag("stat_card_chart")
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Weekly Activity Trend",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Disciplines completed per day",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                val maxVal = maxOf(uiState.weeklyActivity.maxOfOrNull { it.count } ?: 1, 1)
                                val todayIso = LocalDate.now().toString()

                                Row(
                                    modifier = Modifier.fillMaxWidth().height(120.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    uiState.weeklyActivity.forEach { dayActivity ->
                                        val value = dayActivity.count
                                        val isToday = dayActivity.dateIso == todayIso
                                        val heightRatio = if (maxVal > 0) value.toFloat() / maxVal.toFloat() else 0f

                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Bottom,
                                            modifier = Modifier.fillMaxHeight()
                                        ) {
                                            Text(
                                                text = "$value",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isToday) StreakGold else PrimaryBlue
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(22.dp)
                                                    .fillMaxHeight(heightRatio.coerceAtLeast(0.08f))
                                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                    .background(if (isToday) StreakGold else PrimaryBlue)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = dayActivity.dayLabel.take(3),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, DividerColor),
                            modifier = Modifier.fillMaxWidth().testTag("stat_card_bible")
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(text = strings.bibleReading, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Column {
                                        Text("Total Chapters", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${uiState.bibleStats.totalChaptersRead}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Bibles Read", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format("%.1f", uiState.bibleStats.biblesReadCount), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text("Completion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format("%.1f%%", uiState.bibleStats.completionPercentage), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Tab 1: History Calendar & Editable Activity Logs
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        // Interactive Full Month Grid Calendar Card
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, DividerColor),
                            modifier = Modifier.fillMaxWidth().testTag("statistics_calendar_card")
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = onPreviousMonth) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                                    }
                                    Text(
                                        text = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        IconButton(onClick = onGoToToday) {
                                            Icon(Icons.Default.Today, contentDescription = "Today", tint = PrimaryBlue)
                                        }
                                        IconButton(onClick = onNextMonth) {
                                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                                        }
                                    }
                                }

                                // Day headers
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                                        Text(
                                            text = day,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }

                                // Calendar Month Days Grid
                                val firstDayOfMonth = currentMonth.atDay(1)
                                val dayOfWeekOffset = (firstDayOfMonth.dayOfWeek.value - 1) % 7
                                val daysInMonth = currentMonth.lengthOfMonth()
                                val totalCells = dayOfWeekOffset + daysInMonth

                                val completionMap = remember(monthDaysCompletion) {
                                    monthDaysCompletion.associateBy { it.dateIso }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    for (weekRow in 0 until (totalCells + 6) / 7) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                            for (col in 0..6) {
                                                val dayNum = weekRow * 7 + col - dayOfWeekOffset + 1
                                                if (dayNum in 1..daysInMonth) {
                                                    val cellDate = currentMonth.atDay(dayNum)
                                                    val cellDateIso = cellDate.toString()
                                                    val isSelected = cellDate == selectedDate
                                                    val isToday = cellDate == LocalDate.now()
                                                    val dayInfo = completionMap[cellDateIso]
                                                    val hasEntries = dayInfo != null && dayInfo.entriesCount > 0

                                                    Box(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .aspectRatio(1f)
                                                            .padding(2.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                when {
                                                                    isSelected -> PrimaryBlue
                                                                    hasEntries -> LightBlueContainer
                                                                    isToday -> StreakGoldContainer
                                                                    else -> Color.Transparent
                                                                }
                                                            )
                                                            .clickable { onSelectDate(cellDate) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                            Text(
                                                                text = "$dayNum",
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            if (hasEntries) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(4.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (isSelected) Color.White else PrimaryBlue)
                                                                )
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Spacer(modifier = Modifier.weight(1f))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Selected Date: ${selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy"))}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (selectedDateEntries.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, DividerColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No recorded discipline entries for this date. Pick another date or tap on any past entry below to edit.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(selectedDateEntries, key = { "sel_${it.id}" }) { entry ->
                            EntryLogCard(
                                entry = entry,
                                onEdit = { editingEntry = entry },
                                onDelete = { onDeleteEntry(entry.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "All Past Accountability Records (${allEntries.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(allEntries, key = { "all_${it.id}" }) { entry ->
                        EntryLogCard(
                            entry = entry,
                            onEdit = { editingEntry = entry },
                            onDelete = { onDeleteEntry(entry.id) }
                        )
                    }
                }
            }
        }
    }

    if (editingEntry != null) {
        EditEntryDialog(
            entry = editingEntry!!,
            onDismiss = { editingEntry = null },
            onConfirm = { updated ->
                onUpdateEntry(updated)
                editingEntry = null
            }
        )
    }
}

@Composable
fun EntryLogCard(
    entry: AccountabilityEntryEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, DividerColor),
        modifier = Modifier.fillMaxWidth().testTag("entry_log_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = entry.domainId.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
                Text(
                    text = "Date: ${entry.dateIso}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (entry.domainId == "fasting") {
                    val days = if (entry.fastingDaysCount > 0) entry.fastingDaysCount else 1
                    Text(
                        text = "Fasting: $days Days" + if (entry.fastingType.isNotBlank()) " (${entry.fastingType})" else "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentPurple
                    )
                } else {
                    val timeSpan = if (entry.startTimeIso.isNotBlank() && entry.endTimeIso.isNotBlank()) {
                        "${entry.startTimeIso} - ${entry.endTimeIso}"
                    } else if (entry.timestampMs > 0 && entry.durationSeconds > 0) {
                        val fmt = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                        val endStr = fmt.format(java.util.Date(entry.timestampMs))
                        val startStr = fmt.format(java.util.Date(entry.timestampMs - entry.durationSeconds * 1000L))
                        "$startStr - $endStr"
                    } else ""

                    if (timeSpan.isNotBlank()) {
                        Text(
                            text = "Time Span: $timeSpan",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = PrimaryBlueDark
                        )
                    }
                    val mins = (entry.durationSeconds / 60).coerceAtLeast(1)
                    val displayDuration = if (mins >= 60) "${mins / 60} hrs ${mins % 60} mins" else "$mins mins"
                    Text(
                        text = "Duration: $displayDuration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentPurple
                    )
                }
                if (entry.prayerType.isNotBlank()) {
                    Text(
                        text = "Prayer Focus: ${entry.prayerType}" + if (entry.prayerTopicsCount > 0) " (${entry.prayerTopicsCount} Topics)" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryBlueDark
                    )
                }
                if (entry.notes.isNotBlank()) {
                    Text(
                        text = "Notes: ${entry.notes}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Entry", tint = PrimaryBlue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Entry", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun EditEntryDialog(
    entry: AccountabilityEntryEntity,
    onDismiss: () -> Unit,
    onConfirm: (AccountabilityEntryEntity) -> Unit
) {
    var notes by remember { mutableStateOf(entry.notes) }
    var chapters by remember { mutableStateOf(entry.chaptersCount.toString()) }
    var prayerMins by remember { mutableStateOf((entry.durationSeconds / 60).toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Past Discipline Record") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Activity Notes / Reflection") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = chapters,
                    onValueChange = { chapters = it },
                    label = { Text("Chapters Read / Count") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = prayerMins,
                    onValueChange = { prayerMins = it },
                    label = { Text("Duration (Minutes)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = entry.copy(
                    notes = notes,
                    chaptersCount = chapters.toIntOrNull() ?: entry.chaptersCount,
                    durationSeconds = (prayerMins.toLongOrNull() ?: (entry.durationSeconds / 60)) * 60,
                    updatedAtMs = System.currentTimeMillis()
                )
                onConfirm(updated)
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, DividerColor),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            }
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

