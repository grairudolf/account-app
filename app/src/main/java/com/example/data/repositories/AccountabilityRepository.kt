package com.example.data.repositories

import com.example.data.local.BibleMetadata
import com.example.data.local.dao.*
import com.example.data.local.entities.*
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
    private val proclamationTopicDao: ProclamationTopicDao
) {
    val allEntriesFlow: Flow<List<AccountabilityEntryEntity>> = entryDao.getAllEntriesFlow()
    val allGoalsFlow: Flow<List<GoalEntity>> = goalDao.getAllGoalsFlow()
    val customDomainsFlow: Flow<List<CustomDomainEntity>> = customDomainDao.getAllCustomDomainsFlow()
    val remindersFlow: Flow<List<ReminderEntity>> = reminderDao.getAllRemindersFlow()
    val reportsFlow: Flow<List<ReportRecordEntity>> = reportDao.getAllReportsFlow()
    val notificationsFlow: Flow<List<NotificationEntity>> = notificationDao.getAllNotificationsFlow()
    val unreadNotificationCountFlow: Flow<Int> = notificationDao.getUnreadCountFlow()

    fun getProclamationTopicsFlow(userId: String = "guest_user"): Flow<List<ProclamationTopicEntity>> {
        return proclamationTopicDao.getTopicsForUserFlow(userId)
    }

    suspend fun getProclamationTopics(userId: String = "guest_user"): List<ProclamationTopicEntity> {
        return proclamationTopicDao.getTopicsForUser(userId)
    }

    suspend fun saveProclamationTopic(topic: ProclamationTopicEntity) {
        proclamationTopicDao.insertOrUpdateTopic(topic)
    }

    suspend fun deleteProclamationTopic(topic: ProclamationTopicEntity) {
        proclamationTopicDao.deleteTopic(topic)
    }

    suspend fun recordProclamationSession(entry: AccountabilityEntryEntity) {
        entryDao.insertOrUpdateEntry(entry)
        // Also update topic cumulative stats
        if (entry.proclamationTopic.isNotBlank()) {
            val existing = proclamationTopicDao.findTopicByName(entry.userId, entry.proclamationTopic)
            if (existing != null) {
                val updated = existing.copy(
                    cumulativeCount = existing.cumulativeCount + entry.proclamationCount,
                    targetCount = if (entry.proclamationTarget > 0) entry.proclamationTarget else existing.targetCount,
                    totalDurationSeconds = existing.totalDurationSeconds + entry.durationSeconds,
                    lastPracticedIso = entry.dateIso,
                    updatedAtMs = System.currentTimeMillis()
                )
                proclamationTopicDao.insertOrUpdateTopic(updated)
            } else {
                val newTopic = ProclamationTopicEntity(
                    id = UUID.randomUUID().toString(),
                    userId = entry.userId,
                    topic = entry.proclamationTopic,
                    cumulativeCount = entry.proclamationCount,
                    targetCount = if (entry.proclamationTarget > 0) entry.proclamationTarget else 100,
                    totalDurationSeconds = entry.durationSeconds,
                    lastPracticedIso = entry.dateIso,
                    updatedAtMs = System.currentTimeMillis()
                )
                proclamationTopicDao.insertOrUpdateTopic(newTopic)
            }
        }
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
        reportDao.deleteReportById(id)
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
        entryDao.insertOrUpdateEntry(entry)
    }

    suspend fun deleteEntry(id: String) {
        entryDao.deleteEntryById(id)
    }

    suspend fun saveGoal(goal: GoalEntity) {
        goalDao.insertOrUpdateGoal(goal)
    }

    suspend fun deleteGoal(id: String) {
        goalDao.deleteGoalById(id)
    }

    suspend fun saveCustomDomain(domain: CustomDomainEntity) {
        customDomainDao.insertCustomDomain(domain)
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
    }

    suspend fun clearAllData() {
        entryDao.clearAllEntries()
        goalDao.clearAllGoals()
    }

    suspend fun migrateGuestDataToAccount(newUserId: String) {
        entryDao.migrateUserEntries(newUserId)
        goalDao.migrateUserGoals(newUserId)
    }

    // Calculators using real database entries

    fun calculateBibleStats(entries: List<AccountabilityEntryEntity>): BibleStats {
        val bibleEntries = entries.filter { it.domainId == "bible_reading" }
        val totalChapters = bibleEntries.sumOf { it.chaptersCount }
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
            else -> domainEntries
        }

        val u = goal.unit.lowercase()

        return when (goal.domainId) {
            "bible_reading" -> {
                if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else if (u.contains("min")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0
                } else {
                    relevantEntries.sumOf { it.chaptersCount }.toDouble()
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
            "ddewg", "prayer_alone", "prayer_with_others" -> {
                if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else if (u.contains("session") || u.contains("time") || u.contains("fois")) {
                    relevantEntries.size.toDouble()
                } else {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0 // in minutes
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
            "christian_lit", "christian_lit_mem" -> {
                if (u.contains("book") || u.contains("livre")) {
                    relevantEntries.map { it.bookTitle }.filter { it.isNotBlank() }.distinct().size.toDouble()
                } else if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else if (u.contains("min")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 60.0
                } else {
                    relevantEntries.sumOf { it.pagesRead }.toDouble()
                }
            }
            "fasting" -> {
                if (u.contains("hour") || u.contains("heure")) {
                    relevantEntries.sumOf { it.durationSeconds }.toDouble() / 3600.0
                } else {
                    relevantEntries.sumOf { it.fastingDaysCount.coerceAtLeast(1) }.toDouble()
                }
            }
            "soul_winning" -> {
                if (u.contains("convert") || u.contains("âme")) {
                    relevantEntries.sumOf { it.convertedCount }.toDouble()
                } else if (u.contains("baptis")) {
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
