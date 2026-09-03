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
import com.example.core.localization.FrenchStrings
import com.example.core.util.HapticHelper
import com.example.data.local.entities.ProclamationTopicEntity
import com.example.ui.components.AppDatePickerDialog
import com.example.ui.components.AppTimePickerDialog
import androidx.compose.material3.ripple
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.app.Activity
import androidx.core.view.WindowCompat
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
    var showAddTopicDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetTopicAcrossApp by remember { mutableStateOf(false) }
    var isFullscreenCounterOpen by remember { mutableStateOf(false) }

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
                            text = strings.importunatePrayerSubtitle,
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
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
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
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\"Men ought always to pray, and not to faint.\"",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.topicLabel,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            TextButton(
                                onClick = {
                                    if (topicText.isNotBlank()) {
                                        viewModel.savePrayerTopic(
                                            topicText = topicText,
                                            targetCount = targetCount,
                                            currentCount = counter,
                                            onSuccess = {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    if (strings is com.example.core.localization.FrenchStrings) "Sujet de prière enregistré !" else "Prayer topic saved!",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                                onNavigateBack()
                                            }
                                        )
                                    } else {
                                        showAddTopicDialog = true
                                    }
                                },
                                modifier = Modifier.testTag("save_prayer_topic_button")
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (strings is com.example.core.localization.FrenchStrings) "Enregistrer" else "Save Topic", style = MaterialTheme.typography.labelMedium)
                            }
                        }

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
                            text = strings.quickScripturalProclamations,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val suggestionsList = if (strings is FrenchStrings) listOf(
                            "Jésus-Christ est Seigneur sur toutes les nations",
                            "Tout pouvoir m'a été donné dans le ciel et sur la terre",
                            "Mon Dieu pourvoira à tous mes besoins selon sa richesse",
                            "L'Éternel est ma lumière et mon salut, de qui aurais-je crainte?",
                            "Toute arme forgée contre moi sera sans effet",
                            "Par ses meurtrissures nous sommes guéris",
                            "L'Éternel combattra pour vous, et vous garderez le silence",
                            "La moisson est grande, envoie des ouvriers dans ta moisson"
                        ) else viewModel.sampleSuggestions

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(suggestionsList) { suggestion ->
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

                        // Saved Topics Selector Dropdown
                        if (topics.isNotEmpty()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            var topicDropdownExpanded by remember { mutableStateOf(false) }

                            Text(
                                text = strings.yourSavedPrayerTopics,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            ExposedDropdownMenuBox(
                                expanded = topicDropdownExpanded,
                                onExpandedChange = { topicDropdownExpanded = !topicDropdownExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val selectedTopicTitle = selectedTopic?.let { "${it.topic} (${it.cumulativeCount})" }
                                    ?: topics.find { it.topic.equals(topicText, ignoreCase = true) }?.let { "${it.topic} (${it.cumulativeCount})" }
                                    ?: (if (topicText.isNotBlank()) topicText else if (strings is FrenchStrings) "Sélectionner un sujet enregistré..." else "Select saved prayer topic...")

                                OutlinedTextField(
                                    value = selectedTopicTitle,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(if (strings is FrenchStrings) "Sujet de prière enregistré" else "Saved Prayer Topic") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Bookmark,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicDropdownExpanded)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor()
                                        .testTag("saved_topics_dropdown"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                                )

                                ExposedDropdownMenu(
                                    expanded = topicDropdownExpanded,
                                    onDismissRequest = { topicDropdownExpanded = false }
                                ) {
                                    topics.forEach { item ->
                                        val isSelected = selectedTopic?.id == item.id || topicText.equals(item.topic, ignoreCase = true)
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = item.topic,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                                    ) {
                                                        Text(
                                                            text = "${item.cumulativeCount}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                                                    contentDescription = null,
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            onClick = {
                                                viewModel.resumeTopicSession(item)
                                                topicDropdownExpanded = false
                                            }
                                        )
                                    }

                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text(if (strings is FrenchStrings) "+ Nouveau sujet personnalisé" else "+ Enter Custom Topic", color = MaterialTheme.colorScheme.primary) },
                                        leadingIcon = { Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        onClick = {
                                            viewModel.setTopicText("")
                                            topicDropdownExpanded = false
                                        }
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
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
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
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val addedInSession = (counter - startingCount).coerceAtLeast(0)
                                        Column {
                                            Text(
                                                text = String.format(strings.continuingSessionFrom, startingCount),
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = String.format(strings.addedInTodaySession, addedInSession),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = { viewModel.clearResumedSession() },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(strings.startAtZero, style = MaterialTheme.typography.labelSmall)
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
                                    tint = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    color = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Header Action Buttons: Fullscreen Counter + Timer Play/Pause
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        HapticHelper.vibrateClick(context)
                                        isFullscreenCounterOpen = true
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("open_fullscreen_counter_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Fullscreen,
                                        contentDescription = "Open Fullscreen Counter",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

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
                            }
                        }

                        // Giant Circular Tap-to-Proclaim Button & Counter Visualizer
                        val interactionSource = remember { MutableInteractionSource() }
                        val isPressed by interactionSource.collectIsPressedAsState()
                        val scale by animateFloatAsState(
                            targetValue = if (isPressed) 0.94f else 1f,
                            animationSpec = tween(durationMillis = 100),
                            label = "button_scale"
                        )

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                tonalElevation = 8.dp,
                                shadowElevation = 12.dp,
                                modifier = Modifier
                                    .size(220.dp)
                                    .scale(scale)
                                    .clip(CircleShape)
                                    .clickable(
                                        interactionSource = interactionSource,
                                        indication = ripple(bounded = true)
                                    ) {
                                        val nextCount = counter + 1
                                        if (nextCount == targetCount || (nextCount > 0 && nextCount % 50 == 0)) {
                                            HapticHelper.vibrateMilestone(context)
                                        } else {
                                            HapticHelper.vibrateProclamationTap(context)
                                        }
                                        viewModel.incrementCounter(1)
                                    }
                                    .testTag("tap_to_proclaim_button")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = topicText.ifBlank { "Proclamation Topic" },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = counter.toString(),
                                        style = MaterialTheme.typography.displayMedium.copy(
                                            fontSize = 58.sp,
                                            fontWeight = FontWeight.Black
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AddCircle,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = strings.tapToProclaim,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // Quick Edit Counter Badge
                            IconButton(
                                onClick = {
                                    HapticHelper.vibrateClick(context)
                                    showEditCounterDialog = true
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 16.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit count",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
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
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (progress >= 1f) StatusSuccess else MaterialTheme.colorScheme.primary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (progress >= 1f) StatusSuccess else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                                    showResetDialog = true
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .testTag("proclamation_reset_button")
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
                            text = strings.spiritualNotesAndImpressions,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { viewModel.setNotes(it) },
                            label = { Text(strings.sessionNotesPlaceholder) },
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
                            label = { Text(strings.propheticBurdensPlaceholder) },
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.saveProclamationSession,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // 5. Topics History & Lifetime Stats with Plus Icon to add topic
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.savedPrayerTopicsAndProgress,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = {
                            viewModel.setTopicText("")
                            focusManager.clearFocus()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add new prayer topic",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            if (topics.isNotEmpty()) {
                items(topics) { topicItem ->
                    TopicHistoryCard(
                        topic = topicItem,
                        strings = strings,
                        onResume = { viewModel.resumeTopicSession(topicItem) },
                        onStartFresh = { viewModel.startNewSessionForTopic(topicItem) },
                        onEditCount = { newCount -> viewModel.updateTopicCount(topicItem, newCount) },
                        onDelete = { viewModel.deleteTopic(topicItem) }
                    )
                }
            }

            // 6. Manual / Offline Session Recording (At the very bottom)
            item {
                ManualProclamationCard(
                    initialTopic = topicText,
                    onSaveManual = { date, startTime, stopTime, topic, count, dur, notes, onSuccess ->
                        viewModel.saveManualSession(date, startTime, stopTime, topic, count, dur, notes, onSuccess)
                    }
                )
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
                        text = if (strings is FrenchStrings)
                            "Si vous avez déjà proclamé ou prié avant d'enregistrer, vous pouvez régler votre compteur sur n'importe quel nombre (ex. 100) et choisir de continuer à partir de ce nombre comme base de départ."
                        else
                            "If you have already proclaimed or prayed before logging, you can set your counter to any number (e.g., 100) and choose whether to continue from this number as your session starting base.",
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
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (strings is FrenchStrings)
                                "Définir comme point de départ de la session (ne compter que les répétitions supplémentaires pour aujourd'hui)"
                            else
                                "Set as session starting baseline (only count additional repetitions toward today's log)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
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
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (strings is FrenchStrings) "Proclamation & Importunité" else "Proclamation & Importunity",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = if (strings is FrenchStrings) "1. Proclamation de la Foi" else "1. Proclamation of Faith",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (strings is FrenchStrings)
                            "Proclamer la parole de Dieu à voix haute et de manière répétée jusqu'à ce que la foi remplisse l'esprit et que l'opposition spirituelle soit brisée (Hébreux 4:14, Apocalypse 12:11)."
                        else
                            "Speaking God's word aloud repeatedly until faith fills the spirit and spiritual opposition is broken (Hebrews 4:14, Revelation 12:11).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (strings is FrenchStrings) "2. L'Importunité dans la Prière" else "2. Importunity in Prayer",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (strings is FrenchStrings)
                            "Prière persistante, fervente et infatigable pour un sujet précis jusqu'à ce que l'exaucement se manifeste (Luc 11:8, Luc 18:1-8)."
                        else
                            "Persistent, shameless, and untiring prayer for a specific topic until the answer comes (Luke 11:8, Luke 18:1-8).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (strings is FrenchStrings) "3. Conseils Pratiques" else "3. Practical Tips",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (strings is FrenchStrings)
                            "• Choisissez des sujets bibliques clairs.\n• Utilisez le compteur pour maintenir une répétition focalisée.\n• Que vos prières soient audibles, fermes et remplies du Saint-Esprit."
                        else
                            "• Set clear biblical topics.\n• Use the counter to maintain focused repetition.\n• Let your prayers be audible, firm, and filled with the Holy Spirit.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text(if (strings is FrenchStrings) "Compris" else "Understood")
                }
            }
        )
    }

    // Dialog: Save Warning if 0
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = { Text(if (strings is FrenchStrings) "Enregistrer une session vide ?" else "Log Empty Session?") },
            text = { Text(if (strings is FrenchStrings) "Vous avez actuellement 0 répétition et 0 minute enregistrée. Souhaitez-vous tout de même enregistrer ?" else "You currently have 0 repetitions and 0 minutes recorded. Would you still like to log this session?") },
            confirmButton = {
                Button(onClick = {
                    showSaveConfirmDialog = false
                    viewModel.saveSession(onSuccess = { onNavigateBack() })
                }) {
                    Text(if (strings is FrenchStrings) "Enregistrer quand même" else "Save Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveConfirmDialog = false }) {
                    Text(strings.cancelTimer)
                }
            }
        )
    }

    if (showAddTopicDialog) {
        var newTopicInput by remember { mutableStateOf("") }
        var targetInput by remember { mutableStateOf("100") }

        AlertDialog(
            onDismissRequest = { showAddTopicDialog = false },
            title = { Text("Save New Prayer Topic") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTopicInput,
                        onValueChange = { newTopicInput = it },
                        label = { Text("Prayer Topic / Proclamation") },
                        placeholder = { Text("e.g. Divine Wisdom & Breakthrough") },
                        singleLine = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = targetInput,
                        onValueChange = { targetInput = it.filter { c -> c.isDigit() } },
                        label = { Text("Target Proclamations Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetInput.toIntOrNull() ?: 100
                        viewModel.savePrayerTopic(newTopicInput, target)
                        showAddTopicDialog = false
                    },
                    enabled = newTopicInput.isNotBlank()
                ) {
                    Text("Save Topic")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTopicDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Dialog: Reset Proclamation Count Confirmation
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = if (strings is FrenchStrings) "Réinitialiser le compteur ?" else "Reset Counter?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (strings is FrenchStrings)
                            "Voulez-vous remettre le compteur actif et le chronomètre à 0 pour cette session ?"
                        else
                            "Reset the active session counter and chronometer to 0?",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (isResumedSession || selectedTopic != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { resetTopicAcrossApp = !resetTopicAcrossApp }
                                    .padding(8.dp)
                            ) {
                                Checkbox(
                                    checked = resetTopicAcrossApp,
                                    onCheckedChange = { resetTopicAcrossApp = it },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.error)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (strings is FrenchStrings)
                                        "Réinitialiser également le cumul de '${selectedTopic?.topic ?: topicText}' à 0 dans toute l'application"
                                    else
                                        "Also reset '${selectedTopic?.topic ?: topicText}' cumulative count to 0 across the app",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        HapticHelper.vibrateWarning(context)
                        viewModel.resetCounter(resetTopicAcrossApp = resetTopicAcrossApp)
                        showResetDialog = false
                        resetTopicAcrossApp = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_reset_counter_button")
                ) {
                    Text(if (strings is FrenchStrings) "Réinitialiser à 0" else "Reset to 0")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Dialog: Fullscreen Counter
    if (isFullscreenCounterOpen) {
        FullscreenCounterDialog(
            topic = topicText.ifBlank { selectedTopic?.topic ?: if (strings is FrenchStrings) "Proclamation de Foi" else "Faith Proclamation" },
            counter = counter,
            targetCount = targetCount,
            elapsedSeconds = elapsedSeconds,
            isTimerRunning = isTimerRunning,
            onIncrement = {
                val nextCount = counter + 1
                if (nextCount >= targetCount && counter < targetCount) {
                    HapticHelper.vibrateMilestone(context)
                } else {
                    HapticHelper.vibrateProclamationTap(context)
                }
                viewModel.incrementCounter(1)
            },
            onToggleTimer = {
                HapticHelper.vibrateClick(context)
                if (isTimerRunning) viewModel.pauseTimer() else viewModel.startTimer()
            },
            onDismiss = { isFullscreenCounterOpen = false }
        )
    }
}

@Composable
private fun TopicHistoryCard(
    topic: ProclamationTopicEntity,
    strings: AppStrings,
    onResume: () -> Unit,
    onStartFresh: () -> Unit,
    onEditCount: (Int) -> Unit,
    onDelete: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editVal by remember { mutableStateOf(topic.cumulativeCount.toString()) }

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
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                            .clickable { showEditDialog = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${topic.cumulativeCount}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = topic.topic,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${topic.totalDurationSeconds / 60} min total",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (topic.lastPracticedIso.isNotBlank()) {
                                Text(
                                    text = "• ${topic.lastPracticedIso}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("delete_topic_${topic.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete topic",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { menuExpanded = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Topic options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit Number / Count") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    showEditDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (strings is FrenchStrings) "Continuer (${topic.cumulativeCount})" else "Continue Session") },
                                leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onResume()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (strings is FrenchStrings) "Nouvelle Session (+0)" else "Start Fresh (+0)") },
                                leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onStartFresh()
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text(strings.delete, color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                onClick = {
                                    menuExpanded = false
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onResume,
                    modifier = Modifier.weight(1.3f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (strings is FrenchStrings) "Proclamer (${topic.cumulativeCount})" else "Proclaim (${topic.cumulativeCount})",
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
                        text = if (strings is FrenchStrings) "Nouveau (+0)" else "New (+0)",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Topic Number / Count", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Set cumulative proclamations for '${topic.topic}':",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = editVal,
                        onValueChange = { editVal = it.filter { c -> c.isDigit() } },
                        label = { Text("Cumulative Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val count = editVal.toIntOrNull() ?: topic.cumulativeCount
                    onEditCount(count)
                    showEditDialog = false
                }) {
                    Text(strings.save)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = if (strings is FrenchStrings) "Supprimer ce sujet ?" else "Delete Saved Topic?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (strings is FrenchStrings)
                        "Êtes-vous sûr de vouloir supprimer '${topic.topic}' des sujets enregistrés ?"
                    else
                        "Are you sure you want to delete '${topic.topic}' from your saved prayer topics?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    modifier = Modifier.testTag("confirm_delete_topic_${topic.id}")
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun ManualProclamationCard(
    initialTopic: String,
    onSaveManual: (dateIso: String, startTime: String, stopTime: String, topic: String, count: Int, durationMins: Long, notes: String, onSuccess: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var manualDateIso by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var showManualDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showStopTimePicker by remember { mutableStateOf(false) }
    var startTimeText by remember {
        val now = java.time.LocalTime.now()
        val start = now.minusMinutes(15)
        mutableStateOf(String.format("%02d:%02d", start.hour, start.minute))
    }
    var stopTimeText by remember {
        val now = java.time.LocalTime.now()
        mutableStateOf(String.format("%02d:%02d", now.hour, now.minute))
    }
    var manualTopic by remember { mutableStateOf(initialTopic) }
    var manualCountStr by remember { mutableStateOf("100") }
    var manualNotes by remember { mutableStateOf("") }
    var isSavingManual by remember { mutableStateOf(false) }

    // Automatic Duration Calculation from start and stop time
    val calculatedDurationMinutes = remember(startTimeText, stopTimeText) {
        try {
            val startParts = startTimeText.split(":").map { it.trim().toInt() }
            val stopParts = stopTimeText.split(":").map { it.trim().toInt() }
            if (startParts.size == 2 && stopParts.size == 2) {
                val startTotalMin = startParts[0] * 60 + startParts[1]
                val stopTotalMin = stopParts[0] * 60 + stopParts[1]
                val diff = if (stopTotalMin >= startTotalMin) stopTotalMin - startTotalMin else (stopTotalMin + 1440) - startTotalMin
                diff.coerceAtLeast(1).toLong()
            } else 15L
        } catch (_: Exception) {
            15L
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Record Manual Offline Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "If you proclaimed or prayed away from your phone, record your past session manually here:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Date Selector
            OutlinedTextField(
                value = manualDateIso,
                onValueChange = {},
                readOnly = true,
                label = { Text("Session Date") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { showManualDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showManualDatePicker = true },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Manual Topic
            OutlinedTextField(
                value = manualTopic,
                onValueChange = { manualTopic = it },
                label = { Text("Prayer Topic / Proclamation") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Start & Stop Time with Auto-calculated Duration
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Time Span",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = startTimeText,
                            onValueChange = { startTimeText = it },
                            label = { Text("Start Time", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showStartTimePicker = true }) {
                                    Icon(Icons.Default.AccessTime, contentDescription = "Pick Start Time")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_start_time_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = stopTimeText,
                            onValueChange = { stopTimeText = it },
                            label = { Text("Stop Time", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            leadingIcon = {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showStopTimePicker = true }) {
                                    Icon(Icons.Default.AccessTime, contentDescription = "Pick Stop Time")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("manual_stop_time_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    val durHours = calculatedDurationMinutes / 60
                    val durMins = calculatedDurationMinutes % 60
                    val durDisplay = if (durHours > 0) "${durHours}h ${durMins}m" else "${durMins}m"
                    Text(
                        text = "Calculated Duration: $durDisplay (${calculatedDurationMinutes} min)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Proclamations Count
            OutlinedTextField(
                value = manualCountStr,
                onValueChange = { manualCountStr = it.filter { c -> c.isDigit() } },
                label = { Text("Proclamations Count") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = manualNotes,
                onValueChange = { manualNotes = it },
                label = { Text("Notes / Impressions (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 2
            )

            Button(
                onClick = {
                    isSavingManual = true
                    val count = manualCountStr.toIntOrNull() ?: 100
                    onSaveManual(
                        manualDateIso,
                        startTimeText,
                        stopTimeText,
                        manualTopic,
                        count,
                        calculatedDurationMinutes,
                        manualNotes
                    ) {
                        isSavingManual = false
                        manualNotes = ""
                        HapticHelper.vibrateSuccess(context)
                    }
                },
                enabled = !isSavingManual && manualTopic.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Manual Session", fontWeight = FontWeight.Bold)
            }

            if (showManualDatePicker) {
                AppDatePickerDialog(
                    initialDateIso = manualDateIso,
                    onDismiss = { showManualDatePicker = false },
                    onDateSelected = { selDate ->
                        manualDateIso = selDate
                        showManualDatePicker = false
                    }
                )
            }

            if (showStartTimePicker) {
                AppTimePickerDialog(
                    initialTime = startTimeText,
                    onTimeSelected = {
                        startTimeText = it
                        showStartTimePicker = false
                    },
                    onDismiss = { showStartTimePicker = false }
                )
            }

            if (showStopTimePicker) {
                AppTimePickerDialog(
                    initialTime = stopTimeText,
                    onTimeSelected = {
                        stopTimeText = it
                        showStopTimePicker = false
                    },
                    onDismiss = { showStopTimePicker = false }
                )
            }
        }
    }
}

@Composable
private fun FullscreenCounterDialog(
    topic: String,
    counter: Int,
    targetCount: Int,
    elapsedSeconds: Long,
    isTimerRunning: Boolean,
    onIncrement: () -> Unit,
    onToggleTimer: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    // Light theme: dark blue (#14214C / PrimaryBlue) with white text; Dark theme: charcoal dark (#141518) with yellow text
    val backgroundColor = if (isDark) Color(0xFF141518) else PrimaryBlue
    val primaryTextColor = if (isDark) Color(0xFFFFD54F) else Color(0xFFFFFFFF)
    val secondaryTextColor = if (isDark) Color(0xFFFFE082) else Color(0xFFE2E8F0)
    val tertiaryTextColor = if (isDark) Color(0xFFC8B880) else Color(0xFFCBD5E1)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        val context = LocalContext.current
        val activity = context as? Activity

        DisposableEffect(isDark) {
            val window = activity?.window
            val insetsController = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
            // Dialog has a dark background in both light and dark modes, so status bar icons should be light (white)
            insetsController?.isAppearanceLightStatusBars = false
            onDispose {
                insetsController?.isAppearanceLightStatusBars = !isDark
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = primaryTextColor.copy(alpha = 0.25f))
                ) {
                    onIncrement()
                }
                .systemBarsPadding()
                .padding(24.dp)
        ) {
            // Top Bar: Prayer Topic + Close/Minimize Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.ifBlank { "Proclamation & Prayer" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Target: $targetCount proclamations",
                        style = MaterialTheme.typography.labelMedium,
                        color = secondaryTextColor
                    )
                }

                IconButton(
                    onClick = {
                        HapticHelper.vibrateClick(context)
                        onDismiss()
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("fullscreen_counter_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Fullscreen",
                        tint = primaryTextColor,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            // Giant Central Counter Visualizer
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "$counter",
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryTextColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tap anywhere to proclaim",
                    style = MaterialTheme.typography.bodyMedium,
                    color = tertiaryTextColor
                )
            }

            // Bottom Bar: Timer Display & Play/Pause Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mins = elapsedSeconds / 60
                val secs = elapsedSeconds % 60
                val formattedTime = String.format("%02d:%02d", mins, secs)

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = secondaryTextColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        onToggleTimer()
                    },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = primaryTextColor.copy(alpha = 0.2f),
                        contentColor = primaryTextColor
                    )
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isTimerRunning) "Pause Timer" else "Start Timer",
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
