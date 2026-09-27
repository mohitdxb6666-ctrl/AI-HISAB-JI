package com.example.ai

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

class SoundboxManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val hindiLocale = Locale("hi", "IN")
                val result = tts?.setLanguage(hindiLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.setLanguage(Locale.ENGLISH)
                }
                isTtsReady = true
            }
        }
    }

    fun playSoundboxAlert(amount: Double, customerName: String, isCredit: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            playDualChime()
            val spokenMessage = if (isCredit) {
                "AI हिसाब जी: ₹${amount.toInt()} $customerName के खाते में उधार जोड़े गए।"
            } else {
                "AI हिसाब जी: ₹${amount.toInt()} $customerName से प्राप्त हुए।"
            }
            speak(spokenMessage)
        }
    }

    fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "HISAAB_TTS")
        }
    }

    private fun playDualChime() {
        try {
            val sampleRate = 44100
            val duration1 = 0.12
            val duration2 = 0.25
            val totalDuration = duration1 + duration2
            val numSamples = (sampleRate * totalDuration).toInt()
            val buffer = ShortArray(numSamples)

            val freq1 = 880.0  // A5
            val freq2 = 1320.0 // E6

            val samples1 = (sampleRate * duration1).toInt()
            for (i in 0 until samples1) {
                val t = i.toDouble() / sampleRate
                val angle = 2.0 * Math.PI * freq1 * t
                val amplitude = sin(angle) * (1.0 - (i.toDouble() / samples1) * 0.2)
                buffer[i] = (amplitude * Short.MAX_VALUE * 0.5).toInt().toShort()
            }

            for (i in samples1 until numSamples) {
                val idx = i - samples1
                val samples2 = numSamples - samples1
                val t = idx.toDouble() / sampleRate
                val angle = 2.0 * Math.PI * freq2 * t
                val amplitude = sin(angle) * (1.0 - (idx.toDouble() / samples2))
                buffer[i] = (amplitude * Short.MAX_VALUE * 0.6).toInt().toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
        } catch (e: Exception) {
            // Ignored if sound synthesis fails on restricted container
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
