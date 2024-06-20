package com.sik.siksensors

import android.hardware.SensorEvent
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * 传感器数据算法工具类
 */
object SensorMathUtils {

    /**
     * 计算加速度的模。
     *
     * @param event 传感器事件
     * @return 加速度的模
     */
    fun calculateAccelerationMagnitude(event: SensorEvent): Float {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        return sqrt(x * x + y * y + z * z)
    }

    /**
     * 检测摇一摇事件
     *
     * @param event 传感器事件
     * @param threshold 摇一摇的阈值
     * @param lastShakeTime 上一次摇一摇的时间
     * @param shakeInterval 最小的摇一摇时间间隔
     * @return 是否检测到摇一摇事件
     */
    fun detectShake(event: SensorEvent, threshold: Float, lastShakeTime: Long, shakeInterval: Long): Boolean {
        val magnitude = calculateAccelerationMagnitude(event)
        val currentTime = System.currentTimeMillis()
        return magnitude > threshold && currentTime - lastShakeTime > shakeInterval
    }

    /**
     * 计算旋转矢量的欧拉角。
     *
     * @param event 传感器事件
     * @return 包含yaw, pitch, roll的数组
     */
    fun calculateEulerAngles(event: SensorEvent): FloatArray {
        val qw = event.values[3]
        val qx = event.values[0]
        val qy = event.values[1]
        val qz = event.values[2]

        val yaw = atan2(2.0f * (qx * qy + qw * qz), qw * qw + qx * qx - qy * qy - qz * qz)
        val pitch = atan2(2.0f * (qy * qz + qw * qx), qw * qw - qx * qx - qy * qy + qz * qz)
        val roll = atan2(2.0f * (qx * qz + qw * qy), qw * qw - qx * qx + qy * qy - qz * qz)

        return floatArrayOf(yaw, pitch, roll)
    }

    /**
     * 计算线性加速度的模。
     *
     * @param event 传感器事件
     * @return 线性加速度的模
     */
    fun calculateLinearAccelerationMagnitude(event: SensorEvent): Float {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        return sqrt(x * x + y * y + z * z)
    }

    /**
     * 检测步数变化。
     *
     * @param event 传感器事件
     * @return 当前步数
     */
    fun detectStepCount(event: SensorEvent): Int {
        return event.values[0].toInt()
    }

    /**
     * 检测步数事件。
     *
     * @param event 传感器事件
     * @return 是否检测到步数事件
     */
    fun detectStepEvent(event: SensorEvent): Boolean {
        return event.values[0] == 1.0f
    }
}
