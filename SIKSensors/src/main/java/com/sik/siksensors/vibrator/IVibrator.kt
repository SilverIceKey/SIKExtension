package com.sik.siksensors.vibrator

/**
 * 震动马达接口
 */
interface IVibrator {
    /**
     * 频率+模式控制震动
     */
    fun vibrate(pattern: LongArray, amplitudes: IntArray, mode: Int)

    /**
     * 一次性震动
     */
    fun vibrate(time: Long, amplitude: Int)

    /**
     * 暂停震动
     */
    fun cancel()

    /**
     * 是否有震动器
     */
    fun hasVibrator(): Boolean

    /**
     * 震动器列表
     */
    fun vibratorIds(): IntArray

    /**
     * 设置震动马达
     */
    fun setVibrator(vibratorId: Int)
}