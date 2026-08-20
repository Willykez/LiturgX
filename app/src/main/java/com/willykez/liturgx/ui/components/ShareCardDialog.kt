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
import com.willykez.liturgx.ui.theme.seasonAccent

/**
 * Full-screen preview of a [LiturgicalCard] with Save/Share actions -- what the person gets
 * when they tap the image-share icon on a [ReadingBlock]. The card has to actually be part of
 * the visible composition to be captured this way (that's how [rememberGraphicsLayer]'s
 * record-while-drawing works), so showing it as a genuine preview rather than hiding it
 * off-screen is both the simplest implementation and, conveniently, better UX -- the person
 * sees exactly what they're about to save or send before doing either.
 */
@Composable
fun ShareCardDialog(
    dateText: String,
    seasonText: String,
    kindLabel: String,
    citation: String,
    passage: String,
    responseText: String?,
    liturgicalColor: LiturgicalColor,
    onDismiss: () -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()
    val accent = seasonAccent(liturgicalColor)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.drawWithContent {
                    // Record content into the layer for later capture, and composite the layer
                    // itself onto screen (rather than drawing the composable's content twice).
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawLayer(graphicsLayer)
                }
            ) {
                LiturgicalCard(
                    dateText = dateText,
                    seasonText = seasonText,
                    kindLabel = kindLabel,
                    citation = citation,
                    passage = passage,
                    responseText = responseText,
                    liturgicalColor = liturgicalColor
                )
            }

            Spacer(Modifier.height(20.dp))

            ImageSaveShareButtons(
                accent = accent,
                fileName = sanitizeFileName("${kindLabel}_$citation") + ".png",
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
