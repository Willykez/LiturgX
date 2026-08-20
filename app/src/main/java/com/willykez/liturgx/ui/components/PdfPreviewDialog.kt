package com.willykez.liturgx.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.willykez.liturgx.core.LiturgicalColor
import com.willykez.liturgx.data.sharing.MediaStoreSaver
import com.willykez.liturgx.data.sharing.PdfShareUtils
import com.willykez.liturgx.ui.theme.seasonAccent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private enum class PdfBusyAction { NONE, SAVE, SHARE }

/**
 * Preview of the generated day PDF -- first page rasterized via [PdfRenderer] so the person
 * sees roughly what they're about to save or send, with Save (to Downloads/LiturgX) and Share
 * actions, same pairing as [ImageSaveShareButtons] uses for the image cards.
 */
@Composable
fun PdfPreviewDialog(
    file: File,
    color: LiturgicalColor,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val accent = seasonAccent(color)

    var previewBitmap by remember(file) { mutableStateOf<Bitmap?>(null) }
    var pageCount by remember(file) { mutableStateOf(1) }
    var busy by remember(file) { mutableStateOf(PdfBusyAction.NONE) }
    var saved by remember(file) { mutableStateOf(false) }

    LaunchedEffect(file) {
        val (bitmap, count) = withContext(Dispatchers.IO) { renderFirstPage(file) }
        previewBitmap = bitmap
        pageCount = count
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(595f / 842f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                val bmp = previewBitmap
                if (bmp != null) {
                    Image(bmp.asImageBitmap(), contentDescription = "Onyesho la PDF", modifier = Modifier.fillMaxSize())
                } else {
                    CircularProgressIndicator(color = accent)
                }
            }

            if (pageCount > 1) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Ukurasa 1 kati ya $pageCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        if (busy != PdfBusyAction.NONE) return@OutlinedButton
                        busy = PdfBusyAction.SAVE
                        scope.launch {
                            val uri = withContext(Dispatchers.IO) {
                                MediaStoreSaver.savePdf(context, file, sanitizeFileName(file.nameWithoutExtension) + ".pdf")
                            }
                            busy = PdfBusyAction.NONE
                            saved = uri != null
                        }
                    },
                    enabled = busy == PdfBusyAction.NONE,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
                ) {
                    Text(
                        when {
                            busy == PdfBusyAction.SAVE -> "Inahifadhi..."
                            saved -> "Imehifadhiwa"
                            else -> "Hifadhi"
                        }
                    )
                }
                Button(
                    onClick = {
                        if (busy != PdfBusyAction.NONE) return@Button
                        PdfShareUtils.share(context, file)
                    },
                    enabled = busy == PdfBusyAction.NONE,
                    colors = ButtonDefaults.buttonColors(containerColor = accent)
                ) {
                    Text("Shiriki")
                }
            }

            Spacer(Modifier.height(4.dp))
            Text(
                "Gusa nje kufunga",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun renderFirstPage(file: File): Pair<Bitmap?, Int> {
    return try {
        ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { pfd ->
            PdfRenderer(pfd).use { renderer ->
                val count = renderer.pageCount
                if (count == 0) return null to 0
                renderer.openPage(0).use { page ->
                    // 2x for a crisper preview than the PDF's native point-based page size would give.
                    val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
                    bitmap.eraseColor(android.graphics.Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmap to count
                }
            }
        }
    } catch (e: Exception) {
        null to 1
    }
}
