package com.sik.sikcore.device

import android.app.ActivityManager
import android.content.Context
import com.sik.sikcore.SIKCore

/**
 * 内存工具类
 */
object MemoryUtils {
    private val activityManager: ActivityManager by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        SIKCore.getApplication()
            .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    /**
     * 获取总的内存占用大小
     */
    fun getTotalMemory(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem
    }

    /**
     * 获取可用内存大小
     */
    fun getAvailMemory(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.availMem
    }

    /**
     * 获取使用过的内存占用
     */
    fun getUsedMemory(): Long {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        return memoryInfo.totalMem - memoryInfo.availMem
    }
}