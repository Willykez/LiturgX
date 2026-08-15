package com.willykez.liturgx

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.ui.screens.CalendarScreen
import com.willykez.liturgx.ui.screens.PrayersScreen
import com.willykez.liturgx.ui.screens.ReadingsScreen
import com.willykez.liturgx.ui.screens.SavedScreen
import com.willykez.liturgx.ui.screens.SettingsScreen
import com.willykez.liturgx.ui.theme.LiturgicalCalendarTheme
import com.willykez.liturgx.ui.theme.ThemeMode
import com.willykez.liturgx.ui.viewmodel.LiturgicalViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Readings : Screen("readings", "Readings", Icons.Default.MenuBook)
    object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    object Prayers : Screen("prayers", "Prayers", Icons.Default.AutoAwesome)
    object Saved : Screen("saved", "Saved", Icons.Default.Bookmark)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: LiturgicalViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val preferences by viewModel.userPreferences.collectAsState()
            val themeMode = remember(preferences.darkMode) {
                try {
                    ThemeMode.valueOf(preferences.darkMode)
                } catch (e: Exception) {
                    ThemeMode.SYSTEM
                }
            }

            LiturgicalCalendarTheme(themeMode = themeMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: LiturgicalViewModel) {
    // A real back stack for the bottom tabs: switching tabs pushes onto it, and the system
    // back gesture/button pops one entry at a time instead of exiting the app immediately.
    // Readings is always the root — the stack never drops below it.
    val backStack = remember { mutableStateListOf<Screen>(Screen.Readings) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Readings) }
    val isDistractionFree by viewModel.distractionFreeMode.collectAsState()

    fun navigateTo(screen: Screen) {
        if (screen.route == currentScreen.route) return
        backStack.add(screen)
        currentScreen = screen
    }

    // Only intercept back when there's actually somewhere to go — otherwise let the
    // system handle it natively (predictive-back edge swipe / app exit animation).
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
        currentScreen = backStack.last()
    }

    val screens = listOf(
        Screen.Readings,
        Screen.Calendar,
        Screen.Prayers,
        Screen.Saved,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (!isDistractionFree) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    screens.forEach { screen ->
                        val isSelected = currentScreen.route == screen.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { navigateTo(screen) },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("nav_item_${screen.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val modifier = Modifier.padding(innerPadding)

        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                fadeIn(animationSpec = tween(180)) togetherWith fadeOut(animationSpec = tween(120))
            },
            label = "tab_transition"
        ) { screen ->
            when (screen) {
                Screen.Readings -> ReadingsScreen(
                    viewModel = viewModel,
                    onOpenCalendarPicker = { navigateTo(Screen.Calendar) },
                    modifier = modifier
                )
                Screen.Calendar -> CalendarScreen(
                    viewModel = viewModel,
                    onDateSelected = { navigateTo(Screen.Readings) },
                    modifier = modifier
                )
                Screen.Prayers -> PrayersScreen(
                    viewModel = viewModel,
                    modifier = modifier
                )
                Screen.Saved -> SavedScreen(
                    viewModel = viewModel,
                    modifier = modifier
                )
                Screen.Settings -> SettingsScreen(
                    viewModel = viewModel,
                    modifier = modifier
                )
            }
        }
    }
}
