package com.willykez.liturgx.ui.saints

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun SaintsScreen(saints: List<Saint>, modifier: Modifier = Modifier) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(query, saints) {
        if (query.isBlank()) saints else saints.filter {
            it.jina.contains(query, ignoreCase = true) || it.tarehe.contains(query, ignoreCase = true)
        }
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
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered, key = { it.id }) { saint -> SaintRow(saint) }
        }
    }
}

@Composable
private fun SaintRow(saint: Saint) {
    val color = LiturgicalColor.fromSwahili(saint.rangi)
    val onBg = MaterialTheme.colorScheme.onBackground
    val onBgDim = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(seasonAccentSoft(color))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LiturgicalSeal(color, size = 34.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(saint.jina, style = MaterialTheme.typography.titleMedium, color = onBg)
            Text("${saint.tarehe} · ${saint.daraja}", style = MaterialTheme.typography.labelMedium, color = onBgDim)
        }
    }
}
