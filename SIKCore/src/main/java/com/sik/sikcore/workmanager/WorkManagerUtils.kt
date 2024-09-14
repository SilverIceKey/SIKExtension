package com.sik.sikcore.workmanager

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * WorkManager 工具类，用于管理工作任务。
 */
object WorkManagerUtils {

    // 延迟初始化 WorkManager 实例
    @Volatile
    private var workManager: WorkManager? = null

    /**
     * 初始化 WorkManager 实例。应在应用启动时调用，例如在 Application 类中。
     *
     * @param context 应用上下文
     */
    fun initialize(context: Context) {
        if (workManager == null) {
            synchronized(this) {
                if (workManager == null) {
                    workManager = WorkManager.getInstance(context.applicationContext)
                }
            }
        }
    }

    /**
     * 获取 WorkManager 实例。
     *
     * @return WorkManager 实例
     * @throws IllegalStateException 如果 WorkManager 未初始化
     */
    private fun getWorkManager(): WorkManager {
        return workManager
            ?: throw IllegalStateException("WorkManagerUtils 未初始化，请先调用 initialize() 方法。")
    }

    /**
     * 创建并启动一个唯一的 OneTimeWorkRequest。
     *
     * @param uniqueWorkName 唯一工作名称
     * @param builder 用于构建 WorkRequest 的 WorkRequestBuilder 实现
     * @param existingWorkPolicy 如果已有同名工作，如何处理
     * @return 工作的 ID
     */
    fun enqueueUniqueOneTimeWork(
        uniqueWorkName: String,
        builder: WorkRequestBuilder,
        existingWorkPolicy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ): String {
        val workRequest = builder.buildOneTimeWorkRequest()
        getWorkManager().enqueueUniqueWork(uniqueWorkName, existingWorkPolicy, workRequest)
        return workRequest.id.toString()
    }

    /**
     * 创建并启动一个唯一的 PeriodicWorkRequest。
     *
     * @param uniqueWorkName 唯一工作名称
     * @param builder 用于构建 WorkRequest 的 WorkRequestBuilder 实现
     * @param existingWorkPolicy 如果已有同名工作，如何处理
     * @return 工作的 ID
     */
    fun enqueueUniquePeriodicWork(
        uniqueWorkName: String,
        builder: WorkRequestBuilder,
        existingWorkPolicy: ExistingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.REPLACE
    ): String {
        val workRequest = builder.buildPeriodicWorkRequest()
        getWorkManager().enqueueUniquePeriodicWork(uniqueWorkName, existingWorkPolicy, workRequest)
        return workRequest.id.toString()
    }

    /**
     * 取消指定名称的工作。
     *
     * @param uniqueWorkName 工作的唯一名称
     */
    fun cancelWorkByName(uniqueWorkName: String) {
        getWorkManager().cancelUniqueWork(uniqueWorkName)
    }

    /**
     * 取消所有工作。
     */
    fun cancelAllWork() {
        getWorkManager().cancelAllWork()
    }

    /**
     * 暂停工作。由于 WorkManager 不直接支持暂停工作，
     * 这里通过设置 Work 的约束来实现暂停效果。
     *
     * @param uniqueWorkName 工作的唯一名称
     */
    @SuppressLint("RestrictedApi")
    suspend fun pauseWork(uniqueWorkName: String) {
        // 获取当前的 WorkInfos
        val workInfos = withContext(Dispatchers.IO) {
            getWorkManager().getWorkInfosForUniqueWork(uniqueWorkName).await()
        }

        // 遍历并取消它们
        workInfos.forEach { workInfo ->
            if (workInfo.state == WorkInfo.State.ENQUEUED || workInfo.state == WorkInfo.State.RUNNING) {
                getWorkManager().cancelWorkById(workInfo.id)
            }
        }
    }

    /**
     * 恢复被暂停的工作。这里通过重新排队被取消的工作来实现恢复。
     *
     * @param uniqueWorkName 工作的唯一名称
     * @param builder 用于构建 WorkRequest 的 WorkRequestBuilder 实现
     * @param existingWorkPolicy 如果已有同名工作，如何处理
     * @return 工作的 ID
     */
    fun resumeWork(
        uniqueWorkName: String,
        builder: WorkRequestBuilder,
        existingWorkPolicy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ): String {
        // 重新排队工作
        return enqueueUniqueOneTimeWork(uniqueWorkName, builder, existingWorkPolicy)
    }

    /**
     * 停止工作。停止工作与取消工作相似，取消当前正在执行的工作。
     *
     * @param uniqueWorkName 工作的唯一名称
     */
    fun stopWork(uniqueWorkName: String) {
        cancelWorkByName(uniqueWorkName)
    }

    /**
     * 观察工作状态。
     *
     * @param uniqueWorkName 工作的唯一名称
     * @param observer 用于观察 WorkInfo 的 Observer
     */
    fun observeWorkStatus(uniqueWorkName: String, observer: (List<WorkInfo>) -> Unit) {
        getWorkManager().getWorkInfosForUniqueWorkLiveData(uniqueWorkName)
            .observeForever { workInfos ->
                observer(workInfos)
            }
    }
}
