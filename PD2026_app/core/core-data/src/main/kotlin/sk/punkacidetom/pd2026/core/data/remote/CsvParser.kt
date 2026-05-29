package sk.punkacidetom.pd2026.core.data.remote

/**
 * Minimal RFC-4180-compatible CSV parser (pure Kotlin, no dependencies).
 * Handles quoted fields and doubled-quote escapes.
 */
object CsvParser {

    /** Parse a full CSV string into rows of fields. */
    fun parse(csv: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val chars = csv.toCharArray()
        var i = 0

        while (i <= chars.lastIndex) {
            val row = mutableListOf<String>()
            // Skip blank lines
            while (i <= chars.lastIndex && (chars[i] == '\r' || chars[i] == '\n')) i++
            if (i > chars.lastIndex) break

            while (i <= chars.lastIndex && chars[i] != '\n' && chars[i] != '\r') {
                parseField(chars, i).let { (field, end) -> row.add(field); i = end }
                if (i <= chars.lastIndex && chars[i] == ',') i++ // consume comma
            }
            if (row.isNotEmpty()) rows.add(row)
        }
        return rows
    }

    private fun parseField(chars: CharArray, start: Int): Pair<String, Int> {
        var i = start
        return if (i <= chars.lastIndex && chars[i] == '"') {
            // Quoted field
            i++ // skip opening quote
            val sb = StringBuilder()
            while (i <= chars.lastIndex) {
                val c = chars[i]
                if (c == '"') {
                    if (i + 1 <= chars.lastIndex && chars[i + 1] == '"') {
                        sb.append('"')
                        i += 2
                    } else {
                        i++ // skip closing quote
                        break
                    }
                } else {
                    sb.append(c)
                    i++
                }
            }
            Pair(sb.toString(), i)
        } else {
            // Unquoted field — ends at comma, newline, or EOF
            val sb = StringBuilder()
            while (i <= chars.lastIndex && chars[i] != ',' && chars[i] != '\n' && chars[i] != '\r') {
                sb.append(chars[i])
                i++
            }
            Pair(sb.toString().trim(), i)
        }
    }
}
