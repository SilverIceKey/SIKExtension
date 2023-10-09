package com.sik.sikcore.date

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import java.util.concurrent.TimeUnit

/**
 * Date manager utils
 * 日程管理工具类
 * @constructor Create empty Date manager utils
 */
object ScheduleManagerUtils {
    /**
     * Add one time work
     * 添加一次性任务
     * @param T
     * @param context
     * @param initialDelay
     * @param workId
     */
    @JvmOverloads
    inline fun <reified T : Worker> addOneTimeWork(
        context: Context, initialDelay: Long = 0L, workId: Long
    ) {
        val uploadWorkRequest: WorkRequest =
            OneTimeWorkRequestBuilder<T>().setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "$workId",
                ExistingWorkPolicy.REPLACE,
                uploadWorkRequest as OneTimeWorkRequest
            )
    }

    /**
     * Cancel
     *
     * @param context
     * @param workId
     */
    fun cancel(context: Context, workId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork("$workId")
    }
}