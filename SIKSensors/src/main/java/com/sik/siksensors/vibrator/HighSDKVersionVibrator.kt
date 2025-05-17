package com.sik.siksensors.vibrator

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresApi
import com.sik.sikcore.SIKCore

@RequiresApi(Build.VERSION_CODES.S)
class HighSDKVersionVibrator : IVibrator {
    private val vibratorManager: VibratorManager by lazy {
        (SIKCore.getApplication()
            .getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).apply {

        }
    }

    private var currentVibrator: Vibrator = vibratorManager.defaultVibrator

    @SuppressLint("MissingPermission")
    override fun vibrate(pattern: LongArray, amplitudes: IntArray, mode: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(pattern, amplitudes, mode)
            currentVibrator.vibrate(vibrationEffect)
        } else {
            currentVibrator.vibrate(pattern, mode)
        }
    }

    @SuppressLint("MissingPermission")
    override fun vibrate(time: Long,amplitude:Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createOneShot(time, amplitude)
            currentVibrator.vibrate(vibrationEffect)
        } else {
            currentVibrator.vibrate(time)
        }
    }

    @SuppressLint("MissingPermission")
    override fun cancel() {
        currentVibrator.cancel()
    }

    override fun hasVibrator(): Boolean {
        return vibratorManager.vibratorIds.isNotEmpty()
    }

    override fun vibratorIds(): IntArray {
        return vibratorManager.vibratorIds
    }

    override fun setVibrator(vibratorId: Int) {
        currentVibrator = vibratorManager.getVibrator(vibratorId)
    }
}