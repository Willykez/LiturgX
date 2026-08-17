package com.willykez.liturgx.data.sharing

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.StaticLayout
import android.text.TextPaint
import com.willykez.liturgx.core.LiturgicalColor
import java.io.File
import java.io.FileOutputStream

/** One reading's worth of content, already resolved to plain text -- shared shape between the
 *  PDF export and [DailyLiturgicalCard], since both lay out "the whole day's readings". */
data class DayCardReading(
    val kindLabel: String,
    val citation: String,
    val passageText: String,
    val responseText: String?
)

/**
 * Lays out the full day's readings as a paginated A4 PDF using [android.graphics.pdf.PdfDocument]
 * and [StaticLayout] for proper word-wrapped, measured text -- not a fixed-lines-per-page guess.
 * A reading's body text can easily run longer than one page (a full Old Testament narrative
 * reading, for instance), so the core of this is [PageCursor.drawWrapped], which can split a
 * single paragraph mid-flow across a page boundary at an exact line boundary, the way a real
 * word processor would, rather than either overflowing the page or wasting the remaining space.
 */
object DailyReadingPdfGenerator {

    private const val PAGE_WIDTH = 595   // A4 at 72dpi
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 48
    private val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
    private const val INK = 0xFF231D2E.toInt()
    private const val INK_DIM = 0xFF5D5568.toInt()
    private const val PAPER = 0xFFFBF6EA.toInt()

    fun generate(
        context: Context,
        dateText: String,
        seasonText: String,
        color: LiturgicalColor,
        readings: List<DayCardReading>
    ): File {
        val document = PdfDocument()
        val cursor = PageCursor(document)
        cursor.newPage()

        cursor.drawAccentBar(color)
        cursor.advance(14)
        cursor.drawText(seasonText.uppercase(), titlePaint(color))
        cursor.advance(4)
        cursor.drawText(dateText, smallPaint(INK_DIM))
        cursor.advance(10)
        cursor.drawDivider()
        cursor.advance(18)

        readings.forEachIndexed { index, reading ->
            cursor.drawText("${reading.kindLabel.uppercase()}: ${reading.citation}", headingPaint(color))
            cursor.advance(10)
            cursor.drawWrapped(reading.passageText, bodyPaint())
            reading.responseText?.let { response ->
                cursor.advance(8)
                response.split("\n").forEach { line ->
                    cursor.drawText(line, italicPaint(INK_DIM))
                }
            }
            if (index != readings.lastIndex) {
                cursor.advance(18)
                cursor.drawDivider()
                cursor.advance(18)
            }
        }

        cursor.advance(24)
        cursor.drawText("Imetumwa kutoka LiturgX", italicPaint(INK_DIM), alignEnd = true)
        cursor.finishPage()

        val outFile = File(File(context.cacheDir, "pdfs").apply { mkdirs() }, "masomo_ya_leo.pdf")
        FileOutputStream(outFile).use { document.writeTo(it) }
        document.close()
        return outFile
    }

    private fun titlePaint(color: LiturgicalColor) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 15f
        this.color = color.hex.toInt()
        letterSpacing = 0.05f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun headingPaint(color: LiturgicalColor) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 13f
        this.color = color.hex.toInt()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun bodyPaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11.5f
        color = INK
        typeface = Typeface.SERIF
    }

    private fun smallPaint(textColor: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 9.5f
        color = textColor
    }

    private fun italicPaint(textColor: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10.5f
        color = textColor
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
    }

    /** Tracks the current page/canvas and vertical write position, creating new pages on demand. */
    private class PageCursor(private val document: PdfDocument) {
        private var page: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var y = MARGIN
        private var pageNumber = 0

        fun newPage() {
            page?.let { document.finishPage(it) }
            pageNumber++
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val newPage = document.startPage(info)
            newPage.canvas.drawColor(PAPER)
            page = newPage
            canvas = newPage.canvas
            y = MARGIN
        }

        fun finishPage() {
            page?.let { document.finishPage(it) }
            page = null
        }

        fun advance(dp: Int) {
            ensureSpace(dp)
            y += dp
        }

        private fun ensureSpace(needed: Int) {
            if (y + needed > PAGE_HEIGHT - MARGIN) newPage()
        }

        fun drawAccentBar(color: LiturgicalColor) {
            ensureSpace(6)
            val c = canvas ?: return
            val paint = Paint().apply { this.color = color.hex.toInt() }
            c.drawRect(MARGIN.toFloat(), y.toFloat(), (PAGE_WIDTH - MARGIN).toFloat(), (y + 5).toFloat(), paint)
            y += 5
        }

        fun drawDivider() {
            ensureSpace(1)
            val c = canvas ?: return
            val paint = Paint().apply { color = Color.argb(60, 0x5D, 0x55, 0x68) }
            c.drawLine(MARGIN.toFloat(), y.toFloat(), (PAGE_WIDTH - MARGIN).toFloat(), y.toFloat(), paint)
        }

        fun drawText(text: String, paint: TextPaint, alignEnd: Boolean = false) {
            drawWrapped(text, paint, alignEnd)
        }

        /**
         * Word-wraps [text] to the content width and draws it, splitting across as many pages
         * as needed at exact line boundaries -- never overflowing a page, never leaving a
         * paragraph's remainder stranded on a page with no room to start it.
         */
        fun drawWrapped(text: String, paint: TextPaint, alignEnd: Boolean = false) {
            if (text.isEmpty()) return
            text.split("\n").forEach { paragraph ->
                if (paragraph.isEmpty()) {
                    advance((paint.textSize * 0.9f).toInt())
                    return@forEach
                }
                val align = if (alignEnd) android.text.Layout.Alignment.ALIGN_OPPOSITE else android.text.Layout.Alignment.ALIGN_NORMAL
                val layout = StaticLayout.Builder
                    .obtain(paragraph, 0, paragraph.length, paint, CONTENT_WIDTH)
                    .setAlignment(align)
                    .setLineSpacing(2f, 1.12f)
                    .build()

                var lineIndex = 0
                val lineCount = layout.lineCount
                while (lineIndex < lineCount) {
                    if (y + (layout.getLineBottom(lineIndex) - layout.getLineTop(lineIndex)) > PAGE_HEIGHT - MARGIN) {
                        newPage()
                        continue
                    }
                    val remaining = (PAGE_HEIGHT - MARGIN) - y
                    var endLine = lineIndex
                    val top = layout.getLineTop(lineIndex)
                    while (endLine < lineCount && layout.getLineBottom(endLine) - top <= remaining) {
                        endLine++
                    }
                    if (endLine == lineIndex) {
                        newPage()
                        continue
                    }

                    val c = canvas
                    if (c != null) {
                        c.save()
                        c.clipRect(MARGIN, y, MARGIN + CONTENT_WIDTH, PAGE_HEIGHT - MARGIN)
                        c.translate(MARGIN.toFloat(), (y - top).toFloat())
                        layout.draw(c)
                        c.restore()
                    }
                    y += layout.getLineBottom(endLine - 1) - top
                    lineIndex = endLine
                }
            }
        }
    }
}
