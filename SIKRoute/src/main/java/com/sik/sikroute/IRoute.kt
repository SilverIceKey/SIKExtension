package com.sik.sikroute

import android.os.Bundle

/**
 * route interface
 * 路由接口
 * @constructor Create empty I route
 */
interface IRoute {
    /**
     * Start activity
     * 跳转
     * @param targetClass
     * @param requestCode
     * @param option
     */
    fun startActivity(targetClass: Class<*>, requestCode: Int = -1, option: Bundle? = null)
}