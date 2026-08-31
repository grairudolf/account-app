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

    fun formatOrdinalDate(date: LocalDate, isFrench: Boolean = false): String {
        val day = date.dayOfMonth
        return if (isFrench) {
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.FRENCH).lowercase().replaceFirstChar { it.uppercase() }
            val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.FRENCH).lowercase().replaceFirstChar { it.uppercase() }
            val dayStr = if (day == 1) "1er" else "$day"
            "$dayName $dayStr $monthName ${date.year}"
        } else {
            val dayName = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            val monthName = date.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            val suffix = when {
                day in 11..13 -> "th"
                day % 10 == 1 -> "st"
                day % 10 == 2 -> "nd"
                day % 10 == 3 -> "rd"
                else -> "th"
            }
            "$dayName ${day}$suffix $monthName ${date.year}"
        }
    }

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
        // Sort entries in ascending chronological order
        val sortedEntries = entries.sortedWith(
            compareBy<AccountabilityEntryEntity> { it.dateIso.trim().take(10) }
                .thenBy { it.startTimeIso.ifBlank { "00:00" } }
                .thenBy { it.timestampMs }
        )

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
        var currentPage = document.startPage(pageInfo)
        var canvas: Canvas = currentPage.canvas
        var pageNumber = 1

        val paint = Paint().apply { isAntiAlias = true }
        val locale = if (isFrench) Locale.FRENCH else Locale.ENGLISH

        var y = 0f

        // 1. Header Bar (Deep Navy with Warm Gold Accent)
        paint.color = Color.parseColor("#0F2942")
        canvas.drawRect(0f, 0f, 595f, 92f, paint)

        paint.color = Color.parseColor("#D97706") // Gold bottom border
        canvas.drawRect(0f, 90f, 595f, 93f, paint)

        // Draw App Logo in Header if present
        try {
            val logoBmp = BitmapFactory.decodeResource(context.resources, com.example.R.drawable.app_logo)
            if (logoBmp != null) {
                val logoRect = RectF(522f, 15f, 574f, 67f)
                val bgPaint = Paint().apply {
                    isAntiAlias = true
                    color = Color.WHITE
                }
                canvas.drawRoundRect(logoRect, 8f, 8f, bgPaint)
                canvas.drawBitmap(logoBmp, null, logoRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 17.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val headerTitle = if (isFrench) "FICHE DE COMPTE CMFI ACCAP" else "CMFI ACCAP ACCOUNT SHEET"
        canvas.drawText(headerTitle, 26f, 38f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.parseColor("#E2E8F0")
        val subTitle = if (isFrench) "Communauté Missionnaire Chrétienne Internationale (CMFI)" else "Christian Missionary Fellowship International (CMFI)"
        canvas.drawText(subTitle, 26f, 56f, paint)

        val mottoText = if (isFrench) 
            "« Ainsi chacun de nous rendra compte à Dieu pour lui-même. » — Romains 14:12" 
        else 
            "“So then, each of us will give an account of ourselves to God.” — Romans 14:12 (NIV)"
        paint.textSize = 8.2f
        paint.color = Color.parseColor("#FDE047") // Vibrant Gold
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText(mottoText, 26f, 74f, paint)

        y = 108f

        // 2. Disciple & Disciple Maker Profile Card
        val cardRect = RectF(26f, y, 569f, y + 66f)
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F1F5F9")
        canvas.drawRoundRect(cardRect, 8f, 8f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#94A3B8") // Darker, crisper border
        paint.strokeWidth = 1f
        canvas.drawRoundRect(cardRect, 8f, 8f, paint)

        paint.style = Paint.Style.FILL

        // Left Column: Disciple Info
        val rawName = user?.fullName?.trim()
        val discipleName = if (!rawName.isNullOrEmpty()) rawName else if (isFrench) "Disciple du Seigneur" else "Disciple of the Lord"
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${if (isFrench) "Disciple :" else "Disciple:"} $discipleName", 36f, y + 20f, paint)

        val assemblyName = user?.localAssembly?.trim()
        val assemblyDisplay = if (!assemblyName.isNullOrEmpty()) assemblyName else if (isFrench) "Assemblée Locale Non Spécifiée" else "Local Assembly Not Set"
        paint.color = Color.parseColor("#334155")
        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("${if (isFrench) "Assemblée :" else "Assembly:"} $assemblyDisplay", 36f, y + 38f, paint)

        val periodTypeTrans = when (reportType) {
            "DAILY" -> if (isFrench) "QUOTIDIEN" else "DAILY"
            "WEEKLY" -> if (isFrench) "HEBDOMADAIRE" else "WEEKLY"
            "MONTHLY" -> if (isFrench) "MENSUEL" else "MONTHLY"
            "CUSTOM" -> if (isFrench) "PERSONNALISÉ" else "CUSTOM"
            else -> reportType
        }
        val periodText = "${if (isFrench) "Période :" else "Period:"} $periodTypeTrans ($dateRangeLabel)"
        canvas.drawText(periodText, 36f, y + 54f, paint)

        // Right Column: Disciple Maker Info
        val rawMaker = user?.discipleMaker?.trim()
        val hasMaker = !rawMaker.isNullOrEmpty()
        val makerDisplay = if (hasMaker) rawMaker!! else "________________________"
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 10.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "Faiseur de Disciples :" else "Disciple Maker:", 310f, y + 20f, paint)

        paint.color = if (hasMaker) Color.parseColor("#1E40AF") else Color.parseColor("#475569")
        paint.textSize = 10.5f
        paint.typeface = if (hasMaker) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        canvas.drawText(makerDisplay, 310f, y + 38f, paint)

        y += 78f

        // 3. Domain Metrics Calculations
        val ddewgEntries = sortedEntries.filter { it.domainId.equals("ddewg", true) || it.domainId.equals("dreqd", true) }
        val prayerAloneEntries = sortedEntries.filter { it.domainId.equals("prayer_alone", true) || it.domainId.equals("prayer", true) }
        val prayerGroupEntries = sortedEntries.filter { it.domainId.startsWith("prayer_with", true) || it.domainId.startsWith("prayer_group", true) }
        val allPrayerEntries = sortedEntries.filter { it.domainId.startsWith("prayer", true) }
        val bibleEntries = sortedEntries.filter { it.domainId.startsWith("bible_read", true) || it.domainId.equals("bible", true) }
        val bibleMemEntries = sortedEntries.filter { it.domainId.startsWith("bible_mem", true) }
        val litEntries = sortedEntries.filter { it.domainId.equals("christian_lit", true) || it.domainId.startsWith("christian_lit_read", true) || it.domainId.equals("literature", true) }
        val litMemEntries = sortedEntries.filter { it.domainId.startsWith("christian_lit_mem", true) || it.domainId.startsWith("lit_mem", true) }
        val proclamationEntries = sortedEntries.filter { it.domainId.startsWith("proclamation", true) }
        val soulEntries = sortedEntries.filter { it.domainId.startsWith("soul", true) || it.domainId.startsWith("evangel", true) }
        val discipleshipEntries = sortedEntries.filter { it.domainId.startsWith("making_disciple", true) || it.domainId.startsWith("disciple", true) || it.domainId.equals("accountability", true) }
        val fastingEntries = sortedEntries.filter { it.domainId.startsWith("fast", true) }
        val givingEntries = sortedEntries.filter { it.domainId.startsWith("giv", true) || it.domainId.startsWith("offrand", true) }
        val retreatEntries = sortedEntries.filter { it.domainId.startsWith("retreat", true) }

        val ddewgCount = ddewgEntries.size
        val ddewgTimeSecs = ddewgEntries.sumOf { it.durationSeconds }
        val prayerAloneTimeSecs = prayerAloneEntries.sumOf { it.durationSeconds }
        val prayerGroupTimeSecs = prayerGroupEntries.sumOf { it.durationSeconds }
        val groupPrayerSessions = prayerGroupEntries.size
        val totalGroupParticipants = prayerGroupEntries.sumOf { it.prayerParticipantsCount }
        val totalPrayerTimeSecs = allPrayerEntries.sumOf { it.durationSeconds }

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

        val totalBibleChapters = bibleEntries.sumOf { it.chaptersCount }
        val bibleTimeSecs = bibleEntries.sumOf { it.durationSeconds }
        val bibleMemVerses = bibleMemEntries.sumOf { if (it.chaptersCount > 0) it.chaptersCount else 1 }

        val totalLitPages = litEntries.sumOf { it.pagesRead }
        val litTimeSecs = litEntries.sumOf { it.durationSeconds }
        val litMemQuotes = litMemEntries.sumOf { if (it.pagesRead > 0) it.pagesRead else 1 }

        val sPreached = soulEntries.sumOf { it.preachedToCount }
        val sConverts = soulEntries.sumOf { it.convertedCount }
        val disciplesMentored = discipleshipEntries.sumOf { if (it.prayerParticipantsCount > 0) it.prayerParticipantsCount else 1 }
        val fDays = fastingEntries.sumOf { it.fastingDaysCount.coerceAtLeast(1) }
        val gAmountXAF = givingEntries.sumOf { it.givingAmount }
        val pCount = proclamationEntries.sumOf { it.proclamationCount }
        val pTimeSecs = proclamationEntries.sumOf { it.durationSeconds }
        val retreatCount = retreatEntries.size
        val retreatTimeSecs = retreatEntries.sumOf { it.durationSeconds }

        val totalTimeWithGodSecs = ddewgTimeSecs + totalPrayerTimeSecs + bibleTimeSecs + litTimeSecs + pTimeSecs + retreatTimeSecs

        // 4. Section Title & 3 Summary Pillar Cards (For all report types to show key aggregates)
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 11.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val summaryTitle = if (isFrench) "RÉSUMÉ DES DISCIPLINES SPIRITUELLES" else "SPIRITUAL DISCIPLINES SUMMARY"
        canvas.drawText(summaryTitle, 26f, y + 10f, paint)

        val timeWithGodLabel = "${if (isFrench) "Temps Total avec Dieu :" else "Total Time with God:"} ${formatDuration(totalTimeWithGodSecs, isFrench)}"
        paint.color = Color.parseColor("#15803D")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(timeWithGodLabel, 290f, y + 10f, paint)

        y += 18f

        // Draw 3 Summary Pillar Cards with high contrast text
        val card1 = RectF(26f, y, 200f, y + 115f)
        val card2 = RectF(208f, y, 384f, y + 115f)
        val card3 = RectF(392f, y, 569f, y + 115f)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(card1, 8f, 8f, paint)
        canvas.drawRoundRect(card2, 8f, 8f, paint)
        canvas.drawRoundRect(card3, 8f, 8f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#94A3B8")
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(card1, 8f, 8f, paint)
        canvas.drawRoundRect(card2, 8f, 8f, paint)
        canvas.drawRoundRect(card3, 8f, 8f, paint)

        paint.style = Paint.Style.FILL

        // Pillar 1: Communion & Prayer
        var cy = y + 14f
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "COMMUNION & PRIÈRE" else "COMMUNION & PRAYER", 32f, cy, paint)
        cy += 14f

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(if (isFrench) "• DREQD : $ddewgCount (${formatDurationShort(ddewgTimeSecs, isFrench)})" else "• DDEWG: $ddewgCount (${formatDurationShort(ddewgTimeSecs, isFrench)})", 32f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Prière Seul : ${formatDurationShort(prayerAloneTimeSecs, isFrench)}" else "• Prayer Alone: ${formatDurationShort(prayerAloneTimeSecs, isFrench)}", 32f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Grâce/Requêtes : $thanksgivingCount / $requestCount" else "• Thanks/Requests: $thanksgivingCount / $requestCount", 32f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Prière Groupe : $groupPrayerSessions sess ($totalGroupParticipants p)" else "• Group Prayer: $groupPrayerSessions sess ($totalGroupParticipants p)", 32f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Proclamations : $pCount (${formatDurationShort(pTimeSecs, isFrench)})" else "• Proclamations: $pCount (${formatDurationShort(pTimeSecs, isFrench)})", 32f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Retraites : $retreatCount (${formatDurationShort(retreatTimeSecs, isFrench)})" else "• Retreats: $retreatCount (${formatDurationShort(retreatTimeSecs, isFrench)})", 32f, cy, paint)

        // Pillar 2: Word & Literature
        cy = y + 14f
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "PAROLE & LITTÉRATURE" else "WORD & LITERATURE", 214f, cy, paint)
        cy += 14f

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(if (isFrench) "• Bible : $totalBibleChapters ch (${formatDurationShort(bibleTimeSecs, isFrench)})" else "• Bible: $totalBibleChapters chs (${formatDurationShort(bibleTimeSecs, isFrench)})", 214f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Mém. Bible : $bibleMemVerses versets" else "• Bible Mem: $bibleMemVerses verses", 214f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Littérature : $totalLitPages p (${formatDurationShort(litTimeSecs, isFrench)})" else "• Literature: $totalLitPages p (${formatDurationShort(litTimeSecs, isFrench)})", 214f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Mém. Littérature : $litMemQuotes citations" else "• Lit Mem: $litMemQuotes quotes", 214f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Bertoua : $bertouaPrayedCount prières" else "• Bertoua: $bertouaPrayedCount prayers", 214f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Retraites 15-Min : $retreats15MinCount" else "• 15-Min Retreats: $retreats15MinCount", 214f, cy, paint)

        // Pillar 3: Ministry & Stewardship
        cy = y + 14f
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "MINISTÈRE & SERVICE" else "MINISTRY & SERVICE", 398f, cy, paint)
        cy += 14f

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText(if (isFrench) "• Évangélisation : $sPreached prêchés" else "• Evangelism: $sPreached preached", 398f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Convertis : $sConverts âmes" else "• Converts: $sConverts souls", 398f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Disciples Suivis : $disciplesMentored" else "• Disciples Mentored: $disciplesMentored", 398f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Jeûne : $fDays jours" else "• Fasting: $fDays days", 398f, cy, paint)
        cy += 13f
        canvas.drawText(if (isFrench) "• Offrandes : $gAmountXAF XAF" else "• Giving: $gAmountXAF XAF", 398f, cy, paint)

        y += 125f

        // 5. Weekly or Monthly compact period overview table if applicable
        if (reportType == "WEEKLY") {
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val dayBreakdownTitle = if (isFrench) "RÉPARTITION JOURNALIÈRE DE LA SEMAINE" else "WEEKLY DAILY OVERVIEW"
            canvas.drawText(dayBreakdownTitle, 26f, y + 10f, paint)
            y += 16f

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#1E3A8A")
            canvas.drawRect(26f, y, 569f, y + 18f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(if (isFrench) "Jour" else "Day", 34f, y + 13f, paint)
            canvas.drawText(if (isFrench) "DREQD" else "DDEWG", 125f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Prière Seul" else "Prayer Alone", 200f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Bible" else "Bible", 295f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Littérature" else "Lit. Pages", 380f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Temps avec Dieu" else "Time with God", 470f, y + 13f, paint)

            y += 18f

            val daysOfWeek = DayOfWeek.values()
            for ((idx, day) in daysOfWeek.withIndex()) {
                val dayEntries = sortedEntries.filter {
                    try {
                        LocalDate.parse(it.dateIso.trim().take(10)).dayOfWeek == day
                    } catch (e: Exception) { false }
                }
                val dayName = day.getDisplayName(TextStyle.FULL, locale).lowercase().replaceFirstChar { it.uppercase() }
                val dCount = dayEntries.count { it.domainId == "ddewg" || it.domainId == "dreqd" }
                val dSecs = dayEntries.filter { it.domainId == "ddewg" || it.domainId == "dreqd" }.sumOf { it.durationSeconds }
                val pSecs = dayEntries.filter { it.domainId == "prayer_alone" || it.domainId == "prayer" }.sumOf { it.durationSeconds }
                val bChaps = dayEntries.filter { it.domainId.startsWith("bible") }.sumOf { it.chaptersCount }
                val lPages = dayEntries.filter { it.domainId.startsWith("christian_lit") || it.domainId.startsWith("lit") }.sumOf { it.pagesRead }
                val godSecs = dayEntries.filter { it.domainId in listOf("ddewg", "dreqd", "prayer_alone", "prayer_with_others", "bible_reading", "christian_lit", "proclamation_importunity", "retreats") }.sumOf { it.durationSeconds }

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
                canvas.drawRect(26f, y, 569f, y + 15f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#CBD5E1")
                paint.strokeWidth = 0.5f
                canvas.drawRect(26f, y, 569f, y + 15f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#0F172A")
                paint.textSize = 8f
                paint.typeface = Typeface.DEFAULT

                canvas.drawText(dayName, 34f, y + 11f, paint)
                canvas.drawText(if (dCount > 0) "$dCount (${formatDurationShort(dSecs, isFrench)})" else "-", 125f, y + 11f, paint)
                canvas.drawText(if (pSecs > 0) formatDurationShort(pSecs, isFrench) else "-", 200f, y + 11f, paint)
                canvas.drawText(if (bChaps > 0) "$bChaps ch" else "-", 295f, y + 11f, paint)
                canvas.drawText(if (lPages > 0) "$lPages p" else "-", 380f, y + 11f, paint)
                canvas.drawText(if (godSecs > 0) formatDuration(godSecs, isFrench) else "-", 470f, y + 11f, paint)

                y += 15f
            }
            y += 8f
        } else if (reportType == "MONTHLY") {
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val weekBreakdownTitle = if (isFrench) "RÉPARTITION PAR SEMAINE DU MOIS" else "MONTHLY WEEKLY OVERVIEW"
            canvas.drawText(weekBreakdownTitle, 26f, y + 10f, paint)
            y += 16f

            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#1E3A8A")
            canvas.drawRect(26f, y, 569f, y + 18f, paint)

            paint.color = Color.WHITE
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(if (isFrench) "Semaine" else "Week", 34f, y + 13f, paint)
            canvas.drawText(if (isFrench) "DREQD" else "DDEWG", 130f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Prière Seul" else "Prayer Alone", 205f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Bible" else "Bible", 300f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Littérature" else "Lit. Pages", 385f, y + 13f, paint)
            canvas.drawText(if (isFrench) "Temps avec Dieu" else "Time with God", 470f, y + 13f, paint)

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
                val weekEntries = sortedEntries.filter {
                    try {
                        val d = LocalDate.parse(it.dateIso.trim().take(10))
                        d.dayOfMonth in weekPair.second
                    } catch (e: Exception) { false }
                }
                val dCount = weekEntries.count { it.domainId == "ddewg" || it.domainId == "dreqd" }
                val dSecs = weekEntries.filter { it.domainId == "ddewg" || it.domainId == "dreqd" }.sumOf { it.durationSeconds }
                val pSecs = weekEntries.filter { it.domainId == "prayer_alone" || it.domainId == "prayer" }.sumOf { it.durationSeconds }
                val bChaps = weekEntries.filter { it.domainId.startsWith("bible") }.sumOf { it.chaptersCount }
                val lPages = weekEntries.filter { it.domainId.startsWith("christian_lit") || it.domainId.startsWith("lit") }.sumOf { it.pagesRead }
                val godSecs = weekEntries.filter { it.domainId in listOf("ddewg", "dreqd", "prayer_alone", "prayer_with_others", "bible_reading", "christian_lit", "proclamation_importunity", "retreats") }.sumOf { it.durationSeconds }

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
                canvas.drawRect(26f, y, 569f, y + 15f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#CBD5E1")
                paint.strokeWidth = 0.5f
                canvas.drawRect(26f, y, 569f, y + 15f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#0F172A")
                paint.textSize = 8f
                paint.typeface = Typeface.DEFAULT

                canvas.drawText(label, 34f, y + 11f, paint)
                canvas.drawText(if (dCount > 0) "$dCount (${formatDurationShort(dSecs, isFrench)})" else "-", 130f, y + 11f, paint)
                canvas.drawText(if (pSecs > 0) formatDurationShort(pSecs, isFrench) else "-", 205f, y + 11f, paint)
                canvas.drawText(if (bChaps > 0) "$bChaps ch" else "-", 300f, y + 11f, paint)
                canvas.drawText(if (lPages > 0) "$lPages p" else "-", 385f, y + 11f, paint)
                canvas.drawText(if (godSecs > 0) formatDuration(godSecs, isFrench) else "-", 470f, y + 11f, paint)

                y += 15f
            }
            y += 8f
        }

        // 6. Unified Itemized Activity Breakdown (SAME exact 5 columns for ALL report types)
        // Check if there is enough space on page 1 for the table header + at least 2 rows (needs ~75f). If not, start on a new page.
        if (y > 670f) {
            drawFooter(canvas, pageNumber, user, isFrench)
            document.finishPage(currentPage)

            pageNumber++
            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
            currentPage = document.startPage(newPageInfo)
            canvas = currentPage.canvas
            y = 35f
        }

        // Breakdown Section Title
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val breakdownTitle = if (isFrench) "DÉTAIL CHRONOLOGIQUE DES ACTIVITÉS" else "CHRONOLOGICAL ACTIVITIES BREAKDOWN"
        canvas.drawText(breakdownTitle, 26f, y + 12f, paint)
        y += 18f

        drawUnifiedTableHeader(canvas, y, isFrench)
        y += 20f

        // Render the sorted entries using the 5 unified columns
        renderUnifiedEntriesLoop(
            document = document,
            initialPage = currentPage,
            initialCanvas = canvas,
            startPageNum = pageNumber,
            startY = y,
            entries = sortedEntries,
            user = user,
            isFrench = isFrench,
            dateRangeLabel = dateRangeLabel
        )

        val reportDir = File(context.filesDir, "reports")
        if (!reportDir.exists()) reportDir.mkdirs()

        val pdfFile = File(reportDir, "CMFI_Report_${reportType}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    private fun drawUnifiedTableHeader(c: Canvas, startY: Float, isFrench: Boolean) {
        val paint = Paint().apply { isAntiAlias = true }
        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#0F2942")
        c.drawRect(26f, startY, 569f, startY + 20f, paint)

        paint.color = Color.WHITE
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val hDate = if (isFrench) "Date (Jour)" else "Date (Day)"
        val hActivity = if (isFrench) "Activité" else "Activity"
        val hTimeSpan = if (isFrench) "Plage Horaire" else "Time span"
        val hDuration = if (isFrench) "Durée" else "Duration"
        val hNotes = if (isFrench) "Notes & Réflexions" else "Notes & reflection"

        c.drawText(hDate, 32f, startY + 14f, paint)
        c.drawText(hActivity, 122f, startY + 14f, paint)
        c.drawText(hTimeSpan, 280f, startY + 14f, paint)
        c.drawText(hDuration, 355f, startY + 14f, paint)
        c.drawText(hNotes, 415f, startY + 14f, paint)
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
            val cleanIso = dateIso.trim().take(10)
            val date = LocalDate.parse(cleanIso)
            formatOrdinalDate(date, isFrench)
        } catch (e: Exception) {
            dateIso
        }
    }

    private fun renderUnifiedEntriesLoop(
        document: PdfDocument,
        initialPage: PdfDocument.Page,
        initialCanvas: Canvas,
        startPageNum: Int,
        startY: Float,
        entries: List<AccountabilityEntryEntity>,
        user: UserEntity?,
        isFrench: Boolean,
        dateRangeLabel: String
    ) {
        val paint = Paint().apply { isAntiAlias = true }
        var y = startY
        var currentPageNum = startPageNum
        var activePage = initialPage
        var activeCanvas = initialCanvas
        var rowIndex = 0

        if (entries.isEmpty()) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#475569")
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            val noEntriesText = if (isFrench)
                "Aucune activité enregistrée trouvée pour la période sélectionnée ($dateRangeLabel)."
            else
                "No logged activities found for the selected period ($dateRangeLabel)."
            activeCanvas.drawText(noEntriesText, 32f, y + 16f, paint)
            y += 28f
        } else {
            for (entry in entries) {
                paint.style = Paint.Style.FILL
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8.5f
                paint.color = Color.parseColor("#0F172A")

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

                val dateLines = wrapText(dateStr, paint, 85f)
                val activityLines = wrapText(activitySummary, paint, 150f)
                val timeSpanLines = wrapText(timeSpan, paint, 68f)
                val durationLines = wrapText(durationStr, paint, 52f)
                val notesLines = wrapText(notesText, paint, 148f)

                val maxLineCount = maxOf(dateLines.size, activityLines.size, timeSpanLines.size, durationLines.size, notesLines.size)
                val rowHeight = (maxLineCount * 11.5f + 8f).coerceAtLeast(22f)

                if (y + rowHeight > 765f) {
                    drawFooter(activeCanvas, currentPageNum, user, isFrench)
                    document.finishPage(activePage)

                    currentPageNum++
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
                    activePage = document.startPage(newPageInfo)
                    activeCanvas = activePage.canvas

                    y = 35f
                    drawUnifiedTableHeader(activeCanvas, y, isFrench)
                    y += 20f
                }

                // High Contrast Alternating Row Background
                paint.style = Paint.Style.FILL
                paint.color = if (rowIndex % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
                activeCanvas.drawRect(26f, y, 569f, y + rowHeight, paint)

                // Crisp Row Grid Line
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#94A3B8")
                paint.strokeWidth = 0.6f
                activeCanvas.drawRect(26f, y, 569f, y + rowHeight, paint)

                // Vertical Column Dividers
                activeCanvas.drawLine(118f, y, 118f, y + rowHeight, paint)
                activeCanvas.drawLine(274f, y, 274f, y + rowHeight, paint)
                activeCanvas.drawLine(350f, y, 350f, y + rowHeight, paint)
                activeCanvas.drawLine(410f, y, 410f, y + rowHeight, paint)

                // Deep, clear text drawing
                paint.style = Paint.Style.FILL
                paint.color = Color.parseColor("#0F172A")

                // Column 1: Date (Day)
                dateLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 30f, y + 12f + (i * 11.5f), paint)
                }

                // Column 2: Activity
                activityLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 122f, y + 12f + (i * 11.5f), paint)
                }

                // Column 3: Time span
                timeSpanLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 278f, y + 12f + (i * 11.5f), paint)
                }

                // Column 4: Duration
                durationLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 354f, y + 12f + (i * 11.5f), paint)
                }

                // Column 5: Notes & Reflection
                notesLines.forEachIndexed { i, line ->
                    activeCanvas.drawText(line, 414f, y + 12f + (i * 11.5f), paint)
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
        paint.color = Color.parseColor("#94A3B8")
        paint.strokeWidth = 0.8f
        c.drawLine(26f, 780f, 569f, 780f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#475569")
        paint.textSize = 8.5f
        val genText = if (isFrench) "Généré le ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)} via CMFI Accap • Page $pNum"
        else "Generated on ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)} via CMFI Accap • Page $pNum"
        c.drawText(genText, 26f, 796f, paint)

        // Disciple Maker Signature Line
        paint.color = Color.parseColor("#0F172A")
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
        val lower = domainId.lowercase().trim()
        return when {
            lower == "ddewg" || lower == "dreqd" -> if (isFrench) "DREQD (DDEWG)" else "DDEWG"
            lower.startsWith("bible_read") || lower == "bible" -> if (isFrench) "Lecture Biblique" else "Bible Reading"
            lower.startsWith("bible_mem") -> if (isFrench) "Mémorisation Bible" else "Bible Memorization"
            lower == "prayer_alone" || lower == "prayer" -> if (isFrench) "Prière Seul" else "Prayer Alone"
            lower.startsWith("prayer_with") || lower.startsWith("prayer_group") -> if (isFrench) "Prière en Groupe" else "Prayer With Others"
            lower.startsWith("proclamation") -> if (isFrench) "Proclamation & Importunité" else "Proclamation & Importunity"
            lower.startsWith("fast") -> if (isFrench) "Jeûne" else "Fasting"
            lower.startsWith("giv") || lower.startsWith("offrand") -> if (isFrench) "Offrandes / Dîmes" else "Giving to God"
            lower.startsWith("making_disciple") || lower.startsWith("disciple") || lower == "accountability" -> if (isFrench) "Faire des Disciples" else "Making Disciples"
            lower.startsWith("christian_lit_mem") || lower.startsWith("lit_mem") -> if (isFrench) "Mémorisation Littérature" else "Lit. Memorization"
            lower.startsWith("christian_lit") || lower.startsWith("literature") -> if (isFrench) "Littérature Chrétienne" else "Christian Literature"
            lower.startsWith("soul") || lower.startsWith("evangel") -> if (isFrench) "Gagnage d'Âmes" else "Soul Winning"
            lower.startsWith("retreat") -> if (isFrench) "Retraite Spirituelle" else "Spiritual Retreat"
            else -> domainId.replace("_", " ").replaceFirstChar { it.uppercase() }
        }
    }

    private fun getEntrySummaryDetails(entry: AccountabilityEntryEntity, isFrench: Boolean): String {
        val lower = entry.domainId.lowercase().trim()
        return when {
            lower == "ddewg" || lower == "dreqd" -> {
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                "${if (isFrench) "Rencontre" else "Encounter"} ($durStr)"
            }
            lower == "prayer_alone" || lower == "prayer" -> {
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                val type = entry.prayerType.ifBlank { if (isFrench) "Prière" else "Prayer" }
                val topics = if (entry.prayerTopicsCount > 0) " (${entry.prayerTopicsCount} ${if (isFrench) "sujets" else "topics"})" else ""
                "$type: $durStr$topics"
            }
            lower.startsWith("prayer_with") || lower.startsWith("prayer_group") -> {
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                "${if (isFrench) "Prière de groupe" else "Group prayer"}: $durStr (${entry.prayerParticipantsCount} ${if (isFrench) "participants" else "participants"})"
            }
            lower.startsWith("proclamation") -> {
                val topic = entry.proclamationTopic.ifBlank { if (isFrench) "Proclamation" else "Proclamation" }
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                "$topic: ${entry.proclamationCount}x ($durStr)"
            }
            lower.startsWith("bible_read") || lower == "bible" -> {
                val durStr = if (entry.durationSeconds > 0) " (${formatDuration(entry.durationSeconds, isFrench)})" else ""
                val book = entry.bibleBook.ifBlank { if (isFrench) "Bible" else "Bible" }
                val chUnit = if (isFrench) "ch" else "chs"
                "$book ${entry.startChapter}-${entry.endChapter} (${entry.chaptersCount} $chUnit)$durStr"
            }
            lower.startsWith("bible_mem") -> {
                val book = entry.bibleMemBook.ifBlank { entry.bibleBook.ifBlank { if (isFrench) "Passage" else "Scripture" } }
                val ch = if (entry.bibleMemChapter > 0) entry.bibleMemChapter else entry.startChapter
                val verse = entry.bibleMemVerse.ifBlank { "1" }
                "$book $ch:$verse (${if (isFrench) "Mémorisation" else "Memory"})"
            }
            lower.startsWith("christian_lit_mem") || lower.startsWith("lit_mem") -> {
                val durStr = if (entry.durationSeconds > 0) " (${formatDuration(entry.durationSeconds, isFrench)})" else ""
                val count = if (entry.pagesMemorized > 0) entry.pagesMemorized else entry.pagesRead
                "${entry.bookTitle.ifBlank { if (isFrench) "Livre" else "Book" }} ($count ${if (isFrench) "pages mémorisées" else "pages memorized"})$durStr"
            }
            lower.startsWith("christian_lit") || lower.startsWith("literature") -> {
                val durStr = if (entry.durationSeconds > 0) " - ${formatDuration(entry.durationSeconds, isFrench)}" else ""
                val author = if (entry.bookAuthor.isNotBlank()) "${if (isFrench) " par " else " by "}${entry.bookAuthor}" else ""
                "${entry.bookTitle}$author (${entry.pagesRead} p)$durStr"
            }
            lower.startsWith("soul") || lower.startsWith("evangel") -> {
                val pText = if (isFrench) "Prêchés" else "Preached"
                val cText = if (isFrench) "Convertis" else "Converts"
                "$pText: ${entry.preachedToCount}, $cText: ${entry.convertedCount}"
            }
            lower.startsWith("making_disciple") || lower.startsWith("disciple") || lower == "accountability" -> {
                val areas = if (entry.areasDiscussed.isNotBlank()) ": ${entry.areasDiscussed}" else ""
                val durStr = if (entry.durationSeconds > 0) " (${formatDuration(entry.durationSeconds, isFrench)})" else ""
                "${if (isFrench) "Suivi Disciple" else "Discipleship"}$areas$durStr"
            }
            lower.startsWith("giv") || lower.startsWith("offrand") -> {
                "${if (isFrench) "Don" else "Giving"}: ${entry.givingAmount} XAF (${entry.givingType})"
            }
            lower.startsWith("fast") -> {
                "${if (isFrench) "Jeûne" else "Fast"}: ${entry.fastingDaysCount} ${if (isFrench) "j" else "d"} (${entry.fastingType})"
            }
            lower.startsWith("retreat") -> {
                val durStr = formatDuration(entry.durationSeconds, isFrench)
                "${entry.retreatFocus.ifBlank { if (isFrench) "Retraite" else "Retreat" }}: $durStr"
            }
            else -> entry.notes.ifBlank { if (isFrench) "Activité enregistrée" else "Recorded entry" }
        }
    }
}
