package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.core.localization.AppStrings
import com.example.core.util.HapticHelper
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.local.entities.TimerSessionEntity
import com.example.domain.models.BibleMetadata
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

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
    onStopAndSaveTimer: (entry: AccountabilityEntryEntity) -> Unit,
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
            Toast.makeText(context, "Notification permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

    val hours = elapsedSeconds / 3600
    val minutes = (elapsedSeconds % 3600) / 60
    val seconds = elapsedSeconds % 60
    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.getDomainTitleById(domainId).uppercase(),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
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
                                    text = "Allow notifications to keep track of this session across background navigation.",
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
                                        HapticHelper.vibrateSuccess(context)
                                        onStartTimer(domainId)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("timer_start_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Start")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.startTimer, fontWeight = FontWeight.Bold)
                                }
                            } else if (activeSession.isRunning) {
                                Button(
                                    onClick = {
                                        HapticHelper.vibrateClick(context)
                                        onPauseTimer()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("timer_pause_button")
                                ) {
                                    Icon(Icons.Default.Pause, contentDescription = "Pause")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.pauseTimer, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        HapticHelper.vibrateSuccess(context)
                                        showSaveDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("timer_stop_save_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = "Stop and Save")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.endSession, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                Button(
                                    onClick = {
                                        HapticHelper.vibrateClick(context)
                                        onResumeTimer()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("timer_resume_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.resumeTimer, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        HapticHelper.vibrateSuccess(context)
                                        showSaveDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("timer_stop_save_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = "Stop and Save")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.endSession, fontWeight = FontWeight.Bold)
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
            elapsedSeconds = elapsedSeconds,
            strings = strings,
            onDismiss = { showSaveDialog = false },
            onConfirm = { entry ->
                onStopAndSaveTimer(entry)
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
    elapsedSeconds: Long,
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (entry: AccountabilityEntryEntity) -> Unit
) {
    val context = LocalContext.current
    var notes by remember { mutableStateOf("") }
    var reflection by remember { mutableStateOf("") }

    // Multi-Book Bible Reading Segments
    val bibleSegments = remember {
        mutableStateListOf(BibleReadingSegment(book = "Genesis", startChapter = 1, endChapter = 1))
    }

    // Prayer Domains
    var prayerFocusType by remember {
        mutableStateOf(
            if (domainId == "prayer_with_others") "Prayer Night" else "Intercession"
        )
    }
    var customPrayerFocus by remember { mutableStateOf("") }
    var prayerParticipantsCountText by remember { mutableStateOf("1") }
    var prayerTopicsCountText by remember { mutableStateOf("1") }

    // Christian Literature
    var bookTitle by remember { mutableStateOf("") }
    var bookAuthor by remember { mutableStateOf("") }
    var startPageText by remember { mutableStateOf("1") }
    var endPageText by remember { mutableStateOf("10") }
    var timesReadText by remember { mutableStateOf("1") }

    // Giving to God
    var givingIncomeText by remember { mutableStateOf("") }
    var givingAmountText by remember { mutableStateOf("") }
    var givingType by remember { mutableStateOf("Tithe") }

    // Spiritual Retreats
    var retreatFocus by remember { mutableStateOf("") }
    val retreatActivities = remember {
        mutableStateMapOf(
            "Solitude & Silence" to false,
            "Complete Fasting" to false,
            "Extended Prayer & Intercession" to false,
            "Intensive Word Study" to false,
            "Meditation & Journaling" to false,
            "Spiritual Examination & Repentance" to false,
            "Waiting on the Holy Spirit" to false
        )
    }

    // Soul Winning
    var preachedToCountText by remember { mutableStateOf("1") }
    var convertedCountText by remember { mutableStateOf("0") }

    // Computed Values
    val calculatedTotalBibleChapters = remember(bibleSegments.toList()) {
        bibleSegments.sumOf { (it.endChapter - it.startChapter + 1).coerceAtLeast(1) }
    }
    val calculatedLiteraturePages = remember(startPageText, endPageText) {
        val s = startPageText.toIntOrNull() ?: 1
        val e = endPageText.toIntOrNull() ?: s
        if (e >= s && s > 0) (e - s + 1) else 0
    }
    val calculatedGivingPercentage = remember(givingIncomeText, givingAmountText) {
        val income = givingIncomeText.toDoubleOrNull() ?: 0.0
        val amount = givingAmountText.toDoubleOrNull() ?: 0.0
        if (income > 0.0) (amount / income) * 100.0 else 0.0
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = strings.sessionComplete,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlueDark
                    )
                    Text(
                        text = "${strings.duration}: $formattedTime",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue
                    )
                }

                // Domain-Specific Metadata Inputs
                when (domainId) {
                    "bible_reading" -> {
                        item {
                            Text(strings.bibleReading, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                        items(bibleSegments.size) { index ->
                            val segment = bibleSegments[index]
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = LightBlueContainer.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("${strings.bookSegment} #${index + 1}", fontWeight = FontWeight.Bold, color = PrimaryBlueDark)
                                        if (bibleSegments.size > 1) {
                                            IconButton(onClick = { bibleSegments.removeAt(index) }, modifier = Modifier.size(28.dp)) {
                                                Icon(Icons.Default.Delete, contentDescription = strings.removeBookSegment, tint = StatusError)
                                            }
                                        }
                                    }

                                    var bookExp by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(expanded = bookExp, onExpandedChange = { bookExp = !bookExp }) {
                                        OutlinedTextField(
                                            value = strings.getBibleBookName(segment.book),
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(strings.selectBibleBook) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookExp) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            singleLine = true
                                        )
                                        ExposedDropdownMenu(expanded = bookExp, onDismissRequest = { bookExp = false }) {
                                            BibleMetadata.BOOKS.forEach { b ->
                                                DropdownMenuItem(
                                                    text = { Text("${strings.getBibleBookName(b.name)} (${b.chapters} ch)") },
                                                    onClick = {
                                                        bibleSegments[index] = segment.copy(book = b.name, startChapter = 1, endChapter = 1)
                                                        bookExp = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    val bookInfo = BibleMetadata.BOOKS.find { it.name.equals(segment.book, ignoreCase = true) } ?: BibleMetadata.BOOKS.first()
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        var sChExp by remember { mutableStateOf(false) }
                                        var eChExp by remember { mutableStateOf(false) }
                                        ExposedDropdownMenuBox(expanded = sChExp, onExpandedChange = { sChExp = !sChExp }, modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = "Ch. ${segment.startChapter}",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text(strings.startChapter) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sChExp) },
                                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                singleLine = true
                                            )
                                            ExposedDropdownMenu(expanded = sChExp, onDismissRequest = { sChExp = false }) {
                                                (1..bookInfo.chapters).forEach { ch ->
                                                    DropdownMenuItem(
                                                        text = { Text("Ch. $ch") },
                                                        onClick = {
                                                            val newEnd = if (segment.endChapter < ch) ch else segment.endChapter
                                                            bibleSegments[index] = segment.copy(startChapter = ch, endChapter = newEnd)
                                                            sChExp = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        ExposedDropdownMenuBox(expanded = eChExp, onExpandedChange = { eChExp = !eChExp }, modifier = Modifier.weight(1f)) {
                                            OutlinedTextField(
                                                value = "Ch. ${segment.endChapter}",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text(strings.endChapter) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eChExp) },
                                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                singleLine = true
                                            )
                                            ExposedDropdownMenu(expanded = eChExp, onDismissRequest = { eChExp = false }) {
                                                (segment.startChapter..bookInfo.chapters).forEach { ch ->
                                                    DropdownMenuItem(
                                                        text = { Text("Ch. $ch") },
                                                        onClick = {
                                                            bibleSegments[index] = segment.copy(endChapter = ch)
                                                            eChExp = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        item {
                            OutlinedButton(
                                onClick = { bibleSegments.add(BibleReadingSegment(book = "Genesis", startChapter = 1, endChapter = 1)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(strings.addAnotherBook)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(shape = RoundedCornerShape(12.dp), color = LightBlueContainer, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = String.format(strings.totalChaptersCalculated, calculatedTotalBibleChapters),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    "prayer_alone" -> {
                        item {
                            Text(strings.typeOfPrayerFocus, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            val aloneTypes = listOf(
                                "Prayer Walk" to strings.prayerTypePrayerWalk,
                                "Intercession" to strings.prayerTypeIntercession,
                                "Personal Supplication" to strings.prayerTypePersonalSupplication,
                                "Spiritual Warfare" to strings.prayerTypeSpiritualWarfare,
                                "Praise & Adoration" to strings.prayerTypePraise,
                                "15-Minute Retreat" to strings.prayerType15MinRetreat,
                                "Bertoua Message" to strings.prayerTypeBertouaMessage,
                                "Thanksgiving" to strings.prayerTypeThanksgiving,
                                "Custom" to strings.prayerTypeCustom
                            )

                            var prayerExp by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = prayerExp, onExpandedChange = { prayerExp = !prayerExp }) {
                                OutlinedTextField(
                                    value = aloneTypes.find { it.first == prayerFocusType }?.second ?: prayerFocusType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(strings.prayerType) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = prayerExp) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(expanded = prayerExp, onDismissRequest = { prayerExp = false }) {
                                    aloneTypes.forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                prayerFocusType = key
                                                prayerExp = false
                                            }
                                        )
                                    }
                                }
                            }
                            if (prayerFocusType == "Custom") {
                                OutlinedTextField(
                                    value = customPrayerFocus,
                                    onValueChange = { customPrayerFocus = it },
                                    label = { Text(strings.customPrayerFocusPrompt) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            OutlinedTextField(
                                value = prayerTopicsCountText,
                                onValueChange = { prayerTopicsCountText = it },
                                label = { Text(strings.numTopicsRecorded) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    "prayer_with_others" -> {
                        item {
                            Text(strings.typeOfPrayerFocus, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            val groupTypes = listOf(
                                "Prayer Walk" to strings.prayerTypePrayerWalk,
                                "Prayer Night" to strings.prayerTypePrayerNight,
                                "Prayer Siege" to strings.prayerTypePrayerSiege,
                                "Cell Group" to strings.prayerTypeCellGroup,
                                "Family Altar" to strings.prayerTypeFamilyAltar,
                                "Corporate Assembly" to strings.prayerTypeCorporateAssembly,
                                "Intercessory Chain" to strings.prayerTypeIntercessoryChain,
                                "Intercession" to strings.prayerTypeIntercession,
                                "Custom" to strings.prayerTypeCustom
                            )

                            var groupExp by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = groupExp, onExpandedChange = { groupExp = !groupExp }) {
                                OutlinedTextField(
                                    value = groupTypes.find { it.first == prayerFocusType }?.second ?: prayerFocusType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text(strings.prayerType) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupExp) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(expanded = groupExp, onDismissRequest = { groupExp = false }) {
                                    groupTypes.forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                prayerFocusType = key
                                                groupExp = false
                                            }
                                        )
                                    }
                                }
                            }
                            if (prayerFocusType == "Custom") {
                                OutlinedTextField(
                                    value = customPrayerFocus,
                                    onValueChange = { customPrayerFocus = it },
                                    label = { Text(strings.customPrayerFocusPrompt) },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            OutlinedTextField(
                                value = prayerParticipantsCountText,
                                onValueChange = { prayerParticipantsCountText = it },
                                label = { Text(strings.participantsCount) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    }

                    "christian_lit" -> {
                        item {
                            OutlinedTextField(
                                value = bookTitle,
                                onValueChange = { bookTitle = it },
                                label = { Text(strings.bookTitle) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = bookAuthor,
                                onValueChange = { bookAuthor = it },
                                label = { Text(strings.author) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = startPageText,
                                    onValueChange = { startPageText = it },
                                    label = { Text(strings.startPageLabel) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = endPageText,
                                    onValueChange = { endPageText = it },
                                    label = { Text(strings.endPageLabel) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                            Surface(shape = RoundedCornerShape(12.dp), color = LightBlueContainer, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = String.format(strings.totalPagesCalculated, calculatedLiteraturePages),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    "retreats" -> {
                        item {
                            OutlinedTextField(
                                value = retreatFocus,
                                onValueChange = { retreatFocus = it },
                                label = { Text(strings.retreatFocusLabel) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Text(
                                text = strings.retreatActivitiesChecklist,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )
                            val retreatItems = listOf(
                                "Solitude & Silence" to strings.retreatSolitude,
                                "Complete Fasting" to strings.retreatFasting,
                                "Extended Prayer & Intercession" to strings.retreatExtendedPrayer,
                                "Intensive Word Study" to strings.retreatWordStudy,
                                "Meditation & Journaling" to strings.retreatMeditation,
                                "Spiritual Examination & Repentance" to strings.retreatExamination,
                                "Waiting on the Holy Spirit" to strings.retreatWaitingSpirit
                            )
                            retreatItems.forEach { (key, label) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { retreatActivities[key] = !(retreatActivities[key] ?: false) }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = retreatActivities[key] ?: false,
                                        onCheckedChange = { retreatActivities[key] = it }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(label, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }

                    "giving" -> {
                        item {
                            OutlinedTextField(
                                value = givingIncomeText,
                                onValueChange = { givingIncomeText = it },
                                label = { Text(strings.amountEarnedLabel) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = givingAmountText,
                                onValueChange = { givingAmountText = it },
                                label = { Text(strings.amountGivenLabel) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Surface(shape = RoundedCornerShape(12.dp), color = LightBlueContainer, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = String.format(strings.givingPercentageCalculated, calculatedGivingPercentage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlueDark,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    "soul_winning" -> {
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = preachedToCountText,
                                    onValueChange = { preachedToCountText = it },
                                    label = { Text(strings.peoplePreachedTo) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = convertedCountText,
                                    onValueChange = { convertedCountText = it },
                                    label = { Text(strings.peopleConverted) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    "ddewg" -> {
                        item {
                            OutlinedTextField(
                                value = reflection,
                                onValueChange = { reflection = it },
                                label = { Text(strings.inspirationForMeditation) },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(strings.reflectionNotes) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_timer_notes_input"),
                        minLines = 3
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(strings.cancelTimer)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val nowIso = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                val endMs = System.currentTimeMillis()
                                val timeFormatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                val endFormatted = timeFormatter.format(java.util.Date(endMs))
                                val startFormatted = timeFormatter.format(java.util.Date(endMs - elapsedSeconds * 1000L))

                                val entry = when (domainId) {
                                    "bible_reading" -> {
                                        val combinedBook = bibleSegments.joinToString(", ") { "${it.book} ${it.startChapter}-${it.endChapter}" }
                                        AccountabilityEntryEntity(
                                            id = UUID.randomUUID().toString(),
                                            userId = "guest_user",
                                            domainId = domainId,
                                            dateIso = nowIso,
                                            timestampMs = endMs,
                                            timezoneId = java.time.ZoneId.systemDefault().id,
                                            durationSeconds = elapsedSeconds,
                                            startTimeIso = startFormatted,
                                            endTimeIso = endFormatted,
                                            bibleBook = combinedBook,
                                            startChapter = bibleSegments.firstOrNull()?.startChapter ?: 1,
                                            endChapter = bibleSegments.lastOrNull()?.endChapter ?: 1,
                                            chaptersCount = calculatedTotalBibleChapters,
                                            notes = notes
                                        )
                                    }
                                    "prayer_alone" -> {
                                        val effectiveFocus = if (prayerFocusType == "Custom") customPrayerFocus else prayerFocusType
                                        AccountabilityEntryEntity(
                                            id = UUID.randomUUID().toString(),
                                            userId = "guest_user",
                                            domainId = domainId,
                                            dateIso = nowIso,
                                            timestampMs = endMs,
                                            timezoneId = java.time.ZoneId.systemDefault().id,
                                            durationSeconds = elapsedSeconds,
                                            startTimeIso = startFormatted,
                                            endTimeIso = endFormatted,
                                            prayerType = effectiveFocus,
                                            prayerTopicsCount = prayerTopicsCountText.toIntOrNull() ?: 0,
                                            notes = notes
                                        )
                                    }
                                    "prayer_with_others" -> {
                                        val effectiveFocus = if (prayerFocusType == "Custom") customPrayerFocus else prayerFocusType
                                        AccountabilityEntryEntity(
                                            id = UUID.randomUUID().toString(),
                                            userId = "guest_user",
                                            domainId = domainId,
                                            dateIso = nowIso,
                                            timestampMs = endMs,
                                            timezoneId = java.time.ZoneId.systemDefault().id,
                                            durationSeconds = elapsedSeconds,
                                            startTimeIso = startFormatted,
                                            endTimeIso = endFormatted,
                                            prayerType = effectiveFocus,
                                            prayerParticipantsCount = prayerParticipantsCountText.toIntOrNull() ?: 1,
                                            notes = notes
                                        )
                                    }
                                    "christian_lit" -> {
                                        val sPage = startPageText.toIntOrNull() ?: 1
                                        val ePage = endPageText.toIntOrNull() ?: sPage
                                        AccountabilityEntryEntity(
                                            id = UUID.randomUUID().toString(),
                                            userId = "guest_user",
                                            domainId = domainId,
                                            dateIso = nowIso,
                                            timestampMs = endMs,
                                            timezoneId = java.time.ZoneId.systemDefault().id,
                                            durationSeconds = elapsedSeconds,
                                            startTimeIso = startFormatted,
                                            endTimeIso = endFormatted,
                                            bookTitle = bookTitle,
                                            bookAuthor = bookAuthor,
                                            startPage = sPage,
                                            endPage = ePage,
                                            pagesRead = calculatedLiteraturePages,
                                            bookTimesRead = timesReadText.toIntOrNull() ?: 1,
                                            notes = notes
                                        )
                                    }
                                    "retreats" -> {
                                        val selectedActList = retreatActivities.filter { it.value }.keys.toList()
                                        AccountabilityEntryEntity(
                                            id = UUID.randomUUID().toString(),
                                            userId = "guest_user",
                                            domainId = domainId,
                                            dateIso = nowIso,
                                            timestampMs = endMs,
                                            timezoneId = java.time.ZoneId.systemDefault().id,
                                            durationSeconds = elapsedSeconds,
                                            startTimeIso = startFormatted,
                                            endTimeIso = endFormatted,
                                            retreatFocus = retreatFocus,
                                            retreatActivitiesJson = selectedActList.joinToString(";;"),
                                            notes = notes
                                        )
                                    }
                                    "soul_winning" -> {
                                        AccountabilityEntryEntity(
                                            id = UUID.randomUUID().toString(),
                                            userId = "guest_user",
                                            domainId = domainId,
                                            dateIso = nowIso,
                                            timestampMs = endMs,
                                            timezoneId = java.time.ZoneId.systemDefault().id,
                                            durationSeconds = elapsedSeconds,
                                            startTimeIso = startFormatted,
                                            endTimeIso = endFormatted,
                                            preachedToCount = preachedToCountText.toIntOrNull() ?: 0,
                                            convertedCount = convertedCountText.toIntOrNull() ?: 0,
                                            notes = notes
                                        )
                                    }
                                    "giving" -> {
                                        val inc = givingIncomeText.toDoubleOrNull() ?: 0.0
                                        val amt = givingAmountText.toDoubleOrNull() ?: 0.0
                                        AccountabilityEntryEntity(
                                            id = UUID.randomUUID().toString(),
                                            userId = "guest_user",
                                            domainId = domainId,
                                            dateIso = nowIso,
                                            timestampMs = endMs,
                                            timezoneId = java.time.ZoneId.systemDefault().id,
                                            durationSeconds = elapsedSeconds,
                                            givingAmount = amt,
                                            givingIncomeReference = inc,
                                            givingPercentage = calculatedGivingPercentage,
                                            givingType = givingType,
                                            notes = notes
                                        )
                                    }
                                    "ddewg" -> {
                                        AccountabilityEntryEntity(
                                            id = UUID.randomUUID().toString(),
                                            userId = "guest_user",
                                            domainId = domainId,
                                            dateIso = nowIso,
                                            timestampMs = endMs,
                                            timezoneId = java.time.ZoneId.systemDefault().id,
                                            durationSeconds = elapsedSeconds,
                                            startTimeIso = startFormatted,
                                            endTimeIso = endFormatted,
                                            reflection = reflection,
                                            notes = notes
                                        )
                                    }
                                    else -> {
                                        AccountabilityEntryEntity(
                                            id = UUID.randomUUID().toString(),
                                            userId = "guest_user",
                                            domainId = domainId,
                                            dateIso = nowIso,
                                            timestampMs = endMs,
                                            timezoneId = java.time.ZoneId.systemDefault().id,
                                            durationSeconds = elapsedSeconds,
                                            startTimeIso = startFormatted,
                                            endTimeIso = endFormatted,
                                            notes = notes,
                                            reflection = reflection
                                        )
                                    }
                                }

                                onConfirm(entry)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.testTag("save_timer_confirm_button")
                        ) {
                            Text(strings.save, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
