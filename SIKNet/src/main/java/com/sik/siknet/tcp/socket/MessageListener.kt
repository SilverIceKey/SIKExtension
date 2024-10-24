package com.sik.siknet.tcp.socket

/**
 * 消息监听器接口，用于处理异步接收到的消息。
 */
interface MessageListener {
    /**
     * 当消息接收到时调用此方法。
     *
     * @param message 接收到的消息内容。
     */
    fun onMessageReceived(message: String)

    /**
     * 当消息接收到时调用此方法。
     *  @param data 接收到的消息内容的二进制。
     */
    fun onMessageReceivedRawData(data: ByteArray)
}
