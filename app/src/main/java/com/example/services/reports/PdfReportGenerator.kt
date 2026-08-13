package com.example.services.reports

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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

        var y = 40f

        // Header Background
        paint.color = Color.parseColor("#1565C0")
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val headerTitle = if (isFrench) "RAPPORT DE REDEVABILITÉ CMFI" else "CMFI ACCOUNTABILITY REPORT"
        canvas.drawText(headerTitle, 30f, 45f, paint)

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        val subTitle = if (isFrench) "Communauté Missionnaire Chrétienne Internationale" else "Christian Missionary Fellowship International"
        canvas.drawText(subTitle, 30f, 68f, paint)

        y = 125f

        // Disciple Profile Info
        paint.color = Color.BLACK
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val discipleName = user?.fullName?.ifBlank { if (isFrench) "Disciple" else "Disciple" } ?: "Disciple"
        val discipleLabel = if (isFrench) "Disciple : $discipleName" else "Disciple: $discipleName"
        canvas.drawText(discipleLabel, 30f, y, paint)

        if (!user?.localAssembly.isNull_Blank()) {
            val assemblyLabel = if (isFrench) "Assemblée : ${user?.localAssembly}" else "Assembly: ${user?.localAssembly}"
            canvas.drawText(assemblyLabel, 320f, y, paint)
        }
        y += 20f

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.DKGRAY
        val periodTypeTrans = when (reportType) {
            "DAILY" -> if (isFrench) "QUOTIDIEN" else "DAILY"
            "WEEKLY" -> if (isFrench) "HEBDOMADAIRE" else "WEEKLY"
            "MONTHLY" -> if (isFrench) "MENSUEL" else "MONTHLY"
            "CUSTOM" -> if (isFrench) "PERSONNALISÉ" else "CUSTOM"
            else -> reportType
        }
        val periodText = if (isFrench) "Période : $periodTypeTrans ($dateRangeLabel)" else "Period: $periodTypeTrans ($dateRangeLabel)"
        canvas.drawText(periodText, 30f, y, paint)
        if (!user?.discipleMaker.isNull_Blank()) {
            val makerLabel = if (isFrench) "Faiseur de disciples : ${user?.discipleMaker}" else "Disciple Maker: ${user?.discipleMaker}"
            canvas.drawText(makerLabel, 320f, y, paint)
        }
        y += 25f

        // Divider
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(30f, y, 565f, y, paint)
        y += 20f

        // Global Summary Header
        paint.color = Color.parseColor("#1565C0")
        paint.textSize = 13f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val summaryTitle = if (isFrench) "RÉSUMÉ GLOBAL DES ACTIVITÉS" else "GLOBAL SUMMARY OF ACTIVITIES"
        canvas.drawText(summaryTitle, 30f, y, paint)
        y += 20f

        // Calculate totals
        val bibleChapters = entries.filter { it.domainId == "bible_reading" }.sumOf { it.chaptersCount }
        val prayerMinutes = entries.filter { it.domainId.startsWith("prayer") || it.domainId == "ddewg" }.sumOf { it.durationSeconds } / 60
        val preachedTo = entries.filter { it.domainId == "soul_winning" }.sumOf { it.preachedToCount }
        val converts = entries.filter { it.domainId == "soul_winning" }.sumOf { it.convertedCount }

        paint.color = Color.BLACK
        paint.textSize = 10.5f
        paint.typeface = Typeface.DEFAULT
        val totalRecText = if (isFrench) "• Activités enregistrées : ${entries.size}" else "• Total Activities Recorded: ${entries.size}"
        val chaptersText = if (isFrench) "• Chapitres bibliques lus : $bibleChapters" else "• Bible Chapters Read: $bibleChapters"
        canvas.drawText(totalRecText, 40f, y, paint)
        canvas.drawText(chaptersText, 300f, y, paint)
        y += 18f

        val prayerText = if (isFrench) "• Temps de Prière / DDEWG : ${prayerMinutes} min" else "• Total Prayer / DDEWG: ${prayerMinutes} mins"
        val soulText = if (isFrench) "• Évangélisation : $preachedTo prêchés, $converts convertis" else "• Soul Winning: Preached $preachedTo, Converts $converts"
        canvas.drawText(prayerText, 40f, y, paint)
        canvas.drawText(soulText, 300f, y, paint)
        y += 25f

        // If report is WEEKLY, draw Breakdown by Day
        if (reportType == "WEEKLY") {
            paint.color = Color.parseColor("#1565C0")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val dayBreakdownTitle = if (isFrench) "RÉPARTITION PAR JOUR DE LA SEMAINE" else "DAILY BREAKDOWN OF THE WEEK"
            canvas.drawText(dayBreakdownTitle, 30f, y, paint)
            y += 15f

            // Table header for breakdown
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#1976D2")
            canvas.drawRect(30f, y, 565f, y + 20f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val colDay = if (isFrench) "Jour" else "Day"
            val colCount = if (isFrench) "Activités" else "Count"
            val colPrayer = if (isFrench) "Prière" else "Prayer"
            val colBible = if (isFrench) "Bible" else "Bible"
            val colSoul = if (isFrench) "Évangélisation" else "Soul Winning"

            canvas.drawText(colDay, 38f, y + 14f, paint)
            canvas.drawText(colCount, 130f, y + 14f, paint)
            canvas.drawText(colPrayer, 210f, y + 14f, paint)
            canvas.drawText(colBible, 310f, y + 14f, paint)
            canvas.drawText(colSoul, 420f, y + 14f, paint)

            y += 20f

            // Group by days of week
            val daysOfWeek = DayOfWeek.values()
            for ((idx, day) in daysOfWeek.withIndex()) {
                val dayEntries = entries.filter {
                    try {
                        LocalDate.parse(it.dateIso).dayOfWeek == day
                    } catch (e: Exception) { false }
                }
                val dayName = day.getDisplayName(TextStyle.FULL, locale).lowercase().replaceFirstChar { it.uppercase() }
                val count = dayEntries.size
                val pMins = dayEntries.filter { it.domainId.startsWith("prayer") || it.domainId == "ddewg" }.sumOf { it.durationSeconds } / 60
                val bChaps = dayEntries.filter { it.domainId == "bible_reading" }.sumOf { it.chaptersCount }
                val sPreach = dayEntries.filter { it.domainId == "soul_winning" }.sumOf { it.preachedToCount }

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.parseColor("#F5F7FA") else Color.WHITE
                canvas.drawRect(30f, y, 565f, y + 18f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E0E0E0")
                paint.strokeWidth = 0.5f
                canvas.drawRect(30f, y, 565f, y + 18f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.BLACK
                paint.textSize = 9f
                paint.typeface = Typeface.DEFAULT

                canvas.drawText(dayName, 38f, y + 13f, paint)
                canvas.drawText("$count", 130f, y + 13f, paint)
                canvas.drawText("${pMins}m", 210f, y + 13f, paint)
                canvas.drawText("${bChaps} ch", 310f, y + 13f, paint)
                canvas.drawText("$sPreach", 420f, y + 13f, paint)

                y += 18f
            }
            y += 15f
        } else if (reportType == "MONTHLY") {
            // Breakdown by Week of Month
            paint.color = Color.parseColor("#1565C0")
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val weekBreakdownTitle = if (isFrench) "RÉPARTITION PAR SEMAINE DU MOIS" else "WEEKLY BREAKDOWN OF THE MONTH"
            canvas.drawText(weekBreakdownTitle, 30f, y, paint)
            y += 15f

            // Table header
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#1976D2")
            canvas.drawRect(30f, y, 565f, y + 20f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val colWk = if (isFrench) "Semaine" else "Week"
            val colCount = if (isFrench) "Activités" else "Count"
            val colPrayer = if (isFrench) "Prière" else "Prayer"
            val colBible = if (isFrench) "Bible" else "Bible"
            val colSoul = if (isFrench) "Évangélisation" else "Soul Winning"

            canvas.drawText(colWk, 38f, y + 14f, paint)
            canvas.drawText(colCount, 150f, y + 14f, paint)
            canvas.drawText(colPrayer, 230f, y + 14f, paint)
            canvas.drawText(colBible, 330f, y + 14f, paint)
            canvas.drawText(colSoul, 430f, y + 14f, paint)

            y += 20f

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
                val count = weekEntries.size
                val pMins = weekEntries.filter { it.domainId.startsWith("prayer") || it.domainId == "ddewg" }.sumOf { it.durationSeconds } / 60
                val bChaps = weekEntries.filter { it.domainId == "bible_reading" }.sumOf { it.chaptersCount }
                val sPreach = weekEntries.filter { it.domainId == "soul_winning" }.sumOf { it.preachedToCount }

                paint.style = Paint.Style.FILL
                paint.color = if (idx % 2 == 0) Color.parseColor("#F5F7FA") else Color.WHITE
                canvas.drawRect(30f, y, 565f, y + 18f, paint)

                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#E0E0E0")
                paint.strokeWidth = 0.5f
                canvas.drawRect(30f, y, 565f, y + 18f, paint)

                paint.style = Paint.Style.FILL
                paint.color = Color.BLACK
                paint.textSize = 9f
                paint.typeface = Typeface.DEFAULT

                canvas.drawText(label, 38f, y + 13f, paint)
                canvas.drawText("$count", 150f, y + 13f, paint)
                canvas.drawText("${pMins}m", 230f, y + 13f, paint)
                canvas.drawText("${bChaps} ch", 330f, y + 13f, paint)
                canvas.drawText("$sPreach", 430f, y + 13f, paint)

                y += 18f
            }
            y += 15f
        }

        // Entries Detail Table Header
        fun drawTableHeader(c: Canvas, startY: Float) {
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#0D47A1")
            c.drawRect(30f, startY, 565f, startY + 22f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val hDate = if (isFrench) "Date" else "Date"
            val hDomain = if (isFrench) "Domaine" else "Domain"
            val hDetails = if (isFrench) "Mesures & Détails" else "Measurements & Details"
            val hNotes = if (isFrench) "Notes / Réflexions" else "Notes / Reflections"

            c.drawText(hDate, 38f, startY + 15f, paint)
            c.drawText(hDomain, 115f, startY + 15f, paint)
            c.drawText(hDetails, 230f, startY + 15f, paint)
            c.drawText(hNotes, 420f, startY + 15f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#0D47A1")
            paint.strokeWidth = 1.2f
            c.drawRect(30f, startY, 565f, startY + 22f, paint)
        }

        if (y > 700f) {
            // Move table header to next page if tight on space
            drawFooter(canvas, 1, isFrench)
            document.finishPage(page)

            val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, 2).create()
            val page2 = document.startPage(newPageInfo)
            var activeCanvas = page2.canvas
            y = 40f
            drawTableHeader(activeCanvas, y)
            y += 22f
            renderEntriesLoop(document, page2, activeCanvas, 2, y, entries, isFrench)
        } else {
            drawTableHeader(canvas, y)
            y += 22f
            renderEntriesLoop(document, page, canvas, 1, y, entries, isFrench)
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
            paint.color = Color.parseColor("#0D47A1")
            c.drawRect(30f, sY, 565f, sY + 22f, paint)

            paint.color = Color.WHITE
            paint.textSize = 9.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

            val hDate = if (isFrench) "Date" else "Date"
            val hDomain = if (isFrench) "Domaine" else "Domain"
            val hDetails = if (isFrench) "Mesures & Détails" else "Measurements & Details"
            val hNotes = if (isFrench) "Notes / Réflexions" else "Notes / Reflections"

            c.drawText(hDate, 38f, sY + 15f, paint)
            c.drawText(hDomain, 115f, sY + 15f, paint)
            c.drawText(hDetails, 230f, sY + 15f, paint)
            c.drawText(hNotes, 420f, sY + 15f, paint)

            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#0D47A1")
            paint.strokeWidth = 1.2f
            c.drawRect(30f, sY, 565f, sY + 22f, paint)
        }

        if (entries.isEmpty()) {
            paint.style = Paint.Style.FILL
            paint.color = Color.DKGRAY
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            val noEntriesText = if (isFrench) "Aucune activité enregistrée pour la période et les filtres sélectionnés." else "No activities recorded for the selected domain filters and period."
            activeCanvas.drawText(noEntriesText, 40f, y + 18f, paint)
            y += 30f
        } else {
            for (entry in entries) {
                if (y > 750f) {
                    drawFooter(activeCanvas, currentPageNum, isFrench)
                    document.finishPage(activePage)

                    currentPageNum++
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
                    activePage = document.startPage(newPageInfo)
                    activeCanvas = activePage.canvas

                    y = 40f
                    drawTableHeaderLocal(activeCanvas, y)
                    y += 22f
                }

                val rowHeight = 22f

                // Alternating Row Background
                paint.style = Paint.Style.FILL
                paint.color = if (rowIndex % 2 == 0) Color.parseColor("#F5F7FA") else Color.WHITE
                activeCanvas.drawRect(30f, y, 565f, y + rowHeight, paint)

                // Row Grid Lines
                paint.style = Paint.Style.STROKE
                paint.color = Color.parseColor("#D1D5DB")
                paint.strokeWidth = 0.8f
                activeCanvas.drawRect(30f, y, 565f, y + rowHeight, paint)

                // Vertical Column Dividers
                activeCanvas.drawLine(110f, y, 110f, y + rowHeight, paint)
                activeCanvas.drawLine(225f, y, 225f, y + rowHeight, paint)
                activeCanvas.drawLine(415f, y, 415f, y + rowHeight, paint)

                // Text Content
                paint.style = Paint.Style.FILL
                paint.typeface = Typeface.DEFAULT
                paint.textSize = 8.5f
                paint.color = Color.BLACK

                activeCanvas.drawText(entry.dateIso, 35f, y + 14f, paint)
                val domainTitle = getDomainDisplayName(entry.domainId, isFrench)
                activeCanvas.drawText(domainTitle.take(18), 115f, y + 14f, paint)

                val details = getEntrySummaryDetails(entry, isFrench)
                activeCanvas.drawText(details.take(32), 230f, y + 14f, paint)

                val notesText = entry.notes.ifBlank { "-" }
                activeCanvas.drawText(notesText.take(28), 420f, y + 14f, paint)

                y += rowHeight
                rowIndex++
            }
        }

        drawFooter(activeCanvas, currentPageNum, isFrench)
        document.finishPage(activePage)
    }

    private fun drawFooter(c: Canvas, pNum: Int, isFrench: Boolean) {
        val paint = Paint().apply { isAntiAlias = true }
        paint.style = Paint.Style.STROKE
        paint.color = Color.LTGRAY
        paint.strokeWidth = 0.8f
        c.drawLine(30f, 780f, 565f, 780f, paint)

        paint.style = Paint.Style.FILL
        paint.color = Color.GRAY
        paint.textSize = 8.5f
        val genText = if (isFrench) "Généré le ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)} via Application CMFI Accap  •  Page $pNum"
        else "Generated on ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)} via CMFI Accap  •  Page $pNum"
        c.drawText(genText, 30f, 795f, paint)

        // Disciple Maker Signature Line
        paint.color = Color.DKGRAY
        val sigText = if (isFrench) "Signature du Faiseur de disciples : ___________________" else "Disciple Maker Signature: ___________________"
        c.drawText(sigText, 320f, 795f, paint)
    }

    private fun getDomainDisplayName(domainId: String, isFrench: Boolean): String {
        return when (domainId) {
            "ddewg" -> if (isFrench) "DREQD (DDEWG)" else "DDEWG"
            "bible_reading" -> if (isFrench) "Lecture Biblique" else "Bible Reading"
            "prayer_alone" -> if (isFrench) "Prière Seul" else "Prayer Alone"
            "prayer_with_others" -> if (isFrench) "Prière en Groupe" else "Prayer With Others"
            "proclamation_importunity" -> if (isFrench) "Proclamation & Importunité" else "Proclamation & Importunity"
            "fasting" -> if (isFrench) "Jeûne" else "Fasting"
            "giving" -> if (isFrench) "Offrandes / Dîmes" else "Giving"
            "accountability" -> if (isFrench) "Redevabilité Disciple" else "Disciple Accountability"
            "christian_lit" -> if (isFrench) "Littérature Chrétienne" else "Christian Literature"
            "christian_lit_mem" -> if (isFrench) "Mémorisation Lit." else "Lit. Memorization"
            "bible_mem" -> if (isFrench) "Mémorisation Bible" else "Bible Memorization"
            "soul_winning" -> if (isFrench) "Évangélisation" else "Soul Winning"
            else -> if (isFrench) "Domaine Personnalisé" else "Custom Domain"
        }
    }

    private fun getEntrySummaryDetails(entry: AccountabilityEntryEntity, isFrench: Boolean): String {
        return when (entry.domainId) {
            "ddewg", "prayer_alone", "prayer_with_others" -> {
                val mins = entry.durationSeconds / 60
                val secs = entry.durationSeconds % 60
                val durLabel = if (isFrench) "Durée" else "Duration"
                "$durLabel: ${mins}m ${secs}s ${entry.notes}".trim()
            }
            "proclamation_importunity" -> {
                val topic = entry.proclamationTopic.ifBlank { if (isFrench) "Proclamation" else "Proclamation" }
                val repLabel = if (isFrench) "proclamations" else "proclamations"
                val mins = entry.durationSeconds / 60
                "$topic: ${entry.proclamationCount} $repLabel (${mins}m)"
            }
            "bible_reading" -> "${entry.bibleBook} ${entry.startChapter}-${entry.endChapter} (${entry.chaptersCount} chs)"
            "soul_winning" -> {
                val pText = if (isFrench) "Prêchés" else "Preached"
                val cText = if (isFrench) "Convertis" else "Converts"
                "$pText: ${entry.preachedToCount}, $cText: ${entry.convertedCount}"
            }
            "giving" -> "${if (isFrench) "Montant" else "Amount"}: $${entry.givingAmount} (${entry.givingType})"
            "fasting" -> "${if (isFrench) "Jours" else "Days"}: ${entry.fastingDaysCount} (${entry.fastingType})"
            "christian_lit" -> "${entry.bookTitle} (${entry.pagesRead} ${if (isFrench) "pages" else "pages"})"
            else -> entry.notes.ifBlank { if (isFrench) "Activité enregistrée" else "Recorded entry" }
        }
    }

    private fun String?.isNull_Blank(): Boolean = this == null || this.isBlank()
}
