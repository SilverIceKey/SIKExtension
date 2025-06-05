# 网络库

## 说明：使用扩展函数的特性基于Okhttp对Api String进行网络请求调用

需要在Manifest中声明以下权限:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

## 使用方法：

    Api.httpGet<T>():T//Get 请求
    Api.httpPostForm<T>():T//Post 请求 表单类型
    Api.httpPostJson<T>():T//Post 请求 Json类型
    Api.httpUploadFile<T>():T//Post 请求 上传文件
    Api.httpDownloadFile()//下载文件
## 方法介绍：

    所有的调用均不自动切换线程


## 关于NETTY的使用 
### 示例代码
1. 创建一个自定义配置类，继承 NettyConfig：
```kotlin
class MyNettyConfig : NettyConfig<String>() {
    override val host: String = "127.0.0.1"
    override val port: Int = 65432
    
    override val sender: ChannelInboundHandler = MySenderHandler()
    
    override val decoder: MessageToMessageDecoder<String> = MyDecoder()
    
    override val encoder: MessageToByteEncoder<String> = MyEncoder()
    
    override val process: SimpleChannelInboundHandler<String> = MyProcessHandler()
    
    override val connectSuccess: () -> Unit = {
        println("Connected successfully!")
    }
    
    override val retryTimes: Int = 5 // 设置重试次数
    override val retryTime: Long = 2000 // 设置重试时间间隔（毫秒）
}
```
2. 创建发送、解码、处理等自定义处理器：
```kotlin
class MySenderHandler : ChannelInboundHandlerAdapter() {
    // 处理发送逻辑
}

class MyDecoder : MessageToMessageDecoder<String>() {
    override fun decode(ctx: ChannelHandlerContext, msg: String, out: MutableList<Any>) {
        // 解码逻辑
        out.add(msg)
    }
}

class MyEncoder : MessageToByteEncoder<String>() {
    override fun encode(ctx: ChannelHandlerContext, msg: String, out: ByteBuf) {
        // 编码逻辑
        out.writeBytes(msg.toByteArray())
    }
}

class MyProcessHandler : SimpleChannelInboundHandler<String>() {
    override fun channelRead0(ctx: ChannelHandlerContext, msg: String) {
        // 处理收到的消息
        println("Received: $msg")
    }
}
```
3. 使用 NettyClientUtils 连接服务器：
```kotlin
fun main() {
    val config = MyNettyConfig()
    NettyClientUtils.instance.connect(config)
}
```

### 更新日志
- 2025-06：整理文档格式并补充说明。
