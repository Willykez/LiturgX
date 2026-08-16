package com.willykez.liturgx.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.ui.theme.seasonAccent
import com.willykez.liturgx.ui.theme.seasonAccentSoft

enum class ReadingKind(val label: String, val icon: ImageVector) {
    SOMO_LA_KWANZA("Somo la Kwanza", Icons.Filled.MenuBook),
    WIMBO("Wimbo wa Katikati", Icons.Filled.MusicNote),
    SOMO_LA_PILI("Somo la Pili", Icons.Filled.ImportContacts),
    SHANGILIO("Shangilio", Icons.Filled.AutoAwesome),
    INJILI("Injili", Icons.Filled.AutoStories)
}

/**
 * A single reading citation, laid out like an entry in a printed missal. The dataset
 * stores citations (book/chapter/verse), not the passage text itself, so this is a
 * clean reference card rather than a text-expander — tapping copies the citation so
 * it's one paste away from a Bible app or website.
 */
@Composable
fun ReadingBlock(
    kind: ReadingKind,
    citation: String,
    color: LiturgicalColor,
    label: String? = null
) {
    val accent = seasonAccent(color)
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(seasonAccentSoft(color))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable {
                copyToClipboard(context, citation)
                copied = true
            }
            .padding(16.dp)
    ) {
        Icon(kind.icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label ?: kind.label, style = MaterialTheme.typography.titleSmall, color = accent)
            Text(citation, style = MaterialTheme.typography.bodyLarge, color = onBg)
        }
        Icon(
            if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
            contentDescription = "Nakili rejea",
            tint = if (copied) accent else onBgDim,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Rejea ya Biblia", text))
}
