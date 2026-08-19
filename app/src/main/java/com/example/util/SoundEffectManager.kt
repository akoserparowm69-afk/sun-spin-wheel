package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

/**
 * Safe sound manager for wheel ticks and winner celebration sounds.
 * Handles missing audio devices, null contexts, and exceptions gracefully without crashing.
 */
class SoundEffectManager(context: Context) {
    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 60)
        } catch (e: Throwable) {
            Log.w("SoundEffectManager", "Unable to initialize ToneGenerator: ${e.message}")
            toneGenerator = null
        }
    }

    fun playTick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 35)
        } catch (e: Throwable) {
            Log.w("SoundEffectManager", "Error playing tick: ${e.message}")
        }
    }

    fun playWin() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
        } catch (e: Throwable) {
            Log.w("SoundEffectManager", "Error playing win sound: ${e.message}")
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
        } catch (e: Throwable) {
            Log.w("SoundEffectManager", "Error releasing ToneGenerator: ${e.message}")
        } finally {
            toneGenerator = null
        }
    }
}
