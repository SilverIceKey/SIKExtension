package com.sik.sikcore.activity

import android.app.Activity
import android.app.Application
import android.content.res.Configuration
import android.os.Bundle
import com.sik.sikcore.explain.LogInfo
import org.slf4j.LoggerFactory
import java.lang.ref.WeakReference
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions

/**
 * Activity追踪
 */
object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var currentActivity: WeakReference<Activity>? = null
    private var lastNightMode = -1
    private val logger = LoggerFactory.getLogger(ActivityTracker::class.java)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (isSecureActivity(activity)) {
            ActivityUtil.setSecure(activity)
        }
        currentActivity = WeakReference(activity)
    }

    /**
     * 检查是否有安全界面注解
     */
    private fun isSecureActivity(activity: Activity): Boolean {
        val secureActivity = activity.javaClass.kotlin.findAnnotation<SecureActivity>()
        return secureActivity != null
    }

    /**
     * 获取夜间模式监听器
     */
    private fun getNightModeChangeListener(activity: Activity): KFunction<*>? {
        return activity::class.functions.find { it.findAnnotation<NightModeChangeListener>() != null }
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = WeakReference(activity)
        val currentNightMode =
            activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode != lastNightMode) {
            lastNightMode = currentNightMode
            if (lastNightMode == Configuration.UI_MODE_NIGHT_YES) {
                getNightModeChangeListener(activity)?.call(activity, lastNightMode)
            } else if (lastNightMode == Configuration.UI_MODE_NIGHT_NO) {
                getNightModeChangeListener(activity)?.call(activity, lastNightMode)
            }
        }
        logActivityInfo()
    }

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {
        if (isSecureActivity(activity)) {
            ActivityUtil.clearSecure(activity)
        }
        currentActivity?.get()?.let {
            if (it == activity) {
                currentActivity = null
            }
        }
    }

    /**
     * 获取当前的Activity
     */
    fun getCurrentActivity(): Activity? {
        return currentActivity?.get()
    }

    /**
     * Activity注解日志信息输出
     */
    private fun logActivityInfo() {
        currentActivity?.get()?.let {
            val activityClass = it::class
            // 检查类上的注解
            activityClass.findAnnotation<LogInfo>()?.let { logInfo ->
                logger.info(logInfo.description)
            }
        }
    }

}
