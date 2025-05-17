package com.sik.siksensors.vibrator

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import com.sik.sikcore.SIKCore

class LowSDKVersionVibrator : IVibrator {
    private val vibrator: Vibrator by lazy {
        SIKCore.getApplication().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    @SuppressLint("MissingPermission")
    override fun vibrate(pattern: LongArray, amplitudes: IntArray, mode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(pattern, amplitudes, mode)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(pattern, mode)
        }
    }

    @SuppressLint("MissingPermission")
    override fun vibrate(time: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(time, amplitude)
            vibrator.vibrate(vibrationEffect)
        } else {
            vibrator.vibrate(time)
        }
    }

    @SuppressLint("MissingPermission")
    override fun cancel() {
        vibrator.cancel()
    }

    @SuppressLint("MissingPermission")
    override fun hasVibrator(): Boolean {
        return vibrator.hasVibrator()
    }

    override fun vibratorIds(): IntArray {
        return intArrayOf(0) // 低版本没有多震动马达的概念
    }

    override fun setVibrator(vibratorId: Int) {
        // 低版本不支持多震动马达，忽略此操作
    }
}