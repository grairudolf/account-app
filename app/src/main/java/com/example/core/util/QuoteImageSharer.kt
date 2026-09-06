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
     * quote text, author, book citation, and CMFI Accap branding.
     */
    fun sharePropheticQuote(
        context: Context,
        quote: PropheticQuote,
        isFrench: Boolean = false
    ) {
        shareQuoteImage(
            context = context,
            quoteText = quote.getText(isFrench),
            author = quote.getAuthor(isFrench),
            bookCitation = quote.getBookCitation(isFrench),
            isFrench = isFrench,
            bgResId = quote.bgDrawableRes
        )
    }

    /**
     * Programmatically renders the quote onto an inspirational canvas graphic,
     * perfectly fitted with center-cropped background, author, book citation, and CMFI Accap branding.
     */
    fun shareQuoteImage(
        context: Context,
        quoteText: String,
        author: String = "Prof. Zacharias Tanee Fomum",
        bookCitation: String = "The Way of Victorious Praying",
        isFrench: Boolean = false,
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

            // 1. Draw Background Image with Aspect-Ratio Preserving Center-Crop (No stretching or distortion)
            val selectedRes = bgResId ?: R.drawable.img_quote_break_dawn_1788398693805
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inScaled = false
            }
            bgBitmap = BitmapFactory.decodeResource(context.resources, selectedRes, options)
            if (bgBitmap != null) {
                val canvasAspect = width.toFloat() / height.toFloat()
                val bitmapAspect = bgBitmap.width.toFloat() / bgBitmap.height.toFloat()
                val srcRect = if (bitmapAspect > canvasAspect) {
                    // Bitmap is wider than canvas -> crop sides
                    val targetWidth = (bgBitmap.height * canvasAspect).toInt()
                    val xOffset = (bgBitmap.width - targetWidth) / 2
                    Rect(xOffset, 0, xOffset + targetWidth, bgBitmap.height)
                } else {
                    // Bitmap is taller than canvas -> crop top and bottom
                    val targetHeight = (bgBitmap.width / canvasAspect).toInt()
                    val yOffset = (bgBitmap.height - targetHeight) / 2
                    Rect(0, yOffset, bgBitmap.width, yOffset + targetHeight)
                }
                val dstRect = Rect(0, 0, width, height)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                canvas.drawBitmap(bgBitmap, srcRect, dstRect, paint)
            } else {
                canvas.drawColor(Color.parseColor("#0A1128"))
            }

            // 2. Luminous Scrim Gradient (Preserves image beauty while guaranteeing crisp text contrast)
            val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(
                        Color.argb(70, 8, 14, 34),    // Soft top scrim (~27%)
                        Color.argb(40, 8, 14, 34),    // Subtle center (~15%)
                        Color.argb(190, 5, 10, 26)    // Solid bottom navy gradient (~75%)
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
                strokeWidth = 2.5f
            }
            canvas.drawRoundRect(24f, 24f, width - 24f, height - 24f, 24f, 24f, borderPaint)

            // 4. Main Translucent Glass Card (Spacious, balanced, and perfectly framed)
            val cardLeft = 60f
            val cardTop = 64f
            val cardRight = width - 60f
            val cardBottom = height - 156f
            val cardRect = RectF(cardLeft, cardTop, cardRight, cardBottom)

            val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#081024")
                alpha = 210
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(cardRect, 32f, 32f, cardBgPaint)

            val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E0C068")
                alpha = 130
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawRoundRect(cardRect, 32f, 32f, cardBorderPaint)

            // Decorative Quote Marks Watermark
            val quoteWatermarkPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFD700")
                alpha = 22
                textSize = 160f
                typeface = montserratBold
            }
            canvas.drawText("“", cardLeft + 36f, cardTop + 140f, quoteWatermarkPaint)
            canvas.drawText("”", cardRight - 100f, cardBottom - 120f, quoteWatermarkPaint)

            // 5. Quote Body Text with Adaptive Sizing so it fits neatly
            val quoteFontSize = when {
                cleanQuote.length > 250 -> 34f
                cleanQuote.length > 170 -> 38f
                cleanQuote.length > 90 -> 42f
                else -> 46f
            }

            val quoteBodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = quoteFontSize
                typeface = montserratMedium
                letterSpacing = 0.015f
                setShadowLayer(8f, 0f, 2f, Color.parseColor("#90000000"))
            }

            val textPaddingHorizontal = 56f
            val quoteTextWidth = (cardRight - cardLeft - (textPaddingHorizontal * 2)).toInt()
            val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(cleanQuote, 0, cleanQuote.length, quoteBodyPaint, quoteTextWidth)
                    .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                    .setLineSpacing(14f, 1.28f)
                    .setIncludePad(true)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(cleanQuote, quoteBodyPaint, quoteTextWidth, Layout.Alignment.ALIGN_NORMAL, 1.28f, 14f, true)
            }

            val citationAreaHeight = 150f
            val availableTextAreaHeight = (cardBottom - cardTop) - citationAreaHeight - 60f
            val textY = cardTop + 50f + ((availableTextAreaHeight - staticLayout.height) / 2f).coerceAtLeast(0f)

            canvas.save()
            canvas.translate(cardLeft + textPaddingHorizontal, textY)
            staticLayout.draw(canvas)
            canvas.restore()

            // 6. Divider Line & Diamond Glyph inside Card
            val dividerY = cardBottom - 105f
            val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#D4AF37")
                alpha = 90
                strokeWidth = 1.5f
            }
            canvas.drawLine(cardLeft + 60f, dividerY, width / 2f - 24f, dividerY, dividerPaint)
            canvas.drawLine(width / 2f + 24f, dividerY, cardRight - 60f, dividerY, dividerPaint)

            val diamondPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF00")
                textSize = 16f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("◆", width / 2f, dividerY + 5f, diamondPaint)

            // 7. Author & Book Citation
            val authorPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FFDF00")
                textSize = 25f
                typeface = montserratBold
                letterSpacing = 0.03f
                textAlign = Paint.Align.CENTER
            }
            val citationPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#CBD5E1")
                textSize = 20f
                typeface = bodyTypeface
                letterSpacing = 0.02f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("— $author", width / 2f, cardBottom - 64f, authorPaint)
            if (bookCitation.isNotBlank()) {
                val formattedBook = if (bookCitation.startsWith("(")) bookCitation else "📖 $bookCitation"
                canvas.drawText(formattedBook, width / 2f, cardBottom - 30f, citationPaint)
            }

            // 8. CMFI Accap Branding Footer
            val logoSize = 72f
            val logoLeft = 68f
            val logoTop = height - 118f
            var drawnLogo = false
            try {
                val logoBmp = BitmapFactory.decodeResource(context.resources, R.drawable.app_logo)
                if (logoBmp != null) {
                    val logoRect = RectF(logoLeft, logoTop, logoLeft + logoSize, logoTop + logoSize)
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
                val crossCenterX = logoLeft + 36f
                val crossCenterY = logoTop + 36f
                canvas.drawRoundRect(RectF(crossCenterX - 4f, crossCenterY - 22f, crossCenterX + 4f, crossCenterY + 22f), 4f, 4f, brandAccentPaint)
                canvas.drawRoundRect(RectF(crossCenterX - 16f, crossCenterY - 10f, crossCenterX + 16f, crossCenterY - 2f), 4f, 4f, brandAccentPaint)
            }

            val textLeft = if (drawnLogo) logoLeft + logoSize + 20f else logoLeft + logoSize + 16f

            val brandTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 30f
                typeface = montserratBold
                letterSpacing = 0.03f
            }
            canvas.drawText("CMFI Accap", textLeft, height - 82f, brandTitlePaint)

            val subtitleText = if (isFrench) "Cahier de Compte Rendu Numérique" else "Digital Accountability Notebook"
            val brandSubPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                textSize = 19f
                typeface = bodyTypeface
                letterSpacing = 0.02f
            }
            canvas.drawText(subtitleText, textLeft, height - 54f, brandSubPaint)

            // 9. Save Bitmap to File and Share
            val shareDir = File(context.cacheDir, "shared_quotes").apply { mkdirs() }
            val shareFile = File(shareDir, "cmfi_quote_${System.currentTimeMillis()}.png")
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
                append("“$cleanQuote”\n\n")
                append("— $author\n")
                if (bookCitation.isNotBlank()) {
                    append("$bookCitation\n\n")
                } else {
                    append("\n")
                }
                append("CMFI Accap • $subtitleText")
            }

            val chooserTitle = if (isFrench) "Partager la citation via :" else "Share quote via:"
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, chooserTitle))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error sharing quote: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
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

