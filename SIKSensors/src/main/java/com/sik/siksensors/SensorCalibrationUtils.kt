package com.sik.siksensors

import android.hardware.Sensor
import android.hardware.SensorManager

/**
 * 传感器校准判断工具类
 */
object SensorCalibrationUtils {

    /**
     * 判断传感器是否需要校准。
     *
     * @param sensor 传感器
     * @param accuracy 当前精度值
     * @return 是否需要校准
     */
    fun needsCalibration(sensor: Sensor, accuracy: Int): Boolean {
        return when (sensor.type) {
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD,
            Sensor.TYPE_LIGHT,
            Sensor.TYPE_GRAVITY,
            Sensor.TYPE_LINEAR_ACCELERATION,
            Sensor.TYPE_ROTATION_VECTOR,
            Sensor.TYPE_STEP_COUNTER,
            Sensor.TYPE_STEP_DETECTOR -> accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE
            else -> false
        }
    }

    /**
     * 获取传感器的校准提示信息。
     *
     * @param sensor 传感器
     * @return 校准提示信息
     */
    fun getCalibrationMessage(sensor: Sensor): String {
        return when (sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> "加速度计需要校准。请平放设备并旋转几次。"
            Sensor.TYPE_GYROSCOPE -> "陀螺仪需要校准。请平放设备并旋转几次。"
            Sensor.TYPE_MAGNETIC_FIELD -> "磁力计需要校准。请画出8字形以校准。"
            Sensor.TYPE_LIGHT -> "光线传感器通常不需要校准。"
            Sensor.TYPE_GRAVITY -> "重力传感器通常不需要校准。"
            Sensor.TYPE_LINEAR_ACCELERATION -> "线性加速度传感器通常不需要校准。"
            Sensor.TYPE_ROTATION_VECTOR -> "旋转矢量传感器通常不需要校准。"
            Sensor.TYPE_STEP_COUNTER -> "计步器通常不需要校准。"
            Sensor.TYPE_STEP_DETECTOR -> "步数检测器通常不需要校准。"
            else -> "传感器需要校准。"
        }
    }
}
