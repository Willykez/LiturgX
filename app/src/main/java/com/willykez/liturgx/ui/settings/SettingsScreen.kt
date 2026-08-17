package com.willykez.liturgx.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.willykez.liturgx.core.EpiphanyMode
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.ui.components.SeasonBackdrop
import com.willykez.liturgx.ui.theme.ThemeMode
import com.willykez.liturgx.ui.theme.seasonAccentSoft

@Composable
fun SettingsScreen(
    region: RegionSettings,
    themeMode: ThemeMode,
    currentColor: LiturgicalColor,
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    onRegionChange: (RegionSettings) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> onReminderEnabledChange(granted) }

    Box(modifier.fillMaxSize()) {
        SeasonBackdrop(currentColor)
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Mipangilio", style = MaterialTheme.typography.headlineSmall, color = onBg)
            Text(
                "Mwonekano wa programu na mila za jimbo lako.",
                style = MaterialTheme.typography.labelMedium,
                color = onBgDim
            )
            Spacer(Modifier.height(20.dp))

            SettingCard(
                title = "Kikumbusho cha Kila Siku",
                description = "Pokea arifa kila siku pindi masomo ya Kiliturujia ya siku hiyo yanapokuwa tayari.",
                color = currentColor
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Washa Kikumbusho", color = onBg, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { checked ->
                            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                val hasPermission = ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.POST_NOTIFICATIONS
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasPermission) {
                                    onReminderEnabledChange(true)
                                } else {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                onReminderEnabledChange(checked)
                            }
                        }
                    )
                }

                if (reminderEnabled) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Muda: %02d:%02d".format(reminderHour, reminderMinute),
                            color = onBg,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(onClick = {
                            TimePickerDialog(
                                context,
                                { _, h, m -> onReminderTimeChange(h, m) },
                                reminderHour,
                                reminderMinute,
                                true
                            ).show()
                        }) {
                            Text("Badilisha Muda")
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            SettingCard(
                title = "Mwonekano",
                description = "Chagua mwonekano wa mwanga au giza, au uache programu ifuate mfumo wa simu yako.",
                color = currentColor
            ) {
                Column(Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEach { mode ->
                        ThemeModeRow(
                            label = mode.label,
                            selected = themeMode == mode,
                            accentColor = currentColor,
                            onClick = { onThemeModeChange(mode) }
                        )
                        if (mode != ThemeMode.entries.last()) Spacer(Modifier.height(2.dp))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            SettingCard(
                title = "Epifania",
                description = "Baadhi ya majimbo huadhimisha Epifania Januari 6 daima; mengine huihamishia Dominika iliyo karibu (Jan 2–8).",
                color = currentColor
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hamishiwa Dominika", color = onBg, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = region.epiphanyMode == EpiphanyMode.TRANSFERRED,
                        onCheckedChange = { checked ->
                            onRegionChange(region.copy(epiphanyMode = if (checked) EpiphanyMode.TRANSFERRED else EpiphanyMode.FIXED_JAN6))
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            SettingCard(
                title = "Kupaa kwa Bwana & Fungu Takatifu",
                description = "Majimbo mengi huhamishia sikukuu hizi Dominika; machache huzishika Alhamisi kama ilivyo asili.",
                color = currentColor
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Shika Alhamisi", color = onBg, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = region.keepThursdaySolemnities,
                        onCheckedChange = { onRegionChange(region.copy(keepThursdaySolemnities = it)) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "LiturgX · Masomo ya Kila Siku kwa Kiswahili",
                style = MaterialTheme.typography.labelSmall,
                color = onBgDim
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ThemeModeRow(label: String, selected: Boolean, accentColor: LiturgicalColor, onClick: () -> Unit) {
    val onBg = MaterialTheme.colorScheme.onBackground
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = selected, onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label, color = onBg, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    color: LiturgicalColor,
    content: @Composable ColumnScope.() -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(seasonAccentSoft(color))
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = onBg)
        Spacer(Modifier.height(4.dp))
        Text(description, style = MaterialTheme.typography.labelMedium, color = onBgDim)
        Spacer(Modifier.height(12.dp))
        content()
    }
}
