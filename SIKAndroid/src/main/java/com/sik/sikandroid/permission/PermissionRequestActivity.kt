package com.sik.sikandroid.permission

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class PermissionRequestActivity : ComponentActivity() {

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val EXTRA_PERMISSIONS = "extra_permissions"

        const val MODE_RUNTIME_PERMISSIONS = 1
        const val MODE_ALL_FILES_ACCESS = 2
    }

    private val runtimePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
            // ✅ 用户点完（或系统返回）后回调
            PermissionResultBus.dispatchPermissions(results)
            finish()
        }

    private val allFilesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else true
            PermissionResultBus.dispatchAllFiles(granted)
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mode = intent.getIntExtra(EXTRA_MODE, MODE_RUNTIME_PERMISSIONS)

        // ✅ 下一帧再 launch，避免生命周期太早导致“直接失败”
        window.decorView.post {
            when (mode) {
                MODE_RUNTIME_PERMISSIONS -> {
                    val permissions: Array<String> =
                        intent.getStringArrayExtra(EXTRA_PERMISSIONS)?.filterNotNull()?.toTypedArray()
                            ?: emptyArray()
                    if (permissions.isEmpty()) {
                        PermissionResultBus.dispatchPermissions(emptyMap())
                        finish()
                        return@post
                    }
                    runtimePermissionLauncher.launch(permissions)
                }

                MODE_ALL_FILES_ACCESS -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        allFilesLauncher.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    } else {
                        PermissionResultBus.dispatchAllFiles(true)
                        finish()
                    }
                }

                else -> {
                    PermissionResultBus.dispatchPermissions(emptyMap())
                    finish()
                }
            }
        }
    }
}
