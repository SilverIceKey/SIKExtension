package com.sik.sikcore

import android.app.Application

/**
 * 初始化监听
 */
interface InitListener {
    /**
     * Application初始化
     */
    fun init(application: Application)
}