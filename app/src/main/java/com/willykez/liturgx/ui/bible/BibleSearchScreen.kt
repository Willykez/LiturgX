package com.willykez.liturgx.ui.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.RecentSearchesStore
import com.willykez.liturgx.data.bible.BibleBookInfo
import com.willykez.liturgx.data.bible.BibleBrowseRepository
import com.willykez.liturgx.data.bible.SearchMode
import com.willykez.liturgx.data.bible.SearchResult
import com.willykez.liturgx.data.bible.SearchScope
import com.willykez.liturgx.ui.components.OneUiGroupCard
import com.willykez.liturgx.ui.components.OneUiIconRow
import com.willykez.liturgx.ui.components.OneUiRowDivider
import com.willykez.liturgx.ui.components.OneUiSwipeToDismissRow
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** Debounce so a fast typist doesn't trigger a SQL query on every single keystroke. */
private const val SEARCH_DEBOUNCE_MS = 300L
private const val MIN_QUERY_LENGTH = 2

@Composable
fun BibleSearchScreen(
    color: LiturgicalColor,
    repository: BibleBrowseRepository,
    books: List<BibleBookInfo>,
    onBack: () -> Unit,
    onSelectResult: (SearchResult) -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = seasonAccent(color)
    val context = LocalContext.current
    val recentStore = remember { RecentSearchesStore(context) }

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(SearchMode.PHRASE) }
    var scope by remember { mutableStateOf<SearchScope>(SearchScope.WholeBible) }
    var showBookPicker by remember { mutableStateOf(false) }
    var recentSearches by remember { mutableStateOf(recentStore.recentSearches()) }
    var referenceHit by remember { mutableStateOf<SearchResult?>(null) }
    val focusRequester = remember { FocusRequester() }

    // Resolved on every keystroke, not debounced -- it's a cheap in-memory alias lookup plus
    // one indexed row fetch, nothing like the LIKE-scan the free-text search below needs
    // debouncing for.
    LaunchedEffect(query) {
        referenceHit = withContext(Dispatchers.IO) { repository.resolveReference(query) }
    }

    LaunchedEffect(query, mode, scope) {
        val term = query.trim()
        if (term.length < MIN_QUERY_LENGTH) {
            results = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(SEARCH_DEBOUNCE_MS)
        val found = withContext(Dispatchers.IO) { repository.search(term, mode, scope) }
        results = found
        isSearching = false
        if (found.isNotEmpty()) {
            recentStore.record(term)
            recentSearches = recentStore.recentSearches()
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Rudi", tint = onBg)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
                placeholder = { Text("Tafuta neno katika Biblia...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    cursorColor = accent
                )
            )
        }

        Spacer(Modifier.height(10.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ScopeChip("Biblia Yote", scope is SearchScope.WholeBible, accent) { scope = SearchScope.WholeBible }
            }
            item {
                ScopeChip("Agano la Kale", scope is SearchScope.OldTestament, accent) { scope = SearchScope.OldTestament }
            }
            item {
                ScopeChip("Agano Jipya", scope is SearchScope.NewTestament, accent) { scope = SearchScope.NewTestament }
            }
            item {
                val bookScope = scope as? SearchScope.Book
                ScopeChip(bookScope?.bookName ?: "Kitabu...", bookScope != null, accent) { showBookPicker = true }
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ScopeChip("Kifungu", mode == SearchMode.PHRASE, accent) { mode = SearchMode.PHRASE }
            }
            item {
                ScopeChip("Neno Lolote", mode == SearchMode.ANY_WORD, accent) { mode = SearchMode.ANY_WORD }
            }
        }

        Spacer(Modifier.height(12.dp))

        referenceHit?.let { hit ->
            Box(Modifier.padding(horizontal = 16.dp)) {
                ReferenceJumpRow(hit, accent, onBgDim) {
                    recentStore.record(query.trim())
                    onSelectResult(hit)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        when {
            query.isBlank() && recentSearches.isNotEmpty() -> {
                Column(Modifier.padding(horizontal = 16.dp)) {
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
                }
            }
            isSearching -> {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accent)
                }
            }
            query.trim().length >= MIN_QUERY_LENGTH && results.isEmpty() -> {
                Text(
                    "Hakuna matokeo kwa \"${query.trim()}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onBgDim,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    item {
                        OneUiGroupCard {
                            results.forEachIndexed { index, result ->
                                SearchResultRow(
                                    result = result,
                                    highlightTerms = if (mode == SearchMode.ANY_WORD) {
                                        query.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
                                    } else {
                                        listOf(query.trim())
                                    },
                                    color = color,
                                    onClick = { onSelectResult(result) }
                                )
                                if (index != results.lastIndex) OneUiRowDivider()
                            }
                        }
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }

    if (showBookPicker) {
        BookPickerDialog(
            books = books,
            onSelect = { book ->
                scope = SearchScope.Book(book.id, book.name)
                showBookPicker = false
            },
            onDismiss = { showBookPicker = false }
        )
    }
}

@Composable
private fun ReferenceJumpRow(
    hit: SearchResult,
    accent: Color,
    onBgDim: Color,
    onClick: () -> Unit
) {
    OneUiGroupCard {
        OneUiIconRow(
            icon = Icons.Filled.SubdirectoryArrowRight,
            iconBackground = accent,
            title = "Nenda kwa ${hit.bookName} ${hit.chapterNum}:${hit.verseNum}",
            subtitle = hit.text,
            subtitleMaxLines = 1,
            onClick = onClick,
        ) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = onBgDim)
        }
    }
}

@Composable
private fun ScopeChip(label: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = accent.copy(alpha = 0.22f),
            selectedLabelColor = accent
        )
    )
}

@Composable
private fun BookPickerDialog(
    books: List<BibleBookInfo>,
    onSelect: (BibleBookInfo) -> Unit,
    onDismiss: () -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val accent = MaterialTheme.colorScheme.primary
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 12.dp)
        ) {
            Text(
                "Chagua Kitabu",
                style = MaterialTheme.typography.titleMedium,
                color = onBg,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                item {
                    OneUiGroupCard {
                        books.forEachIndexed { index, book ->
                            OneUiIconRow(
                                icon = Icons.Filled.MenuBook,
                                iconBackground = accent,
                                title = book.name,
                                onClick = { onSelect(book) },
                            )
                            if (index != books.lastIndex) OneUiRowDivider()
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun SearchResultRow(
    result: SearchResult,
    highlightTerms: List<String>,
    color: LiturgicalColor,
    onClick: () -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val accent = seasonAccent(color)

    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(accent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(
                "${result.bookName} ${result.chapterNum}:${result.verseNum}",
                style = MaterialTheme.typography.titleSmall,
                color = accent
            )
        }
        Text(
            highlightMatches(result.text, highlightTerms, accent),
            style = MaterialTheme.typography.bodyMedium,
            color = onBg,
            modifier = Modifier.padding(start = 50.dp, top = 2.dp)
        )
    }
}

/** Highlights every non-overlapping occurrence of any of [terms] in [text], left to right --
 *  used as-is for phrase mode (one term) and for any-word mode (several terms, each highlighted
 *  wherever it appears). */
private fun highlightMatches(text: String, terms: List<String>, accent: Color) = buildAnnotatedString {
    val validTerms = terms.filter { it.isNotBlank() }
    if (validTerms.isEmpty()) {
        append(text)
        return@buildAnnotatedString
    }

    var cursor = 0
    while (cursor < text.length) {
        var bestIndex = -1
        var bestLength = 0
        for (term in validTerms) {
            val idx = text.indexOf(term, cursor, ignoreCase = true)
            if (idx in 0 until (if (bestIndex == -1) Int.MAX_VALUE else bestIndex)) {
                bestIndex = idx
                bestLength = term.length
            } else if (idx == bestIndex && term.length > bestLength) {
                bestLength = term.length
            }
        }
        if (bestIndex == -1) {
            append(text.substring(cursor))
            break
        }
        append(text.substring(cursor, bestIndex))
        withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold)) {
            append(text.substring(bestIndex, bestIndex + bestLength))
        }
        cursor = bestIndex + bestLength
    }
}
