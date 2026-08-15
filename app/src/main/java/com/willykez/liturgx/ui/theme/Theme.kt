package com.willykez.liturgx.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = AccentLight,
    onPrimary = Color.White,
    primaryContainer = PaperSurfaceVariant,
    onPrimaryContainer = InkPrimary,
    secondary = InkSecondary,
    background = PaperBackground,
    surface = PaperSurface,
    surfaceVariant = PaperSurfaceVariant,
    surfaceTint = AccentLight,
    onBackground = InkPrimary,
    onSurface = InkPrimary,
    onSurfaceVariant = InkSecondary,
    error = ErrorLight
)

private val DarkColorScheme = darkColorScheme(
    primary = AccentDark,
    onPrimary = Color.Black,
    primaryContainer = NightSurfaceVariant,
    onPrimaryContainer = NightTextPrimary,
    secondary = NightTextSecondary,
    background = NightBackground,
    surface = NightSurface,
    surfaceVariant = NightSurfaceVariant,
    surfaceTint = AccentDark,
    onBackground = NightTextPrimary,
    onSurface = NightTextPrimary,
    onSurfaceVariant = NightTextSecondary,
    error = ErrorDark
)

private val EveningColorScheme = darkColorScheme(
    primary = AccentDark,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF2A2116),
    onPrimaryContainer = EveningTextPrimary,
    secondary = EveningTextSecondary,
    background = EveningBackground,
    surface = EveningSurface,
    surfaceVariant = Color(0xFF29231B),
    surfaceTint = AccentDark,
    onBackground = EveningTextPrimary,
    onSurface = EveningTextPrimary,
    onSurfaceVariant = EveningTextSecondary,
    error = ErrorDark
)

enum class ThemeMode {
    SYSTEM, LIGHT, DARK, EVENING
}

@Composable
fun LiturgicalCalendarTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.EVENING -> true
    }

    val colorScheme = when {
        themeMode == ThemeMode.EVENING -> EveningColorScheme
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
