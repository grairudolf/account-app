package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.core.util.HapticHelper
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
    val context = LocalContext.current
    val topics by viewModel.topicsFlow.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val topicText by viewModel.topicText.collectAsState()
    val counter by viewModel.counter.collectAsState()
    val startingCount by viewModel.startingCount.collectAsState()
    val isResumedSession by viewModel.isResumedSession.collectAsState()
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
                                        onClick = { viewModel.resumeTopicSession(item) },
                                        label = {
                                            Text(
                                                text = "${item.topic} (${item.cumulativeCount})",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                        } else {
                                            { Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(14.dp), tint = PrimaryBlue) }
                                        },
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
                        // Resumed Session Status Banner
                        if (isResumedSession && startingCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryBlue.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayCircle,
                                            contentDescription = null,
                                            tint = PrimaryBlue,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val addedInSession = (counter - startingCount).coerceAtLeast(0)
                                        Column {
                                            Text(
                                                text = "Continuing session from $startingCount",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlue
                                            )
                                            Text(
                                                text = "+$addedInSession added in today's session",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = { viewModel.clearResumedSession() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Start at 0", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

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
                                        onClick = {
                                            HapticHelper.vibrateClick(context)
                                            viewModel.startTimer()
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("proclamation_timer_play")
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Start Timer", modifier = Modifier.size(20.dp))
                                    }
                                } else {
                                    FilledTonalIconButton(
                                        onClick = {
                                            HapticHelper.vibrateClick(context)
                                            viewModel.pauseTimer()
                                        },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .testTag("proclamation_timer_pause")
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = "Pause Timer", modifier = Modifier.size(20.dp))
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        HapticHelper.vibrateWarning(context)
                                        viewModel.resetTimer()
                                    },
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
                                .clickable {
                                    HapticHelper.vibrateClick(context)
                                    showEditCounterDialog = true
                                }
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
                            onClick = {
                                val nextCount = counter + 1
                                if (nextCount == targetCount || (nextCount > 0 && nextCount % 50 == 0)) {
                                    HapticHelper.vibrateMilestone(context)
                                } else {
                                    HapticHelper.vibrateProclamationTap(context)
                                }
                                viewModel.incrementCounter(1)
                            },
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
                                onClick = {
                                    HapticHelper.vibrateTick(context)
                                    viewModel.decrementCounter()
                                },
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
                                        onClick = {
                                            val nextCount = counter + amt
                                            if (nextCount >= targetCount && counter < targetCount) {
                                                HapticHelper.vibrateMilestone(context)
                                            } else {
                                                HapticHelper.vibrateHeavyClick(context)
                                            }
                                            viewModel.incrementCounter(amt)
                                        },
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
                                onClick = {
                                    HapticHelper.vibrateWarning(context)
                                    viewModel.resetCounter()
                                },
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
                            HapticHelper.vibrateWarning(context)
                            showSaveConfirmDialog = true
                        } else {
                            HapticHelper.vibrateSuccess(context)
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
                        onResume = { viewModel.resumeTopicSession(topicItem) },
                        onStartFresh = { viewModel.startNewSessionForTopic(topicItem) },
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
        var asStartingBase by remember { mutableStateOf(isResumedSession || startingCount > 0) }

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
                        text = "If you have already proclaimed or prayed before logging, you can set your counter to any number (e.g., 100) and choose whether to continue from this number as your session starting base.",
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { asStartingBase = !asStartingBase }
                            .padding(vertical = 4.dp)
                    ) {
                        Checkbox(
                            checked = asStartingBase,
                            onCheckedChange = { asStartingBase = it },
                            colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Set as session starting baseline (only count additional repetitions toward today's log)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = inputVal.toIntOrNull() ?: 0
                        val tgt = targetVal.toIntOrNull() ?: 100
                        viewModel.setCounterValue(num, asStartingPoint = asStartingBase)
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
    onResume: () -> Unit,
    onStartFresh: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.topic,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PrimaryBlue.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${topic.cumulativeCount} Proclamations",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "${topic.totalDurationSeconds / 60}m logged",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (topic.lastPracticedIso.isNotBlank()) {
                            Text(
                                text = "Last: ${topic.lastPracticedIso}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete topic",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Action Buttons Row: Resume from last count vs Fresh Session
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Continue from ${topic.cumulativeCount}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onStartFresh,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "New (+0)",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
