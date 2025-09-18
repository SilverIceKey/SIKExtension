package com.sik.siknet.tcp.netty.ws

/**
 * 消息监听
 */
interface MessageListener {
    /**
     * 文本消息
     */
    fun onMessage(message: String)

    /**
     * 源数据消息
     */
    fun onRawMessage(message: ByteArray)
}