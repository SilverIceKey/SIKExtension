package com.sik.sikcore.file

import android.os.FileObserver
import java.io.File
import java.io.IOException

/**
 * 单文件监听工具类。
 *
 * 功能说明：
 * 1. 如果监听的文件所在目录不存在，会尝试创建父目录。
 * 2. 如果目标文件不存在，会尝试创建该文件。
 * 3. 监听写入关闭(CLOSE_WRITE)和修改(MODIFY)事件，通过回调通知使用者。
 * 4. 使用 FileObserver（基于 inotify），非阻塞，不锁定文件。
 *
 * 使用示例：
 * ```kotlin
 * val watcher = SingleFileWatcher(
 *   filePath = "/sdcard/mydir/trigger.txt"
 * ) { event, path ->
 *   when (event) {
 *     FileObserver.CLOSE_WRITE, FileObserver.MODIFY ->
 *       Log.d("Watcher", "文件变更: $path")
 *   }
 * }
 * watcher.startWatching()
 * // 在不需要时：
 * watcher.stopWatching()
 * ```
 *
 * @param filePath 要监听的文件绝对路径，例如 "/sdcard/mydir/trigger.txt"
 * @param events   FileObserver 事件掩码，默认监听 CLOSE_WRITE | MODIFY
 * @param listener 监听回调：
 *                 - [event]: 事件类型（FileObserver.CLOSE_WRITE、MODIFY 等）
 *                 - [fullPath]: 目标文件全路径
 */
class SingleFileWatcher(
    private val filePath: String,
    private val events: Int = FileObserver.CLOSE_WRITE or FileObserver.MODIFY,
    private val listener: (event: Int, fullPath: String) -> Unit
) {

    private val observer: FileObserver

    init {
        // 确保父目录和文件都存在，不存在则创建，创建失败抛出异常
        ensureFileExists()

        // 监听父目录下指定文件的变更事件
        val parentDir = File(filePath).parentFile
            ?: throw IllegalArgumentException("文件路径无效，没有父目录：$filePath")

        observer = object : FileObserver(parentDir.absolutePath, events) {
            override fun onEvent(event: Int, path: String?) {
                // 仅对目标文件名感兴趣，其它文件忽略
                if (path != null && path == File(filePath).name) {
                    listener(event, filePath)
                }
            }
        }
    }

    /**
     * 开始监听。调用后，只要有符合 [events] 的文件系统事件，回调就会触发。
     * 请在不需要时调用 [stopWatching] 以释放资源。
     */
    fun startWatching() {
        observer.startWatching()
    }

    /**
     * 停止监听。停止后将不再收到任何回调，并释放对应的 inotify watch 资源。
     */
    fun stopWatching() {
        observer.stopWatching()
    }

    /**
     * 确保监听的文件及其父目录存在：
     * - 如果父目录不存在，尝试创建（包括多级目录），失败抛出 [RuntimeException]
     * - 如果文件不存在，尝试创建，失败抛出 [RuntimeException]
     */
    private fun ensureFileExists() {
        val file = File(filePath)
        val dir = file.parentFile
        // 创建父目录
        if (dir != null && !dir.exists()) {
            if (!dir.mkdirs()) {
                throw RuntimeException("无法创建父目录: ${dir.absolutePath}")
            }
        }
        // 创建文件
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw IOException("createNewFile() 返回 false")
                }
            } catch (e: IOException) {
                throw RuntimeException("无法创建文件: ${file.absolutePath}", e)
            }
        }
    }
}
