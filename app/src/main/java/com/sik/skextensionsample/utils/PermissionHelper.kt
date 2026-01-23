package com.sik.skextensionsample.utils

import android.Manifest
import android.os.Build
import android.util.Log
import com.sik.sikandroid.permission.PermissionUtils

/**
 * 权限帮助工具
 */
object PermissionHelper {
    /**
     * 需要的权限
     */
    private val permissions = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
    )

    /**
     * 检查并申请权限
     */
    fun checkAndRequestPermissions(success: () -> Unit = {}, failed: () -> Unit = {}) {
        PermissionUtils.requestPermissions(permissions = permissions) { granted ->
            if (granted) {
                success()
                Log.i("PermissionHelper", "设备型号:${Build.MODEL}")
            } else {
                failed()
                Log.i("PermissionHelper", "权限获取失败")
            }
        }
    }
}