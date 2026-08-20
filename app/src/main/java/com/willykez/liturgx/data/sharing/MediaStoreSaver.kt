package com.willykez.liturgx.data.sharing

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

/**
 * Saves a card image or PDF to a place the person can actually find later outside the app --
 * Pictures/LiturgX and Downloads/LiturgX respectively -- as distinct from [ImageShareUtils] and
 * [PdfShareUtils], which only stage a file in app cache long enough to hand it to another app
 * via a share sheet. Branches on API level because scoped storage (API 29+) and legacy public
 * storage (API 26-28, this app's minSdk) genuinely need different APIs -- MediaStore's
 * `RELATIVE_PATH` + `IS_PENDING` flow doesn't exist before Q, and direct file paths into public
 * storage aren't the correct approach from Q onward.
 */
object MediaStoreSaver {

    fun saveImage(context: Context, bitmap: Bitmap, displayName: String): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(
                context = context,
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                displayName = displayName,
                mimeType = "image/png",
                relativeDir = "${Environment.DIRECTORY_PICTURES}/LiturgX"
            ) { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        } else {
            saveToLegacyPublicDir(
                context = context,
                publicDir = Environment.DIRECTORY_PICTURES,
                displayName = displayName,
                mimeType = "image/png"
            ) { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        }

    fun savePdf(context: Context, sourceFile: File, displayName: String): Uri? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(
                context = context,
                collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                displayName = displayName,
                mimeType = "application/pdf",
                relativeDir = "${Environment.DIRECTORY_DOWNLOADS}/LiturgX"
            ) { out -> sourceFile.inputStream().use { it.copyTo(out) } }
        } else {
            saveToLegacyPublicDir(
                context = context,
                publicDir = Environment.DIRECTORY_DOWNLOADS,
                displayName = displayName,
                mimeType = "application/pdf"
            ) { out -> sourceFile.inputStream().use { it.copyTo(out) } }
        }

    private fun saveViaMediaStore(
        context: Context,
        collection: Uri,
        displayName: String,
        mimeType: String,
        relativeDir: String,
        write: (java.io.OutputStream) -> Unit
    ): Uri? {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativeDir)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }
        val uri = resolver.insert(collection, values) ?: return null
        return try {
            resolver.openOutputStream(uri)?.use(write) ?: return null
            values.clear()
            values.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            null
        }
    }

    /** Pre-Q: no scoped storage, so this app's minSdk (26) still needs a direct-file path into
     *  the legacy public directory, then a media scan so it shows up in Gallery/Files right away. */
    private fun saveToLegacyPublicDir(
        context: Context,
        publicDir: String,
        displayName: String,
        mimeType: String,
        write: (java.io.OutputStream) -> Unit
    ): Uri? {
        return try {
            val dir = File(Environment.getExternalStoragePublicDirectory(publicDir), "LiturgX").apply { mkdirs() }
            val file = File(dir, displayName)
            FileOutputStream(file).use(write)
            MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(mimeType), null)
            Uri.fromFile(file)
        } catch (e: Exception) {
            null
        }
    }
}
