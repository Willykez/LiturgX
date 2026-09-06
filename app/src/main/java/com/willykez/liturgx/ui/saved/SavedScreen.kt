package com.willykez.liturgx.ui.saved

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.bible.BibleBookInfo
import com.willykez.liturgx.data.bible.BibleBrowseRepository
import com.willykez.liturgx.data.bible.BibleUserDataStore
import com.willykez.liturgx.ui.theme.seasonAccent

private enum class SavedTab(val label: String) { BOOKMARKS("Alama"), HIGHLIGHTS("Iliyoangaziwa"), NOTES("Dokezo") }

private data class SavedEntry(
    val key: String,
    val bookId: Int,
    val chapterNum: Int,
    val verseNum: Int,
    val bookName: String,
    val text: String,
    val note: String? = null
)

/**
 * "Yaliyohifadhiwa" -- everything bookmarked, highlighted, or noted from the Bible tab, in one
 * place, matching BibliaApp's dedicated Saved screen. Each store only remembers an address
 * ("bookId:chapter:verse"), so every entry is resolved back into a book name and verse text
 * here via [repository] before it's shown -- if a verse address is somehow stale (e.g. the
 * bundled Bible data changed), it's silently skipped rather than shown broken.
 */
@Composable
fun SavedScreen(
    books: List<BibleBookInfo>,
    color: LiturgicalColor,
    onSelectVerse: (bookId: Int, chapterNum: Int, verseNum: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { BibleBrowseRepository(context.applicationContext) }
    val userData = remember { BibleUserDataStore(context) }
    val accent = seasonAccent(color)
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant

    var tab by remember { mutableStateOf(SavedTab.BOOKMARKS) }
    // Bumped after any remove action so the list re-resolves against the store's current state.
    var version by remember { mutableStateOf(0) }

    val bookNameById = remember(books) { books.associate { it.id to it.name } }

    fun resolve(key: String, note: String? = null): SavedEntry? {
        val parts = key.split(":")
        if (parts.size != 3) return null
        val bookId = parts[0].toIntOrNull() ?: return null
        val chapterNum = parts[1].toIntOrNull() ?: return null
        val verseNum = parts[2].toIntOrNull() ?: return null
        val bookName = bookNameById[bookId] ?: return null
        val text = repository.verseAt(bookId, chapterNum, verseNum) ?: return null
        return SavedEntry(key, bookId, chapterNum, verseNum, bookName, text, note)
    }

    val entries = remember(tab, version, books) {
        when (tab) {
            SavedTab.BOOKMARKS -> userData.allBookmarkKeys().mapNotNull { resolve(it) }
            SavedTab.HIGHLIGHTS -> userData.allHighlightKeys().mapNotNull { resolve(it) }
            SavedTab.NOTES -> userData.allNotes().mapNotNull { (key, note) -> resolve(key, note) }
        }.sortedWith(compareBy({ it.bookId }, { it.chapterNum }, { it.verseNum }))
    }

    Column(modifier.fillMaxSize().padding(top = 8.dp)) {
        Text(
            "Yaliyohifadhiwa",
            style = MaterialTheme.typography.titleLarge,
            color = onBg,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            SavedTab.entries.forEach { t ->
                val selected = t == tab
                Text(
                    t.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    color = if (selected) accent else onBgDim,
                    modifier = Modifier.clickable { tab = t }
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)

        if (entries.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    when (tab) {
                        SavedTab.BOOKMARKS -> "Bado hujaweka alama kwenye mstari wowote."
                        SavedTab.HIGHLIGHTS -> "Bado hujaangazia mstari wowote."
                        SavedTab.NOTES -> "Bado hujaandika dokezo lolote."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = onBgDim,
                    modifier = Modifier.padding(horizontal = 40.dp)
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(entries, key = { it.key }) { entry ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onSelectVerse(entry.bookId, entry.chapterNum, entry.verseNum) }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                when (tab) {
                                    SavedTab.BOOKMARKS -> Icons.Filled.Bookmark
                                    SavedTab.HIGHLIGHTS -> Icons.Filled.Highlight
                                    SavedTab.NOTES -> Icons.Filled.NoteAlt
                                },
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.height(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "${entry.bookName} ${entry.chapterNum}:${entry.verseNum}",
                                style = MaterialTheme.typography.titleSmall,
                                color = onBg,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    when (tab) {
                                        SavedTab.BOOKMARKS -> userData.setBookmarked(entry.key, false)
                                        SavedTab.HIGHLIGHTS -> userData.setHighlighted(entry.key, false)
                                        SavedTab.NOTES -> userData.removeNote(entry.key)
                                    }
                                    version++
                                },
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Ondoa", tint = onBgDim, modifier = Modifier.height(16.dp))
                            }
                        }
                        Text(
                            entry.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = onBgDim,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        if (entry.note != null) {
                            Text(
                                entry.note,
                                style = MaterialTheme.typography.bodyMedium,
                                color = accent,
                                fontWeight = FontWeight.Medium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                }
            }
        }
    }
}
