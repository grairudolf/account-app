package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.core.localization.AppStrings
import com.example.core.util.HapticHelper
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.local.entities.DiscipleEntity
import com.example.domain.models.BibleMetadata
import com.example.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.UUID

data class BibleReadingSegment(
    val id: String = UUID.randomUUID().toString(),
    var book: String = "Genesis",
    var startChapter: Int = 1,
    var endChapter: Int = 1
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DomainDetailScreen(
    domainId: String,
    strings: AppStrings,
    disciples: List<DiscipleEntity> = emptyList(),
    onSaveDisciple: (DiscipleEntity) -> Unit = {},
    onUpdateDisciple: (DiscipleEntity) -> Unit = {},
    onDeleteDisciple: (DiscipleEntity) -> Unit = {},
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

    // Multi-Book Bible Reading Segments
    val bibleSegments = remember {
        mutableStateListOf(BibleReadingSegment(book = "Genesis", startChapter = 1, endChapter = 1))
    }

    // Bible Memory
    var bibleMemBook by remember { mutableStateOf("Romans") }
    var bibleMemChapter by remember { mutableStateOf(1) }
    var bibleMemVerse by remember { mutableStateOf("1-12") }

    // Prayer Domains
    var prayerFocusType by remember {
        mutableStateOf(
            if (domainId == "prayer_with_others") "Prayer Night" else "Intercession"
        )
    }
    var customPrayerFocus by remember { mutableStateOf("") }
    var prayerParticipantsCountText by remember { mutableStateOf("1") }
    var prayerTopicsCountText by remember { mutableStateOf("1") }

    // DDEWG Field
    var ddewgInspirationText by remember { mutableStateOf("") }

    // Christian Literature
    var bookTitle by remember { mutableStateOf("") }
    var bookAuthor by remember { mutableStateOf("") }
    var startPageText by remember { mutableStateOf("1") }
    var endPageText by remember { mutableStateOf("10") }
    var timesReadText by remember { mutableStateOf("1") }
    var pagesMemorizedText by remember { mutableStateOf("5") }

    // Fasting (Auto-calculated from Start Date & End Date)
    var fastingStartDateIso by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var fastingEndDateIso by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    val calculatedFastingDays = remember(fastingStartDateIso, fastingEndDateIso) {
        try {
            val s = LocalDate.parse(fastingStartDateIso)
            val e = LocalDate.parse(fastingEndDateIso)
            val d = ChronoUnit.DAYS.between(s, e) + 1
            if (d >= 1) d.toInt() else 1
        } catch (ex: Exception) {
            1
        }
    }
    var selectedFastingType by remember { mutableStateOf("Complete Fast") }
    var fastingPurpose by remember { mutableStateOf("") }

    // Making of Disciples (Discipleship Management & Session Logging)
    var discipleshipStartDateIso by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    var discipleshipEndDateIso by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)) }
    val calculatedDiscipleshipDays = remember(discipleshipStartDateIso, discipleshipEndDateIso) {
        try {
            val s = LocalDate.parse(discipleshipStartDateIso)
            val e = LocalDate.parse(discipleshipEndDateIso)
            val d = ChronoUnit.DAYS.between(s, e) + 1
            if (d >= 1) d.toInt() else 1
        } catch (ex: Exception) {
            1
        }
    }
    var selectedDiscipleId by remember { mutableStateOf<String?>(null) }
    var discipleshipTopicsCovered by remember { mutableStateOf("") }
    var showAddEditDiscipleDialog by remember { mutableStateOf(false) }
    var discipleToEdit by remember { mutableStateOf<DiscipleEntity?>(null) }
    var discipleToDelete by remember { mutableStateOf<DiscipleEntity?>(null) }

    // Giving to God
    var givingIncomeText by remember { mutableStateOf("") }
    var givingAmountText by remember { mutableStateOf("") }
    var givingType by remember { mutableStateOf("Tithe") }

    // Soul Winning
    var preachedToCountText by remember { mutableStateOf("1") }
    var convertedCountText by remember { mutableStateOf("0") }
    var waterBaptizedText by remember { mutableStateOf("0") }
    var holySpiritBaptizedText by remember { mutableStateOf("0") }

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

    var isSaved by remember { mutableStateOf(false) }

    // Automatic Duration Calculation
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

    // Calculated Literature Pages
    val calculatedLiteraturePages = remember(startPageText, endPageText) {
        val s = startPageText.toIntOrNull() ?: 1
        val e = endPageText.toIntOrNull() ?: s
        if (e >= s && s > 0) (e - s + 1) else 0
    }

    // Calculated Bible Chapters Total
    val calculatedTotalBibleChapters = remember(bibleSegments.toList()) {
        bibleSegments.sumOf { (it.endChapter - it.startChapter + 1).coerceAtLeast(1) }
    }

    // Calculated Giving Percentage
    val calculatedGivingPercentage = remember(givingIncomeText, givingAmountText) {
        val income = givingIncomeText.toDoubleOrNull() ?: 0.0
        val amount = givingAmountText.toDoubleOrNull() ?: 0.0
        if (income > 0.0) (amount / income) * 100.0 else 0.0
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
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
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Header Card with Live Timer CTA
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, DividerColor),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(LightBlueContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SelfImprovement,
                                        contentDescription = null,
                                        tint = PrimaryBlue,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = strings.getDomainTitleById(domainId),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = strings.getDomainDesc(domainId),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (domainId != "giving" && domainId != "fasting" && domainId != "making_disciples") {
                                Button(
                                    onClick = { onNavigateToTimer(domainId) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("start_live_timer_cta"),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.startTimer, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Manual Entry Form Card
                item {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, DividerColor),
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = strings.manualLogging,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlueDark
                            )

                            // Date Selector (for standard daily domains)
                            if (domainId != "fasting" && domainId != "making_disciples") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(strings.date, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        OutlinedIconButton(
                                            onClick = {
                                                val current = LocalDate.parse(selectedDateIso, DateTimeFormatter.ISO_LOCAL_DATE)
                                                selectedDateIso = current.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = LightBlueContainer,
                                            modifier = Modifier.testTag("entry_date_selector")
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            ) {
                                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = selectedDateIso,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlueDark
                                                )
                                            }
                                        }

                                        OutlinedIconButton(
                                            onClick = {
                                                val current = LocalDate.parse(selectedDateIso, DateTimeFormatter.ISO_LOCAL_DATE)
                                                selectedDateIso = current.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                                        }
                                    }
                                }
                            }

                            // Time & Duration for duration-based domains
                            if (domainId != "fasting" && domainId != "giving" && domainId != "making_disciples") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(strings.timeAndDuration, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = startTimeText,
                                            onValueChange = { startTimeText = it },
                                            label = { Text(strings.startTimePlaceholder, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("entry_start_time"),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = stopTimeText,
                                            onValueChange = { stopTimeText = it },
                                            label = { Text(strings.stopTimePlaceholder, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis) },
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

                            // Domain-Specific Form Fields (Strict Schema Isolation)
                            when (domainId) {
                                "bible_reading" -> {
                                    // Multi-Book Bible Reading Section
                                    Text(
                                        text = strings.bibleReading,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    bibleSegments.forEachIndexed { index, segment ->
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
                                                    Text(
                                                        text = "${strings.bookSegment} #${index + 1}",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryBlueDark
                                                    )
                                                    if (bibleSegments.size > 1) {
                                                        IconButton(
                                                            onClick = { bibleSegments.removeAt(index) },
                                                            modifier = Modifier.size(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.Delete, contentDescription = strings.removeBookSegment, tint = StatusError)
                                                        }
                                                    }
                                                }

                                                var bookExpanded by remember { mutableStateOf(false) }
                                                ExposedDropdownMenuBox(
                                                    expanded = bookExpanded,
                                                    onExpandedChange = { bookExpanded = !bookExpanded }
                                                ) {
                                                    OutlinedTextField(
                                                        value = strings.getBibleBookName(segment.book),
                                                        onValueChange = {},
                                                        readOnly = true,
                                                        label = { Text(strings.selectBibleBook) },
                                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookExpanded) },
                                                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                        singleLine = true
                                                    )
                                                    ExposedDropdownMenu(
                                                        expanded = bookExpanded,
                                                        onDismissRequest = { bookExpanded = false }
                                                    ) {
                                                        BibleMetadata.BOOKS.forEach { b ->
                                                            DropdownMenuItem(
                                                                text = { Text("${strings.getBibleBookName(b.name)} (${b.chapters} ch)") },
                                                                onClick = {
                                                                    bibleSegments[index] = segment.copy(
                                                                        book = b.name,
                                                                        startChapter = 1,
                                                                        endChapter = 1
                                                                    )
                                                                    bookExpanded = false
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                val bookInfo = BibleMetadata.BOOKS.find { it.name.equals(segment.book, ignoreCase = true) } ?: BibleMetadata.BOOKS.first()
                                                val maxCh = bookInfo.chapters

                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    var startChExpanded by remember { mutableStateOf(false) }
                                                    var endChExpanded by remember { mutableStateOf(false) }

                                                    ExposedDropdownMenuBox(
                                                        expanded = startChExpanded,
                                                        onExpandedChange = { startChExpanded = !startChExpanded },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        OutlinedTextField(
                                                            value = "Ch. ${segment.startChapter}",
                                                            onValueChange = {},
                                                            readOnly = true,
                                                            label = { Text(strings.startChapter) },
                                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startChExpanded) },
                                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                            singleLine = true
                                                        )
                                                        ExposedDropdownMenu(
                                                            expanded = startChExpanded,
                                                            onDismissRequest = { startChExpanded = false }
                                                        ) {
                                                            (1..maxCh).forEach { ch ->
                                                                DropdownMenuItem(
                                                                    text = { Text("Ch. $ch") },
                                                                    onClick = {
                                                                        val newEnd = if (segment.endChapter < ch) ch else segment.endChapter
                                                                        bibleSegments[index] = segment.copy(
                                                                            startChapter = ch,
                                                                            endChapter = newEnd
                                                                        )
                                                                        startChExpanded = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }

                                                    ExposedDropdownMenuBox(
                                                        expanded = endChExpanded,
                                                        onExpandedChange = { endChExpanded = !endChExpanded },
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        OutlinedTextField(
                                                            value = "Ch. ${segment.endChapter}",
                                                            onValueChange = {},
                                                            readOnly = true,
                                                            label = { Text(strings.endChapter) },
                                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endChExpanded) },
                                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                                            singleLine = true
                                                        )
                                                        ExposedDropdownMenu(
                                                            expanded = endChExpanded,
                                                            onDismissRequest = { endChExpanded = false }
                                                        ) {
                                                            (segment.startChapter..maxCh).forEach { ch ->
                                                                DropdownMenuItem(
                                                                    text = { Text("Ch. $ch") },
                                                                    onClick = {
                                                                        bibleSegments[index] = segment.copy(
                                                                            endChapter = ch
                                                                        )
                                                                        endChExpanded = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            bibleSegments.add(BibleReadingSegment(book = "Genesis", startChapter = 1, endChapter = 1))
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(strings.addAnotherBook)
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = LightBlueContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = String.format(strings.totalChaptersCalculated, calculatedTotalBibleChapters),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueDark,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                "prayer_alone" -> {
                                    Text(strings.typeOfPrayerFocus, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    val aloneTypes = listOf(
                                        "Intercession" to strings.prayerTypeIntercession,
                                        "Personal Supplication" to strings.prayerTypePersonalSupplication,
                                        "Spiritual Warfare" to strings.prayerTypeSpiritualWarfare,
                                        "Praise & Adoration" to strings.prayerTypePraise,
                                        "Prayer Walk" to strings.prayerTypePrayerWalk,
                                        "15-Minute Retreat" to strings.prayerType15MinRetreat,
                                        "Bertoua Message" to strings.prayerTypeBertouaMessage,
                                        "Thanksgiving" to strings.prayerTypeThanksgiving,
                                        "Custom" to strings.prayerTypeCustom
                                    )

                                    var prayerDropdownExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = prayerDropdownExpanded,
                                        onExpandedChange = { prayerDropdownExpanded = !prayerDropdownExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = aloneTypes.find { it.first == prayerFocusType }?.second ?: prayerFocusType,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(strings.prayerType) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = prayerDropdownExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            singleLine = true
                                        )
                                        ExposedDropdownMenu(
                                            expanded = prayerDropdownExpanded,
                                            onDismissRequest = { prayerDropdownExpanded = false }
                                        ) {
                                            aloneTypes.forEach { (key, label) ->
                                                DropdownMenuItem(
                                                    text = { Text(label) },
                                                    onClick = {
                                                        prayerFocusType = key
                                                        prayerDropdownExpanded = false
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

                                "prayer_with_others" -> {
                                    Text(strings.typeOfPrayerFocus, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    val groupTypes = listOf(
                                        "Prayer Night" to strings.prayerTypePrayerNight,
                                        "Prayer Siege" to strings.prayerTypePrayerSiege,
                                        "Cell Group" to strings.prayerTypeCellGroup,
                                        "Prayer Walk" to strings.prayerTypePrayerWalk,
                                        "Family Altar" to strings.prayerTypeFamilyAltar,
                                        "Corporate Assembly" to strings.prayerTypeCorporateAssembly,
                                        "Intercessory Chain" to strings.prayerTypeIntercessoryChain,
                                        "Intercession" to strings.prayerTypeIntercession,
                                        "Custom" to strings.prayerTypeCustom
                                    )

                                    var groupDropdownExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = groupDropdownExpanded,
                                        onExpandedChange = { groupDropdownExpanded = !groupDropdownExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = groupTypes.find { it.first == prayerFocusType }?.second ?: prayerFocusType,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(strings.prayerType) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = groupDropdownExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            singleLine = true
                                        )
                                        ExposedDropdownMenu(
                                            expanded = groupDropdownExpanded,
                                            onDismissRequest = { groupDropdownExpanded = false }
                                        ) {
                                            groupTypes.forEach { (key, label) ->
                                                DropdownMenuItem(
                                                    text = { Text(label) },
                                                    onClick = {
                                                        prayerFocusType = key
                                                        groupDropdownExpanded = false
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

                                "giving" -> {
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

                                    val givingTypes = listOf("Tithe", strings.givingTypeFirstFruits, "Free-will Offering", strings.givingTypeAlms, strings.givingTypeBuilding)
                                    var givingTypeDropdownExpanded by remember { mutableStateOf(false) }
                                    ExposedDropdownMenuBox(
                                        expanded = givingTypeDropdownExpanded,
                                        onExpandedChange = { givingTypeDropdownExpanded = !givingTypeDropdownExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = givingType,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(strings.givingTypeExtendedPlaceholder) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = givingTypeDropdownExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            singleLine = true
                                        )
                                        ExposedDropdownMenu(
                                            expanded = givingTypeDropdownExpanded,
                                            onDismissRequest = { givingTypeDropdownExpanded = false }
                                        ) {
                                            givingTypes.forEach { type ->
                                                DropdownMenuItem(
                                                    text = { Text(type) },
                                                    onClick = {
                                                        givingType = type
                                                        givingTypeDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (calculatedGivingPercentage >= 10.0) LightBlueContainer else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = String.format(strings.givingPercentageCalculated, calculatedGivingPercentage),
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlueDark
                                            )
                                            Text(
                                                text = strings.titheTargetLabel,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                "christian_lit" -> {
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = LightBlueContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = String.format(strings.totalPagesCalculated, calculatedLiteraturePages),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PrimaryBlueDark,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                "christian_lit_mem" -> {
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
                                    OutlinedTextField(
                                        value = pagesMemorizedText,
                                        onValueChange = { pagesMemorizedText = it },
                                        label = { Text(strings.pagesMemorizedPrompt) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }

                                "retreats" -> {
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

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        retreatItems.forEach { (key, label) ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { retreatActivities[key] = !(retreatActivities[key] ?: false) }
                                                    .padding(vertical = 4.dp, horizontal = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = retreatActivities[key] ?: false,
                                                    onCheckedChange = { retreatActivities[key] = it }
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(label, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }

                                "fasting" -> {
                                    Text(strings.typeOfFast, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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

                                    // Fasting Start Date & End Date (Auto-calculates number of days)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = strings.dateRangePeriod,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Start Date
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = LightBlueContainer,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        text = strings.startDateLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = PrimaryBlueDark
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                val cur = LocalDate.parse(fastingStartDateIso)
                                                                fastingStartDateIso = cur.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        }
                                                        Text(
                                                            text = fastingStartDateIso,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                val cur = LocalDate.parse(fastingStartDateIso)
                                                                fastingStartDateIso = cur.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }

                                            // End Date
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = LightBlueContainer,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        text = strings.endDateLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = PrimaryBlueDark
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                val cur = LocalDate.parse(fastingEndDateIso)
                                                                fastingEndDateIso = cur.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        }
                                                        Text(
                                                            text = fastingEndDateIso,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                val cur = LocalDate.parse(fastingEndDateIso)
                                                                fastingEndDateIso = cur.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Auto-calculated days badge
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = PrimaryBlueDark.copy(alpha = 0.08f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                                Text(
                                                    text = String.format(strings.calculatedDaysFormat, calculatedFastingDays),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlueDark
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = fastingPurpose,
                                        onValueChange = { fastingPurpose = it },
                                        label = { Text(strings.purpose) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }

                                "soul_winning" -> {
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = waterBaptizedText,
                                            onValueChange = { waterBaptizedText = it },
                                            label = { Text(strings.waterBaptized) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = holySpiritBaptizedText,
                                            onValueChange = { holySpiritBaptizedText = it },
                                            label = { Text(strings.holySpiritBaptized) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }
                                }

                                "ddewg" -> {
                                    OutlinedTextField(
                                        value = ddewgInspirationText,
                                        onValueChange = { ddewgInspirationText = it },
                                        label = { Text(strings.inspirationForMeditation) },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )
                                }

                                "making_disciples" -> {
                                    // 1. My Disciples Section
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = strings.myDisciplesTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = PrimaryBlueDark
                                            )
                                            Text(
                                                text = "${disciples.size} ${if (disciples.size == 1) "disciple" else "disciples"}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                discipleToEdit = null
                                                showAddEditDiscipleDialog = true
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(strings.addNewDisciple, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    if (disciples.isEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(16.dp),
                                            color = LightBlueContainer.copy(alpha = 0.5f),
                                            border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(20.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Groups,
                                                    contentDescription = null,
                                                    tint = PrimaryBlue,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Text(
                                                    text = strings.noDisciplesYet,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = PrimaryBlueDark
                                                )
                                                Text(
                                                    text = strings.noDisciplesDesc,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                )
                                            }
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            disciples.forEach { disc ->
                                                Surface(
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = MaterialTheme.colorScheme.surface,
                                                    border = BorderStroke(1.dp, DividerColor),
                                                    shadowElevation = 1.dp,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(14.dp),
                                                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                                                        .background(LightBlueContainer),
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.Person,
                                                                        contentDescription = null,
                                                                        tint = PrimaryBlue,
                                                                        modifier = Modifier.size(20.dp)
                                                                    )
                                                                }
                                                                Column {
                                                                    Text(
                                                                        text = disc.name,
                                                                        style = MaterialTheme.typography.titleSmall,
                                                                        fontWeight = FontWeight.Bold
                                                                    )
                                                                    if (disc.phone.isNotBlank()) {
                                                                        Text(
                                                                            text = disc.phone,
                                                                            style = MaterialTheme.typography.bodySmall,
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Surface(
                                                                    shape = RoundedCornerShape(8.dp),
                                                                    color = PrimaryBlueDark.copy(alpha = 0.1f)
                                                                ) {
                                                                    Text(
                                                                        text = disc.status,
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = PrimaryBlueDark,
                                                                        fontWeight = FontWeight.Bold,
                                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                                    )
                                                                }
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                IconButton(
                                                                    onClick = {
                                                                        discipleToEdit = disc
                                                                        showAddEditDiscipleDialog = true
                                                                    },
                                                                    modifier = Modifier.size(32.dp)
                                                                ) {
                                                                    Icon(Icons.Default.Edit, contentDescription = "Edit Disciple", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                                                }
                                                                IconButton(
                                                                    onClick = {
                                                                        discipleToDelete = disc
                                                                    },
                                                                    modifier = Modifier.size(32.dp)
                                                                ) {
                                                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Disciple", tint = StatusError, modifier = Modifier.size(18.dp))
                                                                }
                                                            }
                                                        }

                                                        if (disc.prayerTopics.isNotBlank()) {
                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = LightBlueContainer.copy(alpha = 0.5f),
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) {
                                                                Column(modifier = Modifier.padding(8.dp)) {
                                                                    Text(
                                                                        text = "🙏 ${strings.prayerTopicsLabel}:",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = PrimaryBlueDark
                                                                    )
                                                                    Text(
                                                                        text = disc.prayerTopics,
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        if (disc.notes.isNotBlank()) {
                                                            Text(
                                                                text = "📝 ${disc.notes}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    HorizontalDivider(color = DividerColor)

                                    // 2. Discipleship Session Log
                                    Text(
                                        text = strings.logDiscipleshipSession,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlueDark
                                    )

                                    // Disciple Selector
                                    var discipleMenuExpanded by remember { mutableStateOf(false) }
                                    val currentSelectedDisciple = disciples.find { it.id == selectedDiscipleId }
                                    ExposedDropdownMenuBox(
                                        expanded = discipleMenuExpanded,
                                        onExpandedChange = { discipleMenuExpanded = !discipleMenuExpanded }
                                    ) {
                                        OutlinedTextField(
                                            value = currentSelectedDisciple?.name ?: strings.generalOrAllDisciples,
                                            onValueChange = {},
                                            readOnly = true,
                                            label = { Text(strings.selectDisciple) },
                                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = discipleMenuExpanded) },
                                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                                            singleLine = true
                                        )
                                        ExposedDropdownMenu(
                                            expanded = discipleMenuExpanded,
                                            onDismissRequest = { discipleMenuExpanded = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(strings.generalOrAllDisciples, fontWeight = FontWeight.Bold) },
                                                onClick = {
                                                    selectedDiscipleId = null
                                                    discipleMenuExpanded = false
                                                }
                                            )
                                            disciples.forEach { d ->
                                                DropdownMenuItem(
                                                    text = { Text("${d.name} (${d.status})") },
                                                    onClick = {
                                                        selectedDiscipleId = d.id
                                                        discipleMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    // Start Date and End Date Selector (Auto-calculates number of days)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = strings.dateRangePeriod,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Start Date
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = LightBlueContainer,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        text = strings.startDateLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = PrimaryBlueDark
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                val cur = LocalDate.parse(discipleshipStartDateIso)
                                                                discipleshipStartDateIso = cur.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        }
                                                        Text(
                                                            text = discipleshipStartDateIso,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                val cur = LocalDate.parse(discipleshipStartDateIso)
                                                                discipleshipStartDateIso = cur.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }

                                            // End Date
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = LightBlueContainer,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Text(
                                                        text = strings.endDateLabel,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = PrimaryBlueDark
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        IconButton(
                                                            onClick = {
                                                                val cur = LocalDate.parse(discipleshipEndDateIso)
                                                                discipleshipEndDateIso = cur.minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronLeft, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        }
                                                        Text(
                                                            text = discipleshipEndDateIso,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        IconButton(
                                                            onClick = {
                                                                val cur = LocalDate.parse(discipleshipEndDateIso)
                                                                discipleshipEndDateIso = cur.plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE)
                                                            },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Auto-calculated days badge
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = PrimaryBlueDark.copy(alpha = 0.08f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.DateRange, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                                                Text(
                                                    text = String.format(strings.calculatedDaysFormat, calculatedDiscipleshipDays),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = PrimaryBlueDark
                                                )
                                            }
                                        }
                                    }

                                    OutlinedTextField(
                                        value = discipleshipTopicsCovered,
                                        onValueChange = { discipleshipTopicsCovered = it },
                                        label = { Text(strings.topicsCoveredLabel) },
                                        modifier = Modifier.fillMaxWidth(),
                                        minLines = 2
                                    )
                                }

                                "bible_mem" -> {
                                    OutlinedTextField(
                                        value = bibleMemBook,
                                        onValueChange = { bibleMemBook = it },
                                        label = { Text(strings.selectBibleBook) },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        OutlinedTextField(
                                            value = bibleMemChapter.toString(),
                                            onValueChange = { bibleMemChapter = it.toIntOrNull() ?: 1 },
                                            label = { Text(strings.startChapter) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                        OutlinedTextField(
                                            value = bibleMemVerse,
                                            onValueChange = { bibleMemVerse = it },
                                            label = { Text(strings.versesPrompt) },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )
                                    }
                                }
                            }

                            // Activity Notes Field
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text(strings.activityNotesPrompt) },
                                minLines = 3,
                                modifier = Modifier.fillMaxWidth().testTag("entry_notes")
                            )

                            Button(
                                onClick = {
                                    val durSecs = (calculatedDurationMinutes * 60).toLong()

                                    // Build isolated entity according to DomainType
                                    val entry = when (domainId) {
                                        "bible_reading" -> {
                                            val combinedBook = bibleSegments.joinToString(", ") { "${it.book} ${it.startChapter}-${it.endChapter}" }
                                            AccountabilityEntryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = "guest_user",
                                                domainId = domainId,
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
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
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
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
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
                                                prayerType = effectiveFocus,
                                                prayerParticipantsCount = prayerParticipantsCountText.toIntOrNull() ?: 1,
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
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = 0L,
                                                givingAmount = amt,
                                                givingIncomeReference = inc,
                                                givingPercentage = calculatedGivingPercentage,
                                                givingType = givingType,
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
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
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
                                            val actJson = selectedActList.joinToString(";;")
                                            AccountabilityEntryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = "guest_user",
                                                domainId = domainId,
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
                                                retreatFocus = retreatFocus,
                                                retreatActivitiesJson = actJson,
                                                notes = notes
                                            )
                                        }
                                        "fasting" -> {
                                            AccountabilityEntryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = "guest_user",
                                                domainId = domainId,
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = 0L,
                                                fastingDaysCount = calculatedFastingDays,
                                                fastingType = selectedFastingType,
                                                notes = if (fastingPurpose.isNotBlank()) "$fastingPurpose\n$notes" else notes
                                            )
                                        }
                                        "making_disciples" -> {
                                            val discName = disciples.find { it.id == selectedDiscipleId }?.name ?: "General"
                                            val combinedNotes = buildString {
                                                append("Disciple: $discName")
                                                if (discipleshipTopicsCovered.isNotBlank()) {
                                                    append("\nTopics: $discipleshipTopicsCovered")
                                                }
                                                if (notes.isNotBlank()) {
                                                    append("\n$notes")
                                                }
                                            }
                                            AccountabilityEntryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = "guest_user",
                                                domainId = domainId,
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = 0L,
                                                fastingDaysCount = calculatedDiscipleshipDays,
                                                notes = combinedNotes
                                            )
                                        }
                                        "soul_winning" -> {
                                            AccountabilityEntryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = "guest_user",
                                                domainId = domainId,
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
                                                preachedToCount = preachedToCountText.toIntOrNull() ?: 0,
                                                convertedCount = convertedCountText.toIntOrNull() ?: 0,
                                                waterBaptizedCount = waterBaptizedText.toIntOrNull() ?: 0,
                                                holySpiritBaptizedCount = holySpiritBaptizedText.toIntOrNull() ?: 0,
                                                notes = notes
                                            )
                                        }
                                        "ddewg" -> {
                                            AccountabilityEntryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = "guest_user",
                                                domainId = domainId,
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
                                                reflection = ddewgInspirationText,
                                                notes = notes
                                            )
                                        }
                                        "christian_lit_mem" -> {
                                            AccountabilityEntryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = "guest_user",
                                                domainId = domainId,
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
                                                bookTitle = bookTitle,
                                                bookAuthor = bookAuthor,
                                                pagesMemorized = pagesMemorizedText.toIntOrNull() ?: 0,
                                                notes = notes
                                            )
                                        }
                                        "bible_mem" -> {
                                            AccountabilityEntryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = "guest_user",
                                                domainId = domainId,
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
                                                bibleMemBook = bibleMemBook,
                                                bibleMemChapter = bibleMemChapter,
                                                bibleMemVerse = bibleMemVerse,
                                                notes = notes
                                            )
                                        }
                                        else -> {
                                            AccountabilityEntryEntity(
                                                id = UUID.randomUUID().toString(),
                                                userId = "guest_user",
                                                domainId = domainId,
                                                dateIso = selectedDateIso,
                                                timestampMs = System.currentTimeMillis(),
                                                timezoneId = java.time.ZoneId.systemDefault().id,
                                                durationSeconds = durSecs,
                                                startTimeIso = startTimeText,
                                                endTimeIso = stopTimeText,
                                                notes = notes
                                            )
                                        }
                                    }

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

    // Add / Edit Disciple Dialog
    if (showAddEditDiscipleDialog) {
        var discName by remember { mutableStateOf(discipleToEdit?.name ?: "") }
        var discPhone by remember { mutableStateOf(discipleToEdit?.phone ?: "") }
        var discNotes by remember { mutableStateOf(discipleToEdit?.notes ?: "") }
        var discPrayerTopics by remember { mutableStateOf(discipleToEdit?.prayerTopics ?: "") }
        var discStatus by remember { mutableStateOf(discipleToEdit?.status ?: "Active") }
        var statusDropdownExpanded by remember { mutableStateOf(false) }

        val statuses = listOf("Active", "New Convert", "Growing", "Leader in Training")

        AlertDialog(
            onDismissRequest = { showAddEditDiscipleDialog = false },
            title = {
                Text(
                    text = if (discipleToEdit == null) strings.addNewDisciple else strings.editDisciple,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = discName,
                        onValueChange = { discName = it },
                        label = { Text(strings.discipleNameLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = discPhone,
                        onValueChange = { discPhone = it },
                        label = { Text(strings.phoneOptionalLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    ExposedDropdownMenuBox(
                        expanded = statusDropdownExpanded,
                        onExpandedChange = { statusDropdownExpanded = !statusDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = discStatus,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.spiritualStageLabel) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = statusDropdownExpanded,
                            onDismissRequest = { statusDropdownExpanded = false }
                        ) {
                            statuses.forEach { st ->
                                DropdownMenuItem(
                                    text = { Text(st) },
                                    onClick = {
                                        discStatus = st
                                        statusDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = discPrayerTopics,
                        onValueChange = { discPrayerTopics = it },
                        label = { Text(strings.prayerTopicsLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = discNotes,
                        onValueChange = { discNotes = it },
                        label = { Text(strings.spiritualJourneyNotesLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (discName.isNotBlank()) {
                            val entity = DiscipleEntity(
                                id = discipleToEdit?.id ?: UUID.randomUUID().toString(),
                                name = discName.trim(),
                                phone = discPhone.trim(),
                                notes = discNotes.trim(),
                                prayerTopics = discPrayerTopics.trim(),
                                status = discStatus,
                                createdAtMs = discipleToEdit?.createdAtMs ?: System.currentTimeMillis(),
                                updatedAtMs = System.currentTimeMillis()
                            )
                            if (discipleToEdit == null) {
                                onSaveDisciple(entity)
                            } else {
                                onUpdateDisciple(entity)
                            }
                            showAddEditDiscipleDialog = false
                            HapticHelper.vibrateSuccess(context)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.saveDiscipleButton, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEditDiscipleDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    discipleToDelete?.let { disc ->
        AlertDialog(
            onDismissRequest = { discipleToDelete = null },
            title = { Text(strings.confirmDeleteDisciple, fontWeight = FontWeight.Bold) },
            text = { Text(String.format(strings.confirmDeleteDisciplePrompt, disc.name)) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteDisciple(disc)
                        discipleToDelete = null
                        HapticHelper.vibrateClick(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusError),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.delete, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { discipleToDelete = null }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
