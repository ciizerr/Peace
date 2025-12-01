package com.nami.peace.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Helper class for providing haptic feedback for notification actions.
 * Implements Requirement 14.7: Add haptic feedback for actions
 */
@Singleton
class HapticFeedbackHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    
    /**
     * Provides haptic feedback for completing a task.
     * Uses a success pattern: short-long-short vibration.
     */
    fun vibrateForComplete() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 50, 50, 100, 50, 50)
            val amplitudes = intArrayOf(0, 100, 0, 150, 0, 100)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 50, 50, 100, 50, 50), -1)
        }
    }
    
    /**
     * Provides haptic feedback for snoozing a task.
     * Uses a gentle double-tap pattern.
     */
    fun vibrateForSnooze() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timings = longArrayOf(0, 30, 100, 30)
            val amplitudes = intArrayOf(0, 80, 0, 80)
            val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 30, 100, 30), -1)
        }
    }
    
    /**
     * Provides haptic feedback for dismissing a task.
     * Uses a single firm tap.
     */
    fun vibrateForDismiss() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
}
