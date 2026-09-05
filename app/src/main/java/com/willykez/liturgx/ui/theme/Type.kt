package com.willykez.liturgx.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Scripture reads like it's printed in a missal (serif); UI chrome stays clean sans-serif.
val ScriptureFont = FontFamily.Serif
val ChromeFont = FontFamily.SansSerif

/**
 * User-facing choice, persisted via SettingsStore -- an accessibility control, not a cosmetic
 * one: scales every TextStyle's fontSize (and lineHeight, so lines don't start overlapping at
 * larger sizes) app-wide via [scaledTypography], the same shape Android's own system text-size
 * setting takes, rather than a "reading mode" that only touches Scripture text and leaves
 * button labels and dialogs behind at the base size.
 *
 * Previously a fixed 4-step enum (Ndogo/Wastani/Kubwa/Kubwa Zaidi); now a continuous factor
 * driven by a Material3 [androidx.compose.material3.Slider] in Settings, so a person can dial
 * in exactly the size that's comfortable instead of picking the nearest of four presets.
 */
object TextScale {
    const val MIN = 0.85f
    const val MAX = 1.5f
    const val DEFAULT = 1.0f

    fun coerce(value: Float): Float = value.coerceIn(MIN, MAX)

    /** Back-compat: the old build stored one of four named presets. Map them onto the new
     *  continuous range so nobody's saved preference silently resets on upgrade. */
    fun fromLegacyName(name: String): Float? = when (name) {
        "NDOGO" -> 0.9f
        "WASTANI" -> 1.0f
        "KUBWA" -> 1.15f
        "KUBWA_ZAIDI" -> 1.3f
        else -> null
    }
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

fun scaledTypography(scale: Float): Typography {
    if (scale == 1.0f) return LiturgXTypography
    fun TextStyle.scaled() = copy(
        fontSize = fontSize * scale,
        lineHeight = if (lineHeight.isSp) lineHeight * scale else lineHeight
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
