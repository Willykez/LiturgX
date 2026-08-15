package com.willykez.liturgx.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.model.AppLanguage
import com.willykez.liturgx.ui.viewmodel.LiturgicalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: LiturgicalViewModel,
    modifier: Modifier = Modifier
) {
    val preferences by viewModel.userPreferences.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val useSerif by viewModel.useSerifFont.collectAsState()
    val interfaceLanguage by viewModel.interfaceLanguage.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings & Notifications",
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Interface Language Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Language, contentDescription = "Language")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lugha ya Programu / App Language",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Controls section labels and headings. The reading text itself " +
                            "is always Swahili — that's the only Bible text bundled with the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val languages = listOf(
                        Pair(AppLanguage.SWAHILI, "Kiswahili"),
                        Pair(AppLanguage.ENGLISH, "English")
                    )

                    languages.forEach { (lang, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = interfaceLanguage == lang,
                                onCheckedChange = { viewModel.updateInterfaceLanguage(lang) },
                                modifier = Modifier.testTag("language_switch_${lang.name}")
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // Theme Mode Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Brightness4, contentDescription = "Theme")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reading Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val themes = listOf(
                        Pair("SYSTEM", "System Default"),
                        Pair("LIGHT", "Light Theme"),
                        Pair("DARK", "Dark Theme"),
                        Pair("EVENING", "Evening Mode (Warm)")
                    )

                    themes.forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = preferences.darkMode == mode,
                                onCheckedChange = { viewModel.updateThemeMode(mode) },
                                modifier = Modifier.testTag("theme_switch_$mode")
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }

            // Custom Push Notification Reminders Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Liturgical Reminders & Push Alerts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Daily Mass Readings Reminder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Daily Mass Readings Reminder",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Scheduled for ${String.format("%02d:%02d", preferences.dailyReminderHour, preferences.dailyReminderMinute)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Switch(
                            checked = preferences.dailyReminderEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateDailyReminder(enabled, preferences.dailyReminderHour, preferences.dailyReminderMinute)
                            },
                            modifier = Modifier.testTag("daily_reminder_switch")
                        )
                    }

                    if (preferences.dailyReminderEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        viewModel.updateDailyReminder(true, hourOfDay, minute)
                                    },
                                    preferences.dailyReminderHour,
                                    preferences.dailyReminderMinute,
                                    true
                                ).show()
                            },
                            modifier = Modifier.testTag("change_daily_time_button")
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Daily Morning Time")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Evening Vespers & Night Prayer Reminder
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Evening Prayer Reminder",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Scheduled for ${String.format("%02d:%02d", preferences.eveningReminderHour, preferences.eveningReminderMinute)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Switch(
                            checked = preferences.eveningReminderEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.updateEveningReminder(enabled, preferences.eveningReminderHour, preferences.eveningReminderMinute)
                            },
                            modifier = Modifier.testTag("evening_reminder_switch")
                        )
                    }

                    if (preferences.eveningReminderEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        viewModel.updateEveningReminder(true, hourOfDay, minute)
                                    },
                                    preferences.eveningReminderHour,
                                    preferences.eveningReminderMinute,
                                    true
                                ).show()
                            },
                            modifier = Modifier.testTag("change_evening_time_button")
                        ) {
                            Icon(Icons.Default.Alarm, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Set Evening Time")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Feast Days & Liturgical Season Alerts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Solemnities & Feast Alerts",
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Get notified on Holy Days of Obligation and major liturgical seasons",
                                style = MaterialTheme.typography.bodyMedium,
                                fontSize = 12.sp
                            )
                        }

                        Switch(
                            checked = preferences.feastAlertsEnabled,
                            onCheckedChange = { viewModel.updateFeastAlerts(it) },
                            modifier = Modifier.testTag("feast_alerts_switch")
                        )
                    }
                }
            }

            // Typography Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TextFormat, contentDescription = "Typography")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reading Font Preferences",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Use Traditional Serif Font",
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.SemiBold
                        )
                        Switch(
                            checked = useSerif,
                            onCheckedChange = { viewModel.toggleSerifFont() },
                            modifier = Modifier.testTag("settings_serif_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Font Scale: ${(fontScale * 100).toInt()}%",
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = fontScale,
                        onValueChange = { viewModel.updateFontScale(it) },
                        valueRange = 0.8f..1.5f,
                        steps = 6
                    )
                }
            }

            // App Information
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About LiturgX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Version 1.0.0 • Distraction-free Catholic Liturgical calendar, offline readings, Gospel reflections, and prayer companion.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
