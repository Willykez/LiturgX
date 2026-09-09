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
import com.willykez.liturgx.data.bible.BibleRepository
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

/** One reading entry within a day: always has a label + citation; [passageText] is only
 *  populated in [YearlyLectionaryPdfGenerator.PdfContentMode.FULL_TEXT] mode. */
private data class CitationEntry(val label: String, val citation: String, val passageText: String?)

/**
 * Builds an "ordo" PDF covering every Sunday and every special holiday/solemnity/major feast
 * across a full civil year, in either of two modes (see [PdfContentMode]):
 *  - [PdfContentMode.REFERENCES_ONLY]: a compact, citation-only at-a-glance reference (date,
 *    title, liturgical colour, reading citations) -- a full year fits in a modest page count.
 *  - [PdfContentMode.FULL_TEXT]: the same structure, but with each citation's actual Scripture
 *    text underneath it (resolved via [BibleRepository], the same engine
 *    [DailyReadingPdfGenerator] uses for a single day) -- necessarily a much longer document,
 *    since it's the whole year's worth of full passages rather than references to look up.
 */
object YearlyLectionaryPdfGenerator {

    enum class PdfContentMode { REFERENCES_ONLY, FULL_TEXT }

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 44
    private val CONTENT_WIDTH = PAGE_WIDTH - MARGIN * 2
    private const val INK = 0xFF231D2E.toInt()
    private const val INK_DIM = 0xFF5D5568.toInt()
    private const val PAPER = 0xFFFBF6EA.toInt()

    /** Runs the day-by-day resolution (365/366 lookups, plus one Bible lookup per reading in
     *  [PdfContentMode.FULL_TEXT] mode) -- call from a background dispatcher. A single day's
     *  resolution failing (a data edge case, a future calendar quirk) shouldn't take down the
     *  whole export -- logged and skipped so the rest of the year still comes through, rather
     *  than the coroutine throwing partway and the person getting nothing. */
    fun buildAndGenerate(context: Context, year: Int, region: RegionSettings, mode: PdfContentMode): File {
        val repository = LectionaryRepository(context)
        val bibleRepository = if (mode == PdfContentMode.FULL_TEXT) BibleRepository(context) else null
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
                    val citations = ReadingPresenter.present(result.readings).map { item ->
                        val passage = bibleRepository?.getPassage(item.citation)?.renderedText()
                        CitationEntry(item.label, item.citation, passage)
                    }
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
        return generate(context, year, entries, mode)
    }

    private data class DayEntry(
        val date: LocalDate,
        val title: String,
        val color: LiturgicalColor,
        val citations: List<CitationEntry>
    )

    private val monthNames = listOf(
        "Januari", "Februari", "Machi", "Aprili", "Mei", "Juni",
        "Julai", "Agosti", "Septemba", "Oktoba", "Novemba", "Desemba"
    )
    private val weekdayNames = mapOf(
        1 to "Jumatatu", 2 to "Jumanne", 3 to "Jumatano", 4 to "Alhamisi",
        5 to "Ijumaa", 6 to "Jumamosi", 7 to "Jumapili"
    )

    private fun generate(context: Context, year: Int, entries: List<DayEntry>, mode: PdfContentMode): File {
        val document = PdfDocument()
        try {
            val cursor = PageCursor(document)
            cursor.newPage()

            cursor.drawText("KALENDA YA MASOMO $year", titlePaint())
            cursor.advance(4)
            val subtitle = if (mode == PdfContentMode.FULL_TEXT)
                "Dominika zote na Sikukuu Maalum — Masomo Kamili — LiturgX"
            else
                "Dominika zote na Sikukuu Maalum — Marejeo — LiturgX"
            cursor.drawText(subtitle, smallPaint(INK_DIM))
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
                if (mode == PdfContentMode.FULL_TEXT) {
                    cursor.drawDayBlockFullText(dateLabel, entry.title, entry.color, entry.citations)
                } else {
                    cursor.drawDayBlockReferencesOnly(dateLabel, entry.title, entry.color, entry.citations)
                }
                cursor.advance(10)
            }

            cursor.advance(18)
            cursor.drawText("Imetumwa kutoka LiturgX", italicPaint(INK_DIM), alignEnd = true)
            cursor.finishPage()

            val suffix = if (mode == PdfContentMode.FULL_TEXT) "kamili" else "marejeo"
            val outFile = File(File(context.cacheDir, "pdfs").apply { mkdirs() }, "kalenda_ya_masomo_${year}_$suffix.pdf")
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

    private fun readingHeadingPaint(color: LiturgicalColor) = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10.5f
        this.color = color.hex.toInt()
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private fun bodyPaint() = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 10.5f
        color = INK
        typeface = Typeface.SERIF
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

    /** Same paginated word-wrap cursor approach as [DailyReadingPdfGenerator]. */
    private class PageCursor(private val document: PdfDocument) {
        private var page: PdfDocument.Page? = null
        private var canvas: Canvas? = null
        private var y = MARGIN
        private var pageNumber = 0

        fun newPage() {
            page?.let {
                drawPageNumber()
                document.finishPage(it)
            }
            pageNumber++
            val info = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
            val newPage = document.startPage(info)
            newPage.canvas.drawColor(PAPER)
            page = newPage
            canvas = newPage.canvas
            y = MARGIN
        }

        fun finishPage() {
            page?.let {
                drawPageNumber()
                document.finishPage(it)
            }
            page = null
        }

        /** "Ukurasa N" centered in the bottom margin -- see the same helper in
         *  [com.willykez.liturgx.data.sharing.DailyReadingPdfGenerator] for why this matters
         *  more here than it might seem: the yearly export can easily run to 40+ pages. */
        private fun drawPageNumber() {
            val c = canvas ?: return
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 8.5f
                color = INK_DIM
                textAlign = Paint.Align.CENTER
            }
            c.drawText("Ukurasa $pageNumber", PAGE_WIDTH / 2f, (PAGE_HEIGHT - MARGIN / 2f), paint)
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

        /** Compact mode: coloured date line, title, then each citation on its own line -- kept
         *  together as a unit, moved to a fresh page if it wouldn't fit rather than splitting a
         *  single day's block across a page boundary. */
        fun drawDayBlockReferencesOnly(dateLabel: String, title: String, color: LiturgicalColor, citations: List<CitationEntry>) {
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
            for (c in citations) {
                drawLine("· ${c.label} — ${c.citation}", cp)
            }
        }

        /** Full-text mode: date/title header (kept together), then each reading's label +
         *  citation heading followed by its full passage text, word-wrapped and paginated --
         *  a passage can easily run longer than one page, so this can't use the "keep as one
         *  unit" trick [drawDayBlockReferencesOnly] uses; only the short header is protected
         *  from an awkward page-top split. */
        fun drawDayBlockFullText(dateLabel: String, title: String, color: LiturgicalColor, citations: List<CitationEntry>) {
            val dp = dateLabelPaint(color)
            val tp = titleRowPaint()
            val headerLineH = kotlin.math.ceil(tp.descent() - tp.ascent()).toInt() + 2
            if (y + headerLineH * 2 > PAGE_HEIGHT - MARGIN) newPage()

            drawLine(dateLabel, dp)
            drawLine(title, tp)
            advance(6)

            val hp = readingHeadingPaint(color)
            val bp = bodyPaint()
            for (c in citations) {
                drawWrapped("${c.label} — ${c.citation}", hp)
                advance(3)
                drawWrapped(c.passageText ?: c.citation, bp)
                advance(10)
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

        /** Word-wraps [text] to the content width and draws it, splitting across as many pages
         *  as needed at exact line boundaries -- same technique [DailyReadingPdfGenerator] uses,
         *  needed here because a full Scripture passage can run well past one page. */
        private fun drawWrapped(text: String, paint: TextPaint) {
            if (text.isEmpty()) return
            text.split("\n").forEach { paragraph ->
                if (paragraph.isEmpty()) {
                    advance((paint.textSize * 0.9f).toInt())
                    return@forEach
                }
                val layout = StaticLayout.Builder
                    .obtain(paragraph, 0, paragraph.length, paint, CONTENT_WIDTH)
                    .setLineSpacing(1f, 1.12f)
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
