package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.localization.AppStrings
import com.example.core.util.HapticHelper
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.domain.models.BibleMetadata
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainDetailScreen(
    domainId: String,
    strings: AppStrings,
    onNavigateToTimer: (String) -> Unit,
    onSaveEntry: (AccountabilityEntryEntity) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedDateIso by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var notes by remember { mutableStateOf("") }

    // Start Time & Stop Time
    var startTimeText by remember { mutableStateOf("06:00") }
    var stopTimeText by remember { mutableStateOf("07:00") }

    // Prayer Domain Fields
    var selectedPrayerType by remember { mutableStateOf("Intercession") } // Thanksgiving, Request, 15-Minute Retreat, Bertoua Message, Intercession, Worship
    var prayerTopicsCountText by remember { mutableStateOf("1") }

    // DDEWG Field
    var ddewgInspirationText by remember { mutableStateOf("") }

    // Bible Reading / Bible Mem Dropdowns
    var selectedBibleBook by remember { mutableStateOf("Genesis") }
    var selectedStartChapter by remember { mutableStateOf("1") }
    var selectedEndChapter by remember { mutableStateOf("1") }
    var bibleMemVerse by remember { mutableStateOf("1-5") }

    var bookBookDropdownExpanded by remember { mutableStateOf(false) }
    var startChapterDropdownExpanded by remember { mutableStateOf(false) }
    var endChapterDropdownExpanded by remember { mutableStateOf(false) }

    // Christian Lit Fields
    var bookTitle by remember { mutableStateOf("") }
    var bookAuthor by remember { mutableStateOf("") }
    var pagesReadText by remember { mutableStateOf("10") }
    var timesReadText by remember { mutableStateOf("1") }
    var pagesMemorizedText by remember { mutableStateOf("5") }

    // Fasting Fields
    var fastingDaysText by remember { mutableStateOf("1") }
    var selectedFastingType by remember { mutableStateOf("Complete Fast") } // "Complete Fast" or "Partial Fast"

    // Giving Fields
    var givingAmountText by remember { mutableStateOf("0.0") }
    var givingIncomeText by remember { mutableStateOf("0.0") }
    var givingPercentageText by remember { mutableStateOf("10") }
    var givingType by remember { mutableStateOf("Tithe") }

    // Soul Winning Fields
    var preachedToCountText by remember { mutableStateOf("1") }
    var convertedCountText by remember { mutableStateOf("0") }
    var waterBaptizedText by remember { mutableStateOf("0") }
    var holySpiritBaptizedText by remember { mutableStateOf("0") }

    var isSaved by remember { mutableStateOf(false) }

    val currentBookInfo = remember(selectedBibleBook) {
        BibleMetadata.BOOKS.find { it.name.equals(selectedBibleBook, ignoreCase = true) }
            ?: BibleMetadata.BOOKS.first()
    }
    val maxChapters = currentBookInfo.chapters

    // Automatic Duration Calculation from Start & Stop Times
    val calculatedDurationMinutes = remember(startTimeText, stopTimeText) {
        try {
            val startParts = startTimeText.split(":").map { it.trim().toInt() }
            val stopParts = stopTimeText.split(":").map { it.trim().toInt() }
            if (startParts.size == 2 && stopParts.size == 2) {
                val startTotalMin = startParts[0] * 60 + startParts[1]
                val stopTotalMin = stopParts[0] * 60 + stopParts[1]
                val diff = if (stopTotalMin >= startTotalMin) stopTotalMin - startTotalMin else (stopTotalMin + 1440) - startTotalMin
                diff.coerceAtLeast(1)
            } else 60
        } catch (e: Exception) {
            60
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Subtle Background Vector Graphic
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            drawCircle(
                color = PrimaryBlue.copy(alpha = 0.04f),
                radius = w * 0.5f,
                center = androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.15f)
            )
            drawCircle(
                color = AccentPurple.copy(alpha = 0.03f),
                radius = w * 0.6f,
                center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.85f)
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = strings.getDomainTitleById(domainId).uppercase(),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("domain_detail_back_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Live Timer Launcher Card for Prayer, DDEWG, Bible Reading & Christian Literature
                if (domainId == "ddewg" || domainId.startsWith("prayer") || domainId == "bible_reading" || domainId == "christian_lit") {
                    item {
                        Surface(
                            shape = RoundedCornerShape(28.dp),
                            color = LightBlueContainer,
                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(PrimaryBlue),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.White)
                                    }
                                    Column {
                                        Text(
                                            text = strings.liveTimerMode,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueDark
                                        )
                                        Text(
                                            text = strings.liveTimerDesc,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PrimaryBlue
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        HapticHelper.vibrateHeavyClick(context)
                                        onNavigateToTimer(domainId)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("start_live_timer_button")
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.startLiveSessionTimer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Manual Entry Form Card
                item {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, DividerColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = strings.logActivityRecord,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            // Date Selector Bar
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(strings.dateOfActivity, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            val current = LocalDate.parse(selectedDateIso, DateTimeFormatter.ISO_LOCAL_DATE)
                                            selectedDateIso = current.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                        },
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                                        Text(strings.prevDay)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = LightBlueContainer,
                                        modifier = Modifier.testTag("entry_date_selector")
                                    ) {
                                        Text(
                                            text = selectedDateIso,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueDark,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val current = LocalDate.parse(selectedDateIso, DateTimeFormatter.ISO_LOCAL_DATE)
                                            selectedDateIso = current.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                        },
                                        shape = RoundedCornerShape(20.dp)
                                    ) {
                                        Text(strings.nextDay)
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                                    }
                                }
                            }

                            if (domainId != "fasting" && domainId != "giving") {
                                // Start Time & Stop Time Duration Input
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(strings.timeAndDuration, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = startTimeText,
                                            onValueChange = { startTimeText = it },
                                            label = { Text(strings.startTimePlaceholder) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("entry_start_time"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = stopTimeText,
                                            onValueChange = { stopTimeText = it },
                                            label = { Text(strings.stopTimePlaceholder) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("entry_stop_time"),
                                            singleLine = true
                                        )
                                    }
                                    Text(
                                        text = String.format(strings.calculatedDurationFormat, calculatedDurationMinutes),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PrimaryBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Prayer Domain Specific Fields
                            if (domainId.startsWith("prayer")) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(strings.typeOfPrayerFocus, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    val prayerTypes = listOf("Thanksgiving", "Request", "15-Min Retreat", "Bertoua Message", "Intercession", "Worship")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        prayerTypes.take(3).forEach { pType ->
                                            FilterChip(
                                                selected = selectedPrayerType == pType,
                                                onClick = { selectedPrayerType = pType },
                                                label = { Text(strings.getPrayerTypeDisplayName(pType), style = MaterialTheme.typography.labelSmall) }
                                            )
                                        }
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        prayerTypes.drop(3).forEach { pType ->
                                            FilterChip(
                                                selected = selectedPrayerType == pType,
                                                onClick = { selectedPrayerType = pType },
                                                label = { Text(strings.getPrayerTypeDisplayName(pType), style = MaterialTheme.typography.labelSmall) }
                                            )
                                        }
                                    }

                                    if (selectedPrayerType == "Thanksgiving" || selectedPrayerType == "Request") {
                                        OutlinedTextField(
                                            value = prayerTopicsCountText,
                                            onValueChange = { prayerTopicsCountText = it },
                                            label = { Text(strings.numTopicsRecorded) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("entry_prayer_topics_count"),
                                            singleLine = true
                                        )
                                    }
                                }
                            }

                            when (domainId) {
                                "ddewg" -> {
                                    OutlinedTextField(
                                        value = ddewgInspirationText,
                                        onValueChange = { ddewgInspirationText = it },
                                        label = { Text(strings.inspirationForMeditation) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("entry_ddewg_inspiration"),
                                        singleLine = false,
                                        minLines = 2
                                    )
                                }

                                "bible_reading", "bible_mem" -> {
                                    // Book Selection Dropdown
                                    ExposedDropdownMenuBox(
                                        expanded = bookBookDropdownExpanded,
                                        onExpandedChange = { bookBookDropdownExpanded = !bookBookDropdownExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = strings.getBibleBookName(selectedBibleBook),
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(strings.selectBibleBook) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookBookDropdownExpanded) },
                                            modifier = Modifier
                                                .menuAnchor()
                                                .fillMaxWidth()
                                                .testTag("entry_bible_book_select"),
                                            singleLine = true
                                        )
                                        ExposedDropdownMenu(
                                            expanded = bookBookDropdownExpanded,
                                            onDismissRequest = { bookBookDropdownExpanded = false }
                                        ) {
                                            BibleMetadata.BOOKS.forEach { book ->
                                                val translatedBookName = strings.getBibleBookName(book.name)
                                                DropdownMenuItem(
                                                    text = { Text("$translatedBookName (${book.chapters} ${strings.chaptersReadLabel.substringBefore("/")})") },
                                                    onClick = {
                                                        selectedBibleBook = book.name
                                                        selectedStartChapter = "1"
                                                        selectedEndChapter = "1"
                                                        bookBookDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        // Start Chapter Dropdown
                                        ExposedDropdownMenuBox(
                                            expanded = startChapterDropdownExpanded,
                                            onExpandedChange = { startChapterDropdownExpanded = !startChapterDropdownExpanded },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            OutlinedTextField(
                                                value = "Ch. $selectedStartChapter",
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text(strings.startChapter) },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startChapterDropdownExpanded) },
                                                modifier = Modifier
                                                    .menuAnchor()
                                                    .fillMaxWidth()
                                                    .testTag("entry_start_chapter_select"),
                                                singleLine = true
                                            )
                                            ExposedDropdownMenu(
                                                expanded = startChapterDropdownExpanded,
                                                onDismissRequest = { startChapterDropdownExpanded = false }
                                            ) {
                                                (1..maxChapters).forEach { ch ->
                                                    DropdownMenuItem(
                                                        text = { Text(String.format(strings.chapterNumberFormat, ch)) },
                                                        onClick = {
                                                            selectedStartChapter = ch.toString()
                                                            if ((selectedEndChapter.toIntOrNull() ?: 1) < ch) {
                                                                selectedEndChapter = ch.toString()
                                                            }
                                                            startChapterDropdownExpanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        if (domainId == "bible_reading") {
                                            // End Chapter Dropdown
                                            ExposedDropdownMenuBox(
                                                expanded = endChapterDropdownExpanded,
                                                onExpandedChange = { endChapterDropdownExpanded = !endChapterDropdownExpanded },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                OutlinedTextField(
                                                    value = "Ch. $selectedEndChapter",
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    label = { Text(strings.endChapter) },
                                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endChapterDropdownExpanded) },
                                                    modifier = Modifier
                                                        .menuAnchor()
                                                        .fillMaxWidth()
                                                        .testTag("entry_end_chapter_select"),
                                                    singleLine = true
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = endChapterDropdownExpanded,
                                                    onDismissRequest = { endChapterDropdownExpanded = false }
                                                ) {
                                                    ((selectedStartChapter.toIntOrNull() ?: 1)..maxChapters).forEach { ch ->
                                                        DropdownMenuItem(
                                                            text = { Text(String.format(strings.chapterNumberFormat, ch)) },
                                                            onClick = {
                                                                selectedEndChapter = ch.toString()
                                                                endChapterDropdownExpanded = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            OutlinedTextField(
                                                value = bibleMemVerse,
                                                onValueChange = { bibleMemVerse = it },
                                                label = { Text(strings.versesPrompt) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("entry_bible_mem_verse"),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }

                                "christian_lit" -> {
                                    OutlinedTextField(
                                        value = bookTitle,
                                        onValueChange = { bookTitle = it },
                                        label = { Text(strings.bookTitle) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("entry_christian_lit_title"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = bookAuthor,
                                        onValueChange = { bookAuthor = it },
                                        label = { Text(strings.author) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("entry_christian_lit_author"),
                                        singleLine = true
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = pagesReadText,
                                            onValueChange = { pagesReadText = it },
                                            label = { Text(strings.pagesRead) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("entry_pages_read"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = timesReadText,
                                            onValueChange = { timesReadText = it },
                                            label = { Text(strings.timesReadPrompt) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("entry_times_read"),
                                            singleLine = true
                                        )
                                    }
                                }

                                "christian_lit_mem" -> {
                                    OutlinedTextField(
                                        value = bookTitle,
                                        onValueChange = { bookTitle = it },
                                        label = { Text(strings.bookTitle) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("entry_lit_mem_title"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = bookAuthor,
                                        onValueChange = { bookAuthor = it },
                                        label = { Text(strings.author) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("entry_lit_mem_author"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = pagesMemorizedText,
                                        onValueChange = { pagesMemorizedText = it },
                                        label = { Text(strings.pagesMemorizedPrompt) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("entry_pages_memorized"),
                                        singleLine = true
                                    )
                                }

                                "fasting" -> {
                                    Text(strings.typeOfFast, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        listOf("Complete Fast", "Partial Fast").forEach { type ->
                                            FilterChip(
                                                selected = selectedFastingType == type,
                                                onClick = { selectedFastingType = type },
                                                label = { Text(strings.getFastingTypeDisplayName(type), fontWeight = FontWeight.SemiBold) },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = PrimaryBlue,
                                                    selectedLabelColor = Color.White
                                                )
                                            )
                                        }
                                    }
                                    OutlinedTextField(
                                        value = fastingDaysText,
                                        onValueChange = { fastingDaysText = it },
                                        label = { Text(strings.fastingDurationDays) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("entry_fasting_days"),
                                        singleLine = true
                                    )
                                }

                                "giving" -> {
                                    OutlinedTextField(
                                        value = givingAmountText,
                                        onValueChange = { givingAmountText = it },
                                        label = { Text(strings.givingAmountLabel) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("entry_giving_amount"),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = givingType,
                                        onValueChange = { givingType = it },
                                        label = { Text(strings.givingTypeExtendedPlaceholder) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("entry_giving_type"),
                                        singleLine = true
                                    )
                                }

                                "soul_winning" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedTextField(
                                                value = preachedToCountText,
                                                onValueChange = { preachedToCountText = it },
                                                label = { Text(strings.peoplePreachedTo) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("entry_preached_to"),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = convertedCountText,
                                                onValueChange = { convertedCountText = it },
                                                label = { Text(strings.peopleConverted) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("entry_converts"),
                                                singleLine = true
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                            OutlinedTextField(
                                                value = waterBaptizedText,
                                                onValueChange = { waterBaptizedText = it },
                                                label = { Text(strings.waterBaptized) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("entry_water_baptized"),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = holySpiritBaptizedText,
                                                onValueChange = { holySpiritBaptizedText = it },
                                                label = { Text(strings.holySpiritBaptized) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("entry_holy_spirit_baptized"),
                                                singleLine = true
                                            )
                                        }
                                    }
                                }

                                else -> {
                                    // General Activity Details
                                }
                            }

                            // Expanded Activity Notes Field
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text(strings.activityNotesPrompt) },
                                minLines = 3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("entry_notes")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    val sCh = selectedStartChapter.toIntOrNull() ?: 1
                                    val eCh = selectedEndChapter.toIntOrNull() ?: 1
                                    val chCount = (eCh - sCh + 1).coerceAtLeast(1)
                                    val durSecs = (calculatedDurationMinutes * 60).toLong()

                                    val entry = AccountabilityEntryEntity(
                                        id = UUID.randomUUID().toString(),
                                        userId = "guest_user",
                                        domainId = domainId,
                                        dateIso = selectedDateIso,
                                        timestampMs = System.currentTimeMillis(),
                                        timezoneId = java.time.ZoneId.systemDefault().id,
                                        durationSeconds = if (domainId == "fasting" || domainId == "giving") 0L else durSecs,
                                        startTimeIso = if (domainId == "fasting" || domainId == "giving") "" else startTimeText,
                                        endTimeIso = if (domainId == "fasting" || domainId == "giving") "" else stopTimeText,
                                        prayerType = selectedPrayerType,
                                        prayerTopicsCount = prayerTopicsCountText.toIntOrNull() ?: 0,
                                        bibleBook = selectedBibleBook,
                                        startChapter = sCh,
                                        endChapter = eCh,
                                        chaptersCount = chCount,
                                        bibleMemBook = selectedBibleBook,
                                        bibleMemChapter = sCh,
                                        bibleMemVerse = bibleMemVerse,
                                        bookTitle = bookTitle,
                                        bookAuthor = bookAuthor,
                                        pagesRead = pagesReadText.toIntOrNull() ?: 0,
                                        bookTimesRead = timesReadText.toIntOrNull() ?: 1,
                                        pagesMemorized = pagesMemorizedText.toIntOrNull() ?: 0,
                                        preachedToCount = preachedToCountText.toIntOrNull() ?: 0,
                                        convertedCount = convertedCountText.toIntOrNull() ?: 0,
                                        waterBaptizedCount = waterBaptizedText.toIntOrNull() ?: 0,
                                        holySpiritBaptizedCount = holySpiritBaptizedText.toIntOrNull() ?: 0,
                                        givingAmount = givingAmountText.toDoubleOrNull() ?: 0.0,
                                        givingIncomeReference = givingIncomeText.toDoubleOrNull() ?: 0.0,
                                        givingPercentage = givingPercentageText.toDoubleOrNull() ?: 0.0,
                                        givingType = givingType,
                                        fastingDaysCount = fastingDaysText.toIntOrNull() ?: 1,
                                        fastingType = selectedFastingType,
                                        notes = notes,
                                        reflection = if (domainId == "ddewg") ddewgInspirationText else ""
                                    )
                                    onSaveEntry(entry)
                                    HapticHelper.vibrateSuccess(context)
                                    isSaved = true
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("save_manual_entry_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(strings.saveActivityRecord, fontWeight = FontWeight.Bold)
                            }

                            if (isSaved) {
                                Text(
                                    text = strings.activityRecordedSuccess,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = StatusSuccess,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
