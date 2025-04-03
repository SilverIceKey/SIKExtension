package com.sik.sikcore.service

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import com.sik.sikcore.SIKCore
import kotlinx.coroutines.*

/**
 * 下载服务工具
 *
 * 该工具类基于 Android 系统的 DownloadManager 封装了一系列下载操作，
 * 提供启动下载、查询状态、取消下载以及下载进度监控等功能，
 * 采用 Kotlin 协程实现异步处理，方便在高并发场景下使用，
 * 同时留有扩展点便于后续增强更多功能。
 */
object DownloadServiceUtils {

    /**
     * 下载状态枚举
     */
    enum class DownloadStatus {
        FAILED, PAUSED, PENDING, RUNNING, SUCCESSFUL, UNKNOWN
    }

    // 自定义 CoroutineScope 用于下载监控任务
    private val downloadScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * 启动下载任务
     *
     * @param context       上下文对象
     * @param url           下载文件的 URL 地址
     * @param title         下载通知栏标题（默认："Downloading..."）
     * @param description   下载通知栏描述（默认为空）
     * @param destinationUri 文件保存的目的地 URI
     * @param mimeType      文件 MIME 类型，可选
     * @return              返回下载任务 ID
     */
    fun startDownload(
        context: Context = SIKCore.getApplication(),
        url: String,
        title: String = "Downloading...",
        description: String = "",
        destinationUri: Uri,
        mimeType: String? = null
    ): Long {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setTitle(title)
            setDescription(description)
            setDestinationUri(destinationUri)
            // 允许在 WiFi 和移动网络下下载
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            // 下载完成后在通知栏显示
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            mimeType?.let { setMimeType(it) }
            // 可在此处增加更多配置，例如设置允许的 roaming 状态等
        }
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        return downloadManager.enqueue(request)
    }

    /**
     * 查询下载任务状态
     *
     * @param context    上下文对象，默认使用 SIKCore 的 Application Context
     * @param downloadId 下载任务 ID
     * @return           返回 DownloadStatus 枚举值
     */
    fun getDownloadStatus(
        context: Context = SIKCore.getApplication(),
        downloadId: Long
    ): DownloadStatus {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val query = DownloadManager.Query().apply { setFilterById(downloadId) }
        var status = DownloadStatus.UNKNOWN
        val cursor: Cursor? = downloadManager.query(query)
        cursor?.use {
            if (it.moveToFirst()) {
                when (it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                    DownloadManager.STATUS_FAILED -> status = DownloadStatus.FAILED
                    DownloadManager.STATUS_PAUSED -> status = DownloadStatus.PAUSED
                    DownloadManager.STATUS_PENDING -> status = DownloadStatus.PENDING
                    DownloadManager.STATUS_RUNNING -> status = DownloadStatus.RUNNING
                    DownloadManager.STATUS_SUCCESSFUL -> status = DownloadStatus.SUCCESSFUL
                    else -> status = DownloadStatus.UNKNOWN
                }
            }
        }
        return status
    }

    /**
     * 取消下载任务
     *
     * @param context    上下文对象，默认使用 SIKCore 的 Application Context
     * @param downloadId 下载任务 ID
     */
    fun cancelDownload(context: Context = SIKCore.getApplication(), downloadId: Long) {
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.remove(downloadId)
    }

    /**
     * 下载任务监控回调接口
     */
    interface DownloadCallback {
        /**
         * 下载进度更新回调
         *
         * @param progress 下载进度百分比（0-100）
         */
        fun onProgress(progress: Int)

        /**
         * 下载完成回调
         *
         * @param fileUri 下载文件的 URI
         */
        fun onComplete(fileUri: Uri)

        /**
         * 下载失败回调
         *
         * @param error 下载错误信息
         */
        fun onError(error: String)
    }

    /**
     * 监控下载任务进度
     *
     * 该方法会在后台线程中定时查询下载状态，并在主线程中回调通知。
     *
     * @param context                上下文对象，默认使用 SIKCore 的 Application Context
     * @param downloadId             下载任务 ID
     * @param destinationUri         文件保存的目的地 URI
     * @param callback               下载回调接口，用于通知进度、完成或失败
     * @param pollingIntervalMillis  轮询时间间隔，默认 500 毫秒
     * @return                       返回监控任务的 Job，调用者可以根据需要取消任务
     */
    fun monitorDownload(
        context: Context = SIKCore.getApplication(),
        downloadId: Long,
        destinationUri: Uri,
        callback: DownloadCallback,
        pollingIntervalMillis: Long = 500L
    ): Job {
        return downloadScope.launch {
            var lastProgress = 0
            try {
                while (isActive) { // 使用 isActive 检查任务是否被取消
                    when (val status = getDownloadStatus(context, downloadId)) {
                        DownloadStatus.RUNNING -> {
                            val downloadManager =
                                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                            val query = DownloadManager.Query().apply { setFilterById(downloadId) }
                            val cursor: Cursor? = downloadManager.query(query)
                            var progress = lastProgress
                            cursor?.use {
                                if (it.moveToFirst()) {
                                    val bytesDownloaded =
                                        it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                                    val bytesTotal =
                                        it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                                    if (bytesTotal > 0) {
                                        progress = ((bytesDownloaded * 100L) / bytesTotal).toInt()
                                    }
                                }
                            }
                            if (progress != lastProgress) {
                                lastProgress = progress
                                withContext(Dispatchers.Main) {
                                    callback.onProgress(progress)
                                }
                            }
                        }

                        DownloadStatus.SUCCESSFUL -> {
                            withContext(Dispatchers.Main) {
                                callback.onComplete(destinationUri)
                            }
                            break
                        }

                        DownloadStatus.FAILED -> {
                            withContext(Dispatchers.Main) {
                                callback.onError("下载失败")
                            }
                            break
                        }

                        else -> {
                            // PENDING 或 PAUSED 状态下可以选择继续轮询或通知用户
                        }
                    }
                    delay(pollingIntervalMillis)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError(e.message ?: "Unknown error")
                }
            }
        }
    }
}
