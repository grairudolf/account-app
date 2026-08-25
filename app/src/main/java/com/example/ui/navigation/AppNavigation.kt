package com.example.ui.navigation

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.core.localization.AppLanguage
import com.example.core.localization.LocalizationManager
import com.example.ui.components.BottomTab
import com.example.ui.components.CmfiBottomBar
import com.example.ui.components.CmfiTopBar
import com.example.ui.screens.*
import com.example.ui.theme.CmfiTheme
import com.example.ui.viewmodels.*
import kotlinx.coroutines.launch

object NavRoutes {
    const val AUTH = "auth"
    const val MAIN_PAGER = "main_pager"
    const val DASHBOARD = "dashboard"
    const val DOMAINS = "domains"
    const val DOMAIN_DETAIL = "domain_detail/{domainId}"
    const val GOALS = "goals"
    const val CALENDAR = "calendar"
    const val STATISTICS = "statistics"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val TIMER = "timer/{domainId}"
    const val PROCLAMATION = "proclamation"

    fun domainDetail(domainId: String) = "domain_detail/$domainId"
    fun timer(domainId: String) = "timer/$domainId"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainApp() {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val notificationsViewModel: NotificationsViewModel = viewModel(factory = factory)
    val globalTimerViewModel: TimerViewModel = viewModel(factory = factory)

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val currentLanguage by settingsViewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle()
    val notificationsList by notificationsViewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by notificationsViewModel.unreadCount.collectAsStateWithLifecycle()
    val globalActiveTimer by globalTimerViewModel.activeSession.collectAsStateWithLifecycle()
    val globalTimerElapsedSeconds by globalTimerViewModel.elapsedSeconds.collectAsStateWithLifecycle()

    val strings = LocalizationManager.getStrings(currentLanguage)

    CmfiTheme(themeMode = currentTheme) {
        val currentUserState = currentUser
        if (currentUserState == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            
            val initialStartDestination = remember {
                if (authViewModel.hasCompletedAuthPrompt() || !currentUserState.isGuest) {
                    NavRoutes.MAIN_PAGER
                } else {
                    NavRoutes.AUTH
                }
            }

            val currentNavRoute = navBackStackEntry?.destination?.route ?: initialStartDestination

            val pagerState = rememberPagerState(
                initialPage = 0,
                pageCount = { BottomTab.entries.size }
            )

            val isMainPagerActive = currentNavRoute == NavRoutes.MAIN_PAGER
            val currentTab = BottomTab.entries.getOrElse(pagerState.currentPage) { BottomTab.DASHBOARD }
            val activeRouteName = if (isMainPagerActive) currentTab.route else currentNavRoute

            val isMainBottomBarVisible = isMainPagerActive
            val isTopBarVisible = currentNavRoute != NavRoutes.AUTH

        val topBarTitle = if (isMainPagerActive) {
            when (pagerState.currentPage) {
                0 -> strings.appName
                1 -> strings.domains
                2 -> strings.goals
                3 -> strings.statistics
                4 -> strings.reports
                else -> strings.appName
            }
        } else {
            when (currentNavRoute) {
                NavRoutes.SETTINGS -> strings.settings
                NavRoutes.SEARCH -> strings.search
                NavRoutes.NOTIFICATIONS -> "Notifications"
                NavRoutes.CALENDAR -> "Spiritual Calendar"
                else -> strings.appName
            }
        }

        Scaffold(
            topBar = {
                if (isTopBarVisible) {
                    CmfiTopBar(
                        title = topBarTitle,
                        userName = currentUserState.fullName.ifBlank { "Disciple" },
                        profileImageUri = currentUserState.profileImageUri,
                        currentLanguage = currentLanguage,
                        unreadCount = unreadNotificationsCount,
                        onLanguageSelected = { newLang ->
                            settingsViewModel.updateLanguage(newLang)
                        },
                        onProfileClick = {
                            if (currentNavRoute != NavRoutes.SETTINGS) {
                                navController.navigate(NavRoutes.SETTINGS)
                            }
                        },
                        onNotificationClick = {
                            if (currentNavRoute != NavRoutes.NOTIFICATIONS) {
                                navController.navigate(NavRoutes.NOTIFICATIONS)
                            }
                        },
                        onSearchClick = {
                            if (currentNavRoute != NavRoutes.SEARCH) {
                                navController.navigate(NavRoutes.SEARCH)
                            }
                        }
                    )
                }
            },
            bottomBar = {
                Column {
                    val activeTimer = globalActiveTimer
                    if (activeTimer != null && currentNavRoute != NavRoutes.TIMER) {
                        val hours = globalTimerElapsedSeconds / 3600
                        val minutes = (globalTimerElapsedSeconds % 3600) / 60
                        val seconds = globalTimerElapsedSeconds % 60
                        val timeStr = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                        Surface(
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clickable {
                                    navController.navigate(NavRoutes.timer(activeTimer.domainId))
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    val isRunningActive = activeTimer.isRunning && !activeTimer.isPaused
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(if (isRunningActive) StatusSuccess else AccentPurple)
                                    )
                                    val domainLabel = if (activeTimer.domainId == "ddewg") strings.ddewgAbbr else strings.getDomainTitleById(activeTimer.domainId)
                                    Text(
                                        text = "${domainLabel.uppercase()}: $timeStr",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.wrapContentWidth()
                                ) {
                                    val isRunningActive = activeTimer.isRunning && !activeTimer.isPaused
                                    Text(
                                        text = if (isRunningActive) strings.timerRunning else strings.timerPaused,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Open Timer",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (isMainBottomBarVisible) {
                        CmfiBottomBar(
                            currentRoute = activeRouteName,
                            strings = strings,
                            onTabSelected = { tab ->
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(
                                        page = tab.ordinal,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    )
                                }
                            }
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = initialStartDestination,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                        initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() }
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                    slideOutHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                        targetOffsetX = { fullWidth -> (-fullWidth * 0.35f).toInt() }
                    )
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)) +
                    slideInHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                        initialOffsetX = { fullWidth -> (-fullWidth * 0.35f).toInt() }
                    )
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                    slideOutHorizontally(
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioNoBouncy),
                        targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() }
                    )
                }
            ) {
                composable(NavRoutes.AUTH) {
                    AuthScreen(
                        strings = strings,
                        onContinueAsGuest = {
                            authViewModel.continueAsGuest()
                            navController.navigate(NavRoutes.MAIN_PAGER) {
                                popUpTo(NavRoutes.AUTH) { inclusive = true }
                            }
                        },
                        onSignInWithAccount = { id, name, email, photoUrl, assembly ->
                            authViewModel.signInWithAccount(id, name, email, photoUrl, assembly, migrateLocalData = true)
                            navController.navigate(NavRoutes.MAIN_PAGER) {
                                popUpTo(NavRoutes.AUTH) { inclusive = true }
                            }
                        }
                    )
                }

                // Main Swipeable Tab Container (Dashboard, Domains, Goals, Statistics, Reports)
                composable(NavRoutes.MAIN_PAGER) {
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
                    val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                    val statisticsViewModel: StatisticsViewModel = viewModel(factory = factory)
                    val domainsViewModel: DomainsViewModel = viewModel(factory = factory)
                    val goalsViewModel: GoalsViewModel = viewModel(factory = factory)
                    val reportsViewModel: ReportsViewModel = viewModel(factory = factory)

                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                        key = { it }
                    ) { page ->
                        when (page) {
                            0 -> {
                                DashboardScreen(
                                    strings = strings,
                                    uiState = dashboardUiState,
                                    onNavigateToDomain = { domainId ->
                                        navController.navigate(NavRoutes.domainDetail(domainId))
                                    },
                                    onNavigateToDomains = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(BottomTab.DOMAINS.ordinal)
                                        }
                                    },
                                    onNavigateToGoals = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(BottomTab.GOALS.ordinal)
                                        }
                                    },
                                    onQuickAdd = { domainId ->
                                        navController.navigate(NavRoutes.domainDetail(domainId))
                                    },
                                    onNavigateToRecentActivity = { entry ->
                                        statisticsViewModel.selectRecentActivity(entry.dateIso)
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(BottomTab.STATISTICS.ordinal)
                                        }
                                    }
                                )
                            }
                            1 -> {
                                val searchDomainQuery by domainsViewModel.searchQuery.collectAsStateWithLifecycle()
                                val domainsList by domainsViewModel.allDomainsFlow.collectAsStateWithLifecycle()
                                DomainsScreen(
                                    strings = strings,
                                    domains = domainsList,
                                    searchQuery = searchDomainQuery,
                                    onSearchQueryChange = { domainsViewModel.onSearchQueryChange(it) },
                                    onDomainClick = { domainId ->
                                        navController.navigate(NavRoutes.domainDetail(domainId))
                                    },
                                    onAddCustomDomain = { name, desc, icon, unit ->
                                        domainsViewModel.addCustomDomain(name, desc, icon, unit)
                                    }
                                )
                            }
                            2 -> {
                                val goalsWithProgress by goalsViewModel.goalsWithProgressFlow.collectAsStateWithLifecycle()
                                val selectedGoalFreq by goalsViewModel.selectedFrequency.collectAsStateWithLifecycle()
                                GoalsScreen(
                                    strings = strings,
                                    goalsWithProgress = goalsWithProgress,
                                    selectedFrequency = selectedGoalFreq,
                                    onFrequencySelected = { goalsViewModel.onFrequencySelected(it) },
                                    onAddGoal = { uId, dId, title, target, unit, freq, startDate, fastingType, periodDays, isReminder, reminderTime ->
                                        goalsViewModel.addGoal(uId, dId, title, target, unit, freq, startDate, fastingType, periodDays, isReminder, reminderTime)
                                    },
                                    onDeleteGoal = { goalsViewModel.deleteGoal(it) }
                                )
                            }
                            3 -> {
                                val statsUiState by statisticsViewModel.uiState.collectAsStateWithLifecycle()
                                val selectedDate by statisticsViewModel.selectedDate.collectAsStateWithLifecycle()
                                val currentMonth by statisticsViewModel.currentMonth.collectAsStateWithLifecycle()
                                val monthDaysCompletion by statisticsViewModel.monthDaysCompletionFlow.collectAsStateWithLifecycle()
                                val selectedDateEntries by statisticsViewModel.selectedDateEntries.collectAsStateWithLifecycle()
                                val allEntries by statisticsViewModel.allEntries.collectAsStateWithLifecycle()
                                val selectedTab by statisticsViewModel.selectedTab.collectAsStateWithLifecycle()
                                val selectedTimeRange by statisticsViewModel.selectedTimeRange.collectAsStateWithLifecycle()

                                StatisticsScreen(
                                    strings = strings,
                                    uiState = statsUiState,
                                    selectedDate = selectedDate,
                                    currentMonth = currentMonth,
                                    monthDaysCompletion = monthDaysCompletion,
                                    selectedDateEntries = selectedDateEntries,
                                    allEntries = allEntries,
                                    selectedTab = selectedTab,
                                    selectedTimeRange = selectedTimeRange,
                                    onTimeRangeSelected = { statisticsViewModel.setTimeRange(it) },
                                    onTabSelected = { statisticsViewModel.setSelectedTab(it) },
                                    onSelectDate = { statisticsViewModel.selectDate(it) },
                                    onNextMonth = { statisticsViewModel.nextMonth() },
                                    onPreviousMonth = { statisticsViewModel.previousMonth() },
                                    onGoToToday = { statisticsViewModel.goToToday() },
                                    onUpdateEntry = { statisticsViewModel.updateEntry(it) },
                                    onDeleteEntry = { statisticsViewModel.deleteEntry(it) }
                                )
                            }
                            4 -> {
                                val selectedReportType by reportsViewModel.selectedReportType.collectAsStateWithLifecycle()
                                val selectedDomains by reportsViewModel.selectedDomains.collectAsStateWithLifecycle()
                                val targetDate by reportsViewModel.targetDate.collectAsStateWithLifecycle()
                                val startDate by reportsViewModel.startDate.collectAsStateWithLifecycle()
                                val endDate by reportsViewModel.endDate.collectAsStateWithLifecycle()
                                val reportHistory by reportsViewModel.reportHistory.collectAsStateWithLifecycle()
                                ReportsScreen(
                                    strings = strings,
                                    user = currentUserState,
                                    selectedReportType = selectedReportType,
                                    selectedDomains = selectedDomains,
                                    targetDate = targetDate,
                                    startDate = startDate,
                                    endDate = endDate,
                                    reportHistory = reportHistory,
                                    onSelectReportType = { reportsViewModel.selectReportType(it) },
                                    onToggleDomainFilter = { reportsViewModel.toggleDomainFilter(it) },
                                    onSelectAllDomains = { reportsViewModel.selectAllDomains() },
                                    onSetTargetDate = { reportsViewModel.setTargetDate(it) },
                                    onSetDateRange = { start, end -> reportsViewModel.setDateRange(start, end) },
                                    onGeneratePdfReport = { ctx, onFile ->
                                        reportsViewModel.generatePdfReport(ctx, onFile)
                                    },
                                    onDeleteReport = { reportId ->
                                        reportsViewModel.deleteReport(reportId)
                                    }
                                )
                            }
                        }
                    }
                }

                composable(
                    route = NavRoutes.DOMAIN_DETAIL,
                    arguments = listOf(navArgument("domainId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val domainId = backStackEntry.arguments?.getString("domainId") ?: "ddewg"
                    if (domainId == "proclamation_importunity") {
                        val proclamationViewModel: ProclamationViewModel = viewModel(factory = factory)
                        ProclamationScreen(
                            viewModel = proclamationViewModel,
                            strings = strings,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    } else {
                        val entryViewModel: EntryViewModel = viewModel(factory = factory)
                        val disciplesList by entryViewModel.disciples.collectAsStateWithLifecycle()
                        val allEntriesList by entryViewModel.allEntries.collectAsStateWithLifecycle()
                        DomainDetailScreen(
                            domainId = domainId,
                            strings = strings,
                            userId = currentUserState.id,
                            disciples = disciplesList,
                            allEntries = allEntriesList,
                            onSaveDisciple = { entryViewModel.saveDisciple(it) },
                            onUpdateDisciple = { entryViewModel.updateDisciple(it) },
                            onDeleteDisciple = { entryViewModel.deleteDisciple(it) },
                            onNavigateToTimer = { id ->
                                navController.navigate(NavRoutes.timer(id))
                            },
                            onSaveEntry = { entry ->
                                entryViewModel.saveEntry(context, entry)
                                navController.popBackStack()
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }
                }

                composable(NavRoutes.PROCLAMATION) {
                    val proclamationViewModel: ProclamationViewModel = viewModel(factory = factory)
                    ProclamationScreen(
                        viewModel = proclamationViewModel,
                        strings = strings,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.CALENDAR) {
                    val calendarViewModel: CalendarViewModel = viewModel(factory = factory)
                    val calSelectedDate by calendarViewModel.selectedDate.collectAsStateWithLifecycle()
                    val calCurrentMonth by calendarViewModel.currentMonth.collectAsStateWithLifecycle()
                    val calMonthCompletion by calendarViewModel.monthDaysCompletionFlow.collectAsStateWithLifecycle()
                    val calDateEntries by calendarViewModel.selectedDateEntries.collectAsStateWithLifecycle()
                    CalendarScreen(
                        strings = strings,
                        selectedDate = calSelectedDate,
                        currentMonth = calCurrentMonth,
                        monthDaysCompletion = calMonthCompletion,
                        selectedDateEntries = calDateEntries,
                        onSelectDate = { calendarViewModel.selectDate(it) },
                        onNextMonth = { calendarViewModel.nextMonth() },
                        onPreviousMonth = { calendarViewModel.previousMonth() },
                        onGoToToday = { calendarViewModel.goToToday() }
                    )
                }

                composable(NavRoutes.NOTIFICATIONS) {
                    NotificationsScreen(
                        notifications = notificationsList,
                        unreadCount = unreadNotificationsCount,
                        onMarkAllAsRead = { notificationsViewModel.markAllAsRead() },
                        onDeleteNotification = { notificationsViewModel.deleteNotification(it) },
                        onClearAll = { notificationsViewModel.clearAllNotifications() },
                        onTriggerTestNotification = { ctx -> notificationsViewModel.triggerTestNotification(ctx) },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.SETTINGS) {
                    val settingsReminders by settingsViewModel.reminders.collectAsStateWithLifecycle()
                    val isSyncing by settingsViewModel.isSyncing.collectAsStateWithLifecycle()
                    val syncProgress by settingsViewModel.syncProgress.collectAsStateWithLifecycle()
                    SettingsScreen(
                        strings = strings,
                        user = currentUserState,
                        currentLanguage = currentLanguage,
                        currentTheme = currentTheme,
                        reminders = settingsReminders,
                        isSyncing = isSyncing,
                        syncProgress = syncProgress,
                        onSyncCloudData = { settingsViewModel.syncCloudData() },
                        onUpdateLanguage = { settingsViewModel.updateLanguage(it) },
                        onUpdateTheme = { settingsViewModel.updateThemeMode(it) },
                        onUpdateProfileImage = { uri -> settingsViewModel.updateProfileImage(uri) },
                        onUpdateProfile = { name, email, assembly, maker, phone, convDate, accDays ->
                            settingsViewModel.updateProfile(name, email, assembly, maker, phone, convDate, accDays)
                        },
                        onAddReminder = { ctx, dId, title, msg, h, m ->
                            settingsViewModel.addOrUpdateReminder(ctx, dId, title, msg, h, m)
                        },
                        onEditReminder = { ctx, dId, title, msg, h, m, id ->
                            settingsViewModel.addOrUpdateReminder(ctx, dId, title, msg, h, m, id)
                        },
                        onToggleReminder = { ctx, rem, isEnabled ->
                            settingsViewModel.toggleReminder(ctx, rem, isEnabled)
                        },
                        onDeleteReminder = { ctx, id -> settingsViewModel.deleteReminder(ctx, id) },
                        onSignOut = {
                            val isGuest = currentUserState.isGuest
                            if (isGuest) {
                                // If guest, safely navigate to the login/auth screen by removing the Settings screen from backstack
                                // This preserves their local data and allows them to sign in/up and migrate data
                                navController.navigate(NavRoutes.AUTH) {
                                    popUpTo(NavRoutes.MAIN_PAGER) { inclusive = false }
                                }
                            } else {
                                // If authenticated user, perform a full sign out and clear backstack up to MAIN_PAGER
                                authViewModel.signOut()
                                navController.navigate(NavRoutes.AUTH) {
                                    popUpTo(NavRoutes.MAIN_PAGER) { inclusive = false }
                                }
                            }
                        }
                    )
                }

                composable(NavRoutes.SEARCH) {
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
                    val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                    SearchScreen(
                        strings = strings,
                        allEntries = dashboardUiState.allEntries,
                        onNavigateToDomain = { domainId ->
                            navController.navigate(NavRoutes.domainDetail(domainId))
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    route = NavRoutes.TIMER,
                    arguments = listOf(navArgument("domainId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val domainId = backStackEntry.arguments?.getString("domainId") ?: "ddewg"
                    val timerViewModel: TimerViewModel = viewModel(factory = factory)
                    val activeTimerSession by timerViewModel.activeSession.collectAsStateWithLifecycle()
                    val timerElapsedSeconds by timerViewModel.elapsedSeconds.collectAsStateWithLifecycle()
                    TimerScreen(
                        domainId = domainId,
                        strings = strings,
                        userId = currentUserState.id,
                        activeSession = activeTimerSession,
                        elapsedSeconds = timerElapsedSeconds,
                        onStartTimer = { dId ->
                            timerViewModel.startTimer(currentUserState.id, dId)
                        },
                        onPauseTimer = { timerViewModel.pauseTimer() },
                        onResumeTimer = { timerViewModel.resumeTimer() },
                        onStopAndSaveTimer = { entry ->
                            timerViewModel.saveEntryAndStopTimer(entry)
                            navController.popBackStack()
                        },
                        onDiscardTimer = { timerViewModel.discardTimer() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
}
