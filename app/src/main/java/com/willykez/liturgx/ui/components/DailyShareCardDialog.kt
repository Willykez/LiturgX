package com.willykez.liturgx.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.sharing.DayCardReading
import com.willykez.liturgx.ui.theme.seasonAccent

/** Full-day counterpart to [ShareCardDialog] -- same capture-while-visible pattern, wrapping
 *  [DailyLiturgicalCard] instead of a single [LiturgicalCard]. */
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

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.drawWithContent {
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
            ) {
                DailyLiturgicalCard(
                    dateText = dateText,
                    seasonText = seasonText,
                    readings = readings,
                    liturgicalColor = liturgicalColor
                )
            }

            Spacer(Modifier.height(20.dp))

            ImageSaveShareButtons(
                accent = accent,
                fileName = sanitizeFileName("Masomo_$dateText") + ".png",
                getBitmap = { graphicsLayer.toImageBitmap().asAndroidBitmap() }
            )

            Spacer(Modifier.height(4.dp))
            Text(
                "Gusa nje ya kadi kufunga",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
