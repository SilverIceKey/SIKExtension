package com.sik.sikandroid.permission

internal object PermissionResultBus {
    private var permissionsCallback: ((Map<String, Boolean>) -> Unit)? = null
    private var allFilesCallback: ((Boolean) -> Unit)? = null

    fun setPermissionsCallback(cb: (Map<String, Boolean>) -> Unit) {
        permissionsCallback = cb
        allFilesCallback = null
    }

    fun setAllFilesCallback(cb: (Boolean) -> Unit) {
        allFilesCallback = cb
        permissionsCallback = null
    }

    fun dispatchPermissions(results: Map<String, Boolean>) {
        permissionsCallback?.invoke(results)
        permissionsCallback = null
    }

    fun dispatchAllFiles(granted: Boolean) {
        allFilesCallback?.invoke(granted)
        allFilesCallback = null
    }
}
