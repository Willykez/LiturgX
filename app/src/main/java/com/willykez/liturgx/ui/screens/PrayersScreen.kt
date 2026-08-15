package com.willykez.liturgx.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.model.Prayer
import com.willykez.liturgx.model.PrayerCategory
import com.willykez.liturgx.ui.components.RosaryCounterWidget
import com.willykez.liturgx.ui.viewmodel.LiturgicalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayersScreen(
    viewModel: LiturgicalViewModel,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedPrayerCategory.collectAsState()
    val rosaryDecade by viewModel.rosaryDecade.collectAsState()
    val rosaryBead by viewModel.rosaryBead.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val fontScale by viewModel.fontScale.collectAsState()
    val useSerif by viewModel.useSerifFont.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPrayerForModal by remember { mutableStateOf<Prayer?>(null) }
    var showLatinText by remember { mutableStateOf(false) }

    val allPrayers = remember { viewModel.getOfflinePrayers() }
    val filteredPrayers = remember(selectedCategory, searchQuery) {
        allPrayers.filter { prayer ->
            prayer.category == selectedCategory &&
            (searchQuery.isEmpty() || prayer.title.contains(searchQuery, ignoreCase = true) || prayer.text.contains(searchQuery, ignoreCase = true))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Offline Prayer Mode",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDistractionFreeMode() },
                        modifier = Modifier.testTag("night_prayer_mode_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = "Quiet Night Mode"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Category Tabs Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(PrayerCategory.entries) { category ->
                    val isSelected = category == selectedCategory
                    Surface(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .clickable { viewModel.selectedPrayerCategory.value = category }
                            .testTag("prayer_cat_${category.name}"),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = category.title,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("prayer_search_input"),
                placeholder = { Text("Search prayers, hours, rosary...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            // Rosary Bead Counter Widget when ROSARY category is selected
            if (selectedCategory == PrayerCategory.ROSARY) {
                RosaryCounterWidget(
                    currentDecade = rosaryDecade,
                    currentBead = rosaryBead,
                    onNextBead = { viewModel.incrementRosaryBead() },
                    onReset = { viewModel.resetRosary() }
                )
            }

            // Prayers List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredPrayers) { prayer ->
                    val isBookmarked = bookmarks.any { it.id == "prayer_${prayer.id}" }
                    val isExpanded = selectedPrayerForModal?.id == prayer.id

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                selectedPrayerForModal = if (isExpanded) null else prayer
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prayer.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (prayer.subtitle.isNotEmpty()) {
                                        Text(
                                            text = prayer.subtitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 13.sp
                                        )
                                    }
                                }

                                Row {
                                    if (prayer.latinText != null) {
                                        IconButton(onClick = { showLatinText = !showLatinText }) {
                                            Icon(
                                                imageVector = Icons.Default.Translate,
                                                contentDescription = "Toggle Latin",
                                                tint = if (showLatinText) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    IconButton(onClick = { viewModel.toggleBookmarkPrayer(prayer) }) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark Prayer",
                                            tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(visible = isExpanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Text(
                                        text = if (showLatinText && prayer.latinText != null) prayer.latinText else prayer.text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontFamily = if (useSerif) FontFamily.Serif else FontFamily.Default,
                                        fontSize = (16 * fontScale).sp,
                                        lineHeight = (26 * fontScale).sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
