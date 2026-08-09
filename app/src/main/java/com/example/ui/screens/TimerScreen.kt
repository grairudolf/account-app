package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.data.local.entities.TimerSessionEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    domainId: String,
    strings: AppStrings,
    activeSession: TimerSessionEntity?,
    elapsedSeconds: Long,
    onStartTimer: (domainId: String) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopAndSaveTimer: (notes: String, reflection: String) -> Unit,
    onDiscardTimer: () -> Unit,
    onBack: () -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LIVE TIMER - ${domainId.replace("_", " ").uppercase()}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("timer_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("timer_display_card")
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(LightBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = PrimaryBlue,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = domainId.replace("_", " ").uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = formattedTime,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark,
                        letterSpacing = 2.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (activeSession == null) {
                            Button(
                                onClick = { onStartTimer(domainId) },
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("timer_start_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            if (activeSession.isPaused) {
                                Button(
                                    onClick = onResumeTimer,
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("timer_resume_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Resume", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onPauseTimer,
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("timer_pause_button"),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Pause", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = { showSaveDialog = true },
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("timer_stop_save_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (activeSession != null) {
                        TextButton(
                            onClick = onDiscardTimer,
                            modifier = Modifier.testTag("timer_discard_button")
                        ) {
                            Text("Discard Session", color = StatusError, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        SaveTimerDialog(
            formattedTime = formattedTime,
            onDismiss = { showSaveDialog = false },
            onConfirm = { notes, reflection ->
                onStopAndSaveTimer(notes, reflection)
                showSaveDialog = false
                onBack()
            }
        )
    }
}

@Composable
fun SaveTimerDialog(
    formattedTime: String,
    onDismiss: () -> Unit,
    onConfirm: (notes: String, reflection: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Session ($formattedTime)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Prayer / DDEWG Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = reflection,
                    onValueChange = { reflection = it },
                    label = { Text("Spiritual Insight / What God said") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(notes, reflection) }) {
                Text("Save to Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
