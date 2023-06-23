package com.sik.sikroute

import android.content.Intent
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

    /**
     * Start activity use Intent
     * 跳转，基本用于隐式跳转
     * @param intent
     */
    fun startActivityUseIntent(intent:Intent)
}