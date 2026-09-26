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
    private const val BRAND = 0xFF6B4E8E.toInt() // plum accent for chrome with no single day's liturgical colour (cover, month headers)

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

            cursor.drawCoverBand(
                "KALENDA YA MASOMO $year",
                if (mode == PdfContentMode.FULL_TEXT)
                    "Dominika zote na Sikukuu Maalum \u2014 Masomo Kamili \u2014 LiturgX"
                else
                    "Dominika zote na Sikukuu Maalum \u2014 Marejeo \u2014 LiturgX"
            )
            cursor.advance(16)

            var lastMonth = -1
            entries.forEachIndexed { index, entry ->
                val isNewMonth = entry.date.monthValue != lastMonth
                // Every day starts on its own fresh page in full-text mode (see below). In the
                // compact references-only mode, days still flow several to a page, but a new
                // month now gets the same fresh-page treatment a wall calendar gives it -- only
                // a plain day-to-day transition keeps the old "just add spacing" behaviour.
                if (index > 0) {
                    if (mode == PdfContentMode.FULL_TEXT || isNewMonth) cursor.newPage() else cursor.advance(10)
                }
                if (isNewMonth) {
                    cursor.drawMonthHeader(monthNames[entry.date.monthValue - 1].uppercase())
                    lastMonth = entry.date.monthValue
                }
                val dateLabel = "${weekdayNames[entry.date.dayOfWeek.value].orEmpty()}, ${entry.date.dayOfMonth} ${monthNames[entry.date.monthValue - 1]}"
                if (mode == PdfContentMode.FULL_TEXT) {
                    cursor.drawDayBlockFullText(dateLabel, entry.title, entry.color, entry.citations)
                } else {
                    cursor.drawDayBlockReferencesOnly(dateLabel, entry.title, entry.color, entry.citations)
                }
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

        /** Cover-page hero band, same flat-print translation of [DailyLiturgicalCard]'s glowing
         *  header that [DailyReadingPdfGenerator.drawHeaderBand] uses -- but in [BRAND]'s fixed
         *  plum rather than a day's liturgical colour, since a year-long index has no single
         *  day's colour to reach for. */
        fun drawCoverBand(title: String, subtitle: String) {
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 18f
                color = Color.WHITE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.03f
            }
            val subPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 10.5f
                color = Color.argb(210, 255, 255, 255)
            }
            val padding = 18
            val gap = 6
            val titleH = kotlin.math.ceil(titlePaint.descent() - titlePaint.ascent()).toInt()
            val subH = kotlin.math.ceil(subPaint.descent() - subPaint.ascent()).toInt()
            val bandHeight = padding + titleH + gap + subH + padding
            ensureSpace(bandHeight)
            val c = canvas ?: return

            c.save()
            c.clipRect(0f, y.toFloat(), PAGE_WIDTH.toFloat(), (y + bandHeight).toFloat())
            c.drawRect(0f, y.toFloat(), PAGE_WIDTH.toFloat(), (y + bandHeight).toFloat(), Paint().apply { color = BRAND })
            c.drawCircle(PAGE_WIDTH - 60f, y + 6f, 70f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(30, 255, 255, 255) })
            c.restore()

            var textY = y + padding
            c.drawText(title, MARGIN.toFloat(), (textY - titlePaint.ascent()), titlePaint)
            textY += titleH + gap
            c.drawText(subtitle, MARGIN.toFloat(), (textY - subPaint.ascent()), subPaint)
            y += bandHeight
        }

        /** A month name with a short accent underline instead of plain bold text -- the same
         *  "small colored bar + label" language [MonthGrid]/[ReadingBlock] use on-screen. */
        fun drawMonthHeader(label: String) {
            val paint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 12.5f
                color = INK
                letterSpacing = 0.08f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val lineH = kotlin.math.ceil(paint.descent() - paint.ascent()).toInt()
            ensureSpace(lineH + 8)
            val c = canvas
            if (c != null) {
                c.drawText(label, MARGIN.toFloat(), (y - paint.ascent()), paint)
                c.drawRect(MARGIN.toFloat(), (y + lineH + 2).toFloat(), (MARGIN + 26).toFloat(), (y + lineH + 5).toFloat(), Paint().apply { color = BRAND })
            }
            y += lineH + 9
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

            val dotRadius = 3f
            canvas?.drawCircle(MARGIN + dotRadius, (y + dotRadius * 1.3f), dotRadius, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = color.hex.toInt() })
            drawLineIndented(dateLabel, dp, indent = 10)
            drawLine(title, tp)
            for (c in citations) {
                drawLine("\u00b7 ${c.label} \u2014 ${c.citation}", cp)
            }
            advance(4)
            drawDivider()
        }

        /** Full-text mode: date/title header (kept together), then each reading's label +
         *  citation heading followed by its full passage text, word-wrapped and paginated.
         *  Each reading is measured before drawing: if it doesn't fit in whatever space is left
         *  on the current page but WOULD fit entirely on a fresh page, it moves to one rather
         *  than leaving an orphaned line or two behind -- the exact scenario in the screenshot
         *  that prompted this (a reading starting three lines from the bottom of a page). A
         *  reading too long to fit on any single page (a full Old Testament narrative, say)
         *  still just flows across as many pages as it needs; there's no avoiding that split,
         *  and forcing it to a fresh page wouldn't change that, so it isn't given the same
         *  measure-first treatment. */
        fun drawDayBlockFullText(dateLabel: String, title: String, color: LiturgicalColor, citations: List<CitationEntry>) {
            drawDayHeaderBand(dateLabel, title, color)
            advance(14)

            val hp = readingHeadingPaint(color)
            val bp = bodyPaint()
            val fullPageCapacity = PAGE_HEIGHT - 2 * MARGIN
            for (c in citations) {
                val headingText = "${c.label} \u2014 ${c.citation}"
                val bodyText = c.passageText ?: c.citation
                val blockHeight = measureWrappedHeight(headingText, hp) + 3 + measureWrappedHeight(bodyText, bp)

                if (y + blockHeight + 16 > PAGE_HEIGHT - MARGIN && blockHeight + 16 <= fullPageCapacity) {
                    newPage()
                }

                advance(12)
                drawReadingCardBackground(color, blockHeight)
                drawWrapped(headingText, hp)
                advance(3)
                drawWrapped(bodyText, bp)
                advance(12)
            }
        }

        /** Compact colored header for one day's block in full-text mode -- one day per page in
         *  this mode, so it can afford the same "band" treatment [DailyReadingPdfGenerator] gives
         *  a whole page, just shorter. */
        private fun drawDayHeaderBand(dateLabel: String, title: String, color: LiturgicalColor) {
            val datePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 10f
                this.color = Color.argb(215, 255, 255, 255)
                letterSpacing = 0.04f
            }
            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 15f
                this.color = Color.WHITE
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val padding = 14
            val gap = 5
            val dateH = kotlin.math.ceil(datePaint.descent() - datePaint.ascent()).toInt()
            val titleH = kotlin.math.ceil(titlePaint.descent() - titlePaint.ascent()).toInt()
            val bandHeight = padding + dateH + gap + titleH + padding
            ensureSpace(bandHeight)
            val c = canvas ?: return

            val hex = color.hex.toInt()
            c.save()
            c.clipRect(0f, y.toFloat(), PAGE_WIDTH.toFloat(), (y + bandHeight).toFloat())
            c.drawRect(0f, y.toFloat(), PAGE_WIDTH.toFloat(), (y + bandHeight).toFloat(), Paint().apply { color = Color.rgb((hex shr 16) and 0xFF, (hex shr 8) and 0xFF, hex and 0xFF) })
            c.drawCircle(PAGE_WIDTH - 40f, y + 4f, 46f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(30, 255, 255, 255) })
            c.restore()

            var textY = y + padding
            c.drawText(dateLabel.uppercase(), MARGIN.toFloat(), (textY - datePaint.ascent()), datePaint)
            textY += dateH + gap
            c.drawText(title, MARGIN.toFloat(), (textY - titlePaint.ascent()), titlePaint)
            y += bandHeight
        }

        /** Light rounded panel behind one reading in full-text mode -- same recipe as
         *  [DailyReadingPdfGenerator.drawCardBackground], reused here so a full-text yearly
         *  export and a single-day export read as the same product. */
        private fun drawReadingCardBackground(color: LiturgicalColor, height: Int) {
            val c = canvas ?: return
            val hex = color.hex.toInt()
            val r = (hex shr 16) and 0xFF
            val g = (hex shr 8) and 0xFF
            val b = hex and 0xFF
            val pad = 9f
            val rect = android.graphics.RectF((MARGIN - pad), (y - pad), (PAGE_WIDTH - MARGIN + pad), (y + height + pad))
            c.drawRoundRect(rect, 9f, 9f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(15, r, g, b) })
            c.drawRoundRect(rect, 9f, 9f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(55, r, g, b); style = Paint.Style.STROKE; strokeWidth = 1f })
        }

        /** Sums the height [drawWrapped] would need for [text], without drawing anything --
         *  same paragraph-splitting logic, kept in step with it deliberately (a mismatch here
         *  would make the "does this fit on a fresh page" check wrong in exactly the cases it
         *  exists to get right). */
        private fun measureWrappedHeight(text: String, paint: TextPaint): Int {
            if (text.isEmpty()) return 0
            return text.split("\n").sumOf { paragraph ->
                if (paragraph.isEmpty()) {
                    (paint.textSize * 0.9f).toInt()
                } else {
                    StaticLayout.Builder
                        .obtain(paragraph, 0, paragraph.length, paint, CONTENT_WIDTH)
                        .setLineSpacing(1f, 1.12f)
                        .build()
                        .height
                }
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

        /** Same as [drawLine] but offset [indent] points to the right -- used for the date
         *  label so it clears the small colour dot drawn just before it. */
        private fun drawLineIndented(text: String, paint: TextPaint, indent: Int) {
            val layout = StaticLayout.Builder
                .obtain(text, 0, text.length, paint, CONTENT_WIDTH - indent)
                .setLineSpacing(1f, 1.05f)
                .build()
            val height = layout.height
            ensureSpace(height)
            val c = canvas
            if (c != null) {
                c.save()
                c.translate((MARGIN + indent).toFloat(), y.toFloat())
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
