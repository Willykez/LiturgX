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
import com.willykez.liturgx.core.ReadingPresenter
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.data.LectionaryRepository
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

/**
 * A handful of `period_key`s are fixed solemnities that don't fall on a Sunday and aren't
 * flagged by an `overridingSaint` (they're resolved directly by season logic, not the
 * santoral calendar) -- Christmas, Epiphany, the Triduum, Ascension/Corpus Christi when kept
 * on Thursday, etc. Included explicitly so the full-year export doesn't miss them just because
 * they landed on a weekday.
 */
private val FIXED_SOLEMNITY_PERIOD_KEYS = setOf(
    "mchana", "epifania", "maria_mama_wa_mungu", "jumatano_ya_majivu",
    "alhamisi_kuu", "ijumaa_kuu", "vigilia_ya_pasaka", "kupaa_kwa_bwana",
    "fungu_takatifu_la_mwili_na_damu_ya_kristo", "familia_takatifu",
    "ubatizo_wa_bwana", "moyo_mtakatifu_wa_yesu", "utatu_mtakatifu"
)

private val MAJOR_SAINT_RANKS = setOf("Sikukuu", "Sikukuu Kuu")

/**
 * Builds a compact, citation-only "ordo" PDF covering every Sunday and every special
 * holiday/solemnity/major feast across a full civil year -- unlike [DailyReadingPdfGenerator],
 * which lays out one day's full passage text, this is meant as a printable at-a-glance
 * reference (date, title, liturgical colour, reading citations), so a full year fits in a
 * reasonable page count instead of hundreds of pages of Scripture text.
 */
object YearlyLectionaryPdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 44
    private val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
    private const val INK = 0xFF231D2E.toInt()
    private const val INK_DIM = 0xFF5D5568.toInt()
    private const val PAPER = 0xFFFBF6EA.toInt()

    /** Runs the day-by-day resolution (365/366 lookups) -- call from a background dispatcher.
     *  A single day's resolution failing (a data edge case, a future calendar quirk) shouldn't
     *  take down the whole export -- logged and skipped so the rest of the year still comes
     *  through, rather than the coroutine throwing partway and the person getting nothing. */
    fun buildAndGenerate(context: Context, year: Int, region: RegionSettings): File {
        val repository = LectionaryRepository(context)
        val entries = mutableListOf<DayEntry>()

        var date = LocalDate.of(year, 1, 1)
        val end = LocalDate.of(year, 12, 31)
        while (!date.isAfter(end)) {
            try {
                val isSunday = date.dayOfWeek == java.time.DayOfWeek.SUNDAY
                val result = repository.getForDate(date, region)
                val resolved = result.resolved
                val saintRank = resolved.overridingSaint?.daraja
                val isSpecial = resolved.periodKey in FIXED_SOLEMNITY_PERIOD_KEYS ||
                    resolved.season.key == "sikukuu_maalum" ||
                    (saintRank != null && saintRank in MAJOR_SAINT_RANKS)

                if (isSunday || isSpecial) {
                    val title = resolved.overridingSaint?.jina ?: resolved.label
                    val citations = ReadingPresenter.present(result.readings)
                        .map { it.label to it.citation }
                    if (citations.isNotEmpty() || resolved.overridingSaint != null) {
                        entries += DayEntry(date, title, resolved.color, citations)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("YearlyPdfGenerator", "Skipping $date -- resolution failed", e)
            }
            date = date.plusDays(1)
        }

        check(entries.isNotEmpty()) { "No days resolved for $year -- nothing to export" }
        return generate(context, year, entries)
    }

    private data class DayEntry(
        val date: LocalDate,
        val title: String,
        val color: LiturgicalColor,
        val citations: List<Pair<String, String>>
    )

    private val monthNames = listOf(
        "Januari", "Februari", "Machi", "Aprili", "Mei", "Juni",
        "Julai", "Agosti", "Septemba", "Oktoba", "Novemba", "Desemba"
    )
    private val weekdayNames = mapOf(
        1 to "Jumatatu", 2 to "Jumanne", 3 to "Jumatano", 4 to "Alhamisi",
        5 to "Ijumaa", 6 to "Jumamosi", 7 to "Jumapili"
    )

    private fun generate(context: Context, year: Int, entries: List<DayEntry>): File {
        val document = PdfDocument()
        try {
            val cursor = PageCursor(document)
            cursor.newPage()

            cursor.drawText("KALENDA YA MASOMO $year", titlePaint())
            cursor.advance(4)
            cursor.drawText("Dominika zote na Sikukuu Maalum — LiturgX", smallPaint(INK_DIM))
            cursor.advance(10)
            cursor.drawDivider()
            cursor.advance(16)

            var lastMonth = -1
            for (entry in entries) {
                if (entry.date.monthValue != lastMonth) {
                    if (lastMonth != -1) cursor.advance(10)
                    cursor.drawText(monthNames[entry.date.monthValue - 1].uppercase(), monthHeaderPaint())
                    cursor.advance(8)
                    lastMonth = entry.date.monthValue
                }
                val dateLabel = "${weekdayNames[entry.date.dayOfWeek.value].orEmpty()}, ${entry.date.dayOfMonth} ${monthNames[entry.date.monthValue - 1]}"
                cursor.drawDayBlock(dateLabel, entry.title, entry.color, entry.citations)
                cursor.advance(10)
            }

            cursor.advance(18)
            cursor.drawText("Imetumwa kutoka LiturgX", italicPaint(INK_DIM), alignEnd = true)
            cursor.finishPage()

            val outFile = File(File(context.cacheDir, "pdfs").apply { mkdirs() }, "kalenda_ya_masomo_$year.pdf")
            FileOutputStream(outFile).use { document.writeTo(it) }
            return outFile
        } finally {
            document.close()
        }
    }

    private fun titlePaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 17f
        color = INK
        letterSpacing = 0.04f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun monthHeaderPaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 12.5f
        color = INK
        letterSpacing = 0.08f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun dateLabelPaint(color: LiturgicalColor) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10.5f
        this.color = color.hex.toInt()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun titleRowPaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 11.5f
        color = INK
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun citationPaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10f
        color = INK_DIM
        typeface = Typeface.DEFAULT
    }

    private fun smallPaint(textColor: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 9.5f
        color = textColor
    }

    private fun italicPaint(textColor: Int) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 9.5f
        color = textColor
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
    }

    /** Same paginated word-wrap cursor approach as [DailyReadingPdfGenerator], scaled down for
     *  compact multi-entry-per-page layout instead of one day's full Scripture text. */
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

        fun drawDivider() {
            ensureSpace(1)
            val c = canvas ?: return
            val paint = Paint().apply { color = Color.argb(60, 0x5D, 0x55, 0x68) }
            c.drawLine(MARGIN.toFloat(), y.toFloat(), (PAGE_WIDTH - MARGIN).toFloat(), y.toFloat(), paint)
        }

        /** One day's worth of compact info: coloured date line, title, then each citation on
         *  its own line -- kept together as a unit, moved to a fresh page if it wouldn't fit
         *  rather than splitting a single day's block across a page boundary. */
        fun drawDayBlock(dateLabel: String, title: String, color: LiturgicalColor, citations: List<Pair<String, String>>) {
            val dp = dateLabelPaint(color)
            val tp = titleRowPaint()
            val cp = citationPaint()
            val lineH = kotlin.math.ceil(tp.descent() - tp.ascent()).toInt() + 2
            val citationLineH = kotlin.math.ceil(cp.descent() - cp.ascent()).toInt() + 1
            val blockHeight = lineH * 2 + citationLineH * citations.size + 6

            if (y + blockHeight > PAGE_HEIGHT - MARGIN && blockHeight <= PAGE_HEIGHT - 2 * MARGIN) {
                newPage()
            }

            drawLine(dateLabel, dp)
            drawLine(title, tp)
            for ((label, citation) in citations) {
                drawLine("· $label — $citation", cp)
            }
        }

        private fun drawLine(text: String, paint: TextPaint) {
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, CONTENT_WIDTH)
                .setLineSpacing(1f, 1.05f)
                .build()
            val height = layout.height
            ensureSpace(height)
            val c = canvas
            if (c != null) {
                c.save()
                c.translate(MARGIN.toFloat(), y.toFloat())
                layout.draw(c)
                c.restore()
            }
            y += height
        }

        fun drawText(text: String, paint: TextPaint, alignEnd: Boolean = false) {
            val align = if (alignEnd) android.text.Layout.Alignment.ALIGN_OPPOSITE else android.text.Layout.Alignment.ALIGN_NORMAL
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, CONTENT_WIDTH)
                .setAlignment(align)
                .build()
            val height = layout.height
            ensureSpace(height)
            val c = canvas
            if (c != null) {
                c.save()
                c.translate(MARGIN.toFloat(), y.toFloat())
                layout.draw(c)
                c.restore()
            }
            y += height
        }
    }
}
