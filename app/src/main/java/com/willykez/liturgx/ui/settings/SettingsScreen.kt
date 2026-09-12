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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import com.willykez.liturgx.ui.components.OneUiGroupCard
import com.willykez.liturgx.ui.components.OneUiIconRow
import com.willykez.liturgx.ui.components.OneUiRowDivider
import com.willykez.liturgx.ui.components.OneUiSectionLabel
import com.willykez.liturgx.ui.components.OneUiSegmentedRow
import com.willykez.liturgx.ui.components.OneUiSwitchRow
import com.willykez.liturgx.ui.theme.TextScale
import com.willykez.liturgx.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

/** One fixed color per row/section, One UI-style -- helps the eye tell rows apart at a glance
 *  rather than everything sharing one flat accent, while the liturgical accent colour is still
 *  what drives every selection state (segmented buttons, switches). */
private val BLUE = Color(0xFF4285F4)
private val PURPLE = Color(0xFF8E24AA)
private val TEAL = Color(0xFF00897B)
private val ORANGE = Color(0xFFFB8C00)
private val RED = Color(0xFFE53935)
private val INDIGO = Color(0xFF3949AB)
private val PINK = Color(0xFFD81B60)
private val GREEN = Color(0xFF43A047)
private val BROWN = Color(0xFF6D4C41)
private val GRAY = Color(0xFF757575)

/**
 * Settings content for the app-wide [androidx.compose.material3.ModalBottomSheet] (see
 * [com.willykez.liturgx.ui.LiturgXApp]).
 *
 * Layout language: Samsung One UI / Google Settings-style grouped cards -- a rounded card per
 * section, a colored icon circle per row, MD3 segmented buttons for the multi-choice pickers
 * (theme mode, font style, PDF export mode) -- replacing the earlier flat hairline-list design.
 * Every setting, callback and piece of copy is unchanged from before; only the visual language
 * is new. The liturgical accent colour of the day still drives every selection state, which is
 * what keeps this screen feeling like LiturgX rather than a straight OneUI reskin.
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

        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // --- USOMAJI (Bible reading preferences) --------------------------------
            Column {
                OneUiSectionLabel("USOMAJI")
                OneUiGroupCard {
                    OneUiSwitchRow(
                        icon = Icons.Filled.FormatListNumbered,
                        iconBackground = BLUE,
                        title = "Namba za mstari",
                        subtitle = "Onyesha namba ya kila mstari katika Biblia",
                        checked = verseNumbersVisible,
                        accent = accent,
                        onCheckedChange = {
                            verseNumbersVisible = it
                            readingPrefs.saveVerseNumbersVisible(it)
                        },
                    )
                    OneUiRowDivider()
                    OneUiIconRow(icon = Icons.Filled.TextFields, iconBackground = PURPLE, title = "Aina ya maandishi")
                    Column(Modifier.padding(start = 66.dp, end = 16.dp, bottom = 14.dp)) {
                        OneUiSegmentedRow(
                            options = listOf(
                                ScriptureFontStyle.SERIF to "Klasiki",
                                ScriptureFontStyle.SANS to "Rahisi",
                                ScriptureFontStyle.MONO to "Namba",
                            ),
                            selected = fontStyle,
                            accent = accent,
                            onSelect = { style ->
                                fontStyle = style
                                readingPrefs.saveFontStyle(style)
                            },
                        )
                    }
                    OneUiRowDivider()
                    OneUiSwitchRow(
                        icon = Icons.Filled.Notes,
                        iconBackground = TEAL,
                        title = "Hali ya kusoma: Aya",
                        subtitle = "Onyesha kama kitabu, si mstari kwa mstari",
                        checked = paragraphMode,
                        accent = accent,
                        onCheckedChange = {
                            paragraphMode = it
                            readingPrefs.saveParagraphMode(it)
                        },
                    )
                }
            }

            // --- UKUMBUSHO ---------------------------------------------------------
            Column {
                OneUiSectionLabel("UKUMBUSHO")
                OneUiGroupCard {
                    OneUiIconRow(
                        icon = Icons.Filled.NotificationsActive,
                        iconBackground = ORANGE,
                        title = "Kikumbusho cha Kila Siku",
                        subtitle = "Masomo ya Kiliturujia ya siku, kwa wakati unaochagua",
                    ) {
                        ReminderSwitch(checked = reminderEnabled, onCheckedChange = onReminderEnabledChange, accent = accent)
                    }
                    if (reminderEnabled) {
                        OneUiRowDivider()
                        OneUiIconRow(
                            icon = Icons.Filled.NotificationsActive,
                            iconBackground = ORANGE,
                            title = "Wakati",
                            subtitle = "%02d:%02d".format(reminderHour, reminderMinute),
                            onClick = { showTimePicker(reminderHour, reminderMinute, onReminderTimeChange) },
                        )
                    }
                    OneUiRowDivider()
                    OneUiIconRow(
                        icon = Icons.Filled.FormatQuote,
                        iconBackground = RED,
                        title = "Neno la Kila Siku",
                        subtitle = "Andiko fupi la kutafakari, huru dhidi ya masomo ya siku",
                    ) {
                        ReminderSwitch(checked = verseReminderEnabled, onCheckedChange = onVerseReminderEnabledChange, accent = accent)
                    }
                    if (verseReminderEnabled) {
                        OneUiRowDivider()
                        OneUiIconRow(
                            icon = Icons.Filled.FormatQuote,
                            iconBackground = RED,
                            title = "Wakati",
                            subtitle = "%02d:%02d".format(verseReminderHour, verseReminderMinute),
                            onClick = { showTimePicker(verseReminderHour, verseReminderMinute, onVerseReminderTimeChange) },
                        )
                    }
                }
            }

            // --- MWONEKANO -----------------------------------------------------------
            Column {
                OneUiSectionLabel("MWONEKANO")
                OneUiGroupCard {
                    OneUiIconRow(icon = Icons.Filled.Palette, iconBackground = INDIGO, title = "Mwonekano")
                    Column(Modifier.padding(start = 66.dp, end = 16.dp, bottom = 14.dp)) {
                        OneUiSegmentedRow(
                            options = ThemeMode.entries.map { it to it.label },
                            selected = themeMode,
                            accent = accent,
                            onSelect = onThemeModeChange,
                        )
                    }
                    OneUiRowDivider()
                    OneUiIconRow(icon = Icons.Filled.TextIncrease, iconBackground = PINK, title = "Ukubwa wa Maandishi")
                    Column(Modifier.padding(start = 66.dp, end = 16.dp, bottom = 10.dp)) {
                        TextScaleSlider(textScale = textScale, onTextScaleChange = onTextScaleChange, accent = accent)
                    }
                }
            }

            // --- JIMBO (region-specific liturgical rules) --------------------------
            Column {
                OneUiSectionLabel("JIMBO LAKO")
                OneUiGroupCard {
                    OneUiSwitchRow(
                        icon = Icons.Filled.Star,
                        iconBackground = GREEN,
                        title = "Epifania Ihamishiwe Dominika",
                        subtitle = "Baadhi ya majimbo huadhimisha Jan 6 daima; mengine Dominika ya Jan 2-8",
                        checked = region.epiphanyMode == EpiphanyMode.TRANSFERRED,
                        accent = accent,
                        onCheckedChange = { checked ->
                            onRegionChange(region.copy(epiphanyMode = if (checked) EpiphanyMode.TRANSFERRED else EpiphanyMode.FIXED_JAN6))
                        },
                    )
                    OneUiRowDivider()
                    OneUiSwitchRow(
                        icon = Icons.Filled.Upload,
                        iconBackground = GREEN,
                        title = "Shika Alhamisi",
                        subtitle = "Kupaa kwa Bwana na Fungu Takatifu vishikwe Alhamisi, si Dominika",
                        checked = region.keepThursdaySolemnities,
                        accent = accent,
                        onCheckedChange = { onRegionChange(region.copy(keepThursdaySolemnities = it)) },
                    )
                }
            }

            // --- KALENDA YA MWAKA ---------------------------------------------------
            Column {
                OneUiSectionLabel("KALENDA YA MWAKA")
                OneUiGroupCard {
                    OneUiIconRow(
                        icon = Icons.Filled.CalendarMonth,
                        iconBackground = BROWN,
                        title = "Pakua Kalenda ya Mwaka",
                        subtitle = "Dominika zote na Sikukuu Maalum za mwaka mzima, tayari kuchapishwa",
                    )
                    Column(Modifier.padding(start = 66.dp, end = 16.dp, bottom = 14.dp)) {
                        YearlyPdfExportButton(region = region, accent = accent)
                    }
                }
            }

            // --- DATA YAKO (bookmarks / highlights / notes from the Bible tab) ------
            Column {
                OneUiSectionLabel("DATA YAKO")
                OneUiGroupCard {
                    dataVersion.let {
                        ClearableDataRow(
                            icon = Icons.Filled.Bookmark,
                            title = "Alama",
                            subtitle = "${bibleUserData.bookmarkCount()} mstari umewekwa alama",
                            expanded = clearBookmarksExpanded,
                            onToggleExpanded = { clearBookmarksExpanded = !clearBookmarksExpanded },
                            onConfirmClear = { bibleUserData.clearBookmarks(); clearBookmarksExpanded = false; dataVersion++ },
                        )
                        OneUiRowDivider()
                        ClearableDataRow(
                            icon = Icons.Filled.Highlight,
                            title = "Iliyoangaziwa",
                            subtitle = "${bibleUserData.highlightCount()} mstari umeangaziwa",
                            expanded = clearHighlightsExpanded,
                            onToggleExpanded = { clearHighlightsExpanded = !clearHighlightsExpanded },
                            onConfirmClear = { bibleUserData.clearHighlights(); clearHighlightsExpanded = false; dataVersion++ },
                        )
                        OneUiRowDivider()
                        ClearableDataRow(
                            icon = Icons.Filled.NoteAlt,
                            title = "Dokezo",
                            subtitle = "${bibleUserData.noteCount()} dokezo limehifadhiwa",
                            expanded = clearNotesExpanded,
                            onToggleExpanded = { clearNotesExpanded = !clearNotesExpanded },
                            onConfirmClear = { bibleUserData.clearNotes(); clearNotesExpanded = false; dataVersion++ },
                        )
                    }
                }
            }

            // --- KUHUSU --------------------------------------------------------------
            Column {
                OneUiSectionLabel("KUHUSU")
                OneUiGroupCard {
                    OneUiIconRow(
                        icon = Icons.Filled.Info,
                        iconBackground = GRAY,
                        title = "LiturgX",
                        subtitle = "Masomo ya Kila Siku kwa Kiswahili \u2014 kalenda ya kiliturujia, Biblia na watakatifu",
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

    androidx.compose.material3.Switch(
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
        colors = androidx.compose.material3.SwitchDefaults.colors(checkedTrackColor = accent),
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
    var mode by remember { mutableStateOf(YearlyLectionaryPdfGenerator.PdfContentMode.REFERENCES_ONLY) }
    val year = remember { LocalDate.now().year }
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant

    OneUiSegmentedRow(
        options = listOf(
            YearlyLectionaryPdfGenerator.PdfContentMode.REFERENCES_ONLY to "Marejeo Pekee",
            YearlyLectionaryPdfGenerator.PdfContentMode.FULL_TEXT to "Masomo Kamili",
        ),
        selected = mode,
        accent = accent,
        onSelect = { mode = it },
    )
    Spacer(Modifier.height(8.dp))
    Text(
        if (mode == YearlyLectionaryPdfGenerator.PdfContentMode.REFERENCES_ONLY)
            "Tarehe, jina, rangi na marejeo ya masomo -- ukurasa mfupi, rahisi kuchapisha."
        else
            "Masomo kamili ya kila siku, si marejeo tu -- faili kubwa zaidi na huchukua muda mrefu kidogo kutengenezwa.",
        style = MaterialTheme.typography.labelMedium,
        color = onBgDim,
    )
    Spacer(Modifier.height(10.dp))

    OutlinedButton(
        onClick = {
            if (isGenerating) return@OutlinedButton
            isGenerating = true
            errorMessage = null
            scope.launch {
                try {
                    val file = withContext(Dispatchers.IO) {
                        YearlyLectionaryPdfGenerator.buildAndGenerate(context, year, region, mode)
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
            Text(
                if (mode == YearlyLectionaryPdfGenerator.PdfContentMode.REFERENCES_ONLY)
                    "Pakua Kalenda ya $year"
                else
                    "Pakua Masomo Kamili ya $year"
            )
        }
    }
    errorMessage?.let {
        Spacer(Modifier.height(6.dp))
        Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.error)
    }
}

/** "Futa Alama/Iliyoangaziwa/Dokezo" row - tap the row to reveal a confirm button, rather than
 *  clearing on the first tap. */
@Composable
private fun ClearableDataRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onConfirmClear: () -> Unit,
) {
    Column {
        OneUiIconRow(
            icon = icon,
            iconBackground = RED,
            title = "Futa $title",
            subtitle = subtitle,
            onClick = onToggleExpanded,
        )
        AnimatedVisibility(visible = expanded, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 66.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(onClick = onConfirmClear) {
                    Text("Thibitisha")
                }
            }
        }
    }
}
