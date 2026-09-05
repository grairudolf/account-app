package com.example.core.util

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.domain.models.PropheticQuote
import java.io.File
import java.io.FileOutputStream

object QuoteImageSharer {

    /**
     * Shares a complete PropheticQuote with matched background image,
     * highlighting the 3B Prophetic Messages and book citation prominently.
     */
    fun sharePropheticQuote(
        context: Context,
        quote: PropheticQuote,
        isFrench: Boolean = false
    ) {
        shareQuoteImage(
            context = context,
            quoteText = quote.getText(isFrench),
            title = if (isFrench) "LES MESSAGES PROPHÉTIQUES 3B" else "THE 3B PROPHETIC MESSAGES",
            prophecySource = quote.getProphecySource(isFrench),
            themeTag = quote.getThemeTag(isFrench),
            bookCitation = quote.getBookCitation(isFrench),
            author = quote.getAuthor(isFrench),
            bgResId = quote.bgDrawableRes
        )
    }

    /**
     * Programmatically renders the devotional quote onto an inspirational canvas graphic,
     * with the 3B Prophetic Messages prominently highlighted, and launches the native OS share sheet.
     */
    fun shareQuoteImage(
        context: Context,
        quoteText: String,
        title: String = "THE 3B PROPHETIC MESSAGES",
        prophecySource: String = "The Bertoua Message",
        themeTag: String = "FREEDOM FROM ALL SIN",
        bookCitation: String = "Practical Helps For Overcomers (Book 26)",
        author: String = "Pr. Zacharias Tanee Fomum",
        bgResId: Int? = null
    ) {
        var bitmap: Bitmap? = null
        var bgBitmap: Bitmap? = null
        try {
            val cleanQuote = quoteText.trim().trim('“', '”', '"', '\'').trim()
            val width = 1080
            val height = 1350
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Load Custom Font Typefaces safely
            val montserratMedium = try {
                ResourcesCompat.getFont(context, R.font.montserrat_medium) ?: Typeface.DEFAULT
            } catch (e: Throwable) {
                Typeface.DEFAULT
            }

            val montserratBold = try {
                ResourcesCompat.getFont(context, R.font.montserrat_bold) ?: Typeface.DEFAULT_BOLD
            } catch (e: Throwable) {
                Typeface.DEFAULT_BOLD
            }

            val montserratSemiBold = try {
                ResourcesCompat.getFont(context, R.font.montserrat_semibold) ?: Typeface.DEFAULT_BOLD
            } catch (e: Throwable) {
                Typeface.DEFAULT_BOLD
            }

            val bodyTypeface = try {
                ResourcesCompat.getFont(context, R.font.poppins_regular) ?: Typeface.DEFAULT
            } catch (e: Throwable) {
                Typeface.DEFAULT
            }

            // 1. Draw Background Image (scaled to fill)
            val selectedRes = bgResId ?: R.drawable.img_quote_break_dawn_1788398693805
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }
            bgBitmap = BitmapFactory.decodeResource(context.resources, selectedRes, options)
            if (bgBitmap != null) {
                val srcRect = Rect(0, 0, bgBitmap.width, bgBitmap.height)
                val dstRect = Rect(0, 0, width, height)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(bgBitmap, srcRect, dstRect, paint)
            } else {
                // Fallback royal navy
                canvas.drawColor(Color.parseColor("#0A1128"))
            }

            // 2. Uplifting, Luminous Scrim Gradient (Keeps image bright and joyful while aiding contrast)
            val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.argb(90, 8, 14, 34),    // Soft top scrim (~35%)
                        Color.argb(45, 8, 14, 34),    // Luminous center (~18%)
                        Color.argb(185, 5, 10, 26)    // Deep bottom navy gradient (~72%)
                    ),
                    floatArrayOf(0f, 0.42f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

            // 3. Subtle Outer Gold Accent Double Border
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0C068")
                alpha = 95
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(28f, 28f, width - 28f, height - 28f, 28f, 28f, borderPaint)

            val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF00")
                alpha = 45
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRoundRect(36f, 36f, width - 36f, height - 36f, 22f, 22f, innerBorderPaint)

            // 4. PROMINENT 3B PROPHETIC MESSAGES HEADER HIGHLIGHT
            // 4a. Main Glowing Gold Ribbon / Badge
            val badgeWidth = 760f
            val badgeHeight = 64f
            val badgeLeft = (width - badgeWidth) / 2f
            val badgeTop = 64f
            val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeWidth, badgeTop + badgeHeight)

            val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#091124")
                alpha = 245
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(badgeRect, 32f, 32f, badgeBgPaint)

            val badgeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F59E0B")
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            canvas.drawRoundRect(badgeRect, 32f, 32f, badgeStrokePaint)

            val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF00")
                textSize = 24f
                typeface = montserratBold
                letterSpacing = 0.12f
                textAlign = Paint.Align.CENTER
            }
            val formattedTitle = if (title.contains("3B", ignoreCase = true)) {
                title.uppercase()
            } else {
                "✦ THE 3B PROPHETIC MESSAGES ✦"
            }
            val displayHeader = if (formattedTitle.startsWith("✦")) formattedTitle else "✦  $formattedTitle  ✦"
            canvas.drawText(displayHeader, width / 2f, badgeTop + 42f, badgeTextPaint)

            // 4b. Unified Secondary Pill: Source + Theme Tag (Clean and organized)
            val subPillText = if (themeTag.isNotBlank() && !themeTag.equals(prophecySource, ignoreCase = true)) {
                "${prophecySource.uppercase()}  •  ${themeTag.uppercase()}"
            } else {
                prophecySource.uppercase()
            }
            val subPillWidth = 640f
            val subPillHeight = 44f
            val subPillLeft = (width - subPillWidth) / 2f
            val subPillTop = badgeTop + badgeHeight + 14f
            val subPillRect = RectF(subPillLeft, subPillTop, subPillLeft + subPillWidth, subPillTop + subPillHeight)

            val subPillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                alpha = 230
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(subPillRect, 22f, 22f, subPillBgPaint)

            val subPillStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                alpha = 130
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRoundRect(subPillRect, 22f, 22f, subPillStrokePaint)

            val subPillTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E2E8F0")
                textSize = 19f
                typeface = montserratSemiBold
                letterSpacing = 0.06f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(subPillText, width / 2f, subPillTop + 29f, subPillTextPaint)

            // 5. Translucent Frosted Glass Card for Quote Readability
            val cardLeft = 60f
            val cardTop = subPillTop + subPillHeight + 24f
            val cardRight = width - 60f
            val cardBottom = height - 175f
            val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)

            val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#070F22")
                alpha = 215
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(cardRect, 36f, 36f, cardBgPaint)

            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0C068")
                alpha = 140
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            canvas.drawRoundRect(cardRect, 36f, 36f, cardBorderPaint)

            // Watermark Decorative Quote Marks (Faint in background, does not collide with text)
            val quoteWatermarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700")
                alpha = 25
                textSize = 150f
                typeface = montserratBold
            }
            canvas.drawText("“", cardLeft + 36f, cardTop + 140f, quoteWatermarkPaint)
            canvas.drawText("”", cardRight - 100f, cardBottom - 110f, quoteWatermarkPaint)

            // 6. Main Quote Body Text (Adaptive sizing based on length)
            val quoteFontSize = when {
                cleanQuote.length > 240 -> 32f
                cleanQuote.length > 150 -> 36f
                cleanQuote.length > 90 -> 40f
                else -> 44f
            }

            val quoteBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = quoteFontSize
                typeface = montserratMedium
                letterSpacing = 0.015f
                setShadowLayer(8f, 0f, 2f, Color.parseColor("#A0000000"))
            }

            val textPaddingHorizontal = 56f
            val quoteTextWidth = (cardRight - cardLeft - (textPaddingHorizontal * 2)).toInt()
            val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(cleanQuote, 0, cleanQuote.length, quoteBodyPaint, quoteTextWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(14f, 1.26f)
                    .setIncludePad(true)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(cleanQuote, quoteBodyPaint, quoteTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.26f, 14f, true)
            }

            val cardAvailableHeight = cardBottom - cardTop - 160f
            val textY = cardTop + 45f + ((cardAvailableHeight - staticLayout.height) / 2f).coerceAtLeast(0f)

            canvas.save()
            canvas.translate(cardLeft + textPaddingHorizontal, textY)
            staticLayout.draw(canvas)
            canvas.restore()

            // 7. Divider Accent Line inside card with Diamond Glyph
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D4AF37")
                alpha = 110
                strokeWidth = 2f
            }
            val dividerY = cardBottom - 96f
            canvas.drawLine(cardLeft + 60f, dividerY, width / 2f - 24f, dividerY, dividerPaint)
            canvas.drawLine(width / 2f + 24f, dividerY, cardRight - 60f, dividerY, dividerPaint)

            val diamondPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF00")
                textSize = 18f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("◆", width / 2f, dividerY + 6f, diamondPaint)

            // Citation: Author on top line, Book on second line
            val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF00")
                textSize = 23f
                typeface = montserratBold
                letterSpacing = 0.04f
                textAlign = Paint.Align.CENTER
            }
            val citationPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CBD5E1")
                textSize = 19f
                typeface = bodyTypeface
                letterSpacing = 0.02f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("— $author", width / 2f, cardBottom - 58f, authorPaint)
            if (bookCitation.isNotBlank()) {
                val formattedBook = if (bookCitation.startsWith("(")) bookCitation else "📖 $bookCitation"
                canvas.drawText(formattedBook, width / 2f, cardBottom - 26f, citationPaint)
            }

            // 8. Official App Branding (Bottom Bar) with App Logo
            var drawnLogo = false
            try {
                val logoBmp = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
                if (logoBmp != null) {
                    val logoRect = RectF(65f, height - 142f, 137f, height - 70f)
                    canvas.drawBitmap(logoBmp, null, logoRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                    drawnLogo = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (!drawnLogo) {
                val brandAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#FFD700")
                }
                val crossCenterX = 95f
                val crossCenterY = height - 106f
                canvas.drawRoundRect(RectF(crossCenterX - 4f, crossCenterY - 24f, crossCenterX + 4f, crossCenterY + 24f), 4f, 4f, brandAccentPaint)
                canvas.drawRoundRect(RectF(crossCenterX - 18f, crossCenterY - 12f, crossCenterX + 18f, crossCenterY - 4f), 4f, 4f, brandAccentPaint)
            }

            val brandTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 30f
                typeface = montserratBold
                letterSpacing = 0.04f
            }
            val textLeft = if (drawnLogo) 155f else 138f
            canvas.drawText("CMFI Accap", textLeft, height - 112f, brandTitlePaint)

            val brandSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                textSize = 19f
                typeface = bodyTypeface
            }
            canvas.drawText("Cahier du Compte Rendu Numérique • Digital Accountability Notebook", textLeft, height - 82f, brandSubPaint)

            // Right Tag Badge
            val tagBadgeWidth = 220f
            val tagBadgeHeight = 36f
            val tagBadgeRect = RectF(width - 60f - tagBadgeWidth, height - 122f, width - 60f, height - 86f)
            val tagBadgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                alpha = 200
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(tagBadgeRect, 18f, 18f, tagBadgeBgPaint)
            val tagBadgeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0C068")
                alpha = 100
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRoundRect(tagBadgeRect, 18f, 18f, tagBadgeStrokePaint)
            val tagBadgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FDE047")
                textSize = 14f
                typeface = montserratSemiBold
                letterSpacing = 0.05f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("✦ SPIRITUAL LIFE", tagBadgeRect.centerX(), tagBadgeRect.centerY() + 5f, tagBadgeTextPaint)

            // 9. Save Bitmap to File and Share
            val shareDir = File(context.cacheDir, "shared_quotes").apply { mkdirs() }
            val shareFile = File(shareDir, "3b_prophetic_message_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(shareFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                shareFile
            )

            val shareText = buildString {
                append("📖 THE 3B PROPHETIC MESSAGES\n")
                append("— $prophecySource\n\n")
                append("“$cleanQuote”\n\n")
                append("— $author\n")
                append("($bookCitation)\n\n")
                append("Shared via CMFI Accap • Digital Account Book")
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share 3B Prophetic Message via:"))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing quote graphic: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        } finally {
            try {
                bgBitmap?.recycle()
                bitmap?.recycle()
            } catch (e: Exception) {
                // Ignore cleanup errors
            }
        }
    }
}
