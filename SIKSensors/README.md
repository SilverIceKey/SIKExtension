# 传感器库

## 说明：主要用于调用指纹验证以及获取一些传感器的数据

## 使用方法：

## 方法介绍：

### [指纹工具类](./src/main/java/com/sik/siksensors/fingerprints/FingerPrintsUtils.kt)

```kotlin
//认证指纹 自定义配置文件或者使用FingerPrintsConfig.defaultConfig配置，默认使用系统弹窗
inline fun <reified T : FingerPrintsConfig> authenticateFingerprint(fingerConfig: T?, crossinline auth: (SensorErrorEnum) -> Unit)
```

### [传感器工具类](./src/main/java/com/sik/siksensors/SensorUtils.kt)

```kotlin
//注册传感器，支持重载，不传入lifecycleOwner需要自己取消注册，否则会根据生命周期取消注册
fun registerSensor(
    sensorTypeEnum: SensorTypeEnum,
    sensorEventListener: SensorEventListener,
    lifecycleOwner: LifecycleOwner? = null
): Sensor?
//取消注册传感器，支持重载
fun unregisterSensor(
    sensorEventListener: SensorEventListener? = null, sensor: Sensor? = null
)
```

### [传感器校准判断工具类](./src/main/java/com/sik/siksensors/SensorCalibrationUtils.kt)
```kotlin
/**
 * 判断传感器是否需要校准。
 *
 * @param sensor 传感器
 * @param accuracy 当前精度值
 * @return 是否需要校准
 */
fun needsCalibration(sensor: Sensor, accuracy: Int): Boolean

/**
 * 获取传感器的校准提示信息。
 *
 * @param sensor 传感器
 * @return 校准提示信息
 */
fun getCalibrationMessage(sensor: Sensor): String
```

### [传感器数据算法工具类](./src/main/java/com/sik/siksensors/SensorMathUtils.kt)
```kotlin
/**
 * 计算加速度的模。
 *
 * @param event 传感器事件
 * @return 加速度的模
 */
fun calculateAccelerationMagnitude(event: SensorEvent): Float

/**
 * 检测摇一摇事件
 *
 * @param event 传感器事件
 * @param threshold 摇一摇的阈值
 * @param lastShakeTime 上一次摇一摇的时间
 * @param shakeInterval 最小的摇一摇时间间隔
 * @return 是否检测到摇一摇事件
 */
fun detectShake(event: SensorEvent, threshold: Float, lastShakeTime: Long, shakeInterval: Long): Boolean
/**
 * 计算旋转矢量的欧拉角。
 *
 * @param event 传感器事件
 * @return 包含yaw, pitch, roll的数组
 */
fun calculateEulerAngles(event: SensorEvent): FloatArray
/**
 * 计算线性加速度的模。
 *
 * @param event 传感器事件
 * @return 线性加速度的模
 */
fun calculateLinearAccelerationMagnitude(event: SensorEvent): Float
/**
 * 检测步数变化。
 *
 * @param event 传感器事件
 * @return 当前步数
 */
fun detectStepCount(event: SensorEvent): Int
/**
 * 检测步数事件。
 *
 * @param event 传感器事件
 * @return 是否检测到步数事件
 */
fun detectStepEvent(event: SensorEvent): Boolean
```

