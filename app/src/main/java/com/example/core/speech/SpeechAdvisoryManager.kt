package com.example.core.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class SpeechPlaybackState(
    val isSpeaking: Boolean = false,
    val isInitialized: Boolean = false,
    val currentSnippet: String? = null
)

class SpeechAdvisoryManager(context: Context) : TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private val _playbackState = MutableStateFlow(SpeechPlaybackState())
    val playbackState: StateFlow<SpeechPlaybackState> = _playbackState.asStateFlow()

    private val hindiLocale = Locale("hi", "IN")
    private val englishLocale = Locale.ENGLISH

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _playbackState.value = _playbackState.value.copy(isInitialized = true)
            textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _playbackState.value = _playbackState.value.copy(isSpeaking = true)
                }

                override fun onDone(utteranceId: String?) {
                    _playbackState.value = _playbackState.value.copy(
                        isSpeaking = false,
                        currentSnippet = null
                    )
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _playbackState.value = _playbackState.value.copy(
                        isSpeaking = false,
                        currentSnippet = null
                    )
                }
            })
        }
    }

    fun speak(text: String, isHindi: Boolean = false) {
        val tts = textToSpeech ?: return
        if (tts.isSpeaking) {
            tts.stop()
        }

        val targetLocale = if (isHindi) {
            val res = tts.setLanguage(hindiLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                englishLocale
            } else {
                hindiLocale
            }
        } else {
            englishLocale
        }

        tts.language = targetLocale
        tts.setPitch(1.0f)
        tts.setSpeechRate(0.92f) // Slightly slower cadence for clarity in field conditions

        val utteranceId = "crop_ai_speech_${System.currentTimeMillis()}"
        _playbackState.value = _playbackState.value.copy(
            isSpeaking = true,
            currentSnippet = text.take(60)
        )

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    fun stop() {
        textToSpeech?.stop()
        _playbackState.value = _playbackState.value.copy(
            isSpeaking = false,
            currentSnippet = null
        )
    }

    fun shutdown() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            textToSpeech = null
        } catch (_: Exception) { }
    }
}
