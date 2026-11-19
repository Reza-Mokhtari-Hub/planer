package com.example.util

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SoundFeedbackManager {

    private val scope = CoroutineScope(Dispatchers.Default)

    fun playCompleteChime(enabled: Boolean) {
        if (!enabled) return
        scope.launch {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 45)
                tg.startTone(ToneGenerator.TONE_PROP_ACK, 120)
                delay(140)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 160)
                delay(200)
                tg.release()
            } catch (e: Exception) {
                Log.w("SoundFeedback", "Audio feedback unavailable", e)
            }
        }
    }

    fun playPop(enabled: Boolean) {
        if (!enabled) return
        scope.launch {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 35)
                tg.startTone(ToneGenerator.TONE_PROP_BEEP, 75)
                delay(100)
                tg.release()
            } catch (e: Exception) {
                Log.w("SoundFeedback", "Audio feedback unavailable", e)
            }
        }
    }

    fun playDrop(enabled: Boolean) {
        if (!enabled) return
        scope.launch {
            try {
                val tg = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 40)
                tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 100)
                delay(130)
                tg.release()
            } catch (e: Exception) {
                Log.w("SoundFeedback", "Audio feedback unavailable", e)
            }
        }
    }
}
