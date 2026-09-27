package com.example.ai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class SpeechRecognizerState(
    val isListening: Boolean = false,
    val spokenText: String = "",
    val partialText: String = "",
    val soundLevel: Float = 0f, // 0 to 10 scale for audio visualizer
    val statusMessage: String = "माइक तैयार है",
    val isHandsFreeEnabled: Boolean = false,
    val lastRecognizedResult: String? = null,
    val errorMessage: String? = null
)

class SpeechToTextManager(
    private val context: Context,
    private val onFinalResult: (String) -> Unit
) {
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow(SpeechRecognizerState())
    val state: StateFlow<SpeechRecognizerState> = _state.asStateFlow()

    private var isHandsFreeLoopActive = false

    init {
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                    _state.value = _state.value.copy(statusMessage = "SpeechRecognizer तैयार है")
                } else {
                    _state.value = _state.value.copy(statusMessage = "स्पीच सर्विस उपलब्ध नहीं है")
                }
            } catch (e: Exception) {
                Log.e("SpeechToTextManager", "Initialization error", e)
            }
        }
    }

    private fun createListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _state.value = _state.value.copy(
                    isListening = true,
                    statusMessage = "बोलिए, मुनीम जी सुन रहे हैं... (Listening)",
                    errorMessage = null
                )
            }

            override fun onBeginningOfSpeech() {
                _state.value = _state.value.copy(
                    statusMessage = "आवाज़ दर्ज हो रही है..."
                )
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize dB to a smooth 0f..10f range for audio wave visualization
                val normalizedLevel = ((rmsdB + 2f) / 1.2f).coerceIn(0f, 10f)
                _state.value = _state.value.copy(soundLevel = normalizedLevel)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _state.value = _state.value.copy(
                    isListening = false,
                    soundLevel = 0f,
                    statusMessage = "पहचाना जा रहा है..."
                )
            }

            override fun onError(errorCode: Int) {
                val errorMsg = when (errorCode) {
                    SpeechRecognizer.ERROR_AUDIO -> "ऑडियो रिकॉर्डिंग में समस्या"
                    SpeechRecognizer.ERROR_CLIENT -> "क्लाइंट त्रुटि"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "माइक्रोफोन अनुमति नहीं है"
                    SpeechRecognizer.ERROR_NETWORK -> "इंटरनेट कनेक्शन चेक करें"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "नेटवर्क टाइमआउट"
                    SpeechRecognizer.ERROR_NO_MATCH -> "आवाज़ समझ नहीं आई, कृपया फिर बोलें"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "सर्विस व्यस्त है"
                    SpeechRecognizer.ERROR_SERVER -> "सर्वर त्रुटि"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "कोई आवाज़ सुनाई नहीं दी"
                    else -> "त्रुटि कोड: $errorCode"
                }

                _state.value = _state.value.copy(
                    isListening = false,
                    soundLevel = 0f,
                    statusMessage = errorMsg,
                    errorMessage = errorMsg
                )

                // If in hands-free mode for Usha ji & Kamal ji, auto restart after brief pause
                if (isHandsFreeLoopActive) {
                    mainHandler.postDelayed({
                        if (isHandsFreeLoopActive) {
                            startListening()
                        }
                    }, 1500)
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognized = matches?.firstOrNull()?.trim() ?: ""

                _state.value = _state.value.copy(
                    isListening = false,
                    spokenText = recognized,
                    partialText = "",
                    soundLevel = 0f,
                    statusMessage = if (recognized.isNotBlank()) "सफलतापूर्वक पहचाना गया" else "तैयार",
                    lastRecognizedResult = recognized
                )

                if (recognized.isNotBlank()) {
                    onFinalResult(recognized)
                }

                // In hands-free mode, resume listening after waiting for soundbox confirmation
                if (isHandsFreeLoopActive) {
                    mainHandler.postDelayed({
                        if (isHandsFreeLoopActive) {
                            startListening()
                        }
                    }, 2800)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val partial = matches?.firstOrNull() ?: ""
                if (partial.isNotBlank()) {
                    _state.value = _state.value.copy(
                        partialText = partial,
                        statusMessage = "सुन रहा हूँ: \"$partial\""
                    )
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun startListening() {
        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    initializeRecognizer()
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "hi-IN")
                    putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "बोलिए: जैसे 'रमेश को 500 जोड़ दो'")
                }

                speechRecognizer?.startListening(intent)
                _state.value = _state.value.copy(
                    isListening = true,
                    statusMessage = "मुनीम जी सुन रहे हैं..."
                )
            } catch (e: Exception) {
                Log.e("SpeechToTextManager", "Error starting listening", e)
                _state.value = _state.value.copy(
                    isListening = false,
                    statusMessage = "शुरू करने में समस्या: ${e.message}"
                )
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e("SpeechToTextManager", "Error stopping listening", e)
            }
            _state.value = _state.value.copy(isListening = false, soundLevel = 0f)
        }
    }

    fun toggleHandsFreeMode(enable: Boolean) {
        isHandsFreeLoopActive = enable
        _state.value = _state.value.copy(isHandsFreeEnabled = enable)
        if (enable) {
            startListening()
        } else {
            stopListening()
        }
    }

    fun resetState() {
        _state.value = _state.value.copy(
            spokenText = "",
            partialText = "",
            soundLevel = 0f,
            errorMessage = null,
            statusMessage = if (isHandsFreeLoopActive) "हैंड्स-फ्री मोड सक्रिय" else "माइक तैयार है"
        )
    }

    fun destroy() {
        isHandsFreeLoopActive = false
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (e: Exception) {
                Log.e("SpeechToTextManager", "Error destroying speech recognizer", e)
            }
        }
    }
}
