package sk.punkacidetom.pd2026.core.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import sk.punkacidetom.pd2026.core.ui.R

// Three text tiers + two FA icon families — spec §9.2
val ThirdManFontFamily = FontFamily(
    Font(R.font.third_man_regular, FontWeight.Normal),
)

val BebasNeueFontFamily = FontFamily(
    Font(R.font.bebas_neue_regular, FontWeight.Normal),
)

val PoppinsFontFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
)

// Font Awesome icon families
val FontAwesomeRegular = FontFamily(
    Font(R.font.fa_regular_400, FontWeight.Normal),
)

val FontAwesomeBrands = FontFamily(
    Font(R.font.fa_brands_400, FontWeight.Normal),
)

// Text styles — scale is applied externally by PD2026Theme
fun buildTypography(scale: Float = 1.0f) = androidx.compose.material3.Typography(
    // Display — 3rd Man: screen titles, big home buttons, day tabs
    displayLarge = TextStyle(
        fontFamily = ThirdManFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (32 * scale).sp,
        lineHeight = (38 * scale).sp,
        letterSpacing = 1.sp,
    ),
    displayMedium = TextStyle(
        fontFamily = ThirdManFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (28 * scale).sp,
        lineHeight = (34 * scale).sp,
        letterSpacing = 1.sp,
    ),
    displaySmall = TextStyle(
        fontFamily = ThirdManFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (24 * scale).sp,
        lineHeight = (30 * scale).sp,
        letterSpacing = 0.5.sp,
    ),

    // Headline — Bebas Neue: sub-titles, labels, ribbons, captions
    headlineLarge = TextStyle(
        fontFamily = BebasNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (22 * scale).sp,
        lineHeight = (28 * scale).sp,
        letterSpacing = 1.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = BebasNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (18 * scale).sp,
        lineHeight = (24 * scale).sp,
        letterSpacing = 1.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = BebasNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (15 * scale).sp,
        lineHeight = (20 * scale).sp,
        letterSpacing = 0.5.sp,
    ),

    // Title — Bebas Neue (tags, chips, timetable labels)
    titleLarge = TextStyle(
        fontFamily = BebasNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (18 * scale).sp,
        lineHeight = (24 * scale).sp,
        letterSpacing = 1.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = BebasNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (16 * scale).sp,
        lineHeight = (22 * scale).sp,
        letterSpacing = 0.5.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = BebasNeueFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (13 * scale).sp,
        lineHeight = (18 * scale).sp,
        letterSpacing = 0.5.sp,
    ),

    // Body — Poppins: paragraphs, descriptions, settings, list rows
    bodyLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (16 * scale).sp,
        lineHeight = (24 * scale).sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (14 * scale).sp,
        lineHeight = (20 * scale).sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (12 * scale).sp,
        lineHeight = (16 * scale).sp,
        letterSpacing = 0.sp,
    ),

    // Label — Poppins (small UI labels)
    labelLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (14 * scale).sp,
        lineHeight = (20 * scale).sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (12 * scale).sp,
        lineHeight = (16 * scale).sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = (11 * scale).sp,
        lineHeight = (16 * scale).sp,
        letterSpacing = 0.sp,
    ),
)
