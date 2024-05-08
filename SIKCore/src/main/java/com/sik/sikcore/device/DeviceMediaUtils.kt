package com.sik.sikcore.device

import android.content.Context
import android.media.AudioManager
import android.os.Build
import com.sik.sikcore.SIKCore

/**
 * 设备媒体工具
 */
object DeviceMediaUtils {
    private val audioManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        SIKCore.getApplication().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * 设置音量
     */
    fun setAudioVolume(volumeType: Int, volume: Int) {
        audioManager.setStreamVolume(volumeType, volume, AudioManager.FLAG_SHOW_UI)
    }

    /**
     * 获取音量范围
     */
    fun getAudioVolume(volumeType: Int): Pair<Int, Int> {
        val maxVolume = audioManager.getStreamMaxVolume(volumeType)
        val minVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            audioManager.getStreamMinVolume(volumeType)
        } else {
            0
        }
        return minVolume to maxVolume
    }

    /**
     * 获取当前音量大小
     */
    fun getCurrentAudioVolume(volumeType: Int): Int {
        return audioManager.getStreamVolume(volumeType)
    }
}