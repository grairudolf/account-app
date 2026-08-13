package com.example.ui.navigation

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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

object NavRoutes {
    const val AUTH = "auth"
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

    fun domainDetail(domainId: String) = "domain_detail/$domainId"
    fun timer(domainId: String) = "timer/$domainId"
}

@Composable
fun MainApp() {
    val context = LocalContext.current
    val factory = remember { ViewModelFactory.getInstance(context) }

    val authViewModel: AuthViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val notificationsViewModel: NotificationsViewModel = viewModel(factory = factory)

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val currentLanguage by settingsViewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle()
    val notificationsList by notificationsViewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by notificationsViewModel.unreadCount.collectAsStateWithLifecycle()

    val strings = LocalizationManager.getStrings(currentLanguage)

    CmfiTheme(themeMode = currentTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.DASHBOARD

        val isMainBottomBarVisible = currentRoute in listOf(
            NavRoutes.DASHBOARD, NavRoutes.DOMAINS, NavRoutes.GOALS,
            NavRoutes.STATISTICS, NavRoutes.REPORTS
        )

        val isTopBarVisible = currentRoute != NavRoutes.AUTH

        val topBarTitle = when (currentRoute) {
            NavRoutes.DASHBOARD -> strings.appName
            NavRoutes.DOMAINS -> strings.domains
            NavRoutes.GOALS -> strings.goals
            NavRoutes.STATISTICS -> strings.statistics
            NavRoutes.REPORTS -> strings.reports
            NavRoutes.SETTINGS -> strings.settings
            NavRoutes.SEARCH -> strings.search
            NavRoutes.NOTIFICATIONS -> "Notifications"
            NavRoutes.CALENDAR -> "Spiritual Calendar"
            else -> strings.appName
        }

        Scaffold(
            topBar = {
                if (isTopBarVisible) {
                    CmfiTopBar(
                        title = topBarTitle,
                        userName = currentUser?.fullName?.ifBlank { "Disciple" } ?: "Disciple",
                        currentLanguage = currentLanguage,
                        unreadCount = unreadNotificationsCount,
                        onLanguageSelected = { newLang ->
                            settingsViewModel.updateLanguage(newLang)
                        },
                        onProfileClick = {
                            if (currentRoute != NavRoutes.SETTINGS) {
                                navController.navigate(NavRoutes.SETTINGS)
                            }
                        },
                        onNotificationClick = {
                            if (currentRoute != NavRoutes.NOTIFICATIONS) {
                                navController.navigate(NavRoutes.NOTIFICATIONS)
                            }
                        },
                        onSearchClick = {
                            if (currentRoute != NavRoutes.SEARCH) {
                                navController.navigate(NavRoutes.SEARCH)
                            }
                        }
                    )
                }
            },
            bottomBar = {
                if (isMainBottomBarVisible) {
                    CmfiBottomBar(
                        currentRoute = currentRoute,
                        strings = strings,
                        onTabSelected = { tab ->
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            val mainTabsList = remember {
                listOf(
                    NavRoutes.DASHBOARD,
                    NavRoutes.DOMAINS,
                    NavRoutes.GOALS,
                    NavRoutes.STATISTICS,
                    NavRoutes.REPORTS
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pointerInput(currentRoute) {
                        if (isMainBottomBarVisible) {
                            var totalDrag = 0f
                            detectHorizontalDragGestures(
                                onDragStart = { totalDrag = 0f },
                                onHorizontalDrag = { _, dragAmount -> totalDrag += dragAmount },
                                onDragEnd = {
                                    val idx = mainTabsList.indexOf(currentRoute)
                                    if (totalDrag < -50f && idx in 0 until mainTabsList.size - 1) {
                                        navController.navigate(mainTabsList[idx + 1]) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } else if (totalDrag > 50f && idx > 0) {
                                        navController.navigate(mainTabsList[idx - 1]) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
            ) {
                NavHost(
                    navController = navController,
                    startDestination = NavRoutes.DASHBOARD,
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
                            navController.navigate(NavRoutes.DASHBOARD) {
                                popUpTo(NavRoutes.AUTH) { inclusive = true }
                            }
                        },
                        onSignInWithAccount = { id, name, email ->
                            authViewModel.signInWithAccount(id, name, email, migrateLocalData = true)
                            navController.navigate(NavRoutes.DASHBOARD) {
                                popUpTo(NavRoutes.AUTH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(NavRoutes.DASHBOARD) {
                    val dashboardViewModel: DashboardViewModel = viewModel(factory = factory)
                    val dashboardUiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
                    val statisticsViewModel: StatisticsViewModel = viewModel(factory = factory)
                    DashboardScreen(
                        strings = strings,
                        uiState = dashboardUiState,
                        onNavigateToDomain = { domainId ->
                            navController.navigate(NavRoutes.domainDetail(domainId))
                        },
                        onNavigateToGoals = {
                            navController.navigate(NavRoutes.GOALS)
                        },
                        onQuickAdd = { domainId ->
                            navController.navigate(NavRoutes.domainDetail(domainId))
                        },
                        onNavigateToRecentActivity = { entry ->
                            statisticsViewModel.selectRecentActivity(entry.dateIso)
                            navController.navigate(NavRoutes.STATISTICS) {
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable(NavRoutes.DOMAINS) {
                    val domainsViewModel: DomainsViewModel = viewModel(factory = factory)
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

                composable(
                    route = NavRoutes.DOMAIN_DETAIL,
                    arguments = listOf(navArgument("domainId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val domainId = backStackEntry.arguments?.getString("domainId") ?: "ddewg"
                    val entryViewModel: EntryViewModel = viewModel(factory = factory)
                    DomainDetailScreen(
                        domainId = domainId,
                        strings = strings,
                        onNavigateToTimer = { id ->
                            navController.navigate(NavRoutes.timer(id))
                        },
                        onSaveEntry = { entry ->
                            entryViewModel.saveEntry(context, entry)
                            navController.navigate(NavRoutes.DOMAINS) {
                                popUpTo(NavRoutes.DOMAINS)
                                launchSingleTop = true
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.GOALS) {
                    val goalsViewModel: GoalsViewModel = viewModel(factory = factory)
                    val goalsWithProgress by goalsViewModel.goalsWithProgressFlow.collectAsStateWithLifecycle()
                    val selectedGoalFreq by goalsViewModel.selectedFrequency.collectAsStateWithLifecycle()
                    GoalsScreen(
                        strings = strings,
                        goalsWithProgress = goalsWithProgress,
                        selectedFrequency = selectedGoalFreq,
                        onFrequencySelected = { goalsViewModel.onFrequencySelected(it) },
                        onAddGoal = { uId, dId, title, target, unit, freq, startDate ->
                            goalsViewModel.addGoal(uId, dId, title, target, unit, freq, startDate)
                        },
                        onDeleteGoal = { goalsViewModel.deleteGoal(it) }
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

                composable(NavRoutes.STATISTICS) {
                    val statisticsViewModel: StatisticsViewModel = viewModel(factory = factory)
                    val statsUiState by statisticsViewModel.uiState.collectAsStateWithLifecycle()
                    val selectedDate by statisticsViewModel.selectedDate.collectAsStateWithLifecycle()
                    val currentMonth by statisticsViewModel.currentMonth.collectAsStateWithLifecycle()
                    val monthDaysCompletion by statisticsViewModel.monthDaysCompletionFlow.collectAsStateWithLifecycle()
                    val selectedDateEntries by statisticsViewModel.selectedDateEntries.collectAsStateWithLifecycle()
                    val allEntries by statisticsViewModel.allEntries.collectAsStateWithLifecycle()
                    val selectedTab by statisticsViewModel.selectedTab.collectAsStateWithLifecycle()

                    StatisticsScreen(
                        strings = strings,
                        uiState = statsUiState,
                        selectedDate = selectedDate,
                        currentMonth = currentMonth,
                        monthDaysCompletion = monthDaysCompletion,
                        selectedDateEntries = selectedDateEntries,
                        allEntries = allEntries,
                        selectedTab = selectedTab,
                        onTabSelected = { statisticsViewModel.setSelectedTab(it) },
                        onSelectDate = { statisticsViewModel.selectDate(it) },
                        onNextMonth = { statisticsViewModel.nextMonth() },
                        onPreviousMonth = { statisticsViewModel.previousMonth() },
                        onGoToToday = { statisticsViewModel.goToToday() },
                        onUpdateEntry = { statisticsViewModel.updateEntry(it) },
                        onDeleteEntry = { statisticsViewModel.deleteEntry(it) }
                    )
                }

                composable(NavRoutes.REPORTS) {
                    val reportsViewModel: ReportsViewModel = viewModel(factory = factory)
                    val selectedReportType by reportsViewModel.selectedReportType.collectAsStateWithLifecycle()
                    val selectedDomains by reportsViewModel.selectedDomains.collectAsStateWithLifecycle()
                    val targetDate by reportsViewModel.targetDate.collectAsStateWithLifecycle()
                    val startDate by reportsViewModel.startDate.collectAsStateWithLifecycle()
                    val endDate by reportsViewModel.endDate.collectAsStateWithLifecycle()
                    val reportHistory by reportsViewModel.reportHistory.collectAsStateWithLifecycle()
                    ReportsScreen(
                        strings = strings,
                        user = currentUser,
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
                    SettingsScreen(
                        strings = strings,
                        user = currentUser,
                        currentLanguage = currentLanguage,
                        currentTheme = currentTheme,
                        reminders = settingsReminders,
                        onUpdateLanguage = { settingsViewModel.updateLanguage(it) },
                        onUpdateTheme = { settingsViewModel.updateThemeMode(it) },
                        onUpdateProfileImage = { uri -> settingsViewModel.updateProfileImage(uri) },
                        onUpdateProfile = { name, email, assembly, maker, phone, convDate, accDays ->
                            settingsViewModel.updateProfile(name, email, assembly, maker, phone, convDate, accDays)
                        },
                        onAddReminder = { ctx, dId, title, msg, h, m ->
                            settingsViewModel.addOrUpdateReminder(ctx, dId, title, msg, h, m)
                        },
                        onDeleteReminder = { ctx, id -> settingsViewModel.deleteReminder(ctx, id) },
                        onSignOut = {
                            authViewModel.signOut()
                            navController.navigate(NavRoutes.AUTH) {
                                popUpTo(0) { inclusive = true }
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
                        activeSession = activeTimerSession,
                        elapsedSeconds = timerElapsedSeconds,
                        onStartTimer = { dId ->
                            timerViewModel.startTimer(currentUser?.id ?: "guest_user", dId)
                        },
                        onPauseTimer = { timerViewModel.pauseTimer() },
                        onResumeTimer = { timerViewModel.resumeTimer() },
                        onStopAndSaveTimer = { notes, reflection ->
                            timerViewModel.stopAndSaveTimer(notes, reflection)
                            navController.navigate(NavRoutes.DOMAINS) {
                                popUpTo(NavRoutes.DOMAINS)
                                launchSingleTop = true
                            }
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
