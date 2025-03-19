package com.sik.sikcore.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import com.sik.sikcore.SIKCore

/**
 * ClassicBluetoothScanner 用于执行经典蓝牙扫描操作，
 * 通过广播接收器获取扫描到的设备，并通过 IBluetoothScanCallback 通知外部。
 */
class ClassicBluetoothScanner(private val callback: IBluetoothScanCallback) {

    companion object {
        private const val TAG = "ClassicBluetoothScanner"
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    // 使用 SIKCore.getApplication() 获取 Application Context，防止内存泄漏
    private val appContext = SIKCore.getApplication()
    private var isScanning = false

    // 广播接收器用于监听蓝牙设备扫描结果
    private val discoveryReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    // 针对 Android 13+，getParcelableExtra 需要传入类型参数
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let {
                        Log.d(TAG, "发现设备: ${it.name} - ${it.address}")
                        callback.onDeviceFound(it)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.d(TAG, "经典蓝牙扫描结束")
                    isScanning = false
                    callback.onScanFinished()
                }
            }
        }
    }

    /**
     * 开始经典蓝牙扫描
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        if (bluetoothAdapter == null) {
            Log.e(TAG, "设备不支持蓝牙")
            return
        }
        if (isScanning) {
            stopScan()
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        appContext.registerReceiver(discoveryReceiver, filter)
        bluetoothAdapter.startDiscovery()
        isScanning = true
        Log.d(TAG, "开始经典蓝牙扫描")
    }

    /**
     * 停止经典蓝牙扫描并注销广播接收器
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothAdapter?.let { adapter ->
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
        }
        try {
            appContext.unregisterReceiver(discoveryReceiver)
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "广播接收器未注册: ${e.message}")
        }
        isScanning = false
        Log.d(TAG, "停止经典蓝牙扫描")
    }
}
