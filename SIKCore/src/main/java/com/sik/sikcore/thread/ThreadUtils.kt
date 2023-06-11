package com.sik.sikcore.thread

import android.os.Handler
import android.os.Looper

/**
 * 线程工具
 */
object ThreadUtils {
    /**
     * 主线程
     */
    private val mainHandler by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(Looper.getMainLooper())
    }

    /**
     * 主线程
     */
    fun mainHandler(): Handler {
        return mainHandler
    }
}