package com.sik.sikcore.activity

import android.app.Activity
import android.view.WindowManager

/**
 * Activity 工具类
 */
object ActivityUtil {
    /**
     * 安全界面标识
     */
    private const val SECURE_FLAG = WindowManager.LayoutParams.FLAG_SECURE

    /**
     * 设置为安全界面
     */
    fun setSecure(activity: Activity) {
        activity.window.addFlags(SECURE_FLAG)
    }

    /**
     * 清楚安全界面标识
     */
    fun clearSecure(activity: Activity) {
        activity.window.clearFlags(SECURE_FLAG)
    }
}