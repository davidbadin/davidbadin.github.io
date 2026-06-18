package sk.punkacidetom.pd2026.core.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val ASSET_NAME = "PD2026_program_test_data.csv"

/**
 * Reads the bundled offline CSV fallback from the assets folder.
 *
 * Returns the full CSV text, or null when the asset is absent (e.g. in release/production
 * builds where the file is intentionally stripped) or cannot be opened.
 */
@Singleton
class CsvAssetReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun readCsv(): String? = try {
        context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
    } catch (_: Exception) {
        null
    }
}
