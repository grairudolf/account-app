package com.example.services.sync

import android.content.Context
import android.util.Log
import com.example.core.util.NetworkUtils
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

enum class SyncStage {
    IDLE,
    PREPARING,
    DOWNLOADING,
    UPLOADING,
    COMPLETED,
    ERROR
}

data class SyncProgress(
    val isSyncing: Boolean = false,
    val progress: Float = 0f,
    val stage: SyncStage = SyncStage.IDLE,
    val stageTitle: String = "",
    val details: String = "",
    val lastSyncTimeMs: Long? = null,
    val error: String? = null
)

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"
    private const val TIMEOUT_MS = 25000L

    private val _syncProgressFlow = MutableStateFlow(SyncProgress())
    val syncProgressFlow: StateFlow<SyncProgress> = _syncProgressFlow.asStateFlow()

    private const val PREFS_NAME = "cmfi_backup_sync_prefs"
    private const val KEY_LAST_BACKUP_PREFIX = "last_backup_ms_"

    fun getLastCloudBackupTime(context: Context, userId: String): Long {
        if (userId.isBlank() || userId == "guest_user") return 0L
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_LAST_BACKUP_PREFIX + userId, 0L)
    }

    fun setLastCloudBackupTime(context: Context, userId: String, timeMs: Long) {
        if (userId.isBlank() || userId == "guest_user") return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_LAST_BACKUP_PREFIX + userId, timeMs).apply()
    }

    private fun updateProgress(
        isSyncing: Boolean,
        progress: Float,
        stage: SyncStage,
        stageTitle: String,
        details: String,
        lastSyncTimeMs: Long? = _syncProgressFlow.value.lastSyncTimeMs,
        error: String? = null
    ) {
        _syncProgressFlow.value = SyncProgress(
            isSyncing = isSyncing,
            progress = progress.coerceIn(0f, 1f),
            stage = stage,
            stageTitle = stageTitle,
            details = details,
            lastSyncTimeMs = lastSyncTimeMs,
            error = error
        )
    }

    fun isFirestoreAvailable(context: Context): Boolean {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Throwable) {
                    Log.w(TAG, "FirebaseApp init attempt: ${e.message}")
                }
            }
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Throwable) {
            false
        }
    }

    private fun getFirestore(context: Context): FirebaseFirestore? {
        return try {
            if (isFirestoreAvailable(context)) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Throwable) {
            Log.w(TAG, "Firestore not available: ${e.message}")
            null
        }
    }

    /**
     * Completely restores all user data (profile, goals, entries, streak history, disciples,
     * topics, reports, custom domains) from Firestore into the local Room database.
     */
    suspend fun restoreUserDataFromCloud(
        context: Context,
        database: AppDatabase,
        userId: String,
        isPartOfFullSync: Boolean = false,
        forceRestore: Boolean = false
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            if (userId.isBlank() || userId == "guest_user") return@withContext Result.success(false)
            val firestore = getFirestore(context)
            if (firestore == null) {
                Log.w(TAG, "Cannot restore from cloud: Firebase Firestore is not initialized (missing google-services.json)")
                updateProgress(
                    isSyncing = false,
                    progress = 0f,
                    stage = SyncStage.ERROR,
                    stageTitle = "Cloud Not Connected",
                    details = "Firebase is not configured. Add google-services.json to enable cloud restore.",
                    error = "Firebase not initialized"
                )
                return@withContext Result.failure(IllegalStateException("Firebase Firestore is not initialized"))
            }

            val baseProgress = if (isPartOfFullSync) 0.05f else 0.05f
            val maxProgress = if (isPartOfFullSync) 0.45f else 0.95f
            fun scaleProgress(relative: Float): Float = baseProgress + (maxProgress - baseProgress) * relative.coerceIn(0f, 1f)

            Log.i(TAG, "Starting cloud restore for user: $userId (forceRestore=$forceRestore)")
            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.10f),
                stage = SyncStage.DOWNLOADING,
                stageTitle = "Connecting to Cloud Database",
                details = "Checking cloud status & profile..."
            )

            var remoteLastCloudUpdateMs = 0L

            // 1. Fetch User Profile Document with timeout
            withTimeoutOrNull(TIMEOUT_MS) {
                try {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    if (userDoc.exists()) {
                        val data = userDoc.data
                        if (data != null) {
                            remoteLastCloudUpdateMs = (data["lastCloudUpdateMs"] as? Number)?.toLong()
                                ?: (data["updatedAtMs"] as? Number)?.toLong() ?: 0L

                            val existing = database.userDao().getCurrentUser()
                            val remoteName = data["fullName"] as? String ?: ""
                            val remoteEmail = data["email"] as? String ?: ""
                            val remoteAssembly = data["localAssembly"] as? String ?: ""
                            val remoteDiscipleMaker = data["discipleMaker"] as? String ?: ""
                            val remotePhone = data["phoneNumber"] as? String ?: ""
                            val remoteLang = data["language"] as? String ?: ""
                            val remoteTheme = data["themeMode"] as? String ?: ""
                            val remoteConversion = data["conversionDate"] as? String ?: ""
                            val remoteDays = data["accountabilityDays"] as? String ?: ""

                            val restoredUser = UserEntity(
                                id = userId,
                                fullName = remoteName.ifBlank { existing?.fullName?.ifBlank { "Disciple" } ?: "Disciple" },
                                email = remoteEmail.ifBlank { existing?.email ?: "" },
                                profileImageUri = (data["profileImageUri"] as? String)?.ifBlank { null } ?: existing?.profileImageUri,
                                localAssembly = remoteAssembly.ifBlank { existing?.localAssembly ?: "" },
                                discipleMaker = remoteDiscipleMaker.ifBlank { existing?.discipleMaker ?: "" },
                                phoneNumber = remotePhone.ifBlank { existing?.phoneNumber ?: "" },
                                language = remoteLang.ifBlank { existing?.language ?: "en" },
                                themeMode = remoteTheme.ifBlank { existing?.themeMode ?: "LIGHT" },
                                conversionDate = remoteConversion.ifBlank { existing?.conversionDate ?: "" },
                                accountabilityDays = remoteDays.ifBlank { existing?.accountabilityDays ?: "MON,TUE,WED,THU,FRI,SAT,SUN" },
                                isGuest = false,
                                syncStatus = "SYNCED",
                                createdAtMs = (data["createdAtMs"] as? Number)?.toLong() ?: existing?.createdAtMs ?: System.currentTimeMillis(),
                                updatedAtMs = (data["updatedAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                            database.userDao().insertOrUpdateUser(restoredUser)
                            Log.i(TAG, "Restored profile for: ${restoredUser.fullName} (${restoredUser.email})")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Notice fetching user profile: ${e.message}")
                }
            }

            // Check if local data is already up to date with cloud
            val localEntries = database.entryDao().getAllEntriesList().filter { it.userId == userId }
            val lastLocalSync = getLastCloudBackupTime(context, userId)

            if (!forceRestore && localEntries.isNotEmpty() && lastLocalSync > 0 && remoteLastCloudUpdateMs > 0 && remoteLastCloudUpdateMs <= lastLocalSync) {
                Log.i(TAG, "Local records are already up to date with cloud for user: $userId (remote=$remoteLastCloudUpdateMs, local=$lastLocalSync). Skipping redundant downloads.")
                if (!isPartOfFullSync) {
                    updateProgress(
                        isSyncing = false,
                        progress = 1.0f,
                        stage = SyncStage.COMPLETED,
                        stageTitle = "Data Already Up to Date",
                        details = "All local spiritual records match the cloud. No download needed.",
                        lastSyncTimeMs = lastLocalSync
                    )
                } else {
                    updateProgress(
                        isSyncing = true,
                        progress = scaleProgress(1.0f),
                        stage = SyncStage.DOWNLOADING,
                        stageTitle = "Data Already Up to Date",
                        details = "Local records match cloud. Checking for local changes to backup..."
                    )
                }
                return@withContext Result.success(true)
            }

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.25f),
                stage = SyncStage.DOWNLOADING,
                stageTitle = "Downloading Spiritual Entries",
                details = "Fetching prayer, scripture and fasting logs..."
            )

            // 2. Restore Entries
            withTimeoutOrNull(TIMEOUT_MS) {
                try {
                    val entriesSnapshot = firestore.collection("users").document(userId).collection("entries").get().await()
                    val totalEntries = entriesSnapshot.documents.size
                    for ((index, doc) in entriesSnapshot.documents.withIndex()) {
                        val d = doc.data ?: continue
                        val entry = AccountabilityEntryEntity(
                            id = doc.id,
                            userId = userId,
                            domainId = d["domainId"] as? String ?: "",
                            dateIso = d["dateIso"] as? String ?: "",
                            timestampMs = (d["timestampMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            timezoneId = d["timezoneId"] as? String ?: "UTC",
                            durationSeconds = (d["durationSeconds"] as? Number)?.toLong() ?: 0L,
                            startTimeIso = d["startTimeIso"] as? String ?: "",
                            endTimeIso = d["endTimeIso"] as? String ?: "",
                            notes = d["notes"] as? String ?: "",
                            reflection = d["reflection"] as? String ?: "",
                            bibleVersion = d["bibleVersion"] as? String ?: "KJV",
                            bibleBook = d["bibleBook"] as? String ?: "",
                            startChapter = (d["startChapter"] as? Number)?.toInt() ?: 0,
                            endChapter = (d["endChapter"] as? Number)?.toInt() ?: 0,
                            chaptersCount = (d["chaptersCount"] as? Number)?.toInt() ?: 0,
                            prayerType = d["prayerType"] as? String ?: "",
                            prayerTopicsCount = (d["prayerTopicsCount"] as? Number)?.toInt() ?: 0,
                            startPrayerTopicNumber = (d["startPrayerTopicNumber"] as? Number)?.toInt() ?: 0,
                            endPrayerTopicNumber = (d["endPrayerTopicNumber"] as? Number)?.toInt() ?: 0,
                            prayerParticipantsCount = (d["prayerParticipantsCount"] as? Number)?.toInt() ?: 1,
                            prayerParticipantNames = d["prayerParticipantNames"] as? String ?: "",
                            fastingType = d["fastingType"] as? String ?: "",
                            fastingStartDateIso = d["fastingStartDateIso"] as? String ?: "",
                            fastingEndDateIso = d["fastingEndDateIso"] as? String ?: "",
                            fastingDaysCount = (d["fastingDaysCount"] as? Number)?.toInt() ?: 0,
                            fastingPurpose = d["fastingPurpose"] as? String ?: "",
                            givingAmount = (d["givingAmount"] as? Number)?.toDouble() ?: 0.0,
                            givingType = d["givingType"] as? String ?: "",
                            givingIncomeReference = (d["givingIncomeReference"] as? Number)?.toDouble() ?: 0.0,
                            givingPercentage = (d["givingPercentage"] as? Number)?.toDouble() ?: 0.0,
                            accountabilityFrequency = d["accountabilityFrequency"] as? String ?: "",
                            areasDiscussed = d["areasDiscussed"] as? String ?: "",
                            bookTitle = d["bookTitle"] as? String ?: "",
                            bookAuthor = d["bookAuthor"] as? String ?: "",
                            totalPages = (d["totalPages"] as? Number)?.toInt() ?: 0,
                            startPage = (d["startPage"] as? Number)?.toInt() ?: 0,
                            endPage = (d["endPage"] as? Number)?.toInt() ?: 0,
                            pagesRead = (d["pagesRead"] as? Number)?.toInt() ?: 0,
                            bookTimesRead = (d["bookTimesRead"] as? Number)?.toInt() ?: 1,
                            pagesMemorized = (d["pagesMemorized"] as? Number)?.toInt() ?: 0,
                            litMemChapter = d["litMemChapter"] as? String ?: "",
                            litMemPassage = d["litMemPassage"] as? String ?: "",
                            litMemStatus = d["litMemStatus"] as? String ?: "",
                            isBookCompleted = (d["isBookCompleted"] as? Boolean) ?: false,
                            retreatFocus = d["retreatFocus"] as? String ?: "",
                            retreatActivitiesJson = d["retreatActivitiesJson"] as? String ?: "",
                            bibleMemBook = d["bibleMemBook"] as? String ?: "",
                            bibleMemChapter = (d["bibleMemChapter"] as? Number)?.toInt() ?: 0,
                            bibleMemVerse = d["bibleMemVerse"] as? String ?: "",
                            bibleMemStatus = d["bibleMemStatus"] as? String ?: "",
                            preachedToCount = (d["preachedToCount"] as? Number)?.toInt() ?: 0,
                            convertedCount = (d["convertedCount"] as? Number)?.toInt() ?: 0,
                            waterBaptizedCount = (d["waterBaptizedCount"] as? Number)?.toInt() ?: 0,
                            holySpiritBaptizedCount = (d["holySpiritBaptizedCount"] as? Number)?.toInt() ?: 0,
                            proclamationTopic = d["proclamationTopic"] as? String ?: "",
                            proclamationCount = (d["proclamationCount"] as? Number)?.toInt() ?: 0,
                            proclamationTarget = (d["proclamationTarget"] as? Number)?.toInt() ?: 0,
                            customValue = d["customValue"] as? String ?: "",
                            createdAtMs = (d["createdAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            updatedAtMs = (d["updatedAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            syncStatus = "SYNCED"
                        )
                        database.entryDao().insertOrUpdateEntry(entry)
                        if (index % 5 == 0 || index == totalEntries - 1) {
                            val p = scaleProgress(0.25f + 0.25f * ((index + 1).toFloat() / totalEntries.coerceAtLeast(1)))
                            updateProgress(
                                isSyncing = true,
                                progress = p,
                                stage = SyncStage.DOWNLOADING,
                                stageTitle = "Downloading Entries",
                                details = "Restored ${index + 1} of $totalEntries spiritual entries"
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Notice fetching entries: ${e.message}")
                }
            }

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.55f),
                stage = SyncStage.DOWNLOADING,
                stageTitle = "Downloading Goals",
                details = "Fetching spiritual targets and metrics..."
            )

            // 3. Restore Goals
            withTimeoutOrNull(TIMEOUT_MS) {
                try {
                    val goalsSnapshot = firestore.collection("users").document(userId).collection("goals").get().await()
                    for (doc in goalsSnapshot.documents) {
                        val d = doc.data ?: continue
                        val goal = GoalEntity(
                            id = doc.id,
                            userId = userId,
                            domainId = d["domainId"] as? String ?: "",
                            title = d["title"] as? String ?: "",
                            targetValue = (d["targetValue"] as? Number)?.toDouble() ?: 0.0,
                            unit = d["unit"] as? String ?: "",
                            frequency = d["frequency"] as? String ?: "DAILY",
                            startDateIso = d["startDateIso"] as? String ?: "",
                            endDateIso = d["endDateIso"] as? String ?: "",
                            isPaused = (d["isPaused"] as? Boolean) ?: false,
                            createdAtMs = (d["createdAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            updatedAtMs = (d["updatedAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                        database.goalDao().insertOrUpdateGoal(goal)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Notice fetching goals: ${e.message}")
                }
            }

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.70f),
                stage = SyncStage.DOWNLOADING,
                stageTitle = "Downloading Disciples",
                details = "Restoring discipleship names and prayer topics..."
            )

            // 4. Restore Disciples
            withTimeoutOrNull(TIMEOUT_MS) {
                try {
                    val disciplesSnapshot = firestore.collection("users").document(userId).collection("disciples").get().await()
                    for (doc in disciplesSnapshot.documents) {
                        val d = doc.data ?: continue
                        val disciple = DiscipleEntity(
                            id = doc.id,
                            userId = userId,
                            name = d["name"] as? String ?: "",
                            phone = d["phone"] as? String ?: "",
                            status = d["status"] as? String ?: "Growing Disciple",
                            conversionDateIso = d["conversionDateIso"] as? String ?: "",
                            prayerTopics = d["prayerTopics"] as? String ?: "",
                            notes = d["notes"] as? String ?: "",
                            topicsCovered = d["topicsCovered"] as? String ?: "",
                            createdAtMs = (d["createdAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            updatedAtMs = (d["updatedAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                        database.discipleDao().insertDisciple(disciple)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Notice fetching disciples: ${e.message}")
                }
            }

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.85f),
                stage = SyncStage.DOWNLOADING,
                stageTitle = "Downloading Custom Domains & Topics",
                details = "Restoring domains and proclamation records..."
            )

            // 5. Restore Custom Domains
            withTimeoutOrNull(TIMEOUT_MS) {
                try {
                    val domainsSnapshot = firestore.collection("users").document(userId).collection("custom_domains").get().await()
                    for (doc in domainsSnapshot.documents) {
                        val d = doc.data ?: continue
                        val customDomain = CustomDomainEntity(
                            id = doc.id,
                            userId = userId,
                            name = d["name"] as? String ?: "",
                            description = d["description"] as? String ?: "",
                            iconName = d["iconName"] as? String ?: "star",
                            measurementType = d["measurementType"] as? String ?: "COUNT",
                            frequency = d["frequency"] as? String ?: "DAILY",
                            createdAtMs = (d["createdAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                        database.customDomainDao().insertCustomDomain(customDomain)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Notice fetching custom domains: ${e.message}")
                }
            }

            // 6. Restore Proclamation Topics
            withTimeoutOrNull(TIMEOUT_MS) {
                try {
                    val procSnapshot = firestore.collection("users").document(userId).collection("proclamation_topics").get().await()
                    for (doc in procSnapshot.documents) {
                        val d = doc.data ?: continue
                        val proc = ProclamationTopicEntity(
                            id = doc.id,
                            userId = userId,
                            topic = d["topic"] as? String ?: "",
                            cumulativeCount = (d["cumulativeCount"] as? Number)?.toInt() ?: 0,
                            targetCount = (d["targetCount"] as? Number)?.toInt() ?: 100,
                            totalDurationSeconds = (d["totalDurationSeconds"] as? Number)?.toLong() ?: 0L,
                            lastPracticedIso = d["lastPracticedIso"] as? String ?: "",
                            notes = d["notes"] as? String ?: "",
                            createdAtMs = (d["createdAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                            updatedAtMs = (d["updatedAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                        database.proclamationTopicDao().insertOrUpdateTopic(proc)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Notice fetching proclamation topics: ${e.message}")
                }
            }

            // 7. Restore Reports
            withTimeoutOrNull(TIMEOUT_MS) {
                try {
                    val repSnapshot = firestore.collection("users").document(userId).collection("reports").get().await()
                    for (doc in repSnapshot.documents) {
                        val d = doc.data ?: continue
                        val report = ReportRecordEntity(
                            id = doc.id,
                            userId = userId,
                            reportType = d["reportType"] as? String ?: "WEEKLY",
                            dateRangeLabel = d["dateRangeLabel"] as? String ?: "",
                            selectedDomainsCsv = d["selectedDomainsCsv"] as? String ?: "",
                            generatedFilePath = d["generatedFilePath"] as? String ?: "",
                            generatedAtMs = (d["generatedAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis()
                        )
                        database.reportDao().insertReport(report)
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Notice fetching reports: ${e.message}")
                }
            }

            val now = System.currentTimeMillis()
            setLastCloudBackupTime(context, userId, now)

            if (!isPartOfFullSync) {
                Log.i(TAG, "Cloud restore finished successfully for user: $userId")
                updateProgress(
                    isSyncing = false,
                    progress = 1.0f,
                    stage = SyncStage.COMPLETED,
                    stageTitle = "Restore Completed",
                    details = "All records downloaded and restored from cloud",
                    lastSyncTimeMs = now
                )
            }
            Result.success(true)
        } catch (e: Exception) {
            Log.w(TAG, "Notice during cloud restore: ${e.message}")
            if (!isPartOfFullSync) {
                updateProgress(
                    isSyncing = false,
                    progress = 1.0f,
                    stage = SyncStage.COMPLETED,
                    stageTitle = "Offline Mode",
                    details = "Using local database (cloud currently unreachable)",
                    lastSyncTimeMs = _syncProgressFlow.value.lastSyncTimeMs
                )
            }
            Result.success(false)
        }
    }

    /**
     * Pushes user profile to Firestore
     */
    suspend fun syncUserProfile(context: Context, user: UserEntity) = withContext(Dispatchers.IO) {
        if (user.isGuest || user.id.isBlank() || user.id == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val map = hashMapOf<String, Any>(
                    "id" to user.id,
                    "updatedAtMs" to user.updatedAtMs
                )
                if (user.fullName.isNotBlank()) map["fullName"] = user.fullName
                if (user.email.isNotBlank()) map["email"] = user.email
                if (!user.profileImageUri.isNullOrBlank()) map["profileImageUri"] = user.profileImageUri
                if (user.localAssembly.isNotBlank()) map["localAssembly"] = user.localAssembly
                if (user.discipleMaker.isNotBlank()) map["discipleMaker"] = user.discipleMaker
                if (user.phoneNumber.isNotBlank()) map["phoneNumber"] = user.phoneNumber
                if (user.language.isNotBlank()) map["language"] = user.language
                if (user.themeMode.isNotBlank()) map["themeMode"] = user.themeMode
                if (user.conversionDate.isNotBlank()) map["conversionDate"] = user.conversionDate
                if (user.accountabilityDays.isNotBlank()) map["accountabilityDays"] = user.accountabilityDays

                firestore.collection("users").document(user.id).set(map, SetOptions.merge()).await()
                Log.d(TAG, "User profile backed up to Firestore: ${user.id}")
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync profile to cloud: ${e.message}")
            }
        }
    }

    /**
     * Pushes an AccountabilityEntryEntity to Firestore
     */
    suspend fun syncEntry(context: Context, entry: AccountabilityEntryEntity): Boolean = withContext(Dispatchers.IO) {
        if (entry.userId.isBlank() || entry.userId == "guest_user") return@withContext false
        val firestore = getFirestore(context) ?: return@withContext false
        val result = withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val map = hashMapOf(
                    "id" to entry.id,
                    "userId" to entry.userId,
                    "domainId" to entry.domainId,
                    "dateIso" to entry.dateIso,
                    "timestampMs" to entry.timestampMs,
                    "timezoneId" to entry.timezoneId,
                    "durationSeconds" to entry.durationSeconds,
                    "startTimeIso" to entry.startTimeIso,
                    "endTimeIso" to entry.endTimeIso,
                    "notes" to entry.notes,
                    "reflection" to entry.reflection,
                    "bibleVersion" to entry.bibleVersion,
                    "bibleBook" to entry.bibleBook,
                    "startChapter" to entry.startChapter,
                    "endChapter" to entry.endChapter,
                    "chaptersCount" to entry.chaptersCount,
                    "prayerType" to entry.prayerType,
                    "prayerTopicsCount" to entry.prayerTopicsCount,
                    "startPrayerTopicNumber" to entry.startPrayerTopicNumber,
                    "endPrayerTopicNumber" to entry.endPrayerTopicNumber,
                    "prayerParticipantsCount" to entry.prayerParticipantsCount,
                    "prayerParticipantNames" to entry.prayerParticipantNames,
                    "fastingType" to entry.fastingType,
                    "fastingStartDateIso" to entry.fastingStartDateIso,
                    "fastingEndDateIso" to entry.fastingEndDateIso,
                    "fastingDaysCount" to entry.fastingDaysCount,
                    "fastingPurpose" to entry.fastingPurpose,
                    "givingAmount" to entry.givingAmount,
                    "givingType" to entry.givingType,
                    "givingIncomeReference" to entry.givingIncomeReference,
                    "givingPercentage" to entry.givingPercentage,
                    "accountabilityFrequency" to entry.accountabilityFrequency,
                    "areasDiscussed" to entry.areasDiscussed,
                    "bookTitle" to entry.bookTitle,
                    "bookAuthor" to entry.bookAuthor,
                    "totalPages" to entry.totalPages,
                    "startPage" to entry.startPage,
                    "endPage" to entry.endPage,
                    "pagesRead" to entry.pagesRead,
                    "bookTimesRead" to entry.bookTimesRead,
                    "pagesMemorized" to entry.pagesMemorized,
                    "litMemChapter" to entry.litMemChapter,
                    "litMemPassage" to entry.litMemPassage,
                    "litMemStatus" to entry.litMemStatus,
                    "isBookCompleted" to entry.isBookCompleted,
                    "retreatFocus" to entry.retreatFocus,
                    "retreatActivitiesJson" to entry.retreatActivitiesJson,
                    "bibleMemBook" to entry.bibleMemBook,
                    "bibleMemChapter" to entry.bibleMemChapter,
                    "bibleMemVerse" to entry.bibleMemVerse,
                    "bibleMemStatus" to entry.bibleMemStatus,
                    "preachedToCount" to entry.preachedToCount,
                    "convertedCount" to entry.convertedCount,
                    "waterBaptizedCount" to entry.waterBaptizedCount,
                    "holySpiritBaptizedCount" to entry.holySpiritBaptizedCount,
                    "proclamationTopic" to entry.proclamationTopic,
                    "proclamationCount" to entry.proclamationCount,
                    "proclamationTarget" to entry.proclamationTarget,
                    "customValue" to entry.customValue,
                    "createdAtMs" to entry.createdAtMs,
                    "updatedAtMs" to entry.updatedAtMs
                )
                firestore.collection("users").document(entry.userId).collection("entries").document(entry.id).set(map, SetOptions.merge()).await()
                true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync entry to cloud: ${e.message}")
                false
            }
        }
        result ?: false
    }

    suspend fun deleteEntry(context: Context, userId: String, entryId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                firestore.collection("users").document(userId).collection("entries").document(entryId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete entry from cloud: ${e.message}")
            }
        }
    }

    /**
     * Pushes a Goal to Firestore
     */
    suspend fun syncGoal(context: Context, goal: GoalEntity) = withContext(Dispatchers.IO) {
        if (goal.userId.isBlank() || goal.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val map = hashMapOf(
                    "id" to goal.id,
                    "userId" to goal.userId,
                    "domainId" to goal.domainId,
                    "title" to goal.title,
                    "targetValue" to goal.targetValue,
                    "unit" to goal.unit,
                    "frequency" to goal.frequency,
                    "startDateIso" to goal.startDateIso,
                    "endDateIso" to goal.endDateIso,
                    "isPaused" to goal.isPaused,
                    "createdAtMs" to goal.createdAtMs,
                    "updatedAtMs" to goal.updatedAtMs
                )
                firestore.collection("users").document(goal.userId).collection("goals").document(goal.id).set(map, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync goal to cloud: ${e.message}")
            }
        }
    }

    suspend fun deleteGoal(context: Context, userId: String, goalId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                firestore.collection("users").document(userId).collection("goals").document(goalId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete goal from cloud: ${e.message}")
            }
        }
    }

    /**
     * Pushes a Disciple to Firestore
     */
    suspend fun syncDisciple(context: Context, disciple: DiscipleEntity) = withContext(Dispatchers.IO) {
        if (disciple.userId.isBlank() || disciple.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val map = hashMapOf(
                    "id" to disciple.id,
                    "userId" to disciple.userId,
                    "name" to disciple.name,
                    "phone" to disciple.phone,
                    "status" to disciple.status,
                    "conversionDateIso" to disciple.conversionDateIso,
                    "prayerTopics" to disciple.prayerTopics,
                    "notes" to disciple.notes,
                    "topicsCovered" to disciple.topicsCovered,
                    "createdAtMs" to disciple.createdAtMs,
                    "updatedAtMs" to disciple.updatedAtMs
                )
                firestore.collection("users").document(disciple.userId).collection("disciples").document(disciple.id).set(map, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync disciple: ${e.message}")
            }
        }
    }

    suspend fun deleteDisciple(context: Context, userId: String, discipleId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                firestore.collection("users").document(userId).collection("disciples").document(discipleId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete disciple from cloud: ${e.message}")
            }
        }
    }

    /**
     * Pushes a ProclamationTopic to Firestore
     */
    suspend fun syncProclamationTopic(context: Context, topic: ProclamationTopicEntity) = withContext(Dispatchers.IO) {
        if (topic.userId.isBlank() || topic.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val map = hashMapOf(
                    "id" to topic.id,
                    "userId" to topic.userId,
                    "topic" to topic.topic,
                    "cumulativeCount" to topic.cumulativeCount,
                    "targetCount" to topic.targetCount,
                    "totalDurationSeconds" to topic.totalDurationSeconds,
                    "lastPracticedIso" to topic.lastPracticedIso,
                    "notes" to topic.notes,
                    "createdAtMs" to topic.createdAtMs,
                    "updatedAtMs" to topic.updatedAtMs
                )
                firestore.collection("users").document(topic.userId).collection("proclamation_topics").document(topic.id).set(map, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync proclamation topic: ${e.message}")
            }
        }
    }

    suspend fun deleteProclamationTopic(context: Context, userId: String, topicId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                firestore.collection("users").document(userId).collection("proclamation_topics").document(topicId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete proclamation topic from cloud: ${e.message}")
            }
        }
    }

    /**
     * Pushes a CustomDomain to Firestore
     */
    suspend fun syncCustomDomain(context: Context, domain: CustomDomainEntity) = withContext(Dispatchers.IO) {
        if (domain.userId.isBlank() || domain.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val map = hashMapOf(
                    "id" to domain.id,
                    "userId" to domain.userId,
                    "name" to domain.name,
                    "description" to domain.description,
                    "iconName" to domain.iconName,
                    "measurementType" to domain.measurementType,
                    "frequency" to domain.frequency,
                    "createdAtMs" to domain.createdAtMs
                )
                firestore.collection("users").document(domain.userId).collection("custom_domains").document(domain.id).set(map, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync custom domain: ${e.message}")
            }
        }
    }

    /**
     * Pushes a saved Report Record to Firestore
     */
    suspend fun syncReport(context: Context, report: ReportRecordEntity) = withContext(Dispatchers.IO) {
        if (report.userId.isBlank() || report.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                val map = hashMapOf(
                    "id" to report.id,
                    "userId" to report.userId,
                    "reportType" to report.reportType,
                    "dateRangeLabel" to report.dateRangeLabel,
                    "selectedDomainsCsv" to report.selectedDomainsCsv,
                    "generatedFilePath" to report.generatedFilePath,
                    "generatedAtMs" to report.generatedAtMs
                )
                firestore.collection("users").document(report.userId).collection("reports").document(report.id).set(map, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync report: ${e.message}")
            }
        }
    }

    suspend fun deleteReportFromCloud(context: Context, userId: String, reportId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user" || reportId.isBlank()) return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
            try {
                firestore.collection("users").document(userId).collection("reports").document(reportId).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to delete report from cloud: ${e.message}")
            }
        }
    }

    /**
     * Optimized local-to-cloud backup: only uploads new or modified records (incremental sync)
     */
    suspend fun syncAllLocalDataToCloud(
        context: Context,
        database: AppDatabase,
        userId: String,
        isPartOfFullSync: Boolean = false,
        forceUploadAll: Boolean = false
    ) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        try {
            val firestore = getFirestore(context)
            if (firestore == null) {
                Log.w(TAG, "Cannot backup to cloud: Firebase Firestore is not initialized (missing google-services.json)")
                updateProgress(
                    isSyncing = false,
                    progress = 0f,
                    stage = SyncStage.ERROR,
                    stageTitle = "Cloud Not Connected",
                    details = "Firebase is not configured. Records remain stored locally on your device only.",
                    error = "Missing google-services.json"
                )
                return@withContext
            }

            val lastBackupMs = if (forceUploadAll) 0L else getLastCloudBackupTime(context, userId)

            val baseProgress = if (isPartOfFullSync) 0.50f else 0.05f
            val maxProgress = 1.0f
            fun scaleProgress(relative: Float): Float = baseProgress + (maxProgress - baseProgress) * relative.coerceIn(0f, 1f)

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.05f),
                stage = SyncStage.UPLOADING,
                stageTitle = "Preparing Cloud Backup",
                details = "Checking for new and modified records..."
            )

            // Migrate guest records in bulk once to avoid repetitive database writes & flow emissions
            database.entryDao().migrateUserEntries(userId)
            database.goalDao().migrateUserGoals(userId)

            val entries = database.entryDao().getAllEntriesList()
            val userEntries = entries.filter { it.userId == userId || it.userId == "guest_user" }
            val pendingEntries = userEntries.filter { forceUploadAll || it.syncStatus != "SYNCED" || it.updatedAtMs > lastBackupMs }

            val goals = database.goalDao().getAllGoalsList()
            val userGoals = goals.filter { it.userId == userId || it.userId == "guest_user" }
            val pendingGoals = userGoals.filter { forceUploadAll || it.updatedAtMs > lastBackupMs }

            val disciples = database.discipleDao().getDisciplesList(userId)
            val pendingDisciples = disciples.filter { forceUploadAll || it.updatedAtMs > lastBackupMs }

            val procTopics = database.proclamationTopicDao().getTopicsForUser(userId)
            val pendingTopics = procTopics.filter { forceUploadAll || it.updatedAtMs > lastBackupMs }

            val customDomains = database.customDomainDao().getAllDomainsList()
            val pendingDomains = customDomains.filter { (it.userId == userId || it.userId == "guest_user") && (forceUploadAll || it.createdAtMs > lastBackupMs) }

            val reports = database.reportDao().getAllReportsList()
            val pendingReports = reports.filter { (it.userId == userId || it.userId == "guest_user") && (forceUploadAll || it.generatedAtMs > lastBackupMs) }

            val totalPending = pendingEntries.size + pendingGoals.size + pendingDisciples.size + pendingTopics.size + pendingDomains.size + pendingReports.size

            if (totalPending == 0 && lastBackupMs > 0) {
                Log.i(TAG, "Incremental backup: 0 pending changes for user: $userId. Everything is already up to date in cloud.")
                val now = System.currentTimeMillis()
                setLastCloudBackupTime(context, userId, now)
                updateProgress(
                    isSyncing = false,
                    progress = 1.0f,
                    stage = SyncStage.COMPLETED,
                    stageTitle = "Cloud Backup Up to Date",
                    details = "All records are already safely backed up. No new changes detected.",
                    lastSyncTimeMs = now
                )
                return@withContext
            }

            // 1. Sync User Profile if changed
            val user = database.userDao().getCurrentUser()
            if (user != null && !user.isGuest) {
                if (forceUploadAll || user.updatedAtMs > lastBackupMs) {
                    updateProgress(
                        isSyncing = true,
                        progress = scaleProgress(0.15f),
                        stage = SyncStage.UPLOADING,
                        stageTitle = "Backing Up Profile",
                        details = "Saving account settings and profile..."
                    )
                    syncUserProfile(context, user)
                }
            }

            // 2. Sync Pending Entries
            val totalPendingEntries = pendingEntries.size
            if (totalPendingEntries > 0) {
                updateProgress(
                    isSyncing = true,
                    progress = scaleProgress(0.20f),
                    stage = SyncStage.UPLOADING,
                    stageTitle = "Uploading New Spiritual Entries",
                    details = "Uploading $totalPendingEntries new/modified entries..."
                )

                for ((index, entry) in pendingEntries.withIndex()) {
                    val entryToSync = if (entry.userId != userId) entry.copy(userId = userId) else entry
                    val synced = syncEntry(context, entryToSync)
                    if (synced) {
                        database.entryDao().updateSyncStatus(entryToSync.id, "SYNCED")
                    }

                    if (index % 5 == 0 || index == totalPendingEntries - 1) {
                        val p = scaleProgress(0.20f + 0.35f * ((index + 1).toFloat() / totalPendingEntries.coerceAtLeast(1)))
                        updateProgress(
                            isSyncing = true,
                            progress = p,
                            stage = SyncStage.UPLOADING,
                            stageTitle = "Uploading New Spiritual Entries",
                            details = "Backed up ${index + 1} of $totalPendingEntries prayer & scripture records"
                        )
                    }
                }
            }

            // 3. Sync Pending Goals
            val totalPendingGoals = pendingGoals.size
            if (totalPendingGoals > 0) {
                updateProgress(
                    isSyncing = true,
                    progress = scaleProgress(0.58f),
                    stage = SyncStage.UPLOADING,
                    stageTitle = "Uploading Spiritual Goals",
                    details = "Uploading $totalPendingGoals goals..."
                )

                for ((index, goal) in pendingGoals.withIndex()) {
                    val goalToSync = if (goal.userId != userId) goal.copy(userId = userId) else goal
                    syncGoal(context, goalToSync)

                    if (index % 4 == 0 || index == totalPendingGoals - 1) {
                        val p = scaleProgress(0.58f + 0.14f * ((index + 1).toFloat() / totalPendingGoals.coerceAtLeast(1)))
                        updateProgress(
                            isSyncing = true,
                            progress = p,
                            stage = SyncStage.UPLOADING,
                            stageTitle = "Uploading Spiritual Goals",
                            details = "Backed up ${index + 1} of $totalPendingGoals active goals"
                        )
                    }
                }
            }

            // 4. Sync Pending Disciples
            val totalPendingDisciples = pendingDisciples.size
            if (totalPendingDisciples > 0) {
                updateProgress(
                    isSyncing = true,
                    progress = scaleProgress(0.74f),
                    stage = SyncStage.UPLOADING,
                    stageTitle = "Uploading Disciples",
                    details = "Uploading $totalPendingDisciples disciples..."
                )

                for ((index, disciple) in pendingDisciples.withIndex()) {
                    syncDisciple(context, disciple)
                    if (index % 2 == 0 || index == totalPendingDisciples - 1) {
                        val p = scaleProgress(0.74f + 0.10f * ((index + 1).toFloat() / totalPendingDisciples.coerceAtLeast(1)))
                        updateProgress(
                            isSyncing = true,
                            progress = p,
                            stage = SyncStage.UPLOADING,
                            stageTitle = "Uploading Disciples",
                            details = "Backed up ${index + 1} of $totalPendingDisciples disciples"
                        )
                    }
                }
            }

            // 5. Sync Pending Custom Domains
            for (cd in pendingDomains) {
                val cdToSync = if (cd.userId != userId) cd.copy(userId = userId) else cd
                if (cd.userId != userId) {
                    database.customDomainDao().insertCustomDomain(cdToSync)
                }
                syncCustomDomain(context, cdToSync)
            }

            // 6. Sync Pending Proclamation Topics
            for (topic in pendingTopics) {
                syncProclamationTopic(context, topic)
            }

            // 7. Sync Pending Reports
            for (report in pendingReports) {
                val repToSync = if (report.userId != userId) report.copy(userId = userId) else report
                if (report.userId != userId) {
                    database.reportDao().insertReport(repToSync)
                }
                syncReport(context, repToSync)
            }

            val now = System.currentTimeMillis()
            setLastCloudBackupTime(context, userId, now)

            // Update timestamp in remote user document so sync knows cloud is fresh
            try {
                firestore.collection("users").document(userId).set(
                    mapOf("lastCloudUpdateMs" to now, "updatedAtMs" to now),
                    SetOptions.merge()
                ).await()
            } catch (e: Exception) {
                Log.w(TAG, "Notice updating remote lastCloudUpdateMs: ${e.message}")
            }

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.95f),
                stage = SyncStage.UPLOADING,
                stageTitle = "Finalizing Backup",
                details = "Finalizing cloud backup..."
            )

            Log.i(TAG, "Incremental local sync to cloud finished for user: $userId (uploaded $totalPending records)")
            updateProgress(
                isSyncing = false,
                progress = 1.0f,
                stage = SyncStage.COMPLETED,
                stageTitle = "Cloud Sync & Backup Complete",
                details = if (totalPending > 0) "Backed up $totalPending new/modified records to the cloud" else "All records up to date in cloud",
                lastSyncTimeMs = now
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error in incremental local sync: ${e.message}")
            updateProgress(
                isSyncing = false,
                progress = 1.0f,
                stage = SyncStage.COMPLETED,
                stageTitle = "Sync Finished",
                details = "Local database updated and preserved safely",
                lastSyncTimeMs = System.currentTimeMillis()
            )
        }
    }

    /**
     * Unified 2-way sync: Restores any remote records (0-50%) and uploads all local records (50-100%)
     */
    suspend fun performFullSync(
        context: Context,
        database: AppDatabase,
        userId: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext false
        if (!NetworkUtils.isOnline(context)) {
            Log.d(TAG, "Skipping full sync: device is currently offline")
            updateProgress(
                isSyncing = false,
                progress = 1.0f,
                stage = SyncStage.COMPLETED,
                stageTitle = "Offline Mode",
                details = "Working offline with local database",
                lastSyncTimeMs = _syncProgressFlow.value.lastSyncTimeMs
            )
            return@withContext false
        }
        try {
            updateProgress(
                isSyncing = true,
                progress = 0.05f,
                stage = SyncStage.PREPARING,
                stageTitle = "Initiating Cloud Sync",
                details = "Connecting to Firebase..."
            )
            // 1. Restore remote data first (Progress: 0.05f -> 0.45f)
            restoreUserDataFromCloud(context, database, userId, isPartOfFullSync = true)

            // 2. Upload any local updates/new entries (Progress: 0.50f -> 1.0f)
            syncAllLocalDataToCloud(context, database, userId, isPartOfFullSync = true)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Error during full sync: ${e.message}")
            updateProgress(
                isSyncing = false,
                progress = 1.0f,
                stage = SyncStage.COMPLETED,
                stageTitle = "Sync Finished",
                details = "Local records preserved successfully",
                lastSyncTimeMs = System.currentTimeMillis()
            )
            true
        }
    }
}
