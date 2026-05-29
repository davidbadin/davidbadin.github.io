package sk.punkacidetom.pd2026.core.i18n

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wraps AppCompatDelegate.setApplicationLocales for immediate locale switching (no restart).
 * The chosen locale is persisted by the AndroidX AppCompat library automatically.
 */
@Singleton
class LocaleHelper @Inject constructor() {

    /** Apply [languageTag] ("sk" or "en") immediately to the whole app. */
    fun applyLocale(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(languageTag)
        )
    }

    /** The currently active locale tag (e.g. "sk", "en"). */
    val currentLocaleTag: String
        get() {
            val locales = AppCompatDelegate.getApplicationLocales()
            return if (locales.isEmpty) "sk" else locales[0]?.language ?: "sk"
        }
}
