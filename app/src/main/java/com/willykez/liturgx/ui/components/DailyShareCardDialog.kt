package com.willykez.liturgx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.sharing.DayCardReading
import com.willykez.liturgx.ui.theme.seasonAccent

@Composable
fun DailyShareCardDialog(
    dateText: String,
    seasonText: String,
    readings: List<DayCardReading>,
    liturgicalColor: LiturgicalColor,
    onDismiss: () -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()
    val accent = seasonAccent(liturgicalColor)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true,
            dismissOnBackPress = true
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color(0xFF09070D))
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = 14.dp,
                    vertical = 18.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                "MUONEKANO WA KUSHIRIKI",
                color = Color.White.copy(alpha = 0.55f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.5.sp
            )

            Spacer(Modifier.height(12.dp))

            /*
             * The Box records exactly the rendered card.
             * ImageSaveShareButtons receives that same bitmap,
             * so the upgraded visual is also the exported PNG.
             */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawWithContent {

                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }

                        drawLayer(graphicsLayer)
                    }
            ) {
                DailyLiturgicalCard(
                    dateText = dateText,
                    seasonText = seasonText,
                    readings = readings,
                    liturgicalColor = liturgicalColor,
                    brandName = "LiturgX"
                )
            }

            Spacer(Modifier.height(22.dp))

            ImageSaveShareButtons(
                accent = accent,
                fileName = sanitizeFileName(
                    "LiturgX_Masomo_$dateText"
                ) + ".png",
                getBitmap = {
                    graphicsLayer
                        .toImageBitmap()
                        .asAndroidBitmap()
                }
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Gusa nje ya kadi kufunga",
                color = Color.White.copy(alpha = 0.45f),
                style = MaterialTheme.typography.labelSmall
            )

            Spacer(Modifier.height(10.dp))
        }
    }
}