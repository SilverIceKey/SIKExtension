package com.sik.sikcore.workmanager

import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest

/**
 * 接口用于创建 WorkRequest。
 */
interface WorkRequestBuilder {
    /**
     * 创建一个 OneTimeWorkRequest。
     *
     * @return 创建的 OneTimeWorkRequest
     */
    fun buildOneTimeWorkRequest(): OneTimeWorkRequest

    /**
     * 创建一个 PeriodicWorkRequest。
     *
     * @return 创建的 PeriodicWorkRequest
     */
    fun buildPeriodicWorkRequest(): PeriodicWorkRequest
}
