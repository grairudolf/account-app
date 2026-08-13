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
                                onClick = {
                                    if (activeSession != null && !activeSession.isPaused) {
                                        onPauseTimer()
                                    }
                                    showSaveDialog = true
                                },
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
            domainId = domainId,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveTimerDialog(
    domainId: String,
    formattedTime: String,
    onDismiss: () -> Unit,
    onConfirm: (notes: String, reflection: String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }

    // Domain Specific State
    var selectedBook by remember { mutableStateOf("Genesis") }
    var startChapter by remember { mutableStateOf("1") }
    var endChapter by remember { mutableStateOf("1") }
    var bookDropdownExpanded by remember { mutableStateOf(false) }

    var prayerType by remember { mutableStateOf("Intercession") }
    var participantsCountText by remember { mutableStateOf("1") }

    var bookTitle by remember { mutableStateOf("") }
    var pagesReadText by remember { mutableStateOf("10") }

    var fastingType by remember { mutableStateOf("Complete Fast") }

    var givingType by remember { mutableStateOf("Tithe") }
    var amountText by remember { mutableStateOf("0.0") }

    var preachedCountText by remember { mutableStateOf("1") }
    var convertedCountText by remember { mutableStateOf("0") }
    var ddewgInspiration by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (domainId == "fasting") "Save Fasting Record" else "Save Session ($formattedTime)") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (domainId) {
                    "bible_reading" -> {
                        Text("Bible Reading Summary", fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        ExposedDropdownMenuBox(
                            expanded = bookDropdownExpanded,
                            onExpandedChange = { bookDropdownExpanded = !bookDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBook,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Bible Book") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = bookDropdownExpanded,
                                onDismissRequest = { bookDropdownExpanded = false }
                            ) {
                                com.example.domain.models.BibleMetadata.BOOKS.forEach { bookInfo ->
                                    DropdownMenuItem(
                                        text = { Text(bookInfo.name) },
                                        onClick = {
                                            selectedBook = bookInfo.name
                                            bookDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = startChapter,
                                onValueChange = { startChapter = it.filter { c -> c.isDigit() } },
                                label = { Text("Start Chapter") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endChapter,
                                onValueChange = { endChapter = it.filter { c -> c.isDigit() } },
                                label = { Text("End Chapter") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Key Learnings / Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "ddewg" -> {
                        Text("Dynamic Encounter (DDEWG)", fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = ddewgInspiration,
                            onValueChange = { ddewgInspiration = it },
                            label = { Text("Inspiration for Meditation") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "prayer_alone", "prayer_with_others" -> {
                        Text("Prayer Session Details", fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = prayerType,
                            onValueChange = { prayerType = it },
                            label = { Text("Prayer Focus (Intercession, Thanksgiving...)") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (domainId == "prayer_with_others") {
                            OutlinedTextField(
                                value = participantsCountText,
                                onValueChange = { participantsCountText = it.filter { c -> c.isDigit() } },
                                label = { Text("Number of Prayer Partners") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Prayer Topics & Burdens Prayed For") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = reflection,
                            onValueChange = { reflection = it },
                            label = { Text("Prophetic Burdens / Divine Impressions") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "fasting" -> {
                        Text("Fasting Session Summary", fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = fastingType,
                            onValueChange = { fastingType = it },
                            label = { Text("Type of Fast (Complete, Partial, Water)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Spiritual Goals & Reflection") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "giving" -> {
                        Text("Giving Session Summary", fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = givingType,
                            onValueChange = { givingType = it },
                            label = { Text("Giving Category (Tithe, Offering, Alms)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Amount Given") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Recipient / Additional Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "christian_lit", "christian_lit_mem" -> {
                        Text("Christian Literature Summary", fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = bookTitle,
                            onValueChange = { bookTitle = it },
                            label = { Text("Book Title / Author") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = pagesReadText,
                            onValueChange = { pagesReadText = it.filter { c -> c.isDigit() } },
                            label = { Text("Pages Read") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Key Quotes & Takeaways") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "bible_mem" -> {
                        Text("Bible Memorization Summary", fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = selectedBook,
                            onValueChange = { selectedBook = it },
                            label = { Text("Passage / Verses Memorized (e.g. John 3:16)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Reflection on Passages Memorized") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "soul_winning" -> {
                        Text("Evangelism & Soul Winning Summary", fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = preachedCountText,
                                onValueChange = { preachedCountText = it.filter { c -> c.isDigit() } },
                                label = { Text("Preached To") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = convertedCountText,
                                onValueChange = { convertedCountText = it.filter { c -> c.isDigit() } },
                                label = { Text("Converts") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Names & Follow-up Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    else -> {
                        Text("Session Notes", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Session Notes") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = reflection,
                            onValueChange = { reflection = it },
                            label = { Text("Spiritual Reflection / Takeaway") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalNotes = when (domainId) {
                        "bible_reading" -> "Book: $selectedBook, Ch: $startChapter-$endChapter. Notes: $notes"
                        "ddewg" -> if (notes.isNotBlank()) notes else "DDEWG session"
                        "prayer_alone", "prayer_with_others" -> "Type: $prayerType. Notes: $notes"
                        "fasting" -> "Fast Type: $fastingType. Notes: $notes"
                        "giving" -> "Category: $givingType, Amount: $amountText. Notes: $notes"
                        "christian_lit", "christian_lit_mem" -> "Book: $bookTitle, Pages: $pagesReadText. Notes: $notes"
                        "bible_mem" -> "Verses: $selectedBook. Notes: $notes"
                        "soul_winning" -> "Preached: $preachedCountText, Converts: $convertedCountText. Notes: $notes"
                        else -> notes
                    }
                    val finalReflection = if (domainId == "ddewg") ddewgInspiration else if (reflection.isNotBlank()) reflection else "Logged via live timer."
                    onConfirm(finalNotes, finalReflection)
                }
            ) {
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
