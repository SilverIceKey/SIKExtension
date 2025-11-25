package com.sik.siknet.tcp.socket

import android.util.Log

/**
 * 连接管理器类，负责管理多个 Socket 连接实例。
 * 支持根据不同的配置 ID 创建、获取或关闭连接。
 */
class ConnectionManager {

    companion object {
        /**
         * 单例
         */
        val instance: ConnectionManager by lazy {
            ConnectionManager()
        }
    }

    // 存储连接实例的映射表，键为配置ID，值为 SocketUtils 实例
    private val connections = mutableMapOf<String, SocketUtils>()

    /**
     * 根据给定的配置创建一个新的连接。
     *
     * @param config SocketConfig 实例，包含连接的配置信息。
     * @return 创建并返回一个 SocketUtils 实例。
     */
    fun createConnection(config: SocketConfig): SocketUtils {
        val socketUtils = SocketUtils(config)
        connections[config.configId] = socketUtils  // 将连接保存到映射表中
        Log.i("ConnectionManager","创建了新的连接，配置ID: ${config.configId}")
        return socketUtils
    }

    /**
     * 根据配置 ID 获取现有的连接实例。
     *
     * @param configId 配置的唯一标识符。
     * @return 返回对应的 SocketUtils 实例，如果没有则返回 null。
     */
    fun getConnection(configId: String): SocketUtils? {
        return connections[configId].also {
            Log.i("ConnectionManager","获取了连接，配置ID: $configId")
        }
    }

    /**
     * 关闭并移除指定配置 ID 对应的连接。
     *
     * @param configId 配置的唯一标识符。
     */
    fun closeConnection(configId: String) {
        connections[configId]?.disconnect()
        connections.remove(configId)
        Log.i("ConnectionManager","关闭并移除了连接，配置ID: $configId")
    }

    /**
     * 关闭并移除所有连接。
     */
    fun closeAllConnections() {
        connections.forEach { (configId, socket) ->
            socket.disconnect()
            Log.i("ConnectionManager","关闭了连接，配置ID: $configId")
        }
        connections.clear()
        Log.i("ConnectionManager","已关闭所有连接")
    }
}
