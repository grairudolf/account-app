package com.example.data.repositories

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.BibleMetadata
import com.example.data.local.dao.*
import com.example.data.local.entities.*
import com.example.services.sync.FirestoreSyncManager
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

data class DailyProgressStats(
    val completedDomainsCount: Int,
    val totalActiveDomainsCount: Int,
    val progressPercentage: Int
)

data class BibleStats(
    val totalChaptersRead: Int,
    val completionPercentage: Float,
    val biblesReadCount: Float,
    val booksCompletedCount: Int
)

data class StreakStats(
    val currentStreakDays: Int,
    val longestStreakDays: Int
)

data class SoulWinningStats(
    val totalPreachedTo: Int,
    val totalConverted: Int,
    val totalWaterBaptized: Int,
    val totalHolySpiritBaptized: Int
)

class AccountabilityRepository(
    private val entryDao: EntryDao,
    private val goalDao: GoalDao,
    private val customDomainDao: CustomDomainDao,
    private val reminderDao: ReminderDao,
    private val reportDao: ReportDao,
    private val notificationDao: NotificationDao,
    private val proclamationTopicDao: ProclamationTopicDao,
    private val discipleDao: DiscipleDao? = null,
    private val context: Context? = null
) {
    val allEntriesFlow: Flow<List<AccountabilityEntryEntity>> = entryDao.getAllEntriesFlow()
    val allGoalsFlow: Flow<List<GoalEntity>> = goalDao.getAllGoalsFlow()
    val customDomainsFlow: Flow<List<CustomDomainEntity>> = customDomainDao.getAllCustomDomainsFlow()
    val remindersFlow: Flow<List<ReminderEntity>> = reminderDao.getAllRemindersFlow()
    val reportsFlow: Flow<List<ReportRecordEntity>> = reportDao.getAllReportsFlow()
    val notificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()
    val unreadNotificationCountFlow: Flow<Int> = notificationDao.getUnreadCountFlow()

    fun getDisciplesFlow(userId: String? = null): Flow<List<DiscipleEntity>> {
        return if (!userId.isNullOrBlank()) {
            discipleDao?.getAllDisciples(userId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
        } else {
            discipleDao?.getAllDisciplesListFlow() ?: kotlinx.coroutines.flow.flowOf(emptyList())
        }
    }

    suspend fun saveDisciple(disciple: DiscipleEntity) {
        discipleDao?.insertDisciple(disciple)
        if (context != null && disciple.userId.isNotBlank() && disciple.userId != "guest_user") {
            FirestoreSyncManager.syncDisciple(context, disciple)
        }
    }

    suspend fun updateDisciple(disciple: DiscipleEntity) {
        discipleDao?.updateDisciple(disciple)
        if (context != null && disciple.userId.isNotBlank() && disciple.userId != "guest_user") {
            FirestoreSyncManager.syncDisciple(context, disciple)
        }
    }

    suspend fun deleteDisciple(disciple: DiscipleEntity) {
        discipleDao?.deleteDisciple(disciple)
        if (context != null && disciple.userId.isNotBlank() && disciple.userId != "guest_user") {
            FirestoreSyncManager.deleteDisciple(context, disciple.userId, disciple.id)
        }
    }

    suspend fun deleteDiscipleById(id: String, userId: String = "") {
        discipleDao?.deleteDiscipleById(id)
        if (context != null && userId.isNotBlank() && userId != "guest_user") {
            FirestoreSyncManager.deleteDisciple(context, userId, id)
        }
    }

    fun getProclamationTopicsFlow(userId: String? = null): Flow<List<ProclamationTopicEntity>> {
        return if (!userId.isNullOrBlank()) {
            proclamationTopicDao.getTopicsForUserFlow(userId)
        } else {
            proclamationTopicDao.getAllTopicsFlow()
        }
    }

    suspend fun getProclamationTopics(userId: String = "guest_user"): List<ProclamationTopicEntity> {
        return proclamationTopicDao.getTopicsForUser(userId)
    }

    suspend fun saveProclamationTopic(topic: ProclamationTopicEntity) {
        proclamationTopicDao.insertOrUpdateTopic(topic)
        if (context != null && topic.userId.isNotBlank() && topic.userId != "guest_user") {
            FirestoreSyncManager.syncProclamationTopic(context, topic)
        }
    }

    suspend fun deleteProclamationTopic(topic: ProclamationTopicEntity) {
        proclamationTopicDao.deleteTopic(topic)
        if (context != null && topic.userId.isNotBlank() && topic.userId != "guest_user") {
            FirestoreSyncManager.deleteProclamationTopic(context, topic.userId, topic.id)
        }
    }

    suspend fun recordProclamationSession(entry: AccountabilityEntryEntity) {
        saveEntry(entry)
    }

    suspend fun logNotification(context: android.content.Context, title: String, message: String, type: String = "TRANSACTION") {
        val entity = NotificationEntity(
            id = UUID.randomUUID().toString(),
            title = title,
            message = message,
            type = type,
            timestampMs = System.currentTimeMillis()
        )
        notificationDao.insertNotification(entity)
        try {
            com.example.services.notifications.ReminderNotificationReceiver.showNotification(context, title, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    suspend fun deleteNotification(id: String) {
        notificationDao.deleteNotificationById(id)
    }

    suspend fun clearAllNotifications() {
        notificationDao.clearAllNotifications()
    }

    suspend fun deleteReportRecord(id: String) {
        val existing = reportDao.getReportById(id)
        reportDao.deleteReportById(id)
        if (context != null && existing != null && existing.userId.isNotBlank() && existing.userId != "guest_user") {
            FirestoreSyncManager.deleteReportFromCloud(context, existing.userId, id)
        }
    }

    suspend fun getAllEntriesList(): List<AccountabilityEntryEntity> {
        return entryDao.getAllEntriesList()
    }

    fun getEntriesByDateFlow(dateIso: String): Flow<List<AccountabilityEntryEntity>> {
        return entryDao.getEntriesByDateFlow(dateIso)
    }

    fun getEntriesByDomainFlow(domainId: String): Flow<List<AccountabilityEntryEntity>> {
        return entryDao.getEntriesByDomainFlow(domainId)
    }

    fun searchEntriesFlow(query: String): Flow<List<AccountabilityEntryEntity>> {
        return entryDao.searchEntriesFlow(query)
    }

    suspend fun saveEntry(entry: AccountabilityEntryEntity) {
        val now = System.currentTimeMillis()
        val entryToSave = entry.copy(
            createdAtMs = if (entry.createdAtMs == 0L) now else entry.createdAtMs,
            updatedAtMs = now,
            syncStatus = "PENDING"
        )
        entryDao.insertOrUpdateEntry(entryToSave)
        if (context != null && entryToSave.userId.isNotBlank() && entryToSave.userId != "guest_user") {
            val synced = FirestoreSyncManager.syncEntry(context, entryToSave)
            if (synced) {
                entryDao.updateSyncStatus(entryToSave.id, "SYNCED")
            }
        }
        if (entryToSave.domainId == "proclamation_importunity") {
            // Directly and additively update the topic's cumulative count and duration
            if (entryToSave.proclamationTopic.isNotBlank()) {
                val cleanTopic = entryToSave.proclamationTopic.trim()
                val existing = proclamationTopicDao.findTopicByName(entryToSave.userId, cleanTopic)
                val updatedTopic = if (existing != null) {
                    val newCount = maxOf(existing.cumulativeCount + entryToSave.proclamationCount, entryToSave.proclamationCount)
                    existing.copy(
                        cumulativeCount = newCount,
                        targetCount = if (entryToSave.proclamationTarget > 0) entryToSave.proclamationTarget else existing.targetCount,
                        totalDurationSeconds = existing.totalDurationSeconds + entryToSave.durationSeconds,
                        lastPracticedIso = entryToSave.dateIso,
                        updatedAtMs = now
                    )
                } else {
                    ProclamationTopicEntity(
                        id = UUID.randomUUID().toString(),
                        userId = entryToSave.userId,
                        topic = cleanTopic,
                        cumulativeCount = entryToSave.proclamationCount,
                        targetCount = if (entryToSave.proclamationTarget > 0) entryToSave.proclamationTarget else 100,
                        totalDurationSeconds = entryToSave.durationSeconds,
                        lastPracticedIso = entryToSave.dateIso,
                        createdAtMs = now,
                        updatedAtMs = now
                    )
                }
                proclamationTopicDao.insertOrUpdateTopic(updatedTopic)
                if (context != null && updatedTopic.userId.isNotBlank() && updatedTopic.userId != "guest_user") {
                    FirestoreSyncManager.syncProclamationTopic(context, updatedTopic)
                }
            }
        }
    }

    suspend fun deleteEntry(id: String, userId: String = "") {
        val existing = entryDao.getEntryById(id)
        val resolvedUserId = if (userId.isNotBlank()) userId else (existing?.userId ?: "")
        val wasProclamation = existing?.domainId == "proclamation_importunity"
        val deletedTopic = existing?.proclamationTopic?.trim() ?: ""
        val deletedCount = existing?.proclamationCount ?: 0
        val deletedDuration = existing?.durationSeconds ?: 0L

        entryDao.deleteEntryById(id)
        if (context != null && resolvedUserId.isNotBlank() && resolvedUserId != "guest_user") {
            FirestoreSyncManager.deleteEntry(context, resolvedUserId, id)
        }
        if (wasProclamation && deletedTopic.isNotBlank()) {
            val topicEntity = proclamationTopicDao.findTopicByName(resolvedUserId, deletedTopic)
            if (topicEntity != null) {
                val updatedTopic = topicEntity.copy(
                    cumulativeCount = (topicEntity.cumulativeCount - deletedCount).coerceAtLeast(0),
                    totalDurationSeconds = (topicEntity.totalDurationSeconds - deletedDuration).coerceAtLeast(0),
                    updatedAtMs = System.currentTimeMillis()
                )
                proclamationTopicDao.insertOrUpdateTopic(updatedTopic)
                if (context != null && updatedTopic.userId.isNotBlank() && updatedTopic.userId != "guest_user") {
                    FirestoreSyncManager.syncProclamationTopic(context, updatedTopic)
                }
            }
        }
    }

    suspend fun reconcileProclamationTopics(userId: String = "") {
        try {
            val allUserEntries = entryDao.getAllEntriesList().filter { 
                it.domainId == "proclamation_importunity" && (userId.isBlank() || it.userId == userId || it.userId == "guest_user")
            }
            val existingTopics = proclamationTopicDao.getTopicsForUser(if (userId.isNotBlank()) userId else "guest_user")
            val existingTopicsMap = existingTopics.associateBy { it.topic.trim().lowercase() }.toMutableMap()

            val groupedEntries = allUserEntries.groupBy { it.proclamationTopic.trim().lowercase() }

            groupedEntries.forEach { (topicLower, entries) ->
                if (topicLower.isNotBlank()) {
                    val canonicalTopic = entries.firstOrNull { it.proclamationTopic.isNotBlank() }?.proclamationTopic?.trim() ?: topicLower
                    val totalCount = entries.sumOf { it.proclamationCount }
                    val totalDuration = entries.sumOf { it.durationSeconds }
                    val latestIso = entries.maxOfOrNull { it.dateIso } ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val maxTarget = entries.map { it.proclamationTarget }.filter { it > 0 }.maxOrNull() ?: 100

                    val existing = existingTopicsMap[topicLower]
                    if (existing != null) {
                        if (totalCount > existing.cumulativeCount || totalDuration > existing.totalDurationSeconds) {
                            val updated = existing.copy(
                                topic = canonicalTopic,
                                cumulativeCount = maxOf(existing.cumulativeCount, totalCount),
                                targetCount = if (existing.targetCount > 0) existing.targetCount else maxTarget,
                                totalDurationSeconds = maxOf(existing.totalDurationSeconds, totalDuration),
                                lastPracticedIso = if (existing.lastPracticedIso.isBlank()) latestIso else maxOf(existing.lastPracticedIso, latestIso),
                                updatedAtMs = System.currentTimeMillis()
                            )
                            proclamationTopicDao.insertOrUpdateTopic(updated)
                            if (context != null && updated.userId.isNotBlank() && updated.userId != "guest_user") {
                                FirestoreSyncManager.syncProclamationTopic(context, updated)
                            }
                        }
                    }
                    // Deleted topics are NOT recreated
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveGoal(goal: GoalEntity) {
        goalDao.insertOrUpdateGoal(goal)
        if (context != null && goal.userId.isNotBlank() && goal.userId != "guest_user") {
            FirestoreSyncManager.syncGoal(context, goal)
        }
    }

    suspend fun getGoalById(id: String): GoalEntity? {
        return goalDao.getGoalById(id)
    }

    suspend fun deleteGoal(id: String, userId: String = "") {
        goalDao.deleteGoalById(id)
        if (context != null && userId.isNotBlank() && userId != "guest_user") {
            FirestoreSyncManager.deleteGoal(context, userId, id)
        }
    }

    suspend fun saveCustomDomain(domain: CustomDomainEntity) {
        customDomainDao.insertCustomDomain(domain)
        if (context != null && domain.userId.isNotBlank() && domain.userId != "guest_user") {
            FirestoreSyncManager.syncCustomDomain(context, domain)
        }
    }

    suspend fun deleteCustomDomain(id: String) {
        customDomainDao.deleteCustomDomainById(id)
    }

    suspend fun saveReminder(reminder: ReminderEntity) {
        reminderDao.insertOrUpdateReminder(reminder)
    }

    suspend fun deleteReminder(id: String) {
        reminderDao.deleteReminderById(id)
    }

    suspend fun saveReportRecord(report: ReportRecordEntity) {
        reportDao.insertReport(report)
        if (context != null && report.userId.isNotBlank() && report.userId != "guest_user") {
            FirestoreSyncManager.syncReport(context, report)
        }
    }

    suspend fun clearAllData() {
        entryDao.clearAllEntries()
        goalDao.clearAllGoals()
    }

    suspend fun migrateGuestDataToAccount(newUserId: String) {
        entryDao.migrateUserEntries(newUserId)
        goalDao.migrateUserGoals(newUserId)
        if (context != null && newUserId.isNotBlank() && newUserId != "guest_user") {
            try {
                FirestoreSyncManager.syncAllLocalDataToCloud(context, AppDatabase.getInstance(context), newUserId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Calculators using real database entries

    fun calculateBibleStats(entries: List<AccountabilityEntryEntity>): BibleStats {
        val bibleEntries = entries.filter { it.domainId == "bible_reading" }
        val totalChapters = bibleEntries.sumOf { 
            if (it.chaptersCount > 0) it.chaptersCount 
            else if (it.endChapter >= it.startChapter && it.startChapter > 0) it.endChapter - it.startChapter + 1 
            else 1 
        }
        val completionPercentage = (totalChapters.toFloat() / BibleMetadata.TOTAL_BIBLE_CHAPTERS) * 100f
        val biblesReadCount = totalChapters.toFloat() / BibleMetadata.TOTAL_BIBLE_CHAPTERS

        val booksReadMap = bibleEntries.groupBy { it.bibleBook }
            .mapValues { (_, bookEntries) -> bookEntries.sumOf { it.chaptersCount } }
        
        var completedBooks = 0
        for ((bookName, chaptersRead) in booksReadMap) {
            val book = BibleMetadata.getBook(bookName)
            if (book != null && chaptersRead >= book.totalChapters) {
                completedBooks++
            }
        }

        return BibleStats(
            totalChaptersRead = totalChapters,
            completionPercentage = completionPercentage.coerceAtMost(1000f),
            biblesReadCount = biblesReadCount,
            booksCompletedCount = completedBooks
        )
    }

    fun calculateStreakStats(entries: List<AccountabilityEntryEntity>): StreakStats {
        if (entries.isEmpty()) return StreakStats(0, 0)

        val uniqueDates = entries.map { it.dateIso }.distinct().sortedDescending()
        if (uniqueDates.isEmpty()) return StreakStats(0, 0)

        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val todayIso = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val yesterdayIso = yesterday.format(DateTimeFormatter.ISO_LOCAL_DATE)

        var currentStreak = 0
        var checkDate = if (uniqueDates.contains(todayIso)) today else if (uniqueDates.contains(yesterdayIso)) yesterday else null

        if (checkDate != null) {
            while (true) {
                val iso = checkDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                if (iso != null && uniqueDates.contains(iso)) {
                    currentStreak++
                    checkDate = checkDate?.minusDays(1)
                } else {
                    break
                }
            }
        }

        // Calculate longest streak
        var longestStreak = 0
        var tempStreak = 0
        val sortedAsc = uniqueDates.mapNotNull {
            try { LocalDate.parse(it) } catch (e: Exception) { null }
        }.sorted()
        
        if (sortedAsc.isNotEmpty()) {
            tempStreak = 1
            longestStreak = 1
            for (i in 1 until sortedAsc.size) {
                if (sortedAsc[i] == sortedAsc[i - 1].plusDays(1)) {
                    tempStreak++
                } else if (sortedAsc[i] != sortedAsc[i - 1]) {
                    tempStreak = 1
                }
                if (tempStreak > longestStreak) {
                    longestStreak = tempStreak
                }
            }
        }

        return StreakStats(
            currentStreakDays = currentStreak,
            longestStreakDays = maxOf(currentStreak, longestStreak)
        )
    }

    fun calculateSoulWinningStats(entries: List<AccountabilityEntryEntity>): SoulWinningStats {
        val soulEntries = entries.filter { it.domainId == "soul_winning" }
        return SoulWinningStats(
            totalPreachedTo = soulEntries.sumOf { it.preachedToCount },
            totalConverted = soulEntries.sumOf { it.convertedCount },
            totalWaterBaptized = soulEntries.sumOf { it.waterBaptizedCount },
            totalHolySpiritBaptized = soulEntries.sumOf { it.holySpiritBaptizedCount }
        )
    }

    fun calculateDailyProgress(todayEntries: List<AccountabilityEntryEntity>): DailyProgressStats {
        val uniqueDomainsToday = todayEntries.map { it.domainId }.distinct().size
        val totalActiveDomains = 12 // standard CMFI domains including Retreats
        val percentage = ((uniqueDomainsToday.toFloat() / totalActiveDomains) * 100).toInt().coerceIn(0, 100)
        return DailyProgressStats(
            completedDomainsCount = uniqueDomainsToday,
            totalActiveDomainsCount = totalActiveDomains,
            progressPercentage = percentage
        )
    }

    fun calculateGoalProgress(goal: GoalEntity, entries: List<AccountabilityEntryEntity>): Double {
        val domainEntries = entries.filter { it.domainId == goal.domainId }
        val now = LocalDate.now()
        val relevantEntries = when (goal.frequency.uppercase()) {
            "DAILY" -> {
                val todayIso = now.format(DateTimeFormatter.ISO_LOCAL_DATE)
                domainEntries.filter { it.dateIso == todayIso }
            }
            "WEEKLY" -> {
                val startOfWeek = now.minusDays(now.dayOfWeek.value.toLong() - 1)
                val endOfWeek = startOfWeek.plusDays(6)
                domainEntries.filter { 
                    try {
                        val date = LocalDate.parse(it.dateIso)
                        !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek)
                    } catch (e: Exception) { false }
                }
            }
            "MONTHLY" -> {
                val currentMonth = now.month
                val currentYear = now.year
                domainEntries.filter {
                    try {
                        val date = LocalDate.parse(it.dateIso)
                        date.month == currentMonth && date.year == currentYear
                    } catch (e: Exception) { false }
                }
            }
            "YEARLY", "ANNUAL" -> {
                val currentYear = now.year
                domainEntries.filter {
                    try {
                        val date = LocalDate.parse(it.dateIso)
                        date.year == currentYear
                    } catch (e: Exception) { false }
                }
            }
            else -> domainEntries
        }

        val u = goal.unit.lowercase()
        val tTitle = goal.title.lowercase()

        return when (goal.domainId) {
            "bible_reading" -> {
                if (u.contains("times") || u.contains("whole") || u.contains("fois") || tTitle.contains("whole bible") || tTitle.contains("toute la bible") || tTitle.contains("finish")) {
                    val chCount = relevantEntries.sumOf {
                        if (it.chaptersCount > 0) it.chaptersCount
                        else if (it.endChapter >= it.startChapter && it.startChapter > 0) it.endChapter - it.startChapter + 1
                        else 1
                    }
                    chCount.toDouble() / com.example.data.local.BibleMetadata.TOTAL_BIBLE_CHAPTERS.toDouble()
                } else if (u.contains("%") || u.contains("pourcent")) {
                    val chCount = relevantEntries.sumOf {
                        if (it.chaptersCount > 0) it.chaptersCount
                        else if (it.endChapter >= it.startChapter && it.startChapter > 0) it.endChapter - it.startChapter + 1
                        else 1
                    }
                    ((chCount.toDouble() / com.example.data.local.BibleMetadata.TOTAL_BIBLE_CHAPTERS.toDouble()) * 100.0).coerceAtMost(100.0)
                } else if (u.contains("page")) {
                    relevantEntries.sumOf {
                        if (it.pagesRead > 0) it.pagesRead
                        else {
                            val chs = if (it.chaptersCount > 0) it.chaptersCount
                            else if (it.endChapter >= it.startChapter && it.startChapter > 0) it.endChapter - it.startChapter + 1
                            else 1
                            chs * 3 // Standard average 3 pages per chapter in study Bibles
                        }
                    }.toDouble()
                } else if (u.contains("day") || u.contains("jour")) {
                    relevantEntries.map { it.dateIso }.distinct().size.toDouble()
                } else if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else if (u.contains("min")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0
                } else {
                    relevantEntries.sumOf {
                        if (it.chaptersCount > 0) it.chaptersCount
                        else if (it.endChapter >= it.startChapter && it.startChapter > 0) it.endChapter - it.startChapter + 1
                        else 1
                    }.toDouble()
                }
            }
            "retreats" -> {
                if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else if (u.contains("min")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0
                } else if (u.contains("day") || u.contains("jour")) {
                    relevantEntries.map { it.dateIso }.distinct().size.toDouble()
                } else {
                    relevantEntries.size.toDouble()
                }
            }
            "ddewg" -> {
                if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else if (u.contains("min")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0
                } else if (u.contains("day") || u.contains("jour")) {
                    relevantEntries.map { it.dateIso }.distinct().size.toDouble()
                } else {
                    // Count number of DDEWG encounter days/sessions
                    relevantEntries.size.toDouble()
                }
            }
            "prayer_alone" -> {
                if (u.contains("15") || tTitle.contains("15")) {
                    relevantEntries.count {
                        it.prayerType.contains("15", ignoreCase = true) ||
                        it.notes.contains("15", ignoreCase = true) ||
                        (it.durationSeconds in 800..1200)
                    }.toDouble()
                } else if (u.contains("thanksgiving") || u.contains("remerciement") || tTitle.contains("thanksgiving") || tTitle.contains("action de grâce")) {
                    relevantEntries.filter { it.prayerType.contains("Thanksgiving", ignoreCase = true) || it.notes.contains("thanksgiving", ignoreCase = true) || it.prayerType.contains("Action de gr", ignoreCase = true) }
                        .sumOf { if (it.prayerTopicsCount > 0) it.prayerTopicsCount else 1 }.toDouble()
                } else if (u.contains("request") || u.contains("requête") || tTitle.contains("request") || tTitle.contains("requête")) {
                    relevantEntries.filter { it.prayerType.contains("Request", ignoreCase = true) || it.notes.contains("request", ignoreCase = true) || it.prayerType.contains("Requête", ignoreCase = true) }
                        .sumOf { if (it.prayerTopicsCount > 0) it.prayerTopicsCount else 1 }.toDouble()
                } else if (u.contains("night") || u.contains("vigil") || u.contains("nuit") || tTitle.contains("night", ignoreCase = true) || tTitle.contains("vigil", ignoreCase = true)) {
                    relevantEntries.count { it.prayerType.contains("Night", ignoreCase = true) || it.prayerType.contains("Nuit", ignoreCase = true) || it.notes.contains("night", ignoreCase = true) || it.notes.contains("vigil", ignoreCase = true) }.toDouble()
                } else if (u.contains("topic") || u.contains("sujet")) {
                    relevantEntries.sumOf { if (it.prayerTopicsCount > 0) it.prayerTopicsCount else 1 }.toDouble()
                } else if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else if (u.contains("session") || u.contains("time") || u.contains("fois")) {
                    relevantEntries.size.toDouble()
                } else {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0 // default minutes
                }
            }
            "prayer_with_others" -> {
                if (u.contains("session") || u.contains("séance") || u.contains("time") || u.contains("fois")) {
                    relevantEntries.size.toDouble()
                } else if (u.contains("min")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0
                } else {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0 // default hours
                }
            }
            "proclamation_importunity" -> {
                if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else if (u.contains("min")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0
                } else {
                    relevantEntries.sumOf { it.proclamationCount }.toDouble()
                }
            }
            "christian_lit" -> {
                if (u.contains("book") || u.contains("livre") || tTitle.contains("book") || tTitle.contains("livre")) {
                    relevantEntries.map { it.bookTitle.trim().lowercase() }.filter { it.isNotBlank() }.distinct().size.toDouble()
                } else if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else if (u.contains("min")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0
                } else {
                    relevantEntries.sumOf { it.pagesRead }.toDouble()
                }
            }
            "christian_lit_mem" -> {
                if (u.contains("quote") || u.contains("extrait") || u.contains("citation") || tTitle.contains("quote") || tTitle.contains("citation")) {
                    relevantEntries.count { it.litMemPassage.isNotBlank() || it.notes.isNotBlank() }.toDouble()
                } else {
                    relevantEntries.sumOf { if (it.pagesMemorized > 0) it.pagesMemorized else it.pagesRead }.toDouble()
                }
            }
            "bible_mem" -> {
                if (u.contains("verse") || u.contains("verset")) {
                    relevantEntries.count { it.bibleMemVerse.isNotBlank() || it.notes.isNotBlank() || it.bibleMemChapter > 0 }.coerceAtLeast(relevantEntries.size).toDouble()
                } else if (u.contains("chapter") || u.contains("chapitre")) {
                    relevantEntries.count { it.bibleMemChapter > 0 || it.chaptersCount > 0 }.toDouble()
                } else {
                    relevantEntries.size.toDouble()
                }
            }
            "fasting" -> {
                val filteredFastingEntries = when (goal.fastingType.uppercase()) {
                    "PARTIAL" -> relevantEntries.filter { it.fastingType.contains("Partial", ignoreCase = true) || it.notes.contains("partial", ignoreCase = true) }
                    "COMPLETE" -> relevantEntries.filter { it.fastingType.isBlank() || it.fastingType.contains("Complete", ignoreCase = true) || !it.fastingType.contains("Partial", ignoreCase = true) }
                    else -> relevantEntries
                }
                if (u.contains("hour") || u.contains("heure")) {
                    filteredFastingEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else {
                    filteredFastingEntries.sumOf { it.fastingDaysCount.coerceAtLeast(1) }.toDouble()
                }
            }
            "soul_winning" -> {
                if (u.contains("convert") || u.contains("âme") || u.contains("soul") || tTitle.contains("convert") || tTitle.contains("âme")) {
                    relevantEntries.sumOf { it.convertedCount }.toDouble()
                } else if (u.contains("baptis") || tTitle.contains("baptis")) {
                    relevantEntries.sumOf { it.waterBaptizedCount }.toDouble()
                } else {
                    relevantEntries.sumOf { it.preachedToCount }.toDouble()
                }
            }
            "giving" -> {
                if (u.contains("%") || u.contains("percent") || u.contains("pourcent")) {
                    if (relevantEntries.isNotEmpty()) relevantEntries.map { it.givingPercentage }.average() else 0.0
                } else {
                    relevantEntries.sumOf { it.givingAmount }
                }
            }
            else -> relevantEntries.size.toDouble()
        }
    }
}
