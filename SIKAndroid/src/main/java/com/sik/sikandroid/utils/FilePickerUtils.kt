package com.sik.sikandroid.utils

import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

/**
 * 文件/目录选择工具类
 *
 * 支持：
 * - 选择单文件
 * - 选择目录
 * - 自定义 MIME 类型
 * - 回调返回 Uri
 */
class FilePickerUtils(caller: ActivityResultCaller) {

    private var onFilePicked: ((Uri?) -> Unit)? = null
    private var onDirPicked: ((Uri?) -> Unit)? = null

    // 选择文件 Launcher
    private val filePickerLauncher =
        caller.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            onFilePicked?.invoke(uri)
        }

    // 选择目录 Launcher（Android 5.0+）
    private val dirPickerLauncher =
        caller.registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            onDirPicked?.invoke(uri)
        }

    /**
     * 打开文件选择器
     * @param mimeTypes 文件类型，例如 arrayOf("application/pdf", "image")
     *
     */
    fun pickFile(mimeTypes: Array<String> = arrayOf("*/*"), callback: (Uri?) -> Unit) {
        onFilePicked = callback
        filePickerLauncher.launch(mimeTypes)
    }

    /**
     * 打开目录选择器（Android 5.0+）
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun pickDirectory(callback: (Uri?) -> Unit) {
        onDirPicked = callback
        dirPickerLauncher.launch(null)
    }
}