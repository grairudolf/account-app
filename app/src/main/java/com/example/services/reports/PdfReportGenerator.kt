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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object PdfReportGenerator {

    fun generatePdfReport(
        context: Context,
        user: UserEntity?,
        reportType: String, // "DAILY", "WEEKLY", "MONTHLY"
        dateRangeLabel: String,
        entries: List<AccountabilityEntryEntity>
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 page size
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply {
            isAntiAlias = true
        }

        var y = 40f

        // Header Background
        paint.color = Color.parseColor("#1565C0")
        canvas.drawRect(0f, 0f, 595f, 100f, paint)

        // Header Title
        paint.color = Color.WHITE
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("CMFI ACCOUNTABILITY REPORT", 30f, 45f, paint)

        paint.textSize = 12f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Christian Missionary Fellowship International", 30f, 68f, paint)

        y = 125f

        // Disciple Profile Info
        paint.color = Color.BLACK
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val discipleName = user?.fullName?.ifBlank { "Disciple" } ?: "Disciple"
        canvas.drawText("Disciple: $discipleName", 30f, y, paint)

        if (!user?.localAssembly.isNull_Blank()) {
            canvas.drawText("Assembly: ${user?.localAssembly}", 320f, y, paint)
        }
        y += 20f

        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.DKGRAY
        canvas.drawText("Period: $reportType ($dateRangeLabel)", 30f, y, paint)
        if (!user?.discipleMaker.isNull_Blank()) {
            canvas.drawText("Disciple Maker: ${user?.discipleMaker}", 320f, y, paint)
        }
        y += 25f

        // Divider
        paint.color = Color.LTGRAY
        paint.strokeWidth = 1f
        canvas.drawLine(30f, y, 565f, y, paint)
        y += 20f

        // Summary Header
        paint.color = Color.parseColor("#1565C0")
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SUMMARY OF ACTIVITIES", 30f, y, paint)
        y += 20f

        // Calculate summary
        val bibleChapters = entries.filter { it.domainId == "bible_reading" }.sumOf { it.chaptersCount }
        val prayerMinutes = entries.filter { it.domainId.startsWith("prayer") || it.domainId == "ddewg" }.sumOf { it.durationSeconds } / 60
        val preachedTo = entries.filter { it.domainId == "soul_winning" }.sumOf { it.preachedToCount }
        val converts = entries.filter { it.domainId == "soul_winning" }.sumOf { it.convertedCount }

        paint.color = Color.BLACK
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("• Total Activities Recorded: ${entries.size}", 40f, y, paint)
        canvas.drawText("• Bible Chapters Read: $bibleChapters", 300f, y, paint)
        y += 18f
        canvas.drawText("• Total Prayer / DDEWG: ${prayerMinutes} mins", 40f, y, paint)
        canvas.drawText("• Soul Winning: Preached to $preachedTo, Converts $converts", 300f, y, paint)
        y += 30f

        // Entries Table Header with Full Grid Borders
        fun drawTableHeader(c: Canvas, startY: Float) {
            // Table Header Background
            paint.style = Paint.Style.FILL
            paint.color = Color.parseColor("#0D47A1")
            c.drawRect(30f, startY, 565f, startY + 24f, paint)

            // Header Text
            paint.color = Color.WHITE
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            c.drawText("Date", 38f, startY + 16f, paint)
            c.drawText("Domain", 115f, startY + 16f, paint)
            c.drawText("Measurements & Details", 230f, startY + 16f, paint)
            c.drawText("Notes / Reflections", 420f, startY + 16f, paint)

            // Table Outer Border
            paint.style = Paint.Style.STROKE
            paint.color = Color.parseColor("#0D47A1")
            paint.strokeWidth = 1.5f
            c.drawRect(30f, startY, 565f, startY + 24f, paint)
        }

        drawTableHeader(canvas, y)
        y += 24f

        // Itemized Entries
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 9f

        var currentPageNum = 1
        var activePage = page
        var activeCanvas = canvas
        var rowIndex = 0

        fun drawFooter(c: Canvas, pNum: Int) {
            paint.style = Paint.Style.STROKE
            paint.color = Color.LTGRAY
            paint.strokeWidth = 0.8f
            c.drawLine(30f, 780f, 565f, 780f, paint)

            paint.style = Paint.Style.FILL
            paint.color = Color.GRAY
            paint.textSize = 8.5f
            c.drawText("Generated on ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)} via CMFI Accountability App  •  Page $pNum", 30f, 795f, paint)

            // Disciple Maker Signature Line
            paint.color = Color.DKGRAY
            c.drawText("Disciple Maker Signature: _______________________", 320f, 795f, paint)
        }

        if (entries.isEmpty()) {
            paint.style = Paint.Style.FILL
            paint.color = Color.DKGRAY
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            activeCanvas.drawText("No activities recorded for the selected domain filters and period.", 40f, y + 18f, paint)
            y += 30f
        } else {
            for (entry in entries) {
                if (y > 750f) {
                    drawFooter(activeCanvas, currentPageNum)
                    document.finishPage(activePage)

                    currentPageNum++
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, currentPageNum).create()
                    activePage = document.startPage(newPageInfo)
                    activeCanvas = activePage.canvas

                    y = 40f
                    drawTableHeader(activeCanvas, y)
                    y += 24f
                }

                val rowHeight = 22f

                // Alternating Row Background
                paint.style = Paint.Style.FILL
                paint.color = if (rowIndex % 2 == 0) Color.parseColor("#F5F7FA") else Color.WHITE
                activeCanvas.drawRect(30f, y, 565f, y + rowHeight, paint)

                // Row Grid Box Line
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
                val domainTitle = getDomainDisplayName(entry.domainId)
                activeCanvas.drawText(domainTitle.take(18), 115f, y + 14f, paint)

                val details = getEntrySummaryDetails(entry)
                activeCanvas.drawText(details.take(32), 230f, y + 14f, paint)

                val notesText = entry.notes.ifBlank { "-" }
                activeCanvas.drawText(notesText.take(28), 420f, y + 14f, paint)

                y += rowHeight
                rowIndex++
            }
        }

        // Footer for last page
        drawFooter(activeCanvas, currentPageNum)
        document.finishPage(activePage)

        val reportDir = File(context.filesDir, "reports")
        if (!reportDir.exists()) reportDir.mkdirs()

        val pdfFile = File(reportDir, "CMFI_Report_${reportType}_${System.currentTimeMillis()}.pdf")
        FileOutputStream(pdfFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return pdfFile
    }

    private fun getDomainDisplayName(domainId: String): String {
        return when (domainId) {
            "ddewg" -> "DDEWG"
            "bible_reading" -> "Bible Reading"
            "prayer_alone" -> "Prayer Alone"
            "prayer_with_others" -> "Prayer With Others"
            "fasting" -> "Fasting"
            "giving" -> "Giving"
            "accountability" -> "Disciple Accountability"
            "christian_lit" -> "Christian Literature"
            "christian_lit_mem" -> "Lit. Memorization"
            "bible_mem" -> "Bible Memorization"
            "soul_winning" -> "Soul Winning"
            else -> "Custom Domain"
        }
    }

    private fun getEntrySummaryDetails(entry: AccountabilityEntryEntity): String {
        return when (entry.domainId) {
            "ddewg", "prayer_alone", "prayer_with_others" -> {
                val mins = entry.durationSeconds / 60
                val secs = entry.durationSeconds % 60
                "Duration: ${mins}m ${secs}s ${entry.notes}".trim()
            }
            "bible_reading" -> "${entry.bibleBook} ${entry.startChapter}-${entry.endChapter} (${entry.chaptersCount} chs)"
            "soul_winning" -> "Preached: ${entry.preachedToCount}, Converted: ${entry.convertedCount}"
            "giving" -> "Amount: $${entry.givingAmount} (${entry.givingType})"
            "fasting" -> "Days: ${entry.fastingDaysCount} (${entry.fastingType})"
            "christian_lit" -> "${entry.bookTitle} (${entry.pagesRead} pages)"
            else -> entry.notes.ifBlank { "Recorded entry" }
        }
    }

    private fun String?.isNull_Blank(): Boolean = this == null || this.isBlank()
}
