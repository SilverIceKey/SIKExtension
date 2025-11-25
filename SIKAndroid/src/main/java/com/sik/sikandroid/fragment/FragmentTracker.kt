package com.sik.sikandroid.fragment

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.sik.sikcore.explain.LogInfo
import kotlin.reflect.full.findAnnotation

/**
 * 全局 Fragment 生命周期追踪器。
 *
 * 此工具类可用于在运行时监听所有 Fragment 的生命周期事件，
 * 例如：onCreated、onResumed、onDestroyed，并自动输出带 @LogInfo 注解的日志。
 *
 * 用法建议：
 * - 在 Application 中注册 ActivityLifecycleCallbacks，并在 onActivityCreated 中调用 [FragmentTracker.register]
 * - 在 Fragment 上添加 @LogInfo(description = "...") 可自动打印创建或恢复日志
 *
 * 注意：本类依赖于 androidx.fragment 库，仅支持 FragmentActivity 及其派生类。
 */
object FragmentTracker {

    /**
     * 注册 Fragment 生命周期监听器。
     *
     * @param activity 当前的 Activity 实例，仅对 FragmentActivity 生效。
     */
    fun register(activity: Activity) {
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                lifecycleCallback,
                true // 递归监听子 Fragment（包括嵌套 Fragment）
            )
        }
    }

    /**
     * Fragment 生命周期回调对象。
     * 监听创建、恢复、销毁阶段，并根据注解输出日志。
     */
    private val lifecycleCallback = object : FragmentManager.FragmentLifecycleCallbacks() {

        /**
         * Fragment 被创建时调用（等价于 onCreate）。
         */
        override fun onFragmentCreated(
            fm: FragmentManager,
            f: Fragment,
            savedInstanceState: Bundle?
        ) {
            logFragmentInfo(f)
        }

        /**
         * Fragment 进入前台、与用户交互时调用（等价于 onResume）。
         */
        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            logFragmentInfo(f)
        }

        /**
         * Fragment 被销毁时调用（等价于 onDestroy）。
         */
        override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
            Log.d("FragmentTracker","Fragment Destroyed: ${f::class.simpleName}")
        }

        /**
         * 根据 @LogInfo 注解输出 Fragment 日志。
         */
        private fun logFragmentInfo(fragment: Fragment) {
            fragment::class.findAnnotation<LogInfo>()?.let {
                Log.i("FragmentTracker","Fragment [${fragment::class.simpleName}] - ${it.description}")
            }
        }
    }
}
