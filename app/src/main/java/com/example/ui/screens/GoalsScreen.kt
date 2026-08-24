package com.example.ui.screens

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppStrings
import com.example.core.util.HapticHelper
import com.example.domain.models.PredefinedDomains
import com.example.ui.theme.*
import com.example.ui.viewmodels.GoalWithProgress

import com.example.ui.components.AppTimePickerDialog
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive

@Composable
fun GoalsScreen(
    strings: AppStrings,
    goalsWithProgress: List<GoalWithProgress>,
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit,
    onAddGoal: (userId: String, domainId: String, title: String, target: Double, unit: String, freq: String, startDate: String, fastingType: String, periodDays: Int, isDailyReminderEnabled: Boolean, reminderTimeIso: String) -> Unit,
    onDeleteGoal: (String) -> Unit
) {
    val context = LocalContext.current
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
                listOf("ALL" to strings.allPeriod, "DAILY" to strings.daily, "WEEKLY" to strings.weekly, "MONTHLY" to strings.monthly).forEach { (freqKey, freqLabel) ->
                    val selected = selectedFrequency == freqKey
                    val containerBg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    val borderStroke = if (selected) BorderStroke(0.dp, Color.Transparent) else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = containerBg,
                        border = borderStroke,
                        modifier = Modifier
                            .clickable { onFrequencySelected(freqKey) }
                            .testTag("filter_freq_$freqKey")
                    ) {
                        Text(
                            text = freqLabel.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
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
                    containerColor = MaterialTheme.colorScheme.primary,
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
                            onDelete = {
                                HapticHelper.vibrateWarning(context)
                                onDeleteGoal(goalItem.goal.id)
                            }
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
            onConfirm = { domainId, title, target, unit, freq, fastingType, periodDays, isReminder, reminderTime ->
                onAddGoal("guest_user", domainId, title, target, unit, freq, java.time.LocalDate.now().toString(), fastingType, periodDays, isReminder, reminderTime)
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_card_${goalItem.goal.id}")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = goalItem.goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${strings.getDomainTitleById(goalItem.goal.domainId)} • ${goalItem.goal.frequency}",
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
                        contentDescription = strings.delete,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "${goalItem.currentProgress.toInt()} / ${goalItem.goal.targetValue.toInt()} ${goalItem.goal.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (goalItem.goal.domainId == "fasting" && goalItem.goal.fastingType.isNotBlank()) {
                        Text(
                            text = "Type: ${goalItem.goal.fastingType.lowercase().replaceFirstChar { it.uppercase() }} Fast",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (goalItem.goal.isDailyReminderEnabled) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Daily Reminder: ${goalItem.goal.reminderTimeIso}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                val isAchieved = goalItem.currentProgress >= goalItem.goal.targetValue
                val statusText = if (isAchieved) "Achieved" else if (goalItem.progressPercentage >= 50) "On Track" else "Needs Focus"
                val statusBg = if (isAchieved) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                val statusColor = if (isAchieved) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = statusBg
                ) {
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { (goalItem.progressPercentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            val diff = (goalItem.goal.targetValue - goalItem.currentProgress).toInt()
            val comparisonNote = if (diff <= 0) {
                String.format(strings.goalAchievedExceeded, (-diff).toString(), goalItem.goal.unit)
            } else {
                String.format(strings.goalRemainingProgress, diff.toString(), goalItem.goal.unit, goalItem.goal.frequency.lowercase())
            }

            Text(
                text = comparisonNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (domainId: String, title: String, target: Double, unit: String, freq: String, fastingType: String, periodDays: Int, isReminder: Boolean, reminderTime: String) -> Unit
) {
    var selectedDomain by remember { mutableStateOf("bible_reading") }
    var selectedAspect by remember { mutableStateOf("Chapters Read") }
    var customTitle by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("10") }
    var selectedUnit by remember { mutableStateOf(strings.unitChapters) }
    var frequency by remember { mutableStateOf("DAILY") }

    // Fasting & Reminder Specifics
    var fastingType by remember { mutableStateOf("COMPLETE") } // COMPLETE, PARTIAL, WATER_ONLY
    var periodDaysInput by remember { mutableStateOf("3") }
    var isDailyReminderEnabled by remember { mutableStateOf(false) }
    var reminderTimeIso by remember { mutableStateOf("08:00") }
    var showTimePicker by remember { mutableStateOf(false) }

    val allDomains = remember { PredefinedDomains.ALL }

    val domainAspects = remember(selectedDomain) {
        when (selectedDomain) {
            "bible_reading" -> listOf("Chapters Read", "Complete Whole Bible", "Gospel Study", "Epistles Study", "Custom")
            "ddewg" -> listOf("Daily Dynamic Encounters (DDEWG)", "Morning Watch Encounters", "Consistent Daily Encounters", "Custom")
            "prayer_alone" -> listOf("Secret Place Devotion", "Thanksgiving Topics Recorded", "Request Prayer Topics Recorded", "Intercession Hours", "Prayer Night Vigils", "15-Min Retreats", "Custom")
            "prayer_with_others" -> listOf("Corporate Prayer Sessions", "Prayer Siege Hours", "Family Altar Sessions", "Custom")
            "proclamation_importunity" -> listOf("Proclamation Repetitions", "Topic Breakthrough Target", "Custom")
            "retreats" -> listOf("Spiritual Retreat Hours", "Monthly Retreat Days", "Solitude & Silence", "Custom")
            "christian_lit" -> listOf("Pages Read", "Books Completed", "Custom")
            "christian_lit_mem" -> listOf("Pages Memorized", "Quotes Mastered", "Custom")
            "bible_mem" -> listOf("Verses Memorized", "Chapters Memorized", "Custom")
            "fasting" -> listOf("Fasting Days in Period", "Monthly 3-Day Fast", "Weekly Fast Routine", "Partial / Daniel Fast", "Custom")
            "giving" -> listOf("Monthly Giving Amount (XAF)", "Giving Percentage (10% Tithe)", "First Fruits Pledge (XAF)", "Custom")
            "soul_winning" -> listOf("People Preached To", "Converts Won", "Baptisms Target", "Custom")
            else -> listOf("Discipline Practice", "Custom")
        }
    }

    LaunchedEffect(selectedDomain) {
        selectedAspect = domainAspects.first()
        when (selectedDomain) {
            "bible_reading" -> {
                selectedUnit = strings.unitChapters
                target = "10"
            }
            "ddewg" -> {
                selectedUnit = strings.unitDdewg
                target = "7"
                frequency = "WEEKLY"
            }
            "prayer_alone", "prayer_with_others" -> {
                if (selectedAspect.contains("Thanksgiving")) {
                    selectedUnit = "Thanksgiving Topics"
                    target = "5"
                } else if (selectedAspect.contains("Request")) {
                    selectedUnit = "Request Topics"
                    target = "5"
                } else {
                    selectedUnit = strings.unitMinutes
                    target = "60"
                }
            }
            "proclamation_importunity" -> {
                selectedUnit = strings.proclamationsMade
                target = "100"
            }
            "retreats" -> {
                selectedUnit = strings.unitHours
                target = "24"
                frequency = "MONTHLY"
            }
            "christian_lit", "christian_lit_mem" -> {
                selectedUnit = strings.unitPages
                target = "20"
            }
            "fasting" -> {
                selectedUnit = strings.unitDays
                target = "3"
                frequency = "MONTHLY"
            }
            "giving" -> {
                selectedUnit = "XAF"
                target = "10000"
                frequency = "MONTHLY"
            }
            "soul_winning" -> {
                selectedUnit = strings.unitSouls
                target = "5"
                frequency = "WEEKLY"
            }
            else -> {
                selectedUnit = strings.unitSessions
                target = "1"
            }
        }
    }

    val availableUnits = listOf(
        "XAF",
        "Thanksgiving Topics",
        "Request Topics",
        strings.unitDdewg,
        strings.unitChapters,
        strings.unitPages,
        strings.unitHours,
        strings.unitMinutes,
        strings.unitSessions,
        strings.unitDays,
        strings.unitSouls,
        strings.unitConverts,
        strings.unitPercentage
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addGoal, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Domain Dropdown
                Column {
                    Text(strings.goalDomain, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    var domainExp by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = domainExp, onExpandedChange = { domainExp = !domainExp }) {
                        OutlinedTextField(
                            value = strings.getDomainTitleById(selectedDomain),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.goalDomain) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = domainExp) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(expanded = domainExp, onDismissRequest = { domainExp = false }) {
                            allDomains.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(strings.getDomainTitleById(d.id)) },
                                    onClick = {
                                        selectedDomain = d.id
                                        domainExp = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Goal Aspect / Title Dropdown
                Column {
                    Text(strings.goalAspect, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    var aspectExp by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = aspectExp, onExpandedChange = { aspectExp = !aspectExp }) {
                        OutlinedTextField(
                            value = selectedAspect,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.goalAspect) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = aspectExp) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(expanded = aspectExp, onDismissRequest = { aspectExp = false }) {
                            domainAspects.forEach { aspect ->
                                DropdownMenuItem(
                                    text = { Text(aspect) },
                                    onClick = {
                                        selectedAspect = aspect
                                        aspectExp = false
                                        if (aspect.contains("Thanksgiving")) {
                                            selectedUnit = "Thanksgiving Topics"
                                            target = "5"
                                        } else if (aspect.contains("Request")) {
                                            selectedUnit = "Request Topics"
                                            target = "5"
                                        }
                                    }
                                )
                            }
                        }
                    }
                    if (selectedAspect == "Custom") {
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            label = { Text(strings.customGoalTitlePrompt) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // Fasting Specific Controls
                if (selectedDomain == "fasting") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Fasting Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("COMPLETE" to "Complete Fast", "PARTIAL" to "Partial / Daniel", "WATER_ONLY" to "Water Only").forEach { (typeKey, typeLabel) ->
                                FilterChip(
                                    selected = fastingType == typeKey,
                                    onClick = { fastingType = typeKey },
                                    label = { Text(typeLabel, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                    }
                }

                // Target Value and Unit Dropdowns
                Column {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = target,
                            onValueChange = { target = it },
                            label = { Text(strings.targetValue) },
                            modifier = Modifier.weight(1f).testTag("add_goal_target_input"),
                            singleLine = true
                        )

                        var unitExp by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = unitExp,
                            onExpandedChange = { unitExp = !unitExp },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = selectedUnit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.targetUnit) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExp) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(expanded = unitExp, onDismissRequest = { unitExp = false }) {
                                availableUnits.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            selectedUnit = u
                                            unitExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Target Period / Frequency Dropdown
                Column {
                    Text(strings.targetPeriod, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("DAILY" to strings.daily, "WEEKLY" to strings.weekly, "MONTHLY" to strings.monthly).forEach { (freqKey, freqLabel) ->
                            FilterChip(
                                selected = frequency == freqKey,
                                onClick = { frequency = freqKey },
                                label = { Text(freqLabel, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryBlue,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Daily Reminder Setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Enable Daily Reminder", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("Notification at scheduled time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isDailyReminderEnabled,
                        onCheckedChange = { isDailyReminderEnabled = it }
                    )
                }

                if (isDailyReminderEnabled) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reminder Time:", style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { showTimePicker = true }) {
                            Text(reminderTimeIso, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tVal = target.toDoubleOrNull() ?: 1.0
                    val finalTitle = if (selectedAspect == "Custom") {
                        customTitle.ifBlank { "${strings.getDomainTitleById(selectedDomain)} Goal" }
                    } else {
                        selectedAspect
                    }
                    val periodDays = periodDaysInput.toIntOrNull() ?: 0
                    onConfirm(selectedDomain, finalTitle, tVal, selectedUnit, frequency, fastingType, periodDays, isDailyReminderEnabled, reminderTimeIso)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.testTag("confirm_add_goal_button")
            ) {
                Text(strings.save, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelTimer)
            }
        }
    )

    if (showTimePicker) {
        AppTimePickerDialog(
            initialTime = reminderTimeIso,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { selectedTime ->
                reminderTimeIso = selectedTime
                showTimePicker = false
            }
        )
    }
}
