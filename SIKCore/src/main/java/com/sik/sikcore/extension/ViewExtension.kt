package com.sik.sikcore.extension

import android.content.res.Resources
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver

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