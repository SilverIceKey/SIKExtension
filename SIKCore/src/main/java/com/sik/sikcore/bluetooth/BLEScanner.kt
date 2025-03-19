package com.sik.sikcore.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.util.Log

/**
 * BLEScanner 用于执行蓝牙低功耗（BLE）扫描操作，
 * 通过 BluetoothLeScanner API 获取扫描结果，并通过 IBluetoothScanCallback 通知外部。
 */
class BLEScanner(private val callback: IBluetoothScanCallback) {

    companion object {
        private const val TAG = "BLEScanner"
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner
    private var isScanning = false

    // BLE 扫描回调
    private val bleScanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            result?.device?.let { device ->
                Log.d(TAG, "BLE扫描发现设备: ${device.name} - ${device.address}")
                callback.onDeviceFound(device)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "BLE扫描失败，错误码: $errorCode")
        }
    }

    /**
     * 开始 BLE 扫描
     */
    @SuppressLint("MissingPermission")
    fun startScan() {
        bluetoothLeScanner?.let { scanner ->
            scanner.startScan(bleScanCallback)
            isScanning = true
            Log.d(TAG, "开始BLE扫描")
        } ?: run {
            Log.e(TAG, "BLE扫描不可用或蓝牙未开启")
        }
    }

    /**
     * 停止 BLE 扫描
     */
    @SuppressLint("MissingPermission")
    fun stopScan() {
        bluetoothLeScanner?.let { scanner ->
            scanner.stopScan(bleScanCallback)
            isScanning = false
            Log.d(TAG, "停止BLE扫描")
        }
    }
}
