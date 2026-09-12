package com.willykez.liturgx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Samsung One UI / Google Settings-style grouped list -- rounded card groups, colored icon
 * circles per row, MD3 segmented buttons for multi-choice pickers, per direct reference
 * screenshots. Originally built for Settings, now the shared list language for the whole app
 * (Home's upcoming-events sections, and onward) rather than a settings-only pattern, hence the
 * generic name -- one [OneUiGroupCard] per section, rows separated by [OneUiRowDivider] inset
 * to align with the text rather than running edge-to-edge under the icon.
 */

/** Small uppercase label above a group card -- e.g. "USOMAJI", "SIKUKUU ZINAZOKUJA". */
@Composable
fun OneUiSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 8.dp, bottom = 6.dp),
    )
}

/** The rounded card container for one section's rows. */
@Composable
fun OneUiGroupCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
    ) {
        content()
    }
}

/** A hairline between two rows in the same card, inset to align with the row's text (not the
 *  icon), so it doesn't visually cut through the icon column. */
@Composable
fun OneUiRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        thickness = 1.dp,
    )
}

/** One row: a colored icon circle, title/subtitle, and optional trailing content (switch,
 *  value text, chevron) -- the One UI "colored icon circle" list-item pattern. */
@Composable
fun OneUiIconRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        }
    }
}

/** A row whose only content is a switch on the trailing edge -- the most common settings-row shape. */
@Composable
fun OneUiSwitchRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit,
) {
    OneUiIconRow(icon = icon, iconBackground = iconBackground, title = title, subtitle = subtitle) {
        Switch(checked = checked, onCheckedChange = onCheckedChange, colors = SwitchDefaults.colors(checkedTrackColor = accent))
    }
}

/** MD3 segmented button row (see the "Segmented Buttons" reference) for a small fixed set of
 *  mutually-exclusive options -- used for theme mode, font style, and PDF export mode, in place
 *  of the plain tap-a-text-label pickers used before. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OneUiSegmentedRow(
    options: List<Pair<T, String>>,
    selected: T,
    accent: Color,
    onSelect: (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = { onSelect(value) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = accent,
                    activeContentColor = Color.White,
                    activeBorderColor = accent,
                ),
                label = { Text(label, maxLines = 1) },
            )
        }
    }
}
