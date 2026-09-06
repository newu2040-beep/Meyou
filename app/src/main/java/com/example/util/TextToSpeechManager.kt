package com.example.util

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.*
import kotlinx.coroutines.*
import java.util.*

enum class VoicePreset(val label: String, val pitch: Float, val speechRate: Float) {
    NATURAL("Natural", 1.0f, 1.0f),
    CALM_SERENE("Calm Serene", 0.9f, 0.9f),
    EXPRESSIVE("Expressive", 1.15f, 1.05f),
    DEEP_NARRATOR("Deep Narrator", 0.75f, 0.95f),
    SPEED_READER("Fast Reader", 1.0f, 1.4f)
}

class TextToSpeechManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var timerJob: Job? = null

    var isPlaying by mutableStateOf(false)
        private set

    var isPaused by mutableStateOf(false)
        private set

    var currentParagraphIndex by mutableIntStateOf(0)
        private set

    var currentSpokenText by mutableStateOf("")
        private set

    var playbackSpeed by mutableFloatStateOf(1.0f)
        private set

    var pitch by mutableFloatStateOf(1.0f)
        private set

    var selectedVoicePreset by mutableStateOf(VoicePreset.NATURAL)
        private set

    var isPlayerVisible by mutableStateOf(false)
        private set

    var sleepTimerMinutes by mutableStateOf<Int?>(null)
        private set

    var sleepTimerRemainingSeconds by mutableIntStateOf(0)
        private set

    private var activeParagraphs: List<String> = emptyList()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale.getDefault()
            tts?.setSpeechRate(playbackSpeed)
            tts?.setPitch(pitch)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    mainHandler.post {
                        isPlaying = true
                        isPaused = false
                    }
                }

                override fun onDone(utteranceId: String?) {
                    mainHandler.post {
                        val nextIndex = currentParagraphIndex + 1
                        if (nextIndex < activeParagraphs.size) {
                            speakParagraph(nextIndex)
                        } else {
                            isPlaying = false
                            isPaused = false
                            currentSpokenText = ""
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    mainHandler.post {
                        isPlaying = false
                        isPaused = false
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    mainHandler.post {
                        isPlaying = false
                        isPaused = false
                    }
                }
            })
        }
    }

    fun startReading(paragraphs: List<String>, startIndex: Int = 0) {
        if (paragraphs.isEmpty()) return
        activeParagraphs = paragraphs
        isPlayerVisible = true
        speakParagraph(startIndex.coerceIn(0, paragraphs.size - 1))
    }

    private fun speakParagraph(index: Int) {
        if (!isInitialized || tts == null) return
        if (index !in activeParagraphs.indices) return

        currentParagraphIndex = index
        val textToRead = activeParagraphs[index]
        currentSpokenText = textToRead

        try {
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "utterance_$index")

            tts?.setSpeechRate(playbackSpeed)
            tts?.setPitch(pitch)
            val result = tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, params, "utterance_$index")
            if (result != TextToSpeech.ERROR) {
                isPlaying = true
                isPaused = false
            } else {
                isPlaying = false
                isPaused = false
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            isPlaying = false
            isPaused = false
        }
    }

    fun pause() {
        try {
            if (isPlaying) {
                tts?.stop()
                isPlaying = false
                isPaused = true
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun resume() {
        try {
            if (isPaused) {
                speakParagraph(currentParagraphIndex)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (t: Throwable) {
            t.printStackTrace()
        }
        isPlaying = false
        isPaused = false
        currentSpokenText = ""
        cancelSleepTimer()
    }

    fun nextParagraph() {
        if (currentParagraphIndex + 1 < activeParagraphs.size) {
            speakParagraph(currentParagraphIndex + 1)
        }
    }

    fun previousParagraph() {
        if (currentParagraphIndex > 0) {
            speakParagraph(currentParagraphIndex - 1)
        } else {
            speakParagraph(0)
        }
    }

    fun setSpeed(speed: Float) {
        playbackSpeed = speed
        tts?.setSpeechRate(speed)
        if (isPlaying) {
            speakParagraph(currentParagraphIndex)
        }
    }

    fun setVoicePitch(newPitch: Float) {
        pitch = newPitch
        tts?.setPitch(newPitch)
        if (isPlaying) {
            speakParagraph(currentParagraphIndex)
        }
    }

    fun applyVoicePreset(preset: VoicePreset) {
        selectedVoicePreset = preset
        playbackSpeed = preset.speechRate
        pitch = preset.pitch
        tts?.setSpeechRate(playbackSpeed)
        tts?.setPitch(pitch)
        if (isPlaying) {
            speakParagraph(currentParagraphIndex)
        }
    }

    fun showPlayer() {
        isPlayerVisible = true
    }

    fun hidePlayer() {
        isPlayerVisible = false
    }

    fun togglePlayerVisibility() {
        isPlayerVisible = !isPlayerVisible
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerMinutes = minutes
        sleepTimerRemainingSeconds = minutes * 60
        timerJob?.cancel()

        timerJob = scope.launch {
            while (sleepTimerRemainingSeconds > 0) {
                delay(1000)
                sleepTimerRemainingSeconds--
            }
            // Timer expired -> pause playback
            stop()
            sleepTimerMinutes = null
        }
    }

    fun cancelSleepTimer() {
        timerJob?.cancel()
        timerJob = null
        sleepTimerMinutes = null
        sleepTimerRemainingSeconds = 0
    }

    fun release() {
        timerJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
