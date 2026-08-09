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

        // Entries Table Header
        paint.color = Color.parseColor("#E9F0FB")
        canvas.drawRect(30f, y, 565f, y + 25f, paint)

        paint.color = Color.parseColor("#0D47A1")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Date", 40f, y + 17f, paint)
        canvas.drawText("Domain", 120f, y + 17f, paint)
        canvas.drawText("Details / Measurements", 260f, y + 17f, paint)
        y += 32f

        // Itemized Entries
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 10f
        paint.color = Color.BLACK

        val maxDisplayEntries = entries.take(20)
        for (entry in maxDisplayEntries) {
            if (y > 780f) break

            canvas.drawText(entry.dateIso, 40f, y, paint)
            val domainTitle = getDomainDisplayName(entry.domainId)
            canvas.drawText(domainTitle.take(20), 120f, y, paint)

            val details = getEntrySummaryDetails(entry)
            canvas.drawText(details.take(45), 260f, y, paint)

            y += 18f
            paint.color = Color.parseColor("#EEF0F3")
            canvas.drawLine(30f, y - 5f, 565f, y - 5f, paint)
            paint.color = Color.BLACK
        }

        // Footer
        paint.color = Color.GRAY
        paint.textSize = 9f
        canvas.drawText("Generated on ${LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)} via CMFI Accountability App", 30f, 820f, paint)

        document.finishPage(page)

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
