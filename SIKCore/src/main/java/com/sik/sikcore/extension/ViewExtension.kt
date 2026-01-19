package com.sik.sikcore.extension

import android.animation.ObjectAnimator
import android.content.res.Resources
import android.os.SystemClock
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import java.util.WeakHashMap

/**
 * Do after rendered
 * 在View渲染完之后执行的操作
 * @param task
 * @receiver
 */
fun View.doAfterRendered(task: () -> Unit = {}) {
    val onRenderedListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            this@doAfterRendered.viewTreeObserver.removeOnGlobalLayoutListener(this)
            task()
        }
    }
    this.viewTreeObserver.addOnGlobalLayoutListener(onRenderedListener)
}

/**
 * Dp2px
 * dp转px
 * @return
 */
fun Number.dp2px(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    )
}

/**
 * Sp2px
 * sp转px
 * @return
 */
fun Number.sp2px(): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this.toFloat(), Resources.getSystem().displayMetrics
    )
}

/**
 * To dp
 * px转dp
 * @return
 */
fun Number.toDp(): Float {
    return this.toFloat() / (Resources.getSystem().displayMetrics.densityDpi / 160)
}

/**
 * ProgressBar的扩展函数，实现平滑更新进度。
 * @param newProgress 要更新到的新进度值。
 * @param duration 动画持续时间，单位为毫秒。
 */
fun ProgressBar.setProgressSmoothly(newProgress: Int, duration: Long = 500) {
    ObjectAnimator.ofInt(this, "progress", progress, newProgress).apply {
        this.duration = duration
        interpolator = DecelerateInterpolator()
        start()
    }
}

val debouncedClickTime: WeakHashMap<Any, Long> = WeakHashMap()

// 扩展函数：为 View 设置防抖的点击事件监听器
fun View.setDebouncedClickListener(
    debounceMillis: Long = 500L,
    listener: (View) -> Unit
) {
    setOnClickListener { v ->
        val lastClickTime = debouncedClickTime[v] ?: 0L
        val currentTime = SystemClock.elapsedRealtime()

        if (currentTime - lastClickTime >= debounceMillis) {
            debouncedClickTime[v] = currentTime
            listener(v)
        }
    }
}