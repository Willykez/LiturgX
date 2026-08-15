package com.willykez.liturgx.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.data.bible.BiblePassage
import com.willykez.liturgx.data.bible.BibleRepository
import com.willykez.liturgx.model.AppLanguage
import com.willykez.liturgx.model.ReadingItem
import com.willykez.liturgx.model.ReadingLabels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A bounded content block, not a floating card: a hairline border, transparent background
 * (it sits directly on the page), and generous internal padding. This is the "cards must be
 * applied" boundary — content is clearly scoped per reading, but nothing about it competes
 * visually with the text.
 *
 * The Scripture body is always resolved live from the bundled Swahili Bible database — that's
 * the only real verse text this app has, so it's shown as the primary content rather than
 * hidden behind a "tap to reveal" toggle. [sectionLabel] and [language] only affect the
 * surrounding chrome (the eyebrow label, and — in English mode — a small note explaining why
 * the body itself stays in Swahili).
 */
@Composable
fun ReadingCard(
    reading: ReadingItem,
    sectionLabel: String,
    fontScale: Float = 1.0f,
    useSerif: Boolean = true,
    isGospel: Boolean = false,
    language: AppLanguage = AppLanguage.SWAHILI,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {

            // Eyebrow label + citation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sectionLabel.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isGospel) accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = (11 * fontScale).sp
                )
                Text(
                    text = reading.citation,
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontSize = (13 * fontScale).sp
                )
            }

            if (reading.headline.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = reading.headline,
                    style = MaterialTheme.typography.titleLarge,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = (16 * fontScale).sp,
                    lineHeight = (23 * fontScale).sp
                )
            }

            if (reading.responsorialVerse.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "℟. ${reading.responsorialVerse}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontStyle = FontStyle.Italic,
                    color = accentColor,
                    fontSize = (16 * fontScale).sp,
                    lineHeight = (24 * fontScale).sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            ResolvedScriptureBody(
                reading = reading,
                fontScale = fontScale,
                useSerif = useSerif,
                language = language
            )
        }
    }
}

/**
 * Resolves and shows the citation's Swahili verse text directly — no tap needed. Falls back to
 * whatever's in [ReadingItem.text] (usually empty for the bundled dataset) only if the citation
 * doesn't resolve against the Bible database, e.g. a Deuterocanonical book the bundled
 * Protestant/Swahili Union edition doesn't include.
 */
@Composable
private fun ResolvedScriptureBody(
    reading: ReadingItem,
    fontScale: Float,
    useSerif: Boolean,
    language: AppLanguage
) {
    val context = LocalContext.current
    val bibleRepository = remember { BibleRepository(context.applicationContext) }

    val state by produceState(initialValue = ScriptureLoadState(loading = true, passage = null), reading.citation) {
        val resolved = withContext(Dispatchers.IO) { bibleRepository.getPassage(reading.citation) }
        value = ScriptureLoadState(loading = false, passage = resolved)
    }

    val fontFamily = if (useSerif) FontFamily.Serif else FontFamily.Default

    when {
        state.loading -> CircularProgressIndicator(
            modifier = Modifier.padding(vertical = 4.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        state.passage != null && state.passage!!.verses.isNotEmpty() -> {
            Text(
                text = state.passage!!.renderedText(),
                style = MaterialTheme.typography.bodyLarge,
                fontFamily = fontFamily,
                fontSize = (17 * fontScale).sp,
                lineHeight = (28 * fontScale).sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            ReadingLabels.swahiliTextNote(language)?.let { note ->
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = (11 * fontScale).sp
                )
            }
        }
        reading.text.isNotBlank() -> Text(
            text = reading.text,
            style = MaterialTheme.typography.bodyLarge,
            fontFamily = fontFamily,
            fontSize = (17 * fontScale).sp,
            lineHeight = (28 * fontScale).sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        else -> Text(
            text = if (language == AppLanguage.SWAHILI)
                "Maandiko hayapatikani nje ya mtandao kwa somo hili."
            else
                "This passage isn't available offline.",
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class ScriptureLoadState(val loading: Boolean, val passage: BiblePassage?)
