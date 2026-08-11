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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppStrings
import com.example.domain.models.PredefinedDomains
import com.example.ui.theme.*
import com.example.ui.viewmodels.GoalWithProgress
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun GoalsScreen(
    strings: AppStrings,
    goalsWithProgress: List<GoalWithProgress>,
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit,
    onAddGoal: (userId: String, domainId: String, title: String, target: Double, unit: String, freq: String, startDate: String) -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    var showAddGoalDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = AccentPurple.copy(alpha = 0.05f),
                radius = w * 0.55f,
                center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.2f)
            )
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.04f),
                radius = w * 0.5f,
                center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.8f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Frequency Selector Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "DAILY", "WEEKLY", "MONTHLY").forEach { freq ->
                val selected = selectedFrequency == freq
                FilterChip(
                    selected = selected,
                    onClick = { onFrequencySelected(freq) },
                    label = { Text(freq) },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryBlue,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.testTag("filter_freq_$freq")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.spiritualGoals,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            FloatingActionButton(
                onClick = { showAddGoalDialog = true },
                containerColor = PrimaryBlue,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .size(44.dp)
                    .testTag("add_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.addGoal)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (goalsWithProgress.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = strings.noGoalsFound,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(goalsWithProgress) { goalItem ->
                    GoalCard(
                        goalItem = goalItem,
                        strings = strings,
                        onDelete = { onDeleteGoal(goalItem.goal.id) }
                    )
                }
            }
        }
    }
}

    if (showAddGoalDialog) {
        AddGoalDialog(
            strings = strings,
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { domainId, title, target, unit, freq ->
                val startDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                onAddGoal("guest_user", domainId, title, target, unit, freq, startDate)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
fun GoalCard(
    goalItem: GoalWithProgress,
    strings: AppStrings,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, DividerColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_card_${goalItem.goal.id}")
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LightBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = PrimaryBlue
                        )
                    }
                    Column {
                        Text(
                            text = goalItem.goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${goalItem.goal.frequency} • ${goalItem.goal.domainId.replace("_", " ").uppercase()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_goal_${goalItem.goal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Goal",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Current: ${goalItem.currentProgress.toInt()} ${goalItem.goal.unit}  •  Target: ${goalItem.goal.targetValue.toInt()} ${goalItem.goal.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                val isAchieved = goalItem.currentProgress >= goalItem.goal.targetValue
                val statusText = if (isAchieved) "Achieved" else if (goalItem.progressPercentage >= 50) "On Track" else "Needs Focus"
                val statusBg = if (isAchieved) AccentMintContainer else if (goalItem.progressPercentage >= 50) LightBlueContainer else StreakGoldContainer
                val statusColor = if (isAchieved) StatusSuccess else if (goalItem.progressPercentage >= 50) PrimaryBlue else StreakGoldDark

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (goalItem.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = if (goalItem.currentProgress >= goalItem.goal.targetValue) StatusSuccess else PrimaryBlue,
                trackColor = LightBlueContainer
            )

            // Goal vs Progress Comparison Text Delta
            val diff = (goalItem.goal.targetValue - goalItem.currentProgress).toInt()
            val comparisonNote = if (diff <= 0) {
                "Goal achieved! Exceeded target by ${-diff} ${goalItem.goal.unit}."
            } else {
                "$diff ${goalItem.goal.unit} remaining to reach your target for this ${goalItem.goal.frequency.lowercase()} period."
            }

            Text(
                text = comparisonNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AddGoalDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (domainId: String, title: String, target: Double, unit: String, freq: String) -> Unit
) {
    var selectedDomain by remember { mutableStateOf("ddewg") }
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("30") }
    var unit by remember { mutableStateOf("Minutes") }
    var frequency by remember { mutableStateOf("DAILY") }

    val domainOptions = listOf(
        "ddewg" to "DDEWG (Daily Encounter)",
        "bible_reading" to "Bible Reading",
        "prayer_alone" to "Prayer Alone",
        "prayer_with_others" to "Prayer With Others",
        "fasting" to "Fasting",
        "giving" to "Giving & Tithes",
        "christian_lit" to "Christian Literature",
        "soul_winning" to "Soul Winning"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addGoal) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Domain:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                
                // Domain radio/chip list
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    items(domainOptions) { (id, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDomain = id
                                    if (title.isBlank()) title = "Goal: $label"
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            RadioButton(
                                selected = selectedDomain == id,
                                onClick = {
                                    selectedDomain = id
                                    if (title.isBlank()) title = "Goal: $label"
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = label, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_goal_title_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target Value") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_goal_target_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit (Minutes, Chapters, Souls, USD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_goal_unit_input"),
                    singleLine = true
                )
                Text("Target Period:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("DAILY", "WEEKLY", "MONTHLY").forEach { freq ->
                        FilterChip(
                            selected = frequency == freq,
                            onClick = { frequency = freq },
                            label = { Text(freq, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tVal = target.toDoubleOrNull() ?: 1.0
                    val finalTitle = title.ifBlank { "Spiritual Goal" }
                    onConfirm(selectedDomain, finalTitle, tVal, unit, frequency)
                },
                modifier = Modifier.testTag("confirm_add_goal_button")
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
