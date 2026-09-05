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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.willykez.liturgx.core.EpiphanyMode
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.data.sharing.PdfShareUtils
import com.willykez.liturgx.data.sharing.YearlyLectionaryPdfGenerator
import com.willykez.liturgx.ui.theme.TextScale
import com.willykez.liturgx.ui.theme.ThemeMode
import com.willykez.liturgx.ui.theme.seasonAccentSoft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Settings content for the app-wide [androidx.compose.material3.ModalBottomSheet] (see
 * [com.willykez.liturgx.ui.LiturgXApp]) -- previously its own bottom-nav destination/screen,
 * now reachable as an overlay from every screen's top bar so a person never has to leave
 * whatever they're doing (reading, browsing the calendar) just to nudge a setting.
 */
@Composable
fun SettingsSheetContent(
    region: RegionSettings,
    themeMode: ThemeMode,
    currentColor: LiturgicalColor,
    reminderEnabled: Boolean,
    reminderHour: Int,
    reminderMinute: Int,
    verseReminderEnabled: Boolean,
    verseReminderHour: Int,
    verseReminderMinute: Int,
    textScale: Float,
    onRegionChange: (RegionSettings) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (Int, Int) -> Unit,
    onVerseReminderEnabledChange: (Boolean) -> Unit,
    onVerseReminderTimeChange: (Int, Int) -> Unit,
    onTextScaleChange: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Mipangilio", style = MaterialTheme.typography.headlineSmall, color = onBg)
                Text(
                    "Mwonekano wa programu na mila za jimbo lako.",
                    style = MaterialTheme.typography.labelMedium,
                    color = onBgDim
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Funga")
            }
        }
        Spacer(Modifier.height(16.dp))

        SettingCard(
            title = "Kikumbusho cha Kila Siku",
            description = "Pokea arifa kila siku pindi masomo ya Kiliturujia ya siku hiyo yanapokuwa tayari.",
            color = currentColor
        ) {
            ReminderToggleContent(
                enabled = reminderEnabled,
                hour = reminderHour,
                minute = reminderMinute,
                onEnabledChange = onReminderEnabledChange,
                onTimeChange = onReminderTimeChange
            )
        }

        Spacer(Modifier.height(14.dp))

        SettingCard(
            title = "Neno la Kila Siku",
            description = "Pokea andiko fupi la kutafakari kwa muda unaochagua -- huru dhidi ya masomo ya siku.",
            color = currentColor
        ) {
            ReminderToggleContent(
                enabled = verseReminderEnabled,
                hour = verseReminderHour,
                minute = verseReminderMinute,
                onEnabledChange = onVerseReminderEnabledChange,
                onTimeChange = onVerseReminderTimeChange
            )
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
            title = "Ukubwa wa Maandishi",
            description = "Buruta ili kupunguza au kuongeza ukubwa wa maandishi kote kwenye programu.",
            color = currentColor
        ) {
            TextScaleSlider(textScale = textScale, onTextScaleChange = onTextScaleChange, accentColor = currentColor)
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

        Spacer(Modifier.height(14.dp))

        SettingCard(
            title = "Kalenda ya Mwaka (PDF)",
            description = "Pakua orodha ya masomo ya Dominika zote na Sikukuu Maalum za mwaka mzima, tayari kuchapishwa.",
            color = currentColor
        ) {
            YearlyPdfExportButton(region = region, accentColor = currentColor)
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

@Composable
private fun TextScaleSlider(textScale: Float, onTextScaleChange: (Float) -> Unit, accentColor: LiturgicalColor) {
    val onBg = MaterialTheme.colorScheme.onBackground
    var sliderPosition by remember(textScale) { mutableStateOf(textScale) }

    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("A", style = MaterialTheme.typography.bodyMedium, color = onBg)
            Text(
                "${(sliderPosition * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = onBg,
                fontWeight = FontWeight.Bold
            )
            Text("A", style = MaterialTheme.typography.headlineSmall, color = onBg)
        }
        Slider(
            value = sliderPosition,
            valueRange = TextScale.MIN..TextScale.MAX,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = { onTextScaleChange(sliderPosition) },
            colors = SliderDefaults.colors(
                thumbColor = androidx.compose.ui.graphics.Color(accentColor.hex),
                activeTrackColor = androidx.compose.ui.graphics.Color(accentColor.hex)
            )
        )
        Text(
            "Mfano wa maandishi kwa ukubwa huu.",
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = MaterialTheme.typography.bodyMedium.fontSize * sliderPosition),
            color = onBg
        )
    }
}

@Composable
private fun YearlyPdfExportButton(region: RegionSettings, accentColor: LiturgicalColor) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }
    val year = remember { LocalDate.now().year }

    Column(Modifier.fillMaxWidth()) {
        Button(
            onClick = {
                if (isGenerating) return@Button
                isGenerating = true
                scope.launch {
                    val file = withContext(Dispatchers.IO) {
                        YearlyLectionaryPdfGenerator.buildAndGenerate(context, year, region)
                    }
                    isGenerating = false
                    PdfShareUtils.share(context, file, "Shiriki Kalenda ya Masomo $year")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.ui.graphics.Color(accentColor.hex)),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isGenerating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(8.dp))
                Text("Inatengeneza...")
            } else {
                Icon(Icons.Filled.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Pakua Kalenda ya $year")
            }
        }
    }
}

@Composable
private fun ReminderToggleContent(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> onEnabledChange(granted) }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Washa Kikumbusho", color = onBg, style = MaterialTheme.typography.bodyMedium)
        Switch(
            checked = enabled,
            onCheckedChange = { checked ->
                if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPermission) {
                        onEnabledChange(true)
                    } else {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    onEnabledChange(checked)
                }
            }
        )
    }

    if (enabled) {
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Muda: %02d:%02d".format(hour, minute),
                color = onBg,
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(onClick = {
                TimePickerDialog(
                    context,
                    { _, h, m -> onTimeChange(h, m) },
                    hour,
                    minute,
                    true
                ).show()
            }) {
                Text("Badilisha Muda")
            }
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
