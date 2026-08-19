package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accountability_entries")
data class AccountabilityEntryEntity(
    @PrimaryKey val id: String, // UUID
    val userId: String,
    val domainId: String, // e.g. "ddewg", "bible_reading", etc.
    val dateIso: String, // "YYYY-MM-DD" local date string
    val timestampMs: Long, // canonical event timestamp
    val timezoneId: String, // device timezone ID
    val durationSeconds: Long = 0L,
    val startTimeIso: String = "",
    val endTimeIso: String = "",
    val notes: String = "",
    val reflection: String = "",

    // Bible Reading Fields
    val bibleVersion: String = "KJV",
    val bibleBook: String = "",
    val startChapter: Int = 0,
    val endChapter: Int = 0,
    val chaptersCount: Int = 0,

    // Prayer Fields
    val prayerType: String = "", // Thanksgiving, Request, 15-Minute Retreat, Bertoua Message, Intercession, Worship, etc.
    val prayerTopicsCount: Int = 0,
    val prayerParticipantsCount: Int = 1,
    val prayerParticipantNames: String = "",

    // Fasting Fields
    val fastingType: String = "", // Complete, Partial
    val fastingStartDateIso: String = "",
    val fastingEndDateIso: String = "",
    val fastingDaysCount: Int = 0,
    val fastingPurpose: String = "",

    // Giving Fields
    val givingAmount: Double = 0.0,
    val givingType: String = "", // Tithe, Offering, Missions, Other
    val givingIncomeReference: Double = 0.0,
    val givingPercentage: Double = 0.0,

    // Disciple Maker Accountability
    val accountabilityFrequency: String = "",
    val areasDiscussed: String = "", // Walk with God, Finances, Ministry, Purity, etc.

    // Christian Literature Reading & Memorization
    val bookTitle: String = "",
    val bookAuthor: String = "",
    val totalPages: Int = 0,
    val startPage: Int = 0,
    val endPage: Int = 0,
    val pagesRead: Int = 0,
    val bookTimesRead: Int = 1,
    val pagesMemorized: Int = 0,
    val litMemChapter: String = "",
    val litMemPassage: String = "",
    val litMemStatus: String = "", // Learning, Memorized, Reviewing

    // Spiritual Retreats Fields
    val retreatFocus: String = "",
    val retreatActivitiesJson: String = "",

    // Bible Memorization
    val bibleMemBook: String = "",
    val bibleMemChapter: Int = 0,
    val bibleMemVerse: String = "",
    val bibleMemStatus: String = "", // Learning, Memorized, Reviewing

    // Soul Winning
    val preachedToCount: Int = 0,
    val convertedCount: Int = 0,
    val waterBaptizedCount: Int = 0,
    val holySpiritBaptizedCount: Int = 0,

    // Proclamation and Importunity
    val proclamationTopic: String = "",
    val proclamationCount: Int = 0,
    val proclamationTarget: Int = 0,

    // Custom Domain Values
    val customValue: String = "",

    val createdAtMs: Long = System.currentTimeMillis(),
    val updatedAtMs: Long = System.currentTimeMillis(),
    val syncStatus: String = "PENDING"
)
