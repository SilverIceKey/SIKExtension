# 扩展介绍

## Kotlin相关扩展
- ### [ArrayExtension](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/kotlin/ArrayExtension.kt)
```
getString() //对Array进行扩展，Array调用getString可以转化为[参数1,参数2]形式文本

toMutableList() //list转mutableList
```
- ### [FileExtension](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/kotlin/FileExtension.kt)
```
emptyOutputStream() //为文件创建一个输出流
```

## 全局异常捕捉相关
- ### [GlobalCrashCatch](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/crash/GlobalCrashCatch.kt)
```
init(context: Context) //全局异常捕捉初始化

setGlobalCrashHandlerListener(globalCrashHandleCallback: GlobalCrashHandleCallback) //设置异常回调
```

## 时间处理相关
- ### [TimeUtil](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/date/TimeUtil.kt)
```
//计算输入时间到当前时间,默认输入格式:yyyy-MM-dd HH:mm
getTimeIntervalofCur(time: String, timeFormatter: DateFormat): String

//计算输入时间到当前时间时间戳，单位:秒
getTimeIntervalofCur(time: Long): String

//判断输入日期是否是今天,默认时间格式：yyyy-MM-dd
isToday(date: String, dateFormat: String): Boolean

//计算出偏移后的时间，默认当天开始偏移,偏移单位：天
offsetDay(offsetValue: Int, date: Date): Date

//计算出偏移后的时间，默认当天开始偏移,偏移单位：小时
offsetHour(offsetValue: Int, date: Date): Date

//计算出偏移后的时间，默认当天开始偏移,偏移单位：分钟
offsetMin(offsetValue: Int, date: Date): Date

//返回当天时间
now(): Date

//返回当前时间字符串，默认时间格式：yyyy-MM-dd
nowString(dateFormat: String): String

//返回今天0点时间
today(): Date

//判断日期是否在某日期之前
isTimeBefore(realDate: Date, referenceDate: Date): Boolean

//判断日期是否在某日期之前
isTimeBefore(realDate: String, referenceDate: String): Boolean

//判断日期是否在今天之前
isTimeBeforeToday(realDate: Date): Boolean

//判断日期是否在今天之前
isTimeBeforeToday(realDate: String): Boolean

//时间仅保留日期返回Date
getDateOnly(date: Date): Date

//计算时间天数
calcDayNum(beforeDate: Date, afterDate: Date): Long

//计算时间天数
calcDayNum(beforeDate: String, afterDate: String): Long

//通过时间格式转化时间
dateFormatToDateFormat(sourceDateFormat: String,targetDateFormat: String,date: String): String

//倒计时输出分钟和秒 例:01:30
getTimeStr(timeMillis: Long): String
```

## 类属性注释注解相关
- ### [ExplainUtils](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/explain/ExplainUtils.kt)
```
使用方法：
类属性上添加
@Explain(explainValue = "需要显示的注解")

getExplainValueWithKey(clazz: Class<T>, key: String): String//根据属性获取类里的介绍说明

getExplainValues(clazz: Class<T>): Map<String, String>//获取类里所有介绍说明
```

## 网络相关
- ### [RetrofitClient](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/net/RetrofitClient.kt)

当前库的依赖为仅本库编译使用，app中请重新依赖

```
compileOnly('com.squareup.retrofit2:retrofit:2.9.0')
compileOnly('com.squareup.retrofit2:converter-gson:2.9.0')
compileOnly('com.squareup.retrofit2:converter-scalars:2.9.0')
compileOnly('com.squareup.retrofit2:adapter-rxjava2:2.9.0')
compileOnly('com.squareup.okhttp3:logging-interceptor:4.9.2')
compileOnly('io.netty:netty-all:4.1.70.Final')
```

```
setApplicationContext(context: Context?)//设置Retrofit全局Context

defaultConfig(retrofitConfig: RetrofitConfig)//设置默认配置

config(retrofitConfig: RetrofitConfig)//设置临时配置

addUpdateToken(updateToken: () -> Unit, tag: String? = defaultConfigTAG)//添加token刷新接口,如果需要的话

addInterceptor(interceptor: Interceptor?)//添加拦截器

addNetworkInterceptor(interceptor: Interceptor?)//添加网络拦截器

createService(service: Class<T>,tag: String? = defaultConfigTAG): T//返回请求接口实例

addDefaultHeader(key: String?, value: String?,tag: String = defaultConfigTAG!!)//添加默认请求头

addDefaultHeader(headers: Map<String, String>?,tag: String = defaultConfigTAG!!)//批量添加默认请求头

addDefaultParams(key: String, value: String,tag: String = defaultConfigTAG!!)//添加默认参数

addDefaultParams(params: Map<String, String>?,tag: String = defaultConfigTAG!!)//批量添加默认参数
```
- ### [RetrofitConfig](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/net/RetrofitConfig.kt)
继承配置用于配置多种请求参数，基本使用覆写方法来修改配置
```
RetrofitConfig(val baseUrl: String)//获取基础host

connectTimeout()//获取连接超时时间

readTimeout()//获取读取超时时间

writeTimeout()//获取写入超时时间

proxyIPAddr()//获取代理ip

proxyPort()//获取代理端口

proxyType()//获取代理类型

proxyUserName()//代理用户名

proxyPassword()//代理密码

defaultHeaders(): Map<String, String>//默认头部

defaultParams(): Map<String, String>//默认参数

isTokenShouldUpdate(): Boolean//是否需要刷新token,自定义规则
```
- ### [EMQXHelper](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/net/mqtt/EMQXHelper.kt)
```
init(mqttConfig: EMQXConfig = emqxConfig)//初始化EMQX

subscribe(topic: String?, qos: Int = 2)//订阅主题

unSubscribe(topic: String?)//取消主题订阅

release()//释放mqtt客户端
```
- ### [EMQXConfig](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/net/mqtt/EMQXConfig.kt)
继承配置用于配置多种请求参数，基本使用覆写方法来修改配置
```
EMQXConfig(val brokenUrl: String, val topic: String)//初始化配置携带mqtt地址和订阅

username//重写用户名

password//重写密码

getMemoryPersistence(): MemoryPersistence//获取持久化内存配置

qos():Int//重写qos规则

getMqttConnectOptions(): MqttConnectOptions//获取连接配置

getMqttCallback(): EMQXHelper.EMQXCallback//获取EMQX回调
```
- ### [NetUtil](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/net/NetUtil.kt)
```
getMacAddress(): String?//获取MAC地址（有网口的前提下）

getWifiName(): String//获取当前wifi名称

connectToWifi(ssid: String, password: String = "")//连接到指定wifi
```
## 文件相关
- ### [FileUtil](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/file/FileUtil.kt)
```
loadFileAsString(filePath: String?)//读取文件转String
```

## 图像相关工具  
- ### [MatrixUtils](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/image/MatrixUtils.kt)
```
//缩放，缩放之后位置不移动
scale(matrix: Matrix, scale: Float)
```

## 设备相关工具类
- ### [DeviceUtil](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/device/DeviceUtil.kt)
```
getSN(): String//获取设备SN号码

getDeviceId(context: Context): String//获得设备硬件标识

getAndroidId(context: Context): String//获得设备的AndroidId

getSERIAL(): String//获得设备序列号（如：WTK7N16923005607）, 个别设备无法获取

getDeviceUUID(): String//获得设备硬件uuid，使用硬件信息，计算出一个随机数
```
## 显示器状态监听
- ### [ScreenStatusReceiver](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/receivers/ScreenStatusReceiver.kt)
```
registerReceiver(context: Context?)//注册接收器

unRegisterReceiver(context: Context?)//取消注册接收器
```
## 加密工具类 本工具采用jni方式进行加密
- ### [EncryptUtil](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/encrypt/EncryptUtil.kt)
```
AESEncode(key:String,content:String):String//AES加密(AES_ECB_PKCS5)

AESDecode(key:String,content:String):ByteArray//AES解密(AES_ECB_PKCS5)

MD5Encode(content: String):String//MD5加密
```
## EventBus相关类
- ### [BusModel](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/eventbus/BusModel.kt)
本类用于EventBus传输状态数据，后续可能会新增泛型数据传输

## 反射相关
- ### [ReflexUtils](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/reflex/ReflexUtils.kt)
```
//优先调用，之后会使用同一个类
getClassInstance(className: String): ReflexUtils

//反射执行方法
invoke(classInstance:Any = clazz.newInstance(),methodName: String,vararg params: Any,onInvokeCallback: (Any?) -> Unit): ReflexUtils

反射获取数据
srcObj 上级类
getData(fieldName: String, srcObj: Any, onGetCallback: (Any?) -> Unit): ReflexUtils

//反射设置数据
//srcObj 上级类
//targetObj 要替换的参数
setData(fieldName: String,srcObj: Any,targetObj: Any,onSetCallback: (Any?) -> Uni): ReflexUtils
```
## 字符串相关
- ### [StringUtils](https://github.com/SilverIceKey/SKExtension/blob/master/src/main/java/com/sk/skextension/utils/string/StringUtils.kt)
```
//提取中文返回 使用正则
getAllZH(input: String?): String
```
