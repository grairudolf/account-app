package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.RestartAlt
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
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.core.localization.FrenchStrings
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class FastingSelectionMode {
    RANGE,
    SPECIFIC_DAYS
}

/**
 * Modern, interactive calendar component for selecting fasting days:
 * - Either by selecting a date range (Start Day to End Day)
 * - Or picking individual specific days
 * - Selecting fasting type (Complete Fast vs Partial Fast)
 */
@Composable
fun FastingCalendarPicker(
    strings: AppStrings,
    selectedDates: Set<LocalDate>,
    onDatesChanged: (Set<LocalDate>) -> Unit,
    fastingType: String, // "Complete Fast" or "Partial Fast"
    onFastingTypeChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialMonth: YearMonth = YearMonth.now()
) {
    val isFrench = strings is FrenchStrings
    var currentMonth by remember { mutableStateOf(initialMonth) }
    var selectionMode by remember { mutableStateOf(FastingSelectionMode.RANGE) }

    // For range selection
    var rangeStart by remember(selectedDates) {
        mutableStateOf(selectedDates.minOrNull())
    }
    var rangeEnd by remember(selectedDates) {
        mutableStateOf(if (selectedDates.size > 1) selectedDates.maxOrNull() else null)
    }

    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value // 1 = Monday, 7 = Sunday
    // Adjust offset so Monday is 0
    val startOffset = (firstDayOfWeek - 1) % 7

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header with Fasting Type Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = if (isFrench) "Calendrier de Jeûne" else "Fasting Calendar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Badge of selected days
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (isFrench) "${selectedDates.size} jour(s)" else "${selectedDates.size} day(s)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Fasting Type Pills: Complete vs Partial
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = if (isFrench) "Type de Jeûne" else "Fasting Type",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val completeLabel = if (isFrench) "Jeûne Complet" else "Complete Fast"
                    val partialLabel = if (isFrench) "Jeûne Partiel / Daniel" else "Partial / Daniel Fast"

                    FilterChip(
                        selected = fastingType.contains("Complete", ignoreCase = true) || fastingType.equals("COMPLETE", ignoreCase = true),
                        onClick = { onFastingTypeChanged("Complete Fast") },
                        label = { Text(completeLabel, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (fastingType.contains("Complete", ignoreCase = true)) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = fastingType.contains("Partial", ignoreCase = true) || fastingType.equals("PARTIAL", ignoreCase = true),
                        onClick = { onFastingTypeChanged("Partial Fast") },
                        label = { Text(partialLabel, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (fastingType.contains("Partial", ignoreCase = true)) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        } else null,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Selection Mode Tabs: Range of Days vs Specific Days
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val rangeActive = selectionMode == FastingSelectionMode.RANGE
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (rangeActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                selectionMode = FastingSelectionMode.RANGE
                                if (selectedDates.isNotEmpty()) {
                                    rangeStart = selectedDates.minOrNull()
                                    rangeEnd = selectedDates.maxOrNull()
                                }
                            }
                    ) {
                        Text(
                            text = if (isFrench) "Plage de dates" else "Date Range",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (rangeActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (rangeActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    val specificActive = selectionMode == FastingSelectionMode.SPECIFIC_DAYS
                    Surface(
                        shape = RoundedCornerShape(9.dp),
                        color = if (specificActive) MaterialTheme.colorScheme.primary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectionMode = FastingSelectionMode.SPECIFIC_DAYS }
                    ) {
                        Text(
                            text = if (isFrench) "Jours particuliers" else "Pick Specific Days",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (specificActive) FontWeight.Bold else FontWeight.Medium,
                            color = if (specificActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }

            // Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { currentMonth = currentMonth.minusMonths(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month", modifier = Modifier.size(18.dp))
                }

                val locale = if (isFrench) Locale.FRENCH else Locale.ENGLISH
                val monthLabel = currentMonth.month.getDisplayName(TextStyle.FULL, locale).replaceFirstChar { it.uppercase() }
                Text(
                    text = "$monthLabel ${currentMonth.year}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = { currentMonth = currentMonth.plusMonths(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month", modifier = Modifier.size(18.dp))
                }
            }

            // Days of week header (Mon - Sun)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val dayNames = if (isFrench) listOf("L", "M", "M", "J", "V", "S", "D") else listOf("M", "T", "W", "T", "F", "S", "S")
                dayNames.forEach { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Calendar Grid
            val totalCells = startOffset + daysInMonth
            val totalWeeks = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (weekIndex in 0 until totalWeeks) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        for (dayIndex in 0..6) {
                            val cellNumber = weekIndex * 7 + dayIndex
                            val dayNumber = cellNumber - startOffset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val date = currentMonth.atDay(dayNumber)
                                val isSelected = selectedDates.contains(date)
                                val isToday = date == LocalDate.now()
                                val isStart = rangeStart == date
                                val isEnd = rangeEnd == date

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                isStart || isEnd -> MaterialTheme.colorScheme.primary
                                                isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                isToday -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                                else -> Color.Transparent
                                            }
                                        )
                                        .border(
                                            width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            if (selectionMode == FastingSelectionMode.SPECIFIC_DAYS) {
                                                val newSet = selectedDates.toMutableSet()
                                                if (isSelected) newSet.remove(date) else newSet.add(date)
                                                onDatesChanged(newSet)
                                            } else {
                                                // Range selection logic
                                                if (rangeStart == null || (rangeStart != null && rangeEnd != null)) {
                                                    rangeStart = date
                                                    rangeEnd = null
                                                    onDatesChanged(setOf(date))
                                                } else {
                                                    val start = rangeStart!!
                                                    val (effectiveStart, effectiveEnd) = if (date.isBefore(start)) date to start else start to date
                                                    rangeStart = effectiveStart
                                                    rangeEnd = effectiveEnd

                                                    val rangeSet = mutableSetOf<LocalDate>()
                                                    var curr = effectiveStart
                                                    while (!curr.isAfter(effectiveEnd)) {
                                                        rangeSet.add(curr)
                                                        curr = curr.plusDays(1)
                                                    }
                                                    onDatesChanged(rangeSet)
                                                }
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isStart || isEnd -> MaterialTheme.colorScheme.onPrimary
                                            isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                }
            }

            // Quick helpers and summary banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        val today = LocalDate.now()
                        onDatesChanged(setOf(today))
                        rangeStart = today
                        rangeEnd = null
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(if (isFrench) "Aujourd'hui" else "Today", style = MaterialTheme.typography.labelSmall)
                }

                TextButton(
                    onClick = {
                        val today = LocalDate.now()
                        val weekDates = (0..6).map { today.plusDays(it.toLong()) }.toSet()
                        onDatesChanged(weekDates)
                        rangeStart = today
                        rangeEnd = today.plusDays(6)
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(if (isFrench) "7 Jours" else "7 Days", style = MaterialTheme.typography.labelSmall)
                }

                TextButton(
                    onClick = {
                        onDatesChanged(emptySet())
                        rangeStart = null
                        rangeEnd = null
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isFrench) "Effacer" else "Clear",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Informative Summary Card
            if (selectedDates.isNotEmpty()) {
                val sorted = selectedDates.sorted()
                val startStr = sorted.first().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                val endStr = sorted.last().format(DateTimeFormatter.ofPattern("d MMM yyyy"))
                val periodText = if (sorted.size == 1) startStr else "$startStr → $endStr"

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "${selectedDates.size} ${if (selectedDates.size == 1) (if (isFrench) "jour sélectionné" else "day selected") else (if (isFrench) "jours sélectionnés" else "days selected")} ($fastingType)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = periodText,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
