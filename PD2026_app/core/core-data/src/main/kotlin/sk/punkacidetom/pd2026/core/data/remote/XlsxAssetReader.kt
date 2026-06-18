package sk.punkacidetom.pd2026.core.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import javax.inject.Inject
import javax.inject.Singleton

private const val ASSET_NAME = "PD2026_program_test_data.xlsx"
private const val SHEET_NAME = "PD2026"

@Singleton
class XlsxAssetReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    /**
     * Reads the bundled test XLSX from assets and returns rows as a list of string lists,
     * including the header row (row index 0), matching the shape expected by [CsvParser].
     * Returns null if the asset file does not exist (production build).
     */
    fun readRows(): List<List<String>>? {
        val stream = try {
            context.assets.open(ASSET_NAME)
        } catch (e: Exception) {
            return null   // file not present — production build
        }
        return stream.use { inp ->
            val wb = XSSFWorkbook(inp)
            val sheet = wb.getSheet(SHEET_NAME) ?: return null
            sheet.map { row ->
                (0 until row.lastCellNum).map { col ->
                    val cell = row.getCell(col)
                    when (cell?.cellType) {
                        CellType.NUMERIC -> {
                            val raw = cell.numericCellValue
                            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                val ldt = cell.localDateTimeCellValue
                                // Return date as d.M.yyyy or time as H:mm:ss depending on value
                                if (ldt.toLocalDate().year > 1899)
                                    "${ldt.dayOfMonth}.${ldt.monthValue}.${ldt.year}"
                                else
                                    "${ldt.hour}:${ldt.minute}:${ldt.second}"
                            } else raw.toBigDecimal().stripTrailingZeros().toPlainString()
                        }
                        CellType.STRING -> cell.stringCellValue ?: ""
                        CellType.BOOLEAN -> cell.booleanCellValue.toString()
                        else -> ""
                    }
                }
            }
        }
    }
}
