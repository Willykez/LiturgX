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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.sharing.DayCardReading
import com.willykez.liturgx.ui.theme.seasonAccent

@Composable
fun DailyLiturgicalCard(
    dateText: String,
    seasonText: String,
    readings: List<DayCardReading>,
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

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundTop,
                        backgroundBottom
                    )
                )
            )
    ) {

        // ═════════════════════════════════════════════════════════════════════
        // HERO HEADER
        // ═════════════════════════════════════════════════════════════════════

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(245.dp)
        ) {

            // Large atmospheric glow
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 95.dp, y = (-80).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                accent.copy(alpha = 0.42f),
                                accent.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Secondary glow
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-75).dp, y = 65.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Decorative sun
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-30).dp, y = 34.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                accent.copy(alpha = 0.75f),
                                accent.copy(alpha = 0.05f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 28.dp,
                        end = 28.dp,
                        top = 28.dp,
                        bottom = 22.dp
                    ),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color.White.copy(alpha = 0.10f))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.16f),
                                RoundedCornerShape(13.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Spa,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            brandName.uppercase(),
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Text(
                            "NENO LA LEO",
                            color = Color.White.copy(alpha = 0.55f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.4.sp
                        )
                    }
                }

                Column {
                    Text(
                        seasonText.uppercase(),
                        color = accent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )

                    Spacer(Modifier.height(5.dp))

                    Text(
                        dateText,
                        color = Color.White,
                        fontSize = 27.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(Modifier.height(7.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(3.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(accent)
                        )

                        Spacer(Modifier.width(9.dp))

                        Text(
                            "RANGI YA LITURUJIA • ${
                                liturgicalColor.swahili
                                    .replaceFirstChar { it.uppercase() }
                            }",
                            color = Color.White.copy(alpha = 0.62f),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }

        // ═════════════════════════════════════════════════════════════════════
        // READINGS AREA
        // ═════════════════════════════════════════════════════════════════════

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(paper)
                .padding(
                    start = 18.dp,
                    end = 18.dp,
                    top = 22.dp,
                    bottom = 18.dp
                )
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        "MASOMO YA LEO",
                        color = ink,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(Modifier.height(3.dp))

                    Text(
                        "Neno la Mungu kwa siku ya leo",
                        color = inkSoft,
                        fontSize = 11.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(Modifier.height(18.dp))

            readings.forEachIndexed { index, reading ->

                DailyReadingSection(
                    reading = reading,
                    accent = accent,
                    paperSoft = paperSoft,
                    ink = ink,
                    inkSoft = inkSoft
                )

                if (index != readings.lastIndex) {
                    Spacer(Modifier.height(15.dp))
                }
            }

            Spacer(Modifier.height(18.dp))

            // ═════════════════════════════════════════════════════════════════
            // FOOTER
            // ═════════════════════════════════════════════════════════════════

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(inkSoft.copy(alpha = 0.14f))
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoStories,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(Modifier.width(9.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        brandName,
                        color = ink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        "Masomo ya kila siku ya Liturujia",
                        color = inkSoft,
                        fontSize = 8.sp
                    )
                }

                Text(
                    "NENO • IMANI • MAISHA",
                    color = inkSoft,
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
        }
    }
}

@Composable
private fun DailyReadingSection(
    reading: DayCardReading,
    accent: Color,
    paperSoft: Color,
    ink: Color,
    inkSoft: Color
) {
    val icon = when {
        reading.kindLabel.contains("Injili", ignoreCase = true) ->
            Icons.Filled.AutoStories

        reading.kindLabel.contains("Wimbo", ignoreCase = true) ||
            reading.kindLabel.contains("Zaburi", ignoreCase = true) ->
            Icons.Filled.MusicNote

        else ->
            Icons.Filled.MenuBook
    }

    val isGospel = reading.kindLabel.contains(
        "Injili",
        ignoreCase = true
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isGospel) 5.dp else 2.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            )
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isGospel) {
                    Brush.verticalGradient(
                        listOf(
                            accent.copy(alpha = 0.10f),
                            Color.White
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color.White,
                            paperSoft.copy(alpha = 0.55f)
                        )
                    )
                }
            )
            .border(
                width = 1.dp,
                color = if (isGospel) {
                    accent.copy(alpha = 0.30f)
                } else {
                    inkSoft.copy(alpha = 0.10f)
                },
                shape = RoundedCornerShape(20.dp)
            )
            .padding(17.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp)
                )
            }

            Spacer(Modifier.width(11.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    reading.kindLabel.uppercase(),
                    color = accent,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp
                )

                Spacer(Modifier.height(3.dp))

                Text(
                    reading.citation,
                    color = ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 19.sp
                )
            }
        }

        Spacer(Modifier.height(13.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            accent.copy(alpha = 0.75f),
                            accent.copy(alpha = 0.08f),
                            Color.Transparent
                        )
                    )
                )
        )

        Spacer(Modifier.height(13.dp))

        Text(
            text = reading.passageText.trim(),
            color = ink,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.Serif,
                fontSize = if (isGospel) 16.sp else 15.sp,
                lineHeight = if (isGospel) 25.sp else 23.sp,
                fontWeight = if (isGospel) {
                    FontWeight.Normal
                } else {
                    FontWeight.Normal
                }
            )
        )

        reading.responseText
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { response ->

                Spacer(Modifier.height(13.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(13.dp))
                        .background(accent.copy(alpha = 0.07f))
                        .border(
                            1.dp,
                            accent.copy(alpha = 0.12f),
                            RoundedCornerShape(13.dp)
                        )
                        .padding(
                            horizontal = 13.dp,
                            vertical = 10.dp
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {

                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(34.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(accent)
                        )

                        Spacer(Modifier.width(9.dp))

                        Text(
                            text = response,
                            color = inkSoft,
                            fontSize = 11.sp,
                            lineHeight = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

        if (isGospel) {
            Spacer(Modifier.height(12.dp))

            Text(
                "✦  INJILI",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                color = accent.copy(alpha = 0.75f),
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }
    }
}