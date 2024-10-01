package com.sik.siknet.tcp

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
}
