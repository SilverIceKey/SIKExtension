package com.sik.sikcore.activity

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.lang.ref.WeakReference

/**
 * Activity追踪
 */
object ActivityTracker : Application.ActivityLifecycleCallbacks {
    private var currentActivity: WeakReference<Activity>? = null

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = WeakReference(activity)
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
