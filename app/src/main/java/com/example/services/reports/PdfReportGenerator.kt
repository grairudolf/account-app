package com.example.services.reports

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.data.local.entities.AccountabilityEntryEntity
import com.example.data.local.entities.UserEntity
import java.io.File
import java.io.FileOutputStream
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

object PdfReportGenerator {

    fun formatDuration(totalSeconds: Long, isFrench: Boolean = false): String {
        val totalMinutes = totalSeconds / 60
        if (totalMinutes < 60) {
            val minUnit = if (isFrench) "min" else "mins"
            return "$totalMinutes $minUnit"
        }
        val hours = totalMinutes / 60
        val remainingMinutes = totalMinutes % 60
        val hrUnit = if (isFrench) (if (hours > 1) "heures" else "heure") else (if (hours > 1) "hrs" else "hr")
        val minUnit = if (isFrench) "min" else "mins"
        return if (remainingMinutes > 0) {
            "$hours $hrUnit $remainingMinutes $minUnit"
        } else {
            "$hours $hrUnit"
        }
    }

    fun formatDurationShort(totalSeconds: Long, isFrench: Boolean = false): String {
        val totalMinutes = totalSeconds / 60
        if (totalMinutes < 60) {
            return "${totalMinutes}m"
        }
        val hours = totalMinutes / 60
        val remainingMinutes = totalMinutes % 60
        return if (remainingMinutes > 0) "${hours}h ${remainingMinutes}m" else "${hours}h"
    }

    fun generatePdfReport(
        context: Context,
        user: UserEntity?,
        reportType: String, // "DAILY", "WEEKLY", "MONTHLY", "CUSTOM"
        dateRangeLabel: String,
        entries: List<AccountabilityEntryEntity>,
        isFrench: Boolean = false
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val locale = if (isFrench) Locale.FRENCH else Locale.ENGLISH

        var y = 0f

        // 1. Premium Header Bar (Deep Navy with subtle gradient accent)
        paint.color = Color.parseColor("#0F2942")
        canvas.drawRect(0f, 0f, 595f, 95f, paint)

        // Accent bottom line on header
        paint.color = Color.parseColor("#D97706") // Warm Gold
        canvas.drawRect(0f, 92f, 595f, 95f, paint)

        // Draw App Logo in Header
        try {
            val logoBmp = BitmapFactory.decodeResource(context.resources, com.example.R.drawable.app_logo)
            if (logoBmp != null) {
                val logoRect = RectF(520f, 16f, 576f, 72f)
                val bgPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.WHITE
                }
                canvas.drawRoundRect(logoRect, 10f, 10f, bgPaint)
                canvas.drawBitmap(logoBmp, null, logoRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Header Title & Ministry
        paint.color = Color.WHITE
        paint.textSize = 19f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val headerTitle = if (isFrench) "RAPPORT DE REDEVABILITÉ SPIRITUELLE" else "CMFI SPIRITUAL ACCOUNTABILITY REPORT"
        canvas.drawText(headerTitle, 28f, 42f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.parseColor("#E2E8F0")
        val subTitle = if (isFrench) "Communauté Missionnaire Chrétienne Internationale (CMFI)" else "Christian Missionary Fellowship International (CMFI)"
        canvas.drawText(subTitle, 28f, 62f, paint)

        val mottoText = if (isFrench) "« Veillez et priez, afin que vous ne tombiez pas en tentation »" else "\"Watch and pray, that ye enter not into temptation\""
        paint.textSize = 8.5f
        paint.color = Color.parseColor("#FCD34D") // Soft Gold
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(mottoText, 28f, 78f, paint)

        y = 115f

        // 2. Disciple & Disciple Maker Profile Card
        val cardRect = RectF(28f, y, 567f, y + 68f)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F1F5F9") // Slate-50 background
        canvas.drawRoundRect(cardRect, 10f, 10f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#CBD5E1")
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(cardRect, 10f, 10f, paint)

        paint.style = Paint.Style.FILL

        // Disciple Name
        val rawName = user?.fullName?.trim()
        val discipleName = if (!rawName.isNullOrEmpty()) rawName else if (isFrench) "Disciple du Seigneur" else "Disciple of the Lord"
        paint.color = Color.parseColor("#0F2942")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${if (isFrench) "Disciple :" else "Disciple:"} $discipleName", 40f, y + 22f, paint)

        // Assembly
        val assemblyName = user?.localAssembly?.trim()
        val assemblyDisplay = if (!assemblyName.isNullOrEmpty()) assemblyName else if (isFrench) "Assemblée Locale Non Spécifiée" else "Local Assembly Not Set"
        paint.color = Color.parseColor("#475569")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("${if (isFrench) "Assemblée :" else "Assembly:"} $assemblyDisplay", 40f, y + 42f, paint)

        // Period
        val periodTypeTrans = when (reportType) {
            "DAILY" -> if (isFrench) "QUOTIDIEN" else "DAILY"
            "WEEKLY" -> if (isFrench) "HEBDOMADAIRE" else "WEEKLY"
            "MONTHLY" -> if (isFrench) "MENSUEL" else "MONTHLY"
            "CUSTOM" -> if (isFrench) "PERSONNALISÉ" else "CUSTOM"
            else -> reportType
        }
        val periodText = "${if (isFrench) "Période :" else "Period:"} $periodTypeTrans ($dateRangeLabel)"
        canvas.drawText(periodText, 40f, y + 58f, paint)

        // Disciple Maker (Right column of profile card)
        val rawMaker = user?.discipleMaker?.trim()
        val hasMaker = !rawMaker.isNullOrEmpty()
        val makerDisplay = if (hasMaker) rawMaker!! else "________________________"
        paint.color = Color.parseColor("#0F2942")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "Faiseur de Disciples :" else "Disciple Maker:", 320f, y + 22f, paint)

        paint.color = if (hasMaker) Color.parseColor("#1E3A8A") else Color.DKGRAY
        paint.textSize = 11f
        paint.typeface = if (hasMaker) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        canvas.drawText(makerDisplay, 320f, y + 42f, paint)

        y += 82f

        // 3. Aggregate Domain Metrics Calculation for ALL 13 Domains
        val ddewgEntries = entries.filter { it.domainId == "ddewg" }
        val prayerAloneEntries = entries.filter { it.domainId == "prayer_alone" }
        val prayerGroupEntries = entries.filter { it.domainId == "prayer_with_others" }
        val allPrayerEntries = entries.filter { it.domainId.startsWith("prayer") }
        val bibleEntries = entries.filter { it.domainId == "bible_reading" }
        val bibleMemEntries = entries.filter { it.domainId == "bible_mem" }
        val litEntries = entries.filter { it.domainId == "christian_lit" }
        val litMemEntries = entries.filter { it.domainId == "christian_lit_mem" }
        val proclamationEntries = entries.filter { it.domainId == "proclamation_importunity" }
        val soulEntries = entries.filter { it.domainId == "soul_winning" }
        val discipleshipEntries = entries.filter { it.domainId == "making_disciples" || it.domainId == "accountability" }
        val fastingEntries = entries.filter { it.domainId == "fasting" }
        val givingEntries = entries.filter { it.domainId == "giving" }
        val retreatEntries = entries.filter { it.domainId == "retreats" }

        // Specific Metrics across all 13 domains:
        val ddewgCount = ddewgEntries.size
        val ddewgTimeSecs = ddewgEntries.sumOf { it.durationSeconds }

        val prayerAloneTimeSecs = prayerAloneEntries.sumOf { it.durationSeconds }
        val prayerGroupTimeSecs = prayerGroupEntries.sumOf { it.durationSeconds }
        val groupPrayerSessions = prayerGroupEntries.size
        val totalGroupParticipants = prayerGroupEntries.sumOf { it.prayerParticipantsCount }
        val totalPrayerTimeSecs = allPrayerEntries.sumOf { it.durationSeconds }

        // Thanksgiving & Requests
        val thanksgivingCount = prayerAloneEntries.filter {
            it.prayerType.contains("Thanksgiving", ignoreCase = true) || it.notes.contains("Thanksgiving", ignoreCase = true) || it.notes.contains("Grâce", ignoreCase = true)
        }.sumOf { if (it.prayerTopicsCount > 0) it.prayerTopicsCount else 1 }

        val requestCount = prayerAloneEntries.filter {
            it.prayerType.contains("Request", ignoreCase = true) || it.notes.contains("Request", ignoreCase = true) || it.notes.contains("Requête", ignoreCase = true)
        }.sumOf { if (it.prayerTopicsCount > 0) it.prayerTopicsCount else 1 }

        val retreats15MinCount = prayerAloneEntries.count {
            it.prayerType.contains("15", ignoreCase = true) || it.notes.contains("15-Min", ignoreCase = true) || it.notes.contains("15 Min", ignoreCase = true)
        }
        val bertouaPrayedCount = prayerAloneEntries.count {
            it.prayerType.contains("Bertoua", ignoreCase = true) || it.notes.contains("Bertoua", ignoreCase = true)
        }

        // Word & Scripture
        val totalBibleChapters = bibleEntries.sumOf { it.chaptersCount }
        val bibleTimeSecs = bibleEntries.sumOf { it.durationSeconds }
        val bibleMemVerses = bibleMemEntries.sumOf { if (it.chaptersCount > 0) it.chaptersCount else 1 }

        // Literature & Memorization
        val totalLitPages = litEntries.sumOf { it.pagesRead }
        val litTimeSecs = litEntries.sumOf { it.durationSeconds }
        val booksReadMap = litEntries.filter { it.bookTitle.isNotBlank() }
            .groupBy { it.bookTitle.trim() }
            .mapValues { group -> group.value.sumOf { it.pagesRead } }
        val litMemQuotes = litMemEntries.sumOf { if (it.pagesRead > 0) it.pagesRead else 1 }

        // Ministry & Evangelism
        val sPreached = soulEntries.sumOf { it.preachedToCount }
        val sConverts = soulEntries.sumOf { it.convertedCount }
        val disciplesMentored = discipleshipEntries.sumOf { if (it.prayerParticipantsCount > 0) it.prayerParticipantsCount else 1 }

        // Fasting & Giving
        val fDays = fastingEntries.sumOf { it.fastingDaysCount.coerceAtLeast(1) }
        val gAmountXAF = givingEntries.sumOf { it.givingAmount }
        val pCount = proclamationEntries.sumOf { it.proclamationCount }
        val pTimeSecs = proclamationEntries.sumOf { it.durationSeconds }

        // Retreats
        val retreatCount = retreatEntries.size
        val retreatTimeSecs = retreatEntries.sumOf { it.durationSeconds }

        // Total Time with God
        val totalTimeWithGodSecs = ddewgTimeSecs + totalPrayerTimeSecs + bibleTimeSecs + litTimeSecs + pTimeSecs + retreatTimeSecs

        // 4. Section Title: Domain Summary & Key Metrics
        paint.color = Color.parseColor("#0F2942")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val summaryTitle = if (isFrench) "RÉSUMÉ DES 13 DISCIPLINES SPIRITUELLES" else "13 SPIRITUAL DISCIPLINES SUMMARY"
        canvas.drawText(summaryTitle, 28f, y + 10f, paint)

        // Time with God Badge
        val timeWithGodLabel = "${if (isFrench) "Temps Total Passé avec Dieu :" else "Total Time with God:"} ${formatDuration(totalTimeWithGodSecs, isFrench)}"
        paint.color = Color.parseColor("#15803D")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(timeWithGodLabel, 290f, y + 10f, paint)

        y += 18f

        // Draw 3 High-Level Pillar Cards
        val card1 = RectF(28f, y, 200f, y + 122f)
        val card2 = RectF(210f, y, 382f, y + 122f)
        val card3 = RectF(392f, y, 567f, y + 122f)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(card1, 8f, 8f, paint)
        canvas.drawRoundRect(card2, 8f, 8f, paint)
        canvas.drawRoundRect(card3, 8f, 8f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(card1, 8f, 8f, paint)
        canvas.drawRoundRect(card2, 8f, 8f, paint)
        canvas.drawRoundRect(card3, 8f, 8f, paint)

        paint.style = Paint.Style.FILL

        // Pillar 1: COMMUNION & PRAYER
        var cy = y + 14f
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "COMMUNION & PRIÈRE" else "COMMUNION & PRAYER", 34f, cy, paint)
        cy += 13f

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("• DREQD : $ddewgCount (${formatDurationShort(ddewgTimeSecs, isFrench)})", 34f, cy, paint)
        cy += 12f
        canvas.drawText("• Prière Seul : ${formatDurationShort(prayerAloneTimeSecs, isFrench)}", 34f, cy, paint)
        cy += 12f
        canvas.drawText("• Grâce/Requêtes : $thanksgivingCount / $requestCount", 34f, cy, paint)
        cy += 12f
        canvas.drawText("• Prière Groupe : $groupPrayerSessions sess ($totalGroupParticipants p)", 34f, cy, paint)
        cy += 12f
        canvas.drawText("• Proclamations : $pCount (${formatDurationShort(pTimeSecs, isFrench)})", 34f, cy, paint)
        cy += 12f
        canvas.drawText("• Retraites : $retreatCount (${formatDurationShort(retreatTimeSecs, isFrench)})", 34f, cy, paint)

        // Pillar 2: LA PAROLE & LITTÉRATURE
        cy = y + 14f
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "LA PAROLE & LITTÉRATURE" else "THE WORD & LITERATURE", 216f, cy, paint)
        cy += 13f

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("• Bible : $totalBibleChapters ch (${formatDurationShort(bibleTimeSecs, isFrench)})", 216f, cy, paint)
        cy += 12f
        canvas.drawText("• Mém. Bible : $bibleMemVerses versets", 216f, cy, paint)
        cy += 12f
        canvas.drawText("• Littérature : $totalLitPages p (${formatDurationShort(litTimeSecs, isFrench)})", 216f, cy, paint)
        cy += 12f
        canvas.drawText("• Mém. Lit. : $litMemQuotes citations", 216f, cy, paint)
        cy += 12f
        val topBook = booksReadMap.keys.firstOrNull()?.take(22) ?: "-"
        canvas.drawText("• Livre : $topBook", 216f, cy, paint)
        cy += 12f
        canvas.drawText("• Bertoua/15-Min : $bertouaPrayedCount / $retreats15MinCount", 216f, cy, paint)

        // Pillar 3: MINISTÈRE & SERVICE
        cy = y + 14f
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "MINISTÈRE & INTENDANCE" else "MINISTRY & STEWARDSHIP", 398f, cy, paint)
        cy += 13f

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("• Évangélisation : $sPreached prêchés", 398f, cy, paint)
        cy += 12f
        canvas.drawText("• Convertis : $sConverts âmes", 398f, cy, paint)
        cy += 12f
        canvas.drawText("• Disciples Suivis : $disciplesMentored", 398f, cy, paint)
        cy += 12f
        canvas.drawText("• Jeûne : $fDays jours", 398f, cy, paint)
        cy += 12f
        canvas.drawText("• Offrandes : $gAmountXAF XAF", 398f, cy, paint)

        y += 132f

        // 5. If Report is WEEKLY, draw Day Breakdown Table
        if (reportType == "WEEKLY") {
            paint.color = Color.parseColor("#0F2942")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val dayBreakdownTitle = if (isFrench) "RÉPARTITION JOURNALIÈRE DE LA SEMAINE" else "WEEKLY DAILY BREAKDOWN"
            canvas.drawText(dayBreakdownTitle, 28f, y, paint)
            y += 12f

            // Table Header
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#1565C0")
            canvas.drawRect(28f, y, 567f, y + 18f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(if (isFrench) "Jour" else "Day", 35f, y + 13f, paint)
            canvas.drawText(if (isFrench) "DREQD" else "DDEWG", 120f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Prière Seul" else "Prayer Alone", 185f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Bible" else "Bible", 280f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Littérature" else "Lit. Pages", 365f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Temps avec Dieu" else "Time with God", 460f, y + 13f, paint)

            y += 18f

            val daysOfWeek = DayOfWeek.values()
            for ((idx, day) in daysOfWeek.withIndex()) {
                val dayEntries = entries.filter {
                    try {
                        LocalDate.parse(it.dateIso).dayOfWeek == day
                    } catch (e: Exception) { false }
                }
                val dayName = day.getDisplayName(TextStyle.FULL, locale).lowercase().replaceFirstChar { it.uppercase() }
                val dCount = dayEntries.count { it.domainId == "ddewg" }
                val dSecs = dayEntries.filter { it.domainId == "ddewg" }.sumOf { it.durationSeconds }
                val pSecs = dayEntries.filter { it.domainId == "prayer_alone" }.sumOf { it.durationSeconds }
                val bChaps = dayEntries.filter { it.domainId == "bible_reading" }.sumOf { it.chaptersCount }
                val lPages = dayEntries.filter { it.domainId == "christian_lit" || it.domainId == "christian_lit_mem" }.sumOf { it.pagesRead }
                val godSecs = dayEntries.filter { it.domainId in listOf("ddewg", "prayer_alone", "prayer_with_others", "bible_reading", "christian_lit", "proclamation_importunity", "retreats") }.sumOf { it.durationSeconds }

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
                canvas.drawRect(28f, y, 567f, y + 16f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E2E8F0")
                paint.strokeWidth = 0.5f
                canvas.drawRect(28f, y, 567f, y + 16f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#1E293B")
                paint.textSize = 8f
                paint.typeface = Typeface.DEFAULT

                canvas.drawText(dayName, 35f, y + 12f, paint)
                canvas.drawText(if (dCount > 0) "$dCount (${formatDurationShort(dSecs, isFrench)})" else "-", 120f, y + 12f, paint)
                canvas.drawText(if (pSecs > 0) formatDurationShort(pSecs, isFrench) else "-", 185f, y + 12f, paint)
                canvas.drawText(if (bChaps > 0) "$bChaps ch" else "-", 280f, y + 12f, paint)
                canvas.drawText(if (lPages > 0) "$lPages p" else "-", 365f, y + 12f, paint)
                canvas.drawText(if (godSecs > 0) formatDuration(godSecs, isFrench) else "-", 460f, y + 12f, paint)

                y += 16f
            }
            y += 10f
        } else if (reportType == "MONTHLY") {
            paint.color = Color.parseColor("#0F2942")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val weekBreakdownTitle = if (isFrench) "RÉPARTITION PAR SEMAINE DU MOIS" else "MONTHLY WEEKLY BREAKDOWN"
            canvas.drawText(weekBreakdownTitle, 28f, y, paint)
            y += 12f

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#1565C0")
            canvas.drawRect(28f, y, 567f, y + 18f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(if (isFrench) "Semaine" else "Week", 35f, y + 13f, paint)
            canvas.drawText(if (isFrench) "DREQD" else "DDEWG", 125f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Prière Seul" else "Prayer Alone", 195f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Bible" else "Bible", 285f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Littérature" else "Lit. Pages", 370f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Temps avec Dieu" else "Time with God", 460f, y + 13f, paint)

            y += 18f

            val weekRanges = listOf(
                Pair("Week 1 (1-7)", 1..7),
                Pair("Week 2 (8-14)", 8..14),
                Pair("Week 3 (15-21)", 15..21),
                Pair("Week 4 (22-28)", 22..28),
                Pair("Week 5 (29-31)", 29..31)
            )

            for ((idx, weekPair) in weekRanges.withIndex()) {
                val label = if (isFrench) weekPair.first.replace("Week", "Semaine") else weekPair.first
                val weekEntries = entries.filter {
                    try {
                        val d = LocalDate.parse(it.dateIso)
                        d.dayOfMonth in weekPair.second
                    } catch (e: Exception) { false }
                }
                val dCount = weekEntries.count { it.domainId == "ddewg" }
                val dSecs = weekEntries.filter { it.domainId == "ddewg" }.sumOf { it.durationSeconds }
                val pSecs = weekEntries.filter { it.domainId == "prayer_alone" }.sumOf { it.durationSeconds }
                val bChaps = weekEntries.filter { it.domainId == "bible_reading" }.sumOf { it.chaptersCount }
                val lPages = weekEntries.filter { it.domainId == "christian_lit" || it.domainId == "christian_lit_mem" }.sumOf { it.pagesRead }
                val godSecs = weekEntries.filter { it.domainId in listOf("ddewg", "prayer_alone", "prayer_with_others", "bible_reading", "christian_lit", "proclamation_importunity", "retreats") }.sumOf { it.durationSeconds }

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
                canvas.drawRect(28f, y, 567f, y + 16f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E2E8F0")
                paint.strokeWidth = 0.5f
                canvas.drawRect(28f, y, 567f, y + 16f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#1E293B")
                paint.textSize = 8f
                paint.typeface = Typeface.DEFAULT

                canvas.drawText(label, 35f, y + 12f, paint)
                canvas.drawText(if (dCount > 0) "$dCount (${formatDurationShort(dSecs, isFrench)})" else "-", 125f, y + 12f, paint)
                canvas.drawText(if (pSecs > 0) formatDurationShort(pSecs, isFrench) else "-", 195f, y + 12f, paint)
                canvas.drawText(if (bChaps > 0) "$bChaps ch" else "-", 285f, y + 12f, paint)
                canvas.drawText(if (lPages > 0) "$lPages p" else "-", 370f, y + 12f, paint)
                canvas.drawText(if (godSecs > 0) formatDuration(godSecs, isFrench) else "-", 460f, y + 12f, paint)

                y += 16f
            }
            y += 10f
        }

        // 6. Detailed Activity Table / Domain Matrix Table
        if (reportType == "WEEKLY" || reportType == "MONTHLY") {
            // Draw Domain Matrix Table Header
            paint.color = Color.parseColor("#0F2942")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val matrixTitle = if (isFrench) "BILAN SYNTHÉTIQUE PAR DISCIPLINE SPIRITUELLE" else "SYNTHETIC BREAKDOWN BY SPIRITUAL DISCIPLINE"
            canvas.drawText(matrixTitle, 28f, y, paint)
            y += 12f

            fun drawDomainMatrixHeader(c: Canvas, startY: Float) {
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#0F2942")
                c.drawRect(28f, startY, 567f, startY + 20f, paint)

                paint.color = Color.WHITE
                paint.textSize = 8.5f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                c.drawText(if (isFrench) "Discipline Spirituelle" else "Spiritual Discipline", 35f, startY + 14f, paint)
                c.drawText(if (isFrench) "Volume Total & Temps" else "Total Volume & Duration", 180f, startY + 14f, paint)
                c.drawText(if (isFrench) "Régularité" else "Consistency", 330f, startY + 14f, paint)
                c.drawText(if (isFrench) "Détails & Faits Marquants" else "Key Highlights & Details", 410f, startY + 14f, paint)
            }

            drawDomainMatrixHeader(canvas, y)
            y += 20f

            val domainDefinitions = listOf(
                "ddewg" to if (isFrench) "DREQD (DDEWG)" else "DDEWG",
                "bible_reading" to if (isFrench) "Lecture Biblique" else "Bible Reading",
                "bible_mem" to if (isFrench) "Mémorisation Biblique" else "Bible Memorization",
                "prayer_alone" to if (isFrench) "Prière Seul" else "Prayer Alone",
                "prayer_with_others" to if (isFrench) "Prière en Groupe" else "Prayer With Others",
                "proclamation_importunity" to if (isFrench) "Proclamation & Importunité" else "Proclamation & Importunity",
                "christian_lit" to if (isFrench) "Littérature Chrétienne" else "Christian Literature",
                "christian_lit_mem" to if (isFrench) "Mémorisation Littérature" else "Literature Memorization",
                "soul_winning" to if (isFrench) "Gagnagisme d'Âmes" else "Soul Winning",
                "making_disciples" to if (isFrench) "Faire des Disciples" else "Making Disciples",
                "fasting" to if (isFrench) "Jeûne" else "Fasting",
                "giving" to if (isFrench) "Offrandes & Dîmes" else "Giving to God",
                "retreats" to if (isFrench) "Retraites Spirituelles" else "Spiritual Retreats"
            )

            var activeCanvas = canvas
            var activePage = page
            var currentPageNum = 1

            val totalDays = if (reportType == "WEEKLY") 7 else 30

            for ((idx, domainPair) in domainDefinitions.withIndex()) {
                val domId = domainPair.first
                val domTitle = domainPair.second
                val domEntries = entries.filter { it.domainId == domId || (domId == "making_disciples" && it.domainId == "accountability") }

                if (y > 745f) {
                    drawFooter(activeCanvas, currentPageNum, user, isFrench)
                    document.finishPage(activePage)

                    currentPageNum++
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
                    activePage = document.startPage(newPageInfo)
                    activeCanvas = activePage.canvas

                    y = 35f
                    drawDomainMatrixHeader(activeCanvas, y)
                    y += 20f
                }

                val rowHeight = 20f
                val activeDaysCount = domEntries.map { it.dateIso }.distinct().size

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
                activeCanvas.drawRect(28f, y, 567f, y + rowHeight, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E2E8F0")
                paint.strokeWidth = 0.5f
                activeCanvas.drawRect(28f, y, 567f, y + rowHeight, paint)

                activeCanvas.drawLine(175f, y, 175f, y + rowHeight, paint)
                activeCanvas.drawLine(325f, y, 325f, y + rowHeight, paint)
                activeCanvas.drawLine(405f, y, 405f, y + rowHeight, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#1E293B")
                paint.textSize = 8f
                paint.typeface = Typeface.DEFAULT

                // Column 1: Title
                activeCanvas.drawText(domTitle, 33f, y + 13f, paint)

                // Column 2: Volume & Time
                val volSummary = when (domId) {
                    "ddewg" -> "${domEntries.size} enc (${formatDurationShort(domEntries.sumOf { it.durationSeconds }, isFrench)})"
                    "bible_reading" -> "${domEntries.sumOf { it.chaptersCount }} ch (${formatDurationShort(domEntries.sumOf { it.durationSeconds }, isFrench)})"
                    "bible_mem" -> "${domEntries.sumOf { if (it.chaptersCount > 0) it.chaptersCount else 1 }} v"
                    "prayer_alone" -> formatDuration(domEntries.sumOf { it.durationSeconds }, isFrench)
                    "prayer_with_others" -> "${domEntries.size} sess (${formatDurationShort(domEntries.sumOf { it.durationSeconds }, isFrench)})"
                    "proclamation_importunity" -> "${domEntries.sumOf { it.proclamationCount }}x (${formatDurationShort(domEntries.sumOf { it.durationSeconds }, isFrench)})"
                    "christian_lit" -> "${domEntries.sumOf { it.pagesRead }} p (${formatDurationShort(domEntries.sumOf { it.durationSeconds }, isFrench)})"
                    "christian_lit_mem" -> "${domEntries.sumOf { if (it.pagesRead > 0) it.pagesRead else 1 }} cit."
                    "soul_winning" -> "${domEntries.sumOf { it.preachedToCount }} prêchés, ${domEntries.sumOf { it.convertedCount }} conv."
                    "making_disciples" -> "${domEntries.sumOf { if (it.prayerParticipantsCount > 0) it.prayerParticipantsCount else 1 }} disc."
                    "fasting" -> "${domEntries.sumOf { it.fastingDaysCount.coerceAtLeast(1) }} j"
                    "giving" -> "${domEntries.sumOf { it.givingAmount }} XAF"
                    "retreats" -> "${domEntries.size} sess (${formatDurationShort(domEntries.sumOf { it.durationSeconds }, isFrench)})"
                    else -> "${domEntries.size} rec"
                }
                activeCanvas.drawText(if (domEntries.isNotEmpty()) volSummary else "-", 180f, y + 13f, paint)

                // Column 3: Consistency
                val consistencyText = if (domEntries.isNotEmpty()) "$activeDaysCount/$totalDays j (${(activeDaysCount * 100 / totalDays)}%)" else "-"
                activeCanvas.drawText(consistencyText, 330f, y + 13f, paint)

                // Column 4: Highlights / Notes
                val lastNote = domEntries.firstOrNull { it.notes.isNotBlank() }?.notes ?: domEntries.firstOrNull()?.bookTitle ?: domEntries.firstOrNull()?.prayerType ?: "-"
                activeCanvas.drawText(lastNote.take(30), 410f, y + 13f, paint)

                y += rowHeight
            }

            drawFooter(activeCanvas, currentPageNum, user, isFrench)
            document.finishPage(activePage)
        } else {
            // DAILY or CUSTOM report: render raw entry log table
            fun drawTableHeader(c: Canvas, startY: Float) {
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#0F2942")
                c.drawRect(28f, startY, 567f, startY + 20f, paint)

                paint.color = Color.WHITE
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

                val hDate = if (isFrench) "Date (Jour)" else "Date (Day)"
                val hActivity = if (isFrench) "Activité" else "Activity"
                val hTimeSpan = if (isFrench) "Plage Horaire" else "Time span"
                val hDuration = if (isFrench) "Durée" else "Duration"
                val hNotes = if (isFrench) "Notes & Réflexions" else "Notes & reflection"

                c.drawText(hDate, 32f, startY + 14f, paint)
                c.drawText(hActivity, 125f, startY + 14f, paint)
                c.drawText(hTimeSpan, 280f, startY + 14f, paint)
                c.drawText(hDuration, 355f, startY + 14f, paint)
                c.drawText(hNotes, 415f, startY + 14f, paint)
            }

            if (y > 700f) {
                drawFooter(canvas, 1, user, isFrench)
                document.finishPage(page)

                val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, 2).create()
                val page2 = document.startPage(newPageInfo)
                val activeCanvas = page2.canvas
                y = 35f
                drawTableHeader(activeCanvas, y)
                y += 20f
                renderEntriesLoop(document, page2, activeCanvas, 2, y, entries, user, isFrench)
            } else {
                drawTableHeader(canvas, y)
                y += 20f
                renderEntriesLoop(document, page, canvas, 1, y, entries, user, isFrench)
            }
        }

        val reportDir = File(context.filesDir, "reports")
        if (!reportDir.exists()) reportDir.mkdirs()

        val pdfFile = File(reportDir, "CMFI_Report_${reportType}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        if (text.isBlank()) return listOf("-")
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine)
                currentLine = word
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine)
        return if (lines.isEmpty()) listOf(text) else lines
    }

    private fun formatDateWithDay(dateIso: String, isFrench: Boolean): String {
        return try {
            val date = LocalDate.parse(dateIso)
            val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, if (isFrench) Locale.FRENCH else Locale.ENGLISH)
            "$dayOfWeek $dateIso"
        } catch (e: Exception) {
            dateIso
        }
    }

    private fun renderEntriesLoop(
        document: PdfDocument,
        initialPage: PdfDocument.Page,
        initialCanvas: Canvas,
        startPageNum: Int,
        startY: Float,
        entries: List<AccountabilityEntryEntity>,
        user: UserEntity?,
        isFrench: Boolean
    ) {
        val paint = Paint().apply { isAntiAlias = true }
        var y = startY
        var currentPageNum = startPageNum
        var activePage = initialPage
        var activeCanvas = initialCanvas
        var rowIndex = 0

        fun drawTableHeaderLocal(c: Canvas, sY: Float) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#0F2942")
            c.drawRect(28f, sY, 567f, sY + 20f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val hDate = if (isFrench) "Date (Jour)" else "Date (Day)"
            val hActivity = if (isFrench) "Activité" else "Activity"
            val hTimeSpan = if (isFrench) "Plage Horaire" else "Time span"
            val hDuration = if (isFrench) "Durée" else "Duration"
            val hNotes = if (isFrench) "Notes & Réflexions" else "Notes & reflection"

            c.drawText(hDate, 32f, sY + 14f, paint)
            c.drawText(hActivity, 125f, sY + 14f, paint)
            c.drawText(hTimeSpan, 280f, sY + 14f, paint)
            c.drawText(hDuration, 355f, sY + 14f, paint)
            c.drawText(hNotes, 415f, sY + 14f, paint)
        }

        if (entries.isEmpty()) {
            paint.style = Paint.Style.FILL
            paint.color = Color.DKGRAY
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            val noEntriesText = if (isFrench) "Aucune activité enregistrée pour la période et les filtres sélectionnés." else "No activities recorded for the selected domain filters and period."
            activeCanvas.drawText(noEntriesText, 35f, y + 16f, paint)
            y += 28f
        } else {
            for (entry in entries) {
                paint.style = Paint.Style.FILL
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8f
                paint.color = Color.parseColor("#1E293B")

                val dateStr = formatDateWithDay(entry.dateIso, isFrench)
                val domainTitle = getDomainDisplayName(entry.domainId, isFrench)
                val activitySummary = "$domainTitle: ${getEntrySummaryDetails(entry, isFrench)}"
                val timeSpan = if (entry.startTimeIso.isNotBlank() && entry.endTimeIso.isNotBlank()) "${entry.startTimeIso} - ${entry.endTimeIso}" else "-"
                val durationStr = if (entry.durationSeconds > 0) formatDurationShort(entry.durationSeconds, isFrench) else "-"
                val notesText = buildString {
                    if (entry.notes.isNotBlank()) append(entry.notes)
                    if (entry.reflection.isNotBlank()) {
                        if (isNotEmpty()) append(" | Refl: ")
                        append(entry.reflection)
                    }
                    if (isEmpty()) append("-")
                }

                val dateLines = wrapText(dateStr, paint, 88f)
                val activityLines = wrapText(activitySummary, paint, 148f)
                val timeSpanLines = wrapText(timeSpan, paint, 70f)
                val durationLines = wrapText(durationStr, paint, 52f)
                val notesLines = wrapText(notesText, paint, 148f)

                val maxLineCount = maxOf(dateLines.size, activityLines.size, timeSpanLines.size, durationLines.size, notesLines.size)
                val rowHeight = (maxLineCount * 11f + 8f).coerceAtLeast(22f)

                if (y + rowHeight > 765f) {
                    drawFooter(activeCanvas, currentPageNum, user, isFrench)
                    document.finishPage(activePage)

                    currentPageNum++
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
                    activePage = document.startPage(newPageInfo)
                    activeCanvas = activePage.canvas

                    y = 35f
                    drawTableHeaderLocal(activeCanvas, y)
                    y += 20f
                }

                // Alternating Row Background
                paint.style = Paint.Style.FILL
                paint.color = if (rowIndex % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
                activeCanvas.drawRect(28f, y, 567f, y + rowHeight, paint)

                // Row Grid Lines
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E2E8F0")
                paint.strokeWidth = 0.6f
                activeCanvas.drawRect(28f, y, 567f, y + rowHeight, paint)

                // Vertical Column Dividers: Date (120f), Activity (275f), TimeSpan (350f), Duration (410f)
                activeCanvas.drawLine(120f, y, 120f, y + rowHeight, paint)
                activeCanvas.drawLine(275f, y, 275f, y + rowHeight, paint)
                activeCanvas.drawLine(350f, y, 350f, y + rowHeight, paint)
                activeCanvas.drawLine(410f, y, 410f, y + rowHeight, paint)

                // Text Content Drawing
                paint.style = Paint.Style.FILL

                // Column 1: Date (Day)
                dateLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 32f, y + 12f + (i * 11f), paint)
                }

                // Column 2: Activity
                activityLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 125f, y + 12f + (i * 11f), paint)
                }

                // Column 3: Time span
                timeSpanLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 280f, y + 12f + (i * 11f), paint)
                }

                // Column 4: Duration
                durationLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 355f, y + 12f + (i * 11f), paint)
                }

                // Column 5: Notes & Reflection
                notesLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 415f, y + 12f + (i * 11f), paint)
                }

                y += rowHeight
                rowIndex++
            }
        }

        drawFooter(activeCanvas, currentPageNum, user, isFrench)
        document.finishPage(activePage)
    }

    private fun drawFooter(c: Canvas, pNum: Int, user: UserEntity?, isFrench: Boolean) {
        val paint = Paint().apply { isAntiAlias = true }
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#CBD5E1")
        paint.strokeWidth = 0.8f
        c.drawLine(28f, 780f, 567f, 780f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 8f
        val genText = if (isFrench) "Généré le ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)} via CMFI Accap • Page $pNum"
        else "Generated on ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)} via CMFI Accap • Page $pNum"
        c.drawText(genText, 28f, 796f, paint)

        // Disciple Maker Signature Line
        paint.color = Color.parseColor("#334155")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val makerName = user?.discipleMaker?.trim()
        val sigText = if (isFrench) {
            if (!makerName.isNullOrEmpty()) "Faiseur de disciples ($makerName) : ___________________"
            else "Signature du Faiseur de disciples : ___________________"
        } else {
            if (!makerName.isNullOrEmpty()) "Disciple Maker ($makerName): ___________________"
            else "Disciple Maker Signature: ___________________"
        }
        c.drawText(sigText, 270f, 796f, paint)
    }

    private fun getDomainDisplayName(domainId: String, isFrench: Boolean): String {
        return when (domainId) {
            "ddewg" -> if (isFrench) "DREQD (DDEWG)" else "DDEWG"
            "bible_reading" -> if (isFrench) "Lecture Biblique" else "Bible Reading"
            "prayer_alone" -> if (isFrench) "Prière Seul" else "Prayer Alone"
            "prayer_with_others" -> if (isFrench) "Prière en Groupe" else "Prayer With Others"
            "proclamation_importunity" -> if (isFrench) "Proclamation & Importunité" else "Proclamation"
            "fasting" -> if (isFrench) "Jeûne" else "Fasting"
            "giving" -> if (isFrench) "Offrandes / Dîmes" else "Giving"
            "accountability" -> if (isFrench) "Redevabilité Disciple" else "Discipleship"
            "christian_lit" -> if (isFrench) "Littérature Chrétienne" else "Christian Literature"
            "christian_lit_mem" -> if (isFrench) "Mémorisation Lit." else "Lit. Memorization"
            "bible_mem" -> if (isFrench) "Mémorisation Bible" else "Bible Memorization"
            "soul_winning" -> if (isFrench) "Évangélisation" else "Soul Winning"
            "retreats" -> if (isFrench) "Retraite Spirituelle" else "Spiritual Retreat"
            else -> if (isFrench) "Autre Discipline" else "Custom Discipline"
        }
    }

    private fun getEntrySummaryDetails(entry: AccountabilityEntryEntity, isFrench: Boolean): String {
        return when (entry.domainId) {
            "ddewg" -> {
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                "${if (isFrench) "Rencontre" else "Encounter"} ($durStr) ${entry.notes}".trim()
            }
            "prayer_alone" -> {
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                val type = entry.prayerType.ifBlank { if (isFrench) "Prière" else "Prayer" }
                val topics = if (entry.prayerTopicsCount > 0) " (${entry.prayerTopicsCount} ${if (isFrench) "sujets" else "topics"})" else ""
                "$type: $durStr$topics"
            }
            "prayer_with_others" -> {
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                "${if (isFrench) "Prière de groupe" else "Group prayer"}: $durStr (${entry.prayerParticipantsCount} p)"
            }
            "proclamation_importunity" -> {
                val topic = entry.proclamationTopic.ifBlank { if (isFrench) "Proclamation" else "Proclamation" }
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                "$topic: ${entry.proclamationCount}x ($durStr)"
            }
            "bible_reading" -> {
                val durStr = if (entry.durationSeconds > 0) " (${formatDuration(entry.durationSeconds, isFrench)})" else ""
                "${entry.bibleBook} ${entry.startChapter}-${entry.endChapter} (${entry.chaptersCount} chs)$durStr"
            }
            "soul_winning" -> {
                val pText = if (isFrench) "Prêchés" else "Preached"
                val cText = if (isFrench) "Convertis" else "Converts"
                "$pText: ${entry.preachedToCount}, $cText: ${entry.convertedCount}"
            }
            "giving" -> "${if (isFrench) "Don" else "Giving"}: ${entry.givingAmount} XAF (${entry.givingType})"
            "fasting" -> "${if (isFrench) "Jeûne" else "Fast"}: ${entry.fastingDaysCount} ${if (isFrench) "j" else "d"} (${entry.fastingType})"
            "christian_lit" -> {
                val durStr = if (entry.durationSeconds > 0) " - ${formatDuration(entry.durationSeconds, isFrench)}" else ""
                "${entry.bookTitle} (${entry.pagesRead} p)$durStr"
            }
            "retreats" -> {
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                "${entry.retreatFocus.ifBlank { if (isFrench) "Retraite" else "Retreat" }}: $durStr"
            }
            else -> entry.notes.ifBlank { if (isFrench) "Activité enregistrée" else "Recorded entry" }
        }
    }
}
