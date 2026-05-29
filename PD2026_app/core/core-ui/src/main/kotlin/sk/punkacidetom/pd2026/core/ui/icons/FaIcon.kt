package sk.punkacidetom.pd2026.core.ui.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import sk.punkacidetom.pd2026.core.ui.theme.FontAwesomeBrands
import sk.punkacidetom.pd2026.core.ui.theme.FontAwesomeRegular

enum class FaFamily { Regular, Brands }

/**
 * Renders a Font Awesome icon glyph as text.
 *
 * Usage:
 *   FaIcon("heart")                                          // FA Regular
 *   FaIcon("spotify", family = FaFamily.Brands)             // FA Brands
 *   FaIcon("heart", size = 24.dp, tint = Color.Red)
 */
@Composable
fun FaIcon(
    name: String,
    modifier: Modifier = Modifier,
    family: FaFamily = FaFamily.Regular,
    size: Dp = 20.dp,
    tint: Color = LocalContentColor.current,
) {
    val codepoint = when (family) {
        FaFamily.Regular -> FaRegularCodes[name]
        FaFamily.Brands -> FaBrandsCodes[name]
    }
    val fontFamily = when (family) {
        FaFamily.Regular -> FontAwesomeRegular
        FaFamily.Brands -> FontAwesomeBrands
    }

    val glyph = if (codepoint != null) {
        String(Character.toChars(codepoint))
    } else {
        "?"
    }

    Text(
        text = glyph,
        modifier = modifier,
        style = TextStyle(
            fontFamily = fontFamily,
            fontSize = size.value.sp,
            color = tint,
        ),
    )
}
