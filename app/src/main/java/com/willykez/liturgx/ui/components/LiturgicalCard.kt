package com.willykez.liturgx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.ui.theme.seasonAccent

/**
 * A single reading laid out as a shareable card -- the "post this to WhatsApp Status" version
 * of a [ReadingBlock]. Rebuilt to share [DailyLiturgicalCard]'s visual language (dark glowing
 * hero header over a warm paper reading area) rather than the flat parchment card this replaces,
 * so a single-reading share and a whole-day share now look like they came from the same app.
 */
@Composable
fun LiturgicalCard(
    dateText: String,
    seasonText: String,
    kindLabel: String,
    citation: String,
    passage: String,
    responseText: String? = null,
    liturgicalColor: LiturgicalColor,
    brandName: String = "LiturgX",
    modifier: Modifier = Modifier
) {
    val accent = seasonAccent(liturgicalColor)
    val backgroundTop = Color(0xFF17111F)
    val backgroundBottom = Color(0xFF09070D)
    val paper = Color(0xFFFFFCF5)
    val paperSoft = Color(0xFFF4EEE2)
    val ink = Color(0xFF211A27)
    val inkSoft = Color(0xFF665D6B)

    val icon = when {
        kindLabel.contains("Injili", ignoreCase = true) -> Icons.Filled.AutoStories
        kindLabel.contains("Wimbo", ignoreCase = true) || kindLabel.contains("Zaburi", ignoreCase = true) -> Icons.Filled.MusicNote
        else -> Icons.Filled.MenuBook
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Brush.verticalGradient(colors = listOf(backgroundTop, backgroundBottom)))
    ) {
        // ── HERO HEADER ──────────────────────────────────────────────────────
        Box(Modifier.fillMaxWidth().height(190.dp)) {
            Box(
                Modifier
                    .size(240.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 80.dp, y = (-70).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = 0.42f), accent.copy(alpha = 0.08f), Color.Transparent)
                        )
                    )
            )
            Box(
                Modifier
                    .size(72.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-24).dp, y = 26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.9f), accent.copy(alpha = 0.7f), accent.copy(alpha = 0.05f))
                        )
                    )
            )

            Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Filled.Spa, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(brandName.uppercase(), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                }

                Column {
                    Text(seasonText.uppercase(), color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.8.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(dateText, color = Color.White.copy(alpha = 0.82f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(22.dp).height(3.dp).clip(RoundedCornerShape(2.dp)).background(accent))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "RANGI YA LITURUJIA \u2022 ${liturgicalColor.swahili.replaceFirstChar { it.uppercase() }}",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }
        }

        // ── READING CARD ─────────────────────────────────────────────────────
        Column(Modifier.fillMaxWidth().background(paper).padding(18.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.verticalGradient(colors = listOf(accent.copy(alpha = 0.10f), Color.White)))
                    .border(1.dp, accent.copy(alpha = 0.28f), RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(accent.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(kindLabel.uppercase(), color = accent, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.1.sp)
                        Spacer(Modifier.height(3.dp))
                        Text(citation, color = ink, fontSize = 16.sp, fontWeight = FontWeight.Bold, lineHeight = 20.sp)
                    }
                }

                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(listOf(accent.copy(alpha = 0.75f), accent.copy(alpha = 0.08f), Color.Transparent))
                        )
                )
                Spacer(Modifier.height(14.dp))

                Text(
                    passage,
                    style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Serif, fontSize = 16.sp, lineHeight = 25.sp),
                    color = ink
                )

                if (!responseText.isNullOrEmpty()) {
                    Spacer(Modifier.height(14.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(13.dp))
                            .background(accent.copy(alpha = 0.08f))
                            .border(1.dp, accent.copy(alpha = 0.14f), RoundedCornerShape(13.dp))
                            .padding(horizontal = 13.dp, vertical = 10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.Top) {
                            Box(Modifier.width(3.dp).height(34.dp).clip(RoundedCornerShape(2.dp)).background(accent))
                            Spacer(Modifier.width(9.dp))
                            Text(responseText, color = inkSoft, fontSize = 12.sp, lineHeight = 18.sp, fontWeight = FontWeight.SemiBold, fontStyle = FontStyle.Italic)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(inkSoft.copy(alpha = 0.14f)))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(28.dp).clip(CircleShape).background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.AutoStories, contentDescription = null, tint = accent, modifier = Modifier.size(14.dp))
                }
                Spacer(Modifier.width(9.dp))
                Column(Modifier.weight(1f)) {
                    Text(brandName, color = ink, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Masomo ya kila siku ya Liturujia", color = inkSoft, fontSize = 8.sp)
                }
                Text("NENO \u2022 IMANI \u2022 MAISHA", color = inkSoft, fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            }
        }
    }
}
