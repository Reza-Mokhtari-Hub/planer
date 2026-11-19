package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.model.PdfOrientation
import com.example.model.PdfTemplateType
import com.example.model.TaskItem
import com.example.model.WeekStartOption
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class ExportManager(private val context: Context) {

    private val regularTypeface: Typeface by lazy {
        ResourcesCompat.getFont(context, R.font.vazirmatn) ?: Typeface.DEFAULT
    }

    private val boldTypeface: Typeface by lazy {
        ResourcesCompat.getFont(context, R.font.vazirmatn_bold) ?: Typeface.DEFAULT_BOLD
    }

    fun renderScheduleBitmap(
        weekTitle: String,
        tasks: List<TaskItem>,
        weekStart: WeekStartOption,
        usePersianDigits: Boolean,
        template: PdfTemplateType,
        orientation: PdfOrientation,
        includeStats: Boolean,
        includeNotes: Boolean,
        scaleFactor: Float = 1.6f
    ): Bitmap {
        val baseWidth = if (orientation == PdfOrientation.LANDSCAPE) 1188 else 840
        val baseHeight = if (orientation == PdfOrientation.LANDSCAPE) 840 else 1188
        val width = (baseWidth * scaleFactor).toInt()
        val height = (baseHeight * scaleFactor).toInt()

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.scale(scaleFactor, scaleFactor)
        drawScheduleOnCanvas(
            canvas = canvas,
            pageWidth = baseWidth.toFloat(),
            pageHeight = baseHeight.toFloat(),
            weekTitle = weekTitle,
            tasks = tasks,
            weekStart = weekStart,
            usePersianDigits = usePersianDigits,
            template = template,
            orientation = orientation,
            includeStats = includeStats,
            includeNotes = includeNotes
        )
        return bitmap
    }

    fun writeSchedulePdf(
        outputStream: OutputStream,
        weekTitle: String,
        tasks: List<TaskItem>,
        weekStart: WeekStartOption,
        usePersianDigits: Boolean,
        template: PdfTemplateType,
        orientation: PdfOrientation,
        includeStats: Boolean,
        includeNotes: Boolean
    ) {
        val pageWidth = if (orientation == PdfOrientation.LANDSCAPE) 1188 else 840
        val pageHeight = if (orientation == PdfOrientation.LANDSCAPE) 840 else 1188
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        drawScheduleOnCanvas(
            canvas = page.canvas,
            pageWidth = pageWidth.toFloat(),
            pageHeight = pageHeight.toFloat(),
            weekTitle = weekTitle,
            tasks = tasks,
            weekStart = weekStart,
            usePersianDigits = usePersianDigits,
            template = template,
            orientation = orientation,
            includeStats = includeStats,
            includeNotes = includeNotes
        )

        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }

    fun writeBitmapPng(bitmap: Bitmap, outputStream: OutputStream) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    }

    fun createShareableExportFile(fileName: String, writer: (OutputStream) -> Unit): Uri {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { writer(it) }
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    fun shareFileUri(uri: Uri, mimeType: String, chooserTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun drawScheduleOnCanvas(
        canvas: Canvas,
        pageWidth: Float,
        pageHeight: Float,
        weekTitle: String,
        tasks: List<TaskItem>,
        weekStart: WeekStartOption,
        usePersianDigits: Boolean,
        template: PdfTemplateType,
        orientation: PdfOrientation,
        includeStats: Boolean,
        includeNotes: Boolean
    ) {
        canvas.drawColor(Color.WHITE)

        val margin = 24f
        var cursorY = margin
        val contentWidth = pageWidth - (margin * 2)

        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 18f
            typeface = boldTypeface
            textAlign = Paint.Align.RIGHT
        }
        val subtitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 11f
            typeface = regularTypeface
            textAlign = Paint.Align.RIGHT
        }

        // Header banner
        val headerTitle = if (template == PdfTemplateType.MATRIX) {
            "برنامه هفتگی و تقویم تحصیلی ترم ۵ دندانپزشکی"
        } else {
            "چک‌لیست و برنامه روزانه ۷ روز هفته (ترم ۵ دندانپزشکی)"
        }
        canvas.drawText(headerTitle, pageWidth - margin, cursorY + 18f, titlePaint)
        canvas.drawText(
            "$weekTitle  •  ${PersianUtils.formatJalaliDate(usePersianDigits = usePersianDigits)}",
            pageWidth - margin,
            cursorY + 35f,
            subtitlePaint
        )

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F59E0B")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        cursorY += 44f
        canvas.drawLine(margin, cursorY, pageWidth - margin, cursorY, borderPaint)
        cursorY += 10f

        // Optional stats bar
        if (includeStats) {
            val total = tasks.size
            val done = tasks.count { it.completed }
            val pct = if (total == 0) 0 else ((done * 100f) / total).toInt()
            val statsBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFFBEB") }
            val statsBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#FDE68A")
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
            val statsRect = RectF(margin, cursorY, pageWidth - margin, cursorY + 28f)
            canvas.drawRoundRect(statsRect, 8f, 8f, statsBg)
            canvas.drawRoundRect(statsRect, 8f, 8f, statsBorder)

            val statsTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#78350F")
                textSize = 10.5f
                typeface = boldTypeface
                textAlign = Paint.Align.RIGHT
            }
            val statsLine = PersianUtils.formatNumber(
                "کل فعالیت‌ها: $total   |   تکمیل‌شده: $done ($pct%)   |   باقیمانده: ${total - done}   |   ناهار ثابت: ۱۲ تا ۱۳   |   ورزش: ج، ۱ش، ۳ش",
                usePersianDigits
            )
            canvas.drawText(statsLine, pageWidth - margin - 10f, cursorY + 18f, statsTextPaint)
            cursorY += 36f
        }

        val notesHeight = if (includeNotes) 34f else 0f
        val bodyHeight = (pageHeight - margin - notesHeight - cursorY - 8f).coerceAtLeast(200f)

        if (template == PdfTemplateType.MATRIX) {
            drawMatrixBody(
                canvas = canvas,
                left = margin,
                top = cursorY,
                width = contentWidth,
                height = bodyHeight,
                tasks = tasks,
                weekStart = weekStart,
                usePersianDigits = usePersianDigits
            )
        } else {
            drawChecklistBody(
                canvas = canvas,
                left = margin,
                top = cursorY,
                width = contentWidth,
                height = bodyHeight,
                tasks = tasks,
                weekStart = weekStart,
                usePersianDigits = usePersianDigits,
                orientation = orientation
            )
        }

        if (includeNotes) {
            val noteTop = pageHeight - margin - 26f
            val noteBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F8FAFC") }
            val noteBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E2E8F0")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val noteRect = RectF(margin, noteTop, pageWidth - margin, noteTop + 26f)
            canvas.drawRoundRect(noteRect, 6f, 6f, noteBg)
            canvas.drawRoundRect(noteRect, 6f, 6f, noteBorder)

            val notePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#334155")
                textSize = 9.5f
                typeface = regularTypeface
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText(
                PersianUtils.formatNumber(
                    "راهنمای برنامه: بیدارباش ۰۶:۰۰ (شنبه تا چهارشنبه) • تایم ثابت ناهار ۱۲ تا ۱۳ • ۳ جلسه ورزش (جمعه، یکشنبه، سه‌شنبه) • شام ۲۱:۳۰",
                    usePersianDigits
                ),
                pageWidth - margin - 10f,
                noteTop + 17f,
                notePaint
            )
        }
    }

    private fun drawMatrixBody(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        tasks: List<TaskItem>,
        weekStart: WeekStartOption,
        usePersianDigits: Boolean
    ) {
        val orderedDays = PersianUtils.getOrderedDays(weekStart)
        val slots = PersianUtils.TIME_SLOTS
        val slotColWidth = 96f
        val dayColWidth = (width - slotColWidth) / orderedDays.size
        val headerHeight = 28f
        val rowHeight = (height - headerHeight) / slots.size

        val gridStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }
        val headerBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFFBEB") }
        val slotBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFF7ED") }
        val lunchBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FEF3C7") }

        val headerTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#78350F")
            textSize = 10.5f
            typeface = boldTypeface
            textAlign = Paint.Align.CENTER
        }

        // RTL column order: rightmost column is Time Slot, then days right-to-left
        val slotColLeft = left + (dayColWidth * orderedDays.size)
        val slotHeaderRect = RectF(slotColLeft, top, left + width, top + headerHeight)
        canvas.drawRect(slotHeaderRect, headerBg)
        canvas.drawRect(slotHeaderRect, gridStroke)
        canvas.drawText("بازه زمانی", slotHeaderRect.centerX(), top + 18f, headerTextPaint)

        orderedDays.forEachIndexed { idx, dayKey ->
            val colRight = slotColLeft - (idx * dayColWidth)
            val colLeft = colRight - dayColWidth
            val rect = RectF(colLeft, top, colRight, top + headerHeight)
            canvas.drawRect(rect, headerBg)
            canvas.drawRect(rect, gridStroke)
            val dayName = PersianUtils.DAYS_MAP[dayKey]?.fa ?: dayKey
            canvas.drawText(dayName, rect.centerX(), top + 18f, headerTextPaint)
        }

        val slotTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 9f
            typeface = boldTypeface
            textAlign = Paint.Align.CENTER
        }
        val slotTimePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B45309")
            textSize = 8f
            typeface = regularTypeface
            textAlign = Paint.Align.CENTER
        }
        val cellTextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 8f
            typeface = regularTypeface
        }

        slots.forEachIndexed { rIdx, slot ->
            val rowTop = top + headerHeight + (rIdx * rowHeight)
            val rowBottom = rowTop + rowHeight
            val slotRect = RectF(slotColLeft, rowTop, left + width, rowBottom)
            canvas.drawRect(slotRect, if (slot.isLunch) lunchBg else slotBg)
            canvas.drawRect(slotRect, gridStroke)
            canvas.drawText(slot.title, slotRect.centerX(), rowTop + rowHeight * 0.44f, slotTitlePaint)
            canvas.drawText(
                PersianUtils.formatNumber(slot.timeRange, usePersianDigits),
                slotRect.centerX(),
                rowTop + rowHeight * 0.72f,
                slotTimePaint
            )

            orderedDays.forEachIndexed { dIdx, dayKey ->
                val colRight = slotColLeft - (dIdx * dayColWidth)
                val colLeft = colRight - dayColWidth
                val cellRect = RectF(colLeft, rowTop, colRight, rowBottom)
                if (slot.isLunch) {
                    canvas.drawRect(cellRect, lunchBg)
                }
                canvas.drawRect(cellRect, gridStroke)

                val cellTasks = tasks.filter { it.day == dayKey && PersianUtils.getTaskSlot(it) == slot.id }
                if (cellTasks.isNotEmpty()) {
                    val summary = cellTasks.joinToString("\n") { t ->
                        val mark = if (t.completed) "✓ " else "• "
                        val timePrefix = if (t.time.isNotBlank()) "${PersianUtils.formatNumber(t.time, usePersianDigits)} " else ""
                        "$mark$timePrefix${t.title}"
                    }
                    drawStaticWrappedText(
                        canvas = canvas,
                        text = summary,
                        paint = cellTextPaint,
                        x = colLeft + 4f,
                        y = rowTop + 3f,
                        maxWidth = (dayColWidth - 8f).toInt().coerceAtLeast(20),
                        maxHeight = (rowHeight - 6f).toInt().coerceAtLeast(16)
                    )
                }
            }
        }
    }

    private fun drawChecklistBody(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        tasks: List<TaskItem>,
        weekStart: WeekStartOption,
        usePersianDigits: Boolean,
        orientation: PdfOrientation
    ) {
        val orderedDays = PersianUtils.getOrderedDays(weekStart)
        val cols = if (orientation == PdfOrientation.LANDSCAPE) 4 else 2
        val rows = (orderedDays.size + cols - 1) / cols
        val gap = 10f
        val cardWidth = (width - gap * (cols - 1)) / cols
        val cardHeight = (height - gap * (rows - 1)) / rows

        val cardBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
        val cardBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
        }
        val dayHeaderPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#78350F")
            textSize = 12f
            typeface = boldTypeface
            textAlign = Paint.Align.RIGHT
        }
        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F59E0B")
            strokeWidth = 1.5f
        }
        val itemPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 8.5f
            typeface = regularTypeface
        }

        orderedDays.forEachIndexed { idx, dayKey ->
            val r = idx / cols
            val cFromRight = idx % cols
            val cardRight = left + width - (cFromRight * (cardWidth + gap))
            val cardLeft = cardRight - cardWidth
            val cardTop = top + (r * (cardHeight + gap))
            val rect = RectF(cardLeft, cardTop, cardRight, cardTop + cardHeight)
            canvas.drawRoundRect(rect, 10f, 10f, cardBg)
            canvas.drawRoundRect(rect, 10f, 10f, cardBorder)

            val dayInfo = PersianUtils.DAYS_MAP[dayKey]
            val dayTasks = tasks.filter { it.day == dayKey }
                .sortedBy { PersianUtils.parseTimeMinutes(it.time) }
            canvas.drawText(
                PersianUtils.formatNumber("${dayInfo?.fa ?: dayKey} (${dayTasks.size})", usePersianDigits),
                cardRight - 10f,
                cardTop + 18f,
                dayHeaderPaint
            )
            canvas.drawLine(cardLeft + 8f, cardTop + 24f, cardRight - 8f, cardTop + 24f, linePaint)

            val checklistText = if (dayTasks.isEmpty()) {
                "برنامه‌ای ثبت نشده است."
            } else {
                dayTasks.joinToString("\n") { t ->
                    val box = if (t.completed) "☑" else "☐"
                    val timePart = if (t.time.isNotBlank()) "[${PersianUtils.formatNumber(t.time, usePersianDigits)}] " else ""
                    "$box $timePart${t.title}"
                }
            }
            drawStaticWrappedText(
                canvas = canvas,
                text = checklistText,
                paint = itemPaint,
                x = cardLeft + 8f,
                y = cardTop + 28f,
                maxWidth = (cardWidth - 16f).toInt().coerceAtLeast(30),
                maxHeight = (cardHeight - 34f).toInt().coerceAtLeast(20)
            )
        }
    }

    private fun drawStaticWrappedText(
        canvas: Canvas,
        text: String,
        paint: TextPaint,
        x: Float,
        y: Float,
        maxWidth: Int,
        maxHeight: Int
    ) {
        val layout = StaticLayout.Builder
            .obtain(text, 0, text.length, paint, maxWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(2f, 1.05f)
            .setIncludePad(false)
            .build()
        canvas.save()
        canvas.translate(x, y)
        canvas.clipRect(0, 0, maxWidth, maxHeight)
        layout.draw(canvas)
        canvas.restore()
    }
}
