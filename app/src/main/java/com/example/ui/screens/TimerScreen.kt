package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.core.localization.AppStrings
import com.example.core.util.HapticHelper
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

    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            Toast.makeText(context, "Notification permission granted! Live timer will show in status bar.", Toast.LENGTH_SHORT).show()
        }
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${strings.liveTimerMode.uppercase()} - ${strings.getDomainTitleById(domainId).uppercase()}", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("timer_back_button")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    color = PrimaryBlue.copy(alpha = 0.05f),
                    radius = w * 0.55f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.15f)
                )
                drawCircle(
                    color = AccentPurple.copy(alpha = 0.04f),
                    radius = w * 0.6f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.85f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = LightBlueContainer,
                        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(28.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Enable Live Notifications",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark
                                )
                                Text(
                                    text = "Allow notifications so your session countdown and pause/save actions appear on your lockscreen/notification tray.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Button(
                                onClick = { notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Allow", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

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
                        text = strings.getDomainTitleById(domainId).uppercase(),
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
                                onClick = {
                                    HapticHelper.vibrateHeavyClick(context)
                                    onStartTimer(domainId)
                                },
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("timer_start_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(strings.startTimer, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        } else {
                            if (activeSession.isPaused) {
                                Button(
                                    onClick = {
                                        HapticHelper.vibrateHeavyClick(context)
                                        onResumeTimer()
                                    },
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("timer_resume_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.resumeTimer, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        HapticHelper.vibrateClick(context)
                                        onPauseTimer()
                                    },
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("timer_pause_button"),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.pauseTimer, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = {
                                    HapticHelper.vibrateClick(context)
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
                                Text(strings.save, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (activeSession != null) {
                        TextButton(
                            onClick = {
                                HapticHelper.vibrateWarning(context)
                                onDiscardTimer()
                            },
                            modifier = Modifier.testTag("timer_discard_button")
                        ) {
                            Text(strings.discardSession, color = StatusError, fontWeight = FontWeight.SemiBold)
                        }
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
            strings = strings,
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
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (notes: String, reflection: String) -> Unit
) {
    val context = LocalContext.current
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
    var proclamationTopic by remember { mutableStateOf("Jesus Christ is Lord") }
    var proclamationCountText by remember { mutableStateOf("10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (domainId == "fasting") strings.saveActivityRecord else "${strings.save} ($formattedTime)") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (domainId) {
                    "bible_reading" -> {
                        Text(strings.bibleReadingTitle, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        ExposedDropdownMenuBox(
                            expanded = bookDropdownExpanded,
                            onExpandedChange = { bookDropdownExpanded = !bookDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = strings.getBibleBookName(selectedBook),
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.selectBibleBook) },
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
                                        text = { Text(strings.getBibleBookName(bookInfo.name)) },
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
                                label = { Text(strings.startChapter) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endChapter,
                                onValueChange = { endChapter = it.filter { c -> c.isDigit() } },
                                label = { Text(strings.endChapter) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.activityNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "ddewg" -> {
                        Text(strings.ddewgTitle, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = ddewgInspiration,
                            onValueChange = { ddewgInspiration = it },
                            label = { Text(strings.inspirationForMeditation) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.activityNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "prayer_alone", "prayer_with_others" -> {
                        Text(if (domainId == "prayer_alone") strings.prayerAloneTitle else strings.prayerWithOthersTitle, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = prayerType,
                            onValueChange = { prayerType = it },
                            label = { Text(strings.typeOfPrayerFocus) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (domainId == "prayer_with_others") {
                            OutlinedTextField(
                                value = participantsCountText,
                                onValueChange = { participantsCountText = it.filter { c -> c.isDigit() } },
                                label = { Text(strings.numTopicsRecorded) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.sessionNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = reflection,
                            onValueChange = { reflection = it },
                            label = { Text(strings.propheticBurdensPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "proclamation_importunity" -> {
                        Text(strings.proclamationTitle, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = proclamationTopic,
                            onValueChange = { proclamationTopic = it },
                            label = { Text(strings.prayerFocus) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = proclamationCountText,
                            onValueChange = { proclamationCountText = it.filter { c -> c.isDigit() } },
                            label = { Text(strings.numTopicsRecorded) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.sessionNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = reflection,
                            onValueChange = { reflection = it },
                            label = { Text(strings.propheticBurdensPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "fasting" -> {
                        Text(strings.fastingTitle, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = fastingType,
                            onValueChange = { fastingType = it },
                            label = { Text(strings.typeOfFast) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.activityNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "giving" -> {
                        Text(strings.givingTitle, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = givingType,
                            onValueChange = { givingType = it },
                            label = { Text(strings.givingTypeExtendedPlaceholder) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text(strings.givingAmountLabel) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.activityNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "christian_lit", "christian_lit_mem" -> {
                        Text(strings.christianLitTitle, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = bookTitle,
                            onValueChange = { bookTitle = it },
                            label = { Text(strings.bookTitle) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = pagesReadText,
                            onValueChange = { pagesReadText = it.filter { c -> c.isDigit() } },
                            label = { Text(strings.pagesRead) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.activityNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "bible_mem" -> {
                        Text(strings.bibleMemTitle, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        OutlinedTextField(
                            value = selectedBook,
                            onValueChange = { selectedBook = it },
                            label = { Text(strings.versesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.activityNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    "soul_winning" -> {
                        Text(strings.soulWinningTitle, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = preachedCountText,
                                onValueChange = { preachedCountText = it.filter { c -> c.isDigit() } },
                                label = { Text(strings.peoplePreachedTo) },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = convertedCountText,
                                onValueChange = { convertedCountText = it.filter { c -> c.isDigit() } },
                                label = { Text(strings.peopleConverted) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.activityNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    else -> {
                        Text(strings.sessionNotesPrompt, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text(strings.sessionNotesPrompt) },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = reflection,
                            onValueChange = { reflection = it },
                            label = { Text(strings.propheticBurdensPrompt) },
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
                        "proclamation_importunity" -> "Topic: $proclamationTopic, Repetitions: $proclamationCountText. Notes: $notes"
                        "fasting" -> "Fast Type: $fastingType. Notes: $notes"
                        "giving" -> "Category: $givingType, Amount: $amountText. Notes: $notes"
                        "christian_lit", "christian_lit_mem" -> "Book: $bookTitle, Pages: $pagesReadText. Notes: $notes"
                        "bible_mem" -> "Verses: $selectedBook. Notes: $notes"
                        "soul_winning" -> "Preached: $preachedCountText, Converts: $convertedCountText. Notes: $notes"
                        else -> notes
                    }
                    val finalReflection = if (domainId == "ddewg") ddewgInspiration else if (reflection.isNotBlank()) reflection else "Logged via live timer."
                    HapticHelper.vibrateSuccess(context)
                    onConfirm(finalNotes, finalReflection)
                }
            ) {
                Text(strings.saveActivityRecord)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancel)
            }
        }
    )
}
