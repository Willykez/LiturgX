package com.willykez.liturgx.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.willykez.liturgx.data.sharing.ImageShareUtils
import com.willykez.liturgx.data.sharing.MediaStoreSaver
import kotlinx.coroutines.launch

private enum class BusyAction { NONE, SAVE, SHARE }

/** Strips anything that isn't filesystem-safe across both scoped-storage (MediaStore) and
 *  legacy-storage (direct File) save paths -- citations contain colons and spaces
 *  ("Zaburi 132:6-7"), neither of which belongs in a filename. */
fun sanitizeFileName(name: String): String =
    name.replace(Regex("[^A-Za-z0-9_-]+"), "_").trim('_').take(80)

/**
 * The Save / Share button pair shown under every image-card preview
 * ([ShareCardDialog], [DailyShareCardDialog]) -- both dialogs need the identical capture-a-
 * bitmap-then-either-write-it-or-hand-it-to-a-share-sheet logic, so it's factored out once here
 * rather than duplicated. Save on API 26-28 needs a runtime `WRITE_EXTERNAL_STORAGE` permission
 * (requested inline, only on those API levels -- see [MediaStoreSaver]'s doc for why Q+ needs
 * neither the permission nor this branch at all).
 */
@Composable
fun ImageSaveShareButtons(
    accent: Color,
    fileName: String,
    getBitmap: suspend () -> Bitmap,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(BusyAction.NONE) }
    var saved by remember { mutableStateOf(false) }

    fun performSave() {
        busy = BusyAction.SAVE
        scope.launch {
            val bitmap = getBitmap()
            val uri = MediaStoreSaver.saveImage(context, bitmap, fileName)
            busy = BusyAction.NONE
            saved = uri != null
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) performSave() }

    Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = {
                if (busy != BusyAction.NONE) return@OutlinedButton
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                    if (granted) performSave() else saveLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                } else {
                    performSave()
                }
            },
            enabled = busy == BusyAction.NONE,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
        ) {
            Icon(if (saved) Icons.Filled.Check else Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                when {
                    busy == BusyAction.SAVE -> "Inahifadhi..."
                    saved -> "Imehifadhiwa"
                    else -> "Hifadhi"
                }
            )
        }

        Button(
            onClick = {
                if (busy != BusyAction.NONE) return@Button
                busy = BusyAction.SHARE
                scope.launch {
                    val bitmap = getBitmap()
                    val uri = ImageShareUtils.saveBitmapToCache(context, bitmap)
                    busy = BusyAction.NONE
                    if (uri != null) ImageShareUtils.shareImageUri(context, uri)
                }
            },
            enabled = busy == BusyAction.NONE,
            colors = ButtonDefaults.buttonColors(containerColor = accent)
        ) {
            Icon(Icons.Filled.IosShare, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (busy == BusyAction.SHARE) "Inaandaa..." else "Shiriki")
        }
    }
}
