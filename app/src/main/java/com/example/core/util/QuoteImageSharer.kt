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
import java.io.File
import java.io.FileOutputStream

object QuoteImageSharer {

    private val devotionalBgImages = listOf(
        R.drawable.quote_bg_open_bible_1788139223471,
        R.drawable.quote_bg_global_harvest_1788139235063,
        R.drawable.quote_bg_prayer_altar_1788139251276,
        R.drawable.quote_bg_radiant_cross_1788139262304,
        R.drawable.quote_bg_cross_1787235555876,
        R.drawable.quote_bg_mountains_1787235541853,
        R.drawable.quote_bg_sunrise_1787220672419,
        R.drawable.quote_bg_heavens_1787220708792,
        R.drawable.quote_bg_path_1787220696837,
        R.drawable.quote_bg_waters_1787220685176,
        R.drawable.devotional_quote_bg_1787144263336
    )

    /**
     * Programmatically renders the devotional quote onto an inspirational canvas graphic,
     * adds CMFI Accap branding at the bottom, and launches the native OS share sheet.
     */
    fun shareQuoteImage(
        context: Context,
        quoteText: String,
        title: String = "DAILY WORD OF ENCOURAGEMENT",
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
            val montserratTypeface = try {
                ResourcesCompat.getFont(context, R.font.montserrat_medium) ?: Typeface.DEFAULT
            } catch (e: Throwable) {
                Typeface.DEFAULT
            }

            val headingTypeface = try {
                ResourcesCompat.getFont(context, R.font.montserrat_bold) ?: Typeface.DEFAULT_BOLD
            } catch (e: Throwable) {
                Typeface.DEFAULT_BOLD
            }

            val bodyTypeface = try {
                ResourcesCompat.getFont(context, R.font.poppins_regular) ?: Typeface.DEFAULT
            } catch (e: Throwable) {
                Typeface.DEFAULT
            }

            // 1. Draw Background Image (rotating daily or provided)
            val selectedRes = bgResId ?: run {
                val day = java.time.LocalDate.now().dayOfYear
                devotionalBgImages[day % devotionalBgImages.size]
            }

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
                // Fallback deep navy background
                canvas.drawColor(android.graphics.Color.parseColor("#14214C"))
            }

            // 2. Readability Semi-transparent Gradient Overlay
            val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        android.graphics.Color.parseColor("#8014214C"), // ~50%
                        android.graphics.Color.parseColor("#B30D1636"), // ~70%
                        android.graphics.Color.parseColor("#E60A1028")  // ~90% bottom
                    ),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), gradientPaint)

            // 3. Subtle Gold Border Frame
            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#5F6987")
                alpha = 90
                style = Paint.Style.STROKE
                strokeWidth = 3f
            }
            canvas.drawRoundRect(40f, 40f, width - 40f, height - 40f, 32f, 32f, borderPaint)

            // 4. Header Category Tag
            val tagBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#696240")
                alpha = 180
            }
            val tagRect = RectF(100f, 90f, 100f + 560f, 150f)
            canvas.drawRoundRect(tagRect, 30f, 30f, tagBgPaint)

            val tagTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#FAE611")
                textSize = 26f
                typeface = headingTypeface
                letterSpacing = 0.06f
            }
            canvas.drawText("✦  $title", 125f, 130f, tagTextPaint)

            // 5. Decorative Large Quotation Mark
            val quoteMarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#FDBC0A")
                alpha = 140
                textSize = 140f
                typeface = headingTypeface
            }
            canvas.drawText("“", 100f, 260f, quoteMarkPaint)

            // 6. Main Quote Body Text (Centered & Balanced using Montserrat font)
            val quoteBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = if (cleanQuote.length > 220) 38f else if (cleanQuote.length > 140) 44f else 50f
                typeface = montserratTypeface
                letterSpacing = 0.01f
            }

            val textWidth = width - 200
            val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(cleanQuote, 0, cleanQuote.length, quoteBodyPaint, textWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(14f, 1.18f)
                    .setIncludePad(true)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(cleanQuote, quoteBodyPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.18f, 14f, true)
            }

            canvas.save()
            // Center the text block vertically in the upper 75% of the card
            val availableHeight = height - 360f - 180f
            val textY = 280f + ((availableHeight - staticLayout.height) / 2f).coerceAtLeast(0f)
            canvas.translate(100f, textY)
            staticLayout.draw(canvas)
            canvas.restore()

            // 7. Divider Accent Line
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#5F6987")
                alpha = 120
                strokeWidth = 2f
            }
            canvas.drawLine(100f, height - 160f, width - 100f, height - 160f, dividerPaint)

            // 8. Official App Branding (Bottom Bar) with App Logo
            var drawnLogo = false
            try {
                val logoBmp = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
                if (logoBmp != null) {
                    val logoRect = RectF(100f, height - 145f, 164f, height - 81f)
                    canvas.drawBitmap(logoBmp, null, logoRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                    drawnLogo = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (!drawnLogo) {
                // Fallback Gold Accent
                val brandAccentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#FDBC0A")
                }
                val crossCenterX = 125f
                val crossCenterY = height - 105f
                canvas.drawRoundRect(RectF(crossCenterX - 4f, crossCenterY - 24f, crossCenterX + 4f, crossCenterY + 24f), 4f, 4f, brandAccentPaint)
                canvas.drawRoundRect(RectF(crossCenterX - 18f, crossCenterY - 12f, crossCenterX + 18f, crossCenterY - 4f), 4f, 4f, brandAccentPaint)
            }

            // App Name & Tagline Text updated to "CMFI Accap" and "Digital Account Book"
            val brandTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.WHITE
                textSize = 32f
                typeface = headingTypeface
                letterSpacing = 0.04f
            }
            val textLeft = if (drawnLogo) 180f else 160f
            canvas.drawText("CMFI Accap", textLeft, height - 115f, brandTitlePaint)

            val brandSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#CACED7")
                textSize = 22f
                typeface = bodyTypeface
            }
            canvas.drawText("Digital Account Book", textLeft, height - 85f, brandSubPaint)

            // 9. Save Bitmap to File and Share
            val shareDir = File(context.cacheDir, "shared_quotes").apply { mkdirs() }
            val shareFile = File(shareDir, "daily_encouragement_${System.currentTimeMillis()}.png")
            val outputStream = FileOutputStream(shareFile)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
            outputStream.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                shareFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, "✝️ $cleanQuote\n\n— CMFI Accap • Digital Account Book")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Encouragement Card via:"))
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
