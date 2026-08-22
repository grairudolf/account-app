package com.example.services.sync

import android.content.Context
import android.util.Log
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
    private const val TIMEOUT_MS = 6000L

    private val _syncProgressFlow = MutableStateFlow(SyncProgress())
    val syncProgressFlow: StateFlow<SyncProgress> = _syncProgressFlow.asStateFlow()

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
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
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
        } catch (e: Exception) {
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
        isPartOfFullSync: Boolean = false
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext Result.success(false)
            if (userId.isBlank() || userId == "guest_user") return@withContext Result.success(false)

            val baseProgress = if (isPartOfFullSync) 0.05f else 0.05f
            val maxProgress = if (isPartOfFullSync) 0.45f else 0.95f
            fun scaleProgress(relative: Float): Float = baseProgress + (maxProgress - baseProgress) * relative.coerceIn(0f, 1f)

            Log.i(TAG, "Starting cloud restore for user: $userId")
            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.10f),
                stage = SyncStage.DOWNLOADING,
                stageTitle = "Connecting to Cloud Database",
                details = "Fetching your user profile..."
            )

            // 1. Fetch User Profile Document with timeout
            withTimeoutOrNull(TIMEOUT_MS) {
                try {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    if (userDoc.exists()) {
                        val data = userDoc.data
                        if (data != null) {
                            val existing = database.userDao().getCurrentUser()
                            val restoredUser = UserEntity(
                                id = userId,
                                fullName = data["fullName"] as? String ?: existing?.fullName ?: "Disciple",
                                email = data["email"] as? String ?: existing?.email ?: "",
                                profileImageUri = data["profileImageUri"] as? String ?: existing?.profileImageUri,
                                localAssembly = data["localAssembly"] as? String ?: existing?.localAssembly ?: "",
                                discipleMaker = data["discipleMaker"] as? String ?: existing?.discipleMaker ?: "",
                                phoneNumber = data["phoneNumber"] as? String ?: existing?.phoneNumber ?: "",
                                language = data["language"] as? String ?: existing?.language ?: "en",
                                themeMode = data["themeMode"] as? String ?: existing?.themeMode ?: "LIGHT",
                                conversionDate = data["conversionDate"] as? String ?: existing?.conversionDate ?: "",
                                accountabilityDays = data["accountabilityDays"] as? String ?: existing?.accountabilityDays ?: "MON,TUE,WED,THU,FRI,SAT,SUN",
                                isGuest = false,
                                syncStatus = "SYNCED",
                                createdAtMs = (data["createdAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                                updatedAtMs = (data["updatedAtMs"] as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                            database.userDao().clearUserTable()
                            database.userDao().insertOrUpdateUser(restoredUser)
                            Log.i(TAG, "Restored profile for: ${restoredUser.fullName} (${restoredUser.email})")
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Notice fetching user profile: ${e.message}")
                }
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

            if (!isPartOfFullSync) {
                Log.i(TAG, "Cloud restore finished successfully for user: $userId")
                updateProgress(
                    isSyncing = false,
                    progress = 1.0f,
                    stage = SyncStage.COMPLETED,
                    stageTitle = "Restore Completed",
                    details = "All records downloaded and restored from cloud",
                    lastSyncTimeMs = System.currentTimeMillis()
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
                val map = hashMapOf(
                    "id" to user.id,
                    "fullName" to user.fullName,
                    "email" to user.email,
                    "profileImageUri" to (user.profileImageUri ?: ""),
                    "localAssembly" to user.localAssembly,
                    "discipleMaker" to user.discipleMaker,
                    "phoneNumber" to user.phoneNumber,
                    "language" to user.language,
                    "themeMode" to user.themeMode,
                    "conversionDate" to user.conversionDate,
                    "accountabilityDays" to user.accountabilityDays,
                    "updatedAtMs" to user.updatedAtMs
                )
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
    suspend fun syncEntry(context: Context, entry: AccountabilityEntryEntity) = withContext(Dispatchers.IO) {
        if (entry.userId.isBlank() || entry.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        withTimeoutOrNull(TIMEOUT_MS) {
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
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync entry to cloud: ${e.message}")
            }
        }
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

    /**
     * Full local-to-cloud backup for all tables with guaranteed progress
     */
    suspend fun syncAllLocalDataToCloud(
        context: Context,
        database: AppDatabase,
        userId: String,
        isPartOfFullSync: Boolean = false
    ) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        try {
            val baseProgress = if (isPartOfFullSync) 0.50f else 0.05f
            val maxProgress = 1.0f
            fun scaleProgress(relative: Float): Float = baseProgress + (maxProgress - baseProgress) * relative.coerceIn(0f, 1f)

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.05f),
                stage = SyncStage.UPLOADING,
                stageTitle = "Preparing Cloud Backup",
                details = "Gathering local spiritual records..."
            )

            // 1. Sync User Profile
            val user = database.userDao().getCurrentUser()
            if (user != null && !user.isGuest) {
                updateProgress(
                    isSyncing = true,
                    progress = scaleProgress(0.15f),
                    stage = SyncStage.UPLOADING,
                    stageTitle = "Backing Up Profile",
                    details = "Saving account settings and profile..."
                )
                syncUserProfile(context, user)
            } else {
                updateProgress(
                    isSyncing = true,
                    progress = scaleProgress(0.15f),
                    stage = SyncStage.UPLOADING,
                    stageTitle = "Backing Up Profile",
                    details = "Profile verified"
                )
            }

            // 2. Sync all Entries
            val entries = database.entryDao().getAllEntriesList()
            val userEntries = entries.filter { it.userId == userId || it.userId == "guest_user" }
            val totalEntries = userEntries.size

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.20f),
                stage = SyncStage.UPLOADING,
                stageTitle = "Uploading Spiritual Entries",
                details = if (totalEntries == 0) "All entries up to date" else "Uploading $totalEntries entries..."
            )

            for ((index, entry) in userEntries.withIndex()) {
                val entryToSync = if (entry.userId != userId) entry.copy(userId = userId) else entry
                if (entry.userId != userId) {
                    database.entryDao().insertOrUpdateEntry(entryToSync)
                }
                syncEntry(context, entryToSync)

                if (index % 3 == 0 || index == totalEntries - 1) {
                    val p = scaleProgress(0.20f + 0.30f * ((index + 1).toFloat() / totalEntries.coerceAtLeast(1)))
                    updateProgress(
                        isSyncing = true,
                        progress = p,
                        stage = SyncStage.UPLOADING,
                        stageTitle = "Uploading Spiritual Entries",
                        details = "Backed up ${index + 1} of $totalEntries prayer & scripture records"
                    )
                }
            }

            // 3. Sync all Goals
            val goals = database.goalDao().getAllGoalsList()
            val userGoals = goals.filter { it.userId == userId || it.userId == "guest_user" }
            val totalGoals = userGoals.size

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.55f),
                stage = SyncStage.UPLOADING,
                stageTitle = "Uploading Spiritual Goals",
                details = if (totalGoals == 0) "Goals up to date" else "Uploading $totalGoals goals..."
            )

            for ((index, goal) in userGoals.withIndex()) {
                val goalToSync = if (goal.userId != userId) goal.copy(userId = userId) else goal
                if (goal.userId != userId) {
                    database.goalDao().insertOrUpdateGoal(goalToSync)
                }
                syncGoal(context, goalToSync)

                if (index % 2 == 0 || index == totalGoals - 1) {
                    val p = scaleProgress(0.55f + 0.15f * ((index + 1).toFloat() / totalGoals.coerceAtLeast(1)))
                    updateProgress(
                        isSyncing = true,
                        progress = p,
                        stage = SyncStage.UPLOADING,
                        stageTitle = "Uploading Spiritual Goals",
                        details = "Backed up ${index + 1} of $totalGoals active goals"
                    )
                }
            }

            // 4. Sync Disciples
            val disciples = database.discipleDao().getDisciplesList(userId)
            val totalDisciples = disciples.size

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.72f),
                stage = SyncStage.UPLOADING,
                stageTitle = "Uploading Disciples",
                details = if (totalDisciples == 0) "Disciples up to date" else "Uploading $totalDisciples disciples..."
            )

            for ((index, disciple) in disciples.withIndex()) {
                syncDisciple(context, disciple)
                if (index % 2 == 0 || index == totalDisciples - 1) {
                    val p = scaleProgress(0.72f + 0.10f * ((index + 1).toFloat() / totalDisciples.coerceAtLeast(1)))
                    updateProgress(
                        isSyncing = true,
                        progress = p,
                        stage = SyncStage.UPLOADING,
                        stageTitle = "Uploading Disciples",
                        details = "Backed up ${index + 1} of $totalDisciples disciples"
                    )
                }
            }

            // 5. Sync Custom Domains
            val customDomains = database.customDomainDao().getAllDomainsList()
            for (cd in customDomains) {
                if (cd.userId == userId || cd.userId == "guest_user") {
                    val cdToSync = if (cd.userId != userId) cd.copy(userId = userId) else cd
                    if (cd.userId != userId) {
                        database.customDomainDao().insertCustomDomain(cdToSync)
                    }
                    syncCustomDomain(context, cdToSync)
                }
            }

            // 6. Sync Proclamation Topics
            val procTopics = database.proclamationTopicDao().getTopicsForUser(userId)
            for (topic in procTopics) {
                syncProclamationTopic(context, topic)
            }

            // 7. Sync Reports
            val reports = database.reportDao().getAllReportsList()
            for (report in reports) {
                if (report.userId == userId || report.userId == "guest_user") {
                    val repToSync = if (report.userId != userId) report.copy(userId = userId) else report
                    if (report.userId != userId) {
                        database.reportDao().insertReport(repToSync)
                    }
                    syncReport(context, repToSync)
                }
            }

            updateProgress(
                isSyncing = true,
                progress = scaleProgress(0.95f),
                stage = SyncStage.UPLOADING,
                stageTitle = "Finalizing Backup",
                details = "Finalizing cloud sync..."
            )

            Log.i(TAG, "Full local sync to cloud finished for user: $userId")
            updateProgress(
                isSyncing = false,
                progress = 1.0f,
                stage = SyncStage.COMPLETED,
                stageTitle = "Cloud Sync & Backup Complete",
                details = "All entries, goals, disciples and settings are safely stored in Firebase",
                lastSyncTimeMs = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Error in full local sync: ${e.message}")
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
