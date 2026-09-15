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
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Samsung One UI / Google Settings-style grouped list.
 *
 * Rounded card groups, colored icon circles per row, MD3 segmented buttons
 * for multi-choice pickers, and inset dividers.
 *
 * Originally built for Settings, this is now the shared list language for
 * the whole app, including Home's upcoming-events sections.
 */

/**
 * Small uppercase label above a group card.
 *
 * Examples:
 * - "USOMAJI"
 * - "SIKUKUU ZINAZOKUJA"
 */
@Composable
fun OneUiSectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(
            start = 8.dp,
            bottom = 6.dp,
        ),
    )
}

/**
 * Rounded card container for one section's rows.
 */
@Composable
fun OneUiGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.45f,
                ),
            ),
    ) {
        content()
    }
}

/**
 * Hairline divider between rows in the same card.
 *
 * The divider is inset so it aligns with the row text instead of
 * visually cutting through the icon column.
 */
@Composable
fun OneUiRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        color = MaterialTheme.colorScheme.outline.copy(
            alpha = 0.4f,
        ),
        thickness = 1.dp,
    )
}

/**
 * Standard One UI-style row.
 *
 * Contains:
 * - Colored circular icon
 * - Title
 * - Optional subtitle
 * - Optional trailing content
 * - Optional click action
 */
@Composable
fun OneUiIconRow(
    icon: ImageVector,
    iconBackground: Color,
    title: String,
    subtitle: String? = null,
    subtitleMaxLines: Int = Int.MAX_VALUE,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .let { modifier ->
                if (onClick != null) {
                    modifier.clickable(onClick = onClick)
                } else {
                    modifier
                }
            }
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(
            modifier = Modifier.width(14.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = subtitleMaxLines,
                    overflow = if (subtitleMaxLines < Int.MAX_VALUE) {
                        TextOverflow.Ellipsis
                    } else {
                        TextOverflow.Clip
                    },
                )
            }
        }

        if (trailing != null) {
            Spacer(
                modifier = Modifier.width(12.dp),
            )

            trailing()
        }
    }
}

/**
 * One UI-style settings row with a switch on the trailing edge.
 */
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
    OneUiIconRow(
        icon = icon,
        iconBackground = iconBackground,
        title = title,
        subtitle = subtitle,
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = accent,
            ),
        )
    }
}

/**
 * MD3 segmented button row for a small fixed set of mutually-exclusive
 * options.
 *
 * Common uses:
 * - Theme mode
 * - Font style
 * - PDF export mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> OneUiSegmentedRow(
    options: List<Pair<T, String>>,
    selected: T,
    accent: Color,
    onSelect: (T) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        options.forEachIndexed { index, (value, label) ->
            SegmentedButton(
                selected = value == selected,
                onClick = {
                    onSelect(value)
                },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size,
                ),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = accent,
                    activeContentColor = Color.White,
                    activeBorderColor = accent,
                ),
                label = {
                    Text(
                        text = label,
                        maxLines = 1,
                    )
                },
            )
        }
    }
}

/**
 * Wraps a row so swiping it in either direction removes it.
 *
 * Used for recent-search history entries on the Bible and Saints
 * search screens.
 *
 * The row translates and fades while being swiped. No delete/reveal
 * background is displayed.
 *
 * [onRemove] is called once the swipe passes the dismiss threshold.
 * The caller is responsible for removing the underlying item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneUiSwipeToDismissRow(
    onRemove: () -> Unit,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (
                value == SwipeToDismissBoxValue.StartToEnd ||
                value == SwipeToDismissBoxValue.EndToStart
            ) {
                onRemove()
            }

            /*
             * Always reject the dismissed state itself.
             *
             * The underlying list is expected to remove the item through
             * onRemove(), so the SwipeToDismissBox does not need to remain
             * in a dismissed state.
             */
            false
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {},
        modifier = Modifier.graphicsLayer {
            alpha = 1f - dismissState.progress.coerceIn(0f, 1f)
        },
        content = {
            content()
        },
    )
}

