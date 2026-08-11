# CMFI Spiritual Accountability Management System

An enterprise-grade, offline-first Android application developed in Kotlin and Jetpack Compose for members and leadership of Christian Missionary Fellowship International (CMFI). The system provides structured recording, analytics, goal monitoring, and PDF export capabilities for daily spiritual disciplines and disciple accountability tracking.

---

## 1. System Overview

The CMFI Spiritual Accountability Management System digitizes personal and organizational accountability for Christian discipleship. The application tracks eleven standardized spiritual discipline domains (including Daily Dynamic Encounters with God, Bible Reading, Prayer Alone/Together, Fasting, Soul Winning, Christian Literature, Bible Memorization, and Financial Giving), computes continuous habit streaks, calculates target goal completion percentages, and generates multi-page PDF progress reports for submission to Disciple Makers.

---

## 2. Core Functional Specifications

### 2.1 Dashboard and Telemetry Module
* Real-time calculation of daily completion percentage across active spiritual domains.
* Continuous streak engine computing current and longest consecutive active days.
* Interactive live session timer with start, pause, resume, and persistence capabilities.
* Sliding daily check-in prompt targeting incomplete disciplines sequentially.
* Rotating daily spiritual encouragement quotes selected deterministically by day of year.

### 2.2 Domain Accountability Module
* Granular logging for specialized domains:
  * Bible Reading: Book selector, chapter range validation, and automatically calculated chapter metrics against complete Old/New Testament datasets.
  * Prayer: Type classification (Thanksgiving, Request, 15-Minute Retreat, Bertoua Message, Intercession, Worship), start/stop timestamp recording, auto-calculated duration, and topic counts.
  * Fasting: Duration tracking in days and classification (Complete vs. Partial).
  * Soul Winning: Multi-field counters for individuals preached to, conversions, water baptisms, and Holy Spirit baptisms.
  * Giving and Stewardship: Financial giving, earned income references, auto-computed percentage of income, and classification (Tithe, Offering, Missions, Firstfruits).
  * Christian Literature and Memorization: Title, author, page count, reading iteration, and memorized page counters.

### 2.3 Analytics, Calendar, and Historical Audit
* Interactive month grid calendar displaying activity density and historical record presence per date cell.
* Bi-directional date navigation with custom date selector for exact historical or future entry logging.
* Complete historical ledger allowing inspection, modification, and deletion of past accountability entries.
* Categorized weekly activity trends and progress charts.

### 2.4 Goal Comparison Engine
* Independent goal management screen detached from domain setup views.
* Dynamic calculation comparing current logged progress against target metrics across Daily, Weekly, and Monthly intervals.
* Visual status classification: Achieved, On Track, or Needs Focus, with exact numerical delta calculation.

### 2.5 PDF Reporting Engine
* On-device compilation using Android `PdfDocument` graphics canvas.
* Granular report period filtering (Daily, Weekly, Monthly) with date range pickers.
* Domain-level checkable inclusions enabling customizable export scope.
* Secure multi-page layout rendering header metadata, summary telemetry, itemized record logs, and signature lines.
* Native `FileProvider` URI generation for secure sharing via Android Intent filters.

### 2.6 Localized System Architecture
* Comprehensive dual-language support (English and French) handled via typed localization keys.
* System theme support utilizing Material Design 3 color tokens with custom primary blue branding (`#1E3A8A`) and crisp dark top bar styling.

---

## 3. Technical Architecture and Data Schema

### 3.1 Stack Component Matrix
* Programming Language: Kotlin 1.9 / 2.0
* UI Framework: Jetpack Compose with Material Design 3
* State Management: Jetpack ViewModel, Coroutines, StateFlow, and `collectAsStateWithLifecycle`
* Database Layer: Room Database 2.6 with KSP (Kotlin Symbol Processing)
* Navigation: Jetpack Navigation Compose with serializable route objects
* Document Engine: Native Android `PdfDocument` and Canvas rendering APIs

### 3.2 Data Persistence Schema

#### AccountabilityEntryEntity (`accountability_entries`)
| Field Name | Type | Description |
| :--- | :--- | :--- |
| `id` | String (PK) | UUID primary key |
| `userId` | String | Disciple identifier |
| `domainId` | String | Domain key (e.g., `bible_reading`, `prayer_alone`) |
| `dateIso` | String | Activity date in `YYYY-MM-DD` format |
| `timestampMs` | Long | Epoch timestamp of creation |
| `timezoneId` | String | Local system timezone ID |
| `durationSeconds` | Long | Measured duration in seconds |
| `startTimeIso` | String | Activity start timestamp (HH:MM) |
| `endTimeIso` | String | Activity stop timestamp (HH:MM) |
| `prayerType` | String | Classification of prayer session |
| `prayerTopicsCount` | Int | Recorded topics count |
| `bibleBook` | String | Bible book title |
| `startChapter` | Int | Beginning chapter number |
| `endChapter` | Int | Ending chapter number |
| `chaptersCount` | Int | Calculated chapters count |
| `bookTitle` | String | Christian literature title |
| `bookAuthor` | String | Author name |
| `pagesRead` | Int | Number of pages read |
| `givingAmount` | Double | Monetry amount given |
| `givingPercentage` | Double | Computed percentage of income |
| `notes` | String | Extended activity notes |

#### AccountabilityGoalEntity (`accountability_goals`)
| Field Name | Type | Description |
| :--- | :--- | :--- |
| `id` | String (PK) | UUID primary key |
| `userId` | String | Disciple identifier |
| `domainId` | String | Domain target identifier |
| `targetFrequency` | String | Frequency constraint (`DAILY`, `WEEKLY`, `MONTHLY`) |
| `targetValue` | Double | Quantitative goal benchmark |
| `targetUnit` | String | Measurement unit (`MINUTES`, `CHAPTERS`, `PAGES`, `DAYS`, `USD`) |
| `reminderTime` | String | Notification time string (HH:MM) |
| `reminderEnabled` | Boolean | Notification toggle flag |

---

## 4. Directory Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                       # Entry Activity & System WindowInsets host
├── core/
│   ├── localization/
│   │   ├── AppStrings.kt                 # String translations contract
│   │   ├── EnStrings.kt                 # English translation implementation
│   │   └── FrStrings.kt                 # French translation implementation
│   └── theme/
│       ├── Color.kt                     # Primary branding, status, & surface colors
│       ├── Type.kt                      # Typography configurations
│       └── Theme.kt                     # MaterialTheme wrapper
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt               # Room database definition
│   │   ├── BibleMetadata.kt             # Canonical Bible structure metadata
│   │   ├── dao/
│   │   │   ├── AccountabilityDao.kt     # Entry & Goal SQL query mappings
│   │   │   └── UserProfileDao.kt        # User profile repository queries
│   │   └── entities/
│   │       ├── AccountabilityEntryEntity.kt
│   │       ├── AccountabilityGoalEntity.kt
│   │       └── UserProfileEntity.kt
│   └── repositories/
│       └── AccountabilityRepository.kt  # Centralized repository interface
├── domain/
│   └── models/
│       └── PredefinedDomains.kt          # Standard domain metadata
├── services/
│   ├── notifications/
│   │   ├── ReminderManager.kt           # AlarmManager scheduling engine
│   │   └── ReminderNotificationReceiver.kt # BroadcastReceiver for user alarms
│   ├── reports/
│   │   └── PdfReportGenerator.kt        # Canvas PDF renderer & file exporter
│   └── timer/
│       └── ActiveTimerManager.kt        # StateFlow timer session manager
└── ui/
    ├── components/
    │   ├── CmfiTopBar.kt                # Solid blue top app bar
    │   └── CommonUi.kt                  # Shared UI components
    ├── navigation/
    │   └── AppNavigation.kt             # Navigation host & route definitions
    ├── screens/
    │   ├── DashboardScreen.kt           # Dashboard & telemetry view
    │   ├── DomainDetailScreen.kt        # Activity logging screen
    │   ├── DomainsScreen.kt             # Domain catalog view
    │   ├── GoalsScreen.kt               # Goal configuration & progress screen
    │   ├── ReportsScreen.kt             # PDF report generator screen
    │   ├── SettingsScreen.kt            # Application configuration screen
    │   └── StatisticsScreen.kt          # Calendar & activity ledger screen
    └── viewmodels/
        ├── DashboardViewModel.kt        # ViewModel for telemetry
        ├── GoalsViewModel.kt            # ViewModel for goal comparisons
        ├── ReportsViewModel.kt          # ViewModel for report compilation
        └── StatisticsViewModel.kt       # ViewModel for calendar & analytics
```

---

## 5. Build and Deployment Instructions

### Prerequisites
* JDK 17
* Android SDK API Level 36 (Minimum SDK API Level 24)
* Gradle 8.x with Kotlin Symbol Processing (KSP)

### Assembly Commands
To assemble a debug APK package using Gradle:
```bash
gradle :app:assembleDebug
```

To execute local unit and ViewModel state tests:
```bash
gradle :app:testDebugUnitTest
```

---

## 6. Security and Administrative Note

This application is designed for internal organizational accountability within Christian Missionary Fellowship International. User data is persisted locally in an encrypted Room SQLite database container (`app_database.db`) on the client device. Shareable exports utilize secure Android `FileProvider` authorities (`com.example.fileprovider`) preventing unauthorized external storage access.
