package com.willykez.liturgx.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Scripture reads like it's printed in a missal (serif); UI chrome stays clean sans-serif.
val ScriptureFont = FontFamily.Serif
val ChromeFont = FontFamily.SansSerif

/** User-facing choice, persisted via SettingsStore -- an accessibility control, not a cosmetic
 *  one: scales every TextStyle's fontSize (and lineHeight, so lines don't start overlapping at
 *  larger sizes) app-wide via [scaledTypography], the same shape Android's own system text-size
 *  setting takes, rather than a "reading mode" that only touches Scripture text and leaves
 *  button labels and dialogs behind at the base size. */
enum class TextScale(val label: String, val factor: Float) {
    NDOGO("Ndogo", 0.9f),
    WASTANI("Wastani", 1.0f),
    KUBWA("Kubwa", 1.15f),
    KUBWA_ZAIDI("Kubwa Zaidi", 1.3f)
}

val LiturgXTypography = Typography(
    displaySmall = TextStyle(fontFamily = ScriptureFont, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = ScriptureFont, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = ChromeFont, fontWeight = FontWeight.Bold, fontSize = 20.sp),
    titleMedium = TextStyle(fontFamily = ChromeFont, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    titleSmall = TextStyle(fontFamily = ChromeFont, fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.8.sp),
    bodyLarge = TextStyle(fontFamily = ScriptureFont, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp),
    bodyMedium = TextStyle(fontFamily = ScriptureFont, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 23.sp),
    labelLarge = TextStyle(fontFamily = ChromeFont, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = ChromeFont, fontWeight = FontWeight.Medium, fontSize = 12.sp, letterSpacing = 0.6.sp),
    labelSmall = TextStyle(fontFamily = ChromeFont, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.6.sp),
)

fun scaledTypography(scale: TextScale): Typography {
    if (scale == TextScale.WASTANI) return LiturgXTypography
    fun TextStyle.scaled() = copy(
        fontSize = fontSize * scale.factor,
        lineHeight = if (lineHeight.isSp) lineHeight * scale.factor else lineHeight
    )
    return Typography(
        displaySmall = LiturgXTypography.displaySmall.scaled(),
        headlineSmall = LiturgXTypography.headlineSmall.scaled(),
        titleLarge = LiturgXTypography.titleLarge.scaled(),
        titleMedium = LiturgXTypography.titleMedium.scaled(),
        titleSmall = LiturgXTypography.titleSmall.scaled(),
        bodyLarge = LiturgXTypography.bodyLarge.scaled(),
        bodyMedium = LiturgXTypography.bodyMedium.scaled(),
        labelLarge = LiturgXTypography.labelLarge.scaled(),
        labelMedium = LiturgXTypography.labelMedium.scaled(),
        labelSmall = LiturgXTypography.labelSmall.scaled(),
    )
}
