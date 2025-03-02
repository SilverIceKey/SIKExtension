package com.sik.siknet.udp

import org.slf4j.LoggerFactory
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress


/**
 * UDP帮助类
 */
object UDPHelper {
    private val logger = LoggerFactory.getLogger(UDPHelper::class.java)

    /**
     * 发送udp数据
     */
    fun sendUdpData(message: String, host: String, port: Int) {
        var socket: DatagramSocket? = null
        try {
            // 创建UDP套接字
            socket = DatagramSocket()
            val serverAddress = InetAddress.getByName(host)
            // 将消息转换为字节数组
            val data = message.toByteArray()
            // 创建数据报
            val packet: DatagramPacket = DatagramPacket(data, data.size, serverAddress, port)
            // 发送数据
            socket.send(packet)
            logger.debug("数据发送成功")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (socket != null && !socket.isClosed) {
                socket.close()
            }
        }
    }
}