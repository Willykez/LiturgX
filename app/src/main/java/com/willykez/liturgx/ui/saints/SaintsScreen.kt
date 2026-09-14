package com.willykez.liturgx.ui.saints

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.Saint
import com.willykez.liturgx.data.RecentSearchesStore
import com.willykez.liturgx.ui.components.OneUiGroupCard
import com.willykez.liturgx.ui.components.OneUiIconRow
import com.willykez.liturgx.ui.components.OneUiRowDivider
import com.willykez.liturgx.ui.components.OneUiSwipeToDismissRow
import com.willykez.liturgx.ui.theme.seasonAccent

/**
 * [pendingSaintId] arrives from tapping today's saint chip on Home (see
 * [com.willykez.liturgx.ui.LectionaryViewModel.requestSaintJump]) -- when set, this screen clears
 * any active search filter (so the target saint is guaranteed visible), auto-expands that
 * saint's bio, and scrolls to it, then calls [onSaintHandled] so re-entering this tab later
 * doesn't jump again unprompted.
 *
 * One UI-style grouped list (see [com.willykez.liturgx.ui.components.OneUiGroupCard]): each
 * saint's own liturgical color still shows through, just as the row's icon-circle color rather
 * than the whole row's background -- the same treatment Home gives its "Sikukuu Zinazokuja" list.
 * The search bar matches the Bible tab's (rounded, accent-colored border), and recent searches
 * -- kept in their own namespace, separate from Bible search's -- can be swiped away one at a
 * time, same as there.
 */
@Composable
fun SaintsScreen(
    saints: List<Saint>,
    color: LiturgicalColor,
    pendingSaintId: Int? = null,
    onSaintHandled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val recentStore = remember { RecentSearchesStore(context, namespace = "saints") }
    val accent = seasonAccent(color)

    var query by remember { mutableStateOf("") }
    var recentSearches by remember { mutableStateOf(recentStore.recentSearches()) }
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

    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val onBg = MaterialTheme.colorScheme.onBackground

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
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (query.isNotBlank()) {
                        recentStore.record(query)
                        recentSearches = recentStore.recentSearches()
                    }
                }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accent,
                cursorColor = accent
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))

        if (query.isBlank() && recentSearches.isNotEmpty()) {
            Text(
                "Utafutaji wa Karibuni",
                style = MaterialTheme.typography.labelMedium,
                color = onBgDim,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            )
            OneUiGroupCard {
                recentSearches.forEachIndexed { index, term ->
                    key(term) {
                        OneUiSwipeToDismissRow(
                            onRemove = {
                                recentStore.remove(term)
                                recentSearches = recentStore.recentSearches()
                            }
                        ) {
                            OneUiIconRow(
                                icon = Icons.Filled.History,
                                iconBackground = onBgDim,
                                title = term,
                                onClick = { query = term },
                            )
                        }
                    }
                    if (index != recentSearches.lastIndex) OneUiRowDivider()
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        LazyColumn(state = listState) {
            item {
                OneUiGroupCard {
                    filtered.forEachIndexed { index, saint ->
                        SaintRow(
                            saint = saint,
                            expanded = saint.id in expandedIds,
                            onExpandedChange = { open ->
                                expandedIds = if (open) expandedIds + saint.id else expandedIds - saint.id
                            }
                        )
                        if (index != filtered.lastIndex) OneUiRowDivider()
                    }
                }
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

    Column {
        OneUiIconRow(
            icon = Icons.Filled.Star,
            iconBackground = Color(color.hex),
            title = saint.jina,
            subtitle = "${saint.tarehe} \u00b7 ${saint.daraja}",
            onClick = if (hasBio) ({ onExpandedChange(!expanded) }) else null,
        ) {
            if (hasBio) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "Funga wasifu" else "Soma wasifu",
                    tint = onBgDim
                )
            }
        }
        AnimatedVisibility(visible = expanded && hasBio) {
            Text(
                saint.wasifu.orEmpty(),
                style = MaterialTheme.typography.bodyMedium,
                color = onBg,
                modifier = Modifier.padding(start = 66.dp, end = 16.dp, bottom = 14.dp)
            )
        }
    }
}
