package com.sk.skextension.utils.device

import com.blankj.utilcode.util.ShellUtils

/**
 * 设备相关工具类
 */
object DeviceUtil {
    /**
     * 获取设备SN号码
     * @return
     */
    fun getSN(): String? {
        return ShellUtils.execCmd("getprop ro.serialno", false).successMsg
    }
}