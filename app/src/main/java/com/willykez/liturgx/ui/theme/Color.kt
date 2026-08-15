package com.willykez.liturgx.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Editorial "ink on paper" palette. One accent, generous neutral range, season colors
 * demoted to small indicators (a dot, a rule) rather than backgrounds or chrome.
 */

// Paper (light mode)
val PaperBackground = Color(0xFFFAF7F1)
val PaperSurface = Color(0xFFFFFFFF)
val PaperSurfaceVariant = Color(0xFFF0EBE1)
val InkPrimary = Color(0xFF1B1A17)
val InkSecondary = Color(0xFF5B5750)

// Night (dark mode) — true near-black, not navy
val NightBackground = Color(0xFF121110)
val NightSurface = Color(0xFF1B1A18)
val NightSurfaceVariant = Color(0xFF262421)
val NightTextPrimary = Color(0xFFEDE9E1)
val NightTextSecondary = Color(0xFFA39C90)

// Evening / distraction-free — warm sepia, dimmer than night mode
val EveningBackground = Color(0xFF181410)
val EveningSurface = Color(0xFF211C16)
val EveningTextPrimary = Color(0xFFE7DCC8)
val EveningTextSecondary = Color(0xFF9C8F76)

// Single brand accent — a muted liturgical red, used sparingly for interactive elements
val AccentLight = Color(0xFF7A2E22)
val AccentDark = Color(0xFFD98B76)

val ErrorLight = Color(0xFFB3261E)
val ErrorDark = Color(0xFFF2B8B5)

// Liturgical season indicator colors — used only as a small dot/rule, never chrome
val LiturgicalGreen = Color(0xFF3C6E47)
val LiturgicalPurple = Color(0xFF6A4C93)
val LiturgicalGoldWhite = Color(0xFFB08D2E)
val LiturgicalRed = Color(0xFFA13A2E)
val LiturgicalRose = Color(0xFFB4667A)
