package sk.punkacidetom.pd2026.core.data.remote

import android.content.Context
import android.util.Xml
import dagger.hilt.android.qualifiers.ApplicationContext
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.LocalDate
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

private const val ASSET_NAME = "PD2026_program_test_data.xlsx"

/**
 * Reads the bundled XLSX fallback from assets with no external library.
 *
 * XLSX is a ZIP archive of XML files. We open the stream as a [ZipInputStream]
 * and parse the three relevant entries with Android's built-in [XmlPullParser]:
 *   - xl/sharedStrings.xml  — string table referenced by cells
 *   - xl/styles.xml         — cell style → format ID mapping (for date detection)
 *   - xl/worksheets/sheet1.xml — actual cell data
 *
 * Returns rows as [List]<[List]<[String]>> compatible with [BandMapper.mapRows],
 * or null when the asset is absent or cannot be parsed.
 */
@Singleton
class XlsxAssetReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun readRows(): List<List<String>>? = try {
        context.assets.open(ASSET_NAME).use { parseXlsx(it) }
    } catch (_: Exception) { null }

    // -----------------------------------------------------------------------
    // Top-level: unzip and dispatch to per-entry parsers
    // -----------------------------------------------------------------------

    private fun parseXlsx(input: InputStream): List<List<String>> {
        val entries = mutableMapOf<String, ByteArray>()
        ZipInputStream(input.buffered()).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name in NEEDED_ENTRIES) entries[entry.name] = zip.readBytes()
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        val sharedStrings = entries[SHARED_STRINGS]?.let { parseSharedStrings(it) } ?: emptyList()
        val (dateStyleIds, timeStyleIds) = entries[STYLES]?.let { parseStyleIds(it) }
            ?: (emptySet<Int>() to emptySet())
        return entries[SHEET1]?.let { parseSheet(it, sharedStrings, dateStyleIds, timeStyleIds) }
            ?: emptyList()
    }

    // -----------------------------------------------------------------------
    // xl/sharedStrings.xml  →  ordered list of strings
    // -----------------------------------------------------------------------

    private fun parseSharedStrings(bytes: ByteArray): List<String> {
        val result = mutableListOf<String>()
        val parser = pullParser(bytes)
        val buf = StringBuilder()
        var inSi = false

        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "si" -> { inSi = true; buf.clear() }
                    // <t> may appear multiple times inside one <si> (rich text runs)
                    "t" -> if (inSi) buf.append(parser.nextText())
                }
                XmlPullParser.END_TAG -> if (parser.name == "si" && inSi) {
                    result += buf.toString()
                    inSi = false
                }
            }
            ev = parser.next()
        }
        return result
    }

    // -----------------------------------------------------------------------
    // xl/styles.xml  →  (dateStyleIndices, timeStyleIndices)
    // -----------------------------------------------------------------------

    private fun parseStyleIds(bytes: ByteArray): Pair<Set<Int>, Set<Int>> {
        val numFmts = mutableMapOf<Int, String>()  // formatId → formatCode
        val xfFmtIds = mutableListOf<Int>()         // cellXf index → formatId
        val parser = pullParser(bytes)
        var inCellXfs = false

        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "numFmt" -> {
                        val id = parser.getAttributeValue(null, "numFmtId")?.toIntOrNull()
                        val code = parser.getAttributeValue(null, "formatCode") ?: ""
                        if (id != null) numFmts[id] = code
                    }
                    "cellXfs" -> inCellXfs = true
                    "xf" -> if (inCellXfs)
                        xfFmtIds += parser.getAttributeValue(null, "numFmtId")?.toIntOrNull() ?: 0
                }
                XmlPullParser.END_TAG -> if (parser.name == "cellXfs") inCellXfs = false
            }
            ev = parser.next()
        }

        val dateSI = mutableSetOf<Int>()
        val timeSI = mutableSetOf<Int>()
        xfFmtIds.forEachIndexed { idx, fmtId ->
            when {
                fmtId in BUILTIN_DATE_FMT_IDS -> dateSI += idx
                fmtId in BUILTIN_TIME_FMT_IDS -> timeSI += idx
                else -> {
                    val code = numFmts[fmtId]?.lowercase() ?: return@forEachIndexed
                    when {
                        "y" in code || ("d" in code && "m" in code) -> dateSI += idx
                        "h" in code -> timeSI += idx
                    }
                }
            }
        }
        return dateSI to timeSI
    }

    // -----------------------------------------------------------------------
    // xl/worksheets/sheet1.xml  →  list of rows (each row = list of cell strings)
    // -----------------------------------------------------------------------

    private fun parseSheet(
        bytes: ByteArray,
        sharedStrings: List<String>,
        dateStyleIds: Set<Int>,
        timeStyleIds: Set<Int>,
    ): List<List<String>> {
        val rows = mutableListOf<MutableList<String>>()
        val parser = pullParser(bytes)

        var currentRow: MutableList<String>? = null
        var cellRef = ""
        var cellType = ""
        var styleIdx = 0
        var cellVal = ""
        var inInlineStr = false
        val inlineBuf = StringBuilder()

        var ev = parser.eventType
        while (ev != XmlPullParser.END_DOCUMENT) {
            when (ev) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "row" -> { currentRow = mutableListOf(); rows += currentRow }
                    "c" -> {
                        cellRef = parser.getAttributeValue(null, "r") ?: ""
                        cellType = parser.getAttributeValue(null, "t") ?: ""
                        styleIdx = parser.getAttributeValue(null, "s")?.toIntOrNull() ?: 0
                        cellVal = ""
                        inInlineStr = false
                        inlineBuf.clear()
                    }
                    // <v> holds numeric value or shared-string index
                    "v" -> cellVal = parser.nextText()
                    "is" -> inInlineStr = true
                    // <t> inside <is> holds inline string text (may be split across runs)
                    "t" -> if (inInlineStr) inlineBuf.append(parser.nextText())
                }
                XmlPullParser.END_TAG -> when (parser.name) {
                    "is" -> inInlineStr = false
                    "c" -> {
                        val col = colIndex(cellRef)
                        val row = currentRow
                        if (row != null && col >= 0) {
                            // Sparse columns: pad with empty strings up to this column
                            while (row.size < col) row += ""
                            val value = cellToString(
                                cellType, cellVal, styleIdx,
                                sharedStrings, dateStyleIds, timeStyleIds,
                                if (inInlineStr || cellType == "inlineStr") inlineBuf.toString() else null,
                            )
                            if (col < row.size) row[col] = value else row += value
                        }
                    }
                }
            }
            ev = parser.next()
        }
        return rows
    }

    // -----------------------------------------------------------------------
    // Cell value resolution
    // -----------------------------------------------------------------------

    private fun cellToString(
        type: String, rawVal: String, styleIdx: Int,
        sharedStrings: List<String>,
        dateStyleIds: Set<Int>, timeStyleIds: Set<Int>,
        inlineText: String?,
    ): String = when {
        inlineText != null -> inlineText
        type == "s" -> sharedStrings.getOrElse(rawVal.toIntOrNull() ?: 0) { "" }
        type == "str" || type == "e" || type == "b" -> rawVal
        else -> {
            // Numeric cell (no type attribute, or type="n")
            val num = rawVal.toDoubleOrNull() ?: return rawVal
            when {
                styleIdx in dateStyleIds -> serialToDate(num)
                styleIdx in timeStyleIds -> serialToTime(num)
                // Heuristic for unstyled cells: pure fractions (0 < x < 1) are times
                num > 0.0 && num < 1.0 -> serialToTime(num)
                else -> rawVal
            }
        }
    }

    // -----------------------------------------------------------------------
    // Column reference  "A1" → 0,  "B3" → 1,  "AA7" → 26 …
    // -----------------------------------------------------------------------

    private fun colIndex(ref: String): Int {
        var col = 0
        for (ch in ref) {
            if (!ch.isLetter()) break
            col = col * 26 + (ch.uppercaseChar() - 'A' + 1)
        }
        return col - 1   // 0-based
    }

    // -----------------------------------------------------------------------
    // Excel serial → date / time string
    // -----------------------------------------------------------------------

    /**
     * Converts an Excel date serial to "d.M.yyyy" as expected by [BandMapper].
     * Excel epoch = Jan 1, 1900 = serial 1; Unix epoch = serial 25569.
     * The 1900 leap-year bug in Excel only affects serials < 60 (pre-1900-03-01),
     * so for any realistic festival date in 2026 the offset of 25569 is exact.
     */
    private fun serialToDate(serial: Double): String {
        val date = LocalDate.ofEpochDay(serial.toLong() - 25569L)
        return "${date.dayOfMonth}.${date.monthValue}.${date.year}"
    }

    /** Converts the fractional part of an Excel serial to "H:mm:ss". */
    private fun serialToTime(serial: Double): String {
        val secs = ((serial % 1.0) * 86400.0).roundToInt()
        val h = secs / 3600
        val m = (secs % 3600) / 60
        val s = secs % 60
        return "$h:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}"
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private fun pullParser(bytes: ByteArray): XmlPullParser =
        Xml.newPullParser().apply {
            setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            setInput(ByteArrayInputStream(bytes), "UTF-8")
        }

    companion object {
        private const val SHARED_STRINGS = "xl/sharedStrings.xml"
        private const val STYLES = "xl/styles.xml"
        private const val SHEET1 = "xl/worksheets/sheet1.xml"
        private val NEEDED_ENTRIES = setOf(SHARED_STRINGS, STYLES, SHEET1)

        // Built-in Excel format IDs: 14–17 and 22 are date, 18–21 are time
        private val BUILTIN_DATE_FMT_IDS = setOf(14, 15, 16, 17, 22)
        private val BUILTIN_TIME_FMT_IDS = setOf(18, 19, 20, 21)
    }
}
