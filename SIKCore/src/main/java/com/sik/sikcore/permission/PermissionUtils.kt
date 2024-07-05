package com.sik.sikcore.permission

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.sik.sikcore.activity.ActivityTracker
import androidx.core.content.ContextCompat

/**
 * 工具类，用于权限请求和管理。
 */
object PermissionUtils {
    const val PERMISSION_REQUEST_CODE = 100
    const val MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101

    fun interface PermissionCallback {
        fun onPermissionsResult(granted: Boolean)
    }

    /**
     * 检查和请求权限。
     */
    fun checkAndRequestPermissions(
        permissions: Array<String>,
        callback: PermissionCallback = PermissionCallback { }
    ) {
        val currentActivity = ActivityTracker.getCurrentActivity()
        if (currentActivity != null && currentActivity is FragmentActivity) {
            if (hasPermissions(currentActivity, *permissions)) {
                callback.onPermissionsResult(true)
            } else {
                getPermissionFragment(currentActivity).requestPermissions(permissions, callback)
            }
        } else {
            callback.onPermissionsResult(false)  // 当无活动可用时返回失败
        }
    }

    /**
     * 请求文件系统管理权限。
     */
    fun requestAllFilesAccessPermission(
        callback: PermissionCallback = PermissionCallback { }
    ) {
        val currentActivity = ActivityTracker.getCurrentActivity()
        if (currentActivity != null && currentActivity is FragmentActivity) {
            getPermissionFragment(currentActivity).requestManageExternalStorage(callback)
        } else {
            callback.onPermissionsResult(false)  // 当无活动可用时返回失败
        }
    }

    private fun getPermissionFragment(activity: FragmentActivity): PermissionFragment {
        var fragment =
            activity.supportFragmentManager.findFragmentByTag("PermissionFragment") as PermissionFragment?
        if (fragment == null) {
            fragment = PermissionFragment()
            activity.supportFragmentManager.beginTransaction()
                .add(fragment, "PermissionFragment")
                .commitNow()
        }
        return fragment
    }

    @Composable
    fun RequestPermissions(
        permissions: Array<String>,
        triggerPermissionRequest: MutableState<Boolean>,
        onPermissionsResult: (Boolean) -> Unit = {},
    ) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestMultiplePermissions(),
            onResult = { results ->
                val allGranted = results.all { it.value == true }
                onPermissionsResult(allGranted)
            }
        )

        // 触发权限请求
        LaunchedEffect(triggerPermissionRequest.value) {
            if (triggerPermissionRequest.value) {
                launcher.launch(permissions)
                triggerPermissionRequest.value = false // 请求后重置触发器
            }
        }
    }

    @Composable
    fun RequestManageExternalStorage(
        triggerPermissionRequest: MutableState<Boolean>,
        onPermissionsResult: (Boolean) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val granted = Environment.isExternalStorageManager()
                onPermissionsResult(granted)
            }

            LaunchedEffect(triggerPermissionRequest.value) {
                if (triggerPermissionRequest.value) {
                    launcher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    triggerPermissionRequest.value = false // 请求后重置触发器
                }
            }
        } else {
            onPermissionsResult(true) // 对于R以下版本，假设总是有权限
        }
    }


    /**
     * 检查指定的权限是否已被授予。
     */
    private fun hasPermissions(context: Activity, vararg permissions: String): Boolean =
        permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
}
