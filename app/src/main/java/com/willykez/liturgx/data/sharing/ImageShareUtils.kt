package com.willykez.liturgx.data.sharing

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Caches a rendered [LiturgicalCard][com.willykez.liturgx.ui.components.LiturgicalCard] bitmap
 * to app cache and shares it via a content:// URI (required for sharing to other apps since
 * API 24 -- a raw file:// Uri would be rejected by the receiving app).
 */
object ImageShareUtils {

    private const val CACHE_DIR = "images"
    private const val FILE_NAME = "reading_card_share.png"

    /** Reused filename means each share simply overwrites the last one -- no cache cleanup needed. */
    fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, CACHE_DIR)
            cachePath.mkdirs()
            val file = File(cachePath, FILE_NAME)
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            null
        }
    }

    fun shareImageUri(context: Context, uri: Uri) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Shiriki Somo kama Picha"))
    }
}
