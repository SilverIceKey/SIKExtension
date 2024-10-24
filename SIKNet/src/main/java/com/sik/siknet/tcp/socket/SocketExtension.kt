package com.sik.siknet.tcp.socket

/**
 * 建立连接
 *
 */
fun SocketConfig.connect() {
    ConnectionManager.instance.createConnection(this).connect()
}

/**
 * 断开连接
 *
 */
fun String.closeConnection() {
    ConnectionManager.instance.closeConnection(this)
}

/**
 * 获取连接
 *
 * @return
 */
fun String.getConnection(): SocketUtils? {
    return ConnectionManager.instance.getConnection(this)
}