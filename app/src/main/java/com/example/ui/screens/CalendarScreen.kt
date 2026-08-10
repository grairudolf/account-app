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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun CalendarScreen(
    strings: AppStrings,
    selectedDate: LocalDate,
    currentMonth: YearMonth,
    monthDaysCompletion: List<DayCompletionInfo>,
    selectedDateEntries: List<AccountabilityEntryEntity>,
    onSelectDate: (LocalDate) -> Unit,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onGoToToday: () -> Unit
) {
    val monthTitle = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
    val activeStreakDays = monthDaysCompletion.count { it.entriesCount > 0 }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = StreakGold.copy(alpha = 0.05f),
                radius = w * 0.45f,
                center = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.2f)
            )
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.04f),
                radius = w * 0.5f,
                center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.8f)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        // Streak Summary Banner in Calendar
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = StreakGoldContainer,
                border = BorderStroke(1.dp, StreakGold),
                modifier = Modifier.fillMaxWidth().testTag("calendar_streak_summary")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(StreakGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accountability Streaks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = StreakGoldDark
                        )
                        Text(
                            text = "$activeStreakDays days with logged accountability this month",
                            style = MaterialTheme.typography.bodySmall,
                            color = StreakGoldDark.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Month Selector Header
        item {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onPreviousMonth,
                            modifier = Modifier.testTag("calendar_prev_month")
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                        }

                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Row {
                            IconButton(
                                onClick = onGoToToday,
                                modifier = Modifier.testTag("calendar_today_button")
                            ) {
                                Icon(Icons.Default.Today, contentDescription = "Today")
                            }
                            IconButton(
                                onClick = onNextMonth,
                                modifier = Modifier.testTag("calendar_next_month")
                            ) {
                                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Grid Header (Mon, Tue, Wed...)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days Grid with Yellow Streaks
                    val totalDays = monthDaysCompletion.size
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val rows = (totalDays + 6) / 7
                        for (r in 0 until rows) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                for (c in 0 until 7) {
                                    val index = r * 7 + c
                                    if (index < totalDays) {
                                        val dayInfo = monthDaysCompletion[index]
                                        val dayNum = index + 1
                                        val dateObj = currentMonth.atDay(dayNum)

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(2.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        dayInfo.isSelected -> PrimaryBlue
                                                        dayInfo.entriesCount > 0 -> StreakGoldContainer
                                                        else -> Color.Transparent
                                                    }
                                                )
                                                .clickable { onSelectDate(dateObj) }
                                                .testTag("calendar_day_$dayNum"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = dayNum.toString(),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (dayInfo.isToday || dayInfo.entriesCount > 0) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        dayInfo.isSelected -> Color.White
                                                        dayInfo.entriesCount > 0 -> StreakGoldDark
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    }
                                                )
                                                if (dayInfo.entriesCount > 0 && !dayInfo.isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocalFireDepartment,
                                                        contentDescription = null,
                                                        tint = StreakGold,
                                                        modifier = Modifier.size(10.dp)
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

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(StreakGoldContainer)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Streak / Completed Day (Yellow Fire)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Selected Date Summary Header
        item {
            Text(
                text = "Activities for ${selectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d, yyyy"))}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (selectedDateEntries.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.noActivitiesForDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(selectedDateEntries) { entry ->
                RecentActivityCard(entry = entry, strings = strings, onClick = {})
            }
        }
    }
}
}
