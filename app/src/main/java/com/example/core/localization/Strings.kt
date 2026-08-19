package com.example.core.localization

import com.example.data.local.entities.AccountabilityEntryEntity

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    FRENCH("fr", "Français")
}

object LocalizationManager {
    fun getStrings(language: AppLanguage): AppStrings {
        return when (language) {
            AppLanguage.ENGLISH -> EnglishStrings
            AppLanguage.FRENCH -> FrenchStrings
        }
    }
}

interface AppStrings {

    // App Bar & Nav
    val appName: String
    val dashboard: String
    val domains: String
    val calendar: String
    val statistics: String
    val reports: String
    val settings: String
    val profile: String
    val goals: String
    val notifications: String
    val search: String

    // Auth & Guest Mode
    val welcomeTitle: String
    val welcomeSubtitle: String
    val continueAsGuest: String
    val guestExplanation: String
    val signInWithGoogle: String
    val signInWithEmail: String
    val email: String
    val password: String
    val signIn: String
    val signUp: String
    val forgotPassword: String
    val createAccount: String
    val logout: String
    val guestAccountNotice: String
    val migrateDataTitle: String
    val migrateDataDesc: String
    val saveAndContinue: String
    val cancel: String

    // Dashboard
    val greetingMorning: String
    val greetingAfternoon: String
    val greetingEvening: String
    val todaysProgress: String
    val completedDomains: String
    val remainingDomains: String
    val currentStreak: String
    val longestStreak: String
    val days: String
    val goalProgress: String
    val quickAdd: String
    val recentActivities: String
    val upcomingReminders: String
    val noRecentActivities: String
    val noUpcomingReminders: String

    // Domains
    val searchDomains: String
    val spiritualDisciplines: String
    val ddewgTitle: String
    val ddewgDesc: String
    val bibleReadingTitle: String
    val bibleReadingDesc: String
    val prayerAloneTitle: String
    val prayerAloneDesc: String
    val prayerWithOthersTitle: String
    val prayerWithOthersDesc: String
    val fastingTitle: String
    val fastingDesc: String
    val givingTitle: String
    val givingDesc: String
    val accountabilityTitle: String
    val accountabilityDesc: String
    val christianLitTitle: String
    val christianLitDesc: String
    val christianLitMemTitle: String
    val christianLitMemDesc: String
    val bibleMemTitle: String
    val bibleMemDesc: String
    val soulWinningTitle: String
    val soulWinningDesc: String
    val proclamationTitle: String
    val proclamationDesc: String
    val customDomain: String
    val addCustomDomain: String

    // Proclamation & Importunity Feature
    val enterPrayerTopicOrProclamation: String
    val proclamationCounter: String
    val tapToProclaim: String
    val editCounter: String
    val setCounterStartingValue: String
    val currentCount: String
    val targetProclamations: String
    val selectSavedTopic: String
    val createTopic: String
    val proclamationsMade: String
    val topicLabel: String

    // Timer
    val startTimer: String
    val pauseTimer: String
    val resumeTimer: String
    val stopTimer: String
    val cancelTimer: String
    val sessionComplete: String
    val duration: String
    val activeSessionFound: String
    val activeSessionPrompt: String
    val resumeSession: String
    val endSession: String
    val discardSession: String
    val reflectionNotes: String

    // Forms & Fields
    val save: String
    val delete: String
    val edit: String
    val date: String
    val bibleVersion: String
    val bibleBook: String
    val startChapter: String
    val endChapter: String
    val totalChapters: String
    val prayerType: String
    val participantsCount: String
    val participantNames: String
    val fastingType: String
    val completeFast: String
    val partialFast: String
    val startDate: String
    val endDate: String
    val purpose: String
    val amount: String
    val givingType: String
    val tithe: String
    val offering: String
    val missions: String
    val other: String
    val frequency: String
    val areasDiscussed: String
    val bookTitle: String
    val author: String
    val totalPages: String
    val pagesRead: String
    val preachedTo: String
    val converted: String
    val waterBaptized: String
    val holySpiritBaptized: String
    val notes: String

    // Goals
    val spiritualGoals: String
    val addGoal: String
    val goalTitle: String
    val targetValue: String
    val unit: String
    val daily: String
    val weekly: String
    val monthly: String
    val targetReached: String
    val noGoalsYet: String
    val noGoalsFound: String

    // Calendar
    val noActivitiesForDate: String

    // Statistics
    val spiritualAnalytics: String
    val bibleReading: String
    val soulWinning: String
    val totalBibleChapters: String
    val bibleCompletionRate: String
    val biblesReadCount: String
    val totalPrayerTime: String
    val totalSoulWinning: String
    val totalFastingDays: String
    val activityHeatmap: String
    val analyticsOverview: String
    val historyAndCalendar: String
    val totalRecords: String
    val loggedDisciplineActivities: String
    val weeklyActivityTrend: String
    val disciplinesCompletedPerDay: String
    val biblesRead: String
    val completion: String
    val selectedDateLabel: String
    val allPastRecords: String
    val timeSpanLabel: String
    val timesDoneLabel: String
    val weeklyBreakdownTitle: String
    val monthlyBreakdownTitle: String
    val updateProfilePhoto: String

    // Reports
    val accountabilityReports: String
    val generatePdf: String
    val generateReport: String
    val dailyReport: String
    val weeklyReport: String
    val monthlyReport: String
    val selectDomains: String
    val allDomains: String
    val exportPdf: String
    val shareReport: String
    val reportHistory: String
    val generatedHistory: String
    val noReportHistory: String
    val pdfGeneratedSuccess: String

    // Settings
    val appearance: String
    val theme: String
    val themeLight: String
    val themeDark: String
    val themeSystem: String
    val language: String
    val dailyReminders: String
    val signOut: String
    val dataManagement: String
    val cloudBackup: String
    val cloudRestore: String
    val lastSynced: String
    val clearData: String
    val clearDataConfirm: String
    val version: String
    val confirmDelete: String
    val deleteRecordPrompt: String

    // Legal & Support
    val privacyPolicy: String
    val termsConditions: String
    val supportFeedback: String

    // Profile & Settings Extra
    val spiritualJourney: String
    val discipleProfile: String
    val localAssembly: String
    val discipleMakerName: String
    val phoneNumber: String
    val conversionDate: String
    val editProfile: String
    val setDate: String
    val notSet: String
    val noActiveReminders: String
    val addReminderTitle: String
    val reminderTitleLabel: String
    val messageLabel: String
    val hourLabel: String
    val minuteLabel: String

    // Reports Extra
    val reportDesc: String
    val exactDateSelection: String
    val targetDayLabel: String
    val weekEndingLabel: String
    val monthLabel: String
    val dateRangeLabel: String
    val changeDate: String
    val selectDomainsToInclude: String
    val selectAll: String
    val generatePdfButton: String
    val pdfGeneratedTitle: String
    val openPdf: String
    val sharePdf: String
    val shareAccountsTitle: String
    val shareAccountsDesc: String
    val shareSummary: String
    val copyLink: String

    // Daily Word of Encouragement
    val dailyWordTitle: String
    val nextQuote: String
    val dailyQuotes: List<String>

    // Additional Screen Localizations
    val searchResultsFor: String
    val dailyCheckInPrompt: String
    val logAction: String
    val nextAspect: String
    val onFire: String
    val noGoalsSet: String
    val tapToSetGoals: String
    val accountabilityStreaks: String
    val streakDaysWithAccountability: String
    val streakLegend: String
    val activitiesForDate: String
    val editPastRecord: String
    val activityNotesPrompt: String
    val givingAmountLabel: String
    val givingTypePlaceholder: String
    val chaptersReadLabel: String
    val durationMinutesLabel: String
    val saveChanges: String
    val prayerFocus: String
    val topicsCountFormat: String
    val hoursUnit: String
    val minutesUnit: String
    val selectDomainLabel: String
    val targetPeriodLabel: String
    val unitPlaceholder: String
    val saveGoal: String
    val filterAll: String
    val logEmptySessionTitle: String
    val logEmptySessionDesc: String
    val saveAnyway: String
    val startAtZero: String
    val sessionNotesPrompt: String
    val propheticBurdensPrompt: String
    val understood: String
    val noRecordedEntriesForDate: String

    // Domain Detail Screen Strings
    val liveTimerMode: String
    val liveTimerDesc: String
    val startLiveSessionTimer: String
    val logActivityRecord: String
    val dateOfActivity: String
    val prevDay: String
    val nextDay: String
    val timeAndDuration: String
    val startTimePlaceholder: String
    val stopTimePlaceholder: String
    val calculatedDurationFormat: String
    val typeOfPrayerFocus: String
    val prayerTypeThanksgiving: String
    val prayerTypeRequest: String
    val prayerType15MinRetreat: String
    val prayerTypeBertouaMessage: String
    val prayerTypeIntercession: String
    val prayerTypeWorship: String
    val numTopicsRecorded: String
    val inspirationForMeditation: String
    val selectBibleBook: String
    val chapterNumberFormat: String
    val versesPrompt: String
    val timesReadPrompt: String
    val pagesMemorizedPrompt: String
    val typeOfFast: String
    val fastingDurationDays: String
    val givingTypeExtendedPlaceholder: String
    val peoplePreachedTo: String
    val peopleConverted: String
    val saveActivityRecord: String
    val activityRecordedSuccess: String
    val domainNamePrompt: String
    val descriptionPrompt: String
    val measurementUnitPrompt: String
    val haveYouSpentTimeWithGod: String
    val timeWithGodSubtitle: String
    val exploreDisciplines: String
    val editReminderTitle: String


    fun getBibleBookName(englishName: String): String {
        if (this is FrenchStrings) {
            return when (englishName.trim()) {
                "Genesis" -> "Genèse"
                "Exodus" -> "Exode"
                "Leviticus" -> "Lévitique"
                "Numbers" -> "Nombres"
                "Deuteronomy" -> "Deutéronome"
                "Joshua" -> "Josué"
                "Judges" -> "Juges"
                "Ruth" -> "Ruth"
                "1 Samuel" -> "1 Samuel"
                "2 Samuel" -> "2 Samuel"
                "1 Kings" -> "1 Rois"
                "2 Kings" -> "2 Rois"
                "1 Chronicles" -> "1 Chroniques"
                "2 Chronicles" -> "2 Chroniques"
                "Ezra" -> "Esdras"
                "Nehemiah" -> "Néhémie"
                "Esther" -> "Esther"
                "Job" -> "Job"
                "Psalms" -> "Psaumes"
                "Proverbs" -> "Proverbes"
                "Ecclesiastes" -> "Ecclésiaste"
                "Song of Solomon" -> "Cantique des Cantiques"
                "Isaiah" -> "Ésaïe"
                "Jeremiah" -> "Jérémie"
                "Lamentations" -> "Lamentations"
                "Ezekiel" -> "Ézéchiel"
                "Daniel" -> "Daniel"
                "Hosea" -> "Osée"
                "Joel" -> "Joël"
                "Amos" -> "Amos"
                "Obadiah" -> "Abdias"
                "Jonah" -> "Jonas"
                "Micah" -> "Michée"
                "Nahum" -> "Nahum"
                "Habakkuk" -> "Habacuc"
                "Zephaniah" -> "Sophonie"
                "Haggai" -> "Aggée"
                "Zechariah" -> "Zacharie"
                "Malachi" -> "Malachie"
                "Matthew" -> "Matthieu"
                "Mark" -> "Marc"
                "Luke" -> "Luc"
                "John" -> "Jean"
                "Acts" -> "Actes"
                "Romans" -> "Romains"
                "1 Corinthians" -> "1 Corinthiens"
                "2 Corinthians" -> "2 Corinthiens"
                "Galatians" -> "Galates"
                "Ephesians" -> "Éphésiens"
                "Philippians" -> "Philippiens"
                "Colossians" -> "Colossiens"
                "1 Thessalonians" -> "1 Thessaloniciens"
                "2 Thessalonians" -> "2 Thessaloniciens"
                "1 Timothy" -> "1 Timothée"
                "2 Timothy" -> "2 Timothée"
                "Titus" -> "Tite"
                "Philemon" -> "Philémon"
                "Hebrews" -> "Hébreux"
                "James" -> "Jacques"
                "1 Peter" -> "1 Pierre"
                "2 Peter" -> "2 Pierre"
                "1 John" -> "1 Jean"
                "2 John" -> "2 Jean"
                "3 John" -> "3 Jean"
                "Jude" -> "Jude"
                "Revelation" -> "Apocalypse"
                else -> englishName
            }
        }
        return englishName
    }

    fun getPrayerTypeDisplayName(pType: String): String {
        return when (pType) {
            "Thanksgiving" -> prayerTypeThanksgiving
            "Request" -> prayerTypeRequest
            "15-Min Retreat", "15-Minute Retreat" -> prayerType15MinRetreat
            "Bertoua Message" -> prayerTypeBertouaMessage
            "Intercession" -> prayerTypeIntercession
            "Worship" -> prayerTypeWorship
            else -> pType
        }
    }

    fun getFastingTypeDisplayName(fType: String): String {
        return when (fType) {
            "Complete Fast" -> completeFast
            "Partial Fast" -> partialFast
            else -> fType
        }
    }
    fun getDomainTitle(key: String): String {
        return when (key) {
            "ddewgTitle" -> ddewgTitle
            "bibleReadingTitle" -> bibleReadingTitle
            "prayerAloneTitle" -> prayerAloneTitle
            "prayerWithOthersTitle" -> prayerWithOthersTitle
            "proclamationTitle" -> proclamationTitle
            "fastingTitle" -> fastingTitle
            "givingTitle" -> givingTitle
            "accountabilityTitle" -> accountabilityTitle
            "christianLitTitle" -> christianLitTitle
            "christianLitMemTitle" -> christianLitMemTitle
            "bibleMemTitle" -> bibleMemTitle
            "soulWinningTitle" -> soulWinningTitle
            else -> key
        }
    }

    fun getDomainTitleById(id: String): String {
        return when (id.lowercase().trim()) {
            "ddewg" -> ddewgTitle
            "bible_reading" -> bibleReadingTitle
            "prayer_alone" -> prayerAloneTitle
            "prayer_with_others" -> prayerWithOthersTitle
            "fasting" -> fastingTitle
            "giving" -> givingTitle
            "accountability" -> accountabilityTitle
            "christian_lit_reading", "christian_lit" -> christianLitTitle
            "christian_lit_memory", "christian_lit_mem" -> christianLitMemTitle
            "bible_memory", "bible_mem" -> bibleMemTitle
            "soul_winning" -> soulWinningTitle
            "proclamation_importunity", "proclamation" -> proclamationTitle
            else -> id.replace("_", " ").split(" ").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
        }
    }

    fun getDomainDesc(key: String): String {
        return when (key) {
            "ddewgDesc" -> ddewgDesc
            "bibleReadingDesc" -> bibleReadingDesc
            "prayerAloneDesc" -> prayerAloneDesc
            "prayerWithOthersDesc" -> prayerWithOthersDesc
            "proclamationDesc" -> proclamationDesc
            "fastingDesc" -> fastingDesc
            "givingDesc" -> givingDesc
            "accountabilityDesc" -> accountabilityDesc
            "christianLitDesc" -> christianLitDesc
            "christianLitMemDesc" -> christianLitMemDesc
            "bibleMemDesc" -> bibleMemDesc
            "soulWinningDesc" -> soulWinningDesc
            else -> key
        }
    }

    fun formatActivitySummary(entry: AccountabilityEntryEntity): String {
        return when (entry.domainId) {
            "bible_reading" -> "${entry.bibleBook} Ch. ${entry.startChapter}-${entry.endChapter}"
            "ddewg", "prayer_alone", "prayer_with_others" -> {
                val mins = (entry.durationSeconds / 60).coerceAtLeast(1)
                if (mins >= 60) "${mins / 60} $hoursUnit ${mins % 60} $minutesUnit" else "$mins $minutesUnit"
            }
            "proclamation_importunity" -> {
                val topic = entry.proclamationTopic.ifBlank { proclamationTitle }
                val mins = entry.durationSeconds / 60
                "$topic: ${entry.proclamationCount} proclamations ($mins $minutesUnit)"
            }
            "soul_winning" -> {
                "$preachedTo: ${entry.preachedToCount}, $converted: ${entry.convertedCount}"
            }
            "giving" -> {
                val typeStr = if (entry.givingType.isNotBlank()) "${entry.givingType}: " else ""
                "$givingTitle: $typeStr$amount $${entry.givingAmount}"
            }
            "fasting" -> {
                val daysCount = if (entry.fastingDaysCount > 0) entry.fastingDaysCount else 1
                val typeStr = if (entry.fastingType.isNotBlank()) " (${entry.fastingType})" else ""
                "$fastingTitle: $daysCount $days$typeStr"
            }
            else -> entry.notes.ifBlank { loggedDisciplineActivities }
        }
    }

}

object EnglishStrings : AppStrings {
    override val appName = "CMFI Accap"
    override val dashboard = "Home"
    override val domains = "Domains"
    override val calendar = "Calendar"
    override val statistics = "Statistics"
    override val reports = "Reports"
    override val settings = "Settings"
    override val profile = "Profile"
    override val goals = "Goals"
    override val notifications = "Notifications"
    override val search = "Search"

    override val welcomeTitle = "CMFI Accap Book"
    override val welcomeSubtitle = "Digital accountability tool for spiritual disciplines and growth in Christian discipleship."
    override val continueAsGuest = "Continue as Guest"
    override val guestExplanation = "You can record all activities locally without an account. Sign in anytime to sync to the cloud."
    override val signInWithGoogle = "Sign in with Google"
    override val signInWithEmail = "Sign in with Email"
    override val email = "Email Address"
    override val password = "Password"
    override val signIn = "Sign In"
    override val signUp = "Create Account"
    override val forgotPassword = "Forgot Password?"
    override val createAccount = "Create Account"
    override val logout = "Sign Out"
    override val guestAccountNotice = "You are in Guest Mode (Offline local storage)"
    override val migrateDataTitle = "Migrate Local Records"
    override val migrateDataDesc = "Save your local accountability records to your new authenticated account?"
    override val saveAndContinue = "Save & Continue"
    override val cancel = "Cancel"

    override val greetingMorning = "Good Morning"
    override val greetingAfternoon = "Good Afternoon"
    override val greetingEvening = "Good Evening"
    override val todaysProgress = "Today's Progress"
    override val completedDomains = "Completed"
    override val remainingDomains = "Remaining"
    override val currentStreak = "Current Streak"
    override val longestStreak = "Longest Streak"
    override val days = "days"
    override val goalProgress = "Goal Progress"
    override val quickAdd = "Quick Record"
    override val recentActivities = "Recent Activities"
    override val upcomingReminders = "Upcoming Reminders"
    override val noRecentActivities = "No activities recorded today yet."
    override val noUpcomingReminders = "No active reminders scheduled."

    override val searchDomains = "Search spiritual disciplines..."
    override val spiritualDisciplines = "Spiritual Disciplines"
    override val ddewgTitle = "Daily Dynamic Encounter With God"
    override val ddewgDesc = "Morning quiet time, scripture meditation, and fellowship with God."
    override val bibleReadingTitle = "Bible Reading"
    override val bibleReadingDesc = "Systematic reading and study of the Scriptures."
    override val prayerAloneTitle = "Prayer Alone"
    override val prayerAloneDesc = "Personal secret place prayer, thanksgiving, and intercession."
    override val prayerWithOthersTitle = "Prayer With Others"
    override val prayerWithOthersDesc = "Corporate prayer, family altar, cell group, and church prayer."
    override val fastingTitle = "Fasting"
    override val fastingDesc = "Abstaining from food or drink for spiritual devotion."
    override val givingTitle = "Giving to God"
    override val givingDesc = "Tithes, offerings, missions, and freewill giving."
    override val accountabilityTitle = "Disciple Maker Accountability"
    override val accountabilityDesc = "Regular reporting and fellowship with your disciple maker."
    override val christianLitTitle = "Christian Literature Reading"
    override val christianLitDesc = "Reading spiritual edifying Christian books."
    override val christianLitMemTitle = "Christian Literature Memorization"
    override val christianLitMemDesc = "Memorizing key excerpts from Christian literature."
    override val bibleMemTitle = "Bible Memorization"
    override val bibleMemDesc = "Hiding God's word in your heart through scripture memory."
    override val soulWinningTitle = "Soul Winning"
    override val soulWinningDesc = "Evangelism, outreach, conversions, and baptisms."
    override val proclamationTitle = "Proclamation & Importunity"
    override val proclamationDesc = "Persistent proclamation and repeated importunate prayer for specific spiritual topics."
    override val customDomain = "Custom Domain"
    override val addCustomDomain = "Add Custom Domain"

    override val enterPrayerTopicOrProclamation = "Enter prayer topic or proclamation..."
    override val proclamationCounter = "Repetition Counter"
    override val tapToProclaim = "TAP TO PROCLAIM"
    override val editCounter = "Edit Counter"
    override val setCounterStartingValue = "Set Counter Starting Value"
    override val currentCount = "Current Count"
    override val targetProclamations = "Target Repetitions"
    override val selectSavedTopic = "Select Saved Topic"
    override val createTopic = "Add Topic"
    override val proclamationsMade = "Proclamations Made"
    override val topicLabel = "Prayer Topic / Scripture Proclamation"

    override val startTimer = "Start Session"
    override val pauseTimer = "Pause"
    override val resumeTimer = "Resume"
    override val stopTimer = "Stop Session"
    override val cancelTimer = "Cancel"
    override val sessionComplete = "Session Complete"
    override val duration = "Duration"
    override val activeSessionFound = "Active Session Detected"
    override val activeSessionPrompt = "An interrupted timer session was found from earlier."
    override val resumeSession = "Resume"
    override val endSession = "Finish & Save"
    override val discardSession = "Discard"
    override val reflectionNotes = "Notes & Spiritual Reflection"

    override val save = "Save Record"
    override val delete = "Delete"
    override val edit = "Edit"
    override val date = "Date"
    override val bibleVersion = "Bible Version"
    override val bibleBook = "Book of the Bible"
    override val startChapter = "Start Chapter"
    override val endChapter = "End Chapter"
    override val totalChapters = "Total Chapters"
    override val prayerType = "Prayer Category"
    override val participantsCount = "Number of Participants"
    override val participantNames = "Participant Names (Optional)"
    override val fastingType = "Fasting Type"
    override val completeFast = "Complete Fast"
    override val partialFast = "Partial Fast"
    override val startDate = "Start Date"
    override val endDate = "End Date"
    override val purpose = "Fasting Purpose"
    override val amount = "Amount"
    override val givingType = "Giving Type"
    override val tithe = "Tithe"
    override val offering = "Offering"
    override val missions = "Missions"
    override val other = "Other"
    override val frequency = "Frequency"
    override val areasDiscussed = "Areas Discussed"
    override val bookTitle = "Book Title"
    override val author = "Author"
    override val totalPages = "Total Pages"
    override val pagesRead = "Pages Read"
    override val preachedTo = "Preached To"
    override val converted = "Converted"
    override val waterBaptized = "Water Baptized"
    override val holySpiritBaptized = "Holy Spirit Baptized"
    override val notes = "Notes & Insights"

    override val spiritualGoals = "Spiritual Goals"
    override val addGoal = "Add Goal"
    override val goalTitle = "Goal Title"
    override val targetValue = "Target Amount"
    override val unit = "Unit"
    override val daily = "Daily"
    override val weekly = "Weekly"
    override val monthly = "Monthly"
    override val targetReached = "Goal Reached!"
    override val noGoalsYet = "No goals established yet. Create a goal to track consistency."
    override val noGoalsFound = "No goals matching the selected filter."

    override val noActivitiesForDate = "No activities recorded for this date."

    override val spiritualAnalytics = "Spiritual Analytics"
    override val bibleReading = "Bible Reading"
    override val soulWinning = "Soul Winning"
    override val totalBibleChapters = "Total Bible Chapters Read"
    override val bibleCompletionRate = "Bible Completion Rate"
    override val biblesReadCount = "Equivalent Bibles Read"
    override val totalPrayerTime = "Total Prayer Time"
    override val totalSoulWinning = "Soul Winning Impact"
    override val totalFastingDays = "Total Fasting Days"
    override val activityHeatmap = "Consistency Heatmap"
    override val analyticsOverview = "Overview Analytics"
    override val historyAndCalendar = "History & Calendar"
    override val totalRecords = "Total Records"
    override val loggedDisciplineActivities = "Logged Discipline Activities"
    override val weeklyActivityTrend = "Weekly Activity Trend"
    override val disciplinesCompletedPerDay = "Disciplines completed per day"
    override val biblesRead = "Bibles Read"
    override val completion = "Completion"
    override val selectedDateLabel = "Selected Date"
    override val allPastRecords = "All Past Accountability Records"
    override val timeSpanLabel = "Time Span"
    override val timesDoneLabel = "Times Executed"
    override val weeklyBreakdownTitle = "Weekly Activities by Day"
    override val monthlyBreakdownTitle = "Monthly Activities by Week"
    override val updateProfilePhoto = "Update Profile Photo"

    override val accountabilityReports = "Accountability Reports"
    override val generatePdf = "Generate PDF Report"
    override val generateReport = "Generate Report"
    override val dailyReport = "Daily Report"
    override val weeklyReport = "Weekly Report"
    override val monthlyReport = "Monthly Report"
    override val selectDomains = "Filter Domains"
    override val allDomains = "All Domains"
    override val exportPdf = "Export PDF Document"
    override val shareReport = "Share Report"
    override val reportHistory = "Generated Reports History"
    override val generatedHistory = "Generated Reports"
    override val noReportHistory = "No PDF reports generated yet."
    override val pdfGeneratedSuccess = "PDF Report generated and saved successfully."

    override val appearance = "Appearance"
    override val theme = "Theme Mode"
    override val themeLight = "Light Theme"
    override val themeDark = "Dark Theme"
    override val themeSystem = "System Default"
    override val language = "Language"
    override val dailyReminders = "Daily Reminders"
    override val signOut = "Sign Out"
    override val dataManagement = "Data & Sync"
    override val cloudBackup = "Cloud Backup"
    override val cloudRestore = "Cloud Restore"
    override val lastSynced = "Last synced"
    override val clearData = "Clear Local Data"
    override val clearDataConfirm = "Are you sure you want to delete all local records?"
    override val version = "App Version"
    override val confirmDelete = "Confirm Delete"
    override val deleteRecordPrompt = "Are you sure you want to permanently delete this record?"

    override val privacyPolicy = "Privacy Policy"
    override val termsConditions = "Terms & Conditions"
    override val supportFeedback = "Support & Feedback"

    override val spiritualJourney = "Spiritual Journey"
    override val discipleProfile = "Disciple Profile"
    override val localAssembly = "Local Assembly"
    override val discipleMakerName = "Disciple Maker Name"
    override val phoneNumber = "Phone Number"
    override val conversionDate = "Conversion Date (YYYY-MM-DD)"
    override val editProfile = "Edit Disciple Profile"
    override val setDate = "Set Date"
    override val notSet = "Not set"
    override val noActiveReminders = "No active reminders set. Add daily alerts for DDEWG or Prayer."
    override val addReminderTitle = "Add Spiritual Reminder"
    override val reminderTitleLabel = "Reminder Title"
    override val messageLabel = "Message"
    override val hourLabel = "Hour (0-23)"
    override val minuteLabel = "Minute (0-59)"

    override val reportDesc = "Generate a formatted CMFI PDF report summarizing your spiritual disciplines for your Disciple Maker."
    override val exactDateSelection = "Exact Date / Date Range Selection:"
    override val targetDayLabel = "Target Day"
    override val weekEndingLabel = "Week Ending"
    override val monthLabel = "Month"
    override val dateRangeLabel = "Range"
    override val changeDate = "Change Date"
    override val selectDomainsToInclude = "Select Exact Domains to Include:"
    override val selectAll = "Select All"
    override val generatePdfButton = "Generate %s PDF Report"
    override val pdfGeneratedTitle = "PDF Generated!"
    override val openPdf = "Open PDF"
    override val sharePdf = "Share PDF File"
    override val shareAccountsTitle = "Share Accounts to Social Media & Messaging"
    override val shareAccountsDesc = "Send summary links or reports directly to your Disciple Maker, WhatsApp, or Social Platforms:"
    override val shareSummary = "Share Summary"
    override val copyLink = "Copy Link"

    override val dailyWordTitle = "Daily Word of Encouragement (3B Messages)"
    override val nextQuote = "Next Quote"
    override val dailyQuotes = listOf(
        "“Return to your first love for the Lord Jesus Christ. This return includes freedom from all sin, freedom from love of self, freedom from love of the world, greed, laziness, and goal-lessness.” — The Bertoua Message (Z.T. Fomum)",
        "“Faithfulness in becoming and being a disciple includes praying alone, daily dynamic encounters with God (DDEWG), Bible reading, reading Christian literature, retreats, fasting, and soul-winning.” — The Pathway to Revival (Z.T. Fomum)",
        "“Son, write on your heart that what a person IS before God is of far greater importance than what he DOES. Seek to be filled with the Holy Spirit in all fullness.” — The Beijing Prophecy (Z.T. Fomum)",
        "“The power released in prayer and fasting depends primarily on WHO is praying and fasting, and secondarily on the duration.” — The Beijing Prophecy (Z.T. Fomum)",
        "“Live your life for the exclusive glory of the Lord Jesus Christ in all things, serving Him in the domain of His call upon your life.” — The Congo Brazzaville Message (Z.T. Fomum)",
        "“During Daily Dynamic Encounters with God (DDEWG), read God's Word, meditate on it, listen to His voice, record what He speaks, and pray it through.” — Pr. Zacharias Tanee Fomum",
        "“When a Spirit-filled believer prays and fasts in total surrender, heaven moves and hell is brought to naught for the glory of Christ!” — 3B Prophetic Messages (Z.T. Fomum)"
    )

    override val searchResultsFor = "Search Results for “%s”"
    override val dailyCheckInPrompt = "Daily Check-In: Have you done %s today?"
    override val logAction = "Log %s"
    override val nextAspect = "Next Aspect"
    override val onFire = "ON FIRE!"
    override val noGoalsSet = "No Goals Set"
    override val tapToSetGoals = "Tap to set goals →"
    override val accountabilityStreaks = "Accountability Streaks"
    override val streakDaysWithAccountability = "%d days with logged accountability this month"
    override val streakLegend = "Streak / Completed Day (Yellow Fire)"
    override val activitiesForDate = "Activities for %s"
    override val editPastRecord = "Edit Past Discipline Record"
    override val activityNotesPrompt = "Activity Notes / Reflection"
    override val givingAmountLabel = "Giving Amount ($)"
    override val givingTypePlaceholder = "Giving Type (Tithe, Offering...)"
    override val chaptersReadLabel = "Chapters Read / Count"
    override val durationMinutesLabel = "Duration (Minutes)"
    override val saveChanges = "Save Changes"
    override val prayerFocus = "Prayer Focus"
    override val topicsCountFormat = "%d Topics"
    override val hoursUnit = "hrs"
    override val minutesUnit = "mins"
    override val selectDomainLabel = "Select Domain:"
    override val targetPeriodLabel = "Target Period:"
    override val unitPlaceholder = "Unit (Minutes, Chapters, Souls, USD)"
    override val saveGoal = "Save Goal"
    override val filterAll = "All"
    override val logEmptySessionTitle = "Log Empty Session?"
    override val logEmptySessionDesc = "You currently have 0 repetitions and 0 minutes recorded. Would you still like to log this session?"
    override val saveAnyway = "Save Anyway"
    override val startAtZero = "Start at 0"
    override val sessionNotesPrompt = "Session Notes (e.g. Specific breakthrough, scriptures)"
    override val propheticBurdensPrompt = "Prophetic Burdens / Divine Impressions"
    override val understood = "Understood"
    override val noRecordedEntriesForDate = "No recorded discipline entries for this date. Pick another date or tap on any past entry below to edit."

    // Domain Detail Screen Strings
    override val liveTimerMode = "Live Timer Mode"
    override val liveTimerDesc = "Track your session in real time with precise duration logging."
    override val startLiveSessionTimer = "Start Live Session Timer"
    override val logActivityRecord = "Log Activity Record"
    override val dateOfActivity = "Date of Activity:"
    override val prevDay = "Prev Day"
    override val nextDay = "Next Day"
    override val timeAndDuration = "Time & Duration:"
    override val startTimePlaceholder = "Start Time (e.g. 06:00)"
    override val stopTimePlaceholder = "Stop Time (e.g. 07:15)"
    override val calculatedDurationFormat = "Calculated Duration: %d Minutes"
    override val typeOfPrayerFocus = "Type of Prayer / Focus:"
    override val prayerTypeThanksgiving = "Thanksgiving"
    override val prayerTypeRequest = "Request"
    override val prayerType15MinRetreat = "15-Min Retreat"
    override val prayerTypeBertouaMessage = "Bertoua Message"
    override val prayerTypeIntercession = "Intercession"
    override val prayerTypeWorship = "Worship"
    override val numTopicsRecorded = "Number of Topics Recorded"
    override val inspirationForMeditation = "Inspiration for Meditation"
    override val selectBibleBook = "Select Bible Book"
    override val chapterNumberFormat = "Chapter %d"
    override val versesPrompt = "Verses (e.g. 1-12)"
    override val timesReadPrompt = "Times Read"
    override val pagesMemorizedPrompt = "Pages Memorized"
    override val typeOfFast = "Type of Fast:"
    override val fastingDurationDays = "Fasting Duration (Days)"
    override val givingTypeExtendedPlaceholder = "Type (Tithe, Offering, Missions, Firstfruit)"
    override val peoplePreachedTo = "People Preached To"
    override val peopleConverted = "People Converted"
    override val saveActivityRecord = "Save Activity Record"
    override val activityRecordedSuccess = "Activity recorded successfully!"
    override val domainNamePrompt = "Domain Name"
    override val descriptionPrompt = "Description"
    override val measurementUnitPrompt = "Measurement Unit (e.g., Pages, Minutes)"
    override val haveYouSpentTimeWithGod = "Have you spent time with God today?"
    override val timeWithGodSubtitle = "Reflect on your spiritual walk and record your devotional activities for today."
    override val exploreDisciplines = "Record Disciplines"
    override val editReminderTitle = "Edit Daily Reminder"
}

object FrenchStrings : AppStrings {
    override val appName = "CMFI Accap"
    override val dashboard = "Accueil"
    override val domains = "Domaines"
    override val calendar = "Calendrier"
    override val statistics = "Statistiques"
    override val reports = "Rapports"
    override val settings = "Paramètres"
    override val profile = "Profil"
    override val goals = "Objectifs"
    override val notifications = "Rappels"
    override val search = "Recherche"

    override val welcomeTitle = "Cahier de Redevabilité CMFI"
    override val welcomeSubtitle = "Outil numérique de redevabilité pour les disciplines spirituelles et la croissance dans le discipulat chrétien."
    override val continueAsGuest = "Continuer en tant qu'invité"
    override val guestExplanation = "Vous pouvez enregistrer toutes vos activités localement sans compte. Connectez-vous à tout moment pour synchroniser."
    override val signInWithGoogle = "Se connecter avec Google"
    override val signInWithEmail = "Se connecter par Email"
    override val email = "Adresse Email"
    override val password = "Mot de passe"
    override val signIn = "Se connecter"
    override val signUp = "Créer un compte"
    override val forgotPassword = "Mot de passe oublié ?"
    override val createAccount = "Créer un compte"
    override val logout = "Se déconnecter"
    override val guestAccountNotice = "Mode Invité (Stockage local sur l'appareil)"
    override val migrateDataTitle = "Migrer les données locales"
    override val migrateDataDesc = "Enregistrer vos enregistrements locaux dans votre nouveau compte authentifié ?"
    override val saveAndContinue = "Enregistrer et Continuer"
    override val cancel = "Annuler"

    override val greetingMorning = "Bonjour"
    override val greetingAfternoon = "Bon après-midi"
    override val greetingEvening = "Bonsoir"
    override val todaysProgress = "Progrès du Jour"
    override val completedDomains = "Complétés"
    override val remainingDomains = "Restants"
    override val currentStreak = "Série Actuelle"
    override val longestStreak = "Meilleure Série"
    override val days = "jours"
    override val goalProgress = "Progrès des Objectifs"
    override val quickAdd = "Ajout Rapide"
    override val recentActivities = "Activités Récentes"
    override val upcomingReminders = "Prochains Rappels"
    override val noRecentActivities = "Aucune activité enregistrée aujourd'hui."
    override val noUpcomingReminders = "Aucun rappel programmé."

    override val searchDomains = "Rechercher des disciplines spirituelles..."
    override val spiritualDisciplines = "Disciplines Spirituelles"
    override val ddewgTitle = "Rencontre Dynamique Quotidienne avec Dieu"
    override val ddewgDesc = "Temps calme du matin, méditation des Écritures et communion avec Dieu."
    override val bibleReadingTitle = "Lecture Biblique"
    override val bibleReadingDesc = "Lecture systématique et étude des Saintes Écritures."
    override val prayerAloneTitle = "Prière Seul"
    override val prayerAloneDesc = "Prière personnelle dans le lieu secret, actions de grâces et intercession."
    override val prayerWithOthersTitle = "Prière Avec les Autres"
    override val prayerWithOthersDesc = "Prière corporative, autel familial, cellule de prière et prière d'église."
    override val fastingTitle = "Jeûne"
    override val fastingDesc = "Abstinence de nourriture ou de boisson pour la dévotion spirituelle."
    override val givingTitle = "Offrandes et Libéralités"
    override val givingDesc = "Dîmes, offrandes, missions et dons volontaires."
    override val accountabilityTitle = "Redevabilité au Faiseur de Disciples"
    override val accountabilityDesc = "Rapports réguliers et communion avec votre faiseur de disciples."
    override val christianLitTitle = "Lecture de la Littérature Chrétienne"
    override val christianLitDesc = "Lecture de livres chrétiens édifiants."
    override val christianLitMemTitle = "Mémorisation de la Littérature Chrétienne"
    override val christianLitMemDesc = "Mémorisation d'extraits clés de la littérature chrétienne."
    override val bibleMemTitle = "Mémorisation Biblique"
    override val bibleMemDesc = "Garder la parole de Dieu dans son cœur par la mémorisation."
    override val soulWinningTitle = "Gagnagisme d'Âmes"
    override val soulWinningDesc = "Évangélisation, témoignage, conversions et baptêmes."
    override val proclamationTitle = "Proclamation & Importunité"
    override val proclamationDesc = "Proclamation persistante et prière importune répétée pour des sujets de prière spécifiques."
    override val customDomain = "Domaine Personnalisé"
    override val addCustomDomain = "Ajouter un Domaine Personnalisé"

    override val enterPrayerTopicOrProclamation = "Entrez le sujet de prière ou la proclamation..."
    override val proclamationCounter = "Compteur de Répétitions"
    override val tapToProclaim = "APPUYER POUR PROCLAMER"
    override val editCounter = "Modifier le Compteur"
    override val setCounterStartingValue = "Définir la Valeur Initiale du Compteur"
    override val currentCount = "Nombre Actuel"
    override val targetProclamations = "Répétitions Cibles"
    override val selectSavedTopic = "Sélectionner un Sujet Enregistré"
    override val createTopic = "Ajouter un Sujet"
    override val proclamationsMade = "Proclamations Réalisées"
    override val topicLabel = "Sujet de Prière / Proclamation des Écritures"

    override val startTimer = "Démarrer la Session"
    override val pauseTimer = "Pause"
    override val resumeTimer = "Reprendre"
    override val stopTimer = "Arrêter la Session"
    override val cancelTimer = "Annuler"
    override val sessionComplete = "Session Terminée"
    override val duration = "Durée"
    override val activeSessionFound = "Session Active Détectée"
    override val activeSessionPrompt = "Une session de minuteur interrompue a été trouvée."
    override val resumeSession = "Reprendre"
    override val endSession = "Terminer et Enregistrer"
    override val discardSession = "Ignorer"
    override val reflectionNotes = "Notes & Réflexion Spirituelle"

    override val save = "Enregistrer"
    override val delete = "Supprimer"
    override val edit = "Modifier"
    override val date = "Date"
    override val bibleVersion = "Version de la Bible"
    override val bibleBook = "Livre de la Bible"
    override val startChapter = "Chapitre de Départ"
    override val endChapter = "Chapitre de Fin"
    override val totalChapters = "Total de Chapitres"
    override val prayerType = "Catégorie de Prière"
    override val participantsCount = "Nombre de Participants"
    override val participantNames = "Noms des Participants (Optionnel)"
    override val fastingType = "Type de Jeûne"
    override val completeFast = "Jeûne Complet"
    override val partialFast = "Jeûne Partiel"
    override val startDate = "Date de Début"
    override val endDate = "Date de Fin"
    override val purpose = "Objectif du Jeûne"
    override val amount = "Montant"
    override val givingType = "Type de Don"
    override val tithe = "Dîme"
    override val offering = "Offrande"
    override val missions = "Missions"
    override val other = "Autre"
    override val frequency = "Fréquence"
    override val areasDiscussed = "Sujets Abordés"
    override val bookTitle = "Titre du Livre"
    override val author = "Auteur"
    override val totalPages = "Total de Pages"
    override val pagesRead = "Pages Lues"
    override val preachedTo = "Personnes Évangélisées"
    override val converted = "Convertis"
    override val waterBaptized = "Baptisés d'Eau"
    override val holySpiritBaptized = "Baptisés du Saint-Esprit"
    override val notes = "Notes & Enseignements"

    override val spiritualGoals = "Objectifs Spirituels"
    override val addGoal = "Ajouter un Objectif"
    override val goalTitle = "Titre de l'Objectif"
    override val targetValue = "Valeur Cible"
    override val unit = "Unité"
    override val daily = "Quotidien"
    override val weekly = "Hebdomadaire"
    override val monthly = "Mensuel"
    override val targetReached = "Objectif Atteint !"
    override val noGoalsYet = "Aucun objectif défini. Créez un objectif pour suivre votre régularité."
    override val noGoalsFound = "Aucun objectif ne correspond au filtre."

    override val noActivitiesForDate = "Aucune activité enregistrée pour cette date."

    override val spiritualAnalytics = "Analytique Spirituelle"
    override val bibleReading = "Lecture Biblique"
    override val soulWinning = "Gagnagisme d'Âmes"
    override val totalBibleChapters = "Total Chapitres Bibliques Lus"
    override val bibleCompletionRate = "Taux de Complétion Biblique"
    override val biblesReadCount = "Bibles Lues Équivalentes"
    override val totalPrayerTime = "Temps Total de Prière"
    override val totalSoulWinning = "Impact du Gagnagisme d'Âmes"
    override val totalFastingDays = "Jours Totaux de Jeûne"
    override val activityHeatmap = "Aperçu de la Régularité"
    override val analyticsOverview = "Vue d'Ensemble Analytique"
    override val historyAndCalendar = "Historique & Calendrier"
    override val totalRecords = "Total des Enregistrements"
    override val loggedDisciplineActivities = "Activités de Discipline Enregistrées"
    override val weeklyActivityTrend = "Tendance d'Activité Hebdomadaire"
    override val disciplinesCompletedPerDay = "Disciplines accomplies par jour"
    override val biblesRead = "Bibles Lues"
    override val completion = "Complétion"
    override val selectedDateLabel = "Date Sélectionnée"
    override val allPastRecords = "Tous les Enregistrements de Redevabilité Passés"
    override val timeSpanLabel = "Plage Horaire"
    override val timesDoneLabel = "Nombre de Fois Effectué"
    override val weeklyBreakdownTitle = "Activités Hebdomadaires par Jour"
    override val monthlyBreakdownTitle = "Activités Mensuelles par Semaine"
    override val updateProfilePhoto = "Mettre à jour la Photo de Profil"

    override val accountabilityReports = "Rapports de Redevabilité"
    override val generatePdf = "Générer Rapport PDF"
    override val generateReport = "Générer le Rapport"
    override val dailyReport = "Rapport Journalier"
    override val weeklyReport = "Rapport Hebdomadaire"
    override val monthlyReport = "Rapport Mensuel"
    override val selectDomains = "Filtrer les Domaines"
    override val allDomains = "Tous les Domaines"
    override val exportPdf = "Exporter en Document PDF"
    override val shareReport = "Partager le Rapport"
    override val reportHistory = "Historique des Rapports"
    override val generatedHistory = "Rapports Générés"
    override val noReportHistory = "Aucun rapport PDF généré."
    override val pdfGeneratedSuccess = "Rapport PDF généré et enregistré avec succès."

    override val appearance = "Apparence"
    override val theme = "Mode de Thème"
    override val themeLight = "Thème Clair"
    override val themeDark = "Thème Sombre"
    override val themeSystem = "Thème du Système"
    override val language = "Langue"
    override val dailyReminders = "Rappels Quotidiens"
    override val signOut = "Se Déconnecter"
    override val dataManagement = "Données & Synchronisation"
    override val cloudBackup = "Sauvegarde Nuage"
    override val cloudRestore = "Restauration Nuage"
    override val lastSynced = "Dernière synchro"
    override val clearData = "Effacer les Données Locales"
    override val clearDataConfirm = "Êtes-vous sûr de vouloir supprimer tous les enregistrements locaux ?"
    override val version = "App Version"
    override val confirmDelete = "Confirmer la Suppression"
    override val deleteRecordPrompt = "Êtes-vous sûr de vouloir supprimer définitivement cet enregistrement ?"

    override val privacyPolicy = "Politique de Confidentialité"
    override val termsConditions = "Conditions Générales"
    override val supportFeedback = "Support & Commentaires"

    override val spiritualJourney = "Parcours Spirituel"
    override val discipleProfile = "Profil du Disciple"
    override val localAssembly = "Assemblée Locale"
    override val discipleMakerName = "Nom du Faiseur de Disciples"
    override val phoneNumber = "Numéro de Téléphone"
    override val conversionDate = "Date de Conversion (AAAA-MM-JJ)"
    override val editProfile = "Modifier le Profil du Disciple"
    override val setDate = "Définir la Date"
    override val notSet = "Non défini"
    override val noActiveReminders = "Aucun rappel actif. Ajoutez des alertes quotidiennes pour la RDQD ou la Prière."
    override val addReminderTitle = "Ajouter un Rappel Spirituel"
    override val reminderTitleLabel = "Titre du Rappel"
    override val messageLabel = "Message"
    override val hourLabel = "Heure (0-23)"
    override val minuteLabel = "Minute (0-59)"

    override val reportDesc = "Générez un rapport PDF formaté CMFI résumant vos disciplines spirituelles pour votre faiseur de disciples."
    override val exactDateSelection = "Sélection de Date / Plage de Dates :"
    override val targetDayLabel = "Jour Cible"
    override val weekEndingLabel = "Fin de Semaine"
    override val monthLabel = "Mois"
    override val dateRangeLabel = "Période"
    override val changeDate = "Changer la Date"
    override val selectDomainsToInclude = "Sélectionnez les Domaines à Inclure :"
    override val selectAll = "Tout Sélectionner"
    override val generatePdfButton = "Générer Rapport PDF %s"
    override val pdfGeneratedTitle = "PDF Généré !"
    override val openPdf = "Ouvrir le PDF"
    override val sharePdf = "Partager le Fichier PDF"
    override val shareAccountsTitle = "Partager les Comptes sur les Réseaux Sociaux & Messageries"
    override val shareAccountsDesc = "Envoyez des résumés ou rapports directement à votre faiseur de disciples, WhatsApp, ou réseaux sociaux :"
    override val shareSummary = "Partager le Résumé"
    override val copyLink = "Copier le Lien"

    override val dailyWordTitle = "Parole d'Encouragement Quotidienne (Messages 3B)"
    override val nextQuote = "Citation Suivante"
    override val dailyQuotes = listOf(
        "« Retourne à ton premier amour pour le Seigneur Jésus-Christ. Ce retour inclut la libération de tout péché, de l'amour de soi, de l'amour du monde, de la cupidité, de la paresse et du manque de buts. » — Le Message de Bertoua (Z.T. Fomum)",
        "« La fidélité à devenir et rester un disciple inclut la prière seul, la rencontre dynamique quotidienne avec Dieu (RDQD), la lecture biblique, la littérature chrétienne, les retraites, le jeûne et le gagnagisme d'âmes. » — Le Chemin de la Revivification (Z.T. Fomum)",
        "« Mon fils, écris sur ton cœur que ce qu'une personne EST devant Dieu est de bien plus grande importance que ce qu'elle FAIT. Cherche à être rempli du Saint-Esprit en toute plénitude. » — La Prophétie de Pékin (Z.T. Fomum)",
        "« La puissance libérée dans la prière et le jeûne dépend principalement de QUI prie et jeûne, et secondairement de la durée. » — La Prophétie de Pékin (Z.T. Fomum)",
        "« Vis ta vie pour la gloire exclusive du Seigneur Jésus-Christ en toutes choses, en Le servant dans le domaine de Son appel sur ta vie. » — Le Message de Congo Brazzaville (Z.T. Fomum)",
        "« Pendant la Rencontre Dynamique Quotidienne avec Dieu (RDQD), lis la Parole de Dieu, médite-la, écoute Sa voix, note ce Qu'Il dit, et prie en conséquence. » — Pr. Zacharias Tanee Fomum",
        "« Quand un croyant rempli de l'Esprit prie et jeûne dans un abandon total, le ciel se meut et l'enfer est réduit à néant pour la gloire du Christ ! » — Messages Prophétiques 3B (Z.T. Fomum)"
    )

    override val searchResultsFor = "Résultats de recherche pour « %s »"
    override val dailyCheckInPrompt = "Point Quotidien : Avez-vous fait %s aujourd'hui ?"
    override val logAction = "Enregistrer %s"
    override val nextAspect = "Aspect Suivant"
    override val onFire = "EN FEU !"
    override val noGoalsSet = "Aucun Objectif Défini"
    override val tapToSetGoals = "Appuyez pour définir des objectifs →"
    override val accountabilityStreaks = "Séries de Redevabilité"
    override val streakDaysWithAccountability = "%d jours avec redevabilité enregistrée ce mois-ci"
    override val streakLegend = "Série / Jour Complété (Feu Doré)"
    override val activitiesForDate = "Activités pour %s"
    override val editPastRecord = "Modifier l'Enregistrement de Discipline"
    override val activityNotesPrompt = "Notes d'Activité / Réflexion"
    override val givingAmountLabel = "Montant du Don ($)"
    override val givingTypePlaceholder = "Type de Don (Dîme, Offrande...)"
    override val chaptersReadLabel = "Chapitres Lus / Nombre"
    override val durationMinutesLabel = "Durée (Minutes)"
    override val saveChanges = "Enregistrer les Modifications"
    override val prayerFocus = "Sujet de Prière"
    override val topicsCountFormat = "%d Sujets"
    override val hoursUnit = "h"
    override val minutesUnit = "min"
    override val selectDomainLabel = "Sélectionner le Domaine :"
    override val targetPeriodLabel = "Période Cible :"
    override val unitPlaceholder = "Unité (Minutes, Chapitres, Âmes, USD)"
    override val saveGoal = "Enregistrer l'Objectif"
    override val filterAll = "Tous"
    override val logEmptySessionTitle = "Enregistrer une Session Vide ?"
    override val logEmptySessionDesc = "Vous avez actuellement 0 répétitions et 0 minutes enregistrées. Voulez-vous tout de même enregistrer cette session ?"
    override val saveAnyway = "Enregistrer quand même"
    override val startAtZero = "Départ à 0"
    override val sessionNotesPrompt = "Notes de Session (ex. Victoire spécifique, écritures)"
    override val propheticBurdensPrompt = "Fardeaux Prophétiques / Impressions Divines"
    override val understood = "Compris"
    override val noRecordedEntriesForDate = "Aucune discipline enregistrée pour cette date. Choisissez une autre date ou appuyez sur un enregistrement ci-dessous pour le modifier."

    // Domain Detail Screen Strings
    override val liveTimerMode = "Mode Minuteur en Direct"
    override val liveTimerDesc = "Suivez votre session en temps réel avec un enregistrement précis de la durée."
    override val startLiveSessionTimer = "Démarrer le Minuteur de Session"
    override val logActivityRecord = "Enregistrer une Discipline"
    override val dateOfActivity = "Date de l'Activité :"
    override val prevDay = "Jour Préc."
    override val nextDay = "Jour Suiv."
    override val timeAndDuration = "Heure & Durée :"
    override val startTimePlaceholder = "Heure de Début (ex. 06:00)"
    override val stopTimePlaceholder = "Heure de Fin (ex. 07:15)"
    override val calculatedDurationFormat = "Durée Calculée : %d Minutes"
    override val typeOfPrayerFocus = "Type de Prière / Sujet :"
    override val prayerTypeThanksgiving = "Action de grâce"
    override val prayerTypeRequest = "Requête"
    override val prayerType15MinRetreat = "Retraite 15-Min"
    override val prayerTypeBertouaMessage = "Message de Bertoua"
    override val prayerTypeIntercession = "Intercession"
    override val prayerTypeWorship = "Adoration"
    override val numTopicsRecorded = "Nombre de Sujets Enregistrés"
    override val inspirationForMeditation = "Inspiration pour la Méditation"
    override val selectBibleBook = "Sélectionner le Livre Biblique"
    override val chapterNumberFormat = "Chapitre %d"
    override val versesPrompt = "Versets (ex. 1-12)"
    override val timesReadPrompt = "Fois Lu"
    override val pagesMemorizedPrompt = "Pages Mémorisées"
    override val typeOfFast = "Type de Jeûne :"
    override val fastingDurationDays = "Durée du Jeûne (Jours)"
    override val givingTypeExtendedPlaceholder = "Type (Dîme, Offrande, Missions, Prémices)"
    override val peoplePreachedTo = "Personnes Évangélisées"
    override val peopleConverted = "Personnes Converties"
    override val saveActivityRecord = "Enregistrer la Discipline"
    override val activityRecordedSuccess = "Activité enregistrée avec succès !"
    override val domainNamePrompt = "Nom du Domaine"
    override val descriptionPrompt = "Description"
    override val measurementUnitPrompt = "Unité de Mesure (ex. Pages, Minutes)"
    override val haveYouSpentTimeWithGod = "Avez-vous passé du temps avec Dieu aujourd'hui ?"
    override val timeWithGodSubtitle = "Méditez sur votre marche spirituelle et enregistrez vos activités de dévotion aujourd'hui."
    override val exploreDisciplines = "Enregistrer les disciplines"
    override val editReminderTitle = "Modifier le rappel quotidien"
}
