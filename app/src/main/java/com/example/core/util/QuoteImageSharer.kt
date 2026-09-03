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
            val height = 1080
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
                canvas.drawColor(Color.parseColor("#14214C"))
            }

            // 2. Uplifting, Luminous Scrim Gradient (Keeps image bright and joyful while aiding contrast)
            val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.argb(75, 10, 20, 45),   // Soft top scrim (~30%)
                        Color.argb(35, 10, 20, 45),   // Luminous center (~14%)
                        Color.argb(130, 7, 11, 30)    // Soft bottom gradient (~50%)
                    ),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

            // 3. Subtle Outer Gold Accent Border
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0C068")
                alpha = 80
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(30f, 30f, width - 30f, height - 30f, 28f, 28f, borderPaint)

            // 4. PROMINENT 3B PROPHETIC MESSAGES HEADER HIGHLIGHT
            // 4a. Main Glowing Gold Ribbon / Badge
            val badgeWidth = 720f
            val badgeHeight = 62f
            val badgeLeft = (width - badgeWidth) / 2f
            val badgeTop = 60f
            val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeWidth, badgeTop + badgeHeight)

            val badgeBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#101A33")
                alpha = 230
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(badgeRect, 31f, 31f, badgeBgPaint)

            val badgeStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700")
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(badgeRect, 31f, 31f, badgeStrokePaint)

            val badgeTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF00")
                textSize = 25f
                typeface = montserratBold
                letterSpacing = 0.10f
                textAlign = Paint.Align.CENTER
            }
            val formattedTitle = if (title.contains("3B", ignoreCase = true)) {
                title.uppercase()
            } else {
                "✦ THE 3B PROPHETIC MESSAGES ✦"
            }
            val displayHeader = if (formattedTitle.startsWith("✦")) formattedTitle else "✦  $formattedTitle  ✦"
            canvas.drawText(displayHeader, width / 2f, badgeTop + 41f, badgeTextPaint)

            // 4b. Prophecy Specific Source Sub-Pill
            val pillWidth = 540f
            val pillHeight = 46f
            val pillLeft = (width - pillWidth) / 2f
            val pillTop = badgeTop + badgeHeight + 16f
            val pillRect = RectF(pillLeft, pillTop, pillLeft + pillWidth, pillTop + pillHeight)

            val pillBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1E293B")
                alpha = 210
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(pillRect, 23f, 23f, pillBgPaint)

            val pillStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                alpha = 100
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
            }
            canvas.drawRoundRect(pillRect, 23f, 23f, pillStrokePaint)

            val pillTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 21f
                typeface = montserratSemiBold
                letterSpacing = 0.06f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(prophecySource.uppercase(), width / 2f, pillTop + 31f, pillTextPaint)

            // 4c. Theme Tag (Freedom from Sin, Steadfast Walk, etc.)
            val themeTagPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FBBF24")
                textSize = 19f
                typeface = montserratMedium
                letterSpacing = 0.08f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("•  ${themeTag.uppercase()}  •", width / 2f, pillTop + pillHeight + 28f, themeTagPaint)

            // 5. Translucent Frosted Glass Card for Quote Readability
            val cardLeft = 65f
            val cardTop = pillTop + pillHeight + 46f
            val cardRight = width - 65f
            val cardBottom = height - 170f
            val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)

            val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#091124")
                alpha = 185
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(cardRect, 32f, 32f, cardBgPaint)

            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0C068")
                alpha = 110
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            canvas.drawRoundRect(cardRect, 32f, 32f, cardBorderPaint)

            // Decorative Quote Mark
            val quoteMarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700")
                alpha = 140
                textSize = 110f
                typeface = montserratBold
            }
            canvas.drawText("“", cardLeft + 35f, cardTop + 85f, quoteMarkPaint)

            // 6. Main Quote Body Text (Centered inside Card with subtle drop shadow)
            val quoteBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = if (cleanQuote.length > 200) 37f else if (cleanQuote.length > 130) 43f else 49f
                typeface = montserratMedium
                letterSpacing = 0.015f
                setShadowLayer(8f, 0f, 2f, Color.parseColor("#80000000"))
            }

            val textPaddingHorizontal = 50f
            val quoteTextWidth = (cardRight - cardLeft - (textPaddingHorizontal * 2)).toInt()
            val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(cleanQuote, 0, cleanQuote.length, quoteBodyPaint, quoteTextWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(14f, 1.20f)
                    .setIncludePad(true)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(cleanQuote, quoteBodyPaint, quoteTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.20f, 14f, true)
            }

            val cardAvailableHeight = cardBottom - cardTop - 150f
            val textY = cardTop + 75f + ((cardAvailableHeight - staticLayout.height) / 2f).coerceAtLeast(0f)

            canvas.save()
            canvas.translate(cardLeft + textPaddingHorizontal, textY)
            staticLayout.draw(canvas)
            canvas.restore()

            // 7. Divider Accent Line inside card
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D4AF37")
                alpha = 90
                strokeWidth = 2f
            }
            canvas.drawLine(cardLeft + 50f, cardBottom - 65f, cardRight - 50f, cardBottom - 65f, dividerPaint)

            // Citation: Author & Book
            val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF00")
                textSize = 21f
                typeface = montserratSemiBold
                letterSpacing = 0.04f
                textAlign = Paint.Align.CENTER
            }
            val citationPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#BAC5D6")
                textSize = 18f
                typeface = bodyTypeface
                textAlign = Paint.Align.CENTER
            }
            val citationY = cardBottom - 38f
            canvas.drawText("— $author", width / 2f, citationY, authorPaint)
            canvas.drawText("($bookCitation)", width / 2f, cardBottom - 14f, citationPaint)

            // 8. Official App Branding (Bottom Bar) with App Logo
            var drawnLogo = false
            try {
                val logoBmp = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
                if (logoBmp != null) {
                    val logoRect = RectF(65f, height - 135f, 129f, height - 71f)
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
                val crossCenterX = 90f
                val crossCenterY = height - 100f
                canvas.drawRoundRect(RectF(crossCenterX - 4f, crossCenterY - 22f, crossCenterX + 4f, crossCenterY + 22f), 4f, 4f, brandAccentPaint)
                canvas.drawRoundRect(RectF(crossCenterX - 16f, crossCenterY - 11f, crossCenterX + 16f, crossCenterY - 3f), 4f, 4f, brandAccentPaint)
            }

            val brandTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 30f
                typeface = montserratBold
                letterSpacing = 0.04f
            }
            val textLeft = if (drawnLogo) 145f else 130f
            canvas.drawText("CMFI Accap", textLeft, height - 105f, brandTitlePaint)

            val brandSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CBD5E1")
                textSize = 19f
                typeface = bodyTypeface
            }
            canvas.drawText("Digital Account Book • Overcomers Movement", textLeft, height - 78f, brandSubPaint)

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
