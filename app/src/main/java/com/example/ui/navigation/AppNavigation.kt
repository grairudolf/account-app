package com.example.ui.navigation

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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

    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val currentLanguage by settingsViewModel.currentLanguage.collectAsStateWithLifecycle()
    val currentTheme by settingsViewModel.currentTheme.collectAsStateWithLifecycle()

    val strings = LocalizationManager.getStrings(currentLanguage)

    CmfiTheme(themeMode = currentTheme) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.DASHBOARD

        val isMainBottomBarVisible = currentRoute in listOf(
            NavRoutes.DASHBOARD, NavRoutes.DOMAINS, NavRoutes.GOALS,
            NavRoutes.STATISTICS, NavRoutes.REPORTS
        )

        Scaffold(
            topBar = {
                if (isMainBottomBarVisible) {
                    CmfiTopBar(
                        title = strings.appName,
                        userName = currentUser?.fullName?.ifBlank { "Disciple" } ?: "Disciple",
                        currentLanguage = currentLanguage,
                        onLanguageSelected = { newLang ->
                            settingsViewModel.updateLanguage(newLang)
                        },
                        onProfileClick = { navController.navigate(NavRoutes.SETTINGS) },
                        onNotificationClick = { navController.navigate(NavRoutes.SETTINGS) },
                        onSearchClick = { navController.navigate(NavRoutes.SEARCH) }
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
            NavHost(
                navController = navController,
                startDestination = NavRoutes.DASHBOARD,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(tween(250)) + slideInHorizontally(animationSpec = tween(250), initialOffsetX = { 60 }) },
                exitTransition = { fadeOut(tween(200)) + slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { -60 }) },
                popEnterTransition = { fadeIn(tween(250)) + slideInHorizontally(animationSpec = tween(250), initialOffsetX = { -60 }) },
                popExitTransition = { fadeOut(tween(200)) + slideOutHorizontally(animationSpec = tween(200), targetOffsetX = { 60 }) }
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
                        }
                    )
                }

                composable(NavRoutes.DOMAINS) {
                    val domainsViewModel: DomainsViewModel = viewModel(factory = factory)
                    val goalsViewModel: GoalsViewModel = viewModel(factory = factory)
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
                        },
                        onSetDomainGoal = { domainId, title, target, unit, freq ->
                            goalsViewModel.addGoal(
                                userId = currentUser?.id ?: "guest_user",
                                domainId = domainId,
                                title = title,
                                targetValue = target,
                                unit = unit,
                                frequency = freq,
                                startDateIso = java.time.LocalDate.now().toString()
                            )
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
                            entryViewModel.saveEntry(entry)
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

                    StatisticsScreen(
                        strings = strings,
                        uiState = statsUiState,
                        selectedDate = selectedDate,
                        currentMonth = currentMonth,
                        monthDaysCompletion = monthDaysCompletion,
                        selectedDateEntries = selectedDateEntries,
                        allEntries = allEntries,
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
                    val reportHistory by reportsViewModel.reportHistory.collectAsStateWithLifecycle()
                    ReportsScreen(
                        strings = strings,
                        user = currentUser,
                        selectedReportType = selectedReportType,
                        selectedDomains = selectedDomains,
                        targetDate = targetDate,
                        reportHistory = reportHistory,
                        onSelectReportType = { reportsViewModel.selectReportType(it) },
                        onToggleDomainFilter = { reportsViewModel.toggleDomainFilter(it) },
                        onSelectAllDomains = { reportsViewModel.selectAllDomains() },
                        onSetTargetDate = { reportsViewModel.setTargetDate(it) },
                        onGeneratePdfReport = { ctx, onFile ->
                            reportsViewModel.generatePdfReport(ctx, onFile)
                        }
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
                        },
                        onDiscardTimer = { timerViewModel.discardTimer() },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
