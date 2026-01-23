package com.sik.sikandroid.permission

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import com.sik.sikcore.SIKCore
import java.util.concurrent.atomic.AtomicBoolean

object PermissionUtils {

    fun interface PermissionCallback {
        fun onPermissionsResult(granted: Boolean)
    }

    fun interface PermissionDetailsCallback {
        fun onPermissionsResult(results: Map<String, Boolean>)
    }

    fun interface AllFilesAccessCallback {
        fun onResult(granted: Boolean)
    }

    private val busy = AtomicBoolean(false)

    /**
     * ✅ 推荐：返回每个权限的独立结果
     * 规则：
     * - 全部已授权：立即回调（不会弹窗）
     * - 有未授权：启动 PermissionRequestActivity，请求后在用户点击/系统返回后回调
     */
    fun requestPermissionsDetailed(
        context: Context,
        permissions: Array<String>,
        callback: PermissionDetailsCallback
    ) {
        // 1) 先做“已授权直接过”
        val already = permissions.associateWith { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        val needRequest = already.filterValues { granted -> !granted }.keys.toTypedArray()

        if (needRequest.isEmpty()) {
            callback.onPermissionsResult(already) // ✅ 没弹窗，直接过
            return
        }

        // 2) 有未授权 -> 必须走系统流程，等用户点完再回调
        if (!busy.compareAndSet(false, true)) {
            // 并发请求直接失败返回（你也可以改成排队）
            callback.onPermissionsResult(permissions.associateWith { false })
            return
        }

        PermissionResultBus.setPermissionsCallback { resultsFromSystem ->
            busy.set(false)
            // 把“已授权的 + 系统返回的”合并成完整结果（每个权限都有）
            val merged = permissions.associateWith { perm ->
                already[perm] == true || (resultsFromSystem[perm] == true)
            }
            callback.onPermissionsResult(merged)
        }

        val intent = Intent(context, PermissionRequestActivity::class.java).apply {
            putExtra(
                PermissionRequestActivity.EXTRA_MODE,
                PermissionRequestActivity.MODE_RUNTIME_PERMISSIONS
            )
            putExtra(PermissionRequestActivity.EXTRA_PERMISSIONS, needRequest) // 只请求未授权的
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    fun requestPermissions(
        context: Context = SIKCore.getApplication(),
        permissions: Array<String>,
        callback: PermissionCallback
    ) {
        requestPermissionsDetailed(context, permissions) { results ->
            callback.onPermissionsResult(results.values.all { it == true })
        }
    }

    /**
     * ✅ Android 11+：所有文件访问
     * 规则：
     * - 已有：直接回调 true（不跳设置）
     * - 没有：跳设置页，回来后再回调（用户操作后）
     */
    fun requestAllFilesAccessPermission(
        context: Context,
        callback: AllFilesAccessCallback
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            callback.onResult(true)
            return
        }

        if (Environment.isExternalStorageManager()) {
            callback.onResult(true) // ✅ 已有权限，直接过
            return
        }

        if (!busy.compareAndSet(false, true)) {
            callback.onResult(false)
            return
        }

        PermissionResultBus.setAllFilesCallback { granted ->
            busy.set(false)
            callback.onResult(granted)
        }

        val intent = Intent(context, PermissionRequestActivity::class.java).apply {
            putExtra(
                PermissionRequestActivity.EXTRA_MODE,
                PermissionRequestActivity.MODE_ALL_FILES_ACCESS
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
