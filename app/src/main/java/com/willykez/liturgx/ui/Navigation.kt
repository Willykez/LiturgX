package com.willykez.liturgx.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.willykez.liturgx.data.ProgressStore
import com.willykez.liturgx.data.bible.BibleBrowseRepository
import com.willykez.liturgx.ui.bible.BibleScreen
import com.willykez.liturgx.ui.calendar.CalendarScreen
import com.willykez.liturgx.ui.components.SeasonBackdrop
import com.willykez.liturgx.ui.home.HomeScreen
import com.willykez.liturgx.ui.saints.SaintsScreen
import com.willykez.liturgx.ui.saved.SavedScreen
import com.willykez.liturgx.ui.settings.SettingsSheetContent
import com.willykez.liturgx.ui.theme.LiturgXTheme
import com.willykez.liturgx.ui.theme.isDarkThemeActive
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.launch

/**
 * Settings ("Mipangilio") used to be its own bottom-nav tab/destination -- navigating to it
 * meant leaving whatever you were doing (e.g. mid-read on Leo/Kalenda) and losing your place.
 * It's now a [ModalBottomSheet] reachable from a small gear icon docked in a top bar present
 * on every screen, so a person can nudge a setting (font size, region toggle, reminders)
 * without ever navigating away -- the screen underneath stays exactly as it was, still visible
 * and scrolled to the same spot, once the sheet is dismissed.
 */
private sealed class Dest(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Leo : Dest("leo", "Leo", Icons.Filled.WbSunny)
    object Kalenda : Dest("kalenda", "Kalenda", Icons.Filled.CalendarMonth)
    object Biblia : Dest("biblia", "Biblia", Icons.Filled.MenuBook)
    object Watakatifu : Dest("watakatifu", "Watakatifu", Icons.Filled.Star)
    object Hifadhi : Dest("hifadhi", "Hifadhi", Icons.Filled.Bookmarks)
}

private val destinations = listOf(Dest.Leo, Dest.Kalenda, Dest.Biblia, Dest.Watakatifu, Dest.Hifadhi)

/** "🔥 1" in the top bar, matching BibliaApp's reading-streak badge -- reads [ProgressStore]
 *  fresh on every recomposition (same pattern as the other small SharedPreferences-backed
 *  stores elsewhere in the app), so it picks up today's streak as soon as [HomeScreen] records
 *  it. Hidden entirely at zero so a brand-new install doesn't lead with a "0" that reads as a
 *  broken feature rather than an un-started one. */
@Composable
private fun StreakBadge(today: java.time.LocalDate, accent: Color) {
    val context = LocalContext.current
    val progressStore = remember { ProgressStore(context) }
    val streak = ProgressStore.currentStreak(progressStore.openedDates(), today)
    if (streak <= 0) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 4.dp)
    ) {
        Icon(Icons.Filled.LocalFireDepartment, contentDescription = "Siku $streak mfululizo", tint = accent, modifier = Modifier.padding(end = 2.dp))
        Text(streak.toString(), style = MaterialTheme.typography.labelLarge, color = accent)
    }
}

private fun titleFor(route: String?): String = when (route) {
    Dest.Leo.route -> "LiturgX"
    Dest.Kalenda.route -> "Kalenda"
    Dest.Biblia.route -> "Biblia"
    Dest.Watakatifu.route -> "Watakatifu"
    Dest.Hifadhi.route -> "Yaliyohifadhiwa"
    else -> "LiturgX"
}

@OptIn(ExperimentalMaterial3Api::class)
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

    // Every tab is one back-press away from Leo, and only Leo itself lets the press fall
    // through to the system (which exits the app) -- so back never dumps someone straight out
    // of the app from Kalenda/Biblia/Watakatifu/Hifadhi. Bible's own internal drill-down
    // (Reader -> Chapters -> Books) is handled separately, inside BibleScreen -- by the time
    // back reaches here from that tab, it's already at Books and this just steps up to Leo.
    BackHandler(enabled = currentRoute != null && currentRoute != Dest.Leo.route) {
        navController.navigate(Dest.Leo.route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

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

    var showSettingsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val sheetScope = rememberCoroutineScope()

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
                topBar = {
                    TopAppBar(
                        title = { Text(titleFor(currentRoute), style = MaterialTheme.typography.titleLarge) },
                        actions = {
                            StreakBadge(today = vm.today, accent = seasonAccent(accentColor))
                            IconButton(onClick = { showSettingsSheet = true }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Mipangilio")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = MaterialTheme.colorScheme.onBackground,
                            actionIconContentColor = seasonAccent(accentColor)
                        ),
                        windowInsets = TopAppBarDefaults.windowInsets
                    )
                },
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
                            HomeScreen(
                                todayResult = vm.todayResult,
                                region = vm.region,
                                onOpenInBible = { bookId, chapterNum, verseNum ->
                                    vm.requestBibleJump(bookId, chapterNum, verseNum)
                                    navController.navigate(Dest.Biblia.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onOpenSaint = { saintId ->
                                    vm.requestSaintJump(saintId)
                                    navController.navigate(Dest.Watakatifu.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable(Dest.Kalenda.route) {
                            CalendarScreen(
                                selectedResult = vm.selectedResult,
                                region = vm.region,
                                onSelectDate = { vm.goToDate(it) },
                                onJumpToToday = { vm.jumpToToday() },
                                onOpenInBible = { bookId, chapterNum, verseNum ->
                                    vm.requestBibleJump(bookId, chapterNum, verseNum)
                                    navController.navigate(Dest.Biblia.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onOpenSaint = { saintId ->
                                    vm.requestSaintJump(saintId)
                                    navController.navigate(Dest.Watakatifu.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                        composable(Dest.Watakatifu.route) {
                            SaintsScreen(
                                saints = vm.saintsList(),
                                pendingSaintId = vm.pendingSaintId,
                                onSaintHandled = { vm.consumeSaintJump() }
                            )
                        }
                        composable(Dest.Biblia.route) {
                            BibleScreen(
                                currentColor = vm.selectedResult.resolved.color,
                                pendingJump = vm.pendingBibleJump,
                                onJumpHandled = { vm.consumeBibleJump() }
                            )
                        }
                        composable(Dest.Hifadhi.route) {
                            val savedRepository = remember { BibleBrowseRepository(navController.context.applicationContext) }
                            val savedBooks = remember { savedRepository.allBooks() }
                            SavedScreen(
                                books = savedBooks,
                                color = vm.selectedResult.resolved.color,
                                onSelectVerse = { bookId, chapterNum, verseNum ->
                                    vm.requestBibleJump(bookId, chapterNum, verseNum)
                                    navController.navigate(Dest.Biblia.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (showSettingsSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showSettingsSheet = false },
                    sheetState = sheetState,
                    containerColor = background
                ) {
                    SettingsSheetContent(
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
                        onTextScaleChange = { vm.updateTextScale(it) },
                        onClose = {
                            sheetScope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion { showSettingsSheet = false }
                        }
                    )
                }
            }
        }
    }
}
