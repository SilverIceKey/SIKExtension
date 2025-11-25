package com.sik.sikandroid.activity

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import com.sik.sikandroid.fragment.FragmentTracker
import com.sik.sikcore.explain.LogInfo
import java.lang.ref.WeakReference
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

/**
 * 全局 Activity 生命周期追踪器。
 *
 * 功能概述：
 * 1. 获取当前处于活跃状态的 Activity / LifecycleOwner 实例。
 * 2. 支持 @LogInfo 注解自动输出 Activity 日志信息。
 * 3. 支持 @SecureActivity 注解实现界面防录屏。
 * 4. 支持 @NightModeChangeListener 注解监听夜间模式切换。
 *
 * ⚠️ 使用说明：
 * 请在 Application 中注册：
 * application.registerActivityLifecycleCallbacks(ActivityTracker)
 */
object ActivityTracker : Application.ActivityLifecycleCallbacks {

    // 当前活跃的 Activity（弱引用避免泄漏）
    private var currentActivity: WeakReference<Activity>? = null

    // 当前活跃的 LifecycleOwner（一般与 currentActivity 相同，但支持外部访问）
    private var currentLifecycleOwner: WeakReference<LifecycleOwner>? = null

    // 上一次记录的夜间模式状态
    private var lastNightMode: Int = -1

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        // 安全注解处理：防录屏设置
        if (isSecureActivity(activity)) {
            ActivityUtil.setSecure(activity)
        }

        currentActivity = WeakReference(activity)

        // 注册 Fragment 生命周期追踪器（可选，如果启用了 FragmentTracker）
        FragmentTracker.register(activity)

        // 保存 LifecycleOwner（如 AppCompatActivity、ComponentActivity）
        if (activity is LifecycleOwner) {
            currentLifecycleOwner = WeakReference(activity)
        }
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = WeakReference(activity)

        if (activity is LifecycleOwner) {
            currentLifecycleOwner = WeakReference(activity)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = WeakReference(activity)

        if (activity is LifecycleOwner) {
            currentLifecycleOwner = WeakReference(activity)
        }

        // 监听夜间模式切换
        val currentNightMode =
            activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode != lastNightMode) {
            lastNightMode = currentNightMode

            if (activity is NightModeAware) {
                activity.onNightModeChanged(lastNightMode)
            }
        }

        // 注解日志输出（@LogInfo）
        logActivityInfo()
    }

    override fun onActivityPaused(activity: Activity) {
        // 暂不处理
    }

    override fun onActivityStopped(activity: Activity) {
        // 暂不处理
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // 暂不处理
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (isSecureActivity(activity)) {
            ActivityUtil.clearSecure(activity)
        }

        currentActivity?.get()?.let {
            if (it == activity) {
                currentActivity = null
            }
        }

        if (currentLifecycleOwner?.get() == activity) {
            currentLifecycleOwner = null
        }
    }

    // region 注解支持

    /**
     * 检查是否标注了 @SecureActivity 注解
     */
    private fun isSecureActivity(activity: Activity): Boolean {
        return activity::class.findAnnotation<SecureActivity>() != null
    }

    /**
     * 输出 @LogInfo 注解内容到日志中
     */
    private fun logActivityInfo() {
        currentActivity?.get()?.let { activity ->
            activity::class.findAnnotation<LogInfo>()?.let { logInfo ->
                Log.i(
                    "ActivityTracker",
                    "Activity [${activity::class.simpleName}] - ${logInfo.description}"
                )
            }
        }
    }

    // endregion

    // region 外部访问接口

    /**
     * 获取当前活跃的 Activity 实例（可能为 null）
     */
    fun getCurrentActivity(): Activity? {
        return currentActivity?.get()
    }

    /**
     * 获取当前活跃的 LifecycleOwner（通常等于当前 Activity）
     */
    fun getCurrentLifecycleOwner(): LifecycleOwner? {
        return currentLifecycleOwner?.get()
    }

    // endregion
}
