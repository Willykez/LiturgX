package com.willykez.liturgx.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.ReadingPresenter
import com.willykez.liturgx.data.bible.BiblePassage
import com.willykez.liturgx.data.bible.BibleRepository
import com.willykez.liturgx.ui.theme.seasonAccent
import com.willykez.liturgx.ui.theme.seasonAccentSoft
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ReadingKind(val label: String, val icon: ImageVector) {
    SOMO_LA_KWANZA("Somo la Kwanza", Icons.Filled.MenuBook),
    WIMBO("Wimbo wa Katikati", Icons.Filled.MusicNote),
    SOMO_LA_PILI("Somo la Pili", Icons.Filled.ImportContacts),
    SHANGILIO("Shangilio", Icons.Filled.AutoAwesome),
    INJILI("Injili", Icons.Filled.AutoStories)
}

private enum class ResolveState { IDLE, LOADING, RESOLVED, UNAVAILABLE }

/**
 * A single reading citation, laid out like an entry in a printed missal. Tapping the card
 * reveals the actual Scripture text -- resolved live against the bundled Swahili Bible via
 * [BibleRepository] -- right below the citation, missal-style. A handful of citations can't be
 * resolved (Deuterocanonical First Readings like Baruku or Hekima aren't in this 66-book
 * database, and a few citations carry alternate-reading notes the parser can't split out); for
 * those the card quietly falls back to its original behaviour -- a clean reference you can copy
 * and look up elsewhere, with no expand affordance shown.
 */
@Composable
fun ReadingBlock(
    kind: ReadingKind,
    citation: String,
    color: LiturgicalColor,
    dateText: String,
    seasonLabel: String,
    label: String? = null
) {
    val accent = seasonAccent(color)
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val context = LocalContext.current
    val repository = remember { BibleRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()

    var expanded by remember(citation) { mutableStateOf(false) }
    var copied by remember(citation) { mutableStateOf(false) }
    var resolveState by remember(citation) { mutableStateOf(ResolveState.IDLE) }
    var passage by remember(citation) { mutableStateOf<BiblePassage?>(null) }
    var showShareCard by remember(citation) { mutableStateOf(false) }

    fun resolveIfNeeded() {
        if (resolveState != ResolveState.IDLE) return
        resolveState = ResolveState.LOADING
        scope.launch {
            val result = withContext(Dispatchers.IO) { repository.getPassage(citation) }
            passage = result
            resolveState = if (result != null) ResolveState.RESOLVED else ResolveState.UNAVAILABLE
        }
    }

    val canExpand = resolveState != ResolveState.UNAVAILABLE

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(seasonAccentSoft(color))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .clickable(enabled = canExpand) {
                resolveIfNeeded()
                expanded = !expanded
            }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(kind.icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    (label ?: kind.label).uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Text(citation, style = MaterialTheme.typography.titleMedium, color = onBg)
            }
            if (resolveState == ResolveState.LOADING) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(16.dp),
                    strokeWidth = 2.dp,
                    color = accent
                )
            } else if (canExpand) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Funga andiko" else "Fungua andiko",
                    tint = onBgDim,
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(18.dp)
                )
            }
            IconButton(onClick = {
                val resolvedPassage = passage
                val toCopy = if (expanded && resolvedPassage != null) {
                    "${resolvedPassage.citation}\n\n${resolvedPassage.renderedText()}"
                } else {
                    citation
                }
                copyToClipboard(context, toCopy)
                copied = true
            }, modifier = Modifier.size(28.dp)) {
                Icon(
                    if (copied) Icons.Filled.Check else Icons.Filled.ContentCopy,
                    contentDescription = "Nakili",
                    tint = if (copied) accent else onBgDim,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(onClick = {
                resolveIfNeeded()
                showShareCard = true
            }, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Filled.Image,
                    contentDescription = "Shiriki kama picha",
                    tint = onBgDim,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        AnimatedVisibility(
            visible = expanded && resolveState == ResolveState.RESOLVED,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            val text = passage?.renderedText().orEmpty()
            Column {
                Spacer(Modifier.height(10.dp))
                HairlineDivider(accent.copy(alpha = 0.25f))
                Spacer(Modifier.height(10.dp))
                Text(text, style = MaterialTheme.typography.bodyLarge, color = onBg)
            }
        }
    }

    if (showShareCard) {
        val response = ReadingPresenter.massResponseFor(kind.name)
        ShareCardDialog(
            dateText = dateText,
            seasonText = seasonLabel,
            kindLabel = label ?: kind.label,
            citation = citation,
            passage = passage?.renderedText() ?: citation,
            responseText = response,
            liturgicalColor = color,
            onDismiss = { showShareCard = false }
        )
    }
}

@Composable
private fun HairlineDivider(color: androidx.compose.ui.graphics.Color) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(color)
    )
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Andiko", text))
}
