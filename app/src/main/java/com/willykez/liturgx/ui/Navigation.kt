package com.willykez.liturgx.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.willykez.liturgx.ui.bible.BibleScreen
import com.willykez.liturgx.ui.calendar.CalendarScreen
import com.willykez.liturgx.ui.components.SeasonBackdrop
import com.willykez.liturgx.ui.home.HomeScreen
import com.willykez.liturgx.ui.saints.SaintsScreen
import com.willykez.liturgx.ui.settings.SettingsScreen
import com.willykez.liturgx.ui.theme.LiturgXTheme
import com.willykez.liturgx.ui.theme.isDarkThemeActive
import com.willykez.liturgx.ui.theme.seasonAccent

private sealed class Dest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Leo : Dest("leo", "Leo", Icons.Filled.WbSunny)
    object Kalenda : Dest("kalenda", "Kalenda", Icons.Filled.CalendarMonth)
    object Biblia : Dest("biblia", "Biblia", Icons.Filled.MenuBook)
    object Watakatifu : Dest("watakatifu", "Watakatifu", Icons.Filled.Star)
    object Mipangilio : Dest("mipangilio", "Mipangilio", Icons.Filled.Settings)
}

private val destinations = listOf(Dest.Leo, Dest.Kalenda, Dest.Biblia, Dest.Watakatifu, Dest.Mipangilio)

@Composable
fun LiturgXApp() {
    val vm: LectionaryViewModel = viewModel()
    val navController = rememberNavController()

    // The app's overall theme accent follows whichever screen's day is currently in view —
    // Home always shows "today", Calendar/Saints/Settings follow the browsed/selected date.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val accentColor = if (currentRoute == Dest.Leo.route) vm.todayResult.resolved.color
    else vm.selectedResult.resolved.color

    val darkTheme = isDarkThemeActive(vm.themeMode)

    // Status bar ICONS (clock/battery/signal) need to track this app's own theme choice, not
    // necessarily the system's -- ThemeMode.LIGHT/DARK can be picked independently of the
    // device's own dark mode, so the system default (which follows device dark mode) would be
    // wrong exactly when the person has overridden the app's theme.
    val view = LocalView.current
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    LiturgXTheme(accent = accentColor, darkTheme = darkTheme, textScale = vm.textScale) {
        val background = MaterialTheme.colorScheme.background
        val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

        // Painted ONCE here, full-bleed behind the status bar too (this Box is never inset by
        // Scaffold's padding, unlike its content) -- previously each screen painted its own
        // backdrop *inside* Scaffold's padded content area, leaving a visible seam where the
        // status bar showed Scaffold's plain containerColor instead of the season wash.
        Box(Modifier.fillMaxSize()) {
            SeasonBackdrop(accentColor, modifier = Modifier.fillMaxSize())

            Scaffold(
                containerColor = Color.Transparent,
                bottomBar = {
                    NavigationBar(containerColor = background) {
                        val accent = seasonAccent(accentColor)
                        destinations.forEach { dest ->
                            val selected = currentRoute == dest.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(dest.icon, contentDescription = dest.label) },
                                label = { Text(dest.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = accent,
                                    selectedTextColor = accent,
                                    unselectedIconColor = onSurfaceVariant,
                                    unselectedTextColor = onSurfaceVariant,
                                    indicatorColor = accent.copy(alpha = 0.18f)
                                )
                            )
                        }
                    }
                }
            ) { padding ->
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    NavHost(
                        navController = navController,
                        startDestination = Dest.Leo.route,
                        modifier = Modifier
                            .padding(padding)
                            .widthIn(max = 640.dp)
                            .fillMaxSize()
                    ) {
                    composable(Dest.Leo.route) {
                        HomeScreen(todayResult = vm.todayResult)
                    }
                    composable(Dest.Kalenda.route) {
                        CalendarScreen(
                            selectedResult = vm.selectedResult,
                            region = vm.region,
                            onSelectDate = { vm.goToDate(it) },
                            onJumpToToday = { vm.jumpToToday() }
                        )
                    }
                    composable(Dest.Watakatifu.route) {
                        SaintsScreen(saints = vm.saintsList())
                    }
                    composable(Dest.Biblia.route) {
                        BibleScreen(currentColor = vm.selectedResult.resolved.color)
                    }
                    composable(Dest.Mipangilio.route) {
                        SettingsScreen(
                            region = vm.region,
                            themeMode = vm.themeMode,
                            currentColor = vm.selectedResult.resolved.color,
                            reminderEnabled = vm.reminderEnabled,
                            reminderHour = vm.reminderHour,
                            reminderMinute = vm.reminderMinute,
                            verseReminderEnabled = vm.verseReminderEnabled,
                            verseReminderHour = vm.verseReminderHour,
                            verseReminderMinute = vm.verseReminderMinute,
                            textScale = vm.textScale,
                            onRegionChange = { vm.updateRegion(it) },
                            onThemeModeChange = { vm.updateThemeMode(it) },
                            onReminderEnabledChange = { vm.updateReminderEnabled(it) },
                            onReminderTimeChange = { h, m -> vm.updateReminderTime(h, m) },
                            onVerseReminderEnabledChange = { vm.updateVerseReminderEnabled(it) },
                            onVerseReminderTimeChange = { h, m -> vm.updateVerseReminderTime(h, m) },
                            onTextScaleChange = { vm.updateTextScale(it) }
                        )
                    }
                }
            }
        }
    }
}
}
