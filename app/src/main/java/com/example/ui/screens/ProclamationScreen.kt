package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import com.example.core.localization.AppStrings
import com.example.core.localization.EnglishStrings
import com.example.core.localization.FrenchStrings
import com.example.core.util.HapticHelper
import com.example.data.local.entities.ProclamationTopicEntity
import com.example.ui.components.AppDatePickerDialog
import com.example.ui.components.AppTimePickerDialog
import com.example.ui.theme.*
import com.example.ui.viewmodels.ProclamationViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProclamationScreen(
    viewModel: ProclamationViewModel,
    strings: AppStrings = EnglishStrings,
    onNavigateBack: () -> Unit,
    onNavigateToDomain: (String) -> Unit = {}
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

    var showEditCounterDialog by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<ProclamationTopicEntity?>(null) }
    var topicToDelete by remember { mutableStateOf<ProclamationTopicEntity?>(null) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSaveConfirmDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetTopicAcrossApp by remember { mutableStateOf(false) }
    var isFullscreenCounterOpen by remember { mutableStateOf(false) }
    var isManualRecordingExpanded by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val activeSavedTopic = selectedTopic ?: topics.find { it.topic.trim().equals(topicText.trim(), ignoreCase = true) }

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
                    // Direct quick link to Domain Details
                    IconButton(
                        onClick = { onNavigateToDomain("proclamation_importunity") },
                        modifier = Modifier.testTag("proclamation_domain_link_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Domain Details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = { showInfoDialog = true },
                        modifier = Modifier.testTag("proclamation_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Spiritual Guidelines"
                        )
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
            contentPadding = PaddingValues(top = 8.dp, bottom = 40.dp)
        ) {
            // Scriptural Motto & Domain Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToDomain("proclamation_importunity") },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
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
                                text = strings.proclamationMottoVerse,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = strings.proclamationMottoRef,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open Domain",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Proclamation Topic Input & Fast Switch
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

                            if (activeSavedTopic != null) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Bookmark,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "${activeSavedTopic.cumulativeCount} / ${activeSavedTopic.targetCount}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        onClick = { topicToEdit = activeSavedTopic },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit Topic",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = { topicToDelete = activeSavedTopic },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Topic",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else if (topicText.isNotBlank()) {
                                TextButton(
                                    onClick = {
                                        viewModel.savePrayerTopic(
                                            topicText = topicText,
                                            targetCount = targetCount,
                                            currentCount = counter,
                                            onSuccess = {
                                                android.widget.Toast.makeText(
                                                    context,
                                                    if (strings is FrenchStrings) "Sujet enregistré" else "Topic saved",
                                                    android.widget.Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        )
                                    },
                                    modifier = Modifier.testTag("save_prayer_topic_button"),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (strings is FrenchStrings) "Enregistrer" else "Save Topic",
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
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

                        // Clean Saved Topics Filter Carousel (1-tap switch)
                        if (topics.isNotEmpty()) {
                            Text(
                                text = if (strings is FrenchStrings) "Sujets enregistrés :" else "Saved topics:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 2.dp)
                            ) {
                                items(topics) { item ->
                                    val isSelected = selectedTopic?.id == item.id || topicText.trim().equals(item.topic.trim(), ignoreCase = true)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            viewModel.resumeTopicSession(item)
                                        },
                                        label = {
                                            Text(
                                                text = "${item.topic} (${item.cumulativeCount})",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        },
                                        leadingIcon = if (isSelected) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        } else null,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }

                        // Clean Scriptural Suggestions Carousel
                        Text(
                            text = strings.quickScripturalProclamations,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        val suggestionsList = if (strings is FrenchStrings) listOf(
                            "Jesus-Christ est Seigneur sur toutes les nations",
                            "Tout pouvoir m'a ete donne dans le ciel et sur la terre",
                            "Mon Dieu pourvoira a tous mes besoins selon sa richesse",
                            "L'Eternel est ma lumiere et mon salut, de qui aurais-je crainte?",
                            "Toute arme forgee contre moi sera sans effet",
                            "Par ses meurtrissures nous sommes gueris",
                            "L'Eternel combattra pour vous, et vous garderez le silence",
                            "La moisson est grande, envoie des ouvriers dans ta moisson"
                        ) else viewModel.sampleSuggestions

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 2.dp)
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
                    }
                }
            }

            // Interactive Counter & Live Timer Area
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
                        // Continuing session indicator
                        if (isResumedSession && startingCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val addedInSession = (counter - startingCount).coerceAtLeast(0)
                                    Text(
                                        text = String.format(strings.continuingSessionFrom, startingCount) + " (+$addedInSession)",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    TextButton(
                                        onClick = { viewModel.clearResumedSession() },
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                    ) {
                                        Text(strings.startAtZero, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        // Timer & Fullscreen Bar
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

                        // Central Tap-to-Proclaim Button & Counter Display
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
                                tonalElevation = 6.dp,
                                shadowElevation = 10.dp,
                                modifier = Modifier
                                    .size(210.dp)
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
                                        text = topicText.ifBlank { "Proclamation" },
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
                                            fontSize = 54.sp,
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
                                                modifier = Modifier.size(14.dp)
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

                            // Quick Edit Counter Button
                            IconButton(
                                onClick = {
                                    HapticHelper.vibrateClick(context)
                                    showEditCounterDialog = true
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(end = 12.dp)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit count",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
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
                                    text = "$counter / $targetCount ${if (strings is FrenchStrings) "Proclamations" else "Proclamations"}",
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

                        // Quick Increment & Decrement Controls
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
                                    .size(42.dp)
                                    .testTag("proclamation_minus_1"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("-1", fontWeight = FontWeight.Bold)
                            }

                            // Quick Increment Buttons
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

                            // Reset Action
                            IconButton(
                                onClick = {
                                    HapticHelper.vibrateWarning(context)
                                    showResetDialog = true
                                },
                                modifier = Modifier
                                    .size(42.dp)
                                    .testTag("proclamation_reset_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset Count")
                            }
                        }
                    }
                }
            }

            // Session Notes Field
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
                            placeholder = { Text(strings.sessionNotesPlaceholder) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("proclamation_notes_input"),
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Primary Save Action Button
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
                        .height(52.dp)
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

            // Saved Topics & Progress List
            if (topics.isNotEmpty()) {
                item {
                    Text(
                        text = strings.savedPrayerTopicsAndProgress,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(topics) { topicItem ->
                    TopicHistoryCard(
                        topic = topicItem,
                        strings = strings,
                        onResume = { viewModel.resumeTopicSession(topicItem) },
                        onStartFresh = { viewModel.startNewSessionForTopic(topicItem) },
                        onEdit = { topicToEdit = topicItem },
                        onDelete = { topicToDelete = topicItem }
                    )
                }
            }

            // Expandable Offline / Manual Session Recording Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isManualRecordingExpanded = !isManualRecordingExpanded },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (strings is FrenchStrings) "Enregistrer une session hors-ligne" else "Record Offline Session",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = if (isManualRecordingExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isManualRecordingExpanded) {
                            ManualProclamationContent(
                                initialTopic = topicText,
                                strings = strings,
                                onSaveManual = { date, startTime, stopTime, topic, count, dur, noteText, onSuccess ->
                                    viewModel.saveManualSession(date, startTime, stopTime, topic, count, dur, noteText, onSuccess)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog: Edit Counter Starting Value / Target
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
                            "Réglez le compteur et choisissez si cette valeur sert de point de départ pour la session en cours."
                        else
                            "Set the counter and choose whether this value acts as your starting baseline for this session.",
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
                                "Définir comme point de départ de la session"
                            else
                                "Set as session starting baseline",
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = if (strings is FrenchStrings) "Proclamation de la Foi" else "Proclamation of Faith",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (strings is FrenchStrings)
                            "Proclamer la parole de Dieu a voix haute et de maniere repetee jusqu'a ce que la foi remplisse l'esprit et que l'opposition spirituelle soit brisee (Hebreux 4:14, Apocalypse 12:11)."
                        else
                            "Speaking God's word aloud repeatedly until faith fills the spirit and spiritual opposition is broken (Hebrews 4:14, Revelation 12:11).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (strings is FrenchStrings) "L'Importunite dans la Priere" else "Importunity in Prayer",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (strings is FrenchStrings)
                            "Priere persistante, fervente et infatigable pour un sujet precis jusqu'a ce que l'exaucement se manifeste (Luc 11:8, Luc 18:1-8)."
                        else
                            "Persistent and untiring prayer for a specific topic until the answer comes (Luke 11:8, Luke 18:1-8).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = if (strings is FrenchStrings) "Conseils Pratiques" else "Practical Guidance",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (strings is FrenchStrings)
                            "• Choisissez des sujets bibliques clairs.\n• Utilisez le compteur pour maintenir une repetition focalisee.\n• Que vos prieres soient audibles, fermes et remplies du Saint-Esprit."
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

    // Dialog: Save Empty Warning
    if (showSaveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSaveConfirmDialog = false },
            title = { Text(if (strings is FrenchStrings) "Enregistrer une session vide ?" else "Save Empty Session?") },
            text = { Text(if (strings is FrenchStrings) "Vous avez actuellement 0 répétition. Souhaitez-vous tout de même enregistrer ?" else "You currently have 0 repetitions recorded. Would you still like to save this session?") },
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

    // Dialog: Reset Counter Confirmation
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
                            "Remettre le compteur actif et le chronomètre à 0 pour cette session ?"
                        else
                            "Reset the active counter and chronometer to 0 for this session?",
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
                    Text(if (strings is FrenchStrings) "Réinitialiser" else "Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Dialog: Fullscreen Distraction-Free Counter
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

    topicToEdit?.let { item ->
        EditTopicDialog(
            topic = item,
            strings = strings,
            onDismiss = { topicToEdit = null },
            onSave = { newTitle, newTarget, newCount ->
                viewModel.editTopic(item, newTitle, newTarget, newCount)
                android.widget.Toast.makeText(
                    context,
                    if (strings is FrenchStrings) "Sujet mis à jour" else "Topic updated",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            },
            onDelete = {
                viewModel.deleteTopic(item)
                android.widget.Toast.makeText(
                    context,
                    if (strings is FrenchStrings) "Sujet supprimé" else "Topic deleted",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    topicToDelete?.let { item ->
        DeleteConfirmDialog(
            topic = item,
            strings = strings,
            onDismiss = { topicToDelete = null },
            onConfirmDelete = {
                viewModel.deleteTopic(item)
                android.widget.Toast.makeText(
                    context,
                    if (strings is FrenchStrings) "Sujet supprimé" else "Topic deleted",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

@Composable
private fun EditTopicDialog(
    topic: ProclamationTopicEntity,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onSave: (newTitle: String, newTarget: Int, newCount: Int) -> Unit,
    onDelete: () -> Unit
) {
    var topicTitle by remember { mutableStateOf(topic.topic) }
    var cumulativeCountText by remember { mutableStateOf(topic.cumulativeCount.toString()) }
    var targetCountText by remember { mutableStateOf(topic.targetCount.toString()) }
    var showConfirmDelete by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(if (strings is FrenchStrings) "Modifier le sujet" else "Edit Prayer Topic", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = topicTitle,
                    onValueChange = { topicTitle = it },
                    label = { Text(if (strings is FrenchStrings) "Sujet / Proclamation" else "Prayer Topic / Proclamation") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = cumulativeCountText,
                        onValueChange = { cumulativeCountText = it.filter { c -> c.isDigit() } },
                        label = { Text(if (strings is FrenchStrings) "Total Actuel" else "Current Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = targetCountText,
                        onValueChange = { targetCountText = it.filter { c -> c.isDigit() } },
                        label = { Text(if (strings is FrenchStrings) "Objectif" else "Target Count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                TextButton(
                    onClick = { showConfirmDelete = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (strings is FrenchStrings) "Supprimer ce sujet" else "Delete Topic")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = cumulativeCountText.toIntOrNull() ?: topic.cumulativeCount
                    val target = targetCountText.toIntOrNull() ?: topic.targetCount
                    onSave(topicTitle.trim(), target, count)
                    onDismiss()
                }
            ) {
                Text(strings.save)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelTimer)
            }
        }
    )

    if (showConfirmDelete) {
        DeleteConfirmDialog(
            topic = topic,
            strings = strings,
            onDismiss = { showConfirmDelete = false },
            onConfirmDelete = {
                showConfirmDelete = false
                onDelete()
                onDismiss()
            }
        )
    }
}

@Composable
private fun DeleteConfirmDialog(
    topic: ProclamationTopicEntity,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirmDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
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
                    onConfirmDelete()
                    onDismiss()
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
            TextButton(onClick = onDismiss) {
                Text(strings.cancelTimer)
            }
        }
    )
}

@Composable
private fun TopicHistoryCard(
    topic: ProclamationTopicEntity,
    strings: AppStrings,
    onResume: () -> Unit,
    onStartFresh: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${topic.cumulativeCount}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = topic.topic,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Target: ${topic.targetCount} • ${topic.totalDurationSeconds / 60} min total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Topic",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Topic",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
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
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (strings is FrenchStrings) "Proclamer (${topic.cumulativeCount})" else "Proclaim (${topic.cumulativeCount})",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                OutlinedButton(
                    onClick = onStartFresh,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (strings is FrenchStrings) "Nouveau (+0)" else "New (+0)",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun ManualProclamationContent(
    initialTopic: String,
    strings: AppStrings,
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

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Date Selector
        OutlinedTextField(
            value = manualDateIso,
            onValueChange = {},
            readOnly = true,
            label = { Text("Date") },
            leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showManualDatePicker = true }) {
                    Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showManualDatePicker = true },
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        // Manual Topic
        OutlinedTextField(
            value = manualTopic,
            onValueChange = { manualTopic = it },
            label = { Text(if (strings is FrenchStrings) "Sujet de prière" else "Prayer Topic") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        // Time Range
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = startTimeText,
                onValueChange = { startTimeText = it },
                label = { Text("Start") },
                trailingIcon = {
                    IconButton(onClick = { showStartTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Pick Start Time")
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = stopTimeText,
                onValueChange = { stopTimeText = it },
                label = { Text("Stop") },
                trailingIcon = {
                    IconButton(onClick = { showStopTimePicker = true }) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Pick Stop Time")
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
        }

        // Count & Notes
        OutlinedTextField(
            value = manualCountStr,
            onValueChange = { manualCountStr = it.filter { c -> c.isDigit() } },
            label = { Text(if (strings is FrenchStrings) "Nombre de proclamations" else "Proclamations Count") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        OutlinedTextField(
            value = manualNotes,
            onValueChange = { manualNotes = it },
            label = { Text(if (strings is FrenchStrings) "Notes (optionnel)" else "Notes (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
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
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (strings is FrenchStrings) "Enregistrer la session manuelle" else "Log Manual Session")
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
            // Top Bar
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
