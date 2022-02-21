package com.sk.skextension.utils.net

import android.content.Context
import android.net.wifi.WifiManager
import com.sk.skextension.utils.SKExtension
import com.sk.skextension.utils.file.FileUtil
import java.io.IOException
import java.util.*

object NetUtil {
    /**
     * 获取MAC地址（有网口的前提下）
     */
    fun getMacAddress(): String? {
        return try {
            FileUtil.loadFileAsString("/sys/class/net/eth0/address")
                .uppercase(Locale.ROOT).substring(0, 17)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 获取当前wifi名称
     */
    fun getWifiName(): String? {
        val wifiManager = SKExtension.getApplication().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo!=null){
            return wifiInfo.ssid.replace("\"","")
        }
        return null
    }
}