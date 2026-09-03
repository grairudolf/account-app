package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.core.localization.AppStrings
import com.example.core.localization.FrenchStrings
import com.example.core.util.HapticHelper
import com.example.core.util.QuoteImageSharer
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.domain.models.PredefinedDomains
import com.example.ui.components.DuolingoFlame
import com.example.ui.theme.*
import com.example.ui.viewmodels.DashboardUiState
import com.example.ui.viewmodels.GoalWithProgress
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    strings: AppStrings,
    uiState: DashboardUiState,
    onNavigateToDomain: (String) -> Unit,
    onNavigateToDomains: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onQuickAdd: (String) -> Unit,
    onNavigateToRecentActivity: (AccountabilityEntryEntity) -> Unit = {}
) {
    val context = LocalContext.current
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    val discipleName = uiState.user?.fullName?.ifBlank { "Disciple" } ?: "Disciple"

    var searchQuery by remember { mutableStateOf("") }

    val matchingDisciplines = remember(searchQuery, strings) {
        if (searchQuery.isBlank()) emptyList()
        else PredefinedDomains.ALL.filter { domain ->
            val title = strings.getDomainTitle(domain.titleKey)
            val desc = strings.getDomainDesc(domain.descKey)
            domain.id.contains(searchQuery, ignoreCase = true) ||
            title.contains(searchQuery, ignoreCase = true) ||
            desc.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchingActivities = remember(searchQuery, uiState.allEntries) {
        if (searchQuery.isBlank()) emptyList()
        else uiState.allEntries.filter { entry ->
            entry.domainId.contains(searchQuery, ignoreCase = true) ||
            entry.notes.contains(searchQuery, ignoreCase = true) ||
            entry.reflection.contains(searchQuery, ignoreCase = true) ||
            entry.bibleBook.contains(searchQuery, ignoreCase = true)
        }
    }

    // Infinite transition for pulsing streak flame & graphics
    val infiniteTransition = rememberInfiniteTransition(label = "streak_pulse")
    val flameScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flame_scale"
    )

    // Animated Progress for Hero Banner
    val animatedProgress by animateFloatAsState(
        targetValue = (uiState.dailyProgress.progressPercentage / 100f).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "hero_progress"
    )

    val isDark = isAppInDarkTheme()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 2. User Greeting Section
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = today.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.2.sp
                )
                val currentHour = LocalTime.now().hour
                val timeGreeting = when {
                    currentHour in 5..11 -> strings.greetingMorning
                    currentHour in 12..17 -> strings.greetingAfternoon
                    else -> strings.greetingEvening
                }
                Text(
                    text = "$timeGreeting, $discipleName",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Functional Home Screen Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchDomains, style = MaterialTheme.typography.bodyMedium, color = TextMuted) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = strings.search,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_search_input"),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = DividerColor
                ),
                singleLine = true
            )
        }

        // 7-Day Week Capsule Strip (with Streak Header)
        item {
            val startOfWeek = remember { LocalDate.now().minusDays(LocalDate.now().dayOfWeek.value.toLong() % 7) }
            val currentDayOfMonth = remember { LocalDate.now().dayOfMonth }
            val daysOfWeek = remember(startOfWeek) {
                (0..6).map { startOfWeek.plusDays(it.toLong()) }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth().testTag("dashboard_weekly_calendar_strip")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = strings.calendar,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Prominent Streak Badge in Calendar Header
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (uiState.streakStats.currentStreakDays > 0) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (uiState.streakStats.currentStreakDays > 0) MaterialTheme.colorScheme.tertiary else DividerColor)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                DuolingoFlame(
                                    size = 18.dp,
                                    isActive = uiState.streakStats.currentStreakDays > 0
                                )
                                Text(
                                    text = "${uiState.streakStats.currentStreakDays} ${strings.days} ${strings.currentStreak}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (uiState.streakStats.currentStreakDays > 0) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val locale = if (strings is FrenchStrings) java.util.Locale.FRENCH else java.util.Locale.ENGLISH
                        daysOfWeek.forEach { date ->
                            val isToday = date.dayOfMonth == currentDayOfMonth
                            val dayName = date.format(DateTimeFormatter.ofPattern("EEE", locale))
                            val dayNumber = date.dayOfMonth.toString()
                            val hasEntryForDay = uiState.allEntries.any { it.dateIso == date.format(DateTimeFormatter.ISO_LOCAL_DATE) }

                            val capsuleBg = if (isToday) MaterialTheme.colorScheme.primary else if (hasEntryForDay) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
                            val capsuleBorder = if (isToday) BorderStroke(0.dp, Color.Transparent) else if (hasEntryForDay) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else BorderStroke(1.dp, DividerColor)
                            val textCol = if (isToday) MaterialTheme.colorScheme.onPrimary else if (hasEntryForDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            val numCol = if (isToday) MaterialTheme.colorScheme.onPrimary else if (hasEntryForDay) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
                            val dotCol = if (isToday) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f) else if (hasEntryForDay) MaterialTheme.colorScheme.secondary else Color.Transparent

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = capsuleBg,
                                border = capsuleBorder,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(68.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = dayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textCol
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(dotCol)
                                    )
                                    Text(
                                        text = dayNumber,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = numCol
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = String.format(strings.searchResultsFor, searchQuery),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (matchingDisciplines.isNotEmpty()) {
                item {
                    Text(
                        text = strings.spiritualDisciplines,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(matchingDisciplines, key = { "domain_${it.id}" }) { domain ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, DividerColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToDomain(domain.id) }
                            .testTag("search_result_domain_${domain.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strings.getDomainTitle(domain.titleKey),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = strings.getDomainDesc(domain.descKey),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                            Button(
                                onClick = { onNavigateToDomain(domain.id) },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(strings.save)
                            }
                        }
                    }
                }
            }

            if (matchingActivities.isNotEmpty()) {
                item {
                    Text(
                        text = strings.recentActivities,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(matchingActivities, key = { "act_${it.id}" }) { entry ->
                    RecentActivityCard(
                        entry = entry,
                        strings = strings,
                        onClick = { onNavigateToDomain(entry.domainId) }
                    )
                }
            }

            if (matchingDisciplines.isEmpty() && matchingActivities.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, DividerColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No spiritual disciplines or records found matching “$searchQuery”.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }
        } else {

        // Claymorphic 3D "Today's Progress" Section
        item {
            val heroBgGradient = if (isDark) {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1C2738),
                        Color(0xFF121B27)
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1F3168),
                        Color(0xFF14214C)
                    )
                )
            }

            val heroBorder = if (isDark) {
                BorderStroke(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            BrandChampagneGold.copy(alpha = 0.45f),
                            SurfaceBorderDark,
                            Color(0xFF283548)
                        )
                    )
                )
            } else {
                BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(
                            BrandSlateBlue.copy(alpha = 0.7f),
                            Color(0xFF2A3E7E)
                        )
                    )
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceDarkCard,
                border = heroBorder,
                shadowElevation = if (isDark) 6.dp else 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroBgGradient)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Top Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) Color(0xFF202C3D) else BrandDarkNavy,
                                border = BorderStroke(
                                    1.dp,
                                    if (isDark) BrandChampagneGold.copy(alpha = 0.35f) else BrandSlateBlue.copy(alpha = 0.5f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (isDark) BrandChampagneGold else BrandVibrantYellow,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = strings.todaysProgress.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDark) BrandChampagneGold else BrandVibrantYellow,
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDark) BrandChampagneGold else BrandVibrantYellow
                            ) {
                                Text(
                                    text = "${uiState.dailyProgress.progressPercentage}%",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color(0xFF1B1504) else BrandDarkNavy,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Middle Content Row with 3D Clay Target Illustration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = String.format(strings.completedDomainsCountFormat, uiState.dailyProgress.completedDomainsCount, uiState.dailyProgress.totalActiveDomainsCount),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (uiState.dailyProgress.progressPercentage >= 100)
                                        strings.allDisciplinesFulfilledToday
                                    else
                                        strings.pressingTowardMark,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) Color(0xFFBAC5D6) else BrandLightText
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // 3D Clay Target Image
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        Brush.radialGradient(
                                            listOf(
                                                if (isDark) BrandChampagneGold.copy(alpha = 0.16f) else BrandSlateBlue.copy(alpha = 0.3f),
                                                Color.Transparent
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Flag,
                                    contentDescription = "Daily Goal Target",
                                    tint = if (isDark) BrandChampagneGold else BrandWarmGold,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }

                        // Custom High Contrast Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isDark) Color(0xFF0B1119) else Color(0xFF0D1636))
                                .border(
                                    1.dp,
                                    if (isDark) SurfaceBorderDark else BrandSlateBlue.copy(alpha = 0.4f),
                                    RoundedCornerShape(6.dp)
                                )
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = animatedProgress.coerceIn(0.02f, 1f))
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            if (isDark) listOf(BrandWarmGold, BrandChampagneGold, BrandBrightYellow)
                                            else listOf(BrandWarmGold, BrandVibrantYellow, BrandBrightYellow)
                                        )
                                    )
                            )
                        }
                    }
                }
            }
        }

        // Daily Spiritual Encouragement Devotional Card (Multiple Image Backgrounds + Interactive Refresh + Share Action)
        item {
            val quotes = remember(strings) { strings.dailyQuotes }
            val dayOfYear = remember { LocalDate.now().dayOfYear }
            var manualQuoteOffset by remember { mutableIntStateOf(0) }
            
            val activeIndex = (dayOfYear + manualQuoteOffset) % if (quotes.isNotEmpty()) quotes.size else 1
            val currentQuote = remember(quotes, activeIndex) {
                quotes.getOrElse(activeIndex) { quotes.firstOrNull() ?: "" }
            }
            val cleanQuote = remember(currentQuote) {
                currentQuote.trim().trim('“', '”', '"', '\'').trim()
            }
            val devotionalBgList = remember {
                listOf(
                    com.example.R.drawable.quote_bg_open_bible_1788139223471,
                    com.example.R.drawable.quote_bg_global_harvest_1788139235063,
                    com.example.R.drawable.quote_bg_prayer_altar_1788139251276,
                    com.example.R.drawable.quote_bg_radiant_cross_1788139262304,
                    com.example.R.drawable.quote_bg_cross_1787235555876,
                    com.example.R.drawable.quote_bg_mountains_1787235541853,
                    com.example.R.drawable.quote_bg_sunrise_1787220672419,
                    com.example.R.drawable.quote_bg_heavens_1787220708792,
                    com.example.R.drawable.quote_bg_path_1787220696837,
                    com.example.R.drawable.quote_bg_waters_1787220685176,
                    com.example.R.drawable.devotional_quote_bg_1787144263336
                )
            }
            val currentBgRes = remember(activeIndex) {
                devotionalBgList[activeIndex % devotionalBgList.size]
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = SurfaceDark,
                border = BorderStroke(1.dp, BrandSlateBlue.copy(alpha = 0.4f)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_encouragement_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 175.dp)
                ) {
                    // Inspirational Devotional Background Image (rotates daily + manual interactive refresh!)
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = currentBgRes),
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Readability Cinematic Gradient Overlay
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        BrandDarkNavy.copy(alpha = 0.45f),
                                        Color(0xFF0D1636).copy(alpha = 0.70f),
                                        Color(0xFF070B1E).copy(alpha = 0.88f)
                                    )
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f, fill = false),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(BrandMutedGold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FormatQuote,
                                        contentDescription = null,
                                        tint = BrandBrightYellow,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Text(
                                    text = strings.dailyWordTitle.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BrandVibrantYellow,
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Interactive Quote & Image Refresh Button
                                Surface(
                                    shape = CircleShape,
                                    color = BrandDarkNavy.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, BrandSlateBlue.copy(alpha = 0.6f)),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    IconButton(
                                        onClick = { manualQuoteOffset++ },
                                        modifier = Modifier.fillMaxSize().testTag("refresh_quote_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Cycle Quote & Background",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Inline Share Action Button
                                Surface(
                                    shape = CircleShape,
                                    color = BrandDarkNavy.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, BrandSlateBlue.copy(alpha = 0.6f)),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            QuoteImageSharer.shareQuoteImage(
                                                context = context,
                                                quoteText = cleanQuote,
                                                title = strings.dailyWordTitle,
                                                bgResId = currentBgRes
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .testTag("share_quote_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = "Share Quote",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = cleanQuote,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = QuoteFontFamily,
                                fontSize = 17.sp,
                                lineHeight = 26.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Streamlined Check-In Prompt Card
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isDark) SurfaceBorderDark else BrandSlateBlue.copy(alpha = 0.3f)),
                shadowElevation = if (isDark) 0.dp else 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_checkin_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = if (isDark) listOf(Color(0xFF222F42), Color(0xFF182330)) else listOf(BrandDarkNavy, BrandSlateBlue)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = if (isDark) BrandChampagneGold else BrandBrightYellow,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.haveYouSpentTimeWithGod,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.timeWithGodSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) Color(0xFFBAC5D6) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Button(
                            onClick = onNavigateToDomains,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) BrandChampagneGold else BrandDarkNavy,
                                contentColor = if (isDark) Color(0xFF2B1F05) else BrandBrightYellow
                            ),
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("log_disciplines_prompt_button")
                        ) {
                            Icon(
                                Icons.Default.AddCircleOutline,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isDark) Color(0xFF2B1F05) else BrandBrightYellow
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = strings.exploreDisciplines,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }

        // Goal Progress & Overview Card
        item {
            val hasGoals = uiState.goalsWithProgress.isNotEmpty()
            val avgGoalProgress = if (hasGoals) {
                uiState.goalsWithProgress.map { it.progressPercentage }.average().toInt()
            } else 0

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, if (isDark) SurfaceBorderDark else DividerColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToGoals() }
                    .testTag("dashboard_goals_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF222F42) else BrandDarkNavy)
                            .border(
                                if (isDark) 1.dp else 0.dp,
                                if (isDark) BrandChampagneGold.copy(alpha = 0.35f) else Color.Transparent,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isDark) BrandChampagneGold else BrandVibrantYellow,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = strings.goalProgress,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (hasGoals) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isDark) BrandChampagneGold.copy(alpha = 0.18f) else BrandMutedGold.copy(alpha = 0.2f),
                                    border = if (isDark) BorderStroke(1.dp, BrandChampagneGold.copy(alpha = 0.35f)) else null
                                ) {
                                    Text(
                                        text = "$avgGoalProgress%",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isDark) BrandChampagneGold else BrandDarkNavy,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        if (hasGoals) {
                            LinearProgressIndicator(
                                progress = { (avgGoalProgress / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isDark) BrandChampagneGold else BrandDarkNavy,
                                trackColor = if (isDark) Color(0xFF222D3B) else DividerColor
                            )
                            Text(
                                text = "${uiState.goalsWithProgress.count { it.progressPercentage >= 100 }}/${uiState.goalsWithProgress.size} goals completed",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFFBAC5D6) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = strings.noGoalsSet,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) Color(0xFFBAC5D6) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = strings.tapToSetGoals,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isDark) BrandChampagneGold else BrandDarkNavy,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Record Launcher Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = strings.quickAdd,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(PredefinedDomains.ALL, key = { "quick_${it.id}" }) { domain ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, DividerColor),
                            modifier = Modifier
                                .clickable {
                                    HapticHelper.vibrateClick(context)
                                    onQuickAdd(domain.id)
                                }
                                .testTag("quick_add_${domain.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = strings.getDomainTitle(domain.titleKey).uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Activities Section with Animated Items
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = strings.recentActivities,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (uiState.recentActivities.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.noRecentActivities,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }
        } else {
            items(uiState.recentActivities, key = { "recent_${it.id}" }) { entry ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(400)) + slideInVertically(initialOffsetY = { 40 })
                ) {
                    RecentActivityCard(entry = entry, strings = strings, onClick = { onNavigateToRecentActivity(entry) })
                }
            }
        }
    }
}
}

@Composable
fun RecentActivityCard(
    entry: AccountabilityEntryEntity,
    strings: AppStrings,
    onClick: () -> Unit
) {
    val domainIcon = when (entry.domainId.lowercase().trim()) {
        "bible_reading" -> Icons.Default.AutoStories
        "ddewg" -> Icons.Default.WbSunny
        "prayer_alone" -> Icons.Default.SelfImprovement
        "prayer_with_others" -> Icons.Default.Groups
        "proclamation_importunity" -> Icons.Default.Campaign
        "fasting" -> Icons.Default.Timer
        "giving" -> Icons.Default.VolunteerActivism
        "making_disciples", "disciple_maker", "disciples", "accountability" -> Icons.Default.GroupAdd
        "soul_winning" -> Icons.Default.DirectionsWalk
        "christian_lit_reading", "christian_lit", "literature" -> Icons.Default.MenuBook
        "christian_lit_memory", "christian_lit_mem" -> Icons.Default.Psychology
        "bible_memory", "bible_mem" -> Icons.Default.AutoStories
        "retreats" -> Icons.Default.Landscape
        else -> Icons.Default.MenuBook
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, DividerColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("recent_activity_card_${entry.id}")
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = domainIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = strings.getDomainTitleById(entry.domainId).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = strings.formatActivitySummary(entry),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
            Text(
                text = entry.dateIso,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
