package com.willykez.liturgx.ui.saints

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.Saint
import com.willykez.liturgx.ui.components.LiturgicalSeal
import com.willykez.liturgx.ui.theme.seasonAccentSoft

/**
 * [pendingSaintId] arrives from tapping today's saint chip on Home (see
 * [com.willykez.liturgx.ui.LectionaryViewModel.requestSaintJump]) -- when set, this screen clears
 * any active search filter (so the target saint is guaranteed visible), auto-expands that
 * saint's bio, and scrolls to it, then calls [onSaintHandled] so re-entering this tab later
 * doesn't jump again unprompted.
 */
@Composable
fun SaintsScreen(
    saints: List<Saint>,
    pendingSaintId: Int? = null,
    onSaintHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, saints) {
        if (query.isBlank()) saints else saints.filter {
            it.jina.contains(query, ignoreCase = true) || it.tarehe.contains(query, ignoreCase = true)
        }
    }
    val listState = rememberLazyListState()
    // Which saints are expanded -- hoisted up here (rather than local state inside each row)
    // so a jump from Home can force one open from outside, alongside the normal tap-to-expand.
    var expandedIds by remember { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(pendingSaintId) {
        val id = pendingSaintId ?: return@LaunchedEffect
        query = ""
        expandedIds = expandedIds + id
        val index = saints.indexOfFirst { it.id == id }
        if (index >= 0) listState.animateScrollToItem(index)
        onSaintHandled()
    }

    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier.fillMaxSize().padding(20.dp)) {
        Text("Kalenda ya Watakatifu", style = MaterialTheme.typography.headlineSmall, color = onBg)
        Text(
            "Orodha teule ya sikukuu na kumbukumbu",
            style = MaterialTheme.typography.labelMedium,
            color = onBgDim
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = { Text("Tafuta mtakatifu au tarehe...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        LazyColumn(state = listState, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.id }) { saint ->
                SaintRow(
                    saint = saint,
                    expanded = saint.id in expandedIds,
                    onExpandedChange = { open ->
                        expandedIds = if (open) expandedIds + saint.id else expandedIds - saint.id
                    }
                )
            }
        }
    }
}

@Composable
private fun SaintRow(saint: Saint, expanded: Boolean, onExpandedChange: (Boolean) -> Unit) {
    val color = LiturgicalColor.fromSwahili(saint.rangi)
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val hasBio = !saint.wasifu.isNullOrBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(seasonAccentSoft(color))
            .clickable(enabled = hasBio) { onExpandedChange(!expanded) }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LiturgicalSeal(color, size = 34.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(saint.jina, style = MaterialTheme.typography.titleMedium, color = onBg)
                Text("${saint.tarehe} · ${saint.daraja}", style = MaterialTheme.typography.labelMedium, color = onBgDim)
            }
            if (hasBio) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Funga wasifu" else "Soma wasifu",
                    tint = onBgDim
                )
            }
        }
        AnimatedVisibility(visible = expanded && hasBio) {
            Column {
                Spacer(Modifier.height(10.dp))
                Text(
                    saint.wasifu.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = onBg
                )
            }
        }
    }
}
