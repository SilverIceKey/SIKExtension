package com.sik.sikcore.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.util.UUID

/**
 * BluetoothManager 是蓝牙通信的统一管理类，
 * 包括经典蓝牙扫描（可与 ClassicBluetoothScanner 一起使用）、BLE扫描（与 BLEScanner 配合）以及连接管理。
 *
 * 提供方法启动连接、断开指定连接或所有连接。
 */
object BluetoothManager {

    private const val TAG = "BluetoothManager"

    // 获取经典蓝牙适配器
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    // 保存当前连接的蓝牙连接对象，键为设备地址
    private val connectionMap = mutableMapOf<String, BluetoothConnection>()

    // SPP 协议 UUID，适用于经典蓝牙的 RFCOMM 连接
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /**
     * 建立经典蓝牙连接。
     *
     * @param device 待连接的蓝牙设备
     * @param callback 蓝牙连接事件回调（可为 null）
     * @return 成功建立时返回 BluetoothConnection 对象，连接失败返回 null
     */
    @SuppressLint("MissingPermission")
    fun connect(
        device: BluetoothDevice,
        uuid: UUID = SPP_UUID,
        callback: IBluetoothConnectionCallback? = null
    ): BluetoothConnection? {
        // 停止扫描，避免影响连接
        bluetoothAdapter?.cancelDiscovery()
        var socket: BluetoothSocket? = null
        return try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            socket.connect()
            Log.d(TAG, "成功连接到设备: ${device.name}")
            val connection = BluetoothConnection(device, socket, callback)
            connection.start()
            connectionMap[device.address] = connection
            connection
        } catch (e: IOException) {
            Log.e(TAG, "连接 ${device.name} 失败: ${e.message}")
            try {
                socket?.close()
            } catch (ex: IOException) {
                Log.e(TAG, "关闭 socket 失败: ${ex.message}")
            }
            null
        }
    }

    /**
     * 断开与指定蓝牙设备的连接
     */
    fun disconnect(device: BluetoothDevice) {
        connectionMap[device.address]?.cancel()
        connectionMap.remove(device.address)
    }

    /**
     * 断开所有已建立的蓝牙连接
     */
    fun disconnectAll() {
        for ((_, connection) in connectionMap) {
            connection.cancel()
        }
        connectionMap.clear()
    }
}
