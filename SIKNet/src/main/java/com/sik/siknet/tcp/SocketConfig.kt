package com.sik.siknet.tcp

/**
 * 抽象类，表示 Socket 配置。
 * 该类定义了 Socket 连接的基本配置。
 *
 * @property configId 配置的唯一标识符。
 * @property ipAddress 目标服务器的 IP 地址。
 * @property port 目标服务器的端口号。
 * @property timeout 读取数据的超时时间，单位为毫秒。
 * @property maxReconnectAttempts 最大重连次数。
 * @property reconnectInterval 每次重连的间隔时间，单位为毫秒。
 * @property endMark 结尾标记
 * @property replaceAllEndMarks 是否替换文本中的所有结尾标记
 * @function onConnectionTimeout 可选函数，用于处理连接超时的情况。
 */
abstract class SocketConfig {
    abstract val configId: String
    abstract val ipAddress: String
    abstract val port: Int
    abstract val timeout: Int
    open val maxReconnectAttempts: Int = 3
    open val reconnectInterval: Long = 2000
    open val endMark: String = ""
    open val replaceAllEndMarks: Boolean = false

    // 用户可以覆盖此方法来自定义连接超时的处理逻辑
    open fun onConnectionTimeout() {
        // 默认行为：连接超时无特殊处理
    }
}
