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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.ui.components.AppDatePickerDialog
import com.example.ui.components.AppTimePickerDialog
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
    selectedTab: Int = 0,
    selectedTimeRange: String = "LAST_7_DAYS",
    onTimeRangeSelected: (String) -> Unit = {},
    onTabSelected: (Int) -> Unit = {},
    onSelectDate: (LocalDate) -> Unit = {},
    onNextMonth: () -> Unit = {},
    onPreviousMonth: () -> Unit = {},
    onGoToToday: () -> Unit = {},
    onUpdateEntry: (AccountabilityEntryEntity) -> Unit = {},
    onDeleteEntry: (String) -> Unit = {}
) {
    var editingEntry by remember { mutableStateOf<AccountabilityEntryEntity?>(null) }
    var pastRecordsLimit by remember { mutableIntStateOf(10) }
    val isFrench = strings is com.example.core.localization.FrenchStrings
    val locale = if (isFrench) java.util.Locale.FRENCH else java.util.Locale.ENGLISH

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

            // Section Tabs: Overview vs History Calendar (Sleek Dark / Light Pill Design)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("statistics_tab_row")
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val overviewActive = selectedTab == 0
                    val historyActive = selectedTab == 1

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (overviewActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onTabSelected(0) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = null,
                                tint = if (overviewActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.analyticsOverview,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (overviewActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (historyActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onTabSelected(1) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (historyActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.historyAndCalendar,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (historyActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == 0) {
                val timeRangeRanges = listOf(
                    "LAST_7_DAYS" to if (isFrench) "7 Derniers Jours" else "Last 7 Days",
                    "LAST_30_DAYS" to if (isFrench) "30 Derniers Jours" else "Last 30 Days",
                    "LAST_3_MONTHS" to if (isFrench) "3 Derniers Mois" else "Last 3 Months",
                    "LAST_6_MONTHS" to if (isFrench) "6 Derniers Mois" else "Last 6 Months",
                    "LAST_1_YEAR" to if (isFrench) "Dernière Année" else "Last Year",
                    "ALL_TIME" to if (isFrench) "Tout l'Historique" else "All Time"
                )
                val currentRangeLabel = timeRangeRanges.find { it.first == selectedTimeRange }?.second ?: (if (isFrench) "7 Derniers Jours" else "Last 7 Days")

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.spiritualAnalytics,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            var timeRangeExpanded by remember { mutableStateOf(false) }

                            Box {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable { timeRangeExpanded = true }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = currentRangeLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Filter time range",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = timeRangeExpanded,
                                    onDismissRequest = { timeRangeExpanded = false }
                                ) {
                                    timeRangeRanges.forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = label,
                                                    fontWeight = if (key == selectedTimeRange) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                onTimeRangeSelected(key)
                                                timeRangeExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Feature: Total Time Spent with God Today & Key Spiritual Metrics
                    item {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("stat_card_time_with_god_today")
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.AccessTime,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = strings.totalTimeWithGodToday,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }

                                Text(
                                    text = formatStatsDuration(uiState.todayTimeWithGodSeconds, isFrench),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )

                                Text(
                                    text = "${strings.totalTimeWithGod} ($currentRangeLabel): ${formatStatsDuration(uiState.totalTimeWithGodSeconds, isFrench)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )

                                // Key Domain Badges (DDEWG, Thanksgiving, Requests, 15-min retreats, Proclamations)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${uiState.totalDdewgCount}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = strings.unitDdewg,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${uiState.totalThanksgivingTopics}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = strings.prayerTypeThanksgiving,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${uiState.totalRequestTopics}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = strings.prayerTypeRequest,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${uiState.total15MinRetreats}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "15-Min",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "${uiState.totalProclamationRepetitions}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = "Procl.",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                 }
                            }
                        }
                    }

                    // Proclamations Statistics Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, DividerColor),
                            modifier = Modifier.fillMaxWidth().testTag("stat_card_proclamations")
                        ) {
                            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = strings.proclamationTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Icon(
                                        Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(
                                            strings.proclamationsMade,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "${uiState.totalProclamationRepetitions}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Column {
                                        Text(
                                            strings.topicsCovered,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "${uiState.totalProclamationTopicsCount}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text(
                                            strings.duration,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "${uiState.totalProclamationMinutes} ${strings.minutesUnit}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = strings.currentStreak,
                                value = "${uiState.streakStats.currentStreakDays} ${strings.days}",
                                subtitle = "${strings.longestStreak}: ${uiState.streakStats.longestStreakDays} ${strings.days}",
                                icon = Icons.Default.LocalFireDepartment,
                                iconBg = MaterialTheme.colorScheme.tertiaryContainer,
                                testTag = "stat_card_streak"
                            )
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = strings.totalRecords,
                                value = "${uiState.totalEntriesCount}",
                                subtitle = strings.loggedDisciplineActivities,
                                icon = Icons.Default.List,
                                iconBg = MaterialTheme.colorScheme.primaryContainer,
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
                                            text = strings.weeklyActivityTrend,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = strings.disciplinesCompletedPerDay,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                val maxVal = maxOf(uiState.weeklyActivity.maxOfOrNull { it.count } ?: 1, 1)
                                val todayIso = LocalDate.now().toString()

                                Row(
                                    modifier = Modifier.fillMaxWidth().height(130.dp),
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
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxHeight()
                                        ) {
                                            Text(
                                                text = "$value",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isToday) StreakGold else MaterialTheme.colorScheme.primary,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(22.dp)
                                                    .height((75.dp * heightRatio).coerceAtLeast(8.dp))
                                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                                    .background(if (isToday) StreakGold else MaterialTheme.colorScheme.primary)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            val dayDate = try { LocalDate.parse(dayActivity.dateIso) } catch (e: Exception) { null }
                                            val dayText = if (dayDate != null) {
                                                dayDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale).take(3).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                                            } else {
                                                dayActivity.dayLabel.take(3)
                                            }
                                            Text(
                                                text = dayText,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center
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
                                        Text(
                                            text = if (selectedTimeRange == "ALL_TIME") strings.totalBibleChapters else if (isFrench) "Chapitres Lus ($currentRangeLabel)" else "Chapters Read ($currentRangeLabel)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text("${uiState.bibleStats.totalChaptersRead}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text(strings.biblesRead, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(String.format("%.1f", uiState.bibleStats.biblesReadCount), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    Column {
                                        Text(strings.completion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                    val statMonthTitleRaw = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale))
                                    val statMonthTitle = statMonthTitleRaw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                                    Text(
                                        text = statMonthTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row {
                                        IconButton(onClick = onGoToToday) {
                                            Icon(Icons.Default.Today, contentDescription = "Today", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = onNextMonth) {
                                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                                        }
                                    }
                                }

                                // Day headers
                                val statDayHeaders = if (isFrench) listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim") else listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    statDayHeaders.forEach { day ->
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
                                                                    isSelected -> MaterialTheme.colorScheme.primary
                                                                    hasEntries -> MaterialTheme.colorScheme.primaryContainer
                                                                    isToday -> MaterialTheme.colorScheme.secondaryContainer
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
                                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                                            )
                                                            if (hasEntries) {
                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(4.dp)
                                                                        .clip(CircleShape)
                                                                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
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
                        val selDateFormattedRaw = selectedDate.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", locale))
                        val selDateFormatted = selDateFormattedRaw.replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
                        val communionDomains = listOf("ddewg", "prayer_alone", "prayer_with_others", "bible_reading", "christian_lit", "christian_lit_mem", "bible_mem", "proclamation_importunity", "retreats")
                        val dayGodSecs = selectedDateEntries.filter { it.domainId in communionDomains }.sumOf { it.durationSeconds }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = String.format(strings.selectedDateLabel, selDateFormatted),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )

                            // Time with God for this specific day card
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AccessTime,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Column {
                                            Text(
                                                text = strings.totalTimeWithGodDate,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Text(
                                                text = formatStatsDuration(dayGodSecs, isFrench),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }

                                    val dayDdewgCount = selectedDateEntries.count { it.domainId == "ddewg" }
                                    if (dayDdewgCount > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "$dayDdewgCount ${strings.unitDdewg}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
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
                                    text = strings.noRecordedEntriesForDate,
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
                                strings = strings,
                                onEdit = { editingEntry = entry },
                                onDelete = { onDeleteEntry(entry.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${strings.allPastRecords} (${allEntries.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (allEntries.size > 10) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                ) {
                                    val showingCount = minOf(pastRecordsLimit, allEntries.size)
                                    Text(
                                        text = "$showingCount / ${allEntries.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    val displayedPastRecords = if (allEntries.size <= pastRecordsLimit) allEntries else allEntries.take(pastRecordsLimit)

                    items(displayedPastRecords, key = { "all_${it.id}" }) { entry ->
                        EntryLogCard(
                            entry = entry,
                            strings = strings,
                            onEdit = { editingEntry = entry },
                            onDelete = { onDeleteEntry(entry.id) }
                        )
                    }

                    if (allEntries.size > 10) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (allEntries.size > pastRecordsLimit) {
                                    Button(
                                        onClick = { pastRecordsLimit += 15 },
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("statistics_view_more_records_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExpandMore,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        val remaining = allEntries.size - pastRecordsLimit
                                        val nextBatch = minOf(remaining, 15)
                                        Text(
                                            text = if (isFrench) "Voir plus (+${nextBatch})" else "View More (+${nextBatch})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }

                                if (pastRecordsLimit > 10) {
                                    OutlinedButton(
                                        onClick = { pastRecordsLimit = 10 },
                                        shape = RoundedCornerShape(16.dp),
                                        modifier = Modifier
                                            .weight(if (allEntries.size > pastRecordsLimit) 0.7f else 1f)
                                            .testTag("statistics_show_less_records_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ExpandLess,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isFrench) "Réduire" else "Show Less",
                                            fontWeight = FontWeight.SemiBold,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (editingEntry != null) {
        EditEntryDialog(
            entry = editingEntry!!,
            strings = strings,
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
    strings: AppStrings,
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
                val domainLabel = if (entry.domainId == "ddewg") strings.ddewgAbbr else strings.getDomainTitleById(entry.domainId)
                Text(
                    text = domainLabel.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Date: ${entry.dateIso}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Domain-Specific Log Details
                if (entry.domainId == "fasting") {
                    val days = if (entry.fastingDaysCount > 0) entry.fastingDaysCount else 1
                    val rangeStr = if (entry.fastingStartDateIso.isNotBlank() && entry.fastingEndDateIso.isNotBlank()) " (${entry.fastingStartDateIso} -> ${entry.fastingEndDateIso})" else ""
                    Text(
                        text = "${strings.fastingTitle}: $days ${strings.days}$rangeStr" + if (entry.fastingType.isNotBlank()) " [${entry.fastingType}]" else "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentPurple
                    )
                    if (entry.fastingPurpose.isNotBlank()) {
                        Text(
                            text = "${strings.purpose}: ${entry.fastingPurpose}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (entry.domainId == "giving") {
                    val gType = if (entry.givingType.isNotBlank()) " (${entry.givingType})" else ""
                    val incRef = if (entry.givingIncomeReference > 0) " | Income Ref: XAF ${entry.givingIncomeReference}" else ""
                    Text(
                        text = "${strings.givingTitle}: XAF ${entry.givingAmount}$gType$incRef",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentPurple
                    )
                } else if (entry.domainId in listOf("christian_lit", "christian_lit_reading", "christian_lit_mem", "christian_lit_memory") || entry.bookTitle.isNotBlank()) {
                    if (entry.bookTitle.isNotBlank()) {
                        Text(
                            text = "${strings.bookTitle}: ${entry.bookTitle}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (entry.bookAuthor.isNotBlank()) {
                        Text(
                            text = "${strings.author}: ${entry.bookAuthor}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    val pCount = if (entry.pagesRead > 0) entry.pagesRead else if (entry.pagesMemorized > 0) entry.pagesMemorized else (entry.endPage - entry.startPage + 1).coerceAtLeast(1)
                    Text(
                        text = "Pages: ${entry.startPage} - ${entry.endPage} ($pCount pages)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (entry.isBookCompleted) {
                        Text(
                            text = "[Completed]",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = StreakGold
                        )
                    }
                } else if (entry.domainId == "bible_reading" || entry.bibleBook.isNotBlank()) {
                    if (entry.bibleBook.isNotBlank()) {
                        Text(
                            text = "Bible: ${entry.bibleBook}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (entry.chaptersCount > 0) {
                        Text(
                            text = "${strings.chaptersReadLabel}: ${entry.chaptersCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                } else if (entry.domainId in listOf("bible_mem", "bible_memory") || entry.bibleMemBook.isNotBlank()) {
                    Text(
                        text = "Memory: ${entry.bibleMemBook} Ch ${entry.bibleMemChapter} (${entry.bibleMemVerse})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else if (entry.domainId == "ddewg" || entry.reflection.isNotBlank()) {
                    if (entry.reflection.isNotBlank()) {
                        Text(
                            text = "Encounter: ${entry.reflection}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Time span & Duration
                if (entry.domainId != "fasting" && entry.domainId != "giving") {
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
                            text = "${strings.timeSpanLabel}: $timeSpan",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    val mins = (entry.durationSeconds / 60).coerceAtLeast(1)
                    val displayDuration = if (mins >= 60) "${mins / 60} ${strings.hoursUnit} ${mins % 60} ${strings.minutesUnit}" else "$mins ${strings.minutesUnit}"
                    Text(
                        text = "${strings.duration}: $displayDuration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (entry.prayerType.isNotBlank()) {
                    val topicsStr = if (entry.startPrayerTopicNumber > 0 && entry.endPrayerTopicNumber > 0) {
                        " (Topics #${entry.startPrayerTopicNumber} - #${entry.endPrayerTopicNumber} = ${entry.prayerTopicsCount} topics)"
                    } else if (entry.prayerTopicsCount > 0) {
                        " (" + String.format(strings.topicsCountFormat, entry.prayerTopicsCount) + ")"
                    } else ""

                    val periodStr = if (entry.retreatPeriodOfDay.isNotBlank()) " [${entry.retreatPeriodOfDay}]" else ""
                    val partStr = if (entry.prayerParticipantsCount > 1) " [${entry.prayerParticipantsCount} participants]" else ""
                    Text(
                        text = "${strings.prayerFocus}: ${entry.prayerType}$periodStr$topicsStr$partStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (entry.notes.isNotBlank()) {
                    Text(
                        text = "${strings.notes}: ${entry.notes}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryDialog(
    entry: AccountabilityEntryEntity,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (AccountabilityEntryEntity) -> Unit
) {
    var dateIso by remember { mutableStateOf(entry.dateIso) }
    var notes by remember { mutableStateOf(entry.notes) }
    var startTimeIso by remember { mutableStateOf(entry.startTimeIso.ifBlank { "06:00" }) }
    var endTimeIso by remember { mutableStateOf(entry.endTimeIso.ifBlank { "07:00" }) }

    // Pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showFastingStartDatePicker by remember { mutableStateOf(false) }
    var showFastingEndDatePicker by remember { mutableStateOf(false) }

    // Prayer Alone & Prayer with Others
    var prayerType by remember { mutableStateOf(entry.prayerType) }
    var startPrayerTopicNum by remember { mutableStateOf(if (entry.startPrayerTopicNumber > 0) entry.startPrayerTopicNumber.toString() else "") }
    var endPrayerTopicNum by remember { mutableStateOf(if (entry.endPrayerTopicNumber > 0) entry.endPrayerTopicNumber.toString() else "") }
    var prayerTopicsCount by remember { mutableStateOf(if (entry.prayerTopicsCount > 0) entry.prayerTopicsCount.toString() else "1") }
    var retreatPeriodOfDay by remember { mutableStateOf(entry.retreatPeriodOfDay.ifBlank { "Morning" }) }
    var prayerParticipantsCount by remember { mutableStateOf(if (entry.prayerParticipantsCount > 0) entry.prayerParticipantsCount.toString() else "1") }

    // DDEWG
    var ddewgInspirationText by remember { mutableStateOf(entry.reflection) }

    // Bible Reading & Memory
    var bibleBook by remember { mutableStateOf(entry.bibleBook) }
    var startChapter by remember { mutableStateOf(if (entry.startChapter > 0) entry.startChapter.toString() else "1") }
    var endChapter by remember { mutableStateOf(if (entry.endChapter > 0) entry.endChapter.toString() else "1") }
    var chaptersCount by remember { mutableStateOf(if (entry.chaptersCount > 0) entry.chaptersCount.toString() else "1") }
    var bibleMemBook by remember { mutableStateOf(entry.bibleMemBook) }
    var bibleMemChapter by remember { mutableStateOf(if (entry.bibleMemChapter > 0) entry.bibleMemChapter.toString() else "1") }
    var bibleMemVerse by remember { mutableStateOf(entry.bibleMemVerse) }

    // Christian Literature
    var bookTitle by remember { mutableStateOf(entry.bookTitle) }
    var bookAuthor by remember { mutableStateOf(entry.bookAuthor) }
    var startPage by remember { mutableStateOf(if (entry.startPage > 0) entry.startPage.toString() else "1") }
    var endPage by remember { mutableStateOf(if (entry.endPage > 0) entry.endPage.toString() else "10") }
    var isBookCompleted by remember { mutableStateOf(entry.isBookCompleted) }

    // Fasting
    var fastingType by remember { mutableStateOf(entry.fastingType.ifBlank { "Complete Fast" }) }
    var fastingStartDateIso by remember { mutableStateOf(entry.fastingStartDateIso.ifBlank { entry.dateIso }) }
    var fastingEndDateIso by remember { mutableStateOf(entry.fastingEndDateIso.ifBlank { entry.dateIso }) }
    var fastingPurpose by remember { mutableStateOf(entry.fastingPurpose) }

    // Giving
    var givingAmt by remember { mutableStateOf(if (entry.givingAmount > 0) entry.givingAmount.toString() else "0") }
    var givingIncomeRef by remember { mutableStateOf(if (entry.givingIncomeReference > 0) entry.givingIncomeReference.toString() else "0") }
    var givingType by remember { mutableStateOf(entry.givingType.ifBlank { "Tithe" }) }

    // Proclamation
    var proclamationTopic by remember { mutableStateOf(entry.proclamationTopic) }
    var proclamationCount by remember { mutableStateOf(if (entry.proclamationCount > 0) entry.proclamationCount.toString() else "10") }
    var proclamationTarget by remember { mutableStateOf(if (entry.proclamationTarget > 0) entry.proclamationTarget.toString() else "50") }

    // Discipleship
    var discipleName by remember { mutableStateOf(entry.prayerParticipantNames) }
    var discipleshipTopicsCovered by remember { mutableStateOf(entry.areasDiscussed) }

    // Auto-calculate duration from start & end time
    val calculatedDurationSeconds = remember(startTimeIso, endTimeIso) {
        try {
            val sParts = startTimeIso.split(":")
            val eParts = endTimeIso.split(":")
            val sMin = sParts[0].toInt() * 60 + sParts[1].toInt()
            val eMin = eParts[0].toInt() * 60 + eParts[1].toInt()
            val diff = if (eMin >= sMin) eMin - sMin else (24 * 60 - sMin + eMin)
            diff.toLong() * 60L
        } catch (_: Exception) {
            entry.durationSeconds
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val titleDomain = if (entry.domainId == "ddewg") strings.ddewgAbbr else strings.getDomainTitleById(entry.domainId)
            Text("${strings.editPastRecord}: $titleDomain")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Selector
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(dateIso, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                // Time Span & Duration for timed domains
                if (entry.domainId != "giving" && entry.domainId != "fasting") {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(strings.timeSpanLabel, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.weight(1f).clickable { showStartTimePicker = true }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(strings.startTime, style = MaterialTheme.typography.labelSmall)
                                        Text(startTimeIso, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.weight(1f).clickable { showEndTimePicker = true }
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(strings.stopTime, style = MaterialTheme.typography.labelSmall)
                                        Text(endTimeIso, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            val autoMins = (calculatedDurationSeconds / 60).coerceAtLeast(1)
                            val displayDur = if (autoMins >= 60) "${autoMins / 60} ${strings.hoursUnit} ${autoMins % 60} ${strings.minutesUnit}" else "$autoMins ${strings.minutesUnit}"
                            Text(
                                text = "${strings.duration} (Auto-calculated): $displayDur",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess
                            )
                        }
                    }
                }

                // Domain specific inputs
                when (entry.domainId) {
                    "prayer_alone" -> {
                        OutlinedTextField(
                            value = prayerType,
                            onValueChange = { prayerType = it },
                            label = { Text(strings.prayerType) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (prayerType.equals("15-Minute Retreat", ignoreCase = true) || prayerType.contains("15")) {
                            Text(strings.periodOfDay, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("Morning", "Noon", "Evening", "Night").forEach { period ->
                                    FilterChip(
                                        selected = retreatPeriodOfDay.equals(period, ignoreCase = true),
                                        onClick = { retreatPeriodOfDay = period },
                                        label = { Text(period, fontSize = 11.sp) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startPrayerTopicNum,
                                onValueChange = { startPrayerTopicNum = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.startTopicNumber) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endPrayerTopicNum,
                                onValueChange = { endPrayerTopicNum = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.endTopicNumber) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        val sTopic = startPrayerTopicNum.toIntOrNull() ?: 0
                        val eTopic = endPrayerTopicNum.toIntOrNull() ?: 0
                        if (sTopic > 0 && eTopic >= sTopic) {
                            val autoTopics = eTopic - sTopic + 1
                            Text(
                                text = strings.totalTopicsAutoCalculated.format(autoTopics),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = StatusSuccess
                            )
                        } else {
                            OutlinedTextField(
                                value = prayerTopicsCount,
                                onValueChange = { prayerTopicsCount = it },
                                label = { Text(strings.numTopicsRecorded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    "prayer_with_others" -> {
                        OutlinedTextField(
                            value = prayerType,
                            onValueChange = { prayerType = it },
                            label = { Text(strings.prayerType) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = prayerParticipantsCount,
                            onValueChange = { prayerParticipantsCount = it },
                            label = { Text(strings.participantsCount) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "ddewg" -> {
                        OutlinedTextField(
                            value = ddewgInspirationText,
                            onValueChange = { ddewgInspirationText = it },
                            label = { Text(strings.ddewgInspirationPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "bible_reading" -> {
                        OutlinedTextField(
                            value = bibleBook,
                            onValueChange = { bibleBook = it },
                            label = { Text(strings.selectBibleBook) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startChapter,
                                onValueChange = { startChapter = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.startChapter) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endChapter,
                                onValueChange = { endChapter = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.endChapter) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    "bible_mem", "bible_memory" -> {
                        OutlinedTextField(
                            value = bibleMemBook,
                            onValueChange = { bibleMemBook = it },
                            label = { Text(strings.selectBibleBook) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bibleMemChapter,
                                onValueChange = { bibleMemChapter = it },
                                label = { Text(strings.startChapter) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = bibleMemVerse,
                                onValueChange = { bibleMemVerse = it },
                                label = { Text(strings.versesPrompt) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    "christian_lit", "christian_lit_reading", "christian_lit_mem", "christian_lit_memory" -> {
                        OutlinedTextField(
                            value = bookTitle,
                            onValueChange = { bookTitle = it },
                            label = { Text(strings.bookTitle) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = bookAuthor,
                            onValueChange = { bookAuthor = it },
                            label = { Text(strings.author) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startPage,
                                onValueChange = { startPage = it },
                                label = { Text(strings.startPageLabel) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endPage,
                                onValueChange = { endPage = it },
                                label = { Text(strings.endPageLabel) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Mark Book as Completed", style = MaterialTheme.typography.bodyMedium)
                            Switch(checked = isBookCompleted, onCheckedChange = { isBookCompleted = it })
                        }
                    }

                    "fasting" -> {
                        OutlinedTextField(
                            value = fastingType,
                            onValueChange = { fastingType = it },
                            label = { Text(strings.fastingTypePrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f).clickable { showFastingStartDatePicker = true }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(strings.startDateLabel, style = MaterialTheme.typography.labelSmall)
                                    Text(fastingStartDateIso, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.weight(1f).clickable { showFastingEndDatePicker = true }
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(strings.endDateLabel, style = MaterialTheme.typography.labelSmall)
                                    Text(fastingEndDateIso, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = fastingPurpose,
                            onValueChange = { fastingPurpose = it },
                            label = { Text(strings.purpose) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "giving" -> {
                        OutlinedTextField(
                            value = givingAmt,
                            onValueChange = { givingAmt = it },
                            label = { Text(strings.givingAmountLabel) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = givingIncomeRef,
                            onValueChange = { givingIncomeRef = it },
                            label = { Text(strings.incomeReference) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = givingType,
                            onValueChange = { givingType = it },
                            label = { Text(strings.givingTypePlaceholder) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "making_disciples", "discipleship" -> {
                        OutlinedTextField(
                            value = discipleName,
                            onValueChange = { discipleName = it },
                            label = { Text(strings.discipleName) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = discipleshipTopicsCovered,
                            onValueChange = { discipleshipTopicsCovered = it },
                            label = { Text(strings.topicsCovered) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "proclamation_importunity" -> {
                        OutlinedTextField(
                            value = proclamationTopic,
                            onValueChange = { proclamationTopic = it },
                            label = { Text(strings.topicLabel) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = proclamationCount,
                                onValueChange = { proclamationCount = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.proclamationsMade) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = proclamationTarget,
                                onValueChange = { proclamationTarget = it.filter { ch -> ch.isDigit() } },
                                label = { Text(strings.targetProclamations) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.activityNotesPrompt) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val sPg = startPage.toIntOrNull() ?: entry.startPage
                val ePg = endPage.toIntOrNull() ?: entry.endPage
                val pRead = (ePg - sPg + 1).coerceAtLeast(1)

                val sCh = startChapter.toIntOrNull() ?: entry.startChapter
                val eCh = endChapter.toIntOrNull() ?: entry.endChapter
                val calcCh = (eCh - sCh + 1).coerceAtLeast(1)

                val sTopic = startPrayerTopicNum.toIntOrNull() ?: 0
                val eTopic = endPrayerTopicNum.toIntOrNull() ?: 0
                val calcTopics = if (sTopic > 0 && eTopic >= sTopic) (eTopic - sTopic + 1) else (prayerTopicsCount.toIntOrNull() ?: entry.prayerTopicsCount)

                val fastingDays = try {
                    val s = java.time.LocalDate.parse(fastingStartDateIso)
                    val e = java.time.LocalDate.parse(fastingEndDateIso)
                    val d = java.time.temporal.ChronoUnit.DAYS.between(s, e) + 1
                    if (d >= 1) d.toInt() else 1
                } catch (_: Exception) {
                    entry.fastingDaysCount
                }

                val updated = entry.copy(
                    dateIso = dateIso,
                    notes = notes,
                    startTimeIso = startTimeIso,
                    endTimeIso = endTimeIso,
                    durationSeconds = if (entry.domainId == "giving" || entry.domainId == "fasting") 0L else calculatedDurationSeconds,
                    bookTitle = bookTitle,
                    bookAuthor = bookAuthor,
                    startPage = sPg,
                    endPage = ePg,
                    pagesRead = if (entry.domainId in listOf("christian_lit", "christian_lit_reading")) pRead else entry.pagesRead,
                    pagesMemorized = if (entry.domainId in listOf("christian_lit_mem", "christian_lit_memory")) pRead else entry.pagesMemorized,
                    isBookCompleted = isBookCompleted,
                    bibleBook = bibleBook,
                    startChapter = sCh,
                    endChapter = eCh,
                    chaptersCount = calcCh,
                    bibleMemBook = bibleMemBook,
                    bibleMemChapter = bibleMemChapter.toIntOrNull() ?: entry.bibleMemChapter,
                    bibleMemVerse = bibleMemVerse,
                    prayerType = prayerType,
                    startPrayerTopicNumber = sTopic,
                    endPrayerTopicNumber = eTopic,
                    prayerTopicsCount = calcTopics,
                    retreatPeriodOfDay = retreatPeriodOfDay,
                    prayerParticipantsCount = prayerParticipantsCount.toIntOrNull() ?: entry.prayerParticipantsCount,
                    reflection = ddewgInspirationText,
                    fastingType = fastingType,
                    fastingStartDateIso = fastingStartDateIso,
                    fastingEndDateIso = fastingEndDateIso,
                    fastingDaysCount = fastingDays,
                    fastingPurpose = fastingPurpose,
                    givingAmount = givingAmt.toDoubleOrNull() ?: entry.givingAmount,
                    givingIncomeReference = givingIncomeRef.toDoubleOrNull() ?: entry.givingIncomeReference,
                    givingType = givingType,
                    proclamationTopic = proclamationTopic,
                    proclamationCount = proclamationCount.toIntOrNull() ?: entry.proclamationCount,
                    proclamationTarget = proclamationTarget.toIntOrNull() ?: entry.proclamationTarget,
                    prayerParticipantNames = discipleName,
                    areasDiscussed = discipleshipTopicsCovered,
                    updatedAtMs = System.currentTimeMillis()
                )
                onConfirm(updated)
            }) {
                Text(strings.saveChanges)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateIso = dateIso,
            onDateSelected = { dateIso = it },
            onDismiss = { showDatePicker = false }
        )
    }
    if (showStartTimePicker) {
        AppTimePickerDialog(
            initialTime = startTimeIso,
            onTimeSelected = { startTimeIso = it },
            onDismiss = { showStartTimePicker = false }
        )
    }
    if (showEndTimePicker) {
        AppTimePickerDialog(
            initialTime = endTimeIso,
            onTimeSelected = { endTimeIso = it },
            onDismiss = { showEndTimePicker = false }
        )
    }
    if (showFastingStartDatePicker) {
        AppDatePickerDialog(
            initialDateIso = fastingStartDateIso,
            onDateSelected = { fastingStartDateIso = it },
            onDismiss = { showFastingStartDatePicker = false }
        )
    }
    if (showFastingEndDatePicker) {
        AppDatePickerDialog(
            initialDateIso = fastingEndDateIso,
            onDateSelected = { fastingEndDateIso = it },
            onDismiss = { showFastingEndDatePicker = false }
        )
    }
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
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
            }
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

private fun formatStatsDuration(totalSeconds: Long, isFrench: Boolean = false): String {
    val totalMinutes = totalSeconds / 60
    if (totalMinutes < 60) {
        val minUnit = if (isFrench) "min" else "mins"
        return "$totalMinutes $minUnit"
    }
    val hours = totalMinutes / 60
    val remainingMinutes = totalMinutes % 60
    val hrUnit = if (isFrench) (if (hours > 1) "heures" else "heure") else (if (hours > 1) "hrs" else "hr")
    val minUnit = if (isFrench) "min" else "mins"
    return if (remainingMinutes > 0) {
        "$hours $hrUnit $remainingMinutes $minUnit"
    } else {
        "$hours $hrUnit"
    }
}


