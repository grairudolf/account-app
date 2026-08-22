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
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onSelectDate: (LocalDate) -> Unit = {},
    onNextMonth: () -> Unit = {},
    onPreviousMonth: () -> Unit = {},
    onGoToToday: () -> Unit = {},
    onUpdateEntry: (AccountabilityEntryEntity) -> Unit = {},
    onDeleteEntry: (String) -> Unit = {}
) {
    var editingEntry by remember { mutableStateOf<AccountabilityEntryEntity?>(null) }
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
                                    text = "${strings.totalTimeWithGod} (${strings.allPeriod.lowercase()}): ${formatStatsDuration(uiState.totalTimeWithGodSeconds, isFrench)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )

                                // Key Domain Badges (DDEWG, Thanksgiving, Requests, 15-min retreats, Bertoua)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
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
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
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
                                            modifier = Modifier.padding(8.dp),
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
                                            modifier = Modifier.padding(8.dp),
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
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
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
                                        Text(strings.totalBibleChapters, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                                            Icon(Icons.Default.Today, contentDescription = "Today", tint = PrimaryBlue)
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
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${strings.allPastRecords} (${allEntries.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(allEntries, key = { "all_${it.id}" }) { entry ->
                        EntryLogCard(
                            entry = entry,
                            strings = strings,
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
                Text(
                    text = strings.getDomainTitleById(entry.domainId).uppercase(),
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
                        text = "${strings.fastingTitle}: $days ${strings.days}" + if (entry.fastingType.isNotBlank()) " (${entry.fastingType})" else "",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentPurple
                    )
                } else if (entry.domainId == "giving") {
                    val gType = if (entry.givingType.isNotBlank()) " (${entry.givingType})" else ""
                    Text(
                        text = "${strings.givingTitle}: $${entry.givingAmount}$gType",
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
                    val topicsStr = if (entry.prayerTopicsCount > 0) " (" + String.format(strings.topicsCountFormat, entry.prayerTopicsCount) + ")" else ""
                    Text(
                        text = "${strings.prayerFocus}: ${entry.prayerType}$topicsStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (entry.notes.isNotBlank()) {
                    Text(
                        text = "${strings.notes}: ${entry.notes}",
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = strings.edit, tint = PrimaryBlue)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = strings.delete, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun EditEntryDialog(
    entry: AccountabilityEntryEntity,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (AccountabilityEntryEntity) -> Unit
) {
    var notes by remember { mutableStateOf(entry.notes) }
    var chapters by remember { mutableStateOf(entry.chaptersCount.toString()) }
    var prayerMins by remember { mutableStateOf((entry.durationSeconds / 60).toString()) }
    var givingAmt by remember { mutableStateOf(entry.givingAmount.toString()) }
    var givingType by remember { mutableStateOf(entry.givingType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.editPastRecord) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(strings.activityNotesPrompt) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (entry.domainId == "giving") {
                    OutlinedTextField(
                        value = givingAmt,
                        onValueChange = { givingAmt = it },
                        label = { Text(strings.givingAmountLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = givingType,
                        onValueChange = { givingType = it },
                        label = { Text(strings.givingTypePlaceholder) },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else if (entry.domainId != "fasting") {
                    OutlinedTextField(
                        value = chapters,
                        onValueChange = { chapters = it },
                        label = { Text(strings.chaptersReadLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = prayerMins,
                        onValueChange = { prayerMins = it },
                        label = { Text(strings.durationMinutesLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val updated = entry.copy(
                    notes = notes,
                    chaptersCount = chapters.toIntOrNull() ?: entry.chaptersCount,
                    durationSeconds = if (entry.domainId == "giving" || entry.domainId == "fasting") 0L else ((prayerMins.toLongOrNull() ?: (entry.durationSeconds / 60)) * 60),
                    givingAmount = givingAmt.toDoubleOrNull() ?: entry.givingAmount,
                    givingType = givingType,
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


