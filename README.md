# CMFI Spiritual Accountability App

An Android application built with **Kotlin** and **Jetpack Compose** designed for disciples within **Christian Missionary Fellowship International (CMFI)** to record, track, analyze, and report their daily spiritual habits, disciplines, and accountability commitments to their Disciple Makers.

---

## 📖 Overview

Spiritual growth thrives on intentionality and accountability. The CMFI Spiritual Accountability App provides a structured digital companion for believers to track their daily devotional life across key spiritual disciplines, calculate consistency streaks, set personal growth goals, and export formal PDF progress reports directly to Disciple Makers.

---

## ✨ Features

- **📊 Central Dashboard**: Overview of today's habit execution, active streaks, weekly overview, and quick-logging shortcuts.
- **✝️ 11 Spiritual Discipline Domains**:
  - **DDEWG**: Daily Dynamic Encounter with God (meditation and prayer duration tracking).
  - **Bible Reading**: Chapter-by-chapter reading tracker with Old and New Testament coverage.
  - **Prayer (Alone & With Others)**: Live built-in timer or manual entry for intercession and communion.
  - **Fasting**: Duration, type (dry, water, partial), and frequency log.
  - **Soul Winning & Evangelism**: Preached-to count, convert tracking, and follow-up notes.
  - **Giving & Stewardship**: Tithes, offerings, and sacrificial giving logs.
  - **Christian Literature & Memorization**: Page counters, book titles, and quote records.
  - **Bible Memorization**: Scripture passage memory log.
  - **Disciple Accountability & Custom Domains**: User-defined custom habits.
- **⏱️ Live Interactive Timer**: Background-friendly timer for prayer and quiet-time sessions.
- **📅 Interactive Calendar & Journal**: Historical day-by-day discipline completion visualizer and entry review.
- **📈 Advanced Statistics**: Graphical insights and analytics summarizing weekly and monthly activity completion rates.
- **📄 PDF Report Generator & Export**:
  - Generates formatted **CMFI Spiritual Accountability PDF Reports** (Daily, Weekly, Monthly).
  - Integrated with Android `FileProvider` for sharing via WhatsApp, Email, Telegram, or social platforms.
  - Full multi-page support with itemized discipline entries and summary metrics.
- **🎯 Goals & Target Setting**: Flexible target frequency tracking (Daily, Weekly, Monthly) with custom notification reminders.
- **🌐 Multilingual Support**: Built-in localization support (English & French).

---

## 🛠 Tech Stack & Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (100% declarative UI with Material Design 3)
- **Architecture Pattern**: MVVM (Model-View-ViewModel) with Clean Repository pattern
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) with KSP annotation processing
- **Asynchronous Data**: Kotlin Coroutines & `StateFlow` / `collectAsStateWithLifecycle`
- **Navigation**: Jetpack Compose Navigation
- **PDF Generation**: Android `PdfDocument` with `FileProvider` secure URI sharing
- **Build System**: Gradle with Kotlin DSL (`build.gradle.kts`)

---

## 📂 Project Structure

```
app/
├── src/
│   └── main/
│       ├── java/com/example/
│       │   ├── MainActivity.kt               # App entry point & theme container
│       │   ├── core/
│       │   │   ├── localization/             # AppStrings (English & French translations)
│       │   │   └── notification/             # AlarmManager & Notification Services
│       │   ├── data/
│       │   │   ├── local/                    # Room AppDatabase, DAOs, Entities, & Bible Metadata
│       │   │   └── repositories/             # Accountability, User, & Domain Data Repositories
│       │   ├── services/
│       │   │   ├── reports/                  # PdfReportGenerator (Android PdfDocument builder)
│       │   │   └── timer/                    # Active Timer Service Manager
│       │   ├── ui/
│       │   │   ├── components/               # Reusable Jetpack Compose widgets & cards
│       │   │   ├── navigation/               # AppNavigation & NavRoutes setup
│       │   │   ├── screens/                  # Dashboard, Domains, Calendar, Stats, Reports, Settings
│       │   │   ├── theme/                    # Material 3 Color Schemes, Typography, Shapes
│       │   │   └── viewmodels/               # ViewModels & ViewModelFactory
│       │   └── AndroidManifest.xml           # App manifest & FileProvider configuration
│       └── res/
│           ├── values/                       # Strings, colors, & themes
│           └── xml/                          # file_paths.xml for FileProvider sharing
├── build.gradle.kts                          # Root build script
├── settings.gradle.kts                       # Project settings
└── README.md                                 # Project documentation
```

---

## 📋 Requirements & Prerequisites

- **Android Studio**: Android Studio Jellyfish (2024.1.1) or newer
- **JDK**: JDK 11 or JDK 17
- **Min SDK**: API Level 24 (Android 7.0 Nougat)
- **Target SDK**: API Level 36
- **Gradle**: 8.x with Kotlin Gradle Plugin

---

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/cmfi-accountability-app.git
cd cmfi-accountability-app
```

### 2. Open in Android Studio
1. Open Android Studio and select **Open**.
2. Navigate to the project directory and click **OK**.
3. Allow Gradle to sync dependencies automatically.

### 3. Build & Run
- Connect an Android device or launch an Android Virtual Device (AVD).
- Click **Run 'app'** or use the terminal:
```bash
./gradlew assembleDebug
```

---

## 📄 Generating & Exporting PDF Reports

1. Navigate to the **Reports** tab from the bottom navigation bar.
2. Select your desired report period (**DAILY**, **WEEKLY**, or **MONTHLY**).
3. Tap **Generate PDF Report**.
4. Once generated, tap:
   - **PDF Icon**: To open and view the formatted document in any installed PDF viewer.
   - **Share Icon**: To share the actual `.pdf` document directly with your Disciple Maker via WhatsApp, Email, or messaging apps.

---

## 🤝 Contributing

Contributions, feedback, and feature suggestions are welcome!
1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/NewHabitDomain`).
3. Commit your changes (`git commit -m 'Add NewHabitDomain'`).
4. Push to the branch (`git push origin feature/NewHabitDomain`).
5. Open a Pull Request.

---

## 📜 License

Distributed under the MIT License. See `LICENSE` for more information.
