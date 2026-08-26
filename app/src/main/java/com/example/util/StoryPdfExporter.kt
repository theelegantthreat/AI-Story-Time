package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Base64
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.Chapter
import com.example.data.Story
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StoryPdfExporter {

  private const val PAGE_WIDTH = 595 // A4 standard width in points (72 dpi)
  private const val PAGE_HEIGHT = 842 // A4 standard height in points
  private const val MARGIN_LEFT = 45f
  private const val MARGIN_RIGHT = 45f
  private const val MARGIN_TOP = 50f
  private const val MARGIN_BOTTOM = 55f
  private const val CONTENT_WIDTH = PAGE_WIDTH - MARGIN_LEFT - MARGIN_RIGHT

  suspend fun exportAndShareStory(context: Context, story: Story): Result<File> = withContext(Dispatchers.IO) {
    try {
      val pdfFile = generatePdfFile(context, story)
      withContext(Dispatchers.Main) {
        sharePdfFile(context, pdfFile, story.title)
      }
      Result.success(pdfFile)
    } catch (e: Exception) {
      e.printStackTrace()
      withContext(Dispatchers.Main) {
        Toast.makeText(context, "Failed to export PDF: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_LONG).show()
      }
      Result.failure(e)
    }
  }

  suspend fun generatePdfFile(context: Context, story: Story): File = withContext(Dispatchers.IO) {
    val pdfDocument = PdfDocument()

    val pages = mutableListOf<PdfPageBuilder>()
    var currentPage = PdfPageBuilder(PAGE_WIDTH, PAGE_HEIGHT)
    pages.add(currentPage)

    // Genre Color Scheme
    val primaryColor = (story.genre.colorHex.toInt() or 0xFF000000.toInt())
    val darkTextColor = Color.rgb(30, 27, 34)
    val subtitleTextColor = Color.rgb(85, 80, 92)
    val lightBorderColor = Color.rgb(220, 215, 228)
    val accentBgColor = Color.rgb(244, 240, 250)

    // PAGE 1: Cover & Header
    var yCursor = MARGIN_TOP

    // Decorative Top Accent Bar
    currentPage.drawTopHeaderBanner(primaryColor, story.genre.label, story.genre.icon)
    yCursor += 32f

    // Story Title
    val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      textSize = 22f
      color = primaryColor
    }
    val titleLayout = createStaticLayout(story.title, titlePaint, CONTENT_WIDTH.toInt())
    currentPage.addDrawCommand { canvas ->
      canvas.save()
      canvas.translate(MARGIN_LEFT, yCursor)
      titleLayout.draw(canvas)
      canvas.restore()
    }
    yCursor += titleLayout.height + 10f

    // Subtitle / Prompt Tagline
    val promptPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
      textSize = 11.5f
      color = subtitleTextColor
    }
    val promptText = if (story.prompt.isNotBlank()) "“${story.prompt}”" else "An AI Generated Tale"
    val promptLayout = createStaticLayout(promptText, promptPaint, CONTENT_WIDTH.toInt())
    currentPage.addDrawCommand { canvas ->
      canvas.save()
      canvas.translate(MARGIN_LEFT, yCursor)
      promptLayout.draw(canvas)
      canvas.restore()
    }
    yCursor += promptLayout.height + 12f

    // Meta Badge Bar (Genre, Voice, Chapters, Date)
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(story.createdAt))
    val metaText = "Genre: ${story.genre.label}   •   Narrator: ${story.voiceProfile.displayName}   •   Chapters: ${story.chapters.size}   •   $dateStr"
    val metaPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
      textSize = 9.5f
      color = Color.rgb(110, 105, 120)
    }
    val metaLayout = createStaticLayout(metaText, metaPaint, CONTENT_WIDTH.toInt())

    currentPage.addDrawCommand { canvas ->
      // Meta box background
      val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = accentBgColor
        style = Paint.Style.FILL
      }
      val rect = RectF(MARGIN_LEFT, yCursor - 4f, MARGIN_LEFT + CONTENT_WIDTH, yCursor + metaLayout.height + 6f)
      canvas.drawRoundRect(rect, 8f, 8f, bgPaint)

      val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = lightBorderColor
        style = Paint.Style.STROKE
        strokeWidth = 0.8f
      }
      canvas.drawRoundRect(rect, 8f, 8f, borderPaint)

      canvas.save()
      canvas.translate(MARGIN_LEFT + 10f, yCursor + 2f)
      metaLayout.draw(canvas)
      canvas.restore()
    }
    yCursor += metaLayout.height + 22f

    // Process all chapters
    val bodyPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
      textSize = 11.5f
      color = darkTextColor
    }
    val chapterTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
      textSize = 15f
      color = primaryColor
    }

    story.chapters.forEachIndexed { chIndex, chapter ->
      // Calculate needed space for chapter header
      val chapterHeading = "Chapter ${chIndex + 1}: ${chapter.title.ifBlank { "Untitled Chapter" }}"
      val headingLayout = createStaticLayout(chapterHeading, chapterTitlePaint, CONTENT_WIDTH.toInt())

      // Decode chapter image if present
      val chapterBitmap = decodeBase64Bitmap(chapter.imageBase64)
      val imageDisplayHeight = if (chapterBitmap != null) {
        val aspect = chapterBitmap.height.toFloat() / chapterBitmap.width.toFloat().coerceAtLeast(1f)
        (CONTENT_WIDTH * 0.55f * aspect).coerceIn(120f, 210f)
      } else 0f

      // If we don't have enough space for heading + image (or heading + some text), start a new page
      val neededHeaderSpace = headingLayout.height + 15f + (if (imageDisplayHeight > 0) imageDisplayHeight + 15f else 0f) + 40f
      if (yCursor + neededHeaderSpace > PAGE_HEIGHT - MARGIN_BOTTOM) {
        currentPage = PdfPageBuilder(PAGE_WIDTH, PAGE_HEIGHT)
        pages.add(currentPage)
        yCursor = MARGIN_TOP + 15f
      }

      // Draw Chapter Heading
      val finalHeadingY = yCursor
      currentPage.addDrawCommand { canvas ->
        // Chapter indicator icon or badge line
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = primaryColor
          strokeWidth = 2f
        }
        canvas.drawLine(MARGIN_LEFT, finalHeadingY + 3f, MARGIN_LEFT + 20f, finalHeadingY + 3f, linePaint)

        canvas.save()
        canvas.translate(MARGIN_LEFT + 26f, finalHeadingY - 6f)
        headingLayout.draw(canvas)
        canvas.restore()
      }
      yCursor += headingLayout.height + 10f

      // Draw Image if available
      if (chapterBitmap != null && imageDisplayHeight > 0f) {
        val imageY = yCursor
        val imageWidth = CONTENT_WIDTH * 0.75f
        val imageLeft = MARGIN_LEFT + (CONTENT_WIDTH - imageWidth) / 2f
        currentPage.addDrawCommand { canvas ->
          val dstRect = RectF(imageLeft, imageY, imageLeft + imageWidth, imageY + imageDisplayHeight)
          val bgCardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(230, 225, 235)
          }
          canvas.drawRoundRect(dstRect, 12f, 12f, bgCardPaint)
          canvas.drawBitmap(chapterBitmap, null, dstRect, Paint(Paint.FILTER_BITMAP_FLAG))

          val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lightBorderColor
            style = Paint.Style.STROKE
            strokeWidth = 1f
          }
          canvas.drawRoundRect(dstRect, 12f, 12f, framePaint)
        }
        yCursor += imageDisplayHeight + 14f
      }

      // Split paragraphs
      val paragraphs = chapter.content.split("\n\n", "\n").filter { it.isNotBlank() }
      paragraphs.forEach { paragraph ->
        val paraLayout = createStaticLayout(paragraph.trim(), bodyPaint, CONTENT_WIDTH.toInt())
        val paraHeight = paraLayout.height

        // Check if paragraph fits on current page
        if (yCursor + paraHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
          // New page
          currentPage = PdfPageBuilder(PAGE_WIDTH, PAGE_HEIGHT)
          pages.add(currentPage)
          yCursor = MARGIN_TOP + 15f
        }

        val paraY = yCursor
        currentPage.addDrawCommand { canvas ->
          canvas.save()
          canvas.translate(MARGIN_LEFT, paraY)
          paraLayout.draw(canvas)
          canvas.restore()
        }
        yCursor += paraHeight + 10f
      }

      // Draw Continuation branch options if present
      if (chapter.continuationOptions.isNotEmpty()) {
        val boxTitle = "Branching Paths & Continuations:"
        val boxTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
          typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
          textSize = 10f
          color = primaryColor
        }
        val optionPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
          typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
          textSize = 9.5f
          color = subtitleTextColor
        }

        val optionsText = StringBuilder()
        chapter.continuationOptions.forEachIndexed { idx, opt ->
          optionsText.append("${idx + 1}. ${opt.branchTitle}: ${opt.teaser}\n")
        }
        val optionsLayout = createStaticLayout(optionsText.toString().trimEnd(), optionPaint, (CONTENT_WIDTH - 20).toInt())
        val totalBoxHeight = optionsLayout.height + 26f

        if (yCursor + totalBoxHeight > PAGE_HEIGHT - MARGIN_BOTTOM) {
          currentPage = PdfPageBuilder(PAGE_WIDTH, PAGE_HEIGHT)
          pages.add(currentPage)
          yCursor = MARGIN_TOP + 15f
        }

        val boxY = yCursor
        currentPage.addDrawCommand { canvas ->
          val boxRect = RectF(MARGIN_LEFT, boxY, MARGIN_LEFT + CONTENT_WIDTH, boxY + totalBoxHeight)
          val boxBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accentBgColor
            style = Paint.Style.FILL
          }
          canvas.drawRoundRect(boxRect, 8f, 8f, boxBg)

          val boxBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = lightBorderColor
            style = Paint.Style.STROKE
            strokeWidth = 0.8f
          }
          canvas.drawRoundRect(boxRect, 8f, 8f, boxBorder)

          canvas.drawText("✦ $boxTitle", MARGIN_LEFT + 10f, boxY + 14f, boxTitlePaint)

          canvas.save()
          canvas.translate(MARGIN_LEFT + 10f, boxY + 20f)
          optionsLayout.draw(canvas)
          canvas.restore()
        }
        yCursor += totalBoxHeight + 14f
      }

      // Small spacing after chapter
      yCursor += 12f
    }

    // Render all pages to the PdfDocument
    val totalPages = pages.size
    pages.forEachIndexed { index, builder ->
      val pageNumber = index + 1
      val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
      val page = pdfDocument.startPage(pageInfo)
      val canvas = page.canvas

      // Fill clean background
      canvas.drawColor(Color.WHITE)

      // Execute all draw commands
      builder.draw(canvas)

      // Draw footer on each page
      drawPageFooter(canvas, story.title, pageNumber, totalPages, primaryColor)

      pdfDocument.finishPage(page)
    }

    // Save to Cache Directory
    val sanitizedTitle = story.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30).ifBlank { "Story" }
    val fileName = "Story_${sanitizedTitle}_${System.currentTimeMillis()}.pdf"
    val outputDir = File(context.cacheDir, "story_exports").apply { mkdirs() }
    val outputFile = File(outputDir, fileName)

    val fos = FileOutputStream(outputFile)
    pdfDocument.writeTo(fos)
    fos.flush()
    fos.close()
    pdfDocument.close()

    outputFile
  }

  private fun sharePdfFile(context: Context, pdfFile: File, title: String) {
    try {
      val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        pdfFile
      )

      val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, "Read this illustrated tale: \"$title\" created with AI Story Weaver.")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
      }

      val chooser = Intent.createChooser(intent, "Share Storybook PDF")
      chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      context.startActivity(chooser)
    } catch (e: Exception) {
      e.printStackTrace()
      Toast.makeText(context, "Error sharing PDF: ${e.message}", Toast.LENGTH_LONG).show()
    }
  }

  private fun drawPageFooter(canvas: Canvas, storyTitle: String, pageNumber: Int, totalPages: Int, primaryColor: Int) {
    val footerPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
      textSize = 9f
      color = Color.rgb(140, 135, 150)
    }

    val footerLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      color = Color.rgb(230, 225, 235)
      strokeWidth = 0.8f
    }

    val footerY = PAGE_HEIGHT - MARGIN_BOTTOM + 20f
    canvas.drawLine(MARGIN_LEFT, footerY - 10f, MARGIN_LEFT + CONTENT_WIDTH, footerY - 10f, footerLinePaint)

    val leftText = storyTitle.take(35) + if (storyTitle.length > 35) "..." else ""
    canvas.drawText(leftText, MARGIN_LEFT, footerY + 2f, footerPaint)

    val rightText = "Page $pageNumber of $totalPages • AI Story Weaver"
    val rightWidth = footerPaint.measureText(rightText)
    canvas.drawText(rightText, MARGIN_LEFT + CONTENT_WIDTH - rightWidth, footerY + 2f, footerPaint)
  }

  private fun decodeBase64Bitmap(base64Str: String?): Bitmap? {
    if (base64Str.isNullOrBlank()) return null
    return try {
      val cleanStr = if (base64Str.contains(",")) {
        base64Str.substringAfter(",")
      } else {
        base64Str
      }
      val decodedBytes = Base64.decode(cleanStr, Base64.DEFAULT)
      BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
      null
    }
  }

  @Suppress("DEPRECATION")
  private fun createStaticLayout(text: String, paint: TextPaint, width: Int): StaticLayout {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      StaticLayout.Builder.obtain(text, 0, text.length, paint, width)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL)
        .setLineSpacing(0f, 1.25f)
        .setIncludePad(false)
        .build()
    } else {
      StaticLayout(text, paint, width, Layout.Alignment.ALIGN_NORMAL, 1.25f, 0f, false)
    }
  }

  private class PdfPageBuilder(val width: Int, val height: Int) {
    private val drawCommands = mutableListOf<(Canvas) -> Unit>()

    fun addDrawCommand(command: (Canvas) -> Unit) {
      drawCommands.add(command)
    }

    fun drawTopHeaderBanner(primaryColor: Int, genreLabel: String, icon: String) {
      addDrawCommand { canvas ->
        val bannerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
          color = primaryColor
          style = Paint.Style.FILL
        }
        val bannerRect = RectF(MARGIN_LEFT, MARGIN_TOP - 18f, MARGIN_LEFT + CONTENT_WIDTH, MARGIN_TOP + 8f)
        canvas.drawRoundRect(bannerRect, 6f, 6f, bannerPaint)

        val bannerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
          typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
          textSize = 10f
          color = Color.WHITE
        }
        canvas.drawText("$icon $genreLabel • STORYBOOK EDITION", MARGIN_LEFT + 12f, MARGIN_TOP - 1f, bannerTextPaint)
      }
    }

    fun draw(canvas: Canvas) {
      drawCommands.forEach { it(canvas) }
    }
  }
}
