package com.willykez.liturgx.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.sharing.ImageShareUtils
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.launch

/**
 * Full-screen preview of a [LiturgicalCard] with a "Share as Image" button -- what the person
 * gets when they tap the image-share icon on a [ReadingBlock]. The card has to actually be part
 * of the visible composition to be captured this way (that's how [rememberGraphicsLayer]'s
 * record-while-drawing works), so showing it as a genuine preview rather than hiding it
 * off-screen is both the simplest implementation and, conveniently, better UX -- the person
 * sees exactly what they're about to send before sending it.
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    var isSharing by remember { mutableStateOf(false) }
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
                    graphicsLayer.record { this@drawWithContent.drawContent() }
                    drawContent()
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

            Button(
                onClick = {
                    if (isSharing) return@Button
                    isSharing = true
                    scope.launch {
                        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
                        val uri = ImageShareUtils.saveBitmapToCache(context, bitmap)
                        isSharing = false
                        if (uri != null) {
                            ImageShareUtils.shareImageUri(context, uri)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Icon(Icons.Filled.IosShare, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (isSharing) "Inaandaa..." else "Shiriki kama Picha")
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Gusa nje ya kadi kufunga",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
