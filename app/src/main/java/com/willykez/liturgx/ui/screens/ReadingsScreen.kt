package com.willykez.liturgx.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFormat
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.data.bible.BibleRepository
import com.willykez.liturgx.data.sharing.LectionaryShareFormatter
import com.willykez.liturgx.model.AppLanguage
import com.willykez.liturgx.model.ReadingLabels
import com.willykez.liturgx.model.ReadingSection
import com.willykez.liturgx.ui.components.LiturgicalHeader
import com.willykez.liturgx.ui.components.ReadingCard
import com.willykez.liturgx.ui.viewmodel.LiturgicalViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingsScreen(
    viewModel: LiturgicalViewModel,
    onOpenCalendarPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val reading by viewModel.currentReading.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val useSerifFont by viewModel.useSerifFont.collectAsState()
    val isDistractionFree by viewModel.distractionFreeMode.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val interfaceLanguage by viewModel.interfaceLanguage.collectAsState()

    var showFontControls by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val bibleRepository = remember { BibleRepository(context.applicationContext) }

    val isBookmarked = remember(bookmarks, reading) {
        val dateStr = selectedDate.toString()
        bookmarks.any { it.id == "reading_$dateStr" }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (!isDistractionFree) {
                TopAppBar(
                    title = {
                        Text(
                            text = ReadingLabels.readingsTabTitle(interfaceLanguage),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val currentLit = reading ?: return@IconButton
                                coroutineScope.launch {
                                    val citations = listOfNotNull(
                                        currentLit.firstReading,
                                        currentLit.responsorialPsalm,
                                        currentLit.secondReading,
                                        currentLit.gospel
                                    )
                                    val passages = withContext(Dispatchers.IO) {
                                        citations.associate { it.citation to bibleRepository.getPassage(it.citation) }
                                    }
                                    val text = LectionaryShareFormatter.format(currentLit, selectedDate, passages, interfaceLanguage)
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    }
                                    val shareTitle = if (interfaceLanguage == AppLanguage.SWAHILI) "Shiriki Masomo" else "Share Readings"
                                    context.startActivity(Intent.createChooser(sendIntent, shareTitle))
                                }
                            },
                            modifier = Modifier.testTag("share_reading_button")
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share Readings")
                        }

                        IconButton(
                            onClick = { showFontControls = !showFontControls },
                            modifier = Modifier.testTag("font_controls_button")
                        ) {
                            Icon(imageVector = Icons.Default.FormatSize, contentDescription = "Adjust Font Size")
                        }

                        IconButton(
                            onClick = { viewModel.toggleSerifFont() },
                            modifier = Modifier.testTag("toggle_serif_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.TextFormat,
                                contentDescription = "Toggle Font Style",
                                tint = if (useSerifFont) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleBookmarkCurrentReading() },
                            modifier = Modifier.testTag("bookmark_reading_button")
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Reading",
                                tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleDistractionFreeMode() },
                            modifier = Modifier.testTag("distraction_free_button")
                        ) {
                            Icon(
                                imageVector = if (isDistractionFree) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Distraction Free Mode"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedVisibility(visible = showFontControls) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Text size: ${(fontScale * 100).toInt()}%",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = if (useSerifFont) "Serif" else "Sans-serif",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = fontScale,
                        onValueChange = { viewModel.updateFontScale(it) },
                        valueRange = 0.8f..1.5f,
                        steps = 6,
                        modifier = Modifier.testTag("font_scale_slider")
                    )
                }
            }

            if (!isDistractionFree) {
                LiturgicalHeader(
                    reading = reading,
                    selectedDate = selectedDate,
                    language = interfaceLanguage,
                    onPreviousDate = { viewModel.previousDay() },
                    onNextDate = { viewModel.nextDay() },
                    onTodayClick = { viewModel.jumpToToday() },
                    onOpenCalendar = onOpenCalendarPicker
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (reading != null) {
                val currentLit = reading!!

                if (currentLit.reflection.isNotBlank()) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                        Text(
                            text = ReadingLabels.reflectionLabel(interfaceLanguage).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currentLit.reflection,
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            fontFamily = if (useSerifFont) FontFamily.Serif else FontFamily.Default,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = (15 * fontScale).sp,
                            lineHeight = (23 * fontScale).sp
                        )
                    }
                }

                ReadingCard(
                    reading = currentLit.firstReading,
                    sectionLabel = ReadingLabels.sectionLabel(ReadingSection.FIRST_READING, interfaceLanguage),
                    fontScale = fontScale,
                    useSerif = useSerifFont,
                    language = interfaceLanguage
                )
                ReadingCard(
                    reading = currentLit.responsorialPsalm,
                    sectionLabel = ReadingLabels.sectionLabel(ReadingSection.PSALM, interfaceLanguage),
                    fontScale = fontScale,
                    useSerif = useSerifFont,
                    language = interfaceLanguage
                )

                currentLit.secondReading?.let {
                    ReadingCard(
                        reading = it,
                        sectionLabel = ReadingLabels.sectionLabel(ReadingSection.SECOND_READING, interfaceLanguage),
                        fontScale = fontScale,
                        useSerif = useSerifFont,
                        language = interfaceLanguage
                    )
                }

                ReadingCard(
                    reading = currentLit.gospel,
                    sectionLabel = ReadingLabels.sectionLabel(ReadingSection.GOSPEL, interfaceLanguage),
                    fontScale = fontScale,
                    useSerif = useSerifFont,
                    isGospel = true,
                    language = interfaceLanguage
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
