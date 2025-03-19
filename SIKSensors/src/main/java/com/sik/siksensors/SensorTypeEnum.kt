package com.sik.siksensors

import android.hardware.Sensor

/**
 * SensorTypeEnum 是一个枚举类，用于表示 Android 设备中常见的传感器类型。
 * 每个枚举值都对应于 android.hardware.Sensor 中定义的传感器常量，
 * 便于在代码中统一管理和调用各种传感器。
 */
enum class SensorTypeEnum(val type: Int) {

    /**
     * 加速度传感器
     * 用于测量设备在 x、y、z 轴上的加速度变化，
     * 常用于检测设备的运动状态或倾斜程度。
     */
    ACCELEROMETER(Sensor.TYPE_ACCELEROMETER),

    /**
     * 陀螺仪传感器
     * 用于测量设备的角速度，能够检测设备的旋转运动，
     * 常应用于游戏控制、虚拟现实和导航等场景。
     */
    GYROSCOPE(Sensor.TYPE_GYROSCOPE),

    /**
     * 磁场传感器
     * 用于测量设备所在环境中的磁场强度，
     * 可用于实现电子罗盘功能以辅助定位和导航。
     */
    MAGNETIC_FIELD(Sensor.TYPE_MAGNETIC_FIELD),

    /**
     * 光线传感器
     * 用于测量环境光线强度，
     * 例如自动调节屏幕亮度以适应不同光照条件。
     */
    LIGHT(Sensor.TYPE_LIGHT),

    /**
     * 重力传感器
     * 用于测量设备所受的重力加速度，
     * 通过滤除加速度传感器中的运动分量，提供纯粹的重力数据。
     */
    GRAVITY(Sensor.TYPE_GRAVITY),

    /**
     * 线性加速度传感器
     * 用于测量设备在排除重力后的运动加速度，
     * 适用于精确检测设备运动而不受重力干扰的场景。
     */
    LINEAR_ACCELERATION(Sensor.TYPE_LINEAR_ACCELERATION),

    /**
     * 旋转向量传感器
     * 用于测量设备的旋转状态，
     * 结合了加速度计与陀螺仪的数据，提供设备的旋转角度信息。
     */
    ROTATION_VECTOR(Sensor.TYPE_ROTATION_VECTOR),

    /**
     * 计步器传感器
     * 用于记录设备自开机以来的累计步数，
     * 常用于健康和运动追踪应用中。
     */
    STEP_COUNTER(Sensor.TYPE_STEP_COUNTER),

    /**
     * 步伐检测传感器
     * 用于检测用户的单步动作，
     * 通常与计步器结合使用，提供更细粒度的步行数据。
     */
    STEP_DETECTOR(Sensor.TYPE_STEP_DETECTOR)
}
