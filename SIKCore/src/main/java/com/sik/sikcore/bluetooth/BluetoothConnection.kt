package com.sik.sikcore.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * BluetoothConnection 封装了经典蓝牙连接的通信逻辑，
 * 包括数据读取与发送，并通过 IBluetoothConnectionCallback 通知外部连接事件。
 */
class BluetoothConnection(
    val device: BluetoothDevice,
    val socket: BluetoothSocket,
    private val callback: IBluetoothConnectionCallback? = null
) : Thread() {

    companion object {
        private const val TAG = "BluetoothConnection"
    }

    private val inputStream: InputStream? = try {
        socket.inputStream
    } catch (e: IOException) {
        Log.e(TAG, "获取输入流失败: ${e.message}")
        null
    }

    private val outputStream: OutputStream? = try {
        socket.outputStream
    } catch (e: IOException) {
        Log.e(TAG, "获取输出流失败: ${e.message}")
        null
    }

    // 控制线程运行状态
    @Volatile
    private var running = true

    @SuppressLint("MissingPermission")
    override fun run() {
        callback?.onConnected(this)
        val buffer = ByteArray(1024)
        while (running) {
            try {
                val bytesRead = inputStream?.read(buffer) ?: -1
                if (bytesRead > 0) {
                    val data = buffer.copyOf(bytesRead)
                    Log.d(TAG, "从 ${device.name} 接收到数据: ${String(data)}")
                    callback?.onDataReceived(this, data)
                }
            } catch (e: IOException) {
                Log.e(TAG, "读取数据错误: ${e.message}")
                break
            }
        }
        callback?.onDisconnected(this)
        Log.d(TAG, "通信线程结束: ${device.name}")
    }

    /**
     * 发送字节数组数据到远程设备
     */
    @SuppressLint("MissingPermission")
    fun write(data: ByteArray) {
        try {
            outputStream?.write(data)
            Log.d(TAG, "向 ${device.name} 发送数据")
        } catch (e: IOException) {
            Log.e(TAG, "发送数据失败: ${e.message}")
        }
    }

    /**
     * 发送字符串数据到远程设备
     */
    fun write(data: String) {
        write(data.toByteArray())
    }

    /**
     * 停止通信线程并关闭 Socket
     */
    @SuppressLint("MissingPermission")
    fun cancel() {
        running = false
        try {
            socket.close()
            Log.d(TAG, "断开与 ${device.name} 的连接")
        } catch (e: IOException) {
            Log.e(TAG, "关闭 Socket 失败: ${e.message}")
        }
    }
}
