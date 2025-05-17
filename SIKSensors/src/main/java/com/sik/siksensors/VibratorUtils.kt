package com.sik.siksensors

import android.Manifest
import android.os.Build
import com.sik.siksensors.vibrator.HighSDKVersionVibrator
import com.sik.siksensors.vibrator.IVibrator
import com.sik.siksensors.vibrator.LowSDKVersionVibrator

/**
 * 震动马达工具
 *
 * 调用前请现在AndroidManifest中声明以下权限
 * @see Manifest.permission.VIBRATE
 */
object VibratorUtils {
    /**
     * 震动权限
     */
    private const val VIBRATE_PERMISSION = Manifest.permission.VIBRATE

    /**
     * 高版本和低版本有不同的实现
     */
    private val vibrator: IVibrator by lazy {
        if (isHighSDKVersion()) {
            HighSDKVersionVibrator()
        } else {
            LowSDKVersionVibrator()
        }
    }


    /**
     * 震动频率，
     * 奇数下标为开启，
     * 偶数下标为停止，
     * 单位：ms
     */
    val pattern by lazy {
        mutableListOf<Long>()
    }

    /**
     * 震动强度，
     * 奇数下标为开启，
     * 偶数下标为停止，
     * 单位：0-255
     */
    val amplitudes by lazy {
        mutableListOf<Int>()
    }

    /**
     * 震动一次
     */
    @JvmOverloads
    fun vibrate(time: Long, amplitude: Int = 255) {
        vibrator.vibrate(time, amplitude)
    }

    /**
     * 频率震动
     */
    fun vibrate(vibrateMode: VibrateMode) {
        vibrator.vibrate(pattern.toLongArray(), amplitudes.toIntArray(), vibrateMode.getMode())
    }

    /**
     * 取消震动
     */
    fun cancel() {
        vibrator.cancel()
    }

    /**
     * 是否有震动器
     */
    fun hasVibrator(): Boolean {
        return vibrator.hasVibrator()
    }

    /**
     * 获取震动马达列表
     */
    fun vibratorIds(): IntArray {
        return vibrator.vibratorIds()
    }

    /**
     * 设置震动马达
     */
    fun setVibrator(vibratorId: Int) {
        vibrator.setVibrator(vibratorId)
    }

    /**
     * 是否是高版本
     */
    private fun isHighSDKVersion(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    }

    /**
     * 震动模式
     */
    enum class VibrateMode(private val mode: Int) {
        ONCE(-1),
        INFINITE(0);

        fun getMode(): Int {
            return mode
        }
    }
}