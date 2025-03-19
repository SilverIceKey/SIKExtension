package com.sik.sikcore.bluetooth

/**
 * IBluetoothConnectionCallback 定义了蓝牙连接事件的回调接口，
 * 用于通知连接状态、数据接收以及连接断开事件。
 */
interface IBluetoothConnectionCallback {
    /**
     * 当蓝牙连接成功后调用
     * @param connection 当前建立的蓝牙连接对象
     */
    fun onConnected(connection: BluetoothConnection)

    /**
     * 当接收到数据时调用
     * @param connection 当前蓝牙连接对象
     * @param data 接收到的数据字节数组
     */
    fun onDataReceived(connection: BluetoothConnection, data: ByteArray)

    /**
     * 当蓝牙连接断开时调用
     * @param connection 当前蓝牙连接对象
     */
    fun onDisconnected(connection: BluetoothConnection)
}
