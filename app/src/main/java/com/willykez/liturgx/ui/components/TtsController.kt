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
 * rather than failing outright -- still usable, just without a Swahili accent, which is a much
 * better outcome than "the feature silently does nothing" on the devices where that happens.
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
                override fun onDone(utteranceId: String?) {
                    mainHandler.post { currentlyReadingId = null }
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    mainHandler.post { currentlyReadingId = null }
                }
            })
            isReady = true
        }
    }

    fun speak(id: String, text: String) {
        currentlyReadingId = id
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, id)
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
