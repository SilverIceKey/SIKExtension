# 核心库

## 说明：包含一些基本的例如时间工具类、文件工具类等一些常用工具类
[logback配置样例](./LOGBACK.MD)

## 使用方法：

需要在Manifest中声明以下权限:
```xml
<uses-permission android:name="android.permission.MANAGE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

## 方法介绍：

### [核心模块](./src/main/java/com/sik/sikcore/SIKCore.kt)

```kotlin
//初始化，因为有不少地方会调用Context建议在使用库之前初始化
fun init(application: Application)
//获取application 一般为内部使用
fun getApplication(): Application
```

### [全局异常捕捉](./src/main/java/com/sik/sikcore/crash/GlobalCrashCatch.kt)

```kotlin
//全局异常处理初始化
fun init(context: Context): GlobalCrashCatch
//设置全局异常处理监听供第三方调用
fun setGlobalCrashHandlerListener(globalCrashHandleCallback: GlobalCrashHandleCallback): GlobalCrashCatch
```

### [时间工具类](./src/main/java/com/sik/sikcore/date/TimeUtils.kt)

```kotlin
//计算到目前的时间 返回为刚刚，60分钟前等，默认刚刚为低于300秒，如需修改请设置 timeForCurrent 参数
fun getTimeIntervalOfCur(time: String, timeFormatter: DateFormat): String
fun getTimeIntervalOfCur(time: Long): String
//判断是否是今天
fun isToday(date: String, dateFormat: String): Boolean
//时间偏移天数
fun offsetDay(offsetValue: Int, date: Date): Date
//时间偏移小时
fun offsetHour(offsetValue: Int, date: Date): Date
//时间偏移分钟
fun offsetMin(offsetValue: Int, date: Date): Date
//时间偏移秒
fun offsetSec(offsetValue: Int, date: Date): Date
//获取当前时间
fun now(): Date
//获取当前时间字符串
fun nowString(dateFormat: String): String
//获取今天日期 转换出的时间知道日期，默认时分秒为0，0，0
fun today(): Date
//判断日期是否在某日期之前
fun isTimeBefore(realDate: Date, referenceDate: Date): Boolean
fun isTimeBefore(realDate: String, referenceDate: String): Boolean
//判断日期是否在今天之前
fun isTimeBeforeToday(realDate: Date): Boolean
//时间仅保留日期返回Date
fun getDateOnly(date: Date): Date
//计算时间天数
fun calcDayNum(beforeDate: Date, afterDate: Date): Long
fun calcDayNum(beforeDate: String, afterDate: String): Long
//通过时间格式转化时间
fun dateFormatToDateFormat(sourceDateFormat: String,targetDateFormat: String,date: String): String
//倒计时输出分钟和秒 例:01:30
fun getTimeStr(timeMillis: Long): String
//计算指定时间到现在的时间差
fun calcOffsetTime(sourceTime: String, timeDateFormat: String): Int
```

### [日程管理工具类](./src/main/java/com/sik/sikcore/date/ScheduleManagerUtils.kt)

```kotlin
//添加一次性任务 initialDelay延迟时间 workId唯一日程id
inline fun <reified T : Worker> addOneTimeWork(context: Context, initialDelay: Long = 0L, workId: Long)
//取消日程 workId唯一日程id
fun cancel(context: Context, workId: Long)
```

### [设备相关工具类](./src/main/java/com/sik/sikcore/device/DeviceUtils.kt)

```kotlin
//获取设备SN号码,有可能返回空字符串
fun getSN(): String
//获得设备硬件标识
fun getDeviceId(context: Context): String
```

### [文件工具类](./src/main/java/com/sik/sikcore/file/FileUtils.kt)

```kotlin
//读取文件转String
fun loadFileAsString(filePath: String?): String
//文件是否存在
fun isFileExists(filePath: String): Boolean
//创建的文件如果存在则不创建
fun createOrExistsFile(file: String)
```

### [显示器状态监听](./src/main/java/com/sik/sikcore/receivers/ScreenStatusReceiver.kt)

```kotlin
//注册屏幕开关接收器
fun registerReceiver(context: Context?)
//取消注册屏幕开关接收器
fun unRegisterReceiver(context: Context?)
```

### [反射工具类](./src/main/java/com/sik/sikcore/reflex/ReflexUtils.kt)

```kotlin
//优先调用，之后会使用同一个类
fun getClassInstance(className: String): ReflexUtils
//反射执行方法
fun invoke(classInstance:Any,methodName: String,vararg params: Any,onInvokeCallback: (Any?) -> Unit): ReflexUtils
//反射获取数据
fun getData(fieldName: String, srcObj: Any, onGetCallback: (Any?) -> Unit): ReflexUtils
//反射设置数据
fun setData(fieldName: String,srcObj: Any,targetObj: Any,onSetCallback: (Any?) -> Unit): ReflexUtils
```

### [命令行工具类](./src/main/java/com/sik/sikcore/shell/ShellUtils.kt)

```kotlin
//执行命令
fun execCmd(command: String,isRoot: Boolean = false,env: Array<String> = arrayOf()): ShellResult
```

### [正则工具类](./src/main/java/com/sik/sikcore/string/RegexUtils.kt)

```kotlin
//返回正则匹配字符串
fun getMatches(regex: String, input: CharSequence?): List<String>

/**
 * 使用正则表达式替换匹配的字符串。
 *
 * @param regex 正则表达式.
 * @param input 输入字符串.
 * @param replacement 替换内容.
 * @return 替换后的字符串
 */
fun replaceMatches(regex: String, input: CharSequence?, replacement: String): String
/**
 * 使用正则表达式替换匹配的字符串。
 *
 * @param regex 正则表达式.
 * @param input 输入字符串.
 * @param replacement 替换内容.
 * @return 替换后的字符串
 */
fun replaceMatches(regex: String, input: CharSequence?, replacement: String): String
```

### [字符串工具类](./src/main/java/com/sik/sikcore/string/StringUtils.kt)

```kotlin
//提取中文返回 使用正则
fun getAllZH(input: String?): String
```

### [压缩包工具类](./src/main/java/com/sik/sikcore/zip/ZipUtils.kt)

```kotlin
/**
 * 压缩文件
 *
 * @param srcFiles 源文件列表
 * @param destFile 目标文件
 * @param zipListener 压缩监听
 */
fun zip(vararg srcFiles: File, destFile: File, zipListener: ZipListener? = null)
fun zip(vararg srcFiles: String, destFile: String, zipListener: ZipListener? = null)
/**
* 解压文件
*
* @param srcFile 源文件
* @param destFolder 目标目录
* @param zipListener 解压监听
*/
fun unzip(srcFile: File, destFolder: File, zipListener: ZipListener? = null)
fun unzip(srcFile: String, destFolder: String, zipListener: ZipListener? = null)
```

### [数组扩展函数](./src/main/java/com/sik/sikcore/extension/ArrayExtension.kt)

```kotlin
//对Array进行扩展，Array调用getString可以转化为[参数1,参数2]形式文本
fun <T : Comparable<T>> Array<out T>.getString(): String
//list转mutableList
fun <T : Comparable<T>> List<T>.toMutableList(): MutableList<T>
```

### [数字扩展函数](./src/main/java/com/sik/sikcore/extension/NumberExtension.kt)

```kotlin
//文件大小单位转换
fun Long.formatBytes(): String
```
### [文件扩展函数](./src/main/java/com/sik/sikcore/extension/FileExtension.kt)

```kotlin
//获取文件的输出流
fun File.outputStream(): FileOutputStream
//文件路径直接返回输出流
fun String.fileOutputStream(): FileOutputStream
//文件路径直接返回文件
fun String.file(): File
//文件路径直接返回输入流
fun String.fileInputStream(): FileInputStream
//判断文件是否存在
fun String.exists(): Boolean
//如果文件存在则删除文件
fun String.existsDelete(): Boolean
//判断文件夹是否存在，不存在则创建
fun String.existsAndCreateFolder()
//文件不存的情况下创建文件,存在的情况下直接返回true
fun String.createNewFile(): Boolean
//文件路径写入数据
fun String.write(data: ByteArray)
//文件路径获取文本数据
fun String.getData(): String
```

### [界面扩展函数](./src/main/java/com/sik/sikcore/extension/ViewExtension.kt)

```kotlin
//在View渲染完之后执行的操作
fun View.doAfterRendered(task: () -> Unit = {})
//Dp2px
fun Number.dp2px(): Float
//Sp2px
fun Number.sp2px(): Float
//px2dp
fun Number.toDp(): Float
```

### [对象扩展函数](./src/main/java/com/sik/sikcore/extension/ObjectExtension.kt)

```kotlin
// 扩展Any?（任何可为空的类型）
fun Any?.isNullOrEmpty(): Boolean
//任何类型转为json，如果为空默认为对象,可以指定数组
fun Any?.toJson(isJsonObject: Boolean = true): String
```

### [颜色工具类](./src/main/java/com/sik/sikcore/color/ColorUtils.kt)

```kotlin
//colorInt转16进制
fun colorIntToHex(@ColorInt colorInt: Int): String
```

### [Bean工具类](./src/main/java/com/sik/sikcore/data/BeanUtils.kt)

```kotlin
//使用反射进行相同字段的数据复制
inline fun <reified T> copyData(source: Any, target: Any): T
//使用反射进行相同字段的数据复制
fun copyData(source: Any, target: Any)
```

### [转换工具类](https://github.com/SilverIceKey/SIKExtension/blob/master/SIKCore/src/main/java/com/sik/sikcore/data/ConvertUtils.kt)

```kotlin
//byte数组转16进制字符串
fun bytesToHex(byteArray: ByteArray): String
//16进制字符串转byte数组
fun hexToBytes(hex: String): ByteArray
//byte数组转Base64字符串
fun bytesToBase64String(byteArray: ByteArray): String
//base64字符串转byte数组
fun base64StringToBytes(base64Str:String):ByteArray
```

### [全局参数临时存储池](https://github.com/SilverIceKey/SIKExtension/blob/master/SIKCore/src/main/java/com/sik/sikcore/data/GlobalDataTempStore.kt)

```kotlin
//保存数据
fun saveData(key: String, value: Any)
//获取数据 默认获取完数据就删除
fun getData(key: String, isDeleteAfterGet: Boolean = true): Any?
```

### [MMKV扩展函数](./src/main/java/com/sik/sikcore/extension/MMKVExtension.kt)

```kotlin
//保存数据，支持 Boolean、Float、Int、Long、String、ByteArray、Set<String>
inline fun <reified T> MMKV.saveMMKVData(key: String, value: T)
//获取数据
inline fun <reified T> MMKV.getMMKVData(key: String, defaultValue: T): T
//默认 mmkv 保存
inline fun <reified T> String.saveMMKVData(value: T)
//默认 mmkv 获取
inline fun <reified T> String.getMMKVData(defaultValue: T): T
```

### [日程管理工具类](https://github.com/SilverIceKey/SIKExtension/blob/master/SIKCore/src/main/java/com/sik/sikcore/date/ScheduleManagerUtils.kt)

```kotlin
//添加一次性任务
fun <reified T : Worker> addOneTimeWork(context: Context, initialDelay: Long = 0L, workId: Long)
//取消任务
fun cancel(context: Context, workId: Long)
```

### [位类型操作判断工具](./src/main/java/com/sik/sikcore/bit/BitTypeUtils.kt)

```kotlin
//目标类型是否存在
fun hasType(originType: Int, targetType: Int): Boolean
//添加类型
fun addType(originType: Int, targetType: Int): Int
//删除类型
fun deleteType(originType: Int, targetType: Int): Int
```



### [动画扩展函数](./src/main/java/com/sik/sikcore/anim/AnimExtension.kt)

```kotlin
/**
 * Anim
 * 获取动画
 * @param animConfig 动画配置
 * @param createStart 创建完成之后马上执行
 * @return
 */
fun View.anim(animConfig: AnimConfig, createStart: Boolean = true): ValueAnimator
```


### [设备媒体工具](./src/main/java/com/sik/sikcore/device/DeviceMediaUtils.kt)

```kotlin
/**
 * 设置音量
 */
fun setAudioVolume(volumeType: Int, volume: Int)

/**
 * 获取音量范围
 */
fun getAudioVolume(volumeType: Int): Pair<Int, Int>

/**
 * 获取当前音量大小
 */
fun getCurrentAudioVolume(volumeType: Int): Int
```

### [Activity追踪](./src/main/java/com/sik/sikcore/activity/ActivityTracker.kt)

```kotlin
/**
 * 获取当前的Activity
 */
fun getCurrentActivity(): Activity? 

可以在activity中增加下面的方法来监听是否开启夜间模式监听
@NightModeChangeListener
fun nightModeChangeListener(nightMode: Int) {}

再Activity的Class上面增加
@SecureActivity可以在进入Activity时自动启动安全模式，关闭是自动关闭安全模式
```

### [密码生成器](./src/main/java/com/sik/sikcore/generator/PasswordGenerator.kt)

```kotlin
/**
 * 生成指定长度的密码
 * @param length 密码长度
 * @param useUpper 是否使用大写字母
 * @param useLower 是否使用小写字母
 * @param useDigits 是否使用数字
 * @param useSpecial 是否使用特殊字符
 * @return 生成的密码
 */
fun generatePassword(
    length: Int,
    useUpper: Boolean = true,
    useLower: Boolean = true,
    useDigits: Boolean = true,
    useSpecial: Boolean = true
): String 
```

### [震动马达工具类](./com/sik/sikcore/device/VibratorUtils.kt)

```kotlin
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
 * 检查权限
 */
fun checkPermission(callback: PermissionUtils.PermissionCallback)
/**
 * 频率震动
 */
fun vibrate(vibrateMode: VibrateMode)
/**
 * 取消震动
 */
fun cancel()
/**
 * 是否有震动器
 */
fun hasVibrator(): Boolean
/**
 * 获取震动马达列表
 */
fun vibratorIds(): IntArray
/**
 * 设置震动马达
 */
fun setVibrator(vibratorId: Int)
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
```

### 更新日志
- 2025-06：整理文档格式并补充说明。

