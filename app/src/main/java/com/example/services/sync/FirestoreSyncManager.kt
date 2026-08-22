package com.example.services.sync

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirestoreSyncManager {
    private const val TAG = "FirestoreSyncManager"

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
        userId: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val firestore = getFirestore(context) ?: return@withContext Result.success(false)
            if (userId.isBlank() || userId == "guest_user") return@withContext Result.success(false)

            Log.i(TAG, "Starting cloud restore for user: $userId")

            // 1. Fetch User Profile Document
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
                        createdAtMs = (data["createdAtMs"] as? Long) ?: System.currentTimeMillis(),
                        updatedAtMs = (data["updatedAtMs"] as? Long) ?: System.currentTimeMillis()
                    )
                    database.userDao().clearUserTable()
                    database.userDao().insertOrUpdateUser(restoredUser)
                    Log.i(TAG, "Restored profile for: ${restoredUser.fullName} (${restoredUser.email})")
                }
            }

            // 2. Restore Entries
            val entriesSnapshot = firestore.collection("users").document(userId).collection("entries").get().await()
            for (doc in entriesSnapshot.documents) {
                val d = doc.data ?: continue
                val entry = AccountabilityEntryEntity(
                    id = doc.id,
                    userId = userId,
                    domainId = d["domainId"] as? String ?: "",
                    dateIso = d["dateIso"] as? String ?: "",
                    timestampMs = (d["timestampMs"] as? Long) ?: System.currentTimeMillis(),
                    timezoneId = d["timezoneId"] as? String ?: "UTC",
                    durationSeconds = (d["durationSeconds"] as? Long) ?: 0L,
                    startTimeIso = d["startTimeIso"] as? String ?: "",
                    endTimeIso = d["endTimeIso"] as? String ?: "",
                    notes = d["notes"] as? String ?: "",
                    reflection = d["reflection"] as? String ?: "",
                    bibleVersion = d["bibleVersion"] as? String ?: "KJV",
                    bibleBook = d["bibleBook"] as? String ?: "",
                    startChapter = (d["startChapter"] as? Long)?.toInt() ?: 0,
                    endChapter = (d["endChapter"] as? Long)?.toInt() ?: 0,
                    chaptersCount = (d["chaptersCount"] as? Long)?.toInt() ?: 0,
                    prayerType = d["prayerType"] as? String ?: "",
                    prayerTopicsCount = (d["prayerTopicsCount"] as? Long)?.toInt() ?: 0,
                    prayerParticipantsCount = (d["prayerParticipantsCount"] as? Long)?.toInt() ?: 1,
                    prayerParticipantNames = d["prayerParticipantNames"] as? String ?: "",
                    fastingType = d["fastingType"] as? String ?: "",
                    fastingStartDateIso = d["fastingStartDateIso"] as? String ?: "",
                    fastingEndDateIso = d["fastingEndDateIso"] as? String ?: "",
                    fastingDaysCount = (d["fastingDaysCount"] as? Long)?.toInt() ?: 0,
                    fastingPurpose = d["fastingPurpose"] as? String ?: "",
                    givingAmount = (d["givingAmount"] as? Number)?.toDouble() ?: 0.0,
                    givingType = d["givingType"] as? String ?: "",
                    givingIncomeReference = (d["givingIncomeReference"] as? Number)?.toDouble() ?: 0.0,
                    givingPercentage = (d["givingPercentage"] as? Number)?.toDouble() ?: 0.0,
                    accountabilityFrequency = d["accountabilityFrequency"] as? String ?: "",
                    areasDiscussed = d["areasDiscussed"] as? String ?: "",
                    bookTitle = d["bookTitle"] as? String ?: "",
                    bookAuthor = d["bookAuthor"] as? String ?: "",
                    totalPages = (d["totalPages"] as? Long)?.toInt() ?: 0,
                    startPage = (d["startPage"] as? Long)?.toInt() ?: 0,
                    endPage = (d["endPage"] as? Long)?.toInt() ?: 0,
                    pagesRead = (d["pagesRead"] as? Long)?.toInt() ?: 0,
                    bookTimesRead = (d["bookTimesRead"] as? Long)?.toInt() ?: 1,
                    pagesMemorized = (d["pagesMemorized"] as? Long)?.toInt() ?: 0,
                    litMemChapter = d["litMemChapter"] as? String ?: "",
                    litMemPassage = d["litMemPassage"] as? String ?: "",
                    litMemStatus = d["litMemStatus"] as? String ?: "",
                    retreatFocus = d["retreatFocus"] as? String ?: "",
                    retreatActivitiesJson = d["retreatActivitiesJson"] as? String ?: "",
                    bibleMemBook = d["bibleMemBook"] as? String ?: "",
                    bibleMemChapter = (d["bibleMemChapter"] as? Long)?.toInt() ?: 0,
                    bibleMemVerse = d["bibleMemVerse"] as? String ?: "",
                    bibleMemStatus = d["bibleMemStatus"] as? String ?: "",
                    preachedToCount = (d["preachedToCount"] as? Long)?.toInt() ?: 0,
                    convertedCount = (d["convertedCount"] as? Long)?.toInt() ?: 0,
                    waterBaptizedCount = (d["waterBaptizedCount"] as? Long)?.toInt() ?: 0,
                    holySpiritBaptizedCount = (d["holySpiritBaptizedCount"] as? Long)?.toInt() ?: 0,
                    proclamationTopic = d["proclamationTopic"] as? String ?: "",
                    proclamationCount = (d["proclamationCount"] as? Long)?.toInt() ?: 0,
                    proclamationTarget = (d["proclamationTarget"] as? Long)?.toInt() ?: 0,
                    customValue = d["customValue"] as? String ?: "",
                    createdAtMs = (d["createdAtMs"] as? Long) ?: System.currentTimeMillis(),
                    updatedAtMs = (d["updatedAtMs"] as? Long) ?: System.currentTimeMillis(),
                    syncStatus = "SYNCED"
                )
                database.entryDao().insertOrUpdateEntry(entry)
            }
            Log.i(TAG, "Restored ${entriesSnapshot.size()} entries from cloud")

            // 3. Restore Goals
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
                    createdAtMs = (d["createdAtMs"] as? Long) ?: System.currentTimeMillis(),
                    updatedAtMs = (d["updatedAtMs"] as? Long) ?: System.currentTimeMillis()
                )
                database.goalDao().insertOrUpdateGoal(goal)
            }
            Log.i(TAG, "Restored ${goalsSnapshot.size()} goals from cloud")

            // 4. Restore Disciples
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
                    createdAtMs = (d["createdAtMs"] as? Long) ?: System.currentTimeMillis(),
                    updatedAtMs = (d["updatedAtMs"] as? Long) ?: System.currentTimeMillis()
                )
                database.discipleDao().insertDisciple(disciple)
            }

            // 5. Restore Custom Domains
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
                    createdAtMs = (d["createdAtMs"] as? Long) ?: System.currentTimeMillis()
                )
                database.customDomainDao().insertCustomDomain(customDomain)
            }

            // 6. Restore Proclamation Topics
            val procSnapshot = firestore.collection("users").document(userId).collection("proclamation_topics").get().await()
            for (doc in procSnapshot.documents) {
                val d = doc.data ?: continue
                val proc = ProclamationTopicEntity(
                    id = doc.id,
                    userId = userId,
                    topic = d["topic"] as? String ?: "",
                    cumulativeCount = (d["cumulativeCount"] as? Long)?.toInt() ?: 0,
                    targetCount = (d["targetCount"] as? Long)?.toInt() ?: 100,
                    totalDurationSeconds = (d["totalDurationSeconds"] as? Long) ?: 0L,
                    lastPracticedIso = d["lastPracticedIso"] as? String ?: "",
                    notes = d["notes"] as? String ?: "",
                    createdAtMs = (d["createdAtMs"] as? Long) ?: System.currentTimeMillis(),
                    updatedAtMs = (d["updatedAtMs"] as? Long) ?: System.currentTimeMillis()
                )
                database.proclamationTopicDao().insertOrUpdateTopic(proc)
            }

            // 7. Restore Reports
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
                    generatedAtMs = (d["generatedAtMs"] as? Long) ?: System.currentTimeMillis()
                )
                database.reportDao().insertReport(report)
            }

            Log.i(TAG, "Cloud restore finished successfully for user: $userId")
            Result.success(true)
        } catch (e: com.google.firebase.firestore.FirebaseFirestoreException) {
            Log.i(TAG, "Firestore offline or unreachable during restore (${e.message}). Proceeding with local offline data.")
            Result.success(false)
        } catch (e: Exception) {
            Log.w(TAG, "Notice during cloud restore: ${e.message}")
            Result.success(false)
        }
    }

    /**
     * Pushes user profile to Firestore
     */
    suspend fun syncUserProfile(context: Context, user: UserEntity) = withContext(Dispatchers.IO) {
        if (user.isGuest || user.id.isBlank() || user.id == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
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

    /**
     * Pushes an AccountabilityEntryEntity to Firestore
     */
    suspend fun syncEntry(context: Context, entry: AccountabilityEntryEntity) = withContext(Dispatchers.IO) {
        if (entry.userId.isBlank() || entry.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
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
            Log.d(TAG, "Entry synced to Firestore: ${entry.id}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync entry to cloud: ${e.message}")
        }
    }

    suspend fun deleteEntry(context: Context, userId: String, entryId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        try {
            firestore.collection("users").document(userId).collection("entries").document(entryId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete entry from cloud: ${e.message}")
        }
    }

    /**
     * Pushes a Goal to Firestore
     */
    suspend fun syncGoal(context: Context, goal: GoalEntity) = withContext(Dispatchers.IO) {
        if (goal.userId.isBlank() || goal.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
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
            Log.d(TAG, "Goal synced to Firestore: ${goal.id}")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync goal to cloud: ${e.message}")
        }
    }

    suspend fun deleteGoal(context: Context, userId: String, goalId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        try {
            firestore.collection("users").document(userId).collection("goals").document(goalId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete goal from cloud: ${e.message}")
        }
    }

    /**
     * Pushes a Disciple to Firestore
     */
    suspend fun syncDisciple(context: Context, disciple: DiscipleEntity) = withContext(Dispatchers.IO) {
        if (disciple.userId.isBlank() || disciple.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
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

    suspend fun deleteDisciple(context: Context, userId: String, discipleId: String) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
        try {
            firestore.collection("users").document(userId).collection("disciples").document(discipleId).delete().await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete disciple from cloud: ${e.message}")
        }
    }

    /**
     * Pushes a ProclamationTopic to Firestore
     */
    suspend fun syncProclamationTopic(context: Context, topic: ProclamationTopicEntity) = withContext(Dispatchers.IO) {
        if (topic.userId.isBlank() || topic.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
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

    /**
     * Pushes a CustomDomain to Firestore
     */
    suspend fun syncCustomDomain(context: Context, domain: CustomDomainEntity) = withContext(Dispatchers.IO) {
        if (domain.userId.isBlank() || domain.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
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

    /**
     * Pushes a saved Report Record to Firestore
     */
    suspend fun syncReport(context: Context, report: ReportRecordEntity) = withContext(Dispatchers.IO) {
        if (report.userId.isBlank() || report.userId == "guest_user") return@withContext
        val firestore = getFirestore(context) ?: return@withContext
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

    /**
     * Full local-to-cloud backup for all tables
     */
    suspend fun syncAllLocalDataToCloud(
        context: Context,
        database: AppDatabase,
        userId: String
    ) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || userId == "guest_user") return@withContext
        try {
            val user = database.userDao().getCurrentUser()
            if (user != null && !user.isGuest) {
                syncUserProfile(context, user)
            }
            val entries = database.entryDao().getEntriesInRange(0L, System.currentTimeMillis() + 86400000L)
            for (entry in entries) {
                if (entry.userId == userId) {
                    syncEntry(context, entry)
                }
            }
            val procTopics = database.proclamationTopicDao().getTopicsForUser(userId)
            for (topic in procTopics) {
                syncProclamationTopic(context, topic)
            }
            Log.i(TAG, "Full local sync to cloud finished for user: $userId")
        } catch (e: Exception) {
            Log.w(TAG, "Error in full local sync: ${e.message}")
        }
    }
}
