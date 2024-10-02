package com.sik.siknet.tcp

import com.sik.sikcore.extension.replaceLast
import com.sik.sikcore.log.LogUtils
import com.sik.sikcore.thread.ThreadUtils
import java.io.IOException
import java.net.Socket
import java.net.SocketTimeoutException

/**
 * Socket 工具类，负责管理 Socket 连接、发送和接收数据。
 * 包含基于配置的自动重连逻辑。
 *
 * @property config 用于管理连接的 Socket 配置。
 */
class SocketUtils(private val config: SocketConfig) {

    private var socket: Socket? = null  // Socket 实例
    private var reconnectAttempts = 0   // 当前的重连尝试次数

    private var isMessageListener = false //消息监听是否开启

    // 日志工具类实例，用于记录日志
    private val logger = LogUtils.getLogger(this::class)

    // 消息监听器
    private var messageListener: MessageListener? = null

    /**
     * 设置消息监听器，用于接收服务器返回的消息。
     *
     * @param listener 消息监听器实例。
     */
    fun setMessageListener(listener: MessageListener) {
        messageListener = listener
    }

    /**
     * 尝试使用配置建立 Socket 连接。
     * 如果连接失败，将根据重连配置进行自动重连。
     */
    fun connect() {
        ThreadUtils.runOnIO {
            try {
                // 创建 Socket 并连接到目标地址
                socket = Socket(config.ipAddress, config.port)
                socket?.soTimeout = config.timeout  // 设置超时时间

                // 成功连接后，重连次数清零
                reconnectAttempts = 0
                logger.i("成功连接到 ${config.ipAddress}:${config.port},开始监听")
                startListeningForMessages()
            } catch (e: IOException) {
                // 捕获连接异常并记录日志
                logger.i("连接失败: ${e.message}")
                attemptReconnect()  // 尝试重连
            }
        }
    }

    /**
     * 根据配置的最大重连次数和间隔进行重连。
     * 如果重连次数超过最大值，将停止重连。
     */
    private fun attemptReconnect() {
        ThreadUtils.runOnIO {
            if (config.maxReconnectAttempts == -1 || reconnectAttempts < config.maxReconnectAttempts) {
                reconnectAttempts++
                logger.i("正在重连... 尝试次数 $reconnectAttempts")

                // 等待指定的重连间隔时间后再进行重连
                Thread.sleep(config.reconnectInterval)
                connect()  // 再次尝试连接
            } else {
                // 达到最大重连次数时放弃重连，并调用连接超时处理方法
                logger.i("已达到最大重连次数，放弃重连。")
                config.onConnectionTimeout()
            }
        }
    }

    /**
     * 通过 Socket 的输出流发送消息。
     *
     * @param message 要发送的消息内容。
     */
    fun sendMessage(message: String) {
        ThreadUtils.runOnIO {
            try {
                socket?.getOutputStream()?.write(message.toByteArray())
            } catch (e: IOException) {
                // 捕获发送异常并记录日志，尝试重连
                logger.i("消息发送失败，连接可能已断开: ${e.message}")
                attemptReconnect()
            }
        }
    }

    /**
     * 启动一个新线程来异步监听服务器发送的消息。
     * 一旦接收到消息，将通过 MessageListener 通知调用方。
     */
    private fun startListeningForMessages() {
        ThreadUtils.runOnIO {
            try {
                val inputStream = socket?.getInputStream()
                val buffer = ByteArray(1024)  // 定义一个缓冲区大小
                val messageBuilder = StringBuilder()  // 用于拼接读取到的消息
                val endMark = config.endMark  // 从配置中获取自定义的结束符
                val replaceAllEndMarks = config.replaceAllEndMarks  // 从配置中获取替换策略
                while (socket != null && socket!!.isConnected) {
                    try {
                        val bytesRead = inputStream?.read(buffer) ?: -1
                        if (bytesRead > 0) {
                            // 将读取的字节数据转换为字符串，并拼接到 messageBuilder 中
                            val part = String(buffer, 0, bytesRead)
                            messageBuilder.append(part)

                            // 检查是否包含自定义的结束符
                            if (messageBuilder.contains(endMark)) {
                                val message: String
                                if (replaceAllEndMarks) {
                                    // 替换所有的 endMark
                                    message = messageBuilder.toString().replace(endMark, "")
                                } else {
                                    // 只替换最后一个 endMark
                                    message = messageBuilder.toString().replaceLast(endMark, "")
                                }

                                messageListener?.onMessageReceived(message)
                                logger.i("接收到完整消息: $message")

                                // 清空 StringBuilder，准备处理下一条消息
                                messageBuilder.setLength(0)
                            }
                        }
                    } catch (e: SocketTimeoutException) {
                        // 处理超时异常，继续等待
                        logger.i("读取消息时超时，没有数据到达。继续监听...")
                    }
                }
            } catch (e: IOException) {
                logger.i("接收消息时发生错误: ${e.message}")
                attemptReconnect()
            }
        }
    }


    /**
     * 断开 Socket 连接，并释放资源。
     */
    fun disconnect() {
        ThreadUtils.runOnIO {
            socket?.close()
            logger.i("已断开连接 ${config.ipAddress}:${config.port}")
        }
    }
}
