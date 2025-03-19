package com.sik.sikcore.bluetooth

import android.bluetooth.BluetoothDevice

/**
 * IBluetoothScanCallback 定义了蓝牙扫描回调接口，
 * 用于通知扫描过程中发现的蓝牙设备以及扫描结束事件。
 */
interface IBluetoothScanCallback {
    /**
     * 当扫描到一个蓝牙设备时调用
     * @param device 扫描到的蓝牙设备
     */
    fun onDeviceFound(device: BluetoothDevice)

    /**
     * 当扫描结束时调用
     */
    fun onScanFinished()
}
