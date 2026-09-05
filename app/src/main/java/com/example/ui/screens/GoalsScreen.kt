package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.localization.AppStrings
import com.example.core.localization.FrenchStrings
import com.example.core.util.HapticHelper
import com.example.data.local.entities.GoalEntity
import com.example.domain.models.PredefinedDomains
import com.example.ui.components.AppDatePickerDialog
import com.example.ui.components.AppTimePickerDialog
import com.example.ui.components.FastingCalendarPicker
import com.example.ui.theme.*
import com.example.ui.viewmodels.GoalWithProgress
import java.time.LocalDate

enum class GoalProgressTier(
    val englishLabel: String,
    val frenchLabel: String,
    val icon: ImageVector
) {
    COMPLETED("Achieved", "Accompli", Icons.Default.CheckCircle),
    NEAR_TARGET("Pressing Forward", "En Plein Élan", Icons.Default.TrendingUp),
    IN_PROGRESS("In Progress", "En Cours", Icons.Default.DirectionsRun),
    STARTING("Beginning", "Au Début", Icons.Default.Flag)
}

fun getGoalProgressTier(progress: Double, target: Double): GoalProgressTier {
    if (target <= 0.0) return GoalProgressTier.STARTING
    val pct = (progress / target) * 100.0
    return when {
        pct >= 100.0 -> GoalProgressTier.COMPLETED
        pct >= 80.0 -> GoalProgressTier.NEAR_TARGET
        pct >= 30.0 -> GoalProgressTier.IN_PROGRESS
        else -> GoalProgressTier.STARTING
    }
}

data class SpiritualGoalQuote(
    val quoteEn: String,
    val quoteFr: String,
    val authorEn: String,
    val authorFr: String,
    val reference: String
)

val SpiritualGoalsQuotes = listOf(
    SpiritualGoalQuote(
        "The winner has a clear cut goal and he works at it night and day. The loser gives himself to everything and finally nothing. Are you a winner or loser?",
        "Le gagnant a un but précis et il y travaille jour et nuit. Le perdant s'adonne à tout et finalement à rien. Es-tu un gagnant ou un perdant ?",
        "Zacharias Tanee Fomum",
        "Zacharias Tanee Fomum",
        "How to Succeed in the Christian Life"
    ),
    SpiritualGoalQuote(
        "The spiritual man is a disciplined man. Spiritual discipline is the key to spiritual power and divine victory.",
        "L'homme spirituel est un homme discipliné. La discipline spirituelle est la clé de la puissance et de la victoire divine.",
        "Prof. Zacharias Tanee Fomum",
        "Prof. Zacharias Tanee Fomum",
        "The Spiritual Man"
    ),
    SpiritualGoalQuote(
        "I press toward the mark for the prize of the high calling of God in Christ Jesus.",
        "Je cours vers le but, pour remporter le prix de la vocation céleste de Dieu en Jésus-Christ.",
        "Apostle Paul",
        "Apôtre Paul",
        "Philippians 3:14"
    ),
    SpiritualGoalQuote(
        "Run in such a way as to get the prize. Everyone who competes in the games goes into strict training.",
        "Courez de manière à remporter le prix. Tous ceux qui combattent s'imposent toute espèce d'abstinences.",
        "Holy Scripture",
        "Saintes Écritures",
        "1 Corinthians 9:24-25"
    ),
    SpiritualGoalQuote(
        "Spiritual goals are sacred covenants translated into daily relentless execution before the Lord.",
        "Les objectifs spirituels sont des alliances sacrées traduites en exécution quotidienne devant le Seigneur.",
        "CMFI Accountability",
        "CMFI Compte Rendu",
        "Sacred Consecration"
    ),
    SpiritualGoalQuote(
        "Let us run with perseverance the race marked out for us, fixing our eyes on Jesus, the pioneer and perfecter of faith.",
        "Courons avec persévérance dans la carrière qui nous est ouverte, ayant les regards sur Jésus, l'auteur et le consommateur de la foi.",
        "Holy Scripture",
        "Saintes Écritures",
        "Hebrews 12:1-2"
    )
)

@Composable
fun GoalsScreen(
    strings: AppStrings,
    goalsWithProgress: List<GoalWithProgress>,
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit,
    onAddGoal: (userId: String, domainId: String, title: String, target: Double, unit: String, freq: String, startDate: String, fastingType: String, periodDays: Int, isDailyReminderEnabled: Boolean, reminderTimeIso: String) -> Unit,
    onDeleteGoal: (String) -> Unit,
    onToggleReminder: (GoalEntity, Boolean) -> Unit = { _, _ -> },
    onQuickIncrementGoal: ((GoalEntity) -> Unit)? = null,
    onNavigateToDomain: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val isFr = strings is FrenchStrings
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var selectedTierFilter by remember { mutableStateOf<GoalProgressTier?>(null) }
    var currentQuoteIndex by remember { mutableIntStateOf(0) }

    val activeQuote = SpiritualGoalsQuotes[currentQuoteIndex % SpiritualGoalsQuotes.size]

    val completedCount = goalsWithProgress.count { getGoalProgressTier(it.currentProgress, it.goal.targetValue) == GoalProgressTier.COMPLETED }
    val nearTargetCount = goalsWithProgress.count { getGoalProgressTier(it.currentProgress, it.goal.targetValue) == GoalProgressTier.NEAR_TARGET }
    val inProgressCount = goalsWithProgress.count { getGoalProgressTier(it.currentProgress, it.goal.targetValue) == GoalProgressTier.IN_PROGRESS }
    val startingCount = goalsWithProgress.count { getGoalProgressTier(it.currentProgress, it.goal.targetValue) == GoalProgressTier.STARTING }

    val filteredGoals = if (selectedTierFilter == null) {
        goalsWithProgress
    } else {
        goalsWithProgress.filter { getGoalProgressTier(it.currentProgress, it.goal.targetValue) == selectedTierFilter }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    HapticHelper.vibrateClick(context)
                    showAddGoalDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_goal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = strings.addGoal)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // 1. Clean Scriptural Inspiration Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticHelper.vibrateClick(context)
                        currentQuoteIndex = (currentQuoteIndex + 1) % SpiritualGoalsQuotes.size
                    }
                    .testTag("spiritual_quote_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isFr) activeQuote.quoteFr else activeQuote.quoteEn,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "— ${if (isFr) activeQuote.authorFr else activeQuote.authorEn} • ${activeQuote.reference}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Next Quote",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Frequency Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to strings.allPeriod,
                    "DAILY" to strings.daily,
                    "WEEKLY" to strings.weekly,
                    "MONTHLY" to strings.monthly,
                    "YEARLY" to strings.yearly
                ).forEach { (freqKey, freqLabel) ->
                    val selected = selectedFrequency == freqKey
                    FilterChip(
                        selected = selected,
                        onClick = {
                            HapticHelper.vibrateClick(context)
                            onFrequencySelected(freqKey)
                        },
                        label = {
                            Text(
                                text = freqLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("filter_freq_$freqKey")
                    )
                }
            }

            // 3. Clean Progress Overview Card
            if (goalsWithProgress.isNotEmpty()) {
                val totalCount = goalsWithProgress.size
                val overallPct = if (totalCount > 0) ((completedCount.toFloat() / totalCount) * 100).toInt() else 0
                val animatedOverallProgress by animateFloatAsState(
                    targetValue = (completedCount.toFloat() / totalCount.coerceAtLeast(1)).coerceIn(0f, 1f),
                    animationSpec = tween(600, easing = FastOutSlowInEasing),
                    label = "overall_progress_bar"
                )

                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isFr) "$completedCount sur $totalCount accomplis ($overallPct%)" else "$completedCount of $totalCount Completed ($overallPct%)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (nearTargetCount > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = if (isFr) "$nearTargetCount en élan" else "$nearTargetCount close",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { animatedOverallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Clean Tier Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All
                FilterChip(
                    selected = selectedTierFilter == null,
                    onClick = {
                        HapticHelper.vibrateClick(context)
                        selectedTierFilter = null
                    },
                    label = { Text(if (isFr) "Tous (${goalsWithProgress.size})" else "All (${goalsWithProgress.size})") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_status_all")
                )

                // Completed
                FilterChip(
                    selected = selectedTierFilter == GoalProgressTier.COMPLETED,
                    onClick = {
                        HapticHelper.vibrateClick(context)
                        selectedTierFilter = if (selectedTierFilter == GoalProgressTier.COMPLETED) null else GoalProgressTier.COMPLETED
                    },
                    leadingIcon = {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = { Text(if (isFr) "Accomplis ($completedCount)" else "Achieved ($completedCount)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_status_victorious")
                )

                // Near Target
                FilterChip(
                    selected = selectedTierFilter == GoalProgressTier.NEAR_TARGET,
                    onClick = {
                        HapticHelper.vibrateClick(context)
                        selectedTierFilter = if (selectedTierFilter == GoalProgressTier.NEAR_TARGET) null else GoalProgressTier.NEAR_TARGET
                    },
                    leadingIcon = {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = { Text(if (isFr) "En élan ($nearTargetCount)" else "Pressing ($nearTargetCount)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_status_pressing")
                )

                // In Progress
                FilterChip(
                    selected = selectedTierFilter == GoalProgressTier.IN_PROGRESS,
                    onClick = {
                        HapticHelper.vibrateClick(context)
                        selectedTierFilter = if (selectedTierFilter == GoalProgressTier.IN_PROGRESS) null else GoalProgressTier.IN_PROGRESS
                    },
                    leadingIcon = {
                        Icon(Icons.Default.DirectionsRun, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = { Text(if (isFr) "En cours ($inProgressCount)" else "In Progress ($inProgressCount)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_status_momentum")
                )

                // Starting
                FilterChip(
                    selected = selectedTierFilter == GoalProgressTier.STARTING,
                    onClick = {
                        HapticHelper.vibrateClick(context)
                        selectedTierFilter = if (selectedTierFilter == GoalProgressTier.STARTING) null else GoalProgressTier.STARTING
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(14.dp))
                    },
                    label = { Text(if (isFr) "Départ ($startingCount)" else "Starting ($startingCount)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("filter_status_kindle")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Goals List
            if (filteredGoals.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = if (selectedTierFilter != null) {
                                if (isFr) "Aucun objectif dans cette catégorie." else "No goals in this filter category."
                            } else {
                                strings.noGoalsFound
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedTierFilter != null) {
                            TextButton(onClick = { selectedTierFilter = null }) {
                                Text(if (isFr) "Afficher tous les objectifs" else "Show All Goals")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredGoals, key = { it.goal.id }) { goalItem ->
                        GoalCard(
                            goalItem = goalItem,
                            strings = strings,
                            onDelete = {
                                HapticHelper.vibrateWarning(context)
                                onDeleteGoal(goalItem.goal.id)
                            },
                            onToggleReminder = { goal, enabled ->
                                onToggleReminder(goal, enabled)
                            },
                            onNavigateToDomain = {
                                onNavigateToDomain?.invoke(goalItem.goal.domainId)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            strings = strings,
            onDismiss = { showAddGoalDialog = false },
            onConfirm = { domainId, title, target, unit, freq, fastingType, periodDays, isReminder, reminderTime ->
                onAddGoal("guest_user", domainId, title, target, unit, freq, LocalDate.now().toString(), fastingType, periodDays, isReminder, reminderTime)
                showAddGoalDialog = false
            }
        )
    }
}

@Composable
fun GoalCard(
    goalItem: GoalWithProgress,
    strings: AppStrings,
    onDelete: () -> Unit,
    onToggleReminder: (GoalEntity, Boolean) -> Unit,
    onNavigateToDomain: () -> Unit = {}
) {
    val context = LocalContext.current
    val isFr = strings is FrenchStrings
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val tier = getGoalProgressTier(goalItem.currentProgress, goalItem.goal.targetValue)
    val isCompleted = tier == GoalProgressTier.COMPLETED

    val animatedProgressFraction by animateFloatAsState(
        targetValue = (goalItem.progressPercentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "goal_card_progress"
    )

    val domainIcon = when (goalItem.goal.domainId) {
        "bible_reading" -> Icons.Default.AutoStories
        "prayer_alone" -> Icons.Default.SelfImprovement
        "prayer_with_others" -> Icons.Default.Groups
        "ddewg" -> Icons.Default.WbSunny
        "fasting" -> Icons.Default.NoFood
        "christian_lit" -> Icons.Default.LibraryBooks
        "christian_lit_mem" -> Icons.Default.Psychology
        "bible_mem" -> Icons.Default.BookmarkAdded
        "soul_winning" -> Icons.Default.DirectionsWalk
        "proclamation_importunity" -> Icons.Default.Campaign
        "retreats" -> Icons.Default.Landscape
        "giving" -> Icons.Default.VolunteerActivism
        "making_disciples" -> Icons.Default.GroupAdd
        else -> Icons.Default.Flag
    }

    val progressFormatted = if (goalItem.currentProgress % 1.0 == 0.0) {
        goalItem.currentProgress.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", goalItem.currentProgress)
    }
    val targetFormatted = if (goalItem.goal.targetValue % 1.0 == 0.0) {
        goalItem.goal.targetValue.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", goalItem.goal.targetValue)
    }

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("goal_card_${goalItem.goal.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Domain Icon + Goal Title + Link to Domain + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = domainIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = goalItem.goal.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        // Clickable Domain Link Chip
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                modifier = Modifier.clickable { onNavigateToDomain() }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = strings.getDomainTitleById(goalItem.goal.domainId),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.OpenInNew,
                                        contentDescription = "Open Domain",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }

                            Text(
                                text = "•",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = goalItem.goal.frequency.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.size(32.dp).testTag("delete_goal_${goalItem.goal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = strings.delete,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Progress Numbers & Reminder Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "$progressFormatted / $targetFormatted ${goalItem.goal.unit}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isCompleted) StatusSuccess.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = "${goalItem.progressPercentage}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isCompleted) StatusSuccess else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                if (goalItem.goal.isDailyReminderEnabled) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = goalItem.goal.reminderTimeIso,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            IconButton(
                                onClick = {
                                    HapticHelper.vibrateClick(context)
                                    onToggleReminder(goalItem.goal, false)
                                },
                                modifier = Modifier.size(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Turn off reminder",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(CircleShape),
                color = if (isCompleted) StatusSuccess else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // Comparison Summary Note
            val diff = goalItem.goal.targetValue - goalItem.currentProgress
            val comparisonNote = if (diff <= 0) {
                val excess = -diff
                val excessFormatted = if (excess % 1.0 == 0.0) excess.toInt().toString() else String.format(java.util.Locale.US, "%.1f", excess)
                if (excess > 0) {
                    String.format(strings.goalAchievedExceeded, excessFormatted, goalItem.goal.unit)
                } else {
                    if (isFr) "Objectif pleinement accompli !" else "Target fulfilled!"
                }
            } else {
                val remainingFormatted = if (diff % 1.0 == 0.0) diff.toInt().toString() else String.format(java.util.Locale.US, "%.1f", diff)
                String.format(strings.goalRemainingProgress, remainingFormatted, goalItem.goal.unit, goalItem.goal.frequency.lowercase())
            }

            Text(
                text = comparisonNote,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Domain Navigation Button
            OutlinedButton(
                onClick = onNavigateToDomain,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .testTag("open_domain_${goalItem.goal.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isFr) "Ouvrir ${strings.getDomainTitleById(goalItem.goal.domainId)}" else "Open ${strings.getDomainTitleById(goalItem.goal.domainId)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text(strings.delete, fontWeight = FontWeight.Bold) },
            text = { Text(if (isFr) "Voulez-vous supprimer cet objectif et ses rappels ?" else "Are you sure you want to delete this goal and its reminders?") },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(strings.delete)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

data class DomainGoalPreset(
    val title: String,
    val defaultTarget: String,
    val defaultUnit: String,
    val defaultFrequency: String,
    val allowedUnits: List<String>
)

private fun getDomainPresets(domainId: String, strings: AppStrings): List<DomainGoalPreset> {
    val isFr = strings is FrenchStrings
    return when (domainId) {
        "bible_reading" -> listOf(
            DomainGoalPreset(if (isFr) "Chapitres par jour" else "Chapters per Day", "10", strings.unitChapters, "DAILY", listOf(strings.unitChapters, strings.unitPages, strings.unitDays, strings.unitHours, strings.unitMinutes)),
            DomainGoalPreset(if (isFr) "Pages par jour" else "Pages per Day", "30", strings.unitPages, "DAILY", listOf(strings.unitPages, strings.unitChapters, strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Jours de lecture biblique / semaine" else "Days of Bible Reading / Week", "7", strings.unitDays, "WEEKLY", listOf(strings.unitDays, strings.unitChapters, strings.unitPages)),
            DomainGoalPreset(if (isFr) "Lire toute la Bible (Fois par an)" else "Finish Whole Bible (Times per Year)", "1", strings.unitTimesBible, "YEARLY", listOf(strings.unitTimesBible, strings.unitPercentage, strings.unitChapters, strings.unitPages)),
            DomainGoalPreset(if (isFr) "Pourcentage biblique annuel" else "Bible Reading Percentage (Yearly)", "100", strings.unitPercentage, "YEARLY", listOf(strings.unitPercentage, strings.unitTimesBible, strings.unitChapters)),
            DomainGoalPreset(if (isFr) "Chapitres bibliques par mois" else "Bible Reading in a Month", "100", strings.unitChapters, "MONTHLY", listOf(strings.unitChapters, strings.unitPages, strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Heures d'étude par semaine" else "Weekly Study Hours", "5", strings.unitHours, "WEEKLY", listOf(strings.unitHours, strings.unitMinutes, strings.unitChapters)),
            DomainGoalPreset(if (isFr) "Objectif biblique personnalisé" else "Custom Bible Goal", "10", strings.unitChapters, "DAILY", listOf(strings.unitChapters, strings.unitPages, strings.unitDays, strings.unitPercentage, strings.unitTimesBible, strings.unitHours, strings.unitMinutes))
        )
        "prayer_alone" -> listOf(
            DomainGoalPreset(if (isFr) "4 retraites de 15 min par jour" else "15-Min Retreats per Day", "4", strings.unitRetreats15Min, "DAILY", listOf(strings.unitRetreats15Min, strings.unitHours, strings.unitMinutes, strings.unitSessions)),
            DomainGoalPreset(if (isFr) "Heures de prière seul par jour" else "Hours Spent in Prayer Alone", "1", strings.unitHours, "DAILY", listOf(strings.unitHours, strings.unitMinutes, strings.unitRetreats15Min, strings.unitSessions)),
            DomainGoalPreset(if (isFr) "Heures hebdomadaires dans le lieu secret" else "Weekly Hours in Secret Place", "7", strings.unitHours, "WEEKLY", listOf(strings.unitHours, strings.unitMinutes, strings.unitSessions)),
            DomainGoalPreset(if (isFr) "Veilles de nuit hebdomadaires" else "Weekly Prayer Night Vigils", "1", strings.unitSessions, "WEEKLY", listOf(strings.unitSessions, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Sièges de prière hebdomadaires" else "Weekly Prayer Siege", "1", strings.unitSessions, "WEEKLY", listOf(strings.unitSessions, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Sujets d'actions de grâces par jour" else "Thanksgiving Topics per Day", "5", strings.unitThanksgivingTopics, "DAILY", listOf(strings.unitThanksgivingTopics, strings.unitSessions)),
            DomainGoalPreset(if (isFr) "Sujets de requêtes par jour" else "Request Topics per Day", "5", strings.unitRequestTopics, "DAILY", listOf(strings.unitRequestTopics, strings.unitSessions)),
            DomainGoalPreset(if (isFr) "Objectif prière seul personnalisé" else "Custom Prayer Alone Goal", "60", strings.unitMinutes, "DAILY", listOf(strings.unitHours, strings.unitMinutes, strings.unitRetreats15Min, strings.unitThanksgivingTopics, strings.unitRequestTopics, strings.unitSessions))
        )
        "prayer_with_others" -> listOf(
            DomainGoalPreset(if (isFr) "Heures de PPB (Prière corporative)" else "PWO Corporate Hours", "5", strings.unitHours, "WEEKLY", listOf(strings.unitHours, strings.unitMinutes, strings.unitSessions)),
            DomainGoalPreset(if (isFr) "Veilles / Nuits de prière corporatives" else "Corporate Prayer Nights / Vigils", "1", strings.unitSessions, "WEEKLY", listOf(strings.unitSessions, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Heures de siège de prière" else "Prayer Siege Hours", "3", strings.unitHours, "WEEKLY", listOf(strings.unitHours, strings.unitSessions)),
            DomainGoalPreset(if (isFr) "Croisades de prière par mois" else "Prayer Crusades per Month", "1", strings.unitSessions, "MONTHLY", listOf(strings.unitSessions, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Réunions de prière d'assemblée / semaine" else "Church Prayer Meetings / Week", "2", strings.unitSessions, "WEEKLY", listOf(strings.unitSessions, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Objectif PPB personnalisé" else "Custom PWO Goal", "2", strings.unitHours, "WEEKLY", listOf(strings.unitHours, strings.unitMinutes, strings.unitSessions))
        )
        "ddewg" -> listOf(
            DomainGoalPreset(if (isFr) "RDQ quotidien (Rendez-vous avec Dieu)" else "Daily DDEWG (Daily Encounter)", "1", strings.unitDdewg, "DAILY", listOf(strings.unitDdewg, strings.unitSessions, strings.unitDays)),
            DomainGoalPreset(if (isFr) "RDQ hebdomadaires" else "Weekly DDEWG Encounters", "7", strings.unitDdewg, "WEEKLY", listOf(strings.unitDdewg, strings.unitSessions, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Nombre de RDQ par mois" else "Monthly DDEWG Count", "30", strings.unitDdewg, "MONTHLY", listOf(strings.unitDdewg, strings.unitSessions, strings.unitDays)),
            DomainGoalPreset(if (isFr) "RDQ annuels (365 jours)" else "Annual DDEWG a Year", "365", strings.unitDdewg, "YEARLY", listOf(strings.unitDdewg, strings.unitSessions, strings.unitDays)),
            DomainGoalPreset(if (isFr) "Garde du matin par semaine" else "Morning Watch Encounters", "7", strings.unitDdewg, "WEEKLY", listOf(strings.unitDdewg, strings.unitSessions)),
            DomainGoalPreset(if (isFr) "Objectif RDQ personnalisé" else "Custom DDEWG Goal", "7", strings.unitDdewg, "WEEKLY", listOf(strings.unitDdewg, strings.unitSessions, strings.unitHours))
        )
        "fasting" -> listOf(
            DomainGoalPreset(if (isFr) "Jours de jeûne par mois" else "Fasting Days in Month", "3", strings.unitDays, "MONTHLY", listOf(strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Objectif annuel de jeûne (40 jours)" else "Annual Fasting Target (Days a Year)", "40", strings.unitDays, "YEARLY", listOf(strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Jour de jeûne hebdomadaire" else "Weekly Fasting Day", "1", strings.unitDays, "WEEKLY", listOf(strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Jours de jeûne complet" else "Complete Fasting Days", "3", strings.unitDays, "MONTHLY", listOf(strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Jours de jeûne partiel / Daniel" else "Partial / Daniel Fast Days", "7", strings.unitDays, "MONTHLY", listOf(strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Sélectionner dates de jeûne (Calendrier)" else "Select Fasting Dates (Calendar)", "3", strings.unitDays, "MONTHLY", listOf(strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Objectif de jeûne personnalisé" else "Custom Fasting Goal", "3", strings.unitDays, "MONTHLY", listOf(strings.unitDays, strings.unitHours))
        )
        "christian_lit" -> listOf(
            DomainGoalPreset(if (isFr) "Livres chrétiens à lire par mois" else "Christian Books to Read in a Month", "2", strings.unitBooks, "MONTHLY", listOf(strings.unitBooks, strings.unitPages, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Livres chrétiens à lire par an" else "Christian Books to Read in a Year", "24", strings.unitBooks, "YEARLY", listOf(strings.unitBooks, strings.unitPages)),
            DomainGoalPreset(if (isFr) "Pages de lecture par jour" else "Daily Reading Pages", "20", strings.unitPages, "DAILY", listOf(strings.unitPages, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Heures de lecture par semaine" else "Weekly Reading Hours", "5", strings.unitHours, "WEEKLY", listOf(strings.unitHours, strings.unitMinutes)),
            DomainGoalPreset(if (isFr) "Objectif littérature personnalisé" else "Custom Literature Goal", "20", strings.unitPages, "DAILY", listOf(strings.unitBooks, strings.unitPages, strings.unitHours))
        )
        "christian_lit_mem" -> listOf(
            DomainGoalPreset(if (isFr) "Citations / extraits mémorisés" else "Quotes / Excerpts Mastered", "5", "Quotes", "MONTHLY", listOf("Quotes", strings.unitPages)),
            DomainGoalPreset(if (isFr) "Pages de mémorisation littéraire" else "Literature Memorization Pages", "10", strings.unitPages, "MONTHLY", listOf(strings.unitPages, "Quotes")),
            DomainGoalPreset(if (isFr) "Objectif mémorisation littéraire personnalisé" else "Custom Lit Mem Goal", "5", strings.unitPages, "MONTHLY", listOf(strings.unitPages, "Quotes"))
        )
        "bible_mem" -> listOf(
            DomainGoalPreset(if (isFr) "Versets mémorisés par semaine" else "Verses Memorized per Week", "5", strings.unitVerses, "WEEKLY", listOf(strings.unitVerses, strings.unitChapters)),
            DomainGoalPreset(if (isFr) "Versets mémorisés par an" else "Annual Verses Memorized", "250", strings.unitVerses, "YEARLY", listOf(strings.unitVerses, strings.unitChapters)),
            DomainGoalPreset(if (isFr) "Chapitres mémorisés par mois" else "Chapters Memorized in Month", "1", strings.unitChapters, "MONTHLY", listOf(strings.unitChapters, strings.unitVerses)),
            DomainGoalPreset(if (isFr) "Objectif mémorisation biblique personnalisé" else "Custom Bible Mem Goal", "5", strings.unitVerses, "WEEKLY", listOf(strings.unitVerses, strings.unitChapters))
        )
        "soul_winning" -> listOf(
            DomainGoalPreset(if (isFr) "Âmes gagnées (convertis) par mois" else "Souls Won (Converts) in a Month", "2", strings.unitConverts, "MONTHLY", listOf(strings.unitConverts, strings.unitSouls, "Baptisms")),
            DomainGoalPreset(if (isFr) "Objectif annuel d'âmes gagnées" else "Annual Souls Won Target", "20", strings.unitConverts, "YEARLY", listOf(strings.unitConverts, strings.unitSouls)),
            DomainGoalPreset(if (isFr) "Personnes évangélisées par semaine" else "People Preached To per Week", "5", strings.unitSouls, "WEEKLY", listOf(strings.unitSouls, strings.unitConverts, "Baptisms")),
            DomainGoalPreset(if (isFr) "Objectif de baptêmes" else "Baptisms Target", "5", "Baptisms", "YEARLY", listOf("Baptisms", strings.unitConverts, strings.unitSouls)),
            DomainGoalPreset(if (isFr) "Objectif évangélisation personnalisé" else "Custom Soul Winning Goal", "5", strings.unitSouls, "WEEKLY", listOf(strings.unitSouls, strings.unitConverts, "Baptisms"))
        )
        "proclamation_importunity" -> listOf(
            DomainGoalPreset(if (isFr) "Proclamations quotidiennes" else "Daily Proclamations", "50", strings.unitRepetitions, "DAILY", listOf(strings.unitRepetitions, strings.proclamationsMade, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Objectif hebdomadaire de proclamations" else "Weekly Proclamations Target", "500", strings.unitRepetitions, "WEEKLY", listOf(strings.unitRepetitions, strings.proclamationsMade)),
            DomainGoalPreset(if (isFr) "Objectif de sujet (Cumulatif)" else "Topic Target (Cumulative)", "1000", strings.unitRepetitions, "MONTHLY", listOf(strings.unitRepetitions, strings.proclamationsMade)),
            DomainGoalPreset(if (isFr) "Objectif proclamation personnalisé" else "Custom Proclamation Goal", "100", strings.unitRepetitions, "DAILY", listOf(strings.unitRepetitions, strings.proclamationsMade, strings.unitHours))
        )
        "retreats" -> listOf(
            DomainGoalPreset(if (isFr) "Jours de retraite mensuelle" else "Monthly Retreat Days", "1", strings.unitDays, "MONTHLY", listOf(strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Jours de retraite trimestrielle" else "Quarterly Retreat Days", "3", strings.unitDays, "MONTHLY", listOf(strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Jours de retraite annuelle" else "Annual Retreat Days", "14", strings.unitDays, "YEARLY", listOf(strings.unitDays, strings.unitHours)),
            DomainGoalPreset(if (isFr) "Objectif retraite personnalisé" else "Custom Retreat Goal", "24", strings.unitHours, "MONTHLY", listOf(strings.unitDays, strings.unitHours))
        )
        "giving" -> listOf(
            DomainGoalPreset(if (isFr) "Dîme mensuelle (10%)" else "Monthly Tithe (10%)", "10", "%", "MONTHLY", listOf("%", "XAF")),
            DomainGoalPreset(if (isFr) "Offrande mensuelle (XAF)" else "Monthly Offering (XAF)", "10000", "XAF", "MONTHLY", listOf("XAF", "%")),
            DomainGoalPreset(if (isFr) "Objectif offrande personnalisé" else "Custom Giving Goal", "10000", "XAF", "MONTHLY", listOf("XAF", "%"))
        )
        "making_disciples" -> listOf(
            DomainGoalPreset(if (isFr) "Disciples encadrés" else "Disciples Shepherded", "2", "Disciples", "MONTHLY", listOf("Disciples", strings.unitSessions)),
            DomainGoalPreset(if (isFr) "Sessions de discipulat" else "Discipleship Sessions", "4", strings.unitSessions, "MONTHLY", listOf(strings.unitSessions, "Disciples")),
            DomainGoalPreset(if (isFr) "Objectif discipulat personnalisé" else "Custom Discipleship Goal", "1", "Disciples", "MONTHLY", listOf("Disciples", strings.unitSessions))
        )
        else -> listOf(
            DomainGoalPreset(if (isFr) "Pratique de discipline" else "Discipline Practice", "1", strings.unitSessions, "DAILY", listOf(strings.unitSessions, strings.unitHours, strings.unitMinutes)),
            DomainGoalPreset(if (isFr) "Objectif personnalisé" else "Custom Goal", "1", strings.unitSessions, "DAILY", listOf(strings.unitSessions, strings.unitHours, strings.unitMinutes))
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddGoalDialog(
    strings: AppStrings,
    onDismiss: () -> Unit,
    onConfirm: (domainId: String, title: String, target: Double, unit: String, freq: String, fastingType: String, periodDays: Int, isReminder: Boolean, reminderTime: String) -> Unit
) {
    val isFr = strings is FrenchStrings
    val allDomains = remember { PredefinedDomains.ALL }
    var selectedDomain by remember { mutableStateOf("bible_reading") }

    val currentPresets = remember(selectedDomain) { getDomainPresets(selectedDomain, strings) }
    var selectedPresetTitle by remember(selectedDomain) { mutableStateOf(currentPresets.first().title) }
    var customTitle by remember { mutableStateOf("") }
    var target by remember(selectedDomain) { mutableStateOf(currentPresets.first().defaultTarget) }
    var selectedUnit by remember(selectedDomain) { mutableStateOf(currentPresets.first().defaultUnit) }
    var frequency by remember(selectedDomain) { mutableStateOf(currentPresets.first().defaultFrequency) }

    // Fasting & Reminder Specifics
    var goalFastingDates by remember { mutableStateOf(setOf(LocalDate.now())) }
    var fastingType by remember { mutableStateOf("COMPLETE") }
    var periodDaysInput by remember { mutableStateOf("3") }
    var isReminderEnabled by remember { mutableStateOf(false) }
    var reminderScheduleType by remember { mutableStateOf("DAILY") }
    var reminderDateIso by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var reminderTimeIso by remember { mutableStateOf("08:00") }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val activePreset = currentPresets.find { it.title == selectedPresetTitle } ?: currentPresets.first()
    val availableUnits = activePreset.allowedUnits

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(strings.addGoal, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 540.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. Domain Dropdown
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(strings.goalDomain, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    var domainExp by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = domainExp, onExpandedChange = { domainExp = !domainExp }) {
                        OutlinedTextField(
                            value = strings.getDomainTitleById(selectedDomain),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(strings.goalDomain) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = domainExp) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(expanded = domainExp, onDismissRequest = { domainExp = false }) {
                            allDomains.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(strings.getDomainTitleById(d.id)) },
                                    onClick = {
                                        selectedDomain = d.id
                                        domainExp = false
                                        val newPresets = getDomainPresets(d.id, strings)
                                        val firstP = newPresets.first()
                                        selectedPresetTitle = firstP.title
                                        target = firstP.defaultTarget
                                        selectedUnit = firstP.defaultUnit
                                        frequency = firstP.defaultFrequency
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Goal Presets
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (strings is FrenchStrings) "Modèle d'objectif" else "Preset Goal",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    var presetDropdownExp by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = presetDropdownExp,
                        onExpandedChange = { presetDropdownExp = !presetDropdownExp }
                    ) {
                        OutlinedTextField(
                            value = selectedPresetTitle,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (strings is FrenchStrings) "Modèle d'objectif" else "Preset Goal") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = presetDropdownExp) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = presetDropdownExp,
                            onDismissRequest = { presetDropdownExp = false }
                        ) {
                            currentPresets.forEach { preset ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(
                                                text = preset.title,
                                                fontWeight = if (selectedPresetTitle == preset.title) FontWeight.Bold else FontWeight.Normal
                                            )
                                            Text(
                                                text = "${preset.defaultTarget} ${preset.defaultUnit} • ${preset.defaultFrequency}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedPresetTitle = preset.title
                                        target = preset.defaultTarget
                                        selectedUnit = preset.defaultUnit
                                        frequency = preset.defaultFrequency
                                        presetDropdownExp = false
                                    }
                                )
                            }
                        }
                    }

                    // Quick Presets Chips
                    val quickPresets = remember(currentPresets) {
                        val list = currentPresets.take(3).toMutableList()
                        val customP = currentPresets.find { it.title.startsWith("Custom") || it.title.startsWith("Objectif") }
                        if (customP != null && !list.contains(customP)) {
                            list.add(customP)
                        }
                        list
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        quickPresets.forEach { preset ->
                            val isSelected = selectedPresetTitle == preset.title
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPresetTitle = preset.title
                                    target = preset.defaultTarget
                                    selectedUnit = preset.defaultUnit
                                    frequency = preset.defaultFrequency
                                },
                                label = {
                                    Text(
                                        text = preset.title,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }

                    if (selectedPresetTitle.startsWith("Custom") || selectedPresetTitle.contains("personnalisé", ignoreCase = true)) {
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = customTitle,
                            onValueChange = { customTitle = it },
                            label = { Text(strings.customGoalTitlePrompt) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                // 3. Fasting Calendar & Mode Selection
                if (selectedDomain == "fasting") {
                    HorizontalDivider()
                    FastingCalendarPicker(
                        strings = strings,
                        selectedDates = goalFastingDates,
                        onDatesChanged = { dates ->
                            goalFastingDates = dates
                            if (dates.isNotEmpty()) {
                                target = dates.size.toString()
                                periodDaysInput = dates.size.toString()
                            }
                        },
                        fastingType = if (fastingType == "PARTIAL") "Partial Fast" else "Complete Fast",
                        onFastingTypeChanged = { type ->
                            fastingType = if (type.contains("Partial", ignoreCase = true)) "PARTIAL" else "COMPLETE"
                        }
                    )
                    HorizontalDivider()
                }

                // 4. Target Value and Unit
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(if (strings is FrenchStrings) "Cible et Unité" else "Target & Metric", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = target,
                            onValueChange = { target = it },
                            label = { Text(strings.targetValue) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f).testTag("add_goal_target_input"),
                            singleLine = true
                        )

                        var unitExp by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = unitExp,
                            onExpandedChange = { unitExp = !unitExp },
                            modifier = Modifier.weight(1.3f)
                        ) {
                            OutlinedTextField(
                                value = selectedUnit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text(strings.targetUnit) },
                                shape = RoundedCornerShape(14.dp),
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExp) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                singleLine = true
                            )
                            ExposedDropdownMenu(expanded = unitExp, onDismissRequest = { unitExp = false }) {
                                availableUnits.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            selectedUnit = u
                                            unitExp = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Target Period / Frequency
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(strings.targetPeriod, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "DAILY" to strings.daily,
                            "WEEKLY" to strings.weekly,
                            "MONTHLY" to strings.monthly,
                            "YEARLY" to strings.yearly
                        ).forEach { (freqKey, freqLabel) ->
                            val isSelected = frequency == freqKey
                            FilterChip(
                                selected = isSelected,
                                onClick = { frequency = freqKey },
                                label = {
                                    Text(
                                        freqLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))

                // 6. Reminder Setting
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(if (isFr) "Activer le rappel" else "Enable Reminder", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(if (isFr) "Pour la constance spirituelle" else "Stay consistent towards this target", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = isReminderEnabled,
                        onCheckedChange = { isReminderEnabled = it }
                    )
                }

                if (isReminderEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (isFr) "Fréquence du rappel" else "Reminder Cadence", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "DAILY" to strings.daily,
                                "SPECIFIC_DATE" to (if (isFr) "Date spécifique" else "Specific Date"),
                                "WEEKLY" to strings.weekly,
                                "MONTHLY" to strings.monthly
                            ).forEach { (schedKey, schedLabel) ->
                                val isSelected = reminderScheduleType == schedKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { reminderScheduleType = schedKey },
                                    label = {
                                        Text(
                                            schedLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        if (reminderScheduleType == "SPECIFIC_DATE") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (isFr) "Date du rappel :" else "Reminder Date:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                OutlinedButton(
                                    onClick = { showDatePicker = true },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(reminderDateIso, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (isFr) "Heure du rappel :" else "Reminder Time:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            OutlinedButton(
                                onClick = { showTimePicker = true },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(reminderTimeIso, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tVal = target.toDoubleOrNull() ?: 1.0
                    val finalTitle = if (selectedPresetTitle.startsWith("Custom") || selectedPresetTitle.contains("personnalisé", ignoreCase = true)) {
                        customTitle.ifBlank { "${strings.getDomainTitleById(selectedDomain)} Goal" }
                    } else {
                        selectedPresetTitle
                    }
                    val periodDays = periodDaysInput.toIntOrNull() ?: 0
                    val finalReminderTime = if (reminderScheduleType == "SPECIFIC_DATE") {
                        "$reminderDateIso $reminderTimeIso"
                    } else {
                        reminderTimeIso
                    }
                    onConfirm(selectedDomain, finalTitle, tVal, selectedUnit, frequency, fastingType, periodDays, isReminderEnabled, finalReminderTime)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_add_goal_button")
            ) {
                Text(strings.saveGoal, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.cancelTimer)
            }
        }
    )

    if (showDatePicker) {
        AppDatePickerDialog(
            initialDateIso = reminderDateIso,
            maxDateIso = LocalDate.now().plusYears(2).toString(),
            onDateSelected = { date ->
                reminderDateIso = date
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }

    if (showTimePicker) {
        AppTimePickerDialog(
            initialTime = reminderTimeIso,
            onDismiss = { showTimePicker = false },
            onTimeSelected = { selectedTime ->
                reminderTimeIso = selectedTime
                showTimePicker = false
            }
        )
    }
}
