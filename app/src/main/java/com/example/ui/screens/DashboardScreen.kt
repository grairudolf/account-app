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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.core.localization.AppStrings
import com.example.core.util.HapticHelper
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Date & Welcome Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = today.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    val currentHour = LocalTime.now().hour
                    val timeGreeting = when {
                        currentHour in 5..11 -> strings.greetingMorning
                        currentHour in 12..17 -> strings.greetingAfternoon
                        else -> strings.greetingEvening
                    }
                    Text(
                        text = "$timeGreeting, $discipleName",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(PrimaryBlue, AccentPurple)
                            )
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(LightBlueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        val profilePic = uiState.user?.profileImageUri
                        if (!profilePic.isNullOrBlank()) {
                            AsyncImage(
                                model = profilePic,
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = discipleName.take(1).uppercase(),
                                color = PrimaryBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }

        // Functional Home Screen Search Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(strings.searchDomains, style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = strings.search,
                        tint = PrimaryBlue
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
                    focusedBorderColor = PrimaryBlue,
                    unfocusedBorderColor = DividerColor
                ),
                singleLine = true
            )
        }

        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = "Search Results for “$searchQuery”",
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
                        color = PrimaryBlue
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
                                    .background(LightBlueContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
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
                        color = PrimaryBlue
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

        // Feature Highlight Hero Banner with Vector Graphic Elements
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(185.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(LightBlueContainer, LightBlueContainer.copy(alpha = 0.8f))
                        )
                    )
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.2f), RoundedCornerShape(28.dp))
                    .testTag("dashboard_hero_card")
            ) {
                // Background Vector Graphic Overlay
                val path = remember { Path() }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    drawCircle(
                        color = PrimaryBlue.copy(alpha = 0.08f),
                        radius = h * 0.9f,
                        center = androidx.compose.ui.geometry.Offset(w * 0.9f, h * 0.2f)
                    )
                    drawCircle(
                        color = AccentPurple.copy(alpha = 0.06f),
                        radius = h * 0.6f,
                        center = androidx.compose.ui.geometry.Offset(w * 0.1f, h * 0.9f)
                    )

                    path.reset()
                    path.moveTo(0f, h * 0.7f)
                    path.cubicTo(w * 0.3f, h * 0.5f, w * 0.6f, h * 0.9f, w, h * 0.6f)
                    path.lineTo(w, h)
                    path.lineTo(0f, h)
                    path.close()
                    drawPath(path, color = PrimaryBlue.copy(alpha = 0.04f))
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PrimaryBlue
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = strings.todaysProgress,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.8f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${uiState.dailyProgress.progressPercentage}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryBlue
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "${uiState.dailyProgress.completedDomainsCount} of ${uiState.dailyProgress.totalActiveDomainsCount} Disciplines Completed",
                            style = MaterialTheme.typography.titleLarge,
                            color = PrimaryBlueDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(CircleShape),
                            color = PrimaryBlue,
                            trackColor = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Daily Spiritual Encouragement Card (3B Prophetic Messages Quotes)
        item {
            val quotes = remember(strings) { strings.dailyQuotes }
            var quoteIndex by remember { mutableStateOf(LocalDate.now().dayOfYear % quotes.size) }

            Surface(
                shape = RoundedCornerShape(28.dp),
                color = AccentPurpleContainer,
                border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_encouragement_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(AccentPurple, PrimaryBlue))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FormatQuote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = strings.dailyWordTitle,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccentPurple
                            )
                        }
                        TextButton(onClick = { quoteIndex = (quoteIndex + 1) % quotes.size }) {
                            Text(strings.nextQuote, style = MaterialTheme.typography.labelSmall, color = PrimaryBlue)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = quotes.getOrElse(quoteIndex) { quotes.firstOrNull() ?: "" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Sliding Check-In Notification Banner for Incomplete Disciplines (One aspect at a time)
        item {
            val todayIso = remember { LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE) }
            val completedTodayDomains = remember(uiState.recentActivities) {
                uiState.recentActivities.filter { it.dateIso == todayIso }.map { it.domainId }.toSet()
            }
            val incompleteDisciplines = remember(completedTodayDomains) {
                PredefinedDomains.ALL.filter { !completedTodayDomains.contains(it.id) }
            }
            var activeCheckinIndex by remember { mutableStateOf(0) }

            if (incompleteDisciplines.isNotEmpty() && activeCheckinIndex < incompleteDisciplines.size) {
                val currentIncomplete = incompleteDisciplines[activeCheckinIndex]
                val currentTitle = strings.getDomainTitle(currentIncomplete.titleKey)

                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -50 }) + fadeIn()
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = AccentMintContainer,
                        border = BorderStroke(1.dp, AccentMint),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_checkin_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = AccentMint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Daily Check-In: Have you done $currentTitle today?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onQuickAdd(currentIncomplete.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentMint),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Log $currentTitle")
                                }
                                OutlinedButton(
                                    onClick = { activeCheckinIndex++ },
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Next Aspect")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Stats Grid Cards - Sleek rounded 28.dp cards with Duolingo Burning Fire Flame
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Streak Card (Yellow Gold Color with Duolingo Animated Fire Flame)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(StreakGoldContainer)
                        .border(1.dp, StreakGold, RoundedCornerShape(28.dp))
                        .padding(18.dp)
                        .testTag("dashboard_streak_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DuolingoFlame(
                                size = 48.dp,
                                isActive = uiState.streakStats.currentStreakDays > 0
                            )
                            if (uiState.streakStats.currentStreakDays > 0) {
                                Surface(
                                    shape = CircleShape,
                                    color = StreakGold
                                ) {
                                    Text(
                                        text = "ON FIRE!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = strings.currentStreak,
                            style = MaterialTheme.typography.titleMedium,
                            color = StreakGoldDark
                        )
                        Text(
                            text = "${uiState.streakStats.currentStreakDays} ${strings.days}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = StreakGoldDark
                        )
                    }
                }

                // Goal Completion Card
                val hasGoals = uiState.goalsWithProgress.isNotEmpty()
                val avgGoalProgress = if (hasGoals) {
                    uiState.goalsWithProgress.map { it.progressPercentage }.average().toInt()
                } else 0

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .background(SurfaceVariantLight)
                        .border(1.dp, DividerColor, RoundedCornerShape(28.dp))
                        .clickable { onNavigateToGoals() }
                        .padding(18.dp)
                        .testTag("dashboard_goals_card")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(LightBlueContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = strings.goalProgress,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (hasGoals) {
                            Text(
                                text = "$avgGoalProgress%",
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        } else {
                            Text(
                                text = "No Goals Set",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Tap to set goals →",
                                style = MaterialTheme.typography.labelSmall,
                                color = PrimaryBlue,
                                fontWeight = FontWeight.SemiBold
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
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = domain.id.replace("_", " ").uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryBlue
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
    Surface(
        shape = RoundedCornerShape(24.dp),
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
                    .background(LightBlueContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.domainId.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = getSummaryText(entry),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

private fun getSummaryText(entry: AccountabilityEntryEntity): String {
    return when (entry.domainId) {
        "bible_reading" -> "${entry.bibleBook} Ch ${entry.startChapter}-${entry.endChapter}"
        "ddewg", "prayer_alone", "prayer_with_others" -> "${entry.durationSeconds / 60} minutes"
        "proclamation_importunity" -> "${entry.proclamationTopic.ifBlank { "Proclamation" }}: ${entry.proclamationCount} proclamations (${entry.durationSeconds / 60}m)"
        "soul_winning" -> "Preached: ${entry.preachedToCount}, Converted: ${entry.convertedCount}"
        "giving" -> "${if (entry.givingType.isNotBlank()) "${entry.givingType}: " else ""}Amount $${entry.givingAmount}"
        else -> entry.notes.ifBlank { "Activity logged" }
    }
}
