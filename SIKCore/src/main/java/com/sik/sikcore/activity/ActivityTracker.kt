package com.sik.sikcore.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference
import kotlin.reflect.full.findAnnotation

/**
 * Activity追踪
 */
object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var currentActivity: WeakReference<Activity>? = null

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

    override fun onActivityStarted(activity: Activity) {
        currentActivity = WeakReference(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = WeakReference(activity)
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
}
