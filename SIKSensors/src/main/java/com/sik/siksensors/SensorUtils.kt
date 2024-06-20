package com.sik.siksensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.sik.sikcore.SIKCore

/**
 * 传感器工具类，用于注册和管理传感器事件。
 *
 */
object SensorUtils {
    /**
     * 传感器管理器
     */
    private val sensorManager: SensorManager by lazy {
        SIKCore.getApplication().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    /**
     * 注册传感器
     */
    @JvmStatic
    @JvmOverloads
    fun registerSensor(
        sensorTypeEnum: SensorTypeEnum,
        sensorEventListener: SensorEventListener,
        lifecycleOwner: LifecycleOwner? = null
    ): Sensor? {
        return sensorManager.getDefaultSensor(sensorTypeEnum.type)?.let { sensor ->
            sensorManager.registerListener(
                sensorEventListener,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            lifecycleOwner?.lifecycle?.addObserver(observer = object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when {
                        event.targetState == Lifecycle.State.DESTROYED -> {
                            sensorManager.unregisterListener(sensorEventListener, sensor)
                        }
                    }
                }
            })
            return sensor
        }
    }

    /**
     * 取消传感器的注册。
     */
    @JvmStatic
    @JvmOverloads
    fun unregisterSensor(
        sensorEventListener: SensorEventListener? = null, sensor: Sensor? = null
    ) {
        if (sensor == null) {
            sensorManager.unregisterListener(sensorEventListener)
        } else {
            sensorManager.unregisterListener(sensorEventListener, sensor)
        }
    }
}
