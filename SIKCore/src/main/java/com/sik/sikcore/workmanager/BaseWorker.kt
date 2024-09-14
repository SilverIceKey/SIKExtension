package com.sik.sikcore.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/**
 * 基类工作类，供用户继承并复写 doWork 方法。
 */
open class BaseWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        // 用户应在子类中复写此方法以实现具体任务
        return Result.success()
    }
}
