package com.sik.sikandroid.provider

import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.util.Log
import com.sik.sikandroid.activity.ActivityTracker
import com.sik.sikcore.InitListener
import com.sik.sikcore.SIKCore

/**
 * ModuleInitProvider
 *
 * 用途：利用 ContentProvider 的早期加载特性，在 App 启动时初始化本模块
 */
class ModuleInitProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        // 这里写你的模块初始化逻辑
        // 比如：初始化日志、配置 MMKV、注册全局回调等
        SIKCore.registerInitListener(object : InitListener {
            override fun init(application: Application) {
                application.registerActivityLifecycleCallbacks(ActivityTracker)
            }
        })
        return true
    }

    // 以下四个方法不需要实现，直接返回默认
    override fun query(
        uri: Uri, projection: Array<out String>?,
        selection: String?, selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri, values: ContentValues?,
        selection: String?, selectionArgs: Array<out String>?
    ): Int = 0
}