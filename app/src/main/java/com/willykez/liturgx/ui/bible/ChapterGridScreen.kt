package com.willykez.liturgx.ui.bible

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.bible.BibleBookInfo
import com.willykez.liturgx.ui.theme.seasonAccent
import com.willykez.liturgx.ui.theme.seasonAccentSoft

@Composable
fun ChapterGridScreen(
    book: BibleBookInfo,
    color: LiturgicalColor,
    onBack: () -> Unit,
    onSelectChapter: (Int) -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    val accent = seasonAccent(color)

    Column(Modifier.fillMaxSize().padding(top = 8.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Rudi kwenye vitabu", tint = onBg)
            }
            Column {
                Text(book.name, style = MaterialTheme.typography.headlineSmall, color = onBg)
                Text("Chagua Sura", style = MaterialTheme.typography.labelMedium, color = onBgDim)
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 56.dp),
            contentPadding = PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(book.chapterCount) { index ->
                val chapterNum = index + 1
                Box(
                    Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(seasonAccentSoft(color))
                        .clickable { onSelectChapter(chapterNum) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        chapterNum.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = accent,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
