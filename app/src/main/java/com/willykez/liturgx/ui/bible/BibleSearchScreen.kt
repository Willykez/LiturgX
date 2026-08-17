package com.willykez.liturgx.ui.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.bible.BibleBrowseRepository
import com.willykez.liturgx.data.bible.SearchResult
import com.willykez.liturgx.ui.theme.seasonAccent
import com.willykez.liturgx.ui.theme.seasonAccentSoft
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
    onBack: () -> Unit,
    onSelectResult: (SearchResult) -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = seasonAccent(color)

    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(query) {
        val term = query.trim()
        if (term.length < MIN_QUERY_LENGTH) {
            results = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(SEARCH_DEBOUNCE_MS)
        val found = withContext(Dispatchers.IO) { repository.search(term) }
        results = found
        isSearching = false
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
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    cursorColor = accent
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        when {
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
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { result ->
                        SearchResultRow(
                            result = result,
                            query = query.trim(),
                            color = color,
                            onClick = { onSelectResult(result) }
                        )
                    }
                    item { Spacer(Modifier.height(20.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    result: SearchResult,
    query: String,
    color: LiturgicalColor,
    onClick: () -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val accent = seasonAccent(color)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(seasonAccentSoft(color))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            "${result.bookName} ${result.chapterNum}:${result.verseNum}",
            style = MaterialTheme.typography.titleSmall,
            color = accent
        )
        Spacer(Modifier.height(2.dp))
        Text(
            highlightMatch(result.text, query, accent),
            style = MaterialTheme.typography.bodyMedium,
            color = onBg
        )
    }
}

private fun highlightMatch(text: String, query: String, accent: Color) = buildAnnotatedString {
    if (query.isBlank()) {
        append(text)
        return@buildAnnotatedString
    }
    val idx = text.indexOf(query, ignoreCase = true)
    if (idx < 0) {
        append(text)
        return@buildAnnotatedString
    }
    append(text.substring(0, idx))
    withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold)) {
        append(text.substring(idx, idx + query.length))
    }
    append(text.substring(idx + query.length))
}
