package com.willykez.liturgx.ui.settings

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.willykez.liturgx.core.EpiphanyMode
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.data.bible.BibleUserDataStore
import com.willykez.liturgx.data.bible.ReadingPrefsStore
import com.willykez.liturgx.data.bible.ScriptureFontStyle
import com.willykez.liturgx.data.sharing.PdfShareUtils
import com.willykez.liturgx.data.sharing.YearlyLectionaryPdfGenerator
import com.willykez.liturgx.ui.components.DividedRow
import com.willykez.liturgx.ui.components.SectionLabel
import com.willykez.liturgx.ui.components.SettingsRow
import com.willykez.liturgx.ui.theme.TextScale
import com.willykez.liturgx.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/**
 * Settings content for the app-wide [androidx.compose.material3.ModalBottomSheet] (see
 * [com.willykez.liturgx.ui.LiturgXApp]).
 *
 * Layout language borrowed from BibliaApp: a plain hairline-divided list under uppercase
 * section labels, instead of the previous stack of rounded, tinted "cards". Every setting,
 * callback and piece of copy below is unchanged from before - only the arrangement is new.
 * The liturgical accent colour of the day still drives every selection highlight, which is
 * what keeps this screen feeling like LiturgX rather than a Biblia reskin.
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
    val accent = Color(currentColor.hex)
    val context = LocalContext.current

    fun showTimePicker(hour: Int, minute: Int, onPicked: (Int, Int) -> Unit) {
        TimePickerDialog(context, { _, h, m -> onPicked(h, m) }, hour, minute, true).show()
    }

    // Bible reading preferences and saved-data counts (bookmarks/highlights/notes) - these
    // live in their own small stores (see ReadingPrefsStore, BibleUserDataStore) rather than
    // the LectionaryViewModel above, since they're specific to the Bible tab. Local state here
    // just mirrors what's on disk so the sheet reflects changes immediately.
    val readingPrefs = remember { ReadingPrefsStore(context) }
    val bibleUserData = remember { BibleUserDataStore(context) }
    var fontStyle by remember { mutableStateOf(readingPrefs.loadFontStyle()) }
    var verseNumbersVisible by remember { mutableStateOf(readingPrefs.loadVerseNumbersVisible()) }
    var paragraphMode by remember { mutableStateOf(readingPrefs.loadParagraphMode()) }
    var dataVersion by remember { mutableStateOf(0) }
    var clearBookmarksExpanded by remember { mutableStateOf(false) }
    var clearHighlightsExpanded by remember { mutableStateOf(false) }
    var clearNotesExpanded by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth().fillMaxHeight(0.92f)) {
        // Header: plain title + subtitle, hairline rule beneath - same anatomy as every
        // other top-of-screen header in the app now, not a one-off bespoke row.
        Row(
            Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.padding(vertical = 8.dp)) {
                Text("Mipangilio", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
                Text(
                    "Mwonekano wa programu na mila za jimbo lako.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Funga")
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            // --- USOMAJI (Bible reading preferences) --------------------------------
            SectionLabel("USOMAJI")
            DividedRow {
                SettingsRow(
                    title = "Namba za mstari",
                    subtitle = "Onyesha namba ya kila mstari katika Biblia",
                ) {
                    Switch(
                        checked = verseNumbersVisible,
                        onCheckedChange = {
                            verseNumbersVisible = it
                            readingPrefs.saveVerseNumbersVisible(it)
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = accent),
                    )
                }
            }
            DividedRow {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("Aina ya maandishi", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        listOf(
                            ScriptureFontStyle.SERIF to "Klasiki",
                            ScriptureFontStyle.SANS to "Rahisi",
                            ScriptureFontStyle.MONO to "Namba",
                        ).forEach { (style, label) ->
                            val selected = style == fontStyle
                            Text(
                                label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.clickable {
                                    fontStyle = style
                                    readingPrefs.saveFontStyle(style)
                                },
                            )
                        }
                    }
                }
            }
            DividedRow(showDivider = false) {
                SettingsRow(
                    title = "Hali ya kusoma: Aya",
                    subtitle = "Onyesha kama kitabu, si mstari kwa mstari",
                ) {
                    Switch(
                        checked = paragraphMode,
                        onCheckedChange = {
                            paragraphMode = it
                            readingPrefs.saveParagraphMode(it)
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = accent),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            // --- UKUMBUSHO ---------------------------------------------------------
            SectionLabel("UKUMBUSHO")
            DividedRow {
                SettingsRow(
                    title = "Kikumbusho cha Kila Siku",
                    subtitle = "Masomo ya Kiliturujia ya siku, kwa wakati unaochagua",
                ) {
                    ReminderSwitch(checked = reminderEnabled, onCheckedChange = onReminderEnabledChange, accent = accent)
                }
            }
            if (reminderEnabled) {
                DividedRow {
                    SettingsRow(
                        title = "Wakati",
                        subtitle = "%02d:%02d".format(reminderHour, reminderMinute),
                        onClick = { showTimePicker(reminderHour, reminderMinute, onReminderTimeChange) },
                    )
                }
            }
            DividedRow {
                SettingsRow(
                    title = "Neno la Kila Siku",
                    subtitle = "Andiko fupi la kutafakari, huru dhidi ya masomo ya siku",
                ) {
                    ReminderSwitch(checked = verseReminderEnabled, onCheckedChange = onVerseReminderEnabledChange, accent = accent)
                }
            }
            if (verseReminderEnabled) {
                DividedRow(showDivider = false) {
                    SettingsRow(
                        title = "Wakati",
                        subtitle = "%02d:%02d".format(verseReminderHour, verseReminderMinute),
                        onClick = { showTimePicker(verseReminderHour, verseReminderMinute, onVerseReminderTimeChange) },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            SectionLabel("MWONEKANO")
            DividedRow {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val selected = mode == themeMode
                        Text(
                            mode.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (selected) accent else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.clickable { onThemeModeChange(mode) },
                        )
                    }
                }
            }
            DividedRow(showDivider = false) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text("Ukubwa wa Maandishi", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    Spacer(Modifier.height(4.dp))
                    TextScaleSlider(textScale = textScale, onTextScaleChange = onTextScaleChange, accent = accent)
                }
            }

            // --- JIMBO (region-specific liturgical rules) --------------------------
            Spacer(Modifier.height(12.dp))
            SectionLabel("JIMBO LAKO")
            DividedRow {
                SettingsRow(
                    title = "Epifania Ihamishiwe Dominika",
                    subtitle = "Baadhi ya majimbo huadhimisha Jan 6 daima; mengine Dominika ya Jan 2-8",
                ) {
                    Switch(
                        checked = region.epiphanyMode == EpiphanyMode.TRANSFERRED,
                        onCheckedChange = { checked ->
                            onRegionChange(region.copy(epiphanyMode = if (checked) EpiphanyMode.TRANSFERRED else EpiphanyMode.FIXED_JAN6))
                        },
                        colors = SwitchDefaults.colors(checkedTrackColor = accent),
                    )
                }
            }
            DividedRow(showDivider = false) {
                SettingsRow(
                    title = "Shika Alhamisi",
                    subtitle = "Kupaa kwa Bwana na Fungu Takatifu vishikwe Alhamisi, si Dominika",
                ) {
                    Switch(
                        checked = region.keepThursdaySolemnities,
                        onCheckedChange = { onRegionChange(region.copy(keepThursdaySolemnities = it)) },
                        colors = SwitchDefaults.colors(checkedTrackColor = accent),
                    )
                }
            }

            // --- KALENDA YA MWAKA ---------------------------------------------------
            Spacer(Modifier.height(12.dp))
            SectionLabel("KALENDA YA MWAKA")
            DividedRow(showDivider = false) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
                    Text(
                        "Pakua orodha ya masomo ya Dominika zote na Sikukuu Maalum za mwaka mzima, tayari kuchapishwa.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(10.dp))
                    YearlyPdfExportButton(region = region, accent = accent)
                }
            }

            // --- DATA YAKO (bookmarks / highlights / notes from the Bible tab) ------
            Spacer(Modifier.height(12.dp))
            SectionLabel("DATA YAKO")
            dataVersion.let {
                ClearableDataRow(
                    title = "Alama",
                    subtitle = "${bibleUserData.bookmarkCount()} mstari umewekwa alama",
                    expanded = clearBookmarksExpanded,
                    onToggleExpanded = { clearBookmarksExpanded = !clearBookmarksExpanded },
                    onConfirmClear = { bibleUserData.clearBookmarks(); clearBookmarksExpanded = false; dataVersion++ },
                )
                ClearableDataRow(
                    title = "Iliyoangaziwa",
                    subtitle = "${bibleUserData.highlightCount()} mstari umeangaziwa",
                    expanded = clearHighlightsExpanded,
                    onToggleExpanded = { clearHighlightsExpanded = !clearHighlightsExpanded },
                    onConfirmClear = { bibleUserData.clearHighlights(); clearHighlightsExpanded = false; dataVersion++ },
                )
                ClearableDataRow(
                    title = "Dokezo",
                    subtitle = "${bibleUserData.noteCount()} dokezo limehifadhiwa",
                    expanded = clearNotesExpanded,
                    onToggleExpanded = { clearNotesExpanded = !clearNotesExpanded },
                    onConfirmClear = { bibleUserData.clearNotes(); clearNotesExpanded = false; dataVersion++ },
                )
            }

            // --- KUHUSU --------------------------------------------------------------
            Spacer(Modifier.height(12.dp))
            SectionLabel("KUHUSU")
            DividedRow(showDivider = false) {
                Column(Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                    Text("LiturgX", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
                    Text(
                        "Masomo ya Kila Siku kwa Kiswahili \u2014 kalenda ya kiliturujia, Biblia na watakatifu",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReminderSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, accent: Color) {
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> onCheckedChange(granted) }

    Switch(
        checked = checked,
        onCheckedChange = { wantsOn ->
            if (wantsOn && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val hasPermission = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (hasPermission) onCheckedChange(true) else notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                onCheckedChange(wantsOn)
            }
        },
        colors = SwitchDefaults.colors(checkedTrackColor = accent),
    )
}

@Composable
private fun TextScaleSlider(textScale: Float, onTextScaleChange: (Float) -> Unit, accent: Color) {
    var sliderPosition by remember(textScale) { mutableStateOf(textScale) }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("A", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onBackground)
            Text(
                "${(sliderPosition * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text("A", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onBackground)
        }
        Slider(
            value = sliderPosition,
            valueRange = TextScale.MIN..TextScale.MAX,
            onValueChange = { sliderPosition = it },
            onValueChangeFinished = { onTextScaleChange(sliderPosition) },
            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
        )
    }
}

@Composable
private fun YearlyPdfExportButton(region: RegionSettings, accent: Color) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isGenerating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val year = remember { LocalDate.now().year }

    OutlinedButton(
        onClick = {
            if (isGenerating) return@OutlinedButton
            isGenerating = true
            errorMessage = null
            scope.launch {
                try {
                    val file = withContext(Dispatchers.IO) {
                        YearlyLectionaryPdfGenerator.buildAndGenerate(context, year, region)
                    }
                    isGenerating = false
                    PdfShareUtils.share(context, file, "Shiriki Kalenda ya Masomo $year")
                } catch (e: Exception) {
                    // Never let a bad day's data or a share-sheet hiccup take the whole app
                    // down -- log it (visible in Logcat under this tag if it happens again)
                    // and surface a plain-language message instead of crashing.
                    android.util.Log.e("YearlyPdfExport", "Failed to build/share yearly PDF", e)
                    isGenerating = false
                    errorMessage = "Imeshindikana kutengeneza PDF. Jaribu tena."
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        if (isGenerating) {
            CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp, color = accent)
            Spacer(Modifier.width(8.dp))
            Text("Inatengeneza...")
        } else {
            Icon(Icons.Filled.PictureAsPdf, contentDescription = null, tint = accent, modifier = Modifier.height(18.dp).width(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Pakua Kalenda ya $year")
        }
    }
    errorMessage?.let {
        Spacer(Modifier.height(6.dp))
        Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
    }
}

/** "Futa Alama/Iliyoangaziwa/Dokezo" row - ported from BibliaApp's DATA YAKO pattern: tap the
 *  row to reveal a confirm button, rather than clearing on the first tap. */
@Composable
private fun ClearableDataRow(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onConfirmClear: () -> Unit,
) {
    DividedRow {
        Column {
            SettingsRow(
                title = "Futa $title",
                subtitle = subtitle,
                titleColor = MaterialTheme.colorScheme.error,
                onClick = onToggleExpanded,
            )
            AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    OutlinedButton(onClick = onConfirmClear) {
                        Text("Thibitisha")
                    }
                }
            }
        }
    }
}
