package com.willykez.liturgx.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.willykez.liturgx.core.EpiphanyMode
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.core.RegionSettings
import com.willykez.liturgx.ui.components.SeasonBackdrop
import com.willykez.liturgx.ui.theme.Parchment
import com.willykez.liturgx.ui.theme.ParchmentDim
import com.willykez.liturgx.ui.theme.seasonAccentSoft

@Composable
fun SettingsScreen(
    region: RegionSettings,
    currentColor: LiturgicalColor,
    onRegionChange: (RegionSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier.fillMaxSize()) {
        SeasonBackdrop(currentColor)
        Column(Modifier.fillMaxSize().padding(20.dp)) {
            Text("Mipangilio ya Jimbo", style = MaterialTheme.typography.headlineSmall, color = Parchment)
            Text(
                "Mila hizi hutofautiana kati ya majimbo — chagua zinazolingana na jimbo lako.",
                style = MaterialTheme.typography.labelMedium,
                color = ParchmentDim
            )
            Spacer(Modifier.height(20.dp))

            SettingCard(
                title = "Epifania",
                description = "Baadhi ya majimbo huadhimisha Epifania Januari 6 daima; mengine huihamishia Dominika iliyo karibu (Jan 2–8).",
                color = currentColor
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Hamishiwa Dominika", color = Parchment, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = region.epiphanyMode == EpiphanyMode.TRANSFERRED,
                        onCheckedChange = { checked ->
                            onRegionChange(region.copy(epiphanyMode = if (checked) EpiphanyMode.TRANSFERRED else EpiphanyMode.FIXED_JAN6))
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            SettingCard(
                title = "Kupaa kwa Bwana & Fungu Takatifu",
                description = "Majimbo mengi huhamishia sikukuu hizi Dominika; machache huzishika Alhamisi kama ilivyo asili.",
                color = currentColor
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Shika Alhamisi", color = Parchment, style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = region.keepThursdaySolemnities,
                        onCheckedChange = { onRegionChange(region.copy(keepThursdaySolemnities = it)) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(
                "LiturgX · Masomo ya Kila Siku kwa Kiswahili",
                style = MaterialTheme.typography.labelSmall,
                color = ParchmentDim
            )
        }
    }
}

@Composable
private fun SettingCard(
    title: String,
    description: String,
    color: LiturgicalColor,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(seasonAccentSoft(color))
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Parchment)
        Spacer(Modifier.height(4.dp))
        Text(description, style = MaterialTheme.typography.labelMedium, color = ParchmentDim)
        Spacer(Modifier.height(12.dp))
        content()
    }
}
