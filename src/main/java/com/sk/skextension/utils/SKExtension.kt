package com.sk.skextension.utils

import android.app.Application

/**
 * 扩展的初始化
 */
object SKExtension {
    private var application: Application? = null

    /**
     * 初始化
     */
    fun init(application: Application) {
        this.application = application
    }

    /**
     * 获取application
     */
    fun getApplication(): Application {
        if (application == null) {
            throw NullPointerException("请先初始化SKExtension")
        } else {
            return application!!
        }
    }
}