package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.core.localization.EnglishStrings
import com.example.data.local.entities.ProclamationTopicEntity
import com.example.ui.theme.*
import com.example.ui.viewmodels.ProclamationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProclamationScreen(
    viewModel: ProclamationViewModel,
    strings: AppStrings = EnglishStrings,
    onNavigateBack: () -> Unit
) {
    val topics by viewModel.topicsFlow.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val topicText by viewModel.topicText.collectAsState()
    val counter by viewModel.counter.collectAsState()
    val targetCount by viewModel.targetCount.collectAsState()
    val elapsedSeconds by viewModel.elapsedSeconds.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val reflection by viewModel.reflection.collectAsState()

    var showEditCounterDialog by remember { mutableStateOf(false) }
    var showTargetDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.proclamationTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Importunate Prayer & Faith Proclamation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("proclamation_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }, modifier = Modifier.testTag("proclamation_info_button")) {
                        Icon(Icons.Default.Info, contentDescription = "Spiritual Guidelines")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Scriptural Motto Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = PrimaryBlue.copy(alpha = 0.08f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PrimaryBlue.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = PrimaryBlue
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\"Men ought always to pray, and not to faint.\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryBlue
                            )
                            Text(
                                text = "Luke 18:1 • Proclaim until total victory is obtained",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 1. Enter Prayer Topic / Proclamation
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = strings.topicLabel,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )

                        OutlinedTextField(
                            value = topicText,
                            onValueChange = { viewModel.setTopicText(it) },
                            placeholder = { Text(strings.enterPrayerTopicOrProclamation) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("proclamation_topic_input"),
                            singleLine = false,
                            maxLines = 3,
                            trailingIcon = {
                                if (topicText.isNotBlank()) {
                                    IconButton(onClick = { viewModel.setTopicText("") }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear topic")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Suggestions Row
                        Text(
                            text = "Quick Scriptural Proclamations:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(viewModel.sampleSuggestions) { suggestion ->
                                SuggestionChip(
                                    onClick = {
                                        viewModel.setTopicText(suggestion)
                                    },
                                    label = {
                                        Text(
                                            text = suggestion,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                    )
                                )
                            }
                        }

                        // Saved Topics Selector
                        if (topics.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Text(
                                text = "Your Saved Prayer Topics:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(topics) { item ->
                                    val isSelected = selectedTopic?.id == item.id || topicText == item.topic
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.selectTopic(item) },
                                        label = {
                                            Text(
                                                text = "${item.topic} (${item.cumulativeCount})",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else null,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Interactive Counter & Live Timer Area
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Live Timer Header Bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Live Chronometer
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = if (isTimerRunning) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                val mins = elapsedSeconds / 60
                                val secs = elapsedSeconds % 60
                                val formattedTime = String.format("%02d:%02d", mins, secs)
                                Text(
                                    text = formattedTime,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTimerRunning) PrimaryBlue else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Timer Play/Pause Controls
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!isTimerRunning) {
                                    FilledTonalIconButton(
                                        onClick = { viewModel.startTimer() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("proclamation_timer_play")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Timer", modifier = Modifier.size(20.dp))
                                    }
                                } else {
                                    FilledTonalIconButton(
                                        onClick = { viewModel.pauseTimer() },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("proclamation_timer_pause")
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = "Pause Timer", modifier = Modifier.size(20.dp))
                                    }
                                }

                                IconButton(
                                    onClick = { viewModel.resetTimer() },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = "Reset Timer", modifier = Modifier.size(20.dp))
                                }
                            }
                        }

                        // Circular / Glow Counter Visualizer
                        Box(
                            modifier = Modifier
                                .size(190.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            PrimaryBlue.copy(alpha = 0.15f),
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                        )
                                    )
                                )
                                .border(
                                    width = 3.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            PrimaryBlue,
                                            AccentPurple,
                                            SecondaryBlue,
                                            PrimaryBlue
                                        )
                                    ),
                                    shape = CircleShape
                                )
                                .clickable { showEditCounterDialog = true }
                                .testTag("proclamation_counter_display"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = counter.toString(),
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontSize = 54.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = PrimaryBlue
                                )
                                Text(
                                    text = "Target: $targetCount",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit count",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = strings.edit,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Target Progress Bar
                        val progress = if (targetCount > 0) (counter.toFloat() / targetCount.toFloat()).coerceIn(0f, 1f) else 0f
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$counter / $targetCount Proclamations",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (progress >= 1f) Color(0xFF2E7D32) else PrimaryBlue
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (progress >= 1f) Color(0xFF2E7D32) else PrimaryBlue,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }

                        // Main Big Proclaim Button
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.94f else 1f,
                            animationSpec = tween(durationMillis = 100),
                            label = "button_scale"
                        )

                        Button(
                            onClick = { viewModel.incrementCounter(1) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .scale(scale)
                                .testTag("tap_to_proclaim_button"),
                            shape = RoundedCornerShape(16.dp),
                            interactionSource = interactionSource,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PrimaryBlue
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = null,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = strings.tapToProclaim,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quick Increment & Decrement Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Decrement -1
                            OutlinedIconButton(
                                onClick = { viewModel.decrementCounter() },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("proclamation_minus_1"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("-1", fontWeight = FontWeight.Bold)
                            }

                            // +5, +10, +25, +50 Quick Chips
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(5, 10, 25, 50).forEach { amt ->
                                    FilledTonalButton(
                                        onClick = { viewModel.incrementCounter(amt) },
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("proclamation_plus_$amt")
                                    ) {
                                        Text("+$amt", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Reset
                            IconButton(
                                onClick = { viewModel.resetCounter() },
                                modifier = Modifier.size(44.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Count")
                            }
                        }
                    }
                }
            }

            // 3. Notes & Prophetic Impressions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Spiritual Notes & Impressions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryBlue
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { viewModel.setNotes(it) },
                            label = { Text("Session Notes (e.g. Specific breakthrough, scriptures)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("proclamation_notes_input"),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = reflection,
                            onValueChange = { viewModel.setReflection(it) },
                            label = { Text("Prophetic Burdens / Divine Impressions") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("proclamation_reflection_input"),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // 4. Save Session Button
            item {
                Button(
                    onClick = {
                        if (counter == 0 && elapsedSeconds == 0L) {
                            showSaveConfirmDialog = true
                        } else {
                            viewModel.saveSession(onSuccess = { onNavigateBack() })
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("save_proclamation_session_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Proclamation Session",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 5. Topics History & Lifetime Stats
            if (topics.isNotEmpty()) {
                item {
                    Text(
                        text = "Saved Prayer Topics & Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(topics) { topicItem ->
                    TopicHistoryCard(
                        topic = topicItem,
                        onSelect = { viewModel.selectTopic(topicItem) },
                        onDelete = { viewModel.deleteTopic(topicItem) }
                    )
                }
            }
        }
    }

    // Dialog: Edit Counter Starting Value
    if (showEditCounterDialog) {
        var inputVal by remember { mutableStateOf(counter.toString()) }
        var targetVal by remember { mutableStateOf(targetCount.toString()) }

        AlertDialog(
            onDismissRequest = { showEditCounterDialog = false },
            title = {
                Text(
                    text = strings.setCounterStartingValue,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "If you have already proclaimed or prayed before logging, you can set your counter to any starting number (e.g., 50).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputVal,
                        onValueChange = { inputVal = it.filter { c -> c.isDigit() } },
                        label = { Text(strings.currentCount) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_counter_input_field"),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = targetVal,
                        onValueChange = { targetVal = it.filter { c -> c.isDigit() } },
                        label = { Text(strings.targetProclamations) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputVal.toIntOrNull() ?: 0
                        val tgt = targetVal.toIntOrNull() ?: 100
                        viewModel.setCounterValue(num)
                        viewModel.setTargetCount(tgt)
                        showEditCounterDialog = false
                    },
                    modifier = Modifier.testTag("confirm_edit_counter_button")
                ) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCounterDialog = false }) {
                    Text(strings.cancelTimer)
                }
            }
        )
    }

    // Dialog: Spiritual Guidelines Info
    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Proclamation & Importunity", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "1. Proclamation of Faith",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Speaking God's word aloud repeatedly until faith fills the spirit and spiritual opposition is broken (Hebrews 4:14, Revelation 12:11).",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "2. Importunity in Prayer",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "Persistent, shameless, and untiring prayer for a specific topic until the answer comes (Luke 11:8, Luke 18:1-8).",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3. Practical Tips",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryBlue
                    )
                    Text(
                        text = "• Set clear biblical topics.\n• Use the counter to maintain focused repetition.\n• Let your prayers be audible, firm, and filled with the Holy Spirit.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("Understood")
                }
            }
        )
    }

    // Dialog: Save Warning if 0
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = { Text("Log Empty Session?") },
            text = { Text("You currently have 0 repetitions and 0 minutes recorded. Would you still like to log this session?") },
            confirmButton = {
                Button(onClick = {
                    showSaveConfirmDialog = false
                    viewModel.saveSession(onSuccess = { onNavigateBack() })
                }) {
                    Text("Save Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TopicHistoryCard(
    topic: ProclamationTopicEntity,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.topic,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Total: ${topic.cumulativeCount} proclamations",
                        style = MaterialTheme.typography.labelSmall,
                        color = PrimaryBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Time: ${topic.totalDurationSeconds / 60} mins",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (topic.lastPracticedIso.isNotBlank()) {
                        Text(
                            text = topic.lastPracticedIso,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onSelect) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = "Resume Proclaiming",
                        tint = PrimaryBlue
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete topic",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
