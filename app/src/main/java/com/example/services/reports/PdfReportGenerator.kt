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

        // 3. Aggregate Domain Metrics Calculation
        val ddewgEntries = entries.filter { it.domainId == "ddewg" }
        val prayerAloneEntries = entries.filter { it.domainId == "prayer_alone" }
        val prayerGroupEntries = entries.filter { it.domainId == "prayer_with_others" }
        val allPrayerEntries = entries.filter { it.domainId.startsWith("prayer") }
        val bibleEntries = entries.filter { it.domainId == "bible_reading" }
        val litEntries = entries.filter { it.domainId == "christian_lit" || it.domainId == "christian_lit_mem" }
        val proclamationEntries = entries.filter { it.domainId == "proclamation_importunity" }
        val soulEntries = entries.filter { it.domainId == "soul_winning" }
        val fastingEntries = entries.filter { it.domainId == "fasting" }
        val givingEntries = entries.filter { it.domainId == "giving" }
        val retreatEntries = entries.filter { it.domainId == "retreats" }

        // Specific Metrics requested by user:
        val ddewgCount = ddewgEntries.size
        val ddewgTimeSecs = ddewgEntries.sumOf { it.durationSeconds }

        val prayerAloneTimeSecs = prayerAloneEntries.sumOf { it.durationSeconds }
        val totalPrayerTimeSecs = allPrayerEntries.sumOf { it.durationSeconds }

        // Thanksgiving Topics
        val thanksgivingCount = prayerAloneEntries.filter {
            it.prayerType.contains("Thanksgiving", ignoreCase = true) || it.notes.contains("Thanksgiving", ignoreCase = true) || it.notes.contains("Grâce", ignoreCase = true)
        }.sumOf { if (it.prayerTopicsCount > 0) it.prayerTopicsCount else 1 }

        // Request Topics
        val requestCount = prayerAloneEntries.filter {
            it.prayerType.contains("Request", ignoreCase = true) || it.notes.contains("Request", ignoreCase = true) || it.notes.contains("Requête", ignoreCase = true)
        }.sumOf { if (it.prayerTopicsCount > 0) it.prayerTopicsCount else 1 }

        // 15-Minute Retreats
        val retreats15MinCount = prayerAloneEntries.count {
            it.prayerType.contains("15", ignoreCase = true) || it.notes.contains("15-Min", ignoreCase = true) || it.notes.contains("15 Min", ignoreCase = true)
        }

        // Bertoua Message Prayers
        val bertouaPrayedCount = prayerAloneEntries.count {
            it.prayerType.contains("Bertoua", ignoreCase = true) || it.notes.contains("Bertoua", ignoreCase = true)
        }

        // Bible Reading
        val totalBibleChapters = bibleEntries.sumOf { it.chaptersCount }
        val bibleTimeSecs = bibleEntries.sumOf { it.durationSeconds }

        // Christian Literature Books and Pages
        val totalLitPages = litEntries.sumOf { it.pagesRead }
        val litTimeSecs = litEntries.sumOf { it.durationSeconds }
        val booksReadMap = litEntries.filter { it.bookTitle.isNotBlank() }
            .groupBy { it.bookTitle.trim() }
            .mapValues { group -> group.value.sumOf { it.pagesRead } }

        // Proclamation
        val totalProclamations = proclamationEntries.sumOf { it.proclamationCount }
        val proclamationTimeSecs = proclamationEntries.sumOf { it.durationSeconds }

        // Total Time with God (Sum of communion disciplines)
        val totalTimeWithGodSecs = ddewgTimeSecs + totalPrayerTimeSecs + bibleTimeSecs + litTimeSecs + proclamationTimeSecs + retreatEntries.sumOf { it.durationSeconds }

        // 4. Section Title: Domain Summary & Key Metrics
        paint.color = Color.parseColor("#0F2942")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val summaryTitle = if (isFrench) "RÉSUMÉ DÉTAILLÉ DES DISCIPLINES SPIRITUELLES" else "DETAILED SPIRITUAL DISCIPLINES SUMMARY"
        canvas.drawText(summaryTitle, 28f, y + 10f, paint)

        // Time with God Badge
        val timeWithGodLabel = "${if (isFrench) "Temps Total Passé avec Dieu :" else "Total Time with God:"} ${formatDuration(totalTimeWithGodSecs, isFrench)}"
        paint.color = Color.parseColor("#15803D") // Green
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(timeWithGodLabel, 300f, y + 10f, paint)

        y += 18f

        // Draw 2-Column Summary Cards
        val leftCard = RectF(28f, y, 292f, y + 115f)
        val rightCard = RectF(302f, y, 567f, y + 115f)

        paint.style = Paint.Style.FILL
        paint.color = Color.parseColor("#F8FAFC")
        canvas.drawRoundRect(leftCard, 8f, 8f, paint)
        canvas.drawRoundRect(rightCard, 8f, 8f, paint)

        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 0.8f
        canvas.drawRoundRect(leftCard, 8f, 8f, paint)
        canvas.drawRoundRect(rightCard, 8f, 8f, paint)

        paint.style = Paint.Style.FILL

        // Left Column Content: DDEWG & Prayer Metrics
        var cy = y + 15f
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "DREQD / DDEWG & PRIÈRE DE COMMUNION" else "DDEWG & COMMUNION PRAYER", 38f, cy, paint)
        cy += 14f

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("• ${if (isFrench) "Nombre de DREQD :" else "DDEWG Encounters:"} $ddewgCount  |  ${if (isFrench) "Temps :" else "Time:"} ${formatDuration(ddewgTimeSecs, isFrench)}", 38f, cy, paint)
        cy += 13f

        canvas.drawText("• ${if (isFrench) "Temps Total de Prière :" else "Total Prayer Time:"} ${formatDuration(totalPrayerTimeSecs, isFrench)}", 38f, cy, paint)
        cy += 13f

        canvas.drawText("• ${if (isFrench) "Sujets d'Actions de Grâce :" else "Thanksgiving Topics:"} $thanksgivingCount", 38f, cy, paint)
        cy += 13f

        canvas.drawText("• ${if (isFrench) "Sujets de Requêtes :" else "Request Topics:"} $requestCount", 38f, cy, paint)
        cy += 13f

        canvas.drawText("• ${if (isFrench) "Retraites de 15 Minutes :" else "15-Min Retreats:"} $retreats15MinCount  |  ${if (isFrench) "Messages de Bertoua :" else "Bertoua Messages:"} $bertouaPrayedCount", 38f, cy, paint)

        // Right Column Content: Word, Literature & Ministry
        cy = y + 15f
        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(if (isFrench) "PAROLE, LITTÉRATURE & MINISTÈRE" else "THE WORD, LITERATURE & MINISTRY", 312f, cy, paint)
        cy += 14f

        paint.color = Color.parseColor("#1E293B")
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("• ${if (isFrench) "Lecture Biblique :" else "Bible Reading:"} $totalBibleChapters ${if (isFrench) "chapitres" else "chapters"} (${formatDuration(bibleTimeSecs, isFrench)})", 312f, cy, paint)
        cy += 13f

        canvas.drawText("• ${if (isFrench) "Littérature Chrétienne :" else "Christian Literature:"} $totalLitPages ${if (isFrench) "pages lues" else "pages read"} (${formatDuration(litTimeSecs, isFrench)})", 312f, cy, paint)
        cy += 13f

        if (booksReadMap.isNotEmpty()) {
            val bookListStr = booksReadMap.entries.take(2).joinToString("; ") { "${it.key} (${it.value}p)" }
            canvas.drawText("  › ${if (isFrench) "Livres :" else "Books:"} ${bookListStr.take(48)}", 312f, cy, paint)
            cy += 13f
        } else {
            canvas.drawText("  › ${if (isFrench) "Livres : Aucun livre lu sur cette période" else "Books: No books recorded in this period"}", 312f, cy, paint)
            cy += 13f
        }

        val sPreached = soulEntries.sumOf { it.preachedToCount }
        val sConverts = soulEntries.sumOf { it.convertedCount }
        canvas.drawText("• ${if (isFrench) "Évangélisation :" else "Soul Winning:"} $sPreached ${if (isFrench) "prêchés" else "preached"}, $sConverts ${if (isFrench) "convertis" else "converts"}", 312f, cy, paint)
        cy += 13f

        val fDays = fastingEntries.sumOf { it.fastingDaysCount.coerceAtLeast(1) }
        val gAmount = givingEntries.sumOf { it.givingAmount }
        val pCount = proclamationEntries.sumOf { it.proclamationCount }
        canvas.drawText("• ${if (isFrench) "Jeûne :" else "Fasting:"} $fDays j  |  ${if (isFrench) "Dons :" else "Giving:"} $$gAmount  |  ${if (isFrench) "Proclamations :" else "Proclamations:"} $pCount", 312f, cy, paint)

        y += 125f

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

        // 6. Entries Detail Table
        fun drawTableHeader(c: Canvas, startY: Float) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#0F2942")
            c.drawRect(28f, startY, 567f, startY + 20f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val hDate = if (isFrench) "Date" else "Date"
            val hDomain = if (isFrench) "Discipline" else "Discipline"
            val hDetails = if (isFrench) "Mesures & Détails Spécifiques" else "Measurements & Specific Details"
            val hNotes = if (isFrench) "Notes / Réflexions" else "Notes / Reflections"

            c.drawText(hDate, 35f, startY + 14f, paint)
            c.drawText(hDomain, 115f, startY + 14f, paint)
            c.drawText(hDetails, 230f, startY + 14f, paint)
            c.drawText(hNotes, 425f, startY + 14f, paint)
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

        val reportDir = File(context.filesDir, "reports")
        if (!reportDir.exists()) reportDir.mkdirs()

        val pdfFile = File(reportDir, "CMFI_Report_${reportType}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
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

            val hDate = if (isFrench) "Date" else "Date"
            val hDomain = if (isFrench) "Discipline" else "Discipline"
            val hDetails = if (isFrench) "Mesures & Détails Spécifiques" else "Measurements & Specific Details"
            val hNotes = if (isFrench) "Notes / Réflexions" else "Notes / Reflections"

            c.drawText(hDate, 35f, sY + 14f, paint)
            c.drawText(hDomain, 115f, sY + 14f, paint)
            c.drawText(hDetails, 230f, sY + 14f, paint)
            c.drawText(hNotes, 425f, sY + 14f, paint)
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
                if (y > 745f) {
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

                val rowHeight = 22f

                // Alternating Row Background
                paint.style = Paint.Style.FILL
                paint.color = if (rowIndex % 2 == 0) Color.parseColor("#F8FAFC") else Color.WHITE
                activeCanvas.drawRect(28f, y, 567f, y + rowHeight, paint)

                // Row Grid Lines
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E2E8F0")
                paint.strokeWidth = 0.6f
                activeCanvas.drawRect(28f, y, 567f, y + rowHeight, paint)

                // Vertical Column Dividers
                activeCanvas.drawLine(110f, y, 110f, y + rowHeight, paint)
                activeCanvas.drawLine(225f, y, 225f, y + rowHeight, paint)
                activeCanvas.drawLine(420f, y, 420f, y + rowHeight, paint)

                // Text Content
                paint.style = Paint.Style.FILL
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8.5f
                paint.color = Color.parseColor("#1E293B")

                activeCanvas.drawText(entry.dateIso, 33f, y + 14f, paint)
                val domainTitle = getDomainDisplayName(entry.domainId, isFrench)
                activeCanvas.drawText(domainTitle.take(19), 115f, y + 14f, paint)

                val details = getEntrySummaryDetails(entry, isFrench)
                activeCanvas.drawText(details.take(38), 230f, y + 14f, paint)

                val notesText = entry.notes.ifBlank { "-" }
                activeCanvas.drawText(notesText.take(28), 425f, y + 14f, paint)

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
            "giving" -> "${if (isFrench) "Don" else "Giving"}: $${entry.givingAmount} (${entry.givingType})"
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
