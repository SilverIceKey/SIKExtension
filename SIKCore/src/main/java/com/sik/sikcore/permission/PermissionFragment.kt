package com.sik.sikcore.permission

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

/**
 * 用于请求权限的隐藏Fragment。
 */
class PermissionFragment : Fragment() {
    private var permissionCallback: PermissionUtils.PermissionCallback? = null

    /**
     * 请求一组权限。
     */
    fun requestPermissions(permissions: Array<String>, callback: PermissionUtils.PermissionCallback) {
        this.permissionCallback = callback
        requestPermissions(permissions, PermissionUtils.PERMISSION_REQUEST_CODE)
    }

    /**
     * 请求文件系统管理权限（仅限Android R及以上版本）。
     */
    fun requestManageExternalStorage(callback: PermissionUtils.PermissionCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            // intent.data = Uri.parse("package:${requireActivity().packageName}")

            // Check if the intent can be resolved
            if (intent.resolveActivity(requireActivity().packageManager) != null) {
                try {
                    startActivityForResult(intent, PermissionUtils.MANAGE_EXTERNAL_STORAGE_REQUEST_CODE)
                    this.permissionCallback = callback
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(), "Activity not found: ${e.message}", Toast.LENGTH_LONG).show()
                    callback.onPermissionsResult(false)
                }
            } else {
                // Handle the situation when no activity can handle the intent
                Toast.makeText(requireContext(), "No activity found to handle the intent", Toast.LENGTH_LONG).show()
                callback.onPermissionsResult(false)
            }
        } else {
            callback.onPermissionsResult(true)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PermissionUtils.PERMISSION_REQUEST_CODE) {
            val granted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            permissionCallback?.onPermissionsResult(granted)
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PermissionUtils.MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            val granted = Environment.isExternalStorageManager()
            permissionCallback?.onPermissionsResult(granted)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
