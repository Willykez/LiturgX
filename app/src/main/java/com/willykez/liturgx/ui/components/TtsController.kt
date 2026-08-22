package com.willykez.liturgx.ui.components

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * One shared [TextToSpeech] engine for the whole readings screen, rather than one per
 * [ReadingBlock] -- multiple simultaneous engine connections would be wasteful and risk
 * overlapping audio. [currentlyReadingId] identifies which card (by citation) is the active
 * one, letting each card compare against its own citation to know whether it's the one reading
 * right now, without needing its own separate playing-state.
 *
 * Falls back silently to the device's default TTS language if Swahili voice data isn't
 * installed on this engine (`setLanguage` returning `LANG_MISSING_DATA`/`LANG_NOT_SUPPORTED`)
 * rather than failing outright -- still usable, just without a Swahili accent.
 *
 * [speak] chunks text longer than [TextToSpeech.getMaxSpeechInputLength] into consecutive
 * utterances (Android enforces a real per-utterance character cap; a long reading exceeding it
 * would otherwise just silently fail) and returns whether it actually got queued -- callers
 * should surface that to the person rather than let a failed attempt look identical to a
 * successful one, which was the previous behavior.
 */
class TtsController(private val context: Context) {
    private var tts: TextToSpeech? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    var isReady by mutableStateOf(false)
        private set
    var currentlyReadingId by mutableStateOf<String?>(null)
        private set

    fun initialize() {
        tts = TextToSpeech(context) { status ->
            if (status != TextToSpeech.SUCCESS) return@TextToSpeech
            val engine = tts ?: return@TextToSpeech
            val result = engine.setLanguage(Locale("sw"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                engine.language = Locale.getDefault()
            }
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) = clearIfCurrent(utteranceId)

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) = clearIfCurrent(utteranceId)

                override fun onError(utteranceId: String?, errorCode: Int) = clearIfCurrent(utteranceId)
            })
            isReady = true
        }
    }

    /** Only clears the "reading" indicator if the utterance that just finished/errored is
     *  actually the current one -- a flushed *previous* utterance's callback firing after a
     *  new [speak] call started shouldn't wipe out the new one's playing state. Posted to the
     *  main thread since utterance callbacks fire on a background thread, and both the read and
     *  write of [currentlyReadingId] need to happen together, not interleaved with a UI-thread read. */
    private fun clearIfCurrent(utteranceId: String?) {
        mainHandler.post {
            if (utteranceId != null && utteranceId == currentlyReadingId) currentlyReadingId = null
        }
    }

    /** Returns whether speech was actually queued -- false means nothing will play, and the
     *  caller should tell the person so instead of leaving them looking at a silent "playing" state. */
    fun speak(id: String, text: String): Boolean {
        val engine = tts ?: return false
        if (!isReady || text.isBlank()) return false

        val maxLength = TextToSpeech.getMaxSpeechInputLength().coerceAtLeast(500)
        val chunks = chunkText(text, maxLength)
        if (chunks.isEmpty()) return false

        currentlyReadingId = id
        var allQueued = true
        chunks.forEachIndexed { index, chunk ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            // Only the FINAL chunk carries the real id -- that's the utteranceId clearIfCurrent
            // watches for, so the "reading" indicator clears when the whole passage finishes,
            // not after just the first chunk.
            val utteranceId = if (index == chunks.lastIndex) id else "${id}_part$index"
            val result = engine.speak(chunk, queueMode, null, utteranceId)
            if (result != TextToSpeech.SUCCESS) allQueued = false
        }
        if (!allQueued) {
            currentlyReadingId = null
            return false
        }
        return true
    }

    fun stop() {
        tts?.stop()
        currentlyReadingId = null
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }

    companion object {
        /** Cuts at the last space before [maxLength] rather than mid-word, when possible. */
        fun chunkText(text: String, maxLength: Int): List<String> {
            if (text.length <= maxLength) return if (text.isBlank()) emptyList() else listOf(text)
            val chunks = mutableListOf<String>()
            var remaining = text
            while (remaining.isNotEmpty()) {
                if (remaining.length <= maxLength) {
                    chunks += remaining
                    break
                }
                val cut = remaining.lastIndexOf(' ', maxLength - 1).let { if (it > 0) it else maxLength }
                chunks += remaining.substring(0, cut)
                remaining = remaining.substring(cut).trimStart()
            }
            return chunks
        }
    }
}

@Composable
fun rememberTtsController(): TtsController {
    val context = LocalContext.current
    val controller = remember { TtsController(context.applicationContext) }
    DisposableEffect(Unit) {
        controller.initialize()
        onDispose { controller.shutdown() }
    }
    return controller
}
